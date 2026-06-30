package dev.boecker.cherrycave.marathon.game

import dev.boecker.cherrycave.marathon.MarathonServer
import io.github.oshai.kotlinlogging.KotlinLogging
import net.minestom.server.MinecraftServer
import net.minestom.server.color.Color
import net.minestom.server.coordinate.Point
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.GameMode
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent
import net.minestom.server.event.player.PlayerDisconnectEvent
import net.minestom.server.instance.LightingChunk
import net.minestom.server.instance.block.Block
import net.minestom.server.instance.generator.GenerationUnit
import net.minestom.server.registry.RegistryKey
import net.minestom.server.registry.TagKey
import net.minestom.server.world.DimensionType
import net.minestom.server.world.attribute.EnvironmentAttribute
import net.minestom.server.world.clock.WorldClock
import java.util.*

class GameManager(val server: MarathonServer) {

    private val logger = KotlinLogging.logger {}

    val games = mutableMapOf<UUID, MarathonGame>()

    val marathonMapGenerator = { unit: GenerationUnit ->
        val start: Point = unit.absoluteStart()
        val size: Point = unit.size()

        if (0 in start.blockX()..(start.blockX() + size.blockX()) && 0 in start.blockZ()..(start.blockZ() + size.blockZ()) && 10 in start.blockY()..(start.blockY() + size.blockY())) {
            unit.modifier().setBlock(start.withX(0.0).withZ(0.0).withY(10.0), Block.WHITE_WOOL)
        }
    }

    val joinListener = { event: AsyncPlayerConfigurationEvent ->
        val player = event.player

        val instance = MinecraftServer.getInstanceManager().createInstanceContainer(dimension)

        instance.time = 18000
        instance.setChunkSupplier(::LightingChunk)
        instance.setGenerator(marathonMapGenerator)
        instance.defaultClock()?.pause()
        event.spawningInstance = instance
        player.respawnPoint = Pos(0.5, 11.0, 0.5)
        player.gameMode = GameMode.ADVENTURE

        val game = MarathonGame(server, player, instance)
        games[player.uuid] = game
    }

    val disconnectListener: ((PlayerDisconnectEvent) -> Unit) = { event: PlayerDisconnectEvent ->
        val player = event.player

        MinecraftServer.getInstanceManager().unregisterInstance(games[player.uuid]?.instance)

        games[player.uuid]?.stopGame()
        games.remove(player.uuid)
    }

    val dimension: RegistryKey<DimensionType>

    init {
        val eventHandler = MinecraftServer.getGlobalEventHandler()

        eventHandler.addListener(AsyncPlayerConfigurationEvent::class.java, joinListener)
        eventHandler.addListener(PlayerDisconnectEvent::class.java, disconnectListener)

        dimension = MinecraftServer.getDimensionTypeRegistry().register(
            "marathon", DimensionType.builder()
                .setAttribute(EnvironmentAttribute.FOG_COLOR, Color(0xC0D8FF))
                .setAttribute(EnvironmentAttribute.SKY_COLOR, Color(0x78A7FF))
                .setAttribute(EnvironmentAttribute.AMBIENT_LIGHT_COLOR, Color(0xFFFFFF))
                .defaultClock(WorldClock.OVERWORLD)
                .timelines(MinecraftServer.getTimelineRegistry().getTag(TagKey.ofHash("#minecraft:in_overworld")))
                .fixedTime(true)
                .ambientLight(15.0F)
                .build()
        )
    }
}