package org.reficio.ws.builder.core;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.xmlbeans.SchemaGlobalElement;
import org.apache.xmlbeans.SchemaParticle;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlException;
import org.dom4j.*;
import org.reficio.ws.builder.SoapBuilder;
import org.reficio.ws.builder.SoapOperation;
import org.reficio.ws.legacy.SchemaDefinitionWrapper;
import org.reficio.ws.legacy.SoapMessageBuilder;
import org.reficio.ws.legacy.SoapVersion;
import org.reficio.ws.legacy.WsdlUtils;

import javax.wsdl.*;
import javax.xml.namespace.QName;
import java.util.List;
import java.util.Map;

/**
 * Created by tingfeng on 2018/7/17.
 */
public class SoapXmlJsonUtil {
    public static ElementOccurs buildElementOccursCache(SoapBuilder builder, SoapOperation operation) throws XmlException {
        SoapMessageBuilder messageBuilder = builder.getSoapFacade().getMessageBuilder();
        SchemaDefinitionWrapper schemaDefinition = messageBuilder.getSchemaDefinitionWrapper();
        SoapBuilderImpl builderImpl = (SoapBuilderImpl) builder;
        Definition definition = schemaDefinition.getDefinition();
        SoapVersion soapVersion = messageBuilder.getSoapVersion(builderImpl.getBinding());
        ElementOccurs opCache = new ElementOccurs();

        BindingOperation bindingOperation = builderImpl.getBindingOperation(operation);
        Part[] parts = WsdlUtils.getInputParts(bindingOperation);
        for (Part part : parts) {
            processPart(schemaDefinition, part, opCache.getChildren());
        }

        BindingInput bindingInput = bindingOperation.getBindingInput();
        if (bindingInput != null) {
            List<?> extensibilityElements = bindingInput.getExtensibilityElements();
            List<WsdlUtils.SoapHeader> soapHeaders = WsdlUtils.getSoapHeaders(extensibilityElements);
            for (WsdlUtils.SoapHeader header : soapHeaders) {
                Message message = definition.getMessage(header.getMessage());
                Part part = message.getPart(header.getPart());
                processPart(schemaDefinition, part, opCache.getChildren());
            }
        }
        return opCache;
    }

    private static void processPart(SchemaDefinitionWrapper schemaDefinition, Part part, Map<String, ElementOccurs> opCache) {
        QName type = part.getTypeName();
        QName elementName = part.getElementName();
        if (elementName != null) {
            type = elementName;
        }
        SchemaGlobalElement schemaGlobalElement = schemaDefinition.getSchemaTypeLoader().findElement(type);
        SchemaType stype = schemaGlobalElement.getType();
        ElementOccurs elementOccurs = new ElementOccurs();
        opCache.put(stype.getName().getLocalPart(), elementOccurs);
        processSchemaType(stype, elementOccurs);
    }

    private static void processSchemaType(SchemaType stype, ElementOccurs elementOccurs) {
        if (stype.isSimpleType() || stype.isURType()) {
            processSimpleType(stype, elementOccurs);
            return;
        }
//        QName nm = stype.getName();
//        if (nm == null && stype.getContainerField() != null)
//            nm = stype.getContainerField().getName();
        switch (stype.getContentType()) {
            case SchemaType.NOT_COMPLEX_TYPE:
            case SchemaType.EMPTY_CONTENT: // noop
                break;
            case SchemaType.SIMPLE_CONTENT: {
                processSimpleType(stype, elementOccurs);
            }
            case SchemaType.MIXED_CONTENT:
            case SchemaType.ELEMENT_CONTENT:
                SchemaParticle contentModel = stype.getContentModel();
                if (contentModel != null) {
                    QName name = contentModel.getName();
                    if (name != null) {
                        elementOccurs = elementOccurs.createChild(name.getLocalPart());
                    }
                    processParticle(contentModel, elementOccurs);
                }
                break;
            default:
                throw new IllegalStateException("can't process SchemaType=" + stype.getContentType());
        }
    }

    private static void processSimpleType(SchemaType stype, ElementOccurs elementOccurs) {
//        SchemaType primitiveType = stype.getPrimitiveType();
//        System.out.println("stype" + primitiveType);
    }

    private static void processElement(SchemaParticle sp, ElementOccurs elementOccurs) {
//        System.out.println(sp.getName() + "==>");
        processSchemaType(sp.getType(), elementOccurs);
    }

    private static void processSequence(SchemaParticle sp, ElementOccurs elementOccurs) {
        SchemaParticle[] sps = sp.getParticleChildren();
        for (SchemaParticle schemaParticle : sps) {
            ElementOccurs tmp = elementOccurs.createChild(schemaParticle.getName().getLocalPart());
            processParticle(schemaParticle, tmp);
        }
    }

    private static void processParticle(SchemaParticle sp, ElementOccurs elementOccurs) {
        if (sp.getName() != null) {
            elementOccurs.maxOccurs = sp.getIntMaxOccurs();
        }
        boolean hasSibling = false;
        do {
            switch (sp.getParticleType()) {
                case (SchemaParticle.ELEMENT):
                    processElement(sp, elementOccurs);
                    break;
                case (SchemaParticle.SEQUENCE):
                    processSequence(sp, elementOccurs);
                    break;
                case (SchemaParticle.CHOICE):
                    // processChoice(sp, xmlc, mixed);
                    // break;
                case (SchemaParticle.ALL):
                    // processAll(sp, xmlc, mixed);
                    // break;
                case (SchemaParticle.WILDCARD):
                    // processWildCard(sp, xmlc, mixed);
                    // break;
                default:
                    throw new IllegalStateException("can't process SchemaParticle=" + sp.getParticleType());
            }
        } while (hasSibling);
    }

    public static JSONObject soapXML2JSON(ElementOccurs cache, String xmlStr) throws DocumentException {
        Document doc = DocumentHelper.parseText(xmlStr);
        JSONObject envelope = new JSONObject(1);
        Element envelopeElement = doc.getRootElement();

        Element bodyElement = (Element) envelopeElement.element("Body");
        JSONObject bodyJSON = new JSONObject(1);
        envelope.put("Body", bodyJSON);

        List<Element> bodyElements = bodyElement.elements();
        for (Element bodyEle : bodyElements) {
            JSONObject operation = new JSONObject(1);
            ElementOccurs elementOccurs = cache.getChildren().get(bodyEle.getName());
            dom4j2Json(bodyEle, operation, elementOccurs);
            bodyJSON.put(bodyEle.getName(), operation);
        }

        Element headerElement = (Element) envelopeElement.element("Header");
        JSONObject headerJSON = new JSONObject(1);
        List<Element> headerElements = headerElement.elements();
        for (Element headerEle : headerElements) {
            JSONObject headerJSONObj = new JSONObject(1);
            ElementOccurs elementOccurs = cache.getChildren().get(headerEle.getName());
            dom4j2Json(headerEle, headerJSONObj, elementOccurs);
            headerJSON.put(headerEle.getName(), headerJSONObj);
        }
        if (!headerJSON.isEmpty()) {
            envelope.put("Header", headerJSON);
        }


        JSONObject envelopeWrapper = new JSONObject(1);
        envelopeWrapper.put("Envelope", envelope);
        return envelopeWrapper;
    }

    /**
     * xml转json
     *
     * @param element
     * @param json
     * @param elementOccurs
     */
    public static void dom4j2Json(Element element, JSONObject json, ElementOccurs elementOccurs) {
        //如果是属性
        for (Object o : element.attributes()) {
            Attribute attr = (Attribute) o;
            if (!isEmpty(attr.getValue())) {
                json.put("@" + attr.getName(), attr.getValue());
            }
        }
        List<Element> chdEl = element.elements();
        if (chdEl.isEmpty() && !isEmpty(element.getText())) {//如果没有子元素,只有一个值
            json.put(element.getName(), element.getText());
        }

        for (Element e : chdEl) {//有子元素
            ElementOccurs tmp = elementOccurs.get(e.getName());
            boolean isArray = tmp.maxOccurs > 1;
            if (!e.elements().isEmpty()) {//子元素也有子元素

                JSONObject chdjson = new JSONObject();

                dom4j2Json(e, chdjson, tmp);
                Object o = json.get(e.getName());

                if (o != null) {
                    JSONArray jsona = null;
                    if (o instanceof JSONObject) {//如果此元素已存在,则转为jsonArray
                        JSONObject jsono = (JSONObject) o;
                        json.remove(e.getName());
                        jsona = new JSONArray();
                        jsona.add(jsono);
                        jsona.add(chdjson);
                    } else if (o instanceof JSONArray) {
                        jsona = (JSONArray) o;
                        jsona.add(chdjson);
                    }
                    json.put(e.getName(), jsona);
                } else if (isArray) {
                    JSONArray jsona = new JSONArray();
                    jsona.add(chdjson);
                    json.put(e.getName(), jsona);
                } else {
                    if (!chdjson.isEmpty()) {
                        json.put(e.getName(), chdjson);
                    }
                }


            } else {//子元素没有子元素
                for (Object o : element.attributes()) {
                    Attribute attr = (Attribute) o;
                    if (!isEmpty(attr.getValue())) {
                        json.put("@" + attr.getName(), attr.getValue());
                    }
                }
                if (!e.getText().isEmpty()) {
                    Object o = json.get(e.getName());

                    if (o != null) {
                        if (o instanceof JSONObject) {
                            JSONArray jsona = new JSONArray();
                            jsona.add(o);
                            jsona.add(e.getText());
                            json.put(e.getName(), jsona);
                        } else if (o instanceof JSONArray) {
                            ((JSONArray) o).add(e.getText());
                        }
                    } else if (isArray) {
                        JSONArray jsona = new JSONArray();
                        jsona.add(e.getText());
                        json.put(e.getName(), jsona);
                    } else {
                        json.put(e.getName(), e.getText());
                    }
                }
            }
        }
    }

    public static boolean isEmpty(String str) {

        if (str == null || str.trim().isEmpty() || "null".equals(str)) {
            return true;
        }
        return false;
    }
}
