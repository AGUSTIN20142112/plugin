package me.agusti.anarchyops.services;

import me.agusti.anarchyops.AnarchyOps;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public final class SupplySnapshotStore {
    private final Path file;
    private final List<SupplySnapshot> snapshots = new ArrayList<>();

    public SupplySnapshotStore(Path file) { this.file = file; }
    public synchronized List<SupplySnapshot> all() { return List.copyOf(snapshots); }
    public synchronized Optional<SupplySnapshot> find(String name) { return snapshots.stream().filter(snapshot -> snapshot.name().equalsIgnoreCase(name)).findFirst(); }

    public synchronized void add(SupplySnapshot snapshot) {
        remove(snapshot.name());
        snapshots.add(snapshot);
        save();
    }

    public synchronized boolean remove(String name) {
        boolean changed = snapshots.removeIf(snapshot -> snapshot.name().equalsIgnoreCase(name));
        if (changed) save();
        return changed;
    }

    public synchronized void load() {
        snapshots.clear();
        if (!Files.isRegularFile(file)) return;
        try {
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                String[] p = line.split("\t", -1);
                if (p.length != 9) continue;
                String name = new String(Base64.getUrlDecoder().decode(p[0]), StandardCharsets.UTF_8);
                snapshots.add(new SupplySnapshot(name, Long.parseLong(p[1]), Integer.parseInt(p[2]), Integer.parseInt(p[3]), Integer.parseInt(p[4]), Integer.parseInt(p[5]), Integer.parseInt(p[6]), Integer.parseInt(p[7]), Integer.parseInt(p[8])));
            }
        } catch (Exception e) {
            AnarchyOps.LOG.error("Could not load supply snapshots", e);
        }
    }

    public synchronized void save() {
        try {
            Files.createDirectories(file.getParent());
            List<String> lines = new ArrayList<>();
            for (SupplySnapshot snapshot : snapshots) {
                String name = Base64.getUrlEncoder().withoutPadding().encodeToString(snapshot.name().getBytes(StandardCharsets.UTF_8));
                lines.add(String.join("\t", name, Long.toString(snapshot.createdAt()), Integer.toString(snapshot.totems()), Integer.toString(snapshot.rockets()), Integer.toString(snapshot.pearls()), Integer.toString(snapshot.obsidian()), Integer.toString(snapshot.crystals()), Integer.toString(snapshot.goldenApples()), Integer.toString(snapshot.experienceBottles())));
            }
            Files.write(file, lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            AnarchyOps.LOG.error("Could not save supply snapshots", e);
        }
    }
}
