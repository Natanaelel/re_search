package natte.re_search.search;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;

public class SearchOptions {

    public String expression;
    public boolean isCaseSensitive;
    public int searchMode;
    public boolean searchBlocks;
    public boolean searchEntities;

    public SearchOptions(String expression, boolean isCaseSensitive, int searchMode, boolean searchBlocks,
            boolean searchEntities) {

        this.expression = expression;
        this.isCaseSensitive = isCaseSensitive;
        this.searchMode = searchMode;
        this.searchBlocks = searchBlocks;
        this.searchEntities = searchEntities;

    }

    public PacketByteBuf createPacketByteBuf() {
        PacketByteBuf packet = PacketByteBufs.create();
        packet.writeString(expression);
        packet.writeBoolean(isCaseSensitive);
        packet.writeInt(searchMode);
        packet.writeBoolean(searchBlocks);
        packet.writeBoolean(searchEntities);
        return packet;
    }

    public static SearchOptions readPacketByteBuf(PacketByteBuf packet) {
        String expression = packet.readString();
        boolean isCaseSensitive = packet.readBoolean();
        int searchMode = packet.readInt();
        boolean searchBlocks = packet.readBoolean();
        boolean searchEntities = packet.readBoolean();
        return new SearchOptions(expression, isCaseSensitive, searchMode, searchBlocks, searchEntities);
    }

}
