package me.agusti.anarchyops;

import me.agusti.anarchyops.modules.DurabilityGuard;
import me.agusti.anarchyops.modules.ElytraGuard;
import me.agusti.anarchyops.modules.PanicPearl;
import me.agusti.anarchyops.modules.SupplyGuard;
import me.agusti.anarchyops.modules.ThreatRadar;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AnarchyOps extends MeteorAddon {
    public static final Logger LOG = LoggerFactory.getLogger("AnarchyOps");
    public static final Category CATEGORY = new Category("AnarchyOps");

    @Override
    public void onInitialize() {
        Modules.get().add(new ThreatRadar());
        Modules.get().add(new ElytraGuard());
        Modules.get().add(new SupplyGuard());
        Modules.get().add(new DurabilityGuard());
        Modules.get().add(new PanicPearl());
        LOG.info("AnarchyOps 0.1.0 initialized.");
    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(CATEGORY);
    }

    @Override
    public String getPackage() {
        return "me.agusti.anarchyops";
    }
}
