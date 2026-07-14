package me.agusti.anarchyops.hud;

import me.agusti.anarchyops.AnarchyOps;
import me.agusti.anarchyops.services.Marker;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;

import java.util.Optional;

public final class NavigationHud extends HudElement {
    public static final HudElementInfo<NavigationHud> INFO = new HudElementInfo<>(AnarchyOps.HUD_GROUP, "navigation-target", "Shows the active marker target, direction and distance.", NavigationHud::new);

    public NavigationHud() { super(INFO); }

    @Override
    public void render(HudRenderer renderer) {
        Optional<Marker> target = AnarchyOps.NAVIGATION == null ? Optional.empty() : AnarchyOps.NAVIGATION.target();
        String text;
        Color color;
        if (target.isEmpty()) {
            text = "Target: none";
            color = Color.GRAY;
        } else {
            Marker marker = target.get();
            double distance = AnarchyOps.NAVIGATION.distance(marker);
            if (distance >= 0) {
                text = String.format("Target: %s | %.1fm %s | %d %d %d", marker.name(), distance, AnarchyOps.NAVIGATION.direction(marker), marker.x(), marker.y(), marker.z());
                color = Color.CYAN;
            } else {
                text = String.format("Target: %s | %s | %d %d %d", marker.name(), marker.dimension(), marker.x(), marker.y(), marker.z());
                color = Color.ORANGE;
            }
        }
        setSize(renderer.textWidth(text, true), renderer.textHeight(true));
        renderer.text(text, x, y, color, true);
    }
}
