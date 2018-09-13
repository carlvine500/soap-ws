package org.reficio.ws.quickstart.auth;

import org.dom4j.DocumentException;
import org.reficio.ws.builder.SoapBuilder;
import org.reficio.ws.builder.SoapOperation;
import org.reficio.ws.builder.core.Wsdl;
import org.reficio.ws.client.core.Security;
import org.reficio.ws.client.core.SoapClient;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by tingfeng on 2018/6/25.
 */
public class HelloWorldClientAuth {
    public static void main(String[] args) throws MalformedURLException, DocumentException {
//        String spec = "http://localhost:8080/HelloWorld?wsdl";
//        URL wsdlUrl = new URL(spec);
        String spec = "http://218.83.247.28:8020/sap/bc/srt/wsdl/flv_10002A101AD1/bndg_url/sap/bc/srt/rfc/sap/zws_sd_crm_color_full/710/zsd_crm_color_f/zsd_crm_color_f?sap-client=710";
        URL wsdlUrl = new URL(spec);

        Wsdl wsdl = Wsdl.parse(wsdlUrl,"CRMSOAP:123456");


        SoapBuilder builder = wsdl.binding()
                .localPart("zsd_crm_color_f")
                .find();
        SoapOperation operation = builder.operation()
                .name("Z_SD_CRM_COLOR_FULL")
                .find();
//        RequestConfig
        String request = builder.buildInputMessage(operation);
        System.out.println("request:\n" + request);
        request="<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:urn=\"urn:sap-com:document:sap:rfc:functions\">\n" +
        "   <soapenv:Header/>\n" +
        "   <soapenv:Body>\n" +
        "      <urn:Z_SD_CRM_COLOR_FULL>\n" +
        "         <T_COLOR>           \n" +
        "         </T_COLOR>\n" +
        "         <T_RETURN>            \n" +
        "         </T_RETURN>\n" +
        "      </urn:Z_SD_CRM_COLOR_FULL>\n" +
        "   </soapenv:Body>\n" +
        "</soapenv:Envelope>";
        String endpoint= "http://218.83.247.28:8020/sap/bc/srt/rfc/sap/zws_sd_crm_color_full/710/zsd_crm_color_f/zsd_crm_color_f";
        Security ttlsa = Security.builder().authBasic("CRMSOAP", "123456").build();
        SoapClient client = SoapClient.builder()
                .endpointUri(endpoint).endpointSecurity(ttlsa)
                .build();
        String postResult = client.post(request);
        System.out.println(postResult);
    }


}