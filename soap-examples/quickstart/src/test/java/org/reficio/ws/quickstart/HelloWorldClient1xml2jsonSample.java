package org.reficio.ws.quickstart;

import org.reficio.ws.SoapBuilderException;
import org.reficio.ws.builder.SoapBuilder;
import org.reficio.ws.builder.SoapOperation;
import org.reficio.ws.builder.core.Wsdl;
import org.reficio.ws.client.core.SoapClient;
import org.reficio.ws.common.ResourceUtils;
import org.reficio.ws.legacy.WsdlUtils;

import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.Part;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * Created by tingfeng on 2018/6/25.
 */
public class HelloWorldClient1xml2jsonSample {
    public static void main(String[] args) throws MalformedURLException {
        URL wsdlUrl = new URL("http://localhost:8080/HelloWorld?wsdl");
        Wsdl wsdl = Wsdl.parse(wsdlUrl);


        SoapBuilder builder = wsdl.binding()
                .localPart("SimpleServerPortBinding")
                .find();
        SoapOperation operation = builder.operation()
                .name("getCat")
                .find();

        String request = builder.buildInputMessage(operation);
        System.out.println("request:\n"+request);
        String request1 = builder.buildInputMessage1(operation);
        System.out.println(request1);

//        String response = builder.buildOutputMessage(operation);
//        System.out.println("response:\n"+response);
//        String response1 = builder.buildOutputMessage1(operation);
//        System.out.println("response:\n"+response1);
    }

    public static BindingOperation getBindingOperation(Binding binding, SoapOperation op) {
        BindingOperation operation = binding.getBindingOperation(op.getOperationName(),
                op.getOperationInputName(), op.getOperationOutputName());
        if (operation == null) {
            throw new SoapBuilderException("Operation not found");
        }
        return operation;
    }
}
