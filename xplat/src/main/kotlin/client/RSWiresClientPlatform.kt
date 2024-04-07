package net.dblsaiko.rswires.client

import net.dblsaiko.hctm.client.render.model.WireModelFactory

interface RSWiresClientPlatform {
    companion object {
        val INSTANCE: RSWiresClientPlatform = run {
            var instance: RSWiresClientPlatform? = null

            try {
                instance =
                    Class.forName("net.dblsaiko.rswires.fabric.client.RSWiresClientPlatformFabric").getConstructor()
                        .newInstance() as RSWiresClientPlatform
            } catch (_: Exception) {
            }

            if (instance == null) {
                try {
                    instance = Class.forName("net.dblsaiko.rswires.neoforge.client.RSWiresClientPlatformNeoForge")
                        .getConstructor()
                        .newInstance() as RSWiresClientPlatform
                } catch (_: Exception) {
                }
            }

            if (instance == null) {
                throw RuntimeException("Unable to obtain RSWiresPlatform instance")
            }

            instance
        }
    }

    val wireModelFactory: WireModelFactory
}