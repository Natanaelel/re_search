package natte.re_search.network;

import natte.re_search.search.SearchOptions;
import net.minecraft.network.PacketByteBuf;

public class ItemSearchPacketC2S {
    public static PacketByteBuf createPackedByteBuf(SearchOptions options){        
        return options.createPacketByteBuf();
    }    
}
