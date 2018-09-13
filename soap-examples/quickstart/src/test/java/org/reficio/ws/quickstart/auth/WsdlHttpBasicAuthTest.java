package org.reficio.ws.quickstart.auth;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.dom4j.DocumentException;
import org.reficio.ws.builder.SoapBuilder;
import org.reficio.ws.builder.SoapOperation;
import org.reficio.ws.builder.core.Wsdl;
import org.reficio.ws.client.core.Security;
import org.reficio.ws.client.core.SoapClient;
import org.reficio.ws.legacy.HttpClientUtil;

import javax.wsdl.WSDLException;
import java.io.IOException;
import java.net.URL;

/**
 * nginx 配置：
 * <pre>
  server {
 listen 80;
         server_name localhost;
                auth_basic "Restricted";
                auth_basic_user_file /Users/tingfeng/nginx.pass;
                location / {
                    #return 200 "ok1";
                    proxy_pass http://localhost:8080;
                }
 }
 </pre>
 */
public class WsdlHttpBasicAuthTest {
    public static void main(String[] args) throws DocumentException, IOException, WSDLException {
//        URL url = new URL("http://localhost:8080/HelloWorld?wsdl");
//        String httpResult = HttpClientUtil.httpGetRequestAuth("https://localhost/HelloWorld?wsdl","ttlsa:123456");
//        System.out.println(httpResult);
        URL url = new URL("https://localhost:8443/HelloWorld?wsdl");

//        String xml = HttpClientUtil.httpGetRequestAuth(url.toString(), "ttlsa:123456");
//        System.out.println("xml="+xml);
        Wsdl wsdl = Wsdl.parse(url,"ttlsa:123456");
        System.out.println(JSON.toJSONString(wsdl.getBindings(), SerializerFeature.PrettyFormat));

        SoapBuilder builder = wsdl.binding()
                .localPart("SimpleServerServiceSoapBinding")
                .find();
        SoapOperation operation = builder.operation()
                .name("getList")
                .find();
//        RequestConfig
        final String request = builder.buildInputMessage(operation);
        System.out.println("request:\n" + request);
        Security ttlsa = Security.builder().authBasic("ttlsa", "123456").build();
        final SoapClient client = SoapClient.builder()
                .endpointUri(url.toString()).endpointSecurity(ttlsa).build();
        String post = client.post(request);
        System.out.println(post);

    }


}
