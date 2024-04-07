package net.dblsaiko.rswires.common.init

import net.dblsaiko.hctm.common.block.BaseWireBlockEntity
import net.dblsaiko.rswires.RSWiresPlatform

class BlockEntityTypes {
    private val reg = RSWiresPlatform.INSTANCE.blockEntityTypes

    val redAlloyWire by this.reg.create("red_alloy_wire", ::BaseWireBlockEntity)
    val insulatedWire by this.reg.create("insulated_wire", ::BaseWireBlockEntity)
    val bundledCable by this.reg.create("bundled_cable", ::BaseWireBlockEntity)
}
