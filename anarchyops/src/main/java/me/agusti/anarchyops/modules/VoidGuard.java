package me.agusti.anarchyops.modules;

import me.agusti.anarchyops.AnarchyOps;
import me.agusti.anarchyops.services.Marker;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.s2c.common.DisconnectS2CPacket;
import net.minecraft.text.Text;

public final class VoidGuard extends Module {
    private final SettingGroup sg = settings.getDefaultGroup();
    private final Setting<Double> triggerY = sg.add(new DoubleSetting.Builder()
        .name("trigger-y").description("Triggers below this Y level.")
        .defaultValue(-45).range(-100, 20).sliderRange(-100, 20).build());
    private final Setting<Boolean> onlyEnd = sg.add(new BoolSetting.Builder()
        .name("only-end").defaultValue(true).build());
    private final Setting<Boolean> usePearl = sg.add(new BoolSetting.Builder()
        .name("use-pearl").defaultValue(true).build());
    private final Setting<Boolean> disconnectFallback = sg.add(new BoolSetting.Builder()
        .name("disconnect-fallback").defaultValue(true).build());
    private final Setting<Boolean> saveMarker = sg.add(new BoolSetting.Builder()
        .name("save-marker").defaultValue(true).build());

    private boolean triggered;

    public VoidGuard() {
        super(AnarchyOps.CATEGORY, "void-guard", "Attempts a panic pearl or disconnects when falling into the void.");
    }

    @Override
    public void onActivate() { triggered = false; }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null || mc.world == null || triggered) return;
        String dimension = mc.world.getRegistryKey().getValue().toString();
        if (onlyEnd.get() && !dimension.endsWith("the_end")) return;
        if (mc.player.getY() >= triggerY.get()) return;

        triggered = true;
        if (saveMarker.get() && AnarchyOps.MARKERS != null) {
            long now = System.currentTimeMillis();
            AnarchyOps.MARKERS.add(new Marker("void-" + now, dimension, mc.player.getBlockX(), mc.player.getBlockY(), mc.player.getBlockZ(), now));
        }
        boolean escaped = usePearl.get() && Modules.get().get(PanicPearl.class).throwPearl();
        if (!escaped && disconnectFallback.get()) {
            mc.player.networkHandler.onDisconnect(new DisconnectS2CPacket(Text.literal("[AnarchyOps] VoidGuard triggered below Y " + triggerY.get())));
        }
        warning("VoidGuard triggered at Y %.1f.", mc.player.getY());
        if (isActive()) toggle();
    }
}
