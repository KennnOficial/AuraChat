package wtf.kennn.aurachat.command;

import wtf.kennn.aurachat.integration.LobbyIntegration;
import wtf.kennn.aurachat.message.Messages;
import wtf.kennn.aurachat.messaging.PrivateMessageService;
import wtf.kennn.aurachat.storage.PlayerState;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import java.util.List;
import java.util.Locale;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class ChatCommands implements CommandExecutor, TabCompleter {
    private final JavaPlugin plugin;
    private final PlayerState state;
    private final Messages messages;
    private final LobbyIntegration lobby;
    private final PrivateMessageService privateMessages;
    private final HelpMenu help = new HelpMenu();
    private final LegacyComponentSerializer colors = LegacyComponentSerializer.legacyAmpersand();
    public ChatCommands(JavaPlugin plugin, PlayerState state, Messages messages, LobbyIntegration lobby,
                 PrivateMessageService privateMessages) {
        this.plugin = plugin; this.state = state; this.messages = messages;
        this.lobby = lobby; this.privateMessages = privateMessages;
    }
    @Override public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String name = command.getName();
        if (name.equals("aurachat")) {
            if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
                if (messages.allowed(sender, "aurachat.admin")) { plugin.reloadConfig(); lobby.refresh(); messages.tell(sender, "reloaded"); }
            } else help.show(sender);
            return true;
        }
        if (name.equals("chat")) {
            if (!messages.allowed(sender, "aurachat.admin")) return true;
            if (args.length == 1 && args[0].equalsIgnoreCase("lock")) {
                state.toggleLock(); state.save();
                Bukkit.broadcast(colors.deserialize(plugin.getConfig().getString("messages." + (state.isLocked() ? "chat-locked" : "chat-unlocked"), "")));
            } else if (args.length == 1 && args[0].equalsIgnoreCase("clear")) {
                for (Player online : Bukkit.getOnlinePlayers()) for (int i = 0; i < 100; i++) online.sendMessage(Component.empty());
                Bukkit.broadcast(colors.deserialize(plugin.getConfig().getString("messages.chat-cleared", "")));
            } else sender.sendMessage(Component.text("Uso: /chat <lock|clear>"));
            return true;
        }
        if (name.equals("mute") || name.equals("unmute")) {
            if (!messages.allowed(sender, "aurachat.moderate")) return true;
            if (args.length != 1) { sender.sendMessage(Component.text("Uso: /" + label + " <jugador conectado>")); return true; }
            Player target = Bukkit.getPlayerExact(args[0]);
            if (target == null) { messages.tell(sender, "not-online"); return true; }
            if (name.equals("mute")) {
                if (target.hasPermission("aurachat.mute.exempt")) { messages.tell(sender, "exempt"); return true; }
                state.setMuted(target.getUniqueId(), true); messages.tell(target, "muted");
            } else { state.setMuted(target.getUniqueId(), false); messages.tell(target, "unmuted"); }
            state.save(); messages.tell(sender, "updated"); return true;
        }
        if (!(sender instanceof Player player)) { messages.tell(sender, "players-only"); return true; }
        if (!messages.allowed(sender, "aurachat." + (name.equals("reply") ? "msg" : name))) return true;
        if (name.equals("ignore")) {
            if (args.length != 1) { sender.sendMessage(Component.text("Uso: /ignore <jugador conectado>")); return true; }
            Player target = Bukkit.getPlayerExact(args[0]);
            if (target == null) { messages.tell(sender, "not-online"); return true; }
            if (target.equals(player)) { messages.tell(sender, "self"); return true; }
            messages.tell(player, state.toggleIgnore(player.getUniqueId(), target.getUniqueId()) ? "ignore-on" : "ignore-off");
            state.save(); return true;
        }
        return privateMessages.execute(player, name, args);
    }

    @Override public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length != 1) return List.of();
        List<String> options = switch (command.getName()) {
            case "aurachat" -> sender.hasPermission("aurachat.admin") ? List.of("help", "reload") : List.of("help");
            case "chat" -> sender.hasPermission("aurachat.admin") ? List.of("lock", "clear") : List.of();
            case "reply" -> List.of();
            default -> Bukkit.getOnlinePlayers().stream().filter(p -> !(sender instanceof Player viewer) || viewer.canSee(p)).map(Player::getName).toList();
        };
        return options.stream().filter(s -> s.toLowerCase(Locale.ROOT).startsWith(args[0].toLowerCase(Locale.ROOT))).toList();
    }
}
