package wtf.kennn.aurachat.storage;

import java.nio.file.Path;
import java.util.UUID;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PlayerStateTest {
    @TempDir Path directory;

    @Test void preservesModerationAndIgnoresAcrossRestart() {
        JavaPlugin plugin = mock(JavaPlugin.class);
        when(plugin.getDataFolder()).thenReturn(directory.toFile());
        UUID viewer = UUID.randomUUID();
        UUID sender = UUID.randomUUID();
        PlayerState original = new PlayerState(plugin);
        original.load();
        original.toggleLock();
        original.setMuted(sender, true);
        assertTrue(original.toggleIgnore(viewer, sender));
        original.save();

        PlayerState restored = new PlayerState(plugin);
        restored.load();
        assertTrue(restored.isLocked());
        assertTrue(restored.isMuted(sender));
        assertTrue(restored.ignores(viewer, sender));
        assertFalse(restored.ignores(sender, viewer));
        assertFalse(restored.toggleIgnore(viewer, sender));
        restored.setMuted(sender, false);
        restored.toggleLock();
        restored.save();

        PlayerState cleared = new PlayerState(plugin);
        cleared.load();
        assertFalse(cleared.isLocked());
        assertFalse(cleared.isMuted(sender));
        assertFalse(cleared.ignores(viewer, sender));
    }
}
