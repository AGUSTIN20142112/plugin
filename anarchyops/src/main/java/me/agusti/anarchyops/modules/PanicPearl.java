package me.agusti.anarchyops.modules;

import me.agusti.anarchyops.AnarchyOps;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

public final class PanicPearl extends Module {
    public PanicPearl() {
        super(AnarchyOps.CATEGORY, "panic-pearl", "Throws an ender pearl from the hotbar and switches back. Bind it to a key.");
    }

    @Override
    public void onActivate() {
        throwPearl();
        if (isActive()) toggle();
    }

    public boolean throwPearl() {
        if (mc.player == null || mc.interactionManager == null) return false;
        int pearlSlot = -1;
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).isOf(Items.ENDER_PEARL)) {
                pearlSlot = i;
                break;
            }
        }
        if (pearlSlot < 0) {
            warning("No ender pearl in hotbar.");
            return false;
        }
        int previous = mc.player.getInventory().getSelectedSlot();
        InvUtils.swap(pearlSlot, false);
        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        InvUtils.swap(previous, false);
        return true;
    }

    @Override
    public void sendToggledMsg() {
        if (Config.get().chatFeedback.get() && chatFeedback && isActive()) info("Triggered panic pearl.");
    }
}
