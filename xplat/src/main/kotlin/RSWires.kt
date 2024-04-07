package net.dblsaiko.rswires

import net.dblsaiko.hctm.common.wire.WIRE_NETWORK
import net.dblsaiko.rswires.common.block.BundledCablePartExt
import net.dblsaiko.rswires.common.block.InsulatedWirePartExt
import net.dblsaiko.rswires.common.block.RedAlloyWirePartExt
import net.dblsaiko.rswires.common.init.BlockEntityTypes
import net.dblsaiko.rswires.common.init.Blocks
import net.dblsaiko.rswires.common.init.ItemGroups
import net.dblsaiko.rswires.common.init.Items
import net.minecraft.util.Identifier
import org.apache.logging.log4j.LogManager

const val MOD_ID = "rswires"

fun id(path: String) = Identifier(MOD_ID, path)

object RSWires {
    internal var logger = LogManager.getLogger(MOD_ID)

    var wiresGivePower = true

    val blockEntityTypes = BlockEntityTypes()
    val blocks = Blocks()
    val items = Items(blocks)
    val itemGroups = ItemGroups(blocks, items)

    fun initialize() {
        // run static initializers
    }
    
    fun initializeWireNet() {
        WIRE_NETWORK.addNodeTypes(RedAlloyWirePartExt.TYPE)
        WIRE_NETWORK.addNodeTypes(InsulatedWirePartExt.TYPE)
        WIRE_NETWORK.addNodeTypes(BundledCablePartExt.TYPE)
    }
}