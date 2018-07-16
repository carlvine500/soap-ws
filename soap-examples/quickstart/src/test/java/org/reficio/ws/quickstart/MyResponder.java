package org.reficio.ws.quickstart;

import org.reficio.ws.SoapContext;
import org.reficio.ws.builder.SoapBuilder;
import org.reficio.ws.builder.SoapOperation;
import org.reficio.ws.common.XmlUtils;
import org.reficio.ws.server.SoapServerException;
import org.reficio.ws.server.responder.AbstractResponder;
import org.springframework.ws.soap.SoapMessage;
import org.w3c.dom.Document;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;


public class MyResponder extends AbstractResponder {

    private final SoapContext context;

    /**
     * Constructs an auto responder for the specified binding of the builder
     *
     * @param builder     Soap builder used to construct messages
     */
    public MyResponder(SoapBuilder builder) {
        super(builder);
        this.context = SoapContext.builder().exampleContent(true).build();
    }

    /**
     * Constructs an auto responder for the specified binding of the builder, fine-tuning the content of the generated messages
     * by passing the SoapContext
     *
     * @param builder     Soap builder used to construct messages
     * @param context     Contect that is passed to the builder to fine-tune the content of the generated responses
     */
    public MyResponder(SoapBuilder builder, SoapContext context) {
        super(builder);
        this.context = context;
    }

    @Override
    public Source respond(SoapOperation invokedOperation, SoapMessage message) {
        try {
            System.out.println(xml2string(message.getDocument()));
            String response = getBuilder().buildOutputMessage(invokedOperation, context);
            return XmlUtils.xmlStringToSource(response);
        } catch (Exception e) {
            throw new SoapServerException(e);
        }
    }

    public static String xml2string(Document doc) {
        try {
            StringWriter sw = new StringWriter();
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

            transformer.transform(new DOMSource(doc), new StreamResult(sw));
            return sw.toString();
        } catch (Exception ex) {
            throw new RuntimeException("Error converting to String", ex);
        }
    }

}
