package com.mndk.scjdmc.util.function;

@FunctionalInterface
public interface ThrowableBiConsumer<P1, P2, E extends Throwable> {
    void accept(P1 p1, P2 p2) throws E;
}
