package net.dblsaiko.rswires.common.init

import net.dblsaiko.hctm.common.block.BaseWireItem
import net.dblsaiko.hctm.common.util.flatten
import net.dblsaiko.rswires.RSWires
import net.dblsaiko.rswires.RSWiresPlatform
import net.minecraft.item.Item
import net.minecraft.item.Item.Settings

class Items(blocks: Blocks) {
    private val reg = RSWiresPlatform.INSTANCE.items

    val redAlloyWire by this.reg.create("red_alloy_wire") { BaseWireItem(blocks.redAlloyWire, Settings()) }
    val insulatedWires by RSWires.blocks.insulatedWireObjects.mapValues { (color, block) ->
        this.reg.create("${color.getName()}_insulated_wire") { BaseWireItem(block.get(), Settings()) }
    }.flatten()
    val uncoloredBundledCable by this.reg.create("bundled_cable") {
        BaseWireItem(blocks.uncoloredBundledCable, Settings())
    }
    val coloredBundledCables by RSWires.blocks.coloredBundledCableObjects.mapValues { (color, block) ->
        this.reg.create("${color.getName()}_bundled_cable") { BaseWireItem(block.get(), Settings()) }
    }.flatten()

    val redAlloyCompound by this.reg.create("red_alloy_compound") { Item(Settings()) }
    val redAlloyIngot by this.reg.create("red_alloy_ingot") { Item(Settings()) }
}