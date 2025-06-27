package net.cycastic.portfoliotoolkit.service;

public interface Presigner {
    String getSignature(String data, String algorithm);

    boolean verifySignature(String data, String signature, String algorithm);

    String getDefaultAlgorithm();

    default boolean canSupport(String algorithm){
        return getDefaultAlgorithm().equals(algorithm);
    }
}
