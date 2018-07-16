package org.reficio.ws.quickstart;

import org.reficio.ws.builder.SoapBuilder;
import org.reficio.ws.builder.core.Wsdl;
import org.reficio.ws.common.ResourceUtils;
import org.reficio.ws.server.core.SoapServer;
import org.reficio.ws.server.responder.AutoResponder;

import java.net.URL;
import java.util.concurrent.locks.LockSupport;

/**
 * Created by tingfeng on 2018/6/25.
 */
public class HelloWorldServer {
    public static void main(String[] args) {
        SoapServer server = SoapServer.builder()
                .httpPort(8080)
                .build();
        server.start();

        URL wsdlUrl = ResourceUtils.getResourceWithAbsolutePackagePath("/", "wsdl/HelloWorld.wsdl");
        Wsdl parser = Wsdl.parse(wsdlUrl);
        SoapBuilder builder = parser.binding().localPart("HelloWorldSoapBinding").find();
        MyResponder responder = new MyResponder(builder);
        // TODO how to get wsdl ?
        server.registerRequestResponder("/HelloWorld", responder);
        //server.stop();
        LockSupport.park();
    }
}
