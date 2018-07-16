package org.reficio.ws.quickstart;

import org.reficio.ws.builder.SoapBuilder;
import org.reficio.ws.builder.SoapOperation;
import org.reficio.ws.builder.core.Wsdl;
import org.reficio.ws.client.core.SoapClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HelloWordConsumer {
    private static final Logger log = LoggerFactory.getLogger(HelloWordConsumer.class);

    public static void main(String[] args)throws Exception {
        consume();
    }
//    @Test
//    @Ignore
    public static void consume() throws Exception {
        String endpointUri = "http://localhost:9001/HelloWorld?wsdl";
       final SoapClient client = SoapClient.builder()
                .endpointUri(endpointUri)
                .build();

        // generate is slow
        final String envelope = generateEnvelope(endpointUri, "HelloWordServerPortBinding", "echo");
        ExecutorService pool = Executors.newFixedThreadPool(5);
        for (int i = 0; i < 10; i++) {
            pool.submit(new Runnable() {
                @Override
                public void run() {

//                    synchronized (HelloWordConsumer.class) {
                        log.info("--");
                        String post = client.post(envelope);
                    System.out.println(post);
                        log.info(post);
//                    }

                }
            });

        }


    }

    /**
     * <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:cod="http://coding.ws.successchannel.com/">
     * <soapenv:Header/>
     * <soapenv:Body>
     * <cod:echo>
     * <cod:arg0>gero et</cod:arg0>
     * </cod:echo>
     * </soapenv:Body>
     * </soapenv:Envelope>
     */
    public static String generateEnvelope(String wsdlUri, String binding, String operation) throws Exception {
        URL wsdlUrl = new URL(wsdlUri);
        Wsdl wsdl = Wsdl.parse(wsdlUrl);


        SoapBuilder builder = wsdl.binding().localPart(binding).find();
        SoapOperation soapOperation = builder.operation().name(operation).find();
        String request = builder.buildInputMessage(soapOperation);
        System.out.println(request);
        return request;

    }
}  