package net.mca.network.c2s;

import net.mca.cobalt.network.Message;
import net.mca.server.world.data.Building;
import net.mca.server.world.data.GraveyardManager;
import net.mca.server.world.data.Village;
import net.mca.server.world.data.VillageManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Optional;

public class ReportBuildingMessage implements Message {
    private static final long serialVersionUID = 3510050513221709603L;

    private final Action action;
    private final String data;

    public ReportBuildingMessage(Action action, String data) {
        this.action = action;
        this.data = data;
    }

    public ReportBuildingMessage(Action action) {
        this(action, null);
    }

    @Override
    public void receive(ServerPlayerEntity player) {
        VillageManager villages = VillageManager.get(player.getServerWorld());
        switch (action) {
            case ADD:
            case ADD_ROOM:
                Building.validationResult result = villages.processBuilding(player.getBlockPos(), true, action == Action.ADD_ROOM);
                player.sendMessage(new TranslatableText("blueprint.scan." + result.name().toLowerCase(Locale.ENGLISH)), true);

                // also add tombstones
                GraveyardManager.get(player.getServerWorld()).reportToVillageManager(player);
                break;
            case AUTO_SCAN:
                villages.findNearestVillage(player).ifPresent(Village::toggleAutoScan);
                break;
            case FULL_SCAN:
                villages.findNearestVillage(player).ifPresent(buildings ->
                    new ArrayList<>(buildings.getBuildings().values()).forEach(b ->
                            villages.processBuilding(b.getCenter(), true, false)
                    )
                );
                break;
            case FORCE_TYPE:
            case REMOVE:
                Optional<Village> village = villages.findNearestVillage(player);
                Optional<Building> building = village.flatMap(v -> v.getBuildings().values().stream().filter((b) ->
                        b.containsPos(player.getBlockPos())).findAny());
                if (building.isPresent()) {
                    if (action == Action.FORCE_TYPE) {
                        if (building.get().getType().equals(data)) {
                            building.get().determineType();
                        } else {
                            building.get().setForcedType(data);
                        }
                    } else {
                        village.get().removeBuilding(building.get().getId());
                        village.get().markDirty(player.getServerWorld());
                    }
                } else {
                    player.sendMessage(new TranslatableText("blueprint.noBuilding"), true);
                }
                break;
        }
    }

    public enum Action {
        AUTO_SCAN,
        ADD_ROOM,
        ADD,
        REMOVE,
        FORCE_TYPE,
        FULL_SCAN
    }
}
