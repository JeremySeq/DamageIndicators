package dev.jeremyseq.fabric;

import dev.jeremyseq.events.ClientTickHandler;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class FabricClientTickHandler {
    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> ClientTickHandler.tick());
    }
}
