package net.cycastic.sigil.application.misc;

import an.awesome.pipelinr.Command;

public abstract class OncePerRequestMiddleware implements Command.Middleware {
    private static class ThreadData {
        private boolean requestInProgress;
    }

    private final ThreadLocal<ThreadData> THREAD_DATA = ThreadLocal.withInitial(ThreadData::new);

    protected abstract <R, C extends Command<R>> R invokeInternal(C command, Next<R> next);

    public <R, C extends Command<R>> R invoke(C command, Next<R> next){
        var threadData = THREAD_DATA.get();
        if (threadData.requestInProgress){
            return next.invoke();
        }

        try {
            threadData.requestInProgress = true;
            return invokeInternal(command, next);
        } finally {
            threadData.requestInProgress = false;
        }
    }
}
