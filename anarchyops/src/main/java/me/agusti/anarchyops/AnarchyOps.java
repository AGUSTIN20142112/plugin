package me.agusti.anarchyops;

import me.agusti.anarchyops.commands.OpsCommand;
import me.agusti.anarchyops.hud.NavigationHud;
import me.agusti.anarchyops.hud.OpsHud;
import me.agusti.anarchyops.hud.ThreatHud;
import me.agusti.anarchyops.modules.*;
import me.agusti.anarchyops.services.MarkerStore;
import me.agusti.anarchyops.services.NavigationService;
import me.agusti.anarchyops.services.SessionTracker;
import me.agusti.anarchyops.services.SupplySnapshotStore;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudGroup;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public final class AnarchyOps extends MeteorAddon {
    public static final Logger LOG = LoggerFactory.getLogger("AnarchyOps");
    public static final Category CATEGORY = new Category("AnarchyOps");
    public static final HudGroup HUD_GROUP = new HudGroup("AnarchyOps");

    public static MarkerStore MARKERS;
    public static SupplySnapshotStore SNAPSHOTS;
    public static NavigationService NAVIGATION;
    public static SessionTracker SESSION;

    @Override
    public void onInitialize() {
        Path data = FabricLoader.getInstance().getGameDir().resolve("meteor-client").resolve("anarchyops");

        MARKERS = new MarkerStore(data.resolve("markers.tsv"));
        MARKERS.load();
        SNAPSHOTS = new SupplySnapshotStore(data.resolve("supply-snapshots.tsv"));
        SNAPSHOTS.load();
        NAVIGATION = new NavigationService(MARKERS);
        SESSION = new SessionTracker(MARKERS);
        MeteorClient.EVENT_BUS.subscribe(SESSION);

        Modules.get().add(new ThreatRadar());
        Modules.get().add(new AutoEscape());
        Modules.get().add(new VoidGuard());
        Modules.get().add(new ElytraGuard());
        Modules.get().add(new SupplyGuard());
        Modules.get().add(new DurabilityGuard());
        Modules.get().add(new RocketRestock());
        Modules.get().add(new PanicPearl());
        Modules.get().add(new MarkerEsp());

        Commands.add(new OpsCommand());
        Hud.get().register(OpsHud.INFO);
        Hud.get().register(ThreatHud.INFO);
        Hud.get().register(NavigationHud.INFO);

        LOG.info("AnarchyOps 0.2.0 initialized with 9 modules, 3 HUD elements and persistent navigation data.");
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
