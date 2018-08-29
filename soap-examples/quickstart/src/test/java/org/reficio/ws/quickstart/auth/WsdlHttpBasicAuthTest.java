package org.reficio.ws.quickstart.auth;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.dom4j.DocumentException;
import org.reficio.ws.builder.core.Wsdl;

import javax.wsdl.WSDLException;
import java.io.IOException;
import java.net.URL;

/**
 * nginx 配置：
 * <pre>
  server {
 listen 80;
         server_name localhost;
                auth_basic "Restricted";
                auth_basic_user_file /Users/tingfeng/nginx.pass;
                location / {
                    #return 200 "ok1";
                    proxy_pass http://localhost:8080;
                }
 }
 </pre>
 */
public class WsdlHttpBasicAuthTest {
    public static void main(String[] args) throws DocumentException, IOException, WSDLException {
//        URL url = new URL("http://localhost:8080/HelloWorld?wsdl");
        URL url = new URL("http://localhost/HelloWorld?wsdl");

//        String xml = HttpClientUtil.httpGetRequestAuth(url.toString(), "ttlsa:123456");
//        System.out.println("xml="+xml);
        Wsdl wsdl = Wsdl.parse(url/*, "ttlsa:123456"*/);
        System.out.println(JSON.toJSONString(wsdl.getBindings(), SerializerFeature.PrettyFormat));


    }


}
