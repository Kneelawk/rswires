package net.dblsaiko.rswires.mixin;

import net.dblsaiko.rswires.client.RSWiresClient;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.util.Identifier;

@Mixin(ModelLoader.class)
public abstract class ModelLoaderMixin {
    @Shadow
    protected abstract void add(ModelIdentifier id, UnbakedModel model);

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
