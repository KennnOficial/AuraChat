package wtf.kennn.aurachat.integration;

import com.kennndev.auralobby.chat.ChatListener;
import java.util.Arrays;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuraLobbyHookTest {
    private Plugin plugin(String name) {
        Plugin plugin = mock(Plugin.class);
        when(plugin.getName()).thenReturn(name);
        when(plugin.isEnabled()).thenReturn(true);
        return plugin;
    }

    private RegisteredListener register(HandlerList handlers, Plugin plugin, Listener listener) {
        var entry = new RegisteredListener(listener, (target, event) -> {}, EventPriority.HIGHEST, plugin, true);
        handlers.register(entry);
        return entry;
    }

    @Test void removesOnlyLobbyFormatterAndRestoresItOnce() {
        var handlers = new HandlerList();
        var lobby = plugin("AuraLobby");
        var target = register(handlers, lobby, new ChatListener());
        var otherLobbyFeature = register(handlers, lobby, new Listener() {});
        var otherPlugin = register(handlers, plugin("Other"), new ChatListener());
        var hook = new AuraLobbyHook(handlers);
        assertEquals(1, hook.suspend(lobby));
        assertEquals(0, hook.suspend(lobby));
        assertEquals(2, handlers.getRegisteredListeners().length);
        assertFalse(Arrays.asList(handlers.getRegisteredListeners()).contains(target));
        assertTrue(Arrays.asList(handlers.getRegisteredListeners()).containsAll(java.util.List.of(otherLobbyFeature, otherPlugin)));
        hook.restore();
        hook.restore();
        assertEquals(3, handlers.getRegisteredListeners().length);
    }

    @Test void absentOrDisabledLobbyDoesNotChangeHandlers() {
        var handlers = new HandlerList();
        var lobby = plugin("AuraLobby");
        register(handlers, lobby, new ChatListener());
        var hook = new AuraLobbyHook(handlers);
        assertEquals(0, hook.suspend(null));
        when(lobby.isEnabled()).thenReturn(false);
        assertEquals(0, hook.suspend(lobby));
        assertEquals(1, handlers.getRegisteredListeners().length);
    }

    @Test void disabledLobbyIsNotRestoredAndNewInstanceCanTakeOver() {
        var handlers = new HandlerList();
        var oldLobby = plugin("AuraLobby");
        register(handlers, oldLobby, new ChatListener());
        var hook = new AuraLobbyHook(handlers);
        hook.suspend(oldLobby);
        hook.forget(oldLobby);
        var newLobby = plugin("AuraLobby");
        var newEntry = register(handlers, newLobby, new ChatListener());
        assertEquals(1, hook.suspend(newLobby));
        hook.restore();
        assertArrayEquals(new RegisteredListener[]{newEntry}, handlers.getRegisteredListeners());
        hook.suspend(newLobby);
        when(newLobby.isEnabled()).thenReturn(false);
        hook.restore();
        assertEquals(0, handlers.getRegisteredListeners().length);
    }
}
