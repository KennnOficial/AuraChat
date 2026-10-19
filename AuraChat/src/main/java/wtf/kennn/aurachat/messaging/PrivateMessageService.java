package wtf.kennn.aurachat.messaging;

import wtf.kennn.aurachat.message.Messages;
import wtf.kennn.aurachat.moderation.ModerationService;
import wtf.kennn.aurachat.storage.PlayerState;

import org.bukkit.command.CommandSender;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import java.util.Arrays;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class PrivateMessageService {
    private final PlayerState state;
    private final Messages messages;
    private final ModerationService moderation;
    private final LegacyComponentSerializer colors = LegacyComponentSerializer.legacyAmpersand();
    private final Map<UUID, UUID> replies = new HashMap<>();
    public PrivateMessageService(PlayerState state, Messages messages, ModerationService moderation) {
        this.state = state; this.messages = messages; this.moderation = moderation;
    }
    public void forget(UUID id) { replies.remove(id); replies.values().removeIf(id::equals); }
    public boolean execute(Player player, String name, String[] args) {
        CommandSender sender = player;
        Player target;
        int start;
        if (name.equals("reply")) {
            UUID id = replies.get(player.getUniqueId()); target = id == null ? null : Bukkit.getPlayer(id); start = 0;
            if (target == null) { messages.tell(sender, "no-reply"); return true; }
        } else {
            if (args.length < 2) { sender.sendMessage(Component.text("Uso: /msg <jugador> <mensaje>")); return true; }
            target = Bukkit.getPlayerExact(args[0]); start = 1;
        }
        if (args.length <= start) { sender.sendMessage(Component.text("Uso: /reply <mensaje>")); return true; }
        if (target == null) { messages.tell(sender, "not-online"); return true; }
        if (target.equals(player)) { messages.tell(sender, "self"); return true; }
        if (state.ignores(target.getUniqueId(), player.getUniqueId()) || state.ignores(player.getUniqueId(), target.getUniqueId())) {
            messages.tell(sender, "unavailable"); return true;
        }
        String text = String.join(" ", Arrays.copyOfRange(args, start, args.length));
        if (!moderation.canSpeak(player, text)) return true;
        Component body = player.hasPermission("aurachat.color") ? colors.deserialize(text) : Component.text(text);
        player.sendMessage(colors.deserialize("&8[&dTu &7→ &d" + target.getName() + "&8] &r").append(body));
        target.sendMessage(colors.deserialize("&8[&d" + player.getName() + " &7→ &dTu&8] &r").append(body));
        replies.put(player.getUniqueId(), target.getUniqueId()); replies.put(target.getUniqueId(), player.getUniqueId());
        return true;
    }
}
