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
public class HelloWorldClient2xml2jsonAdjust {
    public static void main(String[] args) throws MalformedURLException {
        URL wsdlUrl = new URL("http://localhost:8080/HelloWorld?wsdl");
        Wsdl wsdl = Wsdl.parse(wsdlUrl);


        SoapBuilder builder = wsdl.binding()
                .localPart("SimpleServerPortBinding")
                .find();
        SoapOperation operation = builder.operation()
                .name("getCat")
                .find();

//        String request = builder.buildInputMessage(operation);
//        System.out.println("request:\n"+request);
        String xml = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:quic=\"http://quickstart.ws.reficio.org/\">\n" +
                "   <soapenv:Header/>\n" +
                "   <soapenv:Body>\n" +
                "      <quic:getCat>\n" +
                "         <arg0>\n" +
                "            <catName>catNameValue</catName>\n" +
                "            <footMap>\n" +
                "               <entry>\n" +
                "                  <key>entryKeyValue</key>\n" +
                "                  <value>\n" +
                "                     <footName>footNameValue</footName>\n" +
                "                  </value>\n" +
                "               </entry>\n" +
                "            </footMap>\n" +
                "            <footsList>\n" +
                "               <footName>listfootNameValue</footName>\n" +
                "               <footName>listfootNameValue1</footName>\n" +
                "               <footName>listfootNameValue2</footName>\n" +
                "            </footsList>\n" +
                "            <stringList>stringListvalue</stringList>\n" +
                "            <stringMap>\n" +
                "               <entry>\n" +
                "                  <key>mapEntryKey</key>\n" +
                "                  <value>entryValueForStringMap</value>\n" +
                "               </entry>\n" +
                "            </stringMap>\n" +
                "         </arg0>\n" +
                "      </quic:getCat>\n" +
                "   </soapenv:Body>\n" +
                "</soapenv:Envelope>";
        String request2 = builder.buildInputMessage2(operation,xml);
        System.out.println(request2);

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
