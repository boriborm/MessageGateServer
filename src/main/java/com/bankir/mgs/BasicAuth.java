package com.bankir.mgs;

import javax.xml.bind.DatatypeConverter;
import java.nio.charset.Charset;

/**
 * Created by bankir on 26.10.16.
 */
public class BasicAuth {
    /**
     * Decode the basic auth and convert it to array login/password
     * @param auth The string encoded authentification
     * @return The login (case 0), the password (case 1)
     */
    public static String[] decode(String auth) {
        auth = auth.replaceFirst("[B|b]asic ", "");
        byte[] decodedBytes = DatatypeConverter.parseBase64Binary(auth);
        if(decodedBytes == null || decodedBytes.length == 0){
            return null;
        }
        return new String(decodedBytes).split(":", 2);
    }

    public static String encode(String login, String password){
        String authorization = login+":"+password;
        String encodedAuth = DatatypeConverter.printBase64Binary(authorization.getBytes(Charset.forName("ISO_8859_1")));
        return encodedAuth;
    }
}
