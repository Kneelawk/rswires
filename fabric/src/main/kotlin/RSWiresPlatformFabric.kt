package net.dblsaiko.rswires.fabric

import net.dblsaiko.hctm.init.BlockEntityTypeRegistry
import net.dblsaiko.hctm.init.BlockRegistry
import net.dblsaiko.hctm.init.ItemGroupRegistry
import net.dblsaiko.hctm.init.ItemRegistry
import net.dblsaiko.rswires.RSWiresPlatform

class RSWiresPlatformFabric : RSWiresPlatform {
    override val blocks: BlockRegistry
        get() = RSWiresFabric.blocks
    override val blockEntityTypes: BlockEntityTypeRegistry
        get() = RSWiresFabric.blockEntityTypes
    override val items: ItemRegistry
        get() = RSWiresFabric.items
    override val itemGroups: ItemGroupRegistry
        get() = RSWiresFabric.itemGroups
}