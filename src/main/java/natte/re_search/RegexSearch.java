package natte.re_search;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.Item;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import natte.re_search.config.Config;
import natte.re_search.network.ItemSearchResultPacketS2C;
import natte.re_search.network.NetworkingConstants;
import natte.re_search.network.Searcher;

public class RegexSearch implements ModInitializer {

	public static final String MOD_ID = "re_search";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final Item ARROW_ITEM = new Item(new FabricItemSettings());

	@Override
	public void onInitialize() {

		Config.init(MOD_ID, Config.class);

		Registry.register(Registries.ITEM, new Identifier(MOD_ID, "arrow_item"), ARROW_ITEM);

		ServerPlayConnectionEvents.INIT.register((handler, server) -> {
			ServerPlayNetworking.registerReceiver(handler, NetworkingConstants.ITEM_SEARCH_PACKET_ID,
					RegexSearch::receive);
		});

	}

	private static void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
		
		String expression = buf.readString();

		server.execute(() -> {
			List<MarkedInventory> inventories = Searcher.search(expression, player);
			
			PacketByteBuf packet = ItemSearchResultPacketS2C.createPackedByteBuf(inventories);
			responseSender.sendPacket(NetworkingConstants.ITEM_SEARCH_RESULT_PACKET_ID, packet);
			
			if(inventories.isEmpty()){
				player.sendMessage( net.minecraft.text.Text.translatable("popup.re_search.no_matching_items_found"), true);
			}
		});
	}
}