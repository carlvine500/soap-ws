package org.reficio.ws.quickstart;

import org.apache.xmlbeans.XmlObject;
import org.reficio.ws.common.ResourceUtils;
import org.reficio.ws.legacy.SchemaUtils;
import org.reficio.ws.legacy.UrlSchemaLoader;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

/**
 * Created by tingfeng on 2018/6/26.
 */
public class SchemaUtilsTest {
    public static void main(String[] args) throws MalformedURLException {
//       URL wsdlUrl = ResourceUtils.getResourceWithAbsolutePackagePath("/", "wsdl/article.wsdl");
//        String wsdlUrl = "/Users/tingfeng/work/java/soap-ws/soap-examples/quickstart/src/test/resources/wsdl/HelloWorld.wsdl";
//        URL wsdlUrl = new URL("http://localhost:8080/HelloWorld?wsdl");
//         URL wsdlUrl = new URL("file:/Users/tingfeng/work/java/xml-xsd-json/testdata/input/OneService.wsdl");
         URL wsdlUrl = new URL("http://localhost:9001/test/TestWebservice?wsdl");
        UrlSchemaLoader loader = new UrlSchemaLoader(wsdlUrl.toString());
        Map<String, XmlObject> schemas = SchemaUtils.getSchemas(loader.getBaseURI(), loader);
        for (Map.Entry<String, XmlObject> entry : schemas.entrySet()) {
            if (entry.getKey() == null) {
                continue;
            }
            System.out.println(entry.getKey()+"------------------------------------------");
            XmlObject value = entry.getValue();
//            value.
//            value.get
            System.out.println(value);
        }
    }
}
