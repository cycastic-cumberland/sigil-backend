package net.cycastic.sigil.domain;

import lombok.Getter;

import java.util.List;

@Getter
public class AggregatedException extends Exception {
    private final List<Throwable> causes;

    private AggregatedException(List<Throwable> causes) {
        super("Multiple exceptions occurred: " + causes.size());
        this.causes = causes;
    }

    public static void doThrow(List<Throwable> causes) throws Throwable {
        if (causes.isEmpty()){
            throw new IllegalArgumentException("causes");
        }
        if (causes.size() == 1){
            throw causes.getFirst();
        }

        throw new AggregatedException(causes);
    }
}
