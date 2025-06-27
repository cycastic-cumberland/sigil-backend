package net.cycastic.portfoliotoolkit.service;

public interface BackgroundCleaner {
    default String getCleanerId(){
        return getClass().getName();
    }
    void clean();
}
