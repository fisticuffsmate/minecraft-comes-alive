package net.mca.network.s2c;

import net.mca.ClientProxy;
import net.mca.network.NbtDataMessage;
import net.minecraft.nbt.NbtCompound;

public class GetFamilyResponse extends NbtDataMessage {
    private static final long serialVersionUID = -8537919427646877115L;

    public GetFamilyResponse(NbtCompound data) {
        super(data);
    }

    @Override
    public void receive() {
        ClientProxy.getNetworkHandler().handleFamilyDataResponse(this);
    }
}
