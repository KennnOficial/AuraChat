package wtf.kennn.aurachat.storage;

import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;
import java.util.Collections;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.io.IOException;

public final class PlayerState {
    private final JavaPlugin plugin;
    private final Set<UUID> muted = new HashSet<>();
    private final Map<UUID, Set<UUID>> ignored = new HashMap<>();
    private boolean locked;
    private File stateFile;
    public PlayerState(JavaPlugin plugin) { this.plugin = plugin; }
    public void load() {
        stateFile = new File(plugin.getDataFolder(), "players.yml");
        var state = YamlConfiguration.loadConfiguration(stateFile);
        locked = state.getBoolean("locked");
        for (String id : state.getStringList("muted")) {
            try { muted.add(UUID.fromString(id)); } catch (IllegalArgumentException ignoredId) { plugin.getLogger().warning("UUID invalido en muted: " + id); }
        }
        var section = state.getConfigurationSection("ignored");
        if (section != null) for (String id : section.getKeys(false)) {
            try {
                Set<UUID> entries = new HashSet<>();
                for (String target : section.getStringList(id)) entries.add(UUID.fromString(target));
                ignored.put(UUID.fromString(id), entries);
            } catch (IllegalArgumentException ignoredId) { plugin.getLogger().warning("UUID invalido en ignored: " + id); }
        }
    }
    public boolean isLocked() { return locked; }
    public void toggleLock() { locked = !locked; }
    public boolean isMuted(UUID id) { return muted.contains(id); }
    public void setMuted(UUID id, boolean value) { if (value) muted.add(id); else muted.remove(id); }
    public boolean toggleIgnore(UUID viewer, UUID source) {
        Set<UUID> entries = ignored.computeIfAbsent(viewer, key -> new HashSet<>());
        if (entries.remove(source)) return false;
        entries.add(source);
        return true;
    }
    public boolean ignores(UUID viewer, UUID source) {
        return ignored.getOrDefault(viewer, Collections.emptySet()).contains(source);
    }
    public void save() {
        var state = new YamlConfiguration();
        state.set("locked", locked);
        state.set("muted", muted.stream().map(UUID::toString).toList());
        ignored.forEach((id, entries) -> state.set("ignored." + id, entries.stream().map(UUID::toString).toList()));
        try { state.save(stateFile); }
        catch (IOException ex) { plugin.getLogger().severe("No se pudieron guardar los datos: " + ex.getMessage()); }
    }

}
