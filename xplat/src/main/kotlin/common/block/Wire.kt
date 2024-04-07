package net.dblsaiko.rswires.common.block

import com.kneelawk.graphlib.api.graph.BlockGraph
import com.kneelawk.graphlib.api.graph.NodeHolder
import com.kneelawk.graphlib.api.graph.user.BlockNode
import com.kneelawk.graphlib.api.graph.user.BlockNodeType
import com.kneelawk.graphlib.api.graph.user.SidedBlockNode
import com.kneelawk.graphlib.api.util.HalfLink
import com.kneelawk.graphlib.api.wire.SidedWireBlockNode
import com.kneelawk.graphlib.api.wire.WireConnectionDiscoverers
import com.kneelawk.graphlib.api.wire.WireConnectionFilter
import net.dblsaiko.hctm.block.BlockBundledCableIo
import net.dblsaiko.hctm.common.block.BaseWireBlock
import net.dblsaiko.hctm.common.block.BaseWireBlockEntity
import net.dblsaiko.hctm.common.block.BaseWireProperties
import net.dblsaiko.hctm.common.block.ConnectionType
import net.dblsaiko.hctm.common.block.SingleBaseWireBlock
import net.dblsaiko.hctm.common.block.WireUtils
import net.dblsaiko.hctm.common.wire.NetNode
import net.dblsaiko.hctm.common.wire.SimpleBaseWireDecoder
import net.dblsaiko.hctm.common.wire.WIRE_NETWORK
import net.dblsaiko.rswires.RSWires
import net.dblsaiko.rswires.id
import net.minecraft.block.AbstractBlock
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.nbt.NbtByte
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.server.world.ServerWorld
import net.minecraft.state.StateManager.Builder
import net.minecraft.state.property.Properties
import net.minecraft.util.DyeColor
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.BlockView
import net.minecraft.world.World
import net.minecraft.world.dimension.DimensionType
import kotlin.experimental.or
import kotlin.streams.asSequence

abstract class BaseRedstoneWireBlock(settings: AbstractBlock.Settings, height: Float) : SingleBaseWireBlock(settings, height) {

    init {
        defaultState = defaultState.with(WireProperties.POWERED, false)
    }

    private fun isCorrectBlock(state: BlockState) = state.block == RSWires.blocks.redAlloyWire

    override fun appendProperties(b: Builder<Block, BlockState>) {
        super.appendProperties(b)
        b.add(WireProperties.POWERED)
    }

    override fun emitsRedstonePower(state: BlockState?): Boolean {
        return true
    }

    override fun neighborUpdate(state: BlockState, world: World, pos: BlockPos, block: Block, neighborPos: BlockPos, moved: Boolean) {
        if (world is ServerWorld) {
            WireUtils.updateClient(world, pos) // redstone connections
            RSWires.wiresGivePower = false
            if (isReceivingPower(state, world, pos) != state[WireProperties.POWERED]) {
                RedstoneWireUtils.scheduleUpdate(world, pos)
            }
            RSWires.wiresGivePower = true
        }
    }

    override fun mustConnectInternally() = true

    abstract fun isReceivingPower(state: BlockState, world: World, pos: BlockPos): Boolean

    override fun overrideConnection(world: World, pos: BlockPos, state: BlockState, side: Direction, edge: Direction, current: ConnectionType?): ConnectionType? {
        if (current == null) {
            val blockState = world.getBlockState(pos.offset(edge))
            if (blockState.block !is BaseWireBlock && blockState.emitsRedstonePower()) {
                return ConnectionType.EXTERNAL
            }
        }
        return super.overrideConnection(world, pos, state, side, edge, current)
    }

}

class RedAlloyWireBlock(settings: AbstractBlock.Settings) : BaseRedstoneWireBlock(settings, 2 / 16f) {

    override fun getStrongRedstonePower(state: BlockState, view: BlockView, pos: BlockPos, facing: Direction): Int {
        // Fix for comparator side input which only respects strong power
        if (view.getBlockState(pos.offset(facing.opposite)).block == Blocks.COMPARATOR) {
            return state.getWeakRedstonePower(view, pos, facing)
        }

        return if (
            RSWires.wiresGivePower &&
            state[WireProperties.POWERED] &&
            state[BaseWireProperties.PLACED_WIRES[facing.opposite]]
        ) 15 else 0
    }

    override fun getWeakRedstonePower(state: BlockState, view: BlockView, pos: BlockPos, facing: Direction): Int {
        return if (
            RSWires.wiresGivePower &&
            state[WireProperties.POWERED] &&
            (BaseWireProperties.PLACED_WIRES - facing.opposite).any { state[it.value] }
        ) 15 else 0
    }

    override fun createPartExtFromSide(side: Direction) = RedAlloyWirePartExt(side)

    override fun createBlockEntity(pos: BlockPos, state: BlockState) = BaseWireBlockEntity(RSWires.blockEntityTypes.redAlloyWire, pos, state)

    override fun isReceivingPower(state: BlockState, world: World, pos: BlockPos) =
        RedstoneWireUtils.isReceivingPower(state, world, pos, true)

}

class InsulatedWireBlock(settings: AbstractBlock.Settings, val color: DyeColor) : BaseRedstoneWireBlock(settings, 3 / 16f) {

    override fun createPartExtFromSide(side: Direction) = InsulatedWirePartExt(side, color)

    override fun createBlockEntity(pos: BlockPos, state: BlockState) = BaseWireBlockEntity(RSWires.blockEntityTypes.insulatedWire, pos, state)

    override fun getStrongRedstonePower(state: BlockState, view: BlockView, pos: BlockPos, facing: Direction): Int {
        // Fix for comparator side input which only respects strong power
        if (view.getBlockState(pos.offset(facing.opposite)).block == Blocks.COMPARATOR) {
            return state.getWeakRedstonePower(view, pos, facing)
        }

        return 0
    }

    override fun getWeakRedstonePower(state: BlockState, view: BlockView, pos: BlockPos, facing: Direction): Int {
        return if (
            RSWires.wiresGivePower &&
            state[WireProperties.POWERED] &&
            (BaseWireProperties.PLACED_WIRES - facing.opposite).any { state[it.value] }
        ) 15 else 0
    }

    override fun isReceivingPower(state: BlockState, world: World, pos: BlockPos) =
        RedstoneWireUtils.isReceivingPower(state, world, pos, false)

}

class BundledCableBlock(settings: AbstractBlock.Settings, val color: DyeColor?) : BaseWireBlock(settings, 4 / 16f) {

    override fun createPartExtsFromSide(side: Direction): Set<BlockNode> {
        return DyeColor.values().map { BundledCablePartExt(side, color, it) }.toSet()
    }

    override fun neighborUpdate(state: BlockState, world: World, pos: BlockPos, block: Block, neighborPos: BlockPos, moved: Boolean) {
        if (world is ServerWorld) {
            WireUtils.updateClient(world, pos) // redstone connections
            RedstoneWireUtils.scheduleUpdate(world, pos)
        }
    }

    override fun overrideConnection(world: World, pos: BlockPos, state: BlockState, side: Direction, edge: Direction, current: ConnectionType?): ConnectionType? {
        if (current == null) {
            val blockState = world.getBlockState(pos.offset(edge))
            val block = blockState.block
            if (block !is BaseWireBlock && block is BlockBundledCableIo && block.canBundledConnectTo(blockState, world, pos.offset(edge), edge, side)) {
                return ConnectionType.EXTERNAL
            }
        }
        return super.overrideConnection(world, pos, state, side, edge, current)
    }

    override fun createBlockEntity(pos: BlockPos, state: BlockState) = BaseWireBlockEntity(RSWires.blockEntityTypes.bundledCable, pos, state)

}

data class RedAlloyWirePartExt(private val side: Direction) : SidedBlockNode, SidedWireBlockNode, PartRedstoneCarrier {
    companion object {
        val TYPE = BlockNodeType.of(id("red_alloy_wire"), SimpleBaseWireDecoder(::RedAlloyWirePartExt))
    }
    
    override val wireType = RedstoneWireType.RedAlloy

    override fun getType() = TYPE
    
    override fun getSide() = side

    private fun isCorrectBlock(state: BlockState) = state.block == RSWires.blocks.redAlloyWire

    override fun getState(world: World, self: NetNode): Boolean {
        val pos = self.blockPos
        val state = world.getBlockState(pos)
        return isCorrectBlock(state) && state[WireProperties.POWERED]
    }

    override fun setState(world: World, self: NetNode, state: Boolean) {
        val pos = self.blockPos
        var blockState = world.getBlockState(pos)

        if (!isCorrectBlock(blockState)) return

        blockState = blockState.with(WireProperties.POWERED, state)
        world.setBlockState(pos, blockState)

        // update neighbors 2 blocks away for strong redstone signal
        WireUtils.getOccupiedSides(blockState)
            .map { pos.offset(it) }
            .flatMap { Direction.values().map { d -> it.offset(d) } }
            .distinct()
            .minus(pos)
            .forEach { world.updateNeighbor(it, blockState.block, pos) }
    }

    override fun getInput(world: World, self: NetNode): Boolean {
        val pos = self.blockPos
        return RedstoneWireUtils.isReceivingPower(world.getBlockState(pos), world, pos, true)
    }

    override fun findConnections(self: NodeHolder<BlockNode>): MutableCollection<HalfLink> {
        return WireConnectionDiscoverers.sidedWireFindConnections(this, self, RedstoneCarrierFilter)
    }

    override fun canConnect(self: NodeHolder<BlockNode>, other: HalfLink): Boolean {
        return WireConnectionDiscoverers.sidedWireCanConnect(this, self, other, RedstoneCarrierFilter)
    }

    override fun onConnectionsChanged(self: NodeHolder<BlockNode>) {
        val world = self.blockWorld
        if (world is ServerWorld) {
            RedstoneWireUtils.scheduleUpdate(world, self.blockPos)
            WireUtils.updateClient(world, self.blockPos)
        }
    }

    override fun toTag(): NbtElement {
        return NbtByte.of(side.id.toByte())
    }
}

data class InsulatedWirePartExt(private val side: Direction, val color: DyeColor) : SidedBlockNode, SidedWireBlockNode, PartRedstoneCarrier {
    companion object {
        val TYPE = BlockNodeType.of(id("insulated_wire")) {
            nbt -> (nbt as? NbtCompound)?.let { 
                InsulatedWirePartExt(Direction.byId(it.getByte("side").toInt()), DyeColor.byId(it.getByte("color").toInt()))
            }
        }
    }
    
    override val wireType = RedstoneWireType.Colored(color)
    
    override fun getType() = TYPE

    override fun getSide() = side

    private fun isCorrectBlock(state: BlockState) = state.block in RSWires.blocks.insulatedWires.values

    override fun getState(world: World, self: NetNode): Boolean {
        val pos = self.blockPos
        val state = world.getBlockState(pos)
        return isCorrectBlock(state) && state[WireProperties.POWERED]
    }

    override fun setState(world: World, self: NetNode, state: Boolean) {
        val pos = self.blockPos
        val blockState = world.getBlockState(pos)
        if (!isCorrectBlock(blockState)) return
        world.setBlockState(pos, blockState.with(WireProperties.POWERED, state))
    }

    override fun getInput(world: World, self: NetNode): Boolean {
        val pos = self.blockPos
        val state = world.getBlockState(pos)

        if (!isCorrectBlock(state)) return false

        return RedstoneWireUtils.isReceivingPower(state, world, pos, false)
    }

    override fun findConnections(self: NodeHolder<BlockNode>): MutableCollection<HalfLink> {
        return WireConnectionDiscoverers.sidedWireFindConnections(this, self, RedstoneCarrierFilter)
    }

    override fun canConnect(self: NodeHolder<BlockNode>, other: HalfLink): Boolean {
        return WireConnectionDiscoverers.sidedWireCanConnect(this, self, other, RedstoneCarrierFilter)
    }

    override fun onConnectionsChanged(self: NodeHolder<BlockNode>) {
        val world = self.blockWorld
        if (world is ServerWorld) {
            RedstoneWireUtils.scheduleUpdate(world, self.blockPos)
            WireUtils.updateClient(world, self.blockPos)
        }
    }

    override fun toTag(): NbtElement {
        val nbt = NbtCompound()
        nbt.putByte("side", side.id.toByte())
        nbt.putByte("color", color.id.toByte())
        return nbt
    }
}

data class BundledCablePartExt(private val side: Direction, val color: DyeColor?, val inner: DyeColor) : SidedBlockNode, SidedWireBlockNode, PartRedstoneCarrier {
    companion object {
        val TYPE = BlockNodeType.of(id("bundled_cable")) {
            nbt -> (nbt as? NbtCompound)?.let { 
                BundledCablePartExt(
                    Direction.byId(it.getByte("side").toInt()), 
                    if (it.contains("color", NbtElement.BYTE_TYPE.toInt())) DyeColor.byId(it.getByte("color").toInt()) else null, 
                    DyeColor.byId(it.getByte("inner").toInt())
                ) 
            } 
        }
    }
    
    override val wireType = RedstoneWireType.Bundled(color, inner)

    override fun getType() = TYPE

    override fun getSide() = side

    fun isCorrectBlock(state: BlockState) = state.block == RSWires.blocks.uncoloredBundledCable ||
        state.block in RSWires.blocks.coloredBundledCables.values

    override fun getState(world: World, self: NetNode): Boolean {
        return false
    }

    override fun setState(world: World, self: NetNode, state: Boolean) {}

    override fun getInput(world: World, self: NetNode): Boolean {
        val pos = self.blockPos
        val state = world.getBlockState(pos)

        if (!isCorrectBlock(state)) return false

        return BundledCableUtils.getReceivedData(state, world, pos).toUInt() and (1u shl inner.id) != 0u
    }

    override fun findConnections(self: NodeHolder<BlockNode>): MutableCollection<HalfLink> {
        return WireConnectionDiscoverers.sidedWireFindConnections(this, self, RedstoneCarrierFilter)
    }

    override fun canConnect(self: NodeHolder<BlockNode>, other: HalfLink): Boolean {
        return WireConnectionDiscoverers.sidedWireCanConnect(this, self, other, RedstoneCarrierFilter)
    }

    override fun onConnectionsChanged(self: NodeHolder<BlockNode>) {
        val world = self.blockWorld
        if (world is ServerWorld) {
            RedstoneWireUtils.scheduleUpdate(world, self.blockPos)
            WireUtils.updateClient(world, self.blockPos)
        }
    }

    override fun toTag(): NbtElement {
        val nbt = NbtCompound()
        nbt.putByte("side", side.id.toByte())
        color?.let { nbt.putByte("color", it.id.toByte()) }
        nbt.putByte("inner", inner.id.toByte())
        return nbt
    }
}

interface PartRedstoneCarrier : BlockNode {
    val wireType: RedstoneWireType

    fun getState(world: World, self: NetNode): Boolean

    fun setState(world: World, self: NetNode, state: Boolean)

    fun getInput(world: World, self: NetNode): Boolean
}

sealed class RedstoneWireType {
    object RedAlloy : RedstoneWireType()
    data class Colored(val color: DyeColor) : RedstoneWireType()
    data class Bundled(val color: DyeColor?, val inner: DyeColor) : RedstoneWireType()

    fun canConnect(other: RedstoneWireType): Boolean {
        if (this == other) return true
        if (this == RedAlloy && other is Colored || this is Colored && other == RedAlloy) return true
        if (this is Colored && other is Bundled && other.inner == this.color || this is Bundled && other is Colored && this.inner == other.color) return true
        if (other is Bundled && this == Bundled(null, other.inner) || this is Bundled && other == Bundled(null, this.inner)) return true
        return false
    }
}

object RedstoneCarrierFilter : WireConnectionFilter {
    override fun accepts(self: BlockNode, other: BlockNode): Boolean {
        val d1 = self as? PartRedstoneCarrier ?: return false
        val d2 = other as? PartRedstoneCarrier ?: return false
        return d1.wireType.canConnect(d2.wireType)
    }
}

object WireProperties {
    val POWERED = Properties.POWERED
}

object RedstoneWireUtils {

    var scheduled = mapOf<DimensionType, Set<Long>>()

    fun scheduleUpdate(world: ServerWorld, pos: BlockPos) {
        scheduled += world.dimension to scheduled[world.dimension].orEmpty() + WIRE_NETWORK.getGraphWorld(world).getAllGraphsAt(pos).map { it.id }.toList()
    }

    fun flushUpdates(world: ServerWorld) {
        val wireNetworkState = WIRE_NETWORK.getGraphWorld(world)
        for (id in scheduled[world.dimension].orEmpty()) {
            val net = wireNetworkState.getGraph(id)
            if (net != null) updateState(world, net)
        }
        scheduled -= world.dimension
    }

    fun updateState(world: World, network: BlockGraph) {
        val isOn = try {
            RSWires.wiresGivePower = false
            network.nodes.asSequence().any { (it.node as PartRedstoneCarrier).getInput(world, it) }
        } finally {
            RSWires.wiresGivePower = true
        }
        for (node in network.nodes) {
            val ext = node.node as PartRedstoneCarrier
            ext.setState(world, node, isOn)
        }
    }

    fun isReceivingPower(state: BlockState, world: World, pos: BlockPos, receiveFromBottom: Boolean): Boolean {
        val sides = WireUtils.getOccupiedSides(state)
        val weakSides = Direction.values().filter { a -> sides.any { b -> b.axis != a.axis } }.distinct() - sides
        return weakSides
            .map {
                val otherPos = pos.offset(it)
                if (world.getBlockState(otherPos).block == Blocks.REDSTONE_WIRE) 0
                else {
                    val state = world.getBlockState(otherPos)
                    if (state.isSolidBlock(world, otherPos)) state.getStrongRedstonePower(world, otherPos, it)
                    else state.getWeakRedstonePower(world, otherPos, it)
                }
            }
            .any { it > 0 } ||
            (receiveFromBottom && sides
                .filterNot { world.getBlockState(pos.offset(it)).block == Blocks.REDSTONE_WIRE }
                .any { world.getEmittedRedstonePower(pos.offset(it), it) > 0 })
    }

}

object BundledCableUtils {

    fun getReceivedData(state: BlockState, world: World, pos: BlockPos): UShort {
        val sides = WireUtils.getOccupiedSides(state)
        val inputSides = Direction.values().filter { a -> sides.any { b -> b.axis != a.axis } }.distinct() - sides
        return inputSides
            .flatMap { side ->
                val edges = (Direction.values().toSet() - sides).filter { edge -> edge.axis != side.axis }
                edges.map { edge ->
                    val otherPos = pos.offset(side)
                    if (world.getBlockState(otherPos).block == RSWires.blocks.uncoloredBundledCable) 0u
                    else {
                        val state = world.getBlockState(otherPos)
                        val block = state.block
                        if (block is BlockBundledCableIo) {
                            block.getBundledOutput(state, world, otherPos, side.opposite, edge).toUShort()
                        } else 0u
                    }
                }
            }
            .fold(0u.toUShort()) { a, b -> a or b }
    }

}