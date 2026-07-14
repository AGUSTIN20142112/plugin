package me.agusti.anarchyops.services;

import java.util.Optional;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public final class NavigationService {
    private final MarkerStore markers;
    private String targetName;

    public NavigationService(MarkerStore markers) {
        this.markers = markers;
    }

    public boolean setTarget(String name) {
        Optional<Marker> marker = markers.find(name);
        if (marker.isEmpty()) return false;
        targetName = marker.get().name();
        return true;
    }

    public void clear() {
        targetName = null;
    }

    public Optional<Marker> target() {
        if (targetName == null) return Optional.empty();
        Optional<Marker> marker = markers.find(targetName);
        if (marker.isEmpty()) targetName = null;
        return marker;
    }

    public String targetName() {
        return targetName == null ? "none" : targetName;
    }

    public boolean isSameDimension(Marker marker) {
        return mc.world != null && marker.dimension().equals(mc.world.getRegistryKey().getValue().toString());
    }

    public double distance(Marker marker) {
        if (mc.player == null || !isSameDimension(marker)) return -1;
        double dx = marker.x() + 0.5 - mc.player.getX();
        double dy = marker.y() + 0.5 - mc.player.getY();
        double dz = marker.z() + 0.5 - mc.player.getZ();
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    public String direction(Marker marker) {
        if (mc.player == null || !isSameDimension(marker)) return "other dimension";
        double dx = marker.x() + 0.5 - mc.player.getX();
        double dz = marker.z() + 0.5 - mc.player.getZ();
        double degrees = Math.toDegrees(Math.atan2(dz, dx));
        String[] directions = {"E", "SE", "S", "SW", "W", "NW", "N", "NE"};
        int index = (int) Math.round(degrees / 45.0);
        index = Math.floorMod(index, 8);
        return directions[index];
    }
}
