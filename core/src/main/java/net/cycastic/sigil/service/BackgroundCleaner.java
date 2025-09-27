package net.cycastic.sigil.service;

public interface BackgroundCleaner {
    default String getCleanerId(){
        return getClass().getName();
    }
    void clean();
}
