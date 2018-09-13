package org.reficio.ws.quickstart;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.apache.xmlbeans.XmlException;
import org.dom4j.DocumentException;
import org.reficio.ws.builder.SoapBuilder;
import org.reficio.ws.builder.SoapOperation;
import org.reficio.ws.builder.core.ElementOccurs;
import org.reficio.ws.builder.core.SoapUtils;
import org.reficio.ws.builder.core.Wsdl;
import org.reficio.ws.client.core.Security;
import org.reficio.ws.client.core.SoapClient;

import javax.xml.namespace.QName;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * Created by tingfeng on 2018/7/3.
 */
public class SoapUtils_Example {
    public static void main(String[] args) throws XmlException, DocumentException, MalformedURLException {
        // webservice request: <1>.soap-xml-template --> <2>.soap-json-template --> <3>.rewrite json --> <4>json replace <1>
        // webservice response: <1>.soap-xml-template --> <2>.soap-json-template --> <3>.read json
        String spec = "http://localhost:8081/HelloWorld?wsdl";
        URL wsdlUrl = new URL(spec);
        Wsdl wsdl = Wsdl.parse(wsdlUrl);
        for (QName qName : wsdl.getBindings()) {
            String binding = qName.getLocalPart();
            SoapBuilder builder = wsdl.binding().localPart(binding).find();
            List<String> serviceUrls = builder.getServiceUrls();
            for (SoapOperation soapOperation : builder.getOperations()) {
                String operationName = soapOperation.getOperationName();
                String requestXmlTemplateAndSample = builder.buildInputMessage(soapOperation);
                System.out.println("requestXmlTemplateAndSample=\n"+requestXmlTemplateAndSample);
                ElementOccurs elementOccurs = SoapUtils.buildElementOccurs(builder, soapOperation,true);
                JSONObject jsonTemplate = SoapUtils.soapXml2Json(elementOccurs, requestXmlTemplateAndSample);
//                jsonTemplate.getJSONObject("Envelope").getJSONObject("Body").getJSONObject("echo").put("arg0", "myCatName");
                System.out.println("jsonTemplate=\n"+JSON.toJSONString(jsonTemplate,SerializerFeature.PrettyFormat));
                String requestXml = SoapUtils.soapJson2Xml(requestXmlTemplateAndSample, jsonTemplate.toJSONString(), true);
                SoapClient client = SoapClient.builder()
                        .endpointUri(serviceUrls.get(0))
                        .endpointSecurity(Security.builder().build())
                        .build();
                String postResult = client.post(requestXml);
                System.out.println("postResult=\n"+postResult);
                String responseXmlTemplateAndSample = builder.buildOutputMessage(soapOperation);
                ElementOccurs elementOccurs1 = SoapUtils.buildElementOccurs(builder, soapOperation,false);
                JSONObject jsonResponse = SoapUtils.soapXml2Json(elementOccurs1, responseXmlTemplateAndSample);
                System.out.println("jsonResponse=\n"+JSON.toJSONString(jsonResponse,SerializerFeature.PrettyFormat));
            }
        }

//        SoapOperation operation = builder.operation().name("getCat").find();
//
//        System.out.println("requestXmlTemplateAndSample:------------------------------------------------");
//        System.out.println(requestXmlTemplateAndSample);
//
//
//
//        jsonTemplate.getJSONObject("Envelope").getJSONObject("Body").getJSONObject("getCat").getJSONObject("arg0").put("catName", "myCatName");
//        String requestJson = JSON.toJSONString(jsonTemplate, SerializerFeature.PrettyFormat);
//        System.out.println("request json:------------------------------------------------");
//        System.out.println(requestJson);
//
//
//        System.out.println("requestXml:------------------------------------------------");
//        System.out.println(requestXml);
//
//
//        System.out.println("requestXml:------------------------------------------------");
//        System.out.println(postResult);


    }


}
