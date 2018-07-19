package org.reficio.ws.builder;

import org.dom4j.DocumentException;
import org.junit.Assert;
import org.junit.Test;
import org.reficio.ws.builder.core.SoapUtils;

/**
 * Created by tingfeng on 2018/7/3.
 */
public class SoapUtils_Json2Xml_Test {
    @Test
    public void nullValue() throws DocumentException {
        String json = "{\"Envelope\":{\"Body\":{\"getCat\":{\"arg0\":null}}}}";
        String xml = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:quic=\"http://quickstart.ws.reficio.org/\">" +
                "<soapenv:Body><quic:getCat><arg0>${xxx}</arg0></quic:getCat></soapenv:Body></soapenv:Envelope>";
        String result = SoapUtils.soapJson2Xml(xml, json, true);
        System.out.println(result);
        String expected = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "\n" +
                "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:quic=\"http://quickstart.ws.reficio.org/\">\n" +
                "  <soapenv:Body>\n" +
                "    <quic:getCat>\n" +
                "      <arg0></arg0>\n" +
                "    </quic:getCat>\n" +
                "  </soapenv:Body>\n" +
                "</soapenv:Envelope>\n";
        Assert.assertEquals(expected, result);
    }

    @Test
    public void stringValue() throws DocumentException {
        String json = "{\"Envelope\":{\"Body\":{\"getCat\":{\"arg0\":\"arg0Value\"}}}}";
        String xml = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:quic=\"http://quickstart.ws.reficio.org/\">" +
                "<soapenv:Body><quic:getCat><arg0>${xxx}</arg0></quic:getCat></soapenv:Body></soapenv:Envelope>";
        String result = SoapUtils.soapJson2Xml(xml, json, true);
        System.out.println(result);
        String expected = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "\n" +
                "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:quic=\"http://quickstart.ws.reficio.org/\">\n" +
                "  <soapenv:Body>\n" +
                "    <quic:getCat>\n" +
                "      <arg0>arg0Value</arg0>\n" +
                "    </quic:getCat>\n" +
                "  </soapenv:Body>\n" +
                "</soapenv:Envelope>\n";
        Assert.assertEquals(expected, result);
    }

    @Test
    public void withoutAXmlElement() throws DocumentException {
        String json = "{\"Envelope\":{\"Body\":{\"getCat\":{\"arg0\":\"arg0Value\"}}}}";
        String xml = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:quic=\"http://quickstart.ws.reficio.org/\">" +
                "<soapenv:Body><quic:getCat></quic:getCat></soapenv:Body></soapenv:Envelope>";
        String result = SoapUtils.soapJson2Xml(xml, json, true);
        System.out.println(result);
        String expected = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "\n" +
                "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:quic=\"http://quickstart.ws.reficio.org/\">\n" +
                "  <soapenv:Body>\n" +
                "    <quic:getCat/>\n" +
                "  </soapenv:Body>\n" +
                "</soapenv:Envelope>\n";
        Assert.assertEquals(expected, result);
    }

    @Test
    public void withoutAJsonElement() throws DocumentException {
        String json = "{\"Envelope\":{\"Body\":{\"getCat\":null}}}";
        String xml = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:quic=\"http://quickstart.ws.reficio.org/\">" +
                "<soapenv:Body><quic:getCat><arg0>${xxx}</arg0></quic:getCat></soapenv:Body></soapenv:Envelope>";
        String result = SoapUtils.soapJson2Xml(xml, json, true);
        System.out.println(result);
        String expected = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "\n" +
                "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:quic=\"http://quickstart.ws.reficio.org/\">\n" +
                "  <soapenv:Body>\n" +
                "    <quic:getCat>\n" +
                "      <arg0></arg0>\n" +
                "    </quic:getCat>\n" +
                "  </soapenv:Body>\n" +
                "</soapenv:Envelope>\n";
        Assert.assertEquals(expected, result);
    }

    @Test
    public void stringArr() throws DocumentException {
        // TODO 模板元素如果出现多次,会造成替换后出现一个空元素,模板是可能出现大于等于2次的
        String json = "{\"Envelope\":{\"Body\":{\"getCat\":{\"arg0\":{\"stringList\":[\"x\",\"y\"]}}}}}";
        String xml = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:quic=\"http://quickstart.ws.reficio.org/\">" +
                "<soapenv:Body><quic:getCat><arg0><stringList>${x}</stringList><stringList>${x}</stringList></arg0></quic:getCat></soapenv:Body></soapenv:Envelope>";
        String result = SoapUtils.soapJson2Xml(xml, json, true);
        System.out.println(result);
        String expected = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "\n" +
                "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:quic=\"http://quickstart.ws.reficio.org/\">\n" +
                "  <soapenv:Body>\n" +
                "    <quic:getCat>\n" +
                "      <arg0>\n" +
                "        <stringList></stringList>\n" +
                "        <stringList>x</stringList>\n" +
                "        <stringList>y</stringList>\n" +
                "      </arg0>\n" +
                "    </quic:getCat>\n" +
                "  </soapenv:Body>\n" +
                "</soapenv:Envelope>\n";
        Assert.assertEquals(expected, result);
    }

    @Test
    public void catArr() throws DocumentException {
        String json = "{\"Envelope\":{\"Body\":{\"getCat\":{\"arg0\":{\"catList\":[{\"catAge\":1},{\"catAge\":2}]}}}}}";
        String xml = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:quic=\"http://quickstart.ws.reficio.org/\">" +
                "<soapenv:Body><quic:getCat><arg0><catList><catAge>${x}</catAge></catList></arg0></quic:getCat></soapenv:Body></soapenv:Envelope>";
        String result = SoapUtils.soapJson2Xml(xml, json, true);
        System.out.println(result);
        String expected = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "\n" +
                "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:quic=\"http://quickstart.ws.reficio.org/\">\n" +
                "  <soapenv:Body>\n" +
                "    <quic:getCat>\n" +
                "      <arg0>\n" +
                "        <catList>\n" +
                "          <catAge>1</catAge>\n" +
                "        </catList>\n" +
                "        <catList>\n" +
                "          <catAge>2</catAge>\n" +
                "        </catList>\n" +
                "      </arg0>\n" +
                "    </quic:getCat>\n" +
                "  </soapenv:Body>\n" +
                "</soapenv:Envelope>\n";
        Assert.assertEquals(expected, result);
    }

    @Test
    public void stringMap() throws DocumentException {
        String json = "{\"Envelope\":{\"Body\":{\"getCat\":{\"arg0\":{\"catMap\":{\"entry\":[{\"key\":1,\"value\":2},{\"key\":3,\"value\":4}]}}}}}}";
        String xml = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:quic=\"http://quickstart.ws.reficio.org/\">" +
                "<soapenv:Body><quic:getCat><arg0><catMap><entry><key>${k}</key><value>${v}</value></entry></catMap></arg0></quic:getCat></soapenv:Body></soapenv:Envelope>";
        String result = SoapUtils.soapJson2Xml(xml, json, true);
        System.out.println(result);
        String expected = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "\n" +
                "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:quic=\"http://quickstart.ws.reficio.org/\">\n" +
                "  <soapenv:Body>\n" +
                "    <quic:getCat>\n" +
                "      <arg0>\n" +
                "        <catMap>\n" +
                "          <entry>\n" +
                "            <key>1</key>\n" +
                "            <value>2</value>\n" +
                "          </entry>\n" +
                "          <entry>\n" +
                "            <key>3</key>\n" +
                "            <value>4</value>\n" +
                "          </entry>\n" +
                "        </catMap>\n" +
                "      </arg0>\n" +
                "    </quic:getCat>\n" +
                "  </soapenv:Body>\n" +
                "</soapenv:Envelope>\n";
        Assert.assertEquals(expected, result);
    }

    @Test
    public void catMap() throws DocumentException {
        String json = "{\"Envelope\":{\"Body\":{\"getCat\":{\"arg0\":{\"catMap\":{\"entry\":[{\"key\":1,\"value\":{\"catName\":2}},{\"key\":3,\"value\":{\"catName\":4}}]}}}}}}";
        String xml = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:quic=\"http://quickstart.ws.reficio.org/\">" +
                "<soapenv:Body><quic:getCat><arg0><catMap><entry><key>${k}</key><value><catName>${v}</catName></value></entry></catMap></arg0></quic:getCat></soapenv:Body></soapenv:Envelope>";
        String result = SoapUtils.soapJson2Xml(xml, json, true);
        
        String expected = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "\n" +
                "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:quic=\"http://quickstart.ws.reficio.org/\">\n" +
                "  <soapenv:Body>\n" +
                "    <quic:getCat>\n" +
                "      <arg0>\n" +
                "        <catMap>\n" +
                "          <entry>\n" +
                "            <key>1</key>\n" +
                "            <value>\n" +
                "              <catName>2</catName>\n" +
                "            </value>\n" +
                "          </entry>\n" +
                "          <entry>\n" +
                "            <key>3</key>\n" +
                "            <value>\n" +
                "              <catName>4</catName>\n" +
                "            </value>\n" +
                "          </entry>\n" +
                "        </catMap>\n" +
                "      </arg0>\n" +
                "    </quic:getCat>\n" +
                "  </soapenv:Body>\n" +
                "</soapenv:Envelope>\n";
        Assert.assertEquals(expected, result);
    }


}
