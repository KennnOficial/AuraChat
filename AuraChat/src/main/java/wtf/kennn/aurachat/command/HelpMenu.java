package wtf.kennn.aurachat.command;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;

public final class HelpMenu {
    private final LegacyComponentSerializer colors = LegacyComponentSerializer.legacyAmpersand();

    public void show(CommandSender sender) {
        line(sender, "&8&m----------------------------------------");
        line(sender, "&d&lAuraChat &8| &fCommand Help");
        line(sender, "&7/aurachat help &8- &fShow this help menu.");
        entry(sender, "aurachat.msg", "/msg <player> <message>", "Send a private message. Aliases: /tell, /w.");
        entry(sender, "aurachat.msg", "/reply <message>", "Reply to your last conversation. Alias: /r.");
        entry(sender, "aurachat.ignore", "/ignore <player>", "Toggle ignoring an online player.");
        entry(sender, "aurachat.moderate", "/mute <player>", "Mute an online player until unmuted.");
        entry(sender, "aurachat.moderate", "/unmute <player>", "Allow a muted player to chat again.");
        entry(sender, "aurachat.admin", "/chat lock", "Toggle the public chat lock.");
        entry(sender, "aurachat.admin", "/chat clear", "Clear chat for all online players.");
        entry(sender, "aurachat.admin", "/aurachat reload", "Reload the configuration.");
        line(sender, "&8&m----------------------------------------");
    }

    private void entry(CommandSender sender, String permission, String command, String description) {
        if (sender.hasPermission(permission)) line(sender, "&d" + command + " &8- &7" + description);
    }

    private void line(CommandSender sender, String text) {
        sender.sendMessage(colors.deserialize(text));
    }
}
