package net.cycastic.sigil.domain;

import jakarta.validation.constraints.NotNull;
import net.cycastic.sigil.domain.dto.FolderItemType;
import net.cycastic.sigil.domain.model.listing.ListingType;

import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.regex.Pattern;

public class ApplicationUtilities {
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private static volatile Object blackhole;

    public static String getMimeType(String fileName) {
        return URLConnection.guessContentTypeFromName(fileName);
    }

    public static OptionalInt tryParseInt(String str){
        try {
            return OptionalInt.of(Integer.parseInt(str));
        } catch (NumberFormatException e){
            return OptionalInt.empty();
        }
    }

    public static OptionalLong tryParseLong(String str){
        try {
            return OptionalLong.of(Long.parseLong(str));
        } catch (NumberFormatException e){
            return OptionalLong.empty();
        }
    }

    public static FolderItemType fromListingType(ListingType t){
        return switch (t){
            case ATTACHMENT -> FolderItemType.ATTACHMENT;
        };
    }

    @Deprecated
    public static String encodeURIComponent(Object obj)
    {
        return UrlUtilities.encodeURIComponent(obj);
    }

    @Deprecated
    public static String decodeURIComponent(String s)
    {
        return UrlUtilities.decodeURIComponent(s);
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

    @Deprecated
    public static Path getTempFile(){
        return FilesUtilities.getTempFile();
    }

    public static boolean isEmail(@NotNull String input){
        return EMAIL_PATTERN.matcher(input).matches();
    }

    public static <T> void deoptimize(T value){
        blackhole = value;
    }
}
