package com.mndk.scjdmc.util;

@FunctionalInterface
public interface ExceptionThrowableRunner<E extends Throwable> {
    void run() throws E;
}
