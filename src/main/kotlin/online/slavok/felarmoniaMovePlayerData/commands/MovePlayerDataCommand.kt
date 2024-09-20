package online.slavok.felarmoniaMovePlayerData.commands

import online.slavok.felarmoniaMovePlayerData.FelarmoniaMovePlayerData
import online.slavok.felarmoniaMovePlayerData.database.Whitelist
import online.slavok.felarmoniaMovePlayerData.database.Auth
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class MovePlayerDataCommand (
    private val accountDatabase: Whitelist,
    private val settingsDatabase: Auth,
    private val instance: FelarmoniaMovePlayerData
) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        if (args != null) {
            if (args.size != 2) return true
        } else {
            return true
        }
        val from = args[0]
        val to = args[1]
        val fromUuid = if (settingsDatabase.isPremium(from)) {
            instance.getOnlineUUID(from)
        } else {
            instance.getOfflineUUID(from)
        }
        val toUuid = if (settingsDatabase.isPremium(to)) {
            instance.getOnlineUUID(to)
        } else {
            instance.getOfflineUUID(to)
        }
        instance.replaceData(fromUuid.toString(), toUuid.toString())
        accountDatabase.changeNickname(from, to)
        sender.sendMessage("Success $from -> $to data move")
        return true
    }
}