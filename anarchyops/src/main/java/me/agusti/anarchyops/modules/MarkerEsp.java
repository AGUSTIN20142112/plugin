package me.agusti.anarchyops.modules;

import me.agusti.anarchyops.AnarchyOps;
import me.agusti.anarchyops.services.Marker;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.Box;

public final class MarkerEsp extends Module {
    private final SettingGroup sg = settings.getDefaultGroup();
    private final Setting<Double> maxDistance = sg.add(new DoubleSetting.Builder()
        .name("max-distance").defaultValue(2000).min(16).sliderMax(10000).build());
    private final Setting<SettingColor> color = sg.add(new ColorSetting.Builder()
        .name("color").defaultValue(new SettingColor(70, 170, 255, 100)).build());
    private final Setting<SettingColor> targetColor = sg.add(new ColorSetting.Builder()
        .name("target-color").defaultValue(new SettingColor(255, 210, 40, 150)).build());

    public MarkerEsp() {
        super(AnarchyOps.CATEGORY, "marker-esp", "Renders persistent manual, escape, void and death markers.");
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (mc.player == null || mc.world == null || AnarchyOps.MARKERS == null) return;
        String dimension = mc.world.getRegistryKey().getValue().toString();
        double maxSq = maxDistance.get() * maxDistance.get();
        String targetName = AnarchyOps.NAVIGATION == null ? "" : AnarchyOps.NAVIGATION.targetName();

        for (Marker marker : AnarchyOps.MARKERS.all()) {
            if (!marker.dimension().equals(dimension)) continue;
            if (mc.player.squaredDistanceTo(marker.x() + 0.5, marker.y() + 0.5, marker.z() + 0.5) > maxSq) continue;
            SettingColor renderColor = marker.name().equalsIgnoreCase(targetName) ? targetColor.get() : color.get();
            event.renderer.box(new Box(marker.pos()), renderColor, renderColor, ShapeMode.Both, 0);
        }
    }
}
