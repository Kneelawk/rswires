package net.dblsaiko.rswires.neoforge

import net.dblsaiko.hctm.init.BlockEntityTypeRegistry
import net.dblsaiko.hctm.init.BlockRegistry
import net.dblsaiko.hctm.init.ItemGroupRegistry
import net.dblsaiko.hctm.init.ItemRegistry
import net.dblsaiko.rswires.RSWiresPlatform

class RSWiresPlatformNeoForge : RSWiresPlatform {
    override val blocks: BlockRegistry
        get() = RSWiresNeoForge.blocks
    override val blockEntityTypes: BlockEntityTypeRegistry
        get() = RSWiresNeoForge.blockEntityTypes
    override val items: ItemRegistry
        get() = RSWiresNeoForge.items
    override val itemGroups: ItemGroupRegistry
        get() = RSWiresNeoForge.itemGroups
}