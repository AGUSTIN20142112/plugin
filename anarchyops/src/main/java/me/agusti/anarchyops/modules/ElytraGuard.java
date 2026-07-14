package me.agusti.anarchyops.modules;

import me.agusti.anarchyops.AnarchyOps;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;

public final class ElytraGuard extends Module {
    private final SettingGroup sg = settings.getDefaultGroup();
    private final Setting<Integer> minDurability = sg.add(new IntSetting.Builder().name("minimum-durability").description("Swap before the equipped elytra reaches this remaining durability.").defaultValue(15).min(1).sliderMax(100).build());
    private final Setting<Boolean> autoSwap = sg.add(new BoolSetting.Builder().name("auto-swap").defaultValue(true).build());
    private final Setting<Boolean> warn = sg.add(new BoolSetting.Builder().name("warn").defaultValue(true).build());
    private int ticks;
    private boolean warned;

    public ElytraGuard() { super(AnarchyOps.CATEGORY, "elytra-guard", "Warns and swaps a nearly broken elytra for the best spare."); }

    @EventHandler private void onTick(TickEvent.Post event) {
        if (mc.player == null || ++ticks % 10 != 0) return;
        ItemStack equipped = mc.player.getEquippedStack(EquipmentSlot.CHEST);
        if (!equipped.contains(DataComponentTypes.GLIDER) || !equipped.isDamageable()) { warned = false; return; }
        int remaining = equipped.getMaxDamage() - equipped.getDamage();
        if (remaining > minDurability.get()) { warned = false; return; }
        int bestSlot = -1;
        int bestRemaining = remaining;
        for (int i = 0; i < mc.player.getInventory().getMainStacks().size(); i++) {
            ItemStack stack = mc.player.getInventory().getMainStacks().get(i);
            if (!stack.contains(DataComponentTypes.GLIDER) || !stack.isDamageable()) continue;
            int candidate = stack.getMaxDamage() - stack.getDamage();
            if (candidate > bestRemaining) { bestRemaining = candidate; bestSlot = i; }
        }
        if (autoSwap.get() && bestSlot >= 0) {
            InvUtils.move().from(bestSlot).toArmor(2);
            info("Swapped elytra: %d durability remaining.", bestRemaining);
            warned = false;
        } else if (warn.get() && !warned) {
            warning("Elytra critical: %d durability remaining%s.", remaining, bestSlot < 0 ? " and no better spare was found" : "");
            warned = true;
        }
    }
}
