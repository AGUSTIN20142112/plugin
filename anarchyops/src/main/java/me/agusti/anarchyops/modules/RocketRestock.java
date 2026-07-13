package me.agusti.anarchyops.modules;

import me.agusti.anarchyops.AnarchyOps;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public final class RocketRestock extends Module {
    private final SettingGroup sg = settings.getDefaultGroup();
    private final Setting<Integer> hotbarSlot = sg.add(new IntSetting.Builder()
        .name("hotbar-slot").description("Target hotbar slot, from 1 to 9.")
        .defaultValue(8).range(1, 9).sliderRange(1, 9).build());
    private final Setting<Integer> minimumCount = sg.add(new IntSetting.Builder()
        .name("minimum-count").description("Restocks when the target stack falls below this count.")
        .defaultValue(16).range(1, 63).sliderRange(1, 63).build());
    private final Setting<Boolean> protectOtherItems = sg.add(new BoolSetting.Builder()
        .name("protect-other-items").description("Does not overwrite a target slot containing another item.")
        .defaultValue(true).build());

    private int ticks;
    private boolean warnedBlocked;

    public RocketRestock() {
        super(AnarchyOps.CATEGORY, "rocket-restock", "Moves spare firework rockets into a configured hotbar slot.");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null || ++ticks % 10 != 0) return;
        int target = hotbarSlot.get() - 1;
        ItemStack targetStack = mc.player.getInventory().getStack(target);
        if (targetStack.isOf(Items.FIREWORK_ROCKET) && targetStack.getCount() >= minimumCount.get()) {
            warnedBlocked = false;
            return;
        }
        if (protectOtherItems.get() && !targetStack.isEmpty() && !targetStack.isOf(Items.FIREWORK_ROCKET)) {
            if (!warnedBlocked) warning("Rocket slot %d is occupied by another item.", hotbarSlot.get());
            warnedBlocked = true;
            return;
        }
        int source = -1;
        for (int i = 9; i < mc.player.getInventory().size(); i++) {
            if (mc.player.getInventory().getStack(i).isOf(Items.FIREWORK_ROCKET)) {
                source = i;
                break;
            }
        }
        if (source >= 0) {
            InvUtils.move().from(source).toHotbar(target);
            info("Restocked rockets into slot %d.", hotbarSlot.get());
            warnedBlocked = false;
        }
    }
}
