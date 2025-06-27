package net.cycastic.portfoliotoolkit.domain;

import jakarta.validation.constraints.NotNull;
import net.cycastic.portfoliotoolkit.domain.dto.FolderItemType;
import net.cycastic.portfoliotoolkit.domain.model.ListingType;

import java.io.UnsupportedEncodingException;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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

    // https://stackoverflow.com/a/611117
    public static String encodeURIComponent(String s)
    {
        String result;
        result = URLEncoder.encode(s, StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20")
                .replaceAll("%21", "!")
                .replaceAll("%27", "'")
                .replaceAll("%28", "(")
                .replaceAll("%29", ")")
                .replaceAll("%7E", "~");

        return result;
    }

    public static String shardObjectKey(@NotNull String objectKey){
        assert objectKey.length() >= 36;
        var sb = new StringBuilder()
                .append(objectKey, 0, 2)
                .append('/')
                .append(objectKey, 2, 4)
                .append('/')
                .append(objectKey, 4, 6)
                .append('/')
                .append(objectKey, 6, 8)
                .append('/')
                .append(objectKey);
        return sb.toString();
    }
}
