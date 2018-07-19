//package org.reficio.ws.quickstart;
//
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.JSONObject;
//import com.alibaba.fastjson.serializer.SerializerFeature;
//import org.apache.xmlbeans.*;
//import org.dom4j.DocumentException;
//import org.reficio.ws.SoapBuilderException;
//import org.reficio.ws.builder.SoapBuilder;
//import org.reficio.ws.builder.SoapOperation;
//import org.reficio.ws.builder.core.SoapBuilderImpl;
//import org.reficio.ws.builder.core.Wsdl;
//import org.reficio.ws.legacy.SchemaDefinitionWrapper;
//import org.reficio.ws.legacy.SoapMessageBuilder;
//import org.reficio.ws.legacy.SoapVersion;
//import org.reficio.ws.legacy.WsdlUtils;
//
//import javax.wsdl.*;
//import javax.xml.namespace.QName;
//import java.net.MalformedURLException;
//import java.net.URL;
//import java.util.List;
//
///**
// * 1.json->xml 安全起见,request envelope 采用填充的方式,把json应用到xml
// * 2.xml->json response envelope采用参照wsdl.xsd转换,主要是根据maxOccurs>1 处理jsonArray/jsonObject
// * refer: xmlCursor操作xml https:r//xmlbeans.apache.org/docs/2.0.0/guide/conNavigatingXMLwithCursors.html
// * Created by tingfeng on 2018/6/25.
// */
//public class HelloWorldClient1xml2jsonSampleStable {
//    public static void main(String[] args) throws MalformedURLException, DocumentException, XmlException {
//        URL wsdlUrl = new URL("http://localhost:8080/HelloWorld?wsdl");
//        Wsdl wsdl = Wsdl.parse(wsdlUrl);
//
//
//        SoapBuilder builder = wsdl.binding()
//                .localPart("SimpleServerPortBinding")
//                .find();
//        SoapOperation operation = builder.operation()
//                .name("getCat")
//                .find();
//
////        String request = builder.buildInputMessage(operation);
////        System.out.println("request:\n"+request);
//        String request = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:quic=\"http://quickstart.ws.reficio.org/\">\n" +
//                "   <soapenv:Header/>\n" +
//                "   <soapenv:Body>\n" +
//                "      <quic:getCat>\n" +
//                "         <arg0>\n" +
//                "            <catName>gero et</catName>\n" +
////                "            <footMap>\n" +
////                "               <entry>\n" +
////                "                  <key>sonoras imperio</key>\n" +
////                "                  <value>\n" +
////                "                     <footName>quae divum incedo</footName>\n" +
////                "                  </value>\n" +
////                "               </entry>\n" +
////                "            </footMap>\n" +
//                "            <footsList>\n" +
//                "               <footName>verrantque per auras1</footName>\n" +
//                "               <footName>verrantque per auras2</footName>\n" +
//                "               <footName>verrantque per auras3</footName>\n" +
//                "            </footsList>\n" +
////                "            <stringList>per auras1</stringList>\n" +
////                "            <stringList>per auras2</stringList>\n" +
////                "            <stringList>per auras3</stringList>\n" +
////                "            <stringMap>\n" +
////                "               <entry>\n" +
////                "                  <key>circum claustra</key>\n" +
////                "                  <value>nimborum in</value>\n" +
////                "               </entry>\n" +
////                "            </stringMap>\n" +
//                "         </arg0>\n" +
//                "      </quic:getCat>\n" +
//                "   </soapenv:Body>\n" +
//                "</soapenv:Envelope>";
////        JSONObject jsonObject = XmlTool.soapXml2Json(request);
////        System.out.println(JSON.toJSONString(jsonObject, SerializerFeature.PrettyFormat));
////        XmlObject xmlObject = XmlObject.Factory.parse(request);
////        XmlCursor xmlCursor = xmlObject.newCursor();
////        xmlCursor.toFirstChild();// to envelope
////        xmlCursor.toFirstChild();// to body
////        while (xmlCursor.hasNextToken()) {
////            System.out.println(xmlCursor.toNextToken());
////            XmlObject object = xmlCursor.getObject();
////            System.out.println(object);
////            System.out.println(xmlCursor.getName());
////            if (xmlCursor.isText()) {
////                System.out.println(xmlCursor.getTextValue());
////            }
////        }
//
//        xml2Json(builder, operation, request);
//
//    }
//
//
//    private static void xml2Json(SoapBuilder builder, SoapOperation operation, String xml) throws XmlException {
//        SoapMessageBuilder messageBuilder = builder.getSoapFacade().getMessageBuilder();
//        SchemaDefinitionWrapper schemaDefinition = messageBuilder.getSchemaDefinitionWrapper();
//        SoapBuilderImpl builderImpl = (SoapBuilderImpl) builder;
//        Definition definition = schemaDefinition.getDefinition();
//        SoapVersion soapVersion = messageBuilder.getSoapVersion(builderImpl.getBinding());
//
//        XmlObject xmlObject = XmlObject.Factory.parse(xml);
//        XmlCursor xmlCursor = xmlObject.newCursor();
//        xmlCursor.toChild(soapVersion.getEnvelopeQName());
//
//
//        xmlCursor.toChild(soapVersion.getBodyQName());
//        BindingOperation bindingOperation = builderImpl.getBindingOperation(operation);
//        Part[] parts = WsdlUtils.getInputParts(bindingOperation);
//        for (Part part : parts) {
//            processPart(schemaDefinition, part, xmlCursor);
//        }
//        xmlCursor.toParent();
//
//        BindingInput bindingInput = bindingOperation.getBindingInput();
//        if (bindingInput != null) {
//            List<?> extensibilityElements = bindingInput.getExtensibilityElements();
//            List<WsdlUtils.SoapHeader> soapHeaders = WsdlUtils.getSoapHeaders(extensibilityElements);
//            xmlCursor.toChild(soapVersion.getHeaderQName());
//            for (WsdlUtils.SoapHeader header : soapHeaders) {
//                Message message = definition.getMessage(header.getMessage());
//                Part part = message.getPart(header.getPart());
//                processPart(schemaDefinition, part, xmlCursor);
//            }
//            xmlCursor.toParent();
//        }
//        xmlCursor.dispose();
//    }
//
//    private static void processPart(SchemaDefinitionWrapper schemaDefinition, Part part, XmlCursor xmlCursor) {
//        QName type = part.getTypeName();
//        QName elementName = part.getElementName();
//        if (elementName != null) {
//            type = elementName;
//        }
//        xmlCursor.toChild(type);
//        SchemaGlobalElement schemaGlobalElement = schemaDefinition.getSchemaTypeLoader().findElement(type);
//        SchemaType stype = schemaGlobalElement.getType();
//        processSchemaType(stype, xmlCursor);
//        xmlCursor.toParent();
//    }
//
//    public static void processSchemaType(SchemaType stype, XmlCursor xmlCursor) {
//        if (stype.isSimpleType() || stype.isURType()) {
//            processSimpleType(stype, xmlCursor);
//            return;
//        }
//        QName nm = stype.getName();
//        if (nm == null && stype.getContainerField() != null)
//            nm = stype.getContainerField().getName();
//        switch (stype.getContentType()) {
//            case SchemaType.NOT_COMPLEX_TYPE:
//            case SchemaType.EMPTY_CONTENT: // noop
//                break;
//            case SchemaType.SIMPLE_CONTENT: {
//                processSimpleType(stype, xmlCursor);
//            }
//            case SchemaType.MIXED_CONTENT:
//            case SchemaType.ELEMENT_CONTENT:
//                SchemaParticle contentModel = stype.getContentModel();
//                if (contentModel != null) {
//                    QName name = contentModel.getName();
//                    boolean b = false;
//                    if (name != null) {
//                        b = xmlCursor.toChild(name, 0);
//                    }
//                    processParticle(contentModel, xmlCursor);
//
//                    if (b) {
//                        xmlCursor.toParent();
//                    }
//
//                }
//                break;
//            default:
//                throw new IllegalStateException("can't process SchemaType=" + stype.getContentType());
//        }
//    }
//
//    public static void processSimpleType(SchemaType stype, XmlCursor xmlCursor) {
//        SchemaType primitiveType = stype.getPrimitiveType();
//        System.out.println("stype" + primitiveType);
//    }
//
//    public static void processElement(SchemaParticle sp, XmlCursor xmlCursor) {
//        System.out.println(sp.getName() + "==>" + xmlCursor.getObject());
//        processSchemaType(sp.getType(), xmlCursor);
//    }
//
//    public static void processSequence(SchemaParticle sp, XmlCursor xmlCursor) {
//        SchemaParticle[] sps = sp.getParticleChildren();
//        for (SchemaParticle schemaParticle : sps) {
//            boolean hasChild = xmlCursor.toChild(schemaParticle.getName());
//            System.out.println(hasChild);
//            if (hasChild) {
//                processParticle(schemaParticle, xmlCursor);
//                xmlCursor.toParent();
//            }
//
//        }
//    }
//
//    public static void processParticle(SchemaParticle sp, XmlCursor xmlCursor) {
////        boolean hasSibling = xmlCursor.toNextSibling();
////        xmlCursor.getObject().selectChildren("");
//
//        boolean hasSibling = false;
//        do {
//            switch (sp.getParticleType()) {
//                case (SchemaParticle.ELEMENT):
//                    processElement(sp, xmlCursor);
//                    break;
//                case (SchemaParticle.SEQUENCE):
//                    processSequence(sp, xmlCursor);
//                    break;
//                case (SchemaParticle.CHOICE):
//                    // processChoice(sp, xmlc, mixed);
//                    // break;
//                case (SchemaParticle.ALL):
//                    // processAll(sp, xmlc, mixed);
//                    // break;
//                case (SchemaParticle.WILDCARD):
//                    // processWildCard(sp, xmlc, mixed);
//                    // break;
//                default:
//                    throw new IllegalStateException("can't process SchemaParticle=" + sp.getParticleType());
//            }
//
//            if (sp.getIntMaxOccurs() > 1) {
////                if(sp.getType().isSimpleType()){
//                    hasSibling = xmlCursor.toNextSibling(sp.getName());
////                }else{
////                    hasSibling = xmlCursor.tochi(sp.getType().getContentModel().getName());
////                }
//
//            }
//        } while (hasSibling);
//    }
//
//    private static int determineMinMaxForXmlCursor(SchemaParticle sp, XmlCursor xmlCursor) {
//        return 1;
////        xmlCursor.has
//    }
//
//    public static BindingOperation getBindingOperation(Binding binding, SoapOperation op) {
//        BindingOperation operation = binding.getBindingOperation(op.getOperationName(),
//                op.getOperationInputName(), op.getOperationOutputName());
//        if (operation == null) {
//            throw new SoapBuilderException("Operation not found");
//        }
//        return operation;
//    }
//
//
//}