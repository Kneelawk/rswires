package net.dblsaiko.rswires.common.init

import net.dblsaiko.hctm.common.util.ext.makeStack
import net.dblsaiko.rswires.RSWires
import net.dblsaiko.rswires.RSWiresPlatform
import net.minecraft.item.ItemGroup
import net.minecraft.text.Text

class ItemGroups(blocks: Blocks, items: Items) {
    private val reg = RSWiresPlatform.INSTANCE.itemGroups

    val all by reg.create("all") {
        ItemGroup.create(ItemGroup.Row.TOP, 0)
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
}