package net.cycastic.sigil.domain.exception;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.SneakyThrows;
import jakarta.annotation.Nullable;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

@Getter
public class RequestException extends RuntimeException {
    @Data
    private static class ExceptionData {
        private String exceptionCode;
        private int statusCode;

        private String en;
        private String vi;
    }

    private static final Map<String, ExceptionData> EXCEPTION_MAP;

    private final int responseCode;

    @Nullable
    private final String exceptionCode;

    static {
        EXCEPTION_MAP = loadExceptionData();
    }
    public RequestException(int responseCode, @Nullable String exceptionCode, Throwable exception, @NotNull String message){
        super(message, exception);
        this.responseCode = responseCode;
        this.exceptionCode = exceptionCode;
    }

    public RequestException(int responseCode, Throwable exception, @NotNull String message){
        this(responseCode, null, exception, message);
    }

    public RequestException(int responseCode, @Nullable String message){
        this(responseCode, null, message);
    }

    public RequestException(int responseCode, @NotNull String template, Object... args){
        this(responseCode, String.format(template, args));
    }

    public RequestException(int responseCode, Throwable exception, @NotNull String template, Object... args){
        this(responseCode, exception, String.format(template, args));
    }

    @SneakyThrows
    private static Map<String, ExceptionData> loadExceptionData(){
        Map<String, ExceptionData> map = new HashMap<>();
        List<String> exceptionList;
        try (var reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(RequestException.class.getClassLoader().getResourceAsStream("exception-list.txt"))))){
            exceptionList = reader.lines().toList();
        }
        for (var file : exceptionList){
            try (var stream = RequestException.class.getClassLoader().getResourceAsStream(file)){
                Objects.requireNonNull(stream);
                var mapper = new CsvMapper();
                var schema = CsvSchema.emptySchema().withHeader();
                try (MappingIterator<ExceptionData> it = mapper.readerFor(ExceptionData.class)
                        .with(schema)
                        .readValues(stream)){
                    while (it.hasNext()){
                        var row = it.next();
                        map.put(row.getExceptionCode(), row);
                    }
                }
            }
        }

        return Map.copyOf(map);
    }

    public static RequestException withExceptionCode(String exceptionCode, Throwable throwable, Object... args){
        var data = EXCEPTION_MAP.get(exceptionCode);
        if (data == null){
            throw new IllegalArgumentException("Could not find exception with code: " + exceptionCode);
        }

        String template = null;
        try {
            if (RequestContextHolder.currentRequestAttributes() instanceof ServletRequestAttributes attr) {
                var locale = attr.getRequest().getLocale();
                if (locale == null){
                    locale = Locale.ENGLISH;
                }

                var language = locale.getLanguage();
                template = switch (language){
                    case "vi" -> data.vi;
                    default -> data.en;
                };
            }
        } catch (IllegalStateException ignored){

        } finally {
            if (template == null){
                template = data.en;
            }
        }

        return new RequestException(data.statusCode,
                data.exceptionCode,
                throwable,
                args.length == 0 ? template : String.format(template, args));
    }

    public static RequestException withExceptionCode(String exceptionCode, Object... args){
        return withExceptionCode(exceptionCode, null, args);
    }

    public static RequestException forbidden(){
        return withExceptionCode("C403T000");
    }
}
