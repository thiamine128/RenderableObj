package com.thiamine128.renderableobj;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record RenderableObjSettings(@NotNull ResourceLocation modelLocation,
                                    boolean automaticCulling, boolean shadeQuads, boolean flipV,
                                    boolean emissiveAmbient, @Nullable String mtlOverride) {
    public static final Gson GSON = new GsonBuilder().registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer()).create();

}
