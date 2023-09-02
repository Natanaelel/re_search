package natte.re_search.search;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;

public class SearchOptions {
    public String expression;
    public boolean isCaseSensitive;
    public int searchMode;

    public SearchOptions(String expression, boolean isCaseSensitive, int searchMode){
        this.expression = expression;
        this.isCaseSensitive = isCaseSensitive;
        this.searchMode = searchMode;
    }


    public PacketByteBuf createPacketByteBuf(){
        PacketByteBuf packet = PacketByteBufs.create();
        packet.writeString(expression);
        packet.writeBoolean(isCaseSensitive);
        packet.writeInt(searchMode);
        return packet;
    }

    public static SearchOptions readPacketByteBuf(PacketByteBuf packet){
        String expression = packet.readString();
        boolean isCaseSensitive = packet.readBoolean();
        int searchMode = packet.readInt();
        return new SearchOptions(expression, isCaseSensitive, searchMode);
    }

}
