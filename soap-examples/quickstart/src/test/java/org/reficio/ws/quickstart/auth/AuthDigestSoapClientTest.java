package org.reficio.ws.quickstart.auth;

import com.sun.xml.internal.messaging.saaj.soap.dynamic.SOAPMessageFactoryDynamicImpl;
import org.reficio.ws.builder.SoapBuilder;
import org.reficio.ws.builder.SoapOperation;
import org.reficio.ws.builder.core.Wsdl;
import org.reficio.ws.client.core.SoapClient;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.ws.soap.security.wss4j2.Wss4jSecurityInterceptor;

import javax.xml.soap.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * Created by tingfeng on 2018/6/25.
 */
public class AuthDigestSoapClientTest {
    public static void main(String[] args) throws Exception {
//        String spec = "http://localhost:8080/HelloWorld?wsdl";
//        URL wsdlUrl = new URL(spec);
        String spec = "http://localhost:8081/HelloWorld?wsdl";
        URL wsdlUrl = new URL(spec);

        Wsdl wsdl = Wsdl.parse(wsdlUrl);


        SoapBuilder builder = wsdl.binding()
                .localPart("HelloWorldSoapBinding")
                .find();
        SoapOperation operation = builder.operation()
                .name("echo")
                .find();
//        RequestConfig
        String request = builder.buildInputMessage(operation);
        System.out.println("request:\n" + request);
//        SoapMessageBuilder.getSoapVersion(bin)
        SOAPMessage soapMessage = convertXml2SoapMessage(request);
//        addHeader(soapMessage);

        Wss4jSecurityInterceptor interceptor = new Wss4jSecurityInterceptor();
        interceptor.setSecurementActions("UsernameToken");
        interceptor.setSecurementPasswordType("PasswordText");
//        interceptor.setSecurementPasswordType("PasswordDigest");
        interceptor.setSecurementUsername("kevin");
        interceptor.setSecurementPassword("111111");
        interceptor.setSecurementUsernameTokenNonce(false);
        interceptor.setSecurementUsernameTokenCreated(false);
//        interceptor.setSecurementSignatureKeyIdentifier();
//        interceptor.setSecurementSignatureAlgorithm();
//        interceptor.setSecurementSignatureDigestAlgorithm();



        SoapClient client = SoapClient.builder()
                .endpointUri(spec)/*.endpointSecurity(ttlsa)*/
                .build();

        SaajSoapMessageFactory messageFactory = new SaajSoapMessageFactory();
        SaajSoapMessage saajSoapMessage = new SaajSoapMessage(soapMessage, new SOAPMessageFactoryDynamicImpl());
        interceptor.handleRequest(new DefaultMessageContext(saajSoapMessage, messageFactory));
        request = convertSaajSoapMessage2String(saajSoapMessage);
        System.out.println("request(header):\n" + request);
//        request="";
        String postResult = client.post(request);
        System.out.println(postResult);

//        org.dom4j.Document dom4jDoc = DocumentHelper.createDocument();
//        org.w3c.dom.Document w3cDoc = new DOMWriter().write(dom4jDoc)


    }



    private static SOAPMessage convertXml2SoapMessage(String xml) throws SOAPException, IOException {
        String protocol = xml.contains(org.reficio.ws.client.core.SoapConstants.SOAP_1_1_NAMESPACE) ?
                SOAPConstants.SOAP_1_1_PROTOCOL : SOAPConstants.SOAP_1_2_PROTOCOL;
        MessageFactory factory = MessageFactory.newInstance(protocol);
        SOAPMessage message = factory.createMessage(new MimeHeaders(), new ByteArrayInputStream(xml.getBytes(Charset.forName("UTF-8"))));
        return message;
    }

    private static String convertSaajSoapMessage2String(SaajSoapMessage saajSoapMessage) throws IOException, SOAPException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        saajSoapMessage.getSaajMessage().writeTo(out);
        return new String(out.toByteArray());
    }
//    private static void addHeader(SOAPMessage soapMessage) {
//        {
//            try {
//                String usernameStr = "kevin";
//                String passwordStr = "111111";
//
//                //From the spec: Password_Digest = Base64 ( SHA-1 ( nonce + created + password ) )
//                //Make the nonce
//                SecureRandom rand = SecureRandom.getInstance("SHA1PRNG");
//                rand.setSeed(System.currentTimeMillis());
//                byte[] nonceBytes = new byte[16];
//                rand.nextBytes(nonceBytes);
//
//                //Make the created date
//                DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
//                df.setTimeZone(TimeZone.getTimeZone("UTC"));
//                String createdDate = df.format(Calendar.getInstance().getTime());
//                byte[] createdDateBytes = createdDate.getBytes("UTF-8");
//
//                //Make the password
//                byte[] passwordBytes = passwordStr.getBytes("UTF-8");
//
//                //SHA-1 hash the bunch of it.
//                ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                baos.write(nonceBytes);
//                baos.write(createdDateBytes);
//                baos.write(passwordBytes);
//                MessageDigest md = MessageDigest.getInstance("SHA-1");
//                byte[] digestedPassword = md.digest(baos.toByteArray());
//
//                //Encode the password and nonce for sending
//                String passwordB64 = (new BASE64Encoder()).encode(digestedPassword);
//                String nonceB64 = (new BASE64Encoder()).encode(nonceBytes);
//
//                //Now create the header with all the appropriate elements
//                SOAPEnvelope envelope = soapMessage.getSOAPPart().getEnvelope();
//                SOAPHeader header = envelope.getHeader();
//                if (header == null) {
//                    header = envelope.addHeader();
//                }
//
//                SOAPElement security = header.addChildElement("Security", "wsse", WSConstants.WSSE_NS);
//                SOAPElement usernameToken = security.addChildElement("UsernameToken", "wsse");
//
//                SOAPElement username = usernameToken.addChildElement("Username", "wsse");
//                username.addTextNode(usernameStr);
//
//                SOAPElement password = usernameToken.addChildElement("Password", "wsse");
////                password.setAttribute("Type", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordDigest");
//                password.setAttribute("Type", WSConstants.PASSWORD_DIGEST);
//
//                password.addTextNode(passwordB64);
//
//                SOAPElement nonce = usernameToken.addChildElement("Nonce", "wsse");
//                nonce.setAttribute("EncodingType", WSConstants.SOAPMESSAGE_NS + "#Base64Binary");
//                nonce.addTextNode(nonceB64);
//
//                SOAPElement created = usernameToken.addChildElement("Created", "wsu", WSConstants.WSU_NS);
//                created.addTextNode(createdDate);
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }
//
//        }
//    }
}