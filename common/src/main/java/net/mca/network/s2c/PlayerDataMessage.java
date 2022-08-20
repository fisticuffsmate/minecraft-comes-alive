package net.mca.network.s2c;

import net.mca.ClientProxy;
import net.mca.network.NbtDataMessage;
import net.minecraft.nbt.NbtCompound;

import java.util.UUID;

public class PlayerDataMessage extends NbtDataMessage {
    private static final long serialVersionUID = 145267688456022788L;

    public final UUID uuid;

    public PlayerDataMessage(UUID uuid, NbtCompound nbt) {
        super(nbt);
        this.uuid = uuid;
    }

    @Override
    public void receive() {
        ClientProxy.getNetworkHandler().handlePlayerDataMessage(this);
    }
}
