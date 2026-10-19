package wtf.kennn.aurachat.message;

import org.bukkit.command.CommandSender;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.plugin.java.JavaPlugin;

public final class Messages {
    private final JavaPlugin plugin;
    private final LegacyComponentSerializer colors = LegacyComponentSerializer.legacyAmpersand();
    public Messages(JavaPlugin plugin) { this.plugin = plugin; }
    public void tell(CommandSender sender, String key) {
        sender.sendMessage(colors.deserialize(plugin.getConfig().getString("messages." + key, key)));
    }
    public boolean allowed(CommandSender sender, String permission) {
        if (sender.hasPermission(permission)) return true;
        tell(sender, "no-permission"); return false;
    }
}
