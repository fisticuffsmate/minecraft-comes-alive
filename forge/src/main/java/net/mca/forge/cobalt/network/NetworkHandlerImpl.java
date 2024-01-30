package net.mca.forge.cobalt.network;

import net.mca.MCA;
import net.mca.cobalt.network.Message;
import net.mca.cobalt.network.NetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraftforge.network.Channel;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;

public class NetworkHandlerImpl extends NetworkHandler.Impl {
    private static final int PROTOCOL_VERSION = 1;

    private final SimpleChannel channel = ChannelBuilder.named(new Identifier(MCA.MOD_ID, "main"))
            .networkProtocolVersion(PROTOCOL_VERSION)
            .acceptedVersions(Channel.VersionTest.exact(PROTOCOL_VERSION))
            .simpleChannel();

    private int id = 0;

    @Override
    public <T extends Message> void registerMessage(Class<T> msg) {
        this.channel.messageBuilder(msg, id++)
                .encoder(Message::encode)
                .decoder(b -> (T) Message.decode(b))
                .consumerNetworkThread((m, ctx) -> {
                    ctx.enqueueWork(() -> {
                        ServerPlayerEntity sender = ctx.getSender();
                        if (sender == null) {
                            m.receive();
                        } else {
                            m.receive(sender);
                        }
                    });
                    ctx.setPacketHandled(true);
                })
                .add();
    }

    @Override
    public void sendToServer(Message m) {
        channel.send(m, PacketDistributor.SERVER.noArg());
    }

    @Override
    public void sendToPlayer(Message m, ServerPlayerEntity e) {
        channel.send(m, PacketDistributor.PLAYER.with(e));
    }
}