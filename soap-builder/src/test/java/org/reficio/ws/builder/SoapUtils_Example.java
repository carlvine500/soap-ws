package org.reficio.ws.builder;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.apache.xmlbeans.XmlException;
import org.dom4j.DocumentException;
import org.reficio.ws.builder.core.ElementOccurs;
import org.reficio.ws.builder.core.SoapUtils;
import org.reficio.ws.builder.core.Wsdl;
import org.reficio.ws.common.ResourceUtils;

import java.net.URL;

/**
 * Created by tingfeng on 2018/7/3.
 */
public class SoapUtils_Example {
    public static void main(String[] args) throws XmlException, DocumentException {
        //  webservice request: <1>.soap-xml-template --> <2>.soap-json-template --> <3>.rewrite json --> <4>json replace <1>
        // webservice response: <1>.soap-xml-template --> <2>.soap-json-template --> <3>.read json
        URL wsdlUrl = ResourceUtils.getResourceWithAbsolutePackagePath("test", "resources.wsdl");
        Wsdl wsdl = Wsdl.parse(wsdlUrl);
        SoapBuilder builder = wsdl.binding().localPart("SimpleServerPortBinding").find();
        SoapOperation operation = builder.operation().name("getCat").find();
        String requestXmlTemplateAndSample = builder.buildInputMessage(operation);
        System.out.println("requestXmlTemplateAndSample:------------------------------------------------");
        System.out.println(requestXmlTemplateAndSample);

        ElementOccurs elementOccurs = SoapUtils.buildElementOccurs(builder, operation,true);
        JSONObject jsonTemplate = SoapUtils.soapXml2Json(elementOccurs, requestXmlTemplateAndSample);
        jsonTemplate.getJSONObject("Envelope").getJSONObject("Body").getJSONObject("getCat").getJSONObject("arg0").put("catName", "myCatName");
        String requestJson = JSON.toJSONString(jsonTemplate, SerializerFeature.PrettyFormat);
        System.out.println("request json:------------------------------------------------");
        System.out.println(requestJson);

        String requestXml = SoapUtils.soapJson2Xml(requestXmlTemplateAndSample, requestJson, true);
        System.out.println("requestXml:------------------------------------------------");
        System.out.println(requestXml);

    }


}
