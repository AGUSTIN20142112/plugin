package me.agusti.anarchyops.hud;

import me.agusti.anarchyops.AnarchyOps;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

public final class OpsHud extends HudElement {
    public static final HudElementInfo<OpsHud> INFO = new HudElementInfo<>(AnarchyOps.HUD_GROUP, "ops-session", "Session statistics, health and critical supplies.", OpsHud::new);

    public OpsHud() { super(INFO); }

    @Override
    public void render(HudRenderer renderer) {
        if (AnarchyOps.SESSION == null) return;
        long seconds = AnarchyOps.SESSION.elapsedSeconds();
        String health = mc.player == null ? "--" : String.format("%.1f", mc.player.getHealth() + mc.player.getAbsorptionAmount());
        String[] lines = {
            String.format("AnarchyOps 0.2 | %02d:%02d:%02d", seconds / 3600, (seconds / 60) % 60, seconds % 60),
            String.format("Health %s | Travel %.1f km | Max %.0fm", health, AnarchyOps.SESSION.distance() / 1000.0, AnarchyOps.SESSION.maxDistanceFromStart()),
            String.format("Totems %d | Rockets %d | Pearls %d", count(Items.TOTEM_OF_UNDYING), count(Items.FIREWORK_ROCKET), count(Items.ENDER_PEARL)),
            String.format("Pops %d | Deaths %d | Seen %d", AnarchyOps.SESSION.totemPops(), AnarchyOps.SESSION.deaths(), AnarchyOps.SESSION.playersSeen())
        };
        double width = 0;
        double height = 0;
        for (String line : lines) {
            width = Math.max(width, renderer.textWidth(line, true));
            height += renderer.textHeight(true);
        }
        setSize(width, height);
        double yy = y;
        for (String line : lines) {
            renderer.text(line, x, yy, Color.WHITE, true);
            yy += renderer.textHeight(true);
        }
    }

    private int count(Item item) {
        return mc.player == null ? 0 : InvUtils.find(item).count();
    }
}
