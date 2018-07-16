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
import java.io.File;
import java.net.URL;
import java.util.List;

/**
 * Created by tingfeng on 2018/6/25.
 */
public class HelloWorldClient {
    public static void main(String[] args) {
        URL wsdlUrl = ResourceUtils.getResourceWithAbsolutePackagePath("/", "wsdl/HelloWorld.wsdl");

        Wsdl wsdl = Wsdl.parse(wsdlUrl);
//        wsdl.saveWsdl(new File("/Users/tingfeng/work/java/soap-ws/soap-examples/quickstart/src/test/resources/xsd"));

//        SoapBuilder builder = wsdl.binding()
//                .localPart("HelloWorldSoapBinding")
//                .find();

        SoapBuilder builder = wsdl.binding()
                .localPart("HelloWorldSoapBinding")
                .find();
        ;
        SoapOperation operation = builder.operation()
                .name("echo")
                .find();
//        builder.getBinding().getPortType().getOperations()
//        operation.get
        String request = builder.buildInputMessage(operation);
        System.out.println(request);
//        List<String> serviceUrls = builder.getServiceUrls();
//        Binding binding = builder.getBinding();
//        BindingOperation bindingOperation = getBindingOperation(binding, operation);
//        // TODO 没有考虑参数属于不同的namespace
//        Part[] parts = WsdlUtils.getInputParts(bindingOperation);
//        if (parts.length > 0) {
//            String namespaceURI = parts[0].getElementName().getNamespaceURI();
//            System.out.println(namespaceURI);
//        }

//      String value = "http://localhost:8080";
        String value = "http://localhost:8080/HelloWorld";
        SoapClient client = SoapClient.builder()
                .endpointUri(value)
                .build();
        String response = client.post(request);
        System.out.println(response);
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
