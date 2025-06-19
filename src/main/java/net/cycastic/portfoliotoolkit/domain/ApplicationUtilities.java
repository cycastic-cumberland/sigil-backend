package net.cycastic.portfoliotoolkit.domain;

import java.net.URLConnection;
import java.util.Optional;

public class ApplicationUtilities {
    public static String getMimeType(String fileName) {
        return URLConnection.guessContentTypeFromName(fileName);
    }

    public static Optional<Integer> tryParseInt(String str){
        try {
            return Optional.of(Integer.parseInt(str));
        } catch (NumberFormatException e){
            return Optional.empty();
        }
    }
}
