package me.agusti.anarchyops.modules;

import me.agusti.anarchyops.AnarchyOps;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import java.util.HashMap;
import java.util.Map;

public final class SupplyGuard extends Module {
    private final SettingGroup sg = settings.getDefaultGroup();
    private final Setting<Integer> totems = sg.add(new IntSetting.Builder().name("minimum-totems").defaultValue(2).min(0).sliderMax(20).build());
    private final Setting<Integer> rockets = sg.add(new IntSetting.Builder().name("minimum-rockets").defaultValue(32).min(0).sliderMax(256).build());
    private final Setting<Integer> pearls = sg.add(new IntSetting.Builder().name("minimum-pearls").defaultValue(4).min(0).sliderMax(64).build());
    private final Setting<Integer> obsidian = sg.add(new IntSetting.Builder().name("minimum-obsidian").defaultValue(32).min(0).sliderMax(256).build());
    private final Setting<Integer> crystals = sg.add(new IntSetting.Builder().name("minimum-crystals").defaultValue(16).min(0).sliderMax(128).build());
    private final Setting<Integer> cooldown = sg.add(new IntSetting.Builder().name("warning-cooldown").description("Seconds between repeated warnings.").defaultValue(30).min(5).sliderMax(120).build());
    private final Map<Item, Long> nextWarning = new HashMap<>();
    private int ticks;

    public SupplyGuard() { super(AnarchyOps.CATEGORY, "supply-guard", "Warns when important anarchy supplies fall below configured levels."); }

    @EventHandler private void onTick(TickEvent.Post event) {
        if (mc.player == null || ++ticks % 20 != 0) return;
        check(Items.TOTEM_OF_UNDYING, "totems", totems.get());
        check(Items.FIREWORK_ROCKET, "rockets", rockets.get());
        check(Items.ENDER_PEARL, "pearls", pearls.get());
        check(Items.OBSIDIAN, "obsidian", obsidian.get());
        check(Items.END_CRYSTAL, "end crystals", crystals.get());
    }

    private void check(Item item, String name, int minimum) {
        if (minimum <= 0) return;
        int count = InvUtils.find(item).count();
        long now = System.currentTimeMillis();
        if (count < minimum && now >= nextWarning.getOrDefault(item, 0L)) {
            warning("Low %s: %d / %d.", name, count, minimum);
            nextWarning.put(item, now + cooldown.get() * 1000L);
        }
    }
}
