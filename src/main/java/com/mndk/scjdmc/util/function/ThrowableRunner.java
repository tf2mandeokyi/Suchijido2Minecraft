package com.mndk.scjdmc.util.function;

@FunctionalInterface
public interface ThrowableRunner<E extends Throwable> {
    void run() throws E;
}
