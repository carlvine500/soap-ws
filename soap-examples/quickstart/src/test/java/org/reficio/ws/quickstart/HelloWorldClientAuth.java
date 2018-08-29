package org.reficio.ws.quickstart;

import org.dom4j.DocumentException;
import org.reficio.ws.builder.SoapBuilder;
import org.reficio.ws.builder.SoapOperation;
import org.reficio.ws.builder.core.Wsdl;
import org.reficio.ws.client.core.Security;
import org.reficio.ws.client.core.SoapClient;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by tingfeng on 2018/6/25.
 */
public class HelloWorldClientAuth {
    public static void main(String[] args) throws MalformedURLException, DocumentException {
//        String spec = "http://localhost:8080/HelloWorld?wsdl";
//        URL wsdlUrl = new URL(spec);
        String spec = "http://localhost:8080/HelloWorld?wsdl";
        URL wsdlUrl = new URL(spec);

        Wsdl wsdl = Wsdl.parse(wsdlUrl);


        SoapBuilder builder = wsdl.binding()
                .localPart("SimpleServerPortBinding")
                .find();
        SoapOperation operation = builder.operation()
                .name("getCat")
                .find();
//        RequestConfig
        String request = builder.buildInputMessage(operation);
        System.out.println("request:\n" + request);

        Security ttlsa = Security.builder().authBasic("ttlsa", "123456").build();
        SoapClient client = SoapClient.builder()
                .endpointUri(spec).endpointSecurity(ttlsa)
                .build();
//        request="";
        String postResult = client.post(request);
        System.out.println(postResult);
    }


}