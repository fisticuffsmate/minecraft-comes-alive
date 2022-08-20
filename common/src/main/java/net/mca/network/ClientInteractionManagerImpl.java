package net.mca.network;

import net.mca.MCAClient;
import net.mca.client.book.Book;
import net.mca.client.gui.*;
import net.mca.entity.EntitiesMCA;
import net.mca.entity.VillagerEntityMCA;
import net.mca.entity.VillagerLike;
import net.mca.item.BabyItem;
import net.mca.item.ExtendedWrittenBookItem;
import net.mca.network.s2c.*;
import net.mca.server.world.data.BabyTracker;
import net.mca.server.world.data.Village;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

import java.util.Optional;

public class ClientInteractionManagerImpl implements ClientInteractionManager {
    private final MinecraftClient client = MinecraftClient.getInstance();

    @Override
    public void handleGuiRequest(OpenGuiRequest message) {
        Entity entity;
        assert client.world != null;
        assert MinecraftClient.getInstance().player != null;
        switch (message.getGui()) {
            case WHISTLE:
                client.openScreen(new WhistleScreen());
                break;
            case BOOK:
                if (client.player != null) {
                    ItemStack item = client.player.getStackInHand(Hand.MAIN_HAND);
                    if (item.getItem() instanceof ExtendedWrittenBookItem) {
                        ExtendedWrittenBookItem bookItem = (ExtendedWrittenBookItem) item.getItem();
                        Book book = bookItem.getBook(item);
                        client.openScreen(new ExtendedBookScreen(book));
                    }
                }
                break;
            case BLUEPRINT:
                client.openScreen(new BlueprintScreen());
                break;
            case INTERACT:
                VillagerLike<?> villager = (VillagerLike<?>)client.world.getEntityById(message.villager);
                client.openScreen(new InteractScreen(villager));
                break;
            case VILLAGER_EDITOR:
                entity = client.world.getEntityById(message.villager);
                assert entity != null;
                client.openScreen(new VillagerEditorScreen(entity.getUuid(), MinecraftClient.getInstance().player.getUuid()));
                break;
            case LIMITED_VILLAGER_EDITOR:
                entity = client.world.getEntityById(message.villager);
                assert entity != null;
                client.openScreen(new LimitedVillagerEditorScreen(entity.getUuid(), MinecraftClient.getInstance().player.getUuid()));
                break;
            case NEEDLE_AND_THREAD:
                entity = client.world.getEntityById(message.villager);
                if (entity == null) {
                    client.openScreen(new NeedleScreen(MinecraftClient.getInstance().player.getUuid()));
                } else {
                    client.openScreen(new NeedleScreen(entity.getUuid(), MinecraftClient.getInstance().player.getUuid()));
                }
                break;
            case COMB:
                entity = client.world.getEntityById(message.villager);
                if (entity == null) {
                    client.openScreen(new CombScreen(MinecraftClient.getInstance().player.getUuid()));
                } else {
                    client.openScreen(new CombScreen(entity.getUuid(), MinecraftClient.getInstance().player.getUuid()));
                }
                break;
            case BABY_NAME:
                if (client.player != null) {
                    ItemStack item = client.player.getStackInHand(Hand.MAIN_HAND);
                    if (item.getItem() instanceof BabyItem) {
                        client.openScreen(new NameBabyScreen(client.player, item));
                    }
                }
                break;
            case FAMILY_TREE:
                client.openScreen(new FamilyTreeSearchScreen());
                break;
            default:
        }
    }

    @Override
    public void handleFamilyTreeResponse(GetFamilyTreeResponse message) {
        Screen screen = client.currentScreen;
        if (screen instanceof FamilyTreeScreen) {
            FamilyTreeScreen gui = (FamilyTreeScreen) screen;
            gui.setFamilyData(message.uuid, message.family);
        }
    }

    @Override
    public void handleInteractDataResponse(GetInteractDataResponse message) {
        Screen screen = client.currentScreen;
        if (screen instanceof InteractScreen) {
            InteractScreen gui = (InteractScreen) screen;
            gui.setConstraints(message.constraints);
            gui.setParents(message.father, message.mother);
            gui.setSpouse(message.marriageState, message.spouse);
        }
    }

    @Override
    public void handleVillageDataResponse(GetVillageResponse message) {
        Screen screen = client.currentScreen;
        if (screen instanceof BlueprintScreen) {
            Village village = new Village();
            village.load(message.getData());

            BlueprintScreen gui = (BlueprintScreen) screen;
            gui.setVillage(village);
            gui.setRank(message.rank, message.reputation, message.isVillage, message.ids, message.tasks, message.buildingTypes);
        }
    }

    @Override
    public void handleVillageDataFailedResponse(GetVillageFailedResponse message) {
        Screen screen = client.currentScreen;
        if (screen instanceof BlueprintScreen) {
            BlueprintScreen gui = (BlueprintScreen) screen;
            gui.setVillage(null);
        }
    }

    @Override
    public void handleFamilyDataResponse(GetFamilyResponse message) {
        Screen screen = client.currentScreen;
        if (screen instanceof WhistleScreen) {
            WhistleScreen gui = (WhistleScreen) screen;
            gui.setVillagerData(message.getData());
        }
    }

    @Override
    public void handleVillagerDataResponse(GetVillagerResponse message) {
        Screen screen = client.currentScreen;
        if (screen instanceof VillagerEditorScreen) {
            VillagerEditorScreen gui = (VillagerEditorScreen) screen;
            gui.setVillagerData(message.getData());
        }
    }

    @Override
    public void handleDialogueResponse(InteractionDialogueResponse message) {
        Screen screen = client.currentScreen;
        if (screen instanceof InteractScreen) {
            InteractScreen gui = (InteractScreen) screen;
            gui.setDialogue(message.question, message.answers, message.silent);
        }
    }

    @Override
    public void handleChildData(GetChildDataResponse message) {
        BabyItem.CLIENT_STATE_CACHE.put(message.id, Optional.ofNullable(message.getData()).map(BabyTracker.ChildSaveState::new));
    }

    @Override
    public void handleSkinListResponse(AnalysisResults message) {
        InteractScreen.setAnalysis(message.analysis);
    }

    @Override
    public void handleBabyNameResponse(BabyNameResponse message) {
        Screen screen = client.currentScreen;
        if (screen instanceof NameBabyScreen) {
            NameBabyScreen gui = (NameBabyScreen) screen;
            gui.setBabyName(message.getName());
        }
    }

    @Override
    public void handleVillagerNameResponse(VillagerNameResponse message) {
        Screen screen = client.currentScreen;
        if (screen instanceof VillagerEditorScreen) {
            VillagerEditorScreen gui = (VillagerEditorScreen) screen;
            gui.setVillagerName(message.getName());
        }
    }

    @Override
    public void handleToastMessage(ShowToastRequest message) {
        SystemToast.add(client.getToastManager(), SystemToast.Type.TUTORIAL_HINT, message.getTitle(), message.getMessage());
    }

    @Override
    public void handleFamilyTreeUUIDResponse(FamilyTreeUUIDResponse response) {
        Screen screen = client.currentScreen;
        if (screen instanceof FamilyTreeSearchScreen) {
            FamilyTreeSearchScreen gui = (FamilyTreeSearchScreen) screen;
            gui.setList(response.getList());
        }
    }

    @Override
    public void handlePlayerDataMessage(PlayerDataMessage response) {
        VillagerEntityMCA villager = EntitiesMCA.MALE_VILLAGER.get().create(MinecraftClient.getInstance().world);
        assert villager != null;
        villager.readCustomDataFromNbt(response.getData());
        MCAClient.playerData.put(response.uuid, villager);
    }

    @Override
    public void handleSkinListResponse(SkinListResponse message) {
        Screen screen = client.currentScreen;
        if (screen instanceof VillagerEditorScreen) {
            VillagerEditorScreen gui = (VillagerEditorScreen) screen;
            gui.setSkinList(message.getClothing(), message.getHair());
        }
    }

    @Override
    public void handleDestinyGuiRequest(OpenDestinyGuiRequest message) {
        MCAClient.getDestinyManager().requestOpen(
                message.allowTeleportation,
                message.allowPlayerModel,
                message.allowVillagerModel
        );
    }
}
