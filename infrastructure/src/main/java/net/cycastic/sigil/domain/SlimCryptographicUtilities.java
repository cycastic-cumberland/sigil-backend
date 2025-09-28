package net.cycastic.sigil.domain;

import lombok.SneakyThrows;

import java.security.MessageDigest;

public class SlimCryptographicUtilities {
    @SneakyThrows
    public static byte[] digestMd5(byte[] data){
        var md = MessageDigest.getInstance("MD5");
        return md.digest(data);
    }
}
