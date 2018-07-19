package org.reficio.ws.builder.core;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class ElementOccurs {
    public static final ElementOccurs EMPTY_INSTANCE = new ElementOccurs();
    public int maxOccurs;
    public Map<String, ElementOccurs> children = new HashMap<String, ElementOccurs>();
    public transient ElementOccurs root;

    public void put(String qName, ElementOccurs elementOccurs) {
        children.put(qName, elementOccurs);
    }

    public ElementOccurs get(String qName) {
        return children.get(qName);
    }

    public static ElementOccurs newInstance() {
        ElementOccurs elementOccurs = new ElementOccurs();
        elementOccurs.root = elementOccurs;
        return elementOccurs;
    }

    public ElementOccurs createChild(String qName) {
        ElementOccurs elementOccurs = new ElementOccurs();
        elementOccurs.root = this.root;
        children.put(qName, elementOccurs);
        return elementOccurs;
    }

    public ElementOccurs getRoot() {
        return root;
    }

    public ElementOccurs setMaxOccurs(int maxOccurs) {
        this.maxOccurs = maxOccurs;
        return this;
    }

    public Map<String, ElementOccurs> getChildren() {
        return children;
    }

    public int eval(String expression){
        String[] elementNames = StringUtils.split(expression, ".");
        ElementOccurs current = this;
        for (String elementName : elementNames) {
            current = current.get(elementName);
        }
        return current.maxOccurs;
    }


}