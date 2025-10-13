package net.cycastic.sigil.service.job;

public interface JobScheduler {
    <T extends C, C extends BackgroundJob> void defer(T data, Class<C> klass);

    default <T extends C, C extends BackgroundJob> void deferInfallible(T data, Class<C> klass){
        defer(data, klass);
    }

    default <T extends BackgroundJob> void defer(T data){
        defer((BackgroundJob) data, (Class) data.getClass());
    }

    default <T extends BackgroundJob> void deferInfallible(T data){
        deferInfallible((BackgroundJob) data, (Class) data.getClass());
    }
}
