package me.agusti.anarchyops.modules;

import me.agusti.anarchyops.AnarchyOps;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.ItemStack;

public final class DurabilityGuard extends Module {
    private final SettingGroup sg = settings.getDefaultGroup();
    private final Setting<Integer> minimum = sg.add(new IntSetting.Builder().name("minimum-durability").defaultValue(10).min(1).sliderMax(100).build());
    private final Setting<Boolean> autoReplace = sg.add(new BoolSetting.Builder().name("auto-replace-held-item").defaultValue(true).build());
    private int ticks;
    private boolean warned;

    public DurabilityGuard() { super(AnarchyOps.CATEGORY, "durability-guard", "Protects the held tool by replacing it with a healthier identical item."); }

    @EventHandler private void onTick(TickEvent.Post event) {
        if (mc.player == null || ++ticks % 10 != 0) return;
        int selected = mc.player.getInventory().getSelectedSlot();
        ItemStack held = mc.player.getInventory().getStack(selected);
        if (!held.isDamageable()) { warned = false; return; }
        int remaining = held.getMaxDamage() - held.getDamage();
        if (remaining > minimum.get()) { warned = false; return; }
        int bestSlot = -1;
        int best = remaining;
        for (int i = 9; i < mc.player.getInventory().size(); i++) {
            ItemStack candidate = mc.player.getInventory().getStack(i);
            if (!candidate.isOf(held.getItem()) || !candidate.isDamageable()) continue;
            int candidateRemaining = candidate.getMaxDamage() - candidate.getDamage();
            if (candidateRemaining > best) { best = candidateRemaining; bestSlot = i; }
        }
        if (autoReplace.get() && bestSlot >= 0) {
            InvUtils.move().from(bestSlot).toHotbar(selected);
            info("Replaced held item with a spare at %d durability.", best);
            warned = false;
        } else if (!warned) {
            warning("Held item is nearly broken: %d durability remaining.", remaining);
            warned = true;
        }
    }
}
