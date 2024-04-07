package net.dblsaiko.rswires

import net.dblsaiko.hctm.init.BlockEntityTypeRegistry
import net.dblsaiko.hctm.init.BlockRegistry
import net.dblsaiko.hctm.init.ItemGroupRegistry
import net.dblsaiko.hctm.init.ItemRegistry

interface RSWiresPlatform {
    companion object {
        val INSTANCE: RSWiresPlatform = run {
            var instance: RSWiresPlatform? = null

            try {
                instance = Class.forName("net.dblsaiko.rswires.fabric.RSWiresPlatformFabric").getConstructor()
                    .newInstance() as RSWiresPlatform
            } catch (_: Exception) {
            }

            if (instance == null) {
                try {
                    instance = Class.forName("net.dblsaiko.rswires.neoforge.RSWiresPlatformNeoForge").getConstructor()
                        .newInstance() as RSWiresPlatform
                } catch (_: Exception) {
                }
            }

            if (instance == null) {
                throw RuntimeException("Unable to obtain RSWiresPlatform instance")
            }

            instance
        }
    }
    
    val blocks: BlockRegistry
    val blockEntityTypes: BlockEntityTypeRegistry
    val items: ItemRegistry
    val itemGroups: ItemGroupRegistry
}