package me.agusti.anarchyops.modules;

import me.agusti.anarchyops.AnarchyOps;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

public final class PanicPearl extends Module {
    public PanicPearl() { super(AnarchyOps.CATEGORY, "panic-pearl", "Throws an ender pearl from the hotbar and switches back. Bind it to a key."); }
    @Override public void onActivate() {
        if (mc.player == null || mc.interactionManager == null) { toggle(); return; }
        FindItemResult pearl = InvUtils.findInHotbar(Items.ENDER_PEARL);
        if (!pearl.found()) { warning("No ender pearl in hotbar."); toggle(); return; }
        int previous = mc.player.getInventory().getSelectedSlot();
        InvUtils.swap(pearl.slot(), false);
        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        InvUtils.swap(previous, false);
        toggle();
    }
    @Override public void sendToggledMsg() {
        if (Config.get().chatFeedback.get() && chatFeedback && isActive()) info("Triggered panic pearl.");
    }
}
