package org.reficio.ws.builder;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.dom4j.*;
import org.reficio.ws.builder.SoapBuilder;
import org.reficio.ws.builder.SoapOperation;
import org.reficio.ws.builder.core.ElementOccurs;
import org.reficio.ws.builder.core.SoapXmlJsonUtil;
import org.reficio.ws.builder.core.Wsdl;
import org.reficio.ws.common.ResourceUtils;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

/**
 * Created by tingfeng on 2018/7/3.
 */
public class SoapXmlJsonUtilTest {
    public static void main(String[] args) throws Exception {
        String xml  = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:quic=\"http://quickstart.ws.reficio.org/\">\n" +
                "   <soapenv:Header/>\n" +
                "   <soapenv:Body>\n" +
                "      <quic:getCat>\n" +
                "         <arg0>\n" +
                "            <catName>gero et</catName>\n" +
                "            <footMap>\n" +
                "               <entry>\n" +
                "                  <key>sonoras imperio</key>\n" +
                "                  <value>\n" +
                "                     <footName>quae divum incedo</footName>\n" +
                "                  </value>\n" +
                "               </entry>\n" +
                "               <entry>\n" +
                "                  <key>sonoras imperio</key>\n" +
                "                  <value>\n" +
                "                     <footName>quae divum incedo</footName>\n" +
                "                  </value>\n" +
                "               </entry>\n" +
                "            </footMap>\n" +
                "            <footsList>\n" +
                "               <footName>verrantque per auras1</footName>\n" +
                "            </footsList>\n" +
                "            <footsList>\n" +
                "               <footName>verrantque per auras1</footName>\n" +
                "            </footsList>\n" +
                "            <stringList>per auras1</stringList>\n" +
                "            <stringList>per auras2</stringList>\n" +
                "            <stringMap>\n" +
                "               <entry>\n" +
                "                  <key>circum claustra</key>\n" +
                "                  <value>nimborum in</value>\n" +
                "               </entry>\n" +
                "               <entry>\n" +
                "                  <key>circum claustra</key>\n" +
                "                  <value>nimborum in</value>\n" +
                "               </entry>\n" +
                "            </stringMap>\n" +
                "         </arg0>\n" +
                "      </quic:getCat>\n" +
                "   </soapenv:Body>\n" +
                "</soapenv:Envelope>";
//        URL wsdlUrl = new URL("http://localhost:8080/HelloWorld?wsdl");
        URL wsdlUrl = ResourceUtils.getResourceWithAbsolutePackagePath("wsdl", "HelloWorld.wsdl");

        Wsdl wsdl = Wsdl.parse(wsdlUrl);

        SoapBuilder builder = wsdl.binding()
                .localPart("SimpleServerPortBinding")
                .find();
        SoapOperation operation = builder.operation()
                .name("getCat")
                .find();
        ElementOccurs opCache = SoapXmlJsonUtil.buildElementOccursCache(builder, operation);
//        System.out.println(xml2Json(xml));
        System.out.println(JSON.toJSONString(SoapXmlJsonUtil.soapXML2JSON(opCache, xml), SerializerFeature.PrettyFormat));
    }


}
