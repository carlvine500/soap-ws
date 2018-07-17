package org.reficio.ws.quickstart;

import org.dom4j.DocumentException;
import org.reficio.ws.builder.SoapBuilder;
import org.reficio.ws.builder.SoapOperation;
import org.reficio.ws.builder.core.Wsdl;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by tingfeng on 2018/6/25.
 */
public class HelloWorldClient {
    public static void main(String[] args) throws MalformedURLException, DocumentException {
        URL wsdlUrl = new URL("http://localhost:8080/HelloWorld?wsdl");
        Wsdl wsdl = Wsdl.parse(wsdlUrl);


        SoapBuilder builder = wsdl.binding()
                .localPart("SimpleServerPortBinding")
                .find();
        SoapOperation operation = builder.operation()
                .name("getCat")
                .find();

        String request = builder.buildInputMessage(operation);
        System.out.println("request:\n" + request);


    }


}