package org.reficio.ws.quickstart.auth;

import org.apache.cxf.binding.soap.saaj.SAAJOutInterceptor;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.handler.WSHandlerConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tingfeng on 2018/8/9.
 */
public class AuthDigestClientTest {

    public static void main(String[] args) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(WSHandlerConstants.ACTION, WSHandlerConstants.USERNAME_TOKEN);
        map.put(WSHandlerConstants.USER, "kevin");
        map.put(WSHandlerConstants.PASSWORD_TYPE, WSConstants.PW_DIGEST);
        map.put(WSHandlerConstants.PW_CALLBACK_CLASS, AuthDigestCallback.class.getName());

        List list = new ArrayList();
        list.add(new SAAJOutInterceptor());
        list.add(new WSS4JOutInterceptor(map));

        JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
        factory.getInInterceptors().add(new LoggingInInterceptor());
        factory.getOutInterceptors().add(new LoggingOutInterceptor());
        factory.setServiceClass(HelloWorld.class);
        factory.setAddress("http://localhost:8081/HelloWorld?wsdl");
        factory.getOutInterceptors().addAll(list);
        HelloWorld es = (HelloWorld) factory.create();

        String u = es.echo("ssss");
        //Integer sui = es.getSendUnitInfo(1L, 2, 3L);
        System.out.println(u);
    }



}
