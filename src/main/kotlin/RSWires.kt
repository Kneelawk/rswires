package net.dblsaiko.rswires

import net.dblsaiko.hctm.common.wire.WIRE_NETWORK
import net.dblsaiko.rswires.common.block.RedAlloyWirePartExt
import net.dblsaiko.rswires.common.block.RedstoneWireUtils
import net.dblsaiko.rswires.common.init.BlockEntityTypes
import net.dblsaiko.rswires.common.init.Blocks
import net.dblsaiko.rswires.common.init.ItemGroups
import net.dblsaiko.rswires.common.init.Items
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import org.apache.logging.log4j.LogManager

const val MOD_ID = "rswires"

fun id(path: String) = Identifier(MOD_ID, path)

object RSWires : ModInitializer {
    internal var logger = LogManager.getLogger(MOD_ID)

    var wiresGivePower = true

    val blockEntityTypes = BlockEntityTypes()
    val blocks = Blocks()
    val items = Items(blocks)
    val itemGroups = ItemGroups(blocks, items)

    override fun onInitialize() {
        this.blockEntityTypes.register()
        this.blocks.register()
        this.items.register()
        this.itemGroups.register()

        WIRE_NETWORK.addNodeTypes(RedAlloyWirePartExt.TYPE)

        ServerTickEvents.END_WORLD_TICK.register(ServerTickEvents.EndWorldTick {
            if (it is ServerWorld) {
                RedstoneWireUtils.flushUpdates(it)
            }
        })
    }
}