package net.dblsaiko.rswires.common.init

import net.dblsaiko.hctm.common.util.flatten
import net.dblsaiko.rswires.RSWiresPlatform
import net.dblsaiko.rswires.common.block.BundledCableBlock
import net.dblsaiko.rswires.common.block.InsulatedWireBlock
import net.dblsaiko.rswires.common.block.RedAlloyWireBlock
import net.minecraft.block.AbstractBlock
import net.minecraft.block.MapColor
import net.minecraft.block.enums.Instrument
import net.minecraft.util.DyeColor

class Blocks {
    private val reg = RSWiresPlatform.INSTANCE.blocks

    private val wireSettings = AbstractBlock.Settings.create()
        .mapColor(MapColor.RED)
        .noCollision()
        .strength(0.05f, 0.05f)
        .instrument(Instrument.BASEDRUM)

    val redAlloyWireObject = this.reg.create("red_alloy_wire") { RedAlloyWireBlock(this.wireSettings) }
    val insulatedWireObjects = DyeColor.entries.associateWith {
        this.reg.create("${it.getName()}_insulated_wire") { InsulatedWireBlock(this.wireSettings, it) }
    }
    val uncoloredBundledCableObject =
        this.reg.create("bundled_cable") { BundledCableBlock(this.wireSettings, null) }
    val coloredBundledCableObjects = DyeColor.entries.associateWith {
        this.reg.create("${it.getName()}_bundled_cable") { BundledCableBlock(this.wireSettings, it) }
    }

    val redAlloyWire by this.redAlloyWireObject
    val insulatedWires by this.insulatedWireObjects.flatten()
    val uncoloredBundledCable by this.uncoloredBundledCableObject
    val coloredBundledCables by this.coloredBundledCableObjects.flatten()

    fun getInsulatedWire(color: DyeColor): InsulatedWireBlock {
        return this.insulatedWires.getValue(color)
    }

    fun getBundledCable(color: DyeColor): BundledCableBlock {
        return this.coloredBundledCables.getValue(color)
    }
}