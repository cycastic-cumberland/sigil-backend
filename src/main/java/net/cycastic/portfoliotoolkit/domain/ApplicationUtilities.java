package net.cycastic.portfoliotoolkit.domain;

import java.util.Optional;

public class ApplicationUtilities {
    public static Optional<Integer> tryParseInt(String str){
        try {
            return Optional.of(Integer.parseInt(str));
        } catch (NumberFormatException e){
            return Optional.empty();
        }
    }
}
