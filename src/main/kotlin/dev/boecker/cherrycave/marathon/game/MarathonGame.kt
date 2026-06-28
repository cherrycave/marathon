package dev.boecker.cherrycave.marathon.game

import dev.boecker.cherrycave.marathon.MarathonServer
import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.Player
import net.minestom.server.event.EventFilter
import net.minestom.server.event.EventNode
import net.minestom.server.event.player.PlayerMoveEvent
import net.minestom.server.event.trait.PlayerEvent
import net.minestom.server.instance.InstanceContainer
import net.minestom.server.instance.block.Block


class MarathonGame(val server: MarathonServer, val player: Player, val instance: InstanceContainer) {

    val eventNode: EventNode<PlayerEvent> = EventNode.value(
        "${player.username}-game",
        EventFilter.PLAYER
    ) { filter -> filter.uuid == player.uuid };

    val blocks = mutableListOf<Pos>()

    val moveListener: ((PlayerMoveEvent) -> Unit) = { event: PlayerMoveEvent ->
        if (event.isOnGround) {
            val posBelow = event.newPosition.add(0.0, -0.5, 0.0)

            if (posBelow.sameBlock(blocks.last())) {
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
            }
        }
    }

    init {
        eventNode.addListener(PlayerMoveEvent::class.java, moveListener)
        blocks.add(spawnNewBlock(player.respawnPoint.add(0.0, -0.5, 0.0)))

        MinecraftServer.getGlobalEventHandler().addChild(eventNode)
    }

    fun stopGame() {
        MinecraftServer.getGlobalEventHandler().removeChild(eventNode)
    }

    fun spawnNewBlock(fromPosition: Pos = blocks.last()): Pos {
        val newPosition = fromPosition.add(listOf(-1.0, 0.0, 1.0).random(), listOf(-1.0, 0.0, 1.0).random(), 4.0)

        instance.setBlock(newPosition, Block.values().filter {
            it.name().contains("wool")
        }.random())

        return newPosition
    }

}