package net.dblsaiko.rswires.neoforge

import net.dblsaiko.hctm.neoforge.init.BlockEntityTypeRegistryNeoForge
import net.dblsaiko.hctm.neoforge.init.BlockRegistryNeoForge
import net.dblsaiko.hctm.neoforge.init.ItemGroupRegistryNeoForge
import net.dblsaiko.hctm.neoforge.init.ItemRegistryNeoForge
import net.dblsaiko.rswires.MOD_ID
import net.dblsaiko.rswires.RSWires
import net.dblsaiko.rswires.common.block.RedstoneWireUtils
import net.minecraft.server.world.ServerWorld
import net.neoforged.bus.api.IEventBus
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.Mod
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.event.TickEvent

@Mod(MOD_ID)
class RSWiresNeoForge(modBus: IEventBus) {
    companion object {
        val blocks = BlockRegistryNeoForge(MOD_ID)
        val blockEntityTypes = BlockEntityTypeRegistryNeoForge(MOD_ID)
        val items = ItemRegistryNeoForge(MOD_ID)
        val itemGroups = ItemGroupRegistryNeoForge(MOD_ID)
    }

    init {
        RSWires.initialize()

        blocks.register(modBus)
        blockEntityTypes.register(modBus)
        items.register(modBus)
        itemGroups.register(modBus)

        modBus.register(ModEvents)
        NeoForge.EVENT_BUS.register(NeoForgeEvents)
    }

    object ModEvents {
        @SubscribeEvent
        private fun onSetup(event: FMLCommonSetupEvent) {
            event.enqueueWork {
                RSWires.initializeWireNet()
            }
        }
    }

    object NeoForgeEvents {
        @SubscribeEvent
        private fun onServerWorldTick(event: TickEvent.LevelTickEvent) {
            val world = event.level
            if (event.phase == TickEvent.Phase.END && world is ServerWorld) {
                RedstoneWireUtils.flushUpdates(world)
            }
        }
    }
}