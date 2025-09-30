package net.cycastic.sigil.service.job;

public interface JobActivator {
    void process(BackgroundJobDetails job);
}
