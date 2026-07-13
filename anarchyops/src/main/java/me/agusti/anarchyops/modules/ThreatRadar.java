package me.agusti.anarchyops.modules;

import me.agusti.anarchyops.AnarchyOps;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;
import java.util.ArrayList;
import java.util.List;

public final class ThreatRadar extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender = settings.createGroup("Render");
    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder().name("range").description("Detection range.").defaultValue(64).min(4).sliderMax(256).build());
    private final Setting<Boolean> ignoreFriends = sgGeneral.add(new BoolSetting.Builder().name("ignore-friends").defaultValue(true).build());
    private final Setting<Boolean> chatAlerts = sgGeneral.add(new BoolSetting.Builder().name("chat-alerts").defaultValue(true).build());
    private final Setting<Integer> alertCooldown = sgGeneral.add(new IntSetting.Builder().name("alert-cooldown").description("Seconds between alerts.").defaultValue(10).min(1).sliderMax(60).build());
    private final Setting<Boolean> render = sgRender.add(new BoolSetting.Builder().name("render-boxes").defaultValue(true).build());
    private final Setting<SettingColor> color = sgRender.add(new ColorSetting.Builder().name("color").defaultValue(new SettingColor(255, 60, 60, 70)).build());
    private final List<PlayerEntity> threats = new ArrayList<>();
    private long nextAlert;

    public ThreatRadar() { super(AnarchyOps.CATEGORY, "threat-radar", "Detects nearby non-friend players and highlights them."); }

    @Override public void onDeactivate() { threats.clear(); }

    @EventHandler private void onTick(TickEvent.Post event) {
        if (mc.player == null || mc.world == null) return;
        threats.clear();
        PlayerEntity nearest = null;
        double best = Double.MAX_VALUE;
        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player || player.isRemoved()) continue;
            if (ignoreFriends.get() && Friends.get().isFriend(player)) continue;
            double distance = mc.player.distanceTo(player);
            if (distance > range.get()) continue;
            threats.add(player);
            if (distance < best) { best = distance; nearest = player; }
        }
        if (nearest != null && chatAlerts.get() && System.currentTimeMillis() >= nextAlert) {
            warning("Threat: %s at %.1f blocks (%d total).", nearest.getName().getString(), best, threats.size());
            nextAlert = System.currentTimeMillis() + alertCooldown.get() * 1000L;
        }
    }

    @EventHandler private void onRender(Render3DEvent event) {
        if (!render.get()) return;
        for (PlayerEntity player : threats) event.renderer.box(player.getBoundingBox(), color.get(), color.get(), ShapeMode.Both, 0);
    }
}
