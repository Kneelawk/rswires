package net.dblsaiko.rswires.neoforge.client

import net.dblsaiko.hctm.client.render.model.WireModelFactory
import net.dblsaiko.hctm.neoforge.client.render.model.NeoForgeWireModelFactory
import net.dblsaiko.rswires.client.RSWiresClientPlatform

class RSWiresClientPlatformNeoForge : RSWiresClientPlatform {
    override val wireModelFactory = NeoForgeWireModelFactory()
}