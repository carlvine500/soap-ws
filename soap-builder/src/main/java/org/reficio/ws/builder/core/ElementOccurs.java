package org.reficio.ws.builder.core;

import java.util.HashMap;
import java.util.Map;

public class ElementOccurs {
    public int maxOccurs;
    public Map<String, ElementOccurs> children = new HashMap<String, ElementOccurs>();

    public void put(String qName, ElementOccurs elementOccurs) {
        children.put(qName, elementOccurs);
    }

    public ElementOccurs get(String qName) {
        return children.get(qName);
    }

    public ElementOccurs createChild(String qName) {
        ElementOccurs elementOccurs = new ElementOccurs();
        children.put(qName, elementOccurs);
        return elementOccurs;
    }

    public Map<String, ElementOccurs> getChildren() {
        return children;
    }
}