package net.cycastic.portfoliotoolkit.domain;

import net.cycastic.portfoliotoolkit.domain.dto.FolderItemType;
import net.cycastic.portfoliotoolkit.domain.model.ListingType;

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

    public static FolderItemType fromListingType(ListingType t){
        return switch (t){
            case TEXT -> FolderItemType.TEXT;
            case DECIMAL -> FolderItemType.DECIMAL;
            case ATTACHMENT -> FolderItemType.ATTACHMENT;
        };
    }
}
