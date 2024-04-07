package net.dblsaiko.rswires.client

import net.dblsaiko.hctm.client.render.model.UnbakedWireModel
import net.dblsaiko.rswires.MOD_ID
import net.dblsaiko.rswires.RSWires
import net.minecraft.client.render.model.UnbakedModel
import net.minecraft.client.util.ModelIdentifier
import net.minecraft.registry.Registries
import net.minecraft.util.DyeColor
import net.minecraft.util.Identifier

object RSWiresClient {
    private val factory = RSWiresClientPlatform.INSTANCE.wireModelFactory
    
    private val redAlloyOffModel = UnbakedWireModel(factory, Identifier(MOD_ID, "block/red_alloy_wire/off"), 0.125f, 0.125f, 32.0f)
    private val redAlloyOnModel = UnbakedWireModel(factory, Identifier(MOD_ID, "block/red_alloy_wire/on"), 0.125f, 0.125f, 32.0f)

    private val insulatedWireOffModel = DyeColor.entries.associateWith { UnbakedWireModel(factory, Identifier(MOD_ID, "block/insulated_wire/${it.getName()}/off"), 0.25f, 0.1875f, 32.0f) }
    private val insulatedWireOnModel = DyeColor.entries.associateWith { UnbakedWireModel(factory, Identifier(MOD_ID, "block/insulated_wire/${it.getName()}/on"), 0.25f, 0.1875f, 32.0f) }

    private val colorBundledCableModel = DyeColor.entries.associateWith { UnbakedWireModel(factory, Identifier(MOD_ID, "block/bundled_cable/${it.getName()}"), 0.375f, 0.25f, 32.0f) }
    private val plainBundledCableModel = UnbakedWireModel(factory, Identifier(MOD_ID, "block/bundled_cable/none"), 0.375f, 0.25f, 32.0f)
    
    private val insulatedWires by lazy { RSWires.blocks.insulatedWires.values.asSequence().map(Registries.BLOCK::getId).toSet() }
    private val bundledCables by lazy { RSWires.blocks.coloredBundledCables.values.asSequence().map(Registries.BLOCK::getId).toSet() }
    
    private val allIds by lazy {
        insulatedWires + bundledCables + Registries.BLOCK.getId(RSWires.blocks.redAlloyWire) + Registries.BLOCK.getId(RSWires.blocks.uncoloredBundledCable)
    }
    
    @JvmStatic
    fun getUnbakedModel(modelId: Identifier): UnbakedModel? {
        if (modelId is ModelIdentifier) {
            // do efficient check before slow processing, because this will be called *a lot*
            val id = Identifier(modelId.namespace, modelId.path)
            if (id in allIds) {
                val props = modelId.variant.split(",")
                return when (id) {
                    Registries.BLOCK.getId(RSWires.blocks.redAlloyWire) -> {
                        if ("powered=false" in props) redAlloyOffModel
                        else redAlloyOnModel
                    }
                    in insulatedWires -> {
                        val (color, _) = RSWires.blocks.insulatedWires.entries.first { (_, block) ->
                            id == Registries.BLOCK.getId(block)
                        }
                        if ("powered=false" in props) insulatedWireOffModel.getValue(color)
                        else insulatedWireOnModel.getValue(color)
                    }
                    Registries.BLOCK.getId(RSWires.blocks.uncoloredBundledCable) -> {
                        plainBundledCableModel
                    }
                    in bundledCables -> {
                        val (color, _) = RSWires.blocks.coloredBundledCables.entries.first { (_, block) ->
                            id == Registries.BLOCK.getId(block)
                        }
                        colorBundledCableModel.getValue(color)
                    }
                    else -> null
                }
            } else {
                return null
            }
        } else {
            return null
        }
    }

}