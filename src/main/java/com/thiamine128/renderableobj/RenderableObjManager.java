package com.thiamine128.renderableobj;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.client.model.geometry.StandaloneGeometryBakingContext;
import net.minecraftforge.client.model.obj.ObjTokenizer;
import net.minecraftforge.client.model.renderable.CompositeRenderable;

import java.io.FileNotFoundException;
import java.util.Map;

public class RenderableObjManager extends SimpleJsonResourceReloadListener {
    public static RenderableObjManager INSTANCE = new RenderableObjManager();

    private Map<RenderableObjSettings, RenderableObjModel> modelCache = Maps.newLinkedHashMap();
    private Map<ResourceLocation, CompositeRenderable> renderables = Maps.newLinkedHashMap();

    public RenderableObjManager() {
        super(RenderableObjSettings.GSON, "models/renderable");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager manager, ProfilerFiller profiler) {
        modelCache.clear();
        renderables.clear();
        for (var entry : map.entrySet()) {
            JsonObject jsonObject = entry.getValue().getAsJsonObject();
            if (!jsonObject.has("model"))
                throw new JsonParseException("Renderable OBJ Loader requires a 'model' key that points to a valid .OBJ model.");

            String modelLocation = jsonObject.get("model").getAsString();

            boolean automaticCulling = GsonHelper.getAsBoolean(jsonObject, "automatic_culling", true);
            boolean shadeQuads = GsonHelper.getAsBoolean(jsonObject, "shade_quads", true);
            boolean flipV = GsonHelper.getAsBoolean(jsonObject, "flip_v", false);
            boolean emissiveAmbient = GsonHelper.getAsBoolean(jsonObject, "emissive_ambient", true);
            String mtlOverride = GsonHelper.getAsString(jsonObject, "mtl_override", null);

            RenderableObjSettings settings = new RenderableObjSettings(new ResourceLocation(modelLocation), automaticCulling, shadeQuads, flipV, emissiveAmbient, mtlOverride);

            RenderableObjModel model = loadObj(settings, manager);

            renderables.put(entry.getKey(), model.bakeRenderable(StandaloneGeometryBakingContext.create(entry.getKey())));
        }
    }

    private RenderableObjModel loadObj(RenderableObjSettings settings, ResourceManager manager) {
        return modelCache.computeIfAbsent(settings, (data) -> {
            Resource resource = manager.getResource(settings.modelLocation()).orElseThrow();
            try (ObjTokenizer tokenizer = new ObjTokenizer(resource.open()))
            {
                return RenderableObjModel.parse(tokenizer, settings);
            } catch (FileNotFoundException e)
            {
                throw new RuntimeException("Could not find OBJ model", e);
            } catch (Exception e)
            {
                throw new RuntimeException("Could not read OBJ model", e);
            }
        });
    }

    public CompositeRenderable getRenderable(ResourceLocation model) {
        return renderables.get(model);
    }
}
