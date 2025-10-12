package net.cycastic.sigil.domain;

import lombok.SneakyThrows;

import java.io.InputStream;
import java.security.MessageDigest;
import java.security.SecureRandom;

public class SlimCryptographicUtilities {
    private static final SecureRandom RANDOM = new SecureRandom();

    @SneakyThrows
    public static byte[] digestMd5(byte[] data){
        var md = MessageDigest.getInstance("MD5");
        return md.digest(data);
    }

    @SneakyThrows
    public static byte[] digestSha256(InputStream inputStream){
        var md = MessageDigest.getInstance("SHA-256");
        var buffer = new byte[1024];
        int read;
        while ((read = inputStream.read(buffer)) != -1) {
            md.update(buffer, 0, read);
        }
        return md.digest();
    }

    public static void generateRandom(byte[] data){
        RANDOM.nextBytes(data);
    }
}
