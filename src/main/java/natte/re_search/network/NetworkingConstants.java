package natte.re_search.network;

import natte.re_search.RegexSearch;
import net.minecraft.util.Identifier;

public class NetworkingConstants {
    public static final Identifier ITEM_SEARCH_PACKET_ID = new Identifier(RegexSearch.MOD_ID, "item_search");

    public static final Identifier ITEM_SEARCH_RESULT_PACKET_ID = new Identifier(RegexSearch.MOD_ID, "item_search_result");

}
