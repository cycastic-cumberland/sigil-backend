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
import java.util.regex.Pattern;

public class ApplicationUtilities {
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

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

    public static FolderItemType fromListingType(ListingType t){
        return switch (t){
            case ATTACHMENT -> FolderItemType.ATTACHMENT;
        };
    }

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

    // https://stackoverflow.com/a/611117
    public static String decodeURIComponent(String s)
    {
        if (s == null)
        {
            return null;
        }

        return URLDecoder.decode(s, StandardCharsets.UTF_8);
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

    public static Path getTempFile(){
        var tempDir = System.getProperty("java.io.tmpdir");
        String fileName;
        {
            var bytes = new byte[32];
            CryptographicUtilities.generateRandom(bytes);
            fileName = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        }

        return Paths.get(tempDir, fileName);
    }

    public static boolean isEmail(@NotNull String input){
        return EMAIL_PATTERN.matcher(input).matches();
    }
}
