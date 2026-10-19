package wtf.kennn.aurachat.integration;

import io.papermc.paper.event.player.AsyncChatEvent;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;

final class AuraLobbyHook {
    private final HandlerList handlers;
    private final List<RegisteredListener> suspended = new ArrayList<>();

    AuraLobbyHook() { this(AsyncChatEvent.getHandlerList()); }
    AuraLobbyHook(HandlerList handlers) { this.handlers = handlers; }

    int suspend(Plugin lobby) {
        if (lobby == null || !lobby.isEnabled() || !lobby.getName().equals("AuraLobby")) return 0;
        int count = 0;
        for (RegisteredListener entry : handlers.getRegisteredListeners()) {
            String listenerClass = entry.getListener().getClass().getName();
            if (entry.getPlugin() == lobby && (listenerClass.equals("com.kennn.auralobby.chat.ChatListener")
                    || listenerClass.equals("com.kennndev.auralobby.chat.ChatListener"))) {
                handlers.unregister(entry);
                suspended.add(entry);
                count++;
            }
        }
        return count;
    }

    void forget(Plugin lobby) {
        suspended.removeIf(entry -> entry.getPlugin() == lobby);
    }

    void restore() {
        for (RegisteredListener entry : suspended) {
            if (entry.getPlugin().isEnabled()) handlers.register(entry);
        }
        suspended.clear();
    }
}
