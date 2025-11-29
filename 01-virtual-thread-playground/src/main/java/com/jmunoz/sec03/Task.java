package com.jmunoz.sec03;

import com.jmunoz.util.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Task {

    private static final Logger log = LoggerFactory.getLogger(Task.class);

    public static void cpuIntensive(int i) {
        // Descomentar los logs para ver el tiempo que lleva una tarea individual.
//        log.info("starting CPU task. Thread Info: {}", Thread.currentThread());
        var timenTaken = CommonUtils.timer(() -> findFib(i));
//        log.info("ending CPU task. Time taken: {} ms", timenTaken);
    }

    // Algoritmo 2^N - hecho de esta forma intencionalmente para simular tareas de CPU intensivas.
    public static long findFib(long input) {
        if (input < 2)
            return input;
        return findFib(input - 1) + findFib(input - 2);
    }
}
