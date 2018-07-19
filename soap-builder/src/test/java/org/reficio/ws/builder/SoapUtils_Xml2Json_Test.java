package org.reficio.ws.builder;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.dom4j.DocumentException;
import org.junit.Assert;
import org.junit.Test;
import org.reficio.ws.builder.core.ElementOccurs;
import org.reficio.ws.builder.core.SoapUtils;

/**
 * Created by tingfeng on 2018/7/3.
 */
public class SoapUtils_Xml2Json_Test {

    @Test
    public void normalTest() throws DocumentException {
        String xml = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:quic=\"http://quickstart.ws.reficio.org/\">" +
                "<soapenv:Body><quic:getCat><arg0>${xxx}</arg0></quic:getCat></soapenv:Body></soapenv:Envelope>";
        String result = SoapUtils.soapXml2Json(ElementOccurs.EMPTY_INSTANCE, xml, true);
        String expected = "{\n" +
                "\t\"Envelope\":{\n" +
                "\t\t\"Body\":{\n" +
                "\t\t\t\"getCat\":{\n" +
                "\t\t\t\t\"arg0\":\"${xxx}\"\n" +
                "\t\t\t}\n" +
                "\t\t}\n" +
                "\t}\n" +
                "}";
        Assert.assertEquals(expected, result);
    }

    @Test
    public void arrTest() throws DocumentException {
        String xml = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:quic=\"http://quickstart.ws.reficio.org/\">" +
                "<soapenv:Body><quic:getCat><arg0>${xxx}</arg0></quic:getCat></soapenv:Body></soapenv:Envelope>";
        ElementOccurs elementOccurs = ElementOccurs.newInstance()//
                .createChild("getCat").setMaxOccurs(0)//
                .createChild("arg0").setMaxOccurs(Integer.MAX_VALUE).getRoot();
        String eoJson = JSON.toJSONString(elementOccurs, SerializerFeature.PrettyFormat);
        String expectedJson = "{\n" +
                "\t\"children\":{\n" +
                "\t\t\"getCat\":{\n" +
                "\t\t\t\"children\":{\n" +
                "\t\t\t\t\"arg0\":{\n" +
                "\t\t\t\t\t\"children\":{},\n" +
                "\t\t\t\t\t\"maxOccurs\":2147483647\n" +
                "\t\t\t\t}\n" +
                "\t\t\t},\n" +
                "\t\t\t\"maxOccurs\":0\n" +
                "\t\t}\n" +
                "\t},\n" +
                "\t\"maxOccurs\":0\n" +
                "}";
        Assert.assertEquals(expectedJson, eoJson);
        String result = SoapUtils.soapXml2Json(elementOccurs, xml, true);
        System.out.println(result);
        String expected = "{\n" +
                "\t\"Envelope\":{\n" +
                "\t\t\"Body\":{\n" +
                "\t\t\t\"getCat\":{\n" +
                "\t\t\t\t\"arg0\":[\"${xxx}\"]\n" +
                "\t\t\t}\n" +
                "\t\t}\n" +
                "\t}\n" +
                "}";
        Assert.assertEquals(expected, result);
    }

    @Test
    public void catArrSingleElementTest() throws DocumentException {
        String xml = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:quic=\"http://quickstart.ws.reficio.org/\">" +
                "<soapenv:Body><quic:getCat><catList><catName>${xxx}</catName></catList></quic:getCat></soapenv:Body></soapenv:Envelope>";
        ElementOccurs elementOccurs = ElementOccurs.newInstance()//
                .createChild("getCat").setMaxOccurs(0)//
                .createChild("catList").setMaxOccurs(0)//
                .createChild("catName").setMaxOccurs(2)//
                .getRoot();

        String result = SoapUtils.soapXml2Json(elementOccurs, xml, true);
        System.out.println(result);
        String expected = "{\n" +
                "\t\"Envelope\":{\n" +
                "\t\t\"Body\":{\n" +
                "\t\t\t\"getCat\":{\n" +
                "\t\t\t\t\"catList\":{\n" +
                "\t\t\t\t\t\"catName\":[\"${xxx}\"]\n" +
                "\t\t\t\t}\n" +
                "\t\t\t}\n" +
                "\t\t}\n" +
                "\t}\n" +
                "}";
        Assert.assertEquals(expected, result);
    }

    @Test
    public void catArrMultiValueTest() throws DocumentException {
        String xml = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:quic=\"http://quickstart.ws.reficio.org/\">" +
                "<soapenv:Body><quic:getCat><catList><catName>${x}</catName><catName>${y}</catName></catList></quic:getCat></soapenv:Body></soapenv:Envelope>";
        ElementOccurs elementOccurs = ElementOccurs.newInstance()//
                .createChild("getCat").setMaxOccurs(0)//
                .createChild("catList").setMaxOccurs(0)//
                .createChild("catName").setMaxOccurs(2)//
                .getRoot();

        String result = SoapUtils.soapXml2Json(elementOccurs, xml, true);
        System.out.println(result);
        String expected = "{\n" +
                "\t\"Envelope\":{\n" +
                "\t\t\"Body\":{\n" +
                "\t\t\t\"getCat\":{\n" +
                "\t\t\t\t\"catList\":{\n" +
                "\t\t\t\t\t\"catName\":[\n" +
                "\t\t\t\t\t\t\"${x}\",\n" +
                "\t\t\t\t\t\t\"${y}\"\n" +
                "\t\t\t\t\t]\n" +
                "\t\t\t\t}\n" +
                "\t\t\t}\n" +
                "\t\t}\n" +
                "\t}\n" +
                "}";
        Assert.assertEquals(expected, result);
    }

    @Test
    public void catEntryTest() throws DocumentException {
        String xml = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:quic=\"http://quickstart.ws.reficio.org/\">" +
                "<soapenv:Body><quic:getCat><catMap><entry><key>${k}</key><value>${v}</value></entry></catMap></quic:getCat></soapenv:Body></soapenv:Envelope>";
        ElementOccurs elementOccurs = ElementOccurs.newInstance()//
                .createChild("getCat").setMaxOccurs(0)//
                .createChild("catMap").setMaxOccurs(0)//
                .createChild("entry").setMaxOccurs(2)//
                .getRoot();

        String result = SoapUtils.soapXml2Json(elementOccurs, xml, true);
        System.out.println(result);
        String expected = "{\n" +
                "\t\"Envelope\":{\n" +
                "\t\t\"Body\":{\n" +
                "\t\t\t\"getCat\":{\n" +
                "\t\t\t\t\"catMap\":{\n" +
                "\t\t\t\t\t\"entry\":[{\n" +
                "\t\t\t\t\t\t\"key\":\"${k}\",\n" +
                "\t\t\t\t\t\t\"value\":\"${v}\"\n" +
                "\t\t\t\t\t}]\n" +
                "\t\t\t\t}\n" +
                "\t\t\t}\n" +
                "\t\t}\n" +
                "\t}\n" +
                "}";
        Assert.assertEquals(expected, result);
    }

    @Test
    public void catEntryCatFootTest() throws DocumentException {
        String xml = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:quic=\"http://quickstart.ws.reficio.org/\">" +
                "<soapenv:Body><quic:getCat><catMap><entry><key>${k}</key><value><catFoot>${v}</catFoot></value></entry></catMap></quic:getCat></soapenv:Body></soapenv:Envelope>";
        ElementOccurs elementOccurs = ElementOccurs.newInstance()//
                .createChild("getCat").setMaxOccurs(0)//
                .createChild("catMap").setMaxOccurs(0)//
                .createChild("entry").setMaxOccurs(2)//
                .createChild("value").setMaxOccurs(0)//
                .createChild("catFoot").setMaxOccurs(2)//
                .getRoot();

        String result = SoapUtils.soapXml2Json(elementOccurs, xml, true);
        System.out.println(result);
        String expected = "{\n" +
                "\t\"Envelope\":{\n" +
                "\t\t\"Body\":{\n" +
                "\t\t\t\"getCat\":{\n" +
                "\t\t\t\t\"catMap\":{\n" +
                "\t\t\t\t\t\"entry\":[{\n" +
                "\t\t\t\t\t\t\"key\":\"${k}\",\n" +
                "\t\t\t\t\t\t\"value\":{\n" +
                "\t\t\t\t\t\t\t\"catFoot\":[\"${v}\"]\n" +
                "\t\t\t\t\t\t}\n" +
                "\t\t\t\t\t}]\n" +
                "\t\t\t\t}\n" +
                "\t\t\t}\n" +
                "\t\t}\n" +
                "\t}\n" +
                "}";
        Assert.assertEquals(expected, result);
    }


}
