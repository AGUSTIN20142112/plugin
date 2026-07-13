package me.agusti.anarchyops.services;

import meteordevelopment.meteorclient.events.game.GameJoinedEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.util.math.Vec3d;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public final class SessionTracker {
    private final MarkerStore markers;
    private final Set<UUID> playersSeen = new HashSet<>();
    private final Set<String> dimensionsVisited = new HashSet<>();
    private long startedAt;
    private double distance;
    private double maxDistanceFromStart;
    private int totemPops;
    private int deaths;
    private Vec3d previous;
    private Vec3d start;
    private String startDimension;
    private boolean deathRecorded;
    private int ticks;

    public SessionTracker(MarkerStore markers) {
        this.markers = markers;
        reset();
    }

    @EventHandler
    private void onJoin(GameJoinedEvent event) {
        reset();
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null || mc.world == null) return;

        String dimension = mc.world.getRegistryKey().getValue().toString();
        dimensionsVisited.add(dimension);
        Vec3d now = mc.player.getPos();

        if (start == null) {
            start = now;
            startDimension = dimension;
        }

        if (previous != null) {
            double travelled = previous.distanceTo(now);
            if (travelled < 100) distance += travelled;
        }
        previous = now;

        if (dimension.equals(startDimension) && start != null) {
            maxDistanceFromStart = Math.max(maxDistanceFromStart, start.distanceTo(now));
        }

        if (++ticks % 20 == 0) {
            for (PlayerEntity player : mc.world.getPlayers()) {
                if (player != mc.player) playersSeen.add(player.getUuid());
            }
        }

        if (mc.player.isDead()) {
            if (!deathRecorded) {
                deathRecorded = true;
                deaths++;
                long timestamp = System.currentTimeMillis();
                markers.add(new Marker("death-" + timestamp, dimension, mc.player.getBlockX(), mc.player.getBlockY(), mc.player.getBlockZ(), timestamp));
            }
        } else {
            deathRecorded = false;
        }
    }

    @EventHandler
    private void onPacket(PacketEvent.Receive event) {
        if (!(event.packet instanceof EntityStatusS2CPacket packet) || mc.world == null || mc.player == null) return;
        if (packet.getStatus() == EntityStatuses.USE_TOTEM_OF_UNDYING && packet.getEntity(mc.world) == mc.player) totemPops++;
    }

    public void reset() {
        startedAt = System.currentTimeMillis();
        distance = 0;
        maxDistanceFromStart = 0;
        totemPops = 0;
        deaths = 0;
        previous = null;
        start = null;
        startDimension = null;
        deathRecorded = false;
        ticks = 0;
        playersSeen.clear();
        dimensionsVisited.clear();
    }

    public long elapsedSeconds() {
        return Math.max(0, (System.currentTimeMillis() - startedAt) / 1000);
    }

    public double distance() { return distance; }
    public double maxDistanceFromStart() { return maxDistanceFromStart; }
    public int totemPops() { return totemPops; }
    public int deaths() { return deaths; }
    public int playersSeen() { return playersSeen.size(); }
    public int dimensionsVisited() { return dimensionsVisited.size(); }
}
