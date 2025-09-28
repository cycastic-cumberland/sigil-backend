package net.cycastic.sigil.domain;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class UrlUtilities {
    public static String encodeURIComponent(Object obj)
    {
        String result;
        result = URLEncoder.encode(obj instanceof String s ? s : obj.toString(), StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20")
                .replaceAll("%21", "!")
                .replaceAll("%27", "'")
                .replaceAll("%28", "(")
                .replaceAll("%29", ")")
                .replaceAll("%7E", "~");

        return result;
    }

    public static String decodeURIComponent(String s)
    {
        if (s == null)
        {
            return null;
        }

        return URLDecoder.decode(s, StandardCharsets.UTF_8);
    }
}
