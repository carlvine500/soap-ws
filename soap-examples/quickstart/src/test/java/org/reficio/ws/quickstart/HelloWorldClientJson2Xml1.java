package org.reficio.ws.quickstart;

import org.reficio.ws.SoapBuilderException;
import org.reficio.ws.builder.SoapBuilder;
import org.reficio.ws.builder.SoapOperation;
import org.reficio.ws.builder.core.Wsdl;

import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by tingfeng on 2018/6/25.
 */
public class HelloWorldClientJson2Xml1 {
    public static void main(String[] args) throws MalformedURLException {
        URL wsdlUrl = new URL("http://localhost:8080/HelloWorld?wsdl");
        Wsdl wsdl = Wsdl.parse(wsdlUrl);


        SoapBuilder builder = wsdl.binding()
                .localPart("SimpleServerPortBinding")
                .find();
        SoapOperation operation = builder.operation()
                .name("getCat")
                .find();

        String json = "{\"Envelope\":{\"Header\":{},\"Body\":{\"getCat\":{\"arg0\":{\"stringList\":[\"sonoras imperio\"],\"catName\":\"gero et\"}}}}}\n";
        String sample = builder.buildInputMessage(operation);
        System.out.println(sample);
        String jsonSample = builder.buildInputMessage1(operation);
        System.out.println(jsonSample);
        String request1 = builder.json2Xml(operation,jsonSample);
        System.out.println(request1);
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
