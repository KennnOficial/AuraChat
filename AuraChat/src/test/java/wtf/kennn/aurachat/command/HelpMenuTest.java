package wtf.kennn.aurachat.command;

import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HelpMenuTest {
    private String output(CommandSender sender) {
        var lines = ArgumentCaptor.forClass(Component.class);
        verify(sender, atLeastOnce()).sendMessage(lines.capture());
        return String.join("\n", lines.getAllValues().stream()
                .map(PlainTextComponentSerializer.plainText()::serialize).toList());
    }

    @Test void helpShowsOnlyAuthorizedCommands() {
        CommandSender sender = mock(CommandSender.class);
        when(sender.hasPermission("aurachat.msg")).thenReturn(true);
        when(sender.hasPermission("aurachat.ignore")).thenReturn(true);
        new HelpMenu().show(sender);
        String text = output(sender);
        assertTrue(text.contains("/aurachat help"));
        assertTrue(text.contains("/msg <player> <message>"));
        assertTrue(text.contains("/reply <message>"));
        assertTrue(text.contains("/ignore <player>"));
        assertFalse(text.contains("/mute"));
        assertFalse(text.contains("/chat lock"));
        assertFalse(text.contains("/aurachat reload"));
    }

    @Test void rootAndHelpBothShowHelpAndCompletionRespectsPermissions() {
        CommandSender sender = mock(CommandSender.class);
        Command command = mock(Command.class);
        when(command.getName()).thenReturn("aurachat");
        ChatCommands commands = new ChatCommands(null, null, null, null, null);
        assertTrue(commands.onCommand(sender, command, "aurachat", new String[0]));
        assertTrue(commands.onCommand(sender, command, "aurachat", new String[]{"help"}));
        assertTrue(output(sender).contains("Command Help"));
        assertEquals(List.of("help"), commands.onTabComplete(sender, command, "aurachat", new String[]{""}));
        when(sender.hasPermission("aurachat.admin")).thenReturn(true);
        assertEquals(List.of("help", "reload"), commands.onTabComplete(sender, command, "aurachat", new String[]{""}));
        new HelpMenu().show(sender);
        assertTrue(output(sender).contains("/aurachat reload"));
    }
}
