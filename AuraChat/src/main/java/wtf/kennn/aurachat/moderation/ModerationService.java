package wtf.kennn.aurachat.moderation;

import wtf.kennn.aurachat.message.Messages;
import wtf.kennn.aurachat.storage.PlayerState;

import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import java.util.Locale;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class ModerationService {
    private final JavaPlugin plugin;
    private final PlayerState state;
    private final Messages messages;
    private final Map<UUID, Long> lastSent = new HashMap<>();
    private final Map<UUID, String> lastMessage = new HashMap<>();
    public ModerationService(JavaPlugin plugin, PlayerState state, Messages messages) {
        this.plugin = plugin; this.state = state; this.messages = messages;
    }
    public void forget(UUID id) { lastSent.remove(id); lastMessage.remove(id); }
    public boolean canSpeak(Player player, String text) {
        if (state.isMuted(player.getUniqueId())) { messages.tell(player, "muted"); return false; }
        if (!plugin.getConfig().getBoolean("antispam.enabled") || player.hasPermission("aurachat.bypass.antispam")) return true;
        UUID id = player.getUniqueId();
        long now = System.currentTimeMillis();
        long delay = Math.max(0, plugin.getConfig().getLong("antispam.cooldown-seconds", 2)) * 1000;
        if (now - lastSent.getOrDefault(id, 0L) < delay) { messages.tell(player, "cooldown"); return false; }
        String normalized = text.strip().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
        if (plugin.getConfig().getBoolean("antispam.block-repeated") && normalized.equals(lastMessage.get(id))) {
            messages.tell(player, "repeated"); return false;
        }
        long letters = text.codePoints().filter(Character::isLetter).count();
        long capitals = text.codePoints().filter(Character::isUpperCase).count();
        if (letters >= plugin.getConfig().getInt("antispam.caps-min-letters", 10)
                && letters > 0 && capitals * 100.0 / letters > plugin.getConfig().getDouble("antispam.max-caps-percent", 70)) {
            messages.tell(player, "caps"); return false;
        }
        lastSent.put(id, now); lastMessage.put(id, normalized); return true;
    }

}
