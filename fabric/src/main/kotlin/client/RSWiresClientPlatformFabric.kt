package net.dblsaiko.rswires.fabric.client

import net.dblsaiko.hctm.client.render.model.CacheKey
import net.dblsaiko.hctm.client.render.model.WireModelFactory
import net.dblsaiko.hctm.fabric.client.render.model.FabricWireModelFactory
import net.dblsaiko.hctm.fabric.client.render.model.FabricWireModelParts
import net.dblsaiko.rswires.client.RSWiresClientPlatform
import net.fabricmc.fabric.api.renderer.v1.RendererAccess
import java.util.concurrent.ConcurrentHashMap

class RSWiresClientPlatformFabric : RSWiresClientPlatform {
    override val wireModelFactory: WireModelFactory
    
    init {
        val r = RendererAccess.INSTANCE.renderer
            ?: throw RuntimeException("Unable to find FRAPI renderer. Wire rendering will not work.")
        val modelStore = ConcurrentHashMap<CacheKey, FabricWireModelParts>()
        
        wireModelFactory = FabricWireModelFactory(r, modelStore)
    }
}