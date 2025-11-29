package com.jmunoz.util;

import java.time.Duration;

public class CommonUtils {

    // Vamos a usar mucho Thread.sleep() y no quiero tener que hacer el catch de InterruptedException cada vez.
    public static void sleep(Duration duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    // Cuando tarda en ejecutarse el runnable.
    public static long timer(Runnable runnable) {
        var start = System.currentTimeMillis();
        runnable.run();
        var end = System.currentTimeMillis();
        return (end - start);
    }
}
