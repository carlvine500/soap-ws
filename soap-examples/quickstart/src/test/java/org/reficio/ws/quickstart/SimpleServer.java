package org.reficio.ws.quickstart;

import javax.jws.WebService;
import javax.xml.ws.Endpoint;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@WebService
public class SimpleServer {
    public static void main(String[] args) {
        String url = "http://localhost:8080/HelloWorld?wsdl";
        Endpoint.publish(url, new SimpleServer());
    }

    public String echo(String arg0) {
        return arg0;
    }

    public Cat getCat(Cat arg0) {
        return arg0;
    }

    public List<String> getList(List<String> list) {
        return list;
    }


    public static class Cat {
        public String catName = "tom";
        public HashMap<String, Foot> footMap = new HashMap<String, Foot>();
        public List<Foot> footsList = new ArrayList<Foot>();
        public List<String> stringList = new ArrayList<String>();
        public HashMap<String, String> stringMap = new HashMap<String, String>();

        public Cat() {
//            footMap.put("left", new SimpleServer.Foot());
//            footsList.add(new SimpleServer.Foot());
//            stringMap.put("x","y");
        }
    }

    public static class Foot {
        public String footName = "t";
    }


} 