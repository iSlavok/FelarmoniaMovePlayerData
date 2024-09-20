package online.slavok.felarmoniaMovePlayerData

import online.slavok.felarmoniaMovePlayerData.commands.MovePlayerDataCommand
import online.slavok.felarmoniaMovePlayerData.database.Whitelist
import online.slavok.felarmoniaMovePlayerData.database.Auth
import org.bukkit.plugin.java.JavaPlugin
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import org.json.simple.parser.ParseException
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.*

class FelarmoniaMovePlayerData : JavaPlugin() {
    private val advancementsPath: Path = dataFolder.toPath().toAbsolutePath().parent.parent.resolve("world/advancements")
    private val playerDataPath: Path = dataFolder.toPath().toAbsolutePath().parent.parent.resolve("world/playerdata")
    private val statsPath: Path = dataFolder.toPath().toAbsolutePath().parent.parent.resolve("world/stats")
    private val client: HttpClient = HttpClient.newBuilder().build()

    override fun onEnable() {
        saveDefaultConfig()
        val whitelistDatabaseUrl = config.getString("whitelist-mysql-url") ?: return
        val authDatabaseUrl = config.getString("auth-mysql-url") ?: return
        val accountDatabase = Whitelist(whitelistDatabaseUrl)
        val settingsDatabase = Auth(authDatabaseUrl)
        getCommand("moveplayerdata")?.setExecutor(MovePlayerDataCommand(accountDatabase, settingsDatabase, this))
    }

    override fun onDisable() {

    }

    @Throws(IOException::class)
    fun replaceData(from: String, to: String) {
        val advancements = advancementsPath.resolve("$from.json")
        val newAdvancements = advancementsPath.resolve("$to.json")
        if (advancements.toFile().exists()) {
            Files.move(advancements, newAdvancements, StandardCopyOption.REPLACE_EXISTING)
        }

        val playerData = playerDataPath.resolve("$from.dat")
        val newPlayerData = playerDataPath.resolve("$to.dat")
        val playerDataOld = playerDataPath.resolve("$from.dat_old")
        val newPlayerDataOld = playerDataPath.resolve("$to.dat_old")
        if (playerData.toFile().exists()) {
            Files.move(playerData, newPlayerData, StandardCopyOption.REPLACE_EXISTING)
        }
        if (playerDataOld.toFile().exists()) {
            Files.move(playerDataOld, newPlayerDataOld, StandardCopyOption.REPLACE_EXISTING)
        }

        val stats = statsPath.resolve("$from.json")
        val newStats = statsPath.resolve("$to.json")
        if (stats.toFile().exists()) {
            Files.move(stats, newStats, StandardCopyOption.REPLACE_EXISTING)
        }
    }

    @Throws(IOException::class, InterruptedException::class, ParseException::class)
    fun getOnlineUUID(nickname: String): UUID {
        val response: HttpResponse<String> = client.send(
            HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("https://api.mojang.com/users/profiles/minecraft/$nickname"))
                .build(),
            HttpResponse.BodyHandlers.ofString()
        )

        val rawJson = response.body()
        val parser = JSONParser()
        val `object` = parser.parse(rawJson) as JSONObject
        val rawUuid = `object`["id"] as String
        val one = rawUuid.substring(0, 8)
        val two = rawUuid.substring(8, 12)
        val three = rawUuid.substring(12, 16)
        val four = rawUuid.substring(16, 20)
        val five = rawUuid.substring(20)
        return UUID.fromString("$one-$two-$three-$four-$five")
    }

    fun getOfflineUUID(nickname: String): UUID {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:$nickname").toByteArray(StandardCharsets.UTF_8))
    }
}
