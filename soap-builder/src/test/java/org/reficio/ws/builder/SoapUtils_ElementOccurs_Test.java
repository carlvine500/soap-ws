package org.reficio.ws.builder;

import org.apache.xmlbeans.XmlException;
import org.junit.Assert;
import org.junit.Test;
import org.reficio.ws.builder.core.ElementOccurs;
import org.reficio.ws.builder.core.SoapUtils;
import org.reficio.ws.builder.core.Wsdl;
import org.reficio.ws.common.ResourceUtils;

import java.net.URL;

/**
 * Created by tingfeng on 2018/7/3.
 */
public class SoapUtils_ElementOccurs_Test {
    @Test
    public void buildElementOccurs() throws XmlException {
        URL wsdlUrl = ResourceUtils.getResourceWithAbsolutePackagePath("wsdl", "HelloWorld.wsdl");

        Wsdl wsdl = Wsdl.parse(wsdlUrl);
//        wsdl.saveWsdl(new File("/Users/tingfeng/work/java/soap-ws/soap-builder/src/test/resources"));
        SoapBuilder builder = wsdl.binding()
                .localPart("SimpleServerPortBinding")
                .find();
        SoapOperation operation = builder.operation()
                .name("getCat")
                .find();
        String request = builder.buildInputMessage(operation);
        System.out.println(request);
        ElementOccurs opCache = SoapUtils.buildElementOccurs(builder, operation,true);
        Assert.assertEquals(Integer.MAX_VALUE, opCache.eval("getCat.arg0.footMap.entry"));
        Assert.assertEquals(Integer.MAX_VALUE, opCache.eval("getCat.arg0.footsList"));
        Assert.assertEquals(Integer.MAX_VALUE, opCache.eval("getCat.arg0.stringList"));
    }


}
