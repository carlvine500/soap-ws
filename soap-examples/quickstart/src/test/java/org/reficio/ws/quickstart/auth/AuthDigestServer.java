package org.reficio.ws.quickstart.auth;

import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.frontend.ServerFactoryBean;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.service.invoker.BeanInvoker;
import org.apache.cxf.ws.security.wss4j.WSS4JInInterceptor;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSPasswordCallback;
import org.apache.ws.security.handler.WSHandlerConstants;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by tingfeng on 2018/8/9.
 */
public class AuthDigestServer {
    /**
     * NoSuchFieldError: QUALIFIED错误解决:
     * CXF 2.7.x would require the 2.x version of XmlSchema (which you have: xmlschema-core-2.1.0.jar) but it's picking up the 1.4.7 version.
     * @param args
     */
    public static void main(String[] args) {
        ServerFactoryBean svrFactory = new ServerFactoryBean();
        svrFactory.setServiceClass(HelloWorld.class);
        svrFactory.setAddress("http://localhost:8081/HelloWorld?wsdl");
        svrFactory.setInvoker(new BeanInvoker(new HelloWorldImpl()));
        svrFactory.getInInterceptors().add(new LoggingInInterceptor());
        svrFactory.getOutInterceptors().add(new LoggingOutInterceptor());
        Server server = svrFactory.create();
        Endpoint cxfEndpoint = server.getEndpoint();
        Map<String, Object> inProps = new HashMap<String, Object>();
        inProps.put(WSHandlerConstants.ACTION,
                WSHandlerConstants.USERNAME_TOKEN);
//        inProps.put(WSHandlerConstants.PASSWORD_TYPE, WSConstants.PW_DIGEST);
//        inProps.put(WSHandlerConstants.PASSWORD_TYPE, WSConstants.PW_TEXT);
//        inProps.put(WSHandlerConstants.PW_CALLBACK_CLASS,
//                ServerPasswordHandler.class.getName());
//        cxfEndpoint.getInInterceptors().add(new WSS4JInInterceptor(inProps));

    }

    public static class ServerPasswordHandler implements CallbackHandler {
        private String user = "kevin";
        private String password = "111111";
        @Override
        public void handle(Callback[] callbacks) throws IOException,
                UnsupportedCallbackException {
            WSPasswordCallback pc = (WSPasswordCallback) callbacks[0];
            if(user.equals(pc.getIdentifier())){
                pc.setPassword(password);
            }
        }
    }

}
