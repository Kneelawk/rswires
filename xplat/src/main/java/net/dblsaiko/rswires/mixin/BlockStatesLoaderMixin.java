package net.dblsaiko.rswires.mixin;

import java.util.function.BiConsumer;

import net.dblsaiko.rswires.client.RSWiresClient;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.block.BlockModels;
import net.minecraft.client.render.model.BlockStatesLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.state.StateManager;
import net.minecraft.util.Identifier;

@Mixin(BlockStatesLoader.class)
public class BlockStatesLoaderMixin {
    @Shadow
    @Final
    private BiConsumer<ModelIdentifier, UnbakedModel> onLoad;

    @Inject(method = "loadBlockStates", at = @At("HEAD"), cancellable = true)
    private void onLoadBlockStates(Identifier id, StateManager<Block, BlockState> stateManager, CallbackInfo ci) {
        if (RSWiresClient.isManaged(id)) {
            for (BlockState state : stateManager.getStates()) {
                ModelIdentifier identifier = BlockModels.getModelId(state);
                UnbakedModel model = RSWiresClient.getUnbakedModel(identifier);
                if (model != null) {
                    onLoad.accept(identifier, model);
                }
            }

            ci.cancel();
        }
    }
}
