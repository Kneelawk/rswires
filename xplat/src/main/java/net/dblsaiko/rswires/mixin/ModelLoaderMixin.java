package net.dblsaiko.rswires.mixin;

import java.util.List;
import java.util.Map;

import net.dblsaiko.rswires.client.RSWiresClient;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.BlockState;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.render.block.BlockModels;
import net.minecraft.client.render.model.BlockStatesLoader;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

@Mixin(ModelLoader.class)
public abstract class ModelLoaderMixin {
    @Shadow
    protected abstract void add(ModelIdentifier id, UnbakedModel model);

    @Inject(method = "<init>",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V",
            ordinal = 1))
    private void onInit(BlockColors blockColors, Profiler profiler, Map<Identifier, JsonUnbakedModel> jsonUnbakedModels,
                        Map<Identifier, List<BlockStatesLoader.SourceTrackedData>> blockStates, CallbackInfo ci) {
        profiler.swap("rswires");
        for (BlockState state : RSWiresClient.getAllStates()) {
            ModelIdentifier mi = BlockModels.getModelId(state);
            UnbakedModel model = RSWiresClient.getUnbakedModel(mi);
            if (model != null) {
                add(mi, model);
            }
        }
    }

    @Inject(method = "loadInventoryVariantItemModel", at = @At("HEAD"), cancellable = true)
    private void onLoadInventoryVariantItemModel(Identifier id, CallbackInfo ci) {
        ModelIdentifier mi = ModelIdentifier.ofInventoryVariant(id);
        UnbakedModel model = RSWiresClient.getUnbakedModel(mi);
        if (model != null) {
            add(mi, model);
            ci.cancel();
        }
    }
}
