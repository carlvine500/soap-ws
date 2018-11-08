/**
 * Copyright (c) 2012-2013 Reficio (TM) - Reestablish your software!. All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.reficio.ws.client.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.*;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.conn.routing.RouteInfo;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeLayeredSocketFactory;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.reficio.ws.SoapException;
import org.reficio.ws.annotation.ThreadSafe;
import org.reficio.ws.client.SoapClientException;
import org.reficio.ws.client.TransmissionException;
import org.reficio.ws.client.ssl.SSLUtils;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.reficio.ws.client.core.SoapConstants.*;

/**
 * SOAP client enables the user to communicate with a SOAP server on a purely XML level.
 * It supports SSL/TLS, basic-authentication and java.net.Proxy.
 * When it comes to SOAP it supports version 1.1 and 1.2 - SOAPAction attribute is automatically properly placed,
 * either in the header (SOAP 1.1) or in the content (SOAP 1.2).
 * SOAP version recognition is based on the SOAP namespace included in the payload.
 * This class may throw an unchecked @see org.reficio.ws.client.SoapClientException
 *
 * @author Tom Bujok
 * @since 1.0.0
 */
@ThreadSafe
public final class SoapClient {

    private final static Log log = LogFactory.getLog(SoapClient.class);

    private final static String NULL_SOAP_ACTION = null;

    private int readTimeoutInMillis;
    private int connectTimeoutInMillis;

    private URI endpointUri;
    private Security endpointProperties;
    private boolean endpointTlsEnabled;

    private URI proxyUri;
    private Security proxyProperties;
    private boolean proxyTlsEnabled;

    private CloseableHttpClient client;
    private Integer keepAliveMillis;
    private Integer maxTotalConn;
    private Integer maxConnectionsPerRoute;
    private Integer waitTimeout;
    private HttpClientContext httpClientContext;


    // ----------------------------------------------------------------
    // PUBLIC API
    // ----------------------------------------------------------------

    /**
     * Post the SOAP message to the SOAP server without specifying the SOAPAction
     *
     * @param requestEnvelope SOAP message envelope
     * @return The result returned by the SOAP server
     */
    public String post(String requestEnvelope) {
        return post(NULL_SOAP_ACTION, requestEnvelope);
    }

    /**
     * Post the SOAP message to the SOAP server specifying the SOAPAction
     *
     * @param soapAction      SOAPAction attribute
     * @param requestEnvelope SOAP message envelope
     * @return The result returned by the SOAP server
     */
    public String post(String soapAction, String requestEnvelope) {
        log.debug(String.format("Sending request to host=[%s] action=[%s] request:%n%s", endpointUri.toString(),
                soapAction, requestEnvelope));
        String response = transmit(soapAction, requestEnvelope);
        log.debug("Received response:\n" + requestEnvelope);
        return response;
    }

    /**
     * Disconnects from the SOAP server
     * Underlying connection is a persistent connection by default:
     *
     * refer: http://docs.oracle.com/javase/1.5.0/docs/guide/net/http-keepalive.html
     */
    public void disconnect() {
        if (client != null) {
            client.getConnectionManager().shutdown();
        }
    }

    // ----------------------------------------------------------------
    // TRANSMISSION API
    // ----------------------------------------------------------------
    private HttpPost generatePost(String soapAction, String requestEnvelope) {
        try {
            HttpPost post = new HttpPost(endpointUri.toString());
            StringEntity contentEntity = new StringEntity(requestEnvelope, "UTF-8");
            contentEntity.setContentEncoding("UTF-8");
            post.setEntity(contentEntity);
            if (requestEnvelope.contains(SOAP_1_1_NAMESPACE)) {
                soapAction = soapAction != null ? "\"" + soapAction + "\"" : "";
                post.addHeader(PROP_SOAP_ACTION_11, soapAction);
                post.addHeader(PROP_CONTENT_TYPE, MIMETYPE_TEXT_XML);
//                client.getParams().setParameter(PROP_CONTENT_TYPE, MIMETYPE_TEXT_XML);
//                post.addHeader(PROP_CONTENT_TYPE, MIMETYPE_TEXT_XML);
            } else if (requestEnvelope.contains(SOAP_1_2_NAMESPACE)) {
                String contentType = MIMETYPE_APPLICATION_XML;
                if (soapAction != null) {
                    contentType = contentType + PROP_DELIMITER + PROP_SOAP_ACTION_12 + "\"" + soapAction + "\"";
                }
                post.addHeader(PROP_CONTENT_TYPE, contentType);
            }
            return post;
        } catch (Exception ex) {
            throw new SoapClientException(ex);
        }
    }

    private String transmit(String soapAction, String data) {
        HttpPost post = generatePost(soapAction, data);
        return executePost(post);
    }

    private String executePost(HttpPost post) {
        try {
            HttpResponse response = client.execute(post, httpClientContext);
            StatusLine statusLine = response.getStatusLine();
            HttpEntity entity = response.getEntity();
            String result = entity == null ? null : EntityUtils.toString(entity);
            if (statusLine.getStatusCode() >= 300) {
//                EntityUtils.consume(entity);
                throw new TransmissionException(statusLine.getReasonPhrase() + ",result=" + result, statusLine.getStatusCode());
            }
            return result;
        } catch (SoapException ex) {
            throw ex;
        } catch (ConnectTimeoutException ex) {
            throw new TransmissionException("Connection timed out", ex);
        } catch (IOException ex) {
            throw new TransmissionException("Transmission failed", ex);
        } catch (RuntimeException ex) {
            post.abort();
            throw new TransmissionException("Transmission aborted", ex);
        }
    }

    // ----------------------------------------------------------------
    // INITIALIZATION API
    // ----------------------------------------------------------------
    private void initialize() {
        HttpClientBuilder httpClientBuilder = HttpClients.custom();
        configureClient(httpClientBuilder);
        configureAuthentication();
        configureTls(httpClientBuilder);
        configureProxy(httpClientBuilder);
        client = httpClientBuilder.build();
    }


//    static {
//        HttpParams params = new BasicHttpParams();
//        Integer CONNECTION_TIMEOUT = 5 * 1000; //设置请求超时2秒钟 根据业务调整
//        Integer SO_TIMEOUT = 2 * 1000; //设置等待数据超时时间2秒钟 根据业务调整
//        Long CONN_MANAGER_TIMEOUT = 500L; //该值就是连接不够用的时候等待超时时间，一定要设置，而且不能太大
//
//        params.setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, CONNECTION_TIMEOUT);
//        params.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, SO_TIMEOUT);
//        params.setLongParameter(ClientPNames.CONN_MANAGER_TIMEOUT, CONN_MANAGER_TIMEOUT);
//        params.setBooleanParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, true);
//
//        conMgr = new PoolingHttpClientConnectionManager();
//        conMgr.setMaxTotal(2000);
//
//        conMgr.setDefaultMaxPerRoute(conMgr.getMaxTotal());
//    }

    private ConnectionKeepAliveStrategy keepAliveStrategy = new ConnectionKeepAliveStrategy() {
        @Override
        public long getKeepAliveDuration(HttpResponse response,
                                         HttpContext context) {
            HeaderElementIterator it = new BasicHeaderElementIterator(
                    response.headerIterator(org.apache.http.protocol.HTTP.CONN_KEEP_ALIVE));
            while (it.hasNext()) {
                HeaderElement he = it.nextElement();
                String param = he.getName();
                String value = he.getValue();
                if (value != null && param.equalsIgnoreCase("timeout")) {
                    return Long.parseLong(value) * 1000;
                }
            }
            return keepAliveMillis;
        }
    };

    private PoolingHttpClientConnectionManager connManager = null;

    private void configureClient(HttpClientBuilder httpClientBuilder) {
//        client = new DefaultHttpClient(conMgr);
//        HttpParams httpParameters = new BasicHttpParams();
//        HttpConnectionParams.setConnectionTimeout(httpParameters, connectTimeoutInMillis);
//        HttpConnectionParams.setSoTimeout(httpParameters, readTimeoutInMillis);
//        HttpClients.custom().setConnectionManager(conMgr);
        // Increase max total connection
        Registry<ConnectionSocketFactory> r = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", SSLSelfSigned.SSL_CONNECTION_SOCKET_FACTORY).build();
        connManager = new PoolingHttpClientConnectionManager(r);
        connManager.setMaxTotal(maxTotalConn);
        // Increase default max connection per route
        connManager.setDefaultMaxPerRoute(maxConnectionsPerRoute);

        // config timeout
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(connectTimeoutInMillis)
                .setConnectionRequestTimeout(waitTimeout)
                .setSocketTimeout(readTimeoutInMillis).build();

        httpClientBuilder
                .setKeepAliveStrategy(keepAliveStrategy)
                .setConnectionManager(connManager)
                .setDefaultRequestConfig(config);
    }

    public static class  SSLSelfSigned {
        public static final SSLConnectionSocketFactory SSL_CONNECTION_SOCKET_FACTORY;
        //	protected static final Logger logger = LoggerFactory.getLogger(SSLSelfSigned.class);
        private final static Logger logger = Logger.getLogger(SSLSelfSigned.class);

        static {
            SSLContext sslContext = null;
            try {
                sslContext = SSLContexts.custom().loadTrustMaterial((org.apache.http.conn.ssl.TrustStrategy)org.apache.http.conn.ssl.TrustSelfSignedStrategy.INSTANCE).build();
            } catch (KeyManagementException e) {
                logger.error("{}", e);
            } catch (NoSuchAlgorithmException e) {
                logger.error("{}", e);
            } catch (KeyStoreException e) {
                logger.error("{}", e);
            }
            SSL_CONNECTION_SOCKET_FACTORY = new SSLConnectionSocketFactory(sslContext,
                    NoopHostnameVerifier.INSTANCE);
        }
        private SSLSelfSigned() {
        }
    }

    private void configureAuthentication() {
        configureAuthentication(endpointUri, endpointProperties);
        configureAuthentication(proxyUri, proxyProperties);
    }

    private void configureAuthentication(URI uri, Security security) {
        if (security.isAuthEnabled()) {
            AuthScope scope = new AuthScope(uri.getHost(), uri.getPort());
            Credentials credentials = null;
            if (security.isAuthBasic()) {
                credentials = new UsernamePasswordCredentials(security.getAuthUsername(), security.getAuthPassword());
            } else if (security.isAuthDigest()) {
                credentials = new UsernamePasswordCredentials(security.getAuthUsername(), security.getAuthPassword());
            } else if (security.isAuthNtlm()) {
                // TODO
                credentials = new NTCredentials(security.getAuthUsername(), security.getAuthPassword(), null, null);
            } else if (security.isAuthSpnego()) {
                // TODO
            }
//            client.getCredentialsProvider().setCredentials(scope, credentials);
            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            HttpClientContext httpClientContext = HttpClientContext.create();
            credentialsProvider.setCredentials(scope, credentials);
            httpClientContext.setCredentialsProvider(credentialsProvider);
            this.httpClientContext = httpClientContext;
        }
    }

    private void configureTls(HttpClientBuilder httpClientBuilder) {
//        SSLSocketFactory factory;
//        int port;
//        try {
//            if (endpointTlsEnabled && proxyTlsEnabled) {
//                factory = SSLUtils.getMergedSocketFactory(endpointProperties, proxyProperties);
//                registerTlsScheme(factory, proxyUri.getPort());
//            } else if (endpointTlsEnabled) {
//                factory = SSLUtils.getFactory(endpointProperties);
//                port = endpointUri.getPort();
//                registerTlsScheme(factory, port);
//            } else if (proxyTlsEnabled) {
//                factory = SSLUtils.getFactory(proxyProperties);
//                port = proxyUri.getPort();
//                registerTlsScheme(factory, port);
//            }
//        } catch (GeneralSecurityException ex) {
//            throw new SoapClientException(ex);
//        }
        try {
            SSLConnectionSocketFactory sslConnectionSocketFactory = null;
            if (endpointTlsEnabled && proxyTlsEnabled) {
                sslConnectionSocketFactory = SSLUtils.getMergedSocketFactoryNew(endpointProperties, proxyProperties);
            } else if (endpointTlsEnabled) {
                sslConnectionSocketFactory = SSLUtils.getFactoryNew(endpointProperties);
            } else if (proxyTlsEnabled) {
                sslConnectionSocketFactory = SSLUtils.getFactoryNew(proxyProperties);
            }
            if (sslConnectionSocketFactory != null) {
                httpClientBuilder.setSSLSocketFactory(sslConnectionSocketFactory);
            }
        } catch (GeneralSecurityException ex) {
            throw new SoapClientException(ex);
        }

    }

    private void registerTlsScheme(SchemeLayeredSocketFactory factory, int port) {
        Scheme sch = new Scheme(HTTPS, port, factory);
        client.getConnectionManager().getSchemeRegistry().register(sch);
    }

    private void configureProxy(HttpClientBuilder httpClientBuilder) {
        if (proxyUri == null) {
            return;
        }
        if (proxyTlsEnabled) {
            final HttpHost proxy = new HttpHost(proxyUri.getHost(), proxyUri.getPort(), HTTPS);
            // https://issues.apache.org/jira/browse/HTTPCLIENT-1318
            // http://stackoverflow.com/questions/15048102/httprouteplanner-how-does-it-work-with-an-https-proxy
            // To make the HttpClient talk to a HTTP End-site through an HTTPS Proxy, the route should be secure,
            //  but there should not be any Tunnelling or Layering.
            if (!endpointTlsEnabled) {
                httpClientBuilder.setRoutePlanner(new HttpRoutePlanner() {
                    @Override
                    public HttpRoute determineRoute(HttpHost target, HttpRequest request, HttpContext context) {
                        return new HttpRoute(target, null, proxy, true, RouteInfo.TunnelType.PLAIN, RouteInfo.LayerType.PLAIN);
                    }
                });
            }
//            client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
            httpClientBuilder.setProxy(proxy);
        } else {
            HttpHost proxy = new HttpHost(proxyUri.getHost(), proxyUri.getPort());
//            client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
            httpClientBuilder.setProxy(proxy);
        }
    }


    // ----------------------------------------------------------------
    // BUILDER API
    // ----------------------------------------------------------------
    private SoapClient() {
    }

    /**
     * Builder to construct a properly populated SoapClient
     */
    public static class Builder {
        public static final int DEFAULT_READ_TIMEOUT_MILLISECONDS = (15 * 1000);
        public static final int DEFAULT_CONNECTION_TIMEOUT_MILLISECONDS = (10 * 1000);
        public static final int DEFAULT_WAIT_TIMEOUT_MILLISECONDS = (10 * 1000);
        public static final int DEFAULT_KEEP_ALIVE_MILLISECONDS = (1 * 60 * 1000);
        public static final int DEFAULT_MAX_TOTAL_CONNECTIONS = 200;
        public static final int DEFAULT_MAX_CONNECTIONS_PER_ROUTE = 200;



        private Integer readTimeoutInMillis = DEFAULT_READ_TIMEOUT_MILLISECONDS;
        private Integer connectTimeoutInMillis = DEFAULT_CONNECTION_TIMEOUT_MILLISECONDS;
        private Integer keepAliveMillis= DEFAULT_KEEP_ALIVE_MILLISECONDS;
        private Integer maxTotalConn= DEFAULT_MAX_TOTAL_CONNECTIONS;
        private Integer maxConnectionsPerRoute= DEFAULT_MAX_CONNECTIONS_PER_ROUTE;
        private Integer waitTimeout = DEFAULT_WAIT_TIMEOUT_MILLISECONDS;

        private URI endpointUri;
        private Security endpointProperties;
        private boolean endpointTlsEnabled;

        private URI proxyUri;
        private Security proxyProperties;
        private boolean proxyTlsEnabled;

        /**
         * @param value URL of the SOAP endpoint to whom the client should send messages. Null is not accepted.
         * @return builder
         */
        public Builder endpointUri(String value) {
            checkNotNull(value);
            try {
                URI uri = new URI(value);
                return endpointUri(uri);
            } catch (URISyntaxException ex) {
                throw new SoapClientException(String.format("URI [%s] is malformed", value), ex);
            }
        }

        /**
         * @param value URL of the SOAP endpoint to whom the client should send messages. Null is not accepted.
         * @return builder
         */
        public Builder endpointUri(URI value) {
            endpointUri = checkNotNull(value);
            endpointTlsEnabled = value.getScheme().equalsIgnoreCase(HTTPS);
            return this;
        }

        /**
         * @param value URL of the SOAP endpoint to whom the client should send messages. Null is not accepted.
         * @return builder
         */
        public Builder proxyUri(String value) {
            checkNotNull(value);
            try {
                URI uri = new URI(value);
                return proxyUri(uri);
            } catch (URISyntaxException ex) {
                throw new SoapClientException(String.format("URI [%s] is malformed", value), ex);
            }
        }

        /**
         * @param value URL of the SOAP endpoint to whom the client should send messages. Null is not accepted.
         * @return builder
         */
        public Builder proxyUri(URI value) {
            proxyUri = checkNotNull(value);
            proxyTlsEnabled = value.getScheme().equalsIgnoreCase(HTTPS);
            return this;
        }

        public Builder endpointSecurity(Security value) {
            this.endpointProperties = checkNotNull(value);
            return this;
        }

        public Builder proxySecurity(Security value) {
            this.proxyProperties = checkNotNull(value);
            return this;
        }

        /**
         * @param value Specifies the timeout in millisecond for the read operation. Has to be not negative.
         * @return builder
         */
        public Builder readTimeoutInMillis(int value) {
            checkArgument(value >= 0);
            readTimeoutInMillis = value;
            return this;
        }

        /**
         * @param value Specifies the timeout in millisecond for the connect operation. Has to be not negative.
         * @return builder
         */
        public Builder connectTimeoutInMillis(int value) {
            checkArgument(value >= 0);
            connectTimeoutInMillis = value;
            return this;
        }

        /**
         * Constructs properly populated soap client
         *
         * @return properly populated soap clients
         */
        public SoapClient build() {
            return initializeClient();
        }

        private SoapClient initializeClient() {
            SoapClient client = new SoapClient();
            client.endpointUri = endpointUri;
            if (endpointProperties == null) {
                endpointProperties = Security.builder().build();
            }
            client.endpointProperties = endpointProperties;
            client.endpointTlsEnabled = endpointTlsEnabled;

            client.proxyUri = proxyUri;
            if (proxyProperties == null) {
                proxyProperties = Security.builder().build();
            }
            client.proxyProperties = proxyProperties;
            client.proxyTlsEnabled = proxyTlsEnabled;

            client.readTimeoutInMillis = readTimeoutInMillis;
            client.connectTimeoutInMillis = connectTimeoutInMillis;
            client.keepAliveMillis = keepAliveMillis;
            client.maxTotalConn = maxTotalConn;
            client.maxConnectionsPerRoute = maxConnectionsPerRoute;
            client.waitTimeout = waitTimeout;

            client.initialize();
            return client;
        }
    }

    /**
     * @return a new instance of a SoapClient Builder
     */
    public static Builder builder() {
        return new Builder();
    }

}