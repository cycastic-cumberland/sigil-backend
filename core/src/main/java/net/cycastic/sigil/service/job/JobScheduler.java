package net.cycastic.sigil.service.job;

public interface JobScheduler {
    <T extends C, C> void defer(T data, Class<C> klass);

    default <T extends C, C> void deferInfallible(T data, Class<C> klass){
        defer(data, klass);
    }

    default <T> void defer(T data){
        defer((Object) data, (Class) data.getClass());
    }

    default <T> void deferInfallible(T data){
        deferInfallible((Object) data, (Class) data.getClass());
    }
}
