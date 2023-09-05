package natte.re_search.network;

import natte.re_search.RegexSearch;
import natte.re_search.search.SearchOptions;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class ItemSearchPacketC2S {

    public static final Identifier PACKET_ID = new Identifier(RegexSearch.MOD_ID, "item_search");


    public static PacketByteBuf createPackedByteBuf(SearchOptions options){        
        return options.createPacketByteBuf();
    }    
}
