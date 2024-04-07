package net.dblsaiko.rswires.mixin;

import net.dblsaiko.rswires.client.RSWiresClient;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.util.Identifier;

@Mixin(ModelLoader.class)
public abstract class ModelLoaderMixin {
    @Shadow protected abstract void putModel(Identifier id, UnbakedModel unbakedModel);

    @Inject(method = "loadModel", at = @At("HEAD"), cancellable = true)
    private void onLoadModel(Identifier id, CallbackInfo ci) {
        UnbakedModel model = RSWiresClient.getUnbakedModel(id);
        if (model != null) {
            putModel(id, model);
            ci.cancel();
        }
    }
}
