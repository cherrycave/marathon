package dev.boecker.cherrycave.marathon.game

import dev.boecker.cherrycave.marathon.MarathonServer
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.Point
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.Player
import net.minestom.server.event.EventFilter
import net.minestom.server.event.EventNode
import net.minestom.server.event.player.PlayerMoveEvent
import net.minestom.server.event.trait.PlayerEvent
import net.minestom.server.instance.InstanceContainer
import net.minestom.server.instance.block.Block
import net.minestom.server.network.ConnectionState


class MarathonGame(val server: MarathonServer, val player: Player, val instance: InstanceContainer) {

    val eventNode: EventNode<PlayerEvent> = EventNode.value(
        "${player.username}-game",
        EventFilter.PLAYER
    ) { filter -> filter.uuid == player.uuid };

    val blocks = mutableListOf<Pos>()

    val moveListener: ((PlayerMoveEvent) -> Unit) = { event: PlayerMoveEvent ->
        if (event.isOnGround) {
            val posBelow = getBlocksBelow(event.newPosition)

            if (posBelow.any { it.sameBlock(blocks[blocks.size - 2]) }) {
                blocks.add(spawnNewBlock())
            } else if (posBelow.any { it.sameBlock(blocks.last()) }) {
                blocks.add(spawnNewBlock())
                blocks.add(spawnNewBlock())
            }
        } else {
            if (event.newPosition.blockY() < (blocks.last().y - 5)) {
                blocks.forEach {
                    instance.setBlock(it, Block.AIR)
                }
                blocks.clear()
                player.teleport(player.respawnPoint)
                blocks.add(spawnNewBlock(player.respawnPoint.add(0.0, -0.5, 0.0)))
                blocks.add(spawnNewBlock())
            }
        }
    }

    fun getBlocksBelow(position: Pos): List<Pos> {
        val offset = 0.5

        return listOf(
            position,
            position.add(offset, -0.5, 0.0),
            position.add(-offset, -0.5, 0.0),
            position.add(0.0, -0.5, offset),
            position.add(0.0, -0.5, -offset),
            position.add(offset, -0.5, offset),
            position.add(-offset, -0.5, -offset),
            position.add(offset, -0.5, -offset),
            position.add(-offset, -0.5, offset),
        )
    }

    fun playJumpSound(player: Player, pos: Point) {
        player.playSound(Sound.sound(Key.key("entity.chicken.egg"), Sound.Source.PLAYER, 0.5f, 0.8f), pos)
    }

    val modifiers = listOf(
        Pos(-1.0, -1.0, 4.0),
        Pos(0.0, -1.0, 4.0),
        Pos(1.0, -1.0, 4.0),
        Pos(-1.0, 0.0, 4.0),
        Pos(0.0, 0.0, 4.0),
        Pos(1.0, 0.0, 4.0),
        Pos(-1.0, 1.0, 4.0),
        Pos(0.0, 1.0, 4.0),
        Pos(1.0, 1.0, 4.0),
        Pos(0.0, 0.0, 5.0),
        Pos(1.0, -1.0, 5.0),
        Pos(0.0, -1.0, 5.0),
        Pos(-1.0, -1.0, 5.0),
        Pos(-1.0, 1.0, 3.0),
        Pos(-2.0, 1.0, 3.0),
        Pos(1.0, 1.0, 3.0),
        Pos(2.0, 1.0, 3.0),
        Pos(2.0, 0.0, 3.0),
        Pos(-2.0, 0.0, 3.0),
    )

    init {
        eventNode.addListener(PlayerMoveEvent::class.java, moveListener)

        blocks.add(spawnNewBlock(player.respawnPoint.add(0.0, -0.5, 0.0)))
        blocks.add(spawnNewBlock())

        MinecraftServer.getGlobalEventHandler().addChild(eventNode)
    }

    fun stopGame() {
        MinecraftServer.getGlobalEventHandler().removeChild(eventNode)
    }

    fun spawnNewBlock(fromPosition: Pos = blocks.last()): Pos {
        val newPosition = fromPosition.add(modifiers.random())

        if (player.playerConnection.clientState == ConnectionState.PLAY) {
            playJumpSound(player, newPosition)
        }

        instance.setBlock(newPosition, Block.values().filter {
            it.name().contains("wool")
        }.random())

        return newPosition
    }

}