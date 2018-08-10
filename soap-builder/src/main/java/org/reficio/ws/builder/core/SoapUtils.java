/**
 * Copyright (c) 2012-2013 Reficio (TM) - Reestablish your software!. All Rights Reserved.
 * <p>
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.reficio.ws.builder.core;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.apache.commons.lang3.StringUtils;
import org.apache.xmlbeans.SchemaGlobalElement;
import org.apache.xmlbeans.SchemaParticle;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlException;
import org.dom4j.*;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.reficio.ws.builder.SoapBuilder;
import org.reficio.ws.builder.SoapOperation;
import org.reficio.ws.builder.SoapOperationBuilder;
import org.reficio.ws.legacy.SchemaDefinitionWrapper;
import org.reficio.ws.legacy.SoapMessageBuilder;
import org.reficio.ws.legacy.SoapVersion;
import org.reficio.ws.legacy.WsdlUtils;

import javax.wsdl.*;
import javax.wsdl.extensions.ElementExtensible;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.wsdl.extensions.soap12.SOAP12Operation;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

/**
 * @author Tom Bujok
 * @since 1.0.0
 */
public class SoapUtils {

    // removes "" from soap action
    public static String normalizeSoapAction(String soapAction) {
        String normalizedSoapAction = "";
        if (soapAction != null && soapAction.length() > 0) {
            normalizedSoapAction = soapAction;
            if (soapAction.charAt(0) == '"' && soapAction.charAt(soapAction.length() - 1) == '"') {
                normalizedSoapAction = soapAction.substring(1, soapAction.length() - 1).trim();
            }
        }
        return normalizedSoapAction;
    }

    public static String getSOAPActionUri(BindingOperation operation) {
        List extensions = operation.getExtensibilityElements();
        if (extensions != null) {
            for (int i = 0; i < extensions.size(); i++) {
                ExtensibilityElement extElement = (ExtensibilityElement) extensions.get(i);
                if (extElement instanceof SOAPOperation) {
                    SOAPOperation soapOp = (SOAPOperation) extElement;
                    return soapOp.getSoapActionURI();
                } else if (extElement instanceof SOAP12Operation) {
                    SOAP12Operation soapOp = (SOAP12Operation) extElement;
                    return soapOp.getSoapActionURI();
                }
            }
        }
        return null;
    }

    public static SoapOperationBuilder createOperation(SoapBuilder builder, Binding binding, BindingOperation operation, String soapAction) {
        return SoapOperationImpl.create(builder, binding, operation, soapAction);
    }

    public static String soapJson2Xml(String xmlTemplate, String json) throws DocumentException {
        return soapJson2Xml(xmlTemplate, json, false);
    }

    /**
     * @param xmlTemplate  eg:<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:quic="http://quickstart.ws.reficio.org/">" +
     *                     "<soapenv:Body><quic:getCat><arg0>${xxx}</arg0></quic:getCat></soapenv:Body></soapenv:Envelope>
     * @param json         eg:{"Envelope":{"Body":{"getCat":{"arg0":"arg0Value"}}}}
     * @param prettyFormat
     * @return
     * @throws DocumentException
     */
    public static String soapJson2Xml(String xmlTemplate, String json, boolean prettyFormat) throws DocumentException {
        Document doc = DocumentHelper.parseText(xmlTemplate);
        JSONObject jsonObject = JSON.parseObject(json);
        replaceTemplateWithJson(doc, jsonObject);
        if (prettyFormat) {
            return formatXml(doc);
        } else {
            return doc.asXML();
        }
    }

    public static String formatXml(String xml) throws DocumentException {
        Document doc = DocumentHelper.parseText(xml);
        return formatXml(doc);
    }

    public static String formatXml(Document doc){
        try {
            StringWriter writer = new StringWriter();
            OutputFormat format = OutputFormat.createPrettyPrint();
            format.setEncoding("utf-8");
            XMLWriter xmlwriter = new XMLWriter(writer, format);
            xmlwriter.write(doc);
            return writer.toString();
        } catch (IOException e) {
            throw new IllegalStateException("doc=" + doc.asXML(), e);
        }
    }

    public static void replaceTemplateWithJson(Document doc, JSONObject jsonObject) {
        Element envelopeElement = doc.getRootElement();
        walkXmlJson(envelopeElement, jsonObject.getJSONObject("Envelope"));
        clearEmptyElement(doc.getRootElement());
    }


    public static void clearEmptyElement(Element element) {
        List<Element> elements = element.elements();
        for (Element e : elements) {
            if (e.elements().isEmpty()) {
                if (isTemplateString(e.getText())) {
                    e.setText("");
                }
            } else {
                clearEmptyElement(e);
            }
        }
    }

    private static void walkXmlJson(Element element, JSON json) {
        if (json instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject) json;
            for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
                String key = entry.getKey();
                Element subElement = element.element(key);
                if (subElement == null) {
                    continue;
                }
                Object value = entry.getValue();
                if (value instanceof JSON) {
                    walkXmlJson(subElement, (JSON) entry.getValue());
                } else {
                    if (isTemplateString(subElement.getText())) {
                        subElement.setText(value != null ? String.valueOf(value) : "");
                    } /*else {
                        Element newElement = (Element) subElement.clone();
                        newElement.setText(value != null ? (String) value : "");
                        element.add(newElement);
                    }*/
                }
            }

        } else if (json instanceof JSONArray) {
            JSONArray jsonArray = (JSONArray) json;
            for (Object subObj : jsonArray) {
                Element clone = (Element) element.clone();
                element.getParent().add(clone);
                if (subObj instanceof JSON) {
                    walkXmlJson(clone, (JSON) subObj);
                } else {
                    clone.setText(String.valueOf(subObj));
                }
            }
            element.getParent().remove(element);
        }
    }

    private static boolean isTemplateString(String text) {
        return StringUtils.isNotBlank(text) && StringUtils.startsWith(text, "${") && StringUtils.endsWith(text, "}");
    }

    public static String soapXml2Json(ElementOccurs cache, String xmlStr, boolean prettyFormat) throws DocumentException {
        JSONObject jsonObject = soapXml2Json(cache, xmlStr);
        if (prettyFormat) {
            return JSON.toJSONString(jsonObject, SerializerFeature.PrettyFormat);
        } else {
            return JSON.toJSONString(jsonObject);
        }
    }

    public static JSONObject soapXml2Json(ElementOccurs cache, String xmlStr) throws DocumentException {
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
        if (headerElement != null) {
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
            ElementOccurs tmp = elementOccurs == null ? ElementOccurs.EMPTY_INSTANCE : elementOccurs.get(e.getName());
            boolean isArray = false;
            if (tmp != null) {
                isArray = tmp.maxOccurs > 1;
            } else {
                tmp = elementOccurs;
            }
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

    private static boolean isEmpty(String str) {

        if (str == null || str.trim().isEmpty() || "null".equals(str)) {
            return true;
        }
        return false;
    }

    public static ElementOccurs buildElementOccurs(SoapBuilder builder, SoapOperation operation,boolean isInput) throws XmlException {
        SoapMessageBuilder messageBuilder = builder.getSoapFacade().getMessageBuilder();
        SchemaDefinitionWrapper schemaDefinition = messageBuilder.getSchemaDefinitionWrapper();
        SoapBuilderImpl builderImpl = (SoapBuilderImpl) builder;
        Definition definition = schemaDefinition.getDefinition();
        ElementOccurs opCache = new ElementOccurs();

        BindingOperation bindingOperation = builderImpl.getBindingOperation(operation);
        Part[] parts = isInput ? WsdlUtils.getInputParts(bindingOperation) : WsdlUtils.getOutputParts(bindingOperation);
        for (Part part : parts) {
            processPart(schemaDefinition, part, opCache.getChildren());
        }

        ElementExtensible bindingInput = isInput ? bindingOperation.getBindingInput() : bindingOperation.getBindingOutput();
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
        javax.xml.namespace.QName type = part.getTypeName();
        javax.xml.namespace.QName elementName = part.getElementName();
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
                    javax.xml.namespace.QName name = contentModel.getName();
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
}
