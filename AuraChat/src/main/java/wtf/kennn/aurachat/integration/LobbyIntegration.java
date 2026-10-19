package wtf.kennn.aurachat.integration;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class LobbyIntegration implements Listener {
    private final JavaPlugin plugin;
    private final AuraLobbyHook lobbyHook = new AuraLobbyHook();
    public LobbyIntegration(JavaPlugin plugin) { this.plugin = plugin; }
    public void close() { lobbyHook.restore(); }
    public void refresh() {
        if (lobbyHook.suspend(plugin.getServer().getPluginManager().getPlugin("AuraLobby")) > 0) {
            plugin.getLogger().info("AuraLobby detectado: AuraChat controla el formato del chat publico.");
        }
    }

    @EventHandler public void onPluginEnable(PluginEnableEvent event) {
        if (event.getPlugin().getName().equals("AuraLobby")) {
            plugin.getServer().getScheduler().runTask(plugin, this::refresh);
        }
    }

    @EventHandler public void onPluginDisable(PluginDisableEvent event) {
        if (lobbyHook != null) lobbyHook.forget(event.getPlugin());
    }

}
