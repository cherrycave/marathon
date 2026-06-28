package dev.boecker.cherrycave.marathon

import dev.boecker.cherrycave.marathon.game.GameManager
import dev.boecker.cherrycave.permission.minestom.PermissionsAPI
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import net.minestom.server.Auth
import net.minestom.server.MinecraftServer
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.readText

class MarathonServer {
    private val logger = KotlinLogging.logger {}

    val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    lateinit var gameManager: GameManager

    fun start() {
        System.setProperty("minestom.chunk-view-distance", "12");

        val auth = enableAuthentication()
        val minecraftServer = MinecraftServer.init(auth);

        PermissionsAPI.init(System.getenv("LUCKPERMS_REST_URL") ?: "http://localhost:25401")

        gameManager = GameManager(this)

        minecraftServer.start(System.getenv("HOST") ?: "[::1]", System.getenv("PORT")?.toInt() ?: 25565);
    }

    private fun enableAuthentication(): Auth {
        val path = Path("forwarding.secret")
        return if (path.exists()) {
            val secret = path.readText().trim()
            logger.info { "Enabling Velocity Auth: \"$secret\"" }
            Auth.Velocity(secret)
        } else {
            logger.info { "Enabling Mojang Auth" }
            Auth.Online()
        }
    }
}