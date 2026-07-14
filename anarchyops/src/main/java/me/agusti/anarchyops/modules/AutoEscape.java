package me.agusti.anarchyops.modules;

import me.agusti.anarchyops.AnarchyOps;
import me.agusti.anarchyops.services.Marker;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.common.DisconnectS2CPacket;
import net.minecraft.text.Text;

public final class AutoEscape extends Module {
    private final SettingGroup sgTriggers = settings.getDefaultGroup();
    private final SettingGroup sgActions = settings.createGroup("Actions");

    private final Setting<Double> minimumHealth = sgTriggers.add(new DoubleSetting.Builder()
        .name("minimum-health").description("Triggers when health plus absorption reaches this value.")
        .defaultValue(7).min(1).sliderMax(20).build());
    private final Setting<Boolean> triggerOnThreat = sgTriggers.add(new BoolSetting.Builder()
        .name("trigger-on-threat").description("Also triggers when a non-friend player gets very close.")
        .defaultValue(false).build());
    private final Setting<Double> threatRange = sgTriggers.add(new DoubleSetting.Builder()
        .name("threat-range").defaultValue(6).min(2).sliderMax(32).visible(triggerOnThreat::get).build());
    private final Setting<Boolean> pearlFirst = sgActions.add(new BoolSetting.Builder()
        .name("pearl-first").description("Attempts to throw a panic pearl before disconnecting.")
        .defaultValue(true).build());
    private final Setting<Boolean> disconnectFallback = sgActions.add(new BoolSetting.Builder()
        .name("disconnect-fallback").description("Disconnects when no pearl can be thrown.")
        .defaultValue(true).build());
    private final Setting<Boolean> saveMarker = sgActions.add(new BoolSetting.Builder()
        .name("save-escape-marker").defaultValue(true).build());
    private final Setting<Boolean> disableAfter = sgActions.add(new BoolSetting.Builder()
        .name("disable-after-action").defaultValue(true).build());
    private final Setting<Integer> cooldown = sgActions.add(new IntSetting.Builder()
        .name("cooldown").description("Seconds before another escape action can trigger.")
        .defaultValue(15).min(1).sliderMax(60).build());

    private long nextAction;
    private int ticks;

    public AutoEscape() {
        super(AnarchyOps.CATEGORY, "auto-escape", "Escapes on low health or nearby threats using a pearl, with disconnect fallback.");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null || mc.world == null || mc.player.isDead() || ++ticks % 5 != 0) return;
        if (System.currentTimeMillis() < nextAction) return;

        double effectiveHealth = mc.player.getHealth() + mc.player.getAbsorptionAmount();
        boolean lowHealth = effectiveHealth <= minimumHealth.get();
        boolean threat = triggerOnThreat.get() && nearestThreatDistance() <= threatRange.get();
        if (!lowHealth && !threat) return;

        String reason = lowHealth ? String.format("low health (%.1f)", effectiveHealth) : "nearby threat";
        if (saveMarker.get() && AnarchyOps.MARKERS != null) {
            long now = System.currentTimeMillis();
            String dimension = mc.world.getRegistryKey().getValue().toString();
            AnarchyOps.MARKERS.add(new Marker("escape-" + now, dimension, mc.player.getBlockX(), mc.player.getBlockY(), mc.player.getBlockZ(), now));
        }

        boolean escaped = false;
        if (pearlFirst.get()) escaped = Modules.get().get(PanicPearl.class).throwPearl();
        if (!escaped && disconnectFallback.get()) {
            mc.player.networkHandler.onDisconnect(new DisconnectS2CPacket(Text.literal("[AnarchyOps] AutoEscape: " + reason)));
            escaped = true;
        }

        if (escaped) {
            warning("AutoEscape triggered: %s.", reason);
            nextAction = System.currentTimeMillis() + cooldown.get() * 1000L;
            if (disableAfter.get() && isActive()) toggle();
        }
    }

    private double nearestThreatDistance() {
        double nearest = Double.MAX_VALUE;
        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player || player.isRemoved() || Friends.get().isFriend(player)) continue;
            nearest = Math.min(nearest, mc.player.distanceTo(player));
        }
        return nearest;
    }
}
