package me.agusti.anarchyops.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.agusti.anarchyops.AnarchyOps;
import me.agusti.anarchyops.modules.ThreatRadar;
import me.agusti.anarchyops.services.Marker;
import me.agusti.anarchyops.services.SupplySnapshot;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import net.minecraft.command.CommandSource;
import net.minecraft.item.Items;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Locale;
import java.util.Optional;

public final class OpsCommand extends Command {
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());

    public OpsCommand() {
        super("ops", "AnarchyOps status, navigation, markers, coordinate conversion and supply snapshots.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(ctx -> { status(); return SINGLE_SUCCESS; });
        builder.then(literal("status").executes(ctx -> { status(); return SINGLE_SUCCESS; }));
        builder.then(literal("reset-session").executes(ctx -> {
            AnarchyOps.SESSION.reset();
            info("Session statistics reset.");
            return SINGLE_SUCCESS;
        }));

        builder.then(literal("coords")
            .then(literal("nether").executes(ctx -> { convert(1.0 / 8.0, "Nether"); return SINGLE_SUCCESS; }))
            .then(literal("overworld").executes(ctx -> { convert(8.0, "Overworld"); return SINGLE_SUCCESS; })));

        builder.then(literal("mark")
            .then(literal("add").then(argument("name", StringArgumentType.greedyString()).executes(ctx -> {
                addMarker(StringArgumentType.getString(ctx, "name"));
                return SINGLE_SUCCESS;
            })))
            .then(literal("remove").then(argument("name", StringArgumentType.greedyString()).executes(ctx -> {
                String name = StringArgumentType.getString(ctx, "name").trim();
                if (AnarchyOps.MARKERS.remove(name)) info("Removed marker '%s'.", name);
                else warning("Marker '%s' was not found.", name);
                return SINGLE_SUCCESS;
            })))
            .then(literal("target").then(argument("name", StringArgumentType.greedyString()).executes(ctx -> {
                String name = StringArgumentType.getString(ctx, "name").trim();
                if (AnarchyOps.NAVIGATION.setTarget(name)) info("Navigation target set to '%s'.", AnarchyOps.NAVIGATION.targetName());
                else warning("Marker '%s' was not found.", name);
                return SINGLE_SUCCESS;
            })))
            .then(literal("clear-target").executes(ctx -> {
                AnarchyOps.NAVIGATION.clear();
                info("Navigation target cleared.");
                return SINGLE_SUCCESS;
            }))
            .then(literal("nearest").executes(ctx -> { setNearestMarker(); return SINGLE_SUCCESS; }))
            .then(literal("list").executes(ctx -> { listMarkers(); return SINGLE_SUCCESS; })));

        builder.then(literal("snapshot")
            .then(literal("save").then(argument("name", StringArgumentType.greedyString()).executes(ctx -> {
                saveSnapshot(StringArgumentType.getString(ctx, "name"));
                return SINGLE_SUCCESS;
            })))
            .then(literal("show").then(argument("name", StringArgumentType.greedyString()).executes(ctx -> {
                showSnapshot(StringArgumentType.getString(ctx, "name"), false);
                return SINGLE_SUCCESS;
            })))
            .then(literal("compare").then(argument("name", StringArgumentType.greedyString()).executes(ctx -> {
                showSnapshot(StringArgumentType.getString(ctx, "name"), true);
                return SINGLE_SUCCESS;
            })))
            .then(literal("remove").then(argument("name", StringArgumentType.greedyString()).executes(ctx -> {
                String name = StringArgumentType.getString(ctx, "name").trim();
                if (AnarchyOps.SNAPSHOTS.remove(name)) info("Removed snapshot '%s'.", name);
                else warning("Snapshot '%s' was not found.", name);
                return SINGLE_SUCCESS;
            })))
            .then(literal("list").executes(ctx -> {
                info("Supply snapshots (%d):", AnarchyOps.SNAPSHOTS.all().size());
                for (SupplySnapshot snapshot : AnarchyOps.SNAPSHOTS.all()) {
                    info("- %s (%s)", snapshot.name(), TIME.format(Instant.ofEpochMilli(snapshot.createdAt())));
                }
                return SINGLE_SUCCESS;
            })));
    }

    private void status() {
        if (AnarchyOps.SESSION == null) return;
        long seconds = AnarchyOps.SESSION.elapsedSeconds();
        info("Session %02d:%02d:%02d | %.1f km | max %.0fm | pops %d | deaths %d | players %d | dimensions %d",
            seconds / 3600, (seconds / 60) % 60, seconds % 60,
            AnarchyOps.SESSION.distance() / 1000.0,
            AnarchyOps.SESSION.maxDistanceFromStart(),
            AnarchyOps.SESSION.totemPops(),
            AnarchyOps.SESSION.deaths(),
            AnarchyOps.SESSION.playersSeen(),
            AnarchyOps.SESSION.dimensionsVisited());

        if (ThreatRadar.nearestDistance >= 0) {
            warning("Nearest threat: %s at %.1f blocks with %.1f health.", ThreatRadar.nearestName, ThreatRadar.nearestDistance, ThreatRadar.nearestHealth);
        }

        AnarchyOps.NAVIGATION.target().ifPresent(marker -> {
            double distance = AnarchyOps.NAVIGATION.distance(marker);
            if (distance >= 0) info("Target %s: %.1fm %s at %d %d %d.", marker.name(), distance, AnarchyOps.NAVIGATION.direction(marker), marker.x(), marker.y(), marker.z());
            else info("Target %s is in %s at %d %d %d.", marker.name(), marker.dimension(), marker.x(), marker.y(), marker.z());
        });
    }

    private void addMarker(String rawName) {
        if (mc.player == null || mc.world == null) return;
        String name = rawName.trim();
        if (name.isEmpty()) {
            warning("Marker name cannot be empty.");
            return;
        }
        String dimension = mc.world.getRegistryKey().getValue().toString();
        Marker marker = new Marker(name, dimension, mc.player.getBlockX(), mc.player.getBlockY(), mc.player.getBlockZ(), System.currentTimeMillis());
        AnarchyOps.MARKERS.add(marker);
        info("Saved marker '%s' at %d %d %d.", name, marker.x(), marker.y(), marker.z());
    }

    private void listMarkers() {
        info("Markers (%d), target: %s", AnarchyOps.MARKERS.all().size(), AnarchyOps.NAVIGATION.targetName());
        for (Marker marker : AnarchyOps.MARKERS.all()) {
            String target = marker.name().equalsIgnoreCase(AnarchyOps.NAVIGATION.targetName()) ? " [TARGET]" : "";
            info("- %s: %d %d %d [%s]%s", marker.name(), marker.x(), marker.y(), marker.z(), marker.dimension(), target);
        }
    }

    private void setNearestMarker() {
        if (mc.player == null || mc.world == null) return;
        String dimension = mc.world.getRegistryKey().getValue().toString();
        Optional<Marker> nearest = AnarchyOps.MARKERS.all().stream()
            .filter(marker -> marker.dimension().equals(dimension))
            .min(Comparator.comparingDouble(marker -> mc.player.squaredDistanceTo(marker.x() + 0.5, marker.y() + 0.5, marker.z() + 0.5)));
        if (nearest.isEmpty()) {
            warning("No marker exists in this dimension.");
            return;
        }
        AnarchyOps.NAVIGATION.setTarget(nearest.get().name());
        info("Nearest marker selected: %s.", nearest.get().name());
    }

    private void saveSnapshot(String rawName) {
        if (mc.player == null) return;
        String name = rawName.trim();
        if (name.isEmpty()) {
            warning("Snapshot name cannot be empty.");
            return;
        }
        SupplySnapshot snapshot = currentSnapshot(name);
        AnarchyOps.SNAPSHOTS.add(snapshot);
        info("Saved supply snapshot '%s': T%d R%d P%d O%d C%d G%d XP%d.", snapshot.name(), snapshot.totems(), snapshot.rockets(), snapshot.pearls(), snapshot.obsidian(), snapshot.crystals(), snapshot.goldenApples(), snapshot.experienceBottles());
    }

    private void showSnapshot(String rawName, boolean compare) {
        String name = rawName.trim();
        Optional<SupplySnapshot> found = AnarchyOps.SNAPSHOTS.find(name);
        if (found.isEmpty()) {
            warning("Snapshot '%s' was not found.", name);
            return;
        }
        SupplySnapshot snapshot = found.get();
        info("Snapshot %s (%s): T%d R%d P%d O%d C%d G%d XP%d.", snapshot.name(), TIME.format(Instant.ofEpochMilli(snapshot.createdAt())), snapshot.totems(), snapshot.rockets(), snapshot.pearls(), snapshot.obsidian(), snapshot.crystals(), snapshot.goldenApples(), snapshot.experienceBottles());
        if (compare && mc.player != null) {
            SupplySnapshot current = currentSnapshot("current");
            info("Difference now: T%+d R%+d P%+d O%+d C%+d G%+d XP%+d.",
                current.totems() - snapshot.totems(), current.rockets() - snapshot.rockets(), current.pearls() - snapshot.pearls(),
                current.obsidian() - snapshot.obsidian(), current.crystals() - snapshot.crystals(),
                current.goldenApples() - snapshot.goldenApples(), current.experienceBottles() - snapshot.experienceBottles());
        }
    }

    private SupplySnapshot currentSnapshot(String name) {
        return new SupplySnapshot(name, System.currentTimeMillis(),
            InvUtils.find(Items.TOTEM_OF_UNDYING).count(),
            InvUtils.find(Items.FIREWORK_ROCKET).count(),
            InvUtils.find(Items.ENDER_PEARL).count(),
            InvUtils.find(Items.OBSIDIAN).count(),
            InvUtils.find(Items.END_CRYSTAL).count(),
            InvUtils.find(stack -> stack.isOf(Items.GOLDEN_APPLE) || stack.isOf(Items.ENCHANTED_GOLDEN_APPLE)).count(),
            InvUtils.find(Items.EXPERIENCE_BOTTLE).count());
    }

    private void convert(double factor, String target) {
        if (mc.player == null) return;
        double x = mc.player.getX() * factor;
        double z = mc.player.getZ() * factor;
        info("%s coordinates: X %.1f, Z %.1f", target, x, z);
        mc.keyboard.setClipboard(String.format(Locale.ROOT, "%.1f %.1f", x, z));
    }
}
