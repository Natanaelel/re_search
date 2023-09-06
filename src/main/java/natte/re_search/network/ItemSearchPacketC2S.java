package natte.re_search.network;

import natte.re_search.RegexSearch;
import natte.re_search.search.SearchOptions;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.util.Identifier;

public class ItemSearchPacketC2S {

    public static final Identifier PACKET_ID = new Identifier(RegexSearch.MOD_ID, "item_search");


    public static void send(SearchOptions options){
        ClientPlayNetworking.send(ItemSearchPacketC2S.PACKET_ID, options.createPacketByteBuf());
    }
}
