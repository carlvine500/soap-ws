package org.reficio.ws.quickstart;

import org.apache.commons.lang3.RandomStringUtils;
import org.dom4j.DocumentException;
import org.reficio.ws.builder.SoapBuilder;
import org.reficio.ws.builder.SoapOperation;
import org.reficio.ws.builder.core.Wsdl;
import org.reficio.ws.client.core.SoapClient;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by tingfeng on 2018/6/25.
 */
public class HelloWorldClient {
    public static void main(String[] args) throws MalformedURLException, DocumentException {
//        String spec = "http://localhost:8080/HelloWorld?wsdl";
//        URL wsdlUrl = new URL(spec);
        String spec = "http://localhost:8080/HelloWorld?wsdl";
        URL wsdlUrl = new URL(spec);

        Wsdl wsdl = Wsdl.parse(wsdlUrl);


        SoapBuilder builder = wsdl.binding()
                .localPart("SimpleServerServiceSoapBinding")
                .find();
        SoapOperation operation = builder.operation()
                .name("getList")
                .find();
//        RequestConfig
        final String request = builder.buildInputMessage(operation);
        System.out.println("request:\n" + request);

        final SoapClient client = SoapClient.builder().endpointUri(spec).build();
        String post = client.post(request);
        System.out.println(post);

        client.disconnect();
        SoapClient client1 = SoapClient.builder().endpointUri(spec).build();
        System.out.println(client1.post(request));

    }


}