package net.cycastic.sigil.domain;

import lombok.SneakyThrows;

import java.io.InputStream;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;

public class SlimCryptographicUtilities {
    private static final PSSParameterSpec STANDARD_PSS_SPEC = new PSSParameterSpec(
            "SHA-256",
            "MGF1",
            MGF1ParameterSpec.SHA256,
            32,
            PSSParameterSpec.TRAILER_FIELD_BC
    );
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

    @SneakyThrows
    public static byte[] digestSha256(byte[]... bytesVaArgs){
        var md = MessageDigest.getInstance("SHA-256");
        for (var bytes : bytesVaArgs){
            md.update(bytes);
        }

        return md.digest();
    }

    public static void generateRandom(byte[] data){
        RANDOM.nextBytes(data);
    }

    public static AlgorithmParameterSpec getStandardPssSpec(){
        return STANDARD_PSS_SPEC;
    }
}
