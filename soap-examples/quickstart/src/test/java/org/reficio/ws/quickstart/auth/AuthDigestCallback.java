package org.reficio.ws.quickstart.auth;

import org.apache.log4j.Logger;
import org.apache.ws.security.WSPasswordCallback;
import org.apache.ws.security.WSSecurityException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class AuthDigestCallback implements CallbackHandler {

    Logger logger = Logger.getLogger(AuthDigestCallback.class);

    private Map<String, String> passwords = new HashMap<String, String>();

    public AuthDigestCallback() {
        passwords.put("kevin", "111111");
    }

    public void handle(Callback[] callbacks) throws IOException,
            UnsupportedCallbackException {

        for (int i = 0; i < callbacks.length; i++) {
            WSPasswordCallback pc = (WSPasswordCallback) callbacks[i];
            String identifier = pc.getIdentifier();
            int usage = pc.getUsage();

            if (usage == WSPasswordCallback.USERNAME_TOKEN) {// 密钥方式USERNAME_TOKEN
                logger.info("Client Token [username: " + identifier + ", password: " + passwords.get(identifier) + "] Exist user= " + passwords.containsKey(identifier));
                if (!passwords.containsKey(identifier)) {
                    try {
                        throw new WSSecurityException("User not match - " + identifier);
                    } catch (WSSecurityException e) {
                        e.printStackTrace();
                    }
                }
                pc.setPassword(passwords.get(identifier));// //▲【这里非常重要】▲
            } else if (usage == WSPasswordCallback.SIGNATURE) {// 密钥方式SIGNATURE
                if (!passwords.containsKey(identifier)) {
                    try {
                        throw new WSSecurityException("User not match - " + identifier);
                    } catch (WSSecurityException e) {
                        e.printStackTrace();
                    }
                }
                pc.setPassword(passwords.get(identifier));// //▲【这里非常重要】▲
            }
        }
    }

}