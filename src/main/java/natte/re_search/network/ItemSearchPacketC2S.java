package natte.re_search.network;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;

public class ItemSearchPacketC2S {
    public static PacketByteBuf createPackedByteBuf(String expression){
        PacketByteBuf packet = PacketByteBufs.create();
        packet.writeString(expression);
        return packet;
    }

    
}
