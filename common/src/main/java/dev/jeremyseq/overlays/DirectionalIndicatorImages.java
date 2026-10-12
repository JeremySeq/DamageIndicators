package dev.jeremyseq.overlays;

import dev.jeremyseq.DamageIndicators;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class DirectionalIndicatorImages {

    private static final String FOLDER = "textures/directional_indicators";

    private static Map<String, Identifier> IMAGES = new HashMap<>();

    public static void reload(ResourceManager resourceManager) {
        Map<String, Identifier> newMap = new HashMap<>();

        Map<Identifier, ?> found = resourceManager.listResources(
                FOLDER,
                loc -> loc.getPath().endsWith(".png")
        );

        for (Identifier loc : found.keySet()) {
            String path = loc.getPath();
            String fileName = path.substring(path.lastIndexOf('/') + 1);
            String key = fileName.substring(0, fileName.length() - ".png".length());

            try {
                Resource resource = resourceManager.getResource(loc).orElseThrow();
                try (InputStream stream = resource.open()) {
                    NativeImage image = NativeImage.read(stream);
                    image.close();
                }
            } catch (IOException e) {
                DamageIndicators.LOGGER.warn("Failed to read directional indicator image: {}", loc, e);
            }

            newMap.put(key, loc);
        }

        IMAGES = newMap;
    }

    public static Identifier get(String name) {
        return IMAGES.get(name);
    }

    public static Map<String, Identifier> getAll() {
        return IMAGES;
    }
}