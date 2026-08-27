package com.jeremyseq.events;

import java.util.ArrayList;
import java.util.List;

public class ClientTickHandler {
    private static final List<Runnable> LISTENERS = new ArrayList<>();

    public static void register(Runnable listener) {
        LISTENERS.add(listener);
    }

    public static void tick() {
        for (Runnable listener : LISTENERS) {
            listener.run();
        }
    }
}