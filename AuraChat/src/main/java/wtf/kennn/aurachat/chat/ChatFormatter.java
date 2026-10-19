package wtf.kennn.aurachat.chat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class ChatFormatter {
    private final JavaPlugin plugin;
    private final LegacyComponentSerializer colors = LegacyComponentSerializer.legacyAmpersand();
    public ChatFormatter(JavaPlugin plugin) { this.plugin = plugin; }
    public Component format(Player player, Component original) {
        String text = PlainTextComponentSerializer.plainText().serialize(original);
        Component body = player.hasPermission("aurachat.color") ? colors.deserialize(text) : original;
        String format = placeholders(player, plugin.getConfig().getString("chat.format", "{prefix}&f{player}&8: &r{message}"))
                .replace("{prefix}", prefix(player)).replace("{player}", player.getName())
                .replace("{world}", player.getWorld().getName());
        String[] pieces = format.split("\\{message}", -1);
        Component rendered = Component.empty();
        for (int i = 0; i < pieces.length; i++) {
            if (i > 0) rendered = rendered.append(body);
            rendered = rendered.append(colors.deserialize(pieces[i]));
        }
        return rendered;
    }
    private String placeholders(Player player, String format) {
        if (!Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) return format;
        try {
            Class<?> api = Class.forName("me.clip.placeholderapi.PlaceholderAPI");
            return (String) api.getMethod("setPlaceholders", org.bukkit.OfflinePlayer.class, String.class).invoke(null, player, format);
        } catch (ReflectiveOperationException ex) {
            plugin.getLogger().warning("No se pudieron resolver los placeholders: " + ex.getMessage()); return format;
        }
    }

    private String prefix(Player player) {
        if (!Bukkit.getPluginManager().isPluginEnabled("LuckPerms")) return "";
        try {
            Class<?> provider = Class.forName("net.luckperms.api.LuckPermsProvider");
            Object api = provider.getMethod("get").invoke(null);
            Class<?> luckApi = Class.forName("net.luckperms.api.LuckPerms");
            Object adapter = luckApi.getMethod("getPlayerAdapter", Class.class).invoke(api, Player.class);
            Class<?> adapterType = Class.forName("net.luckperms.api.platform.PlayerAdapter");
            Object meta = adapterType.getMethod("getMetaData", Object.class).invoke(adapter, player);
            String value = (String) Class.forName("net.luckperms.api.cacheddata.CachedMetaData").getMethod("getPrefix").invoke(meta);
            return value == null ? "" : value;
        } catch (ReflectiveOperationException ex) {
            plugin.getLogger().warning("No se pudo leer el prefijo de LuckPerms: " + ex.getMessage()); return "";
        }
    }

}
