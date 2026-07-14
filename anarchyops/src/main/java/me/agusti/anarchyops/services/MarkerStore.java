package me.agusti.anarchyops.services;

import me.agusti.anarchyops.AnarchyOps;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.Base64;

public final class MarkerStore {
    private final Path file;
    private final List<Marker> markers = new ArrayList<>();

    public MarkerStore(Path file) { this.file = file; }

    public synchronized List<Marker> all() { return List.copyOf(markers); }

    public synchronized void add(Marker marker) {
        remove(marker.name());
        markers.add(marker);
        save();
    }

    public synchronized boolean remove(String name) {
        boolean changed = markers.removeIf(m -> m.name().equalsIgnoreCase(name));
        if (changed) save();
        return changed;
    }

    public synchronized Optional<Marker> find(String name) {
        return markers.stream().filter(m -> m.name().equalsIgnoreCase(name)).findFirst();
    }

    public synchronized void load() {
        markers.clear();
        if (!Files.isRegularFile(file)) return;
        try {
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                String[] p = line.split("\t", -1);
                if (p.length != 6) continue;
                String name = new String(Base64.getUrlDecoder().decode(p[0]), StandardCharsets.UTF_8);
                markers.add(new Marker(name, p[1], Integer.parseInt(p[2]), Integer.parseInt(p[3]), Integer.parseInt(p[4]), Long.parseLong(p[5])));
            }
        } catch (Exception e) {
            AnarchyOps.LOG.error("Could not load markers", e);
        }
    }

    public synchronized void save() {
        try {
            Files.createDirectories(file.getParent());
            List<String> lines = new ArrayList<>();
            for (Marker m : markers) {
                String name = Base64.getUrlEncoder().withoutPadding().encodeToString(m.name().getBytes(StandardCharsets.UTF_8));
                lines.add(String.join("\t", name, m.dimension(), Integer.toString(m.x()), Integer.toString(m.y()), Integer.toString(m.z()), Long.toString(m.createdAt())));
            }
            Files.write(file, lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            AnarchyOps.LOG.error("Could not save markers", e);
        }
    }
}
