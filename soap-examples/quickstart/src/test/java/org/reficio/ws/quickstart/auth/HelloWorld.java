package org.reficio.ws.quickstart.auth;

import javax.jws.WebService;

/**
 * Created by tingfeng on 2018/6/13.
 */
@WebService
public interface HelloWorld {
    String echo(String arg);
    String echo1(String arg);
}
