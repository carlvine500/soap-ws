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
import org.reficio.ws.client.core.SoapClient;
import org.reficio.ws.common.ResourceUtils;
import org.reficio.ws.common.XmlUtils;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by tingfeng on 2018/7/3.
 */
public class SoapUtils_Example {
    public static void main(String[] args) throws XmlException, DocumentException, MalformedURLException {
        //  webservice request: <1>.soap-xml-template --> <2>.soap-json-template --> <3>.rewrite json --> <4>json replace <1>
        // webservice response: <1>.soap-xml-template --> <2>.soap-json-template --> <3>.read json
        String spec = "http://localhost:8080/HelloWorld?wsdl";
        URL wsdlUrl = new URL(spec);
        Wsdl wsdl = Wsdl.parse(wsdlUrl);
        SoapBuilder builder = wsdl.binding().localPart("SimpleServerPortBinding").find();
        SoapOperation operation = builder.operation().name("getCat").find();
        String requestXmlTemplateAndSample = builder.buildInputMessage(operation);
        System.out.println("requestXmlTemplateAndSample:------------------------------------------------");
        System.out.println(requestXmlTemplateAndSample);

        ElementOccurs elementOccurs = SoapUtils.buildElementOccurs(builder, operation);
        JSONObject jsonTemplate = SoapUtils.soapXml2Json(elementOccurs, requestXmlTemplateAndSample);
        jsonTemplate.getJSONObject("Envelope").getJSONObject("Body").getJSONObject("getCat").getJSONObject("arg0").put("catName", "myCatName");
        String requestJson = JSON.toJSONString(jsonTemplate, SerializerFeature.PrettyFormat);
        System.out.println("request json:------------------------------------------------");
        System.out.println(requestJson);

        String requestXml = SoapUtils.soapJson2Xml(requestXmlTemplateAndSample, requestJson, true);
        System.out.println("requestXml:------------------------------------------------");
        System.out.println(requestXml);

        SoapClient client = SoapClient.builder()
                .endpointUri(spec)
                .build();
        String postResult = client.post(requestXml);
        System.out.println("requestXml:------------------------------------------------");
        System.out.println(postResult);


    }


}
