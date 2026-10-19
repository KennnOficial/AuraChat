package wtf.kennn.aurachat.listener;

import wtf.kennn.aurachat.chat.ChatFormatter;
import wtf.kennn.aurachat.message.Messages;
import wtf.kennn.aurachat.messaging.PrivateMessageService;
import wtf.kennn.aurachat.moderation.ModerationService;
import wtf.kennn.aurachat.storage.PlayerState;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import java.util.UUID;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.concurrent.ExecutionException;

public final class ChatListener implements Listener {
    private final JavaPlugin plugin;
    private final PlayerState state;
    private final Messages messages;
    private final ModerationService moderation;
    private final ChatFormatter formatter;
    private final PrivateMessageService privateMessages;
    public ChatListener(JavaPlugin plugin, PlayerState state, Messages messages, ModerationService moderation,
                 ChatFormatter formatter, PrivateMessageService privateMessages) {
        this.plugin = plugin; this.state = state; this.messages = messages;
        this.moderation = moderation; this.formatter = formatter; this.privateMessages = privateMessages;
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        if (!event.isAsynchronous()) {
            handleChat(event);
            return;
        }
        try {
            Bukkit.getScheduler().callSyncMethod(plugin, () -> {
                handleChat(event);
                return null;
            }).get();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            event.setCancelled(true);
        } catch (ExecutionException ex) {
            event.setCancelled(true);
            plugin.getLogger().log(java.util.logging.Level.SEVERE, "No se pudo procesar el chat", ex.getCause());
        }
    }

    private void handleChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        if (state.isLocked() && !player.hasPermission("aurachat.bypass.lock")) { event.setCancelled(true); messages.tell(player, "locked"); return; }
        String text = PlainTextComponentSerializer.plainText().serialize(event.message());
        if (!moderation.canSpeak(player, text)) { event.setCancelled(true); return; }
        Component result = formatter.format(player, event.message());
        event.renderer((source, displayName, message, viewer) -> result);
        event.viewers().removeIf(viewer -> viewer instanceof Player recipient && state.ignores(recipient.getUniqueId(), player.getUniqueId()));
    }

    @EventHandler public void onQuit(PlayerQuitEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        moderation.forget(id);
        privateMessages.forget(id);
    }

}
