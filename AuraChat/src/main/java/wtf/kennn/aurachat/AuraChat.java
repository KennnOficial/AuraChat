package wtf.kennn.aurachat;

import wtf.kennn.aurachat.chat.ChatFormatter;
import wtf.kennn.aurachat.command.ChatCommands;
import wtf.kennn.aurachat.integration.LobbyIntegration;
import wtf.kennn.aurachat.listener.ChatListener;
import wtf.kennn.aurachat.message.Messages;
import wtf.kennn.aurachat.messaging.PrivateMessageService;
import wtf.kennn.aurachat.moderation.ModerationService;
import wtf.kennn.aurachat.storage.PlayerState;

import java.util.List;
import java.util.Objects;
import org.bukkit.plugin.java.JavaPlugin;

public final class AuraChat extends JavaPlugin {
    private PlayerState state;
    private LobbyIntegration lobby;

    @Override public void onEnable() {
        saveDefaultConfig();
        state = new PlayerState(this);
        state.load();
        Messages messages = new Messages(this);
        ModerationService moderation = new ModerationService(this, state, messages);
        ChatFormatter formatter = new ChatFormatter(this);
        PrivateMessageService privateMessages = new PrivateMessageService(state, messages, moderation);
        lobby = new LobbyIntegration(this);
        ChatCommands commands = new ChatCommands(this, state, messages, lobby, privateMessages);
        for (String name : List.of("aurachat", "msg", "reply", "ignore", "mute", "unmute", "chat")) {
            var command = Objects.requireNonNull(getCommand(name));
            command.setExecutor(commands);
            command.setTabCompleter(commands);
        }
        getServer().getPluginManager().registerEvents(
                new ChatListener(this, state, messages, moderation, formatter, privateMessages), this);
        getServer().getPluginManager().registerEvents(lobby, this);
        lobby.refresh();
    }

    @Override public void onDisable() {
        if (lobby != null) lobby.close();
        if (state != null) state.save();
    }
}
