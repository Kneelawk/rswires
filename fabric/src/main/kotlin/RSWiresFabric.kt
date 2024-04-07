package net.dblsaiko.rswires.fabric

import net.dblsaiko.hctm.fabric.init.BlockEntityTypeRegistryFabric
import net.dblsaiko.hctm.fabric.init.BlockRegistryFabric
import net.dblsaiko.hctm.fabric.init.ItemGroupRegistryFabric
import net.dblsaiko.hctm.fabric.init.ItemRegistryFabric
import net.dblsaiko.rswires.MOD_ID
import net.dblsaiko.rswires.RSWires
import net.dblsaiko.rswires.common.block.RedstoneWireUtils
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.server.world.ServerWorld

object RSWiresFabric : ModInitializer {
    val blocks = BlockRegistryFabric(MOD_ID)
    val blockEntityTypes = BlockEntityTypeRegistryFabric(MOD_ID)
    val items = ItemRegistryFabric(MOD_ID)
    val itemGroups = ItemGroupRegistryFabric(MOD_ID)

    override fun onInitialize() {
        RSWires.initialize()

        blocks.register()
        blockEntityTypes.register()
        items.register()
        itemGroups.register()
        RSWires.initializeWireNet()

        ServerTickEvents.END_WORLD_TICK.register(ServerTickEvents.EndWorldTick {
            if (it is ServerWorld) {
                RedstoneWireUtils.flushUpdates(it)
            }
        })
    }
}