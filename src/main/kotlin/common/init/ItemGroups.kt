package net.dblsaiko.rswires.common.init

import net.dblsaiko.hctm.common.util.ext.makeStack
import net.dblsaiko.hctm.fabric.init.ItemGroupRegistryFabric
import net.dblsaiko.rswires.MOD_ID
import net.dblsaiko.rswires.RSWires
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup
import net.minecraft.text.Text

class ItemGroups(blocks: Blocks, items: Items) {
    private val reg = ItemGroupRegistryFabric(MOD_ID)
    
    val all by reg.create("all") {
        FabricItemGroup.builder()
            .displayName(Text.translatable("itemGroup.rswires.all"))
            .icon { RSWires.items.redAlloyWire.makeStack() }
            .entries { _, entries ->
                entries.add(items.redAlloyWire)
                items.insulatedWires.values.forEach(entries::add)
                entries.add(items.uncoloredBundledCable)
                items.coloredBundledCables.values.forEach(entries::add)

                entries.add(items.redAlloyCompound)
                entries.add(items.redAlloyIngot)
            }
            .build()
    }

    fun register() {
        reg.register()
    }
}