package me.agusti.anarchyops.hud;

import me.agusti.anarchyops.AnarchyOps;
import me.agusti.anarchyops.modules.ThreatRadar;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;

public final class ThreatHud extends HudElement {
    public static final HudElementInfo<ThreatHud> INFO = new HudElementInfo<>(AnarchyOps.HUD_GROUP, "threat-radar", "Shows the nearest detected threat, distance and health.", ThreatHud::new);

    public ThreatHud() { super(INFO); }

    @Override
    public void render(HudRenderer renderer) {
        String text = ThreatRadar.nearestDistance < 0
            ? "Threats: clear"
            : String.format("Threats: %d | %s %.1fm | %.1f HP", ThreatRadar.threatCount, ThreatRadar.nearestName, ThreatRadar.nearestDistance, ThreatRadar.nearestHealth);
        setSize(renderer.textWidth(text, true), renderer.textHeight(true));
        Color color = ThreatRadar.nearestDistance < 0 ? Color.GREEN : ThreatRadar.danger ? Color.RED : Color.ORANGE;
        renderer.text(text, x, y, color, true);
    }
}
