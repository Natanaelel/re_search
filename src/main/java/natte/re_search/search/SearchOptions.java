package natte.re_search.search;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;

public class SearchOptions {
    public String expression;
    public boolean isCaseSensitive;

    public SearchOptions(String expression, boolean isCaseSensitive){
        this.expression = expression;
        this.isCaseSensitive = isCaseSensitive;
    }


    public PacketByteBuf createPacketByteBuf(){
        PacketByteBuf packet = PacketByteBufs.create();
        packet.writeString(expression);
        packet.writeBoolean(isCaseSensitive);
        return packet;
    }

    public static SearchOptions readPacketByteBuf(PacketByteBuf packet){
        String expression = packet.readString();
        boolean isCaseSensitive = packet.readBoolean();
        return new SearchOptions(expression, isCaseSensitive);
    }

}
