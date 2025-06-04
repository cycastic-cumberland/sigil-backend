package net.cycastic.portfoliotoolkit.service;

public interface PasswordHasher {
    String hash(String password);
    boolean verify(String input, String hashedPassword);
}
