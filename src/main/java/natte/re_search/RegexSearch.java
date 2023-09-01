package natte.re_search;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
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
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import natte.re_search.config.Config;
import natte.re_search.network.ItemSearchResultPacketS2C;
import natte.re_search.network.NetworkingConstants;
import natte.re_search.search.MarkedInventory;
import natte.re_search.search.SearchOptions;
import natte.re_search.search.Searcher;

import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class RegexSearch implements ModInitializer {

	public static final String MOD_ID = "re_search";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final Item ARROW_ITEM = new Item(new FabricItemSettings());

	@Override
	public void onInitialize() {

		Config.init(Config.class);

		Registry.register(Registries.ITEM, new Identifier(MOD_ID, "arrow_item"), ARROW_ITEM);

		ServerPlayConnectionEvents.INIT.register((handler, server) -> {
			ServerPlayNetworking.registerReceiver(handler, NetworkingConstants.ITEM_SEARCH_PACKET_ID,
					RegexSearch::receive);
		});

		registerCommands();

	}

	private static void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
		
		// String expression = buf.readString();
		SearchOptions searchOptions = SearchOptions.readPacketByteBuf(buf);

		server.execute(() -> {
			List<MarkedInventory> inventories = Searcher.search(searchOptions, player);
			
			PacketByteBuf packet = ItemSearchResultPacketS2C.createPackedByteBuf(inventories);
			responseSender.sendPacket(NetworkingConstants.ITEM_SEARCH_RESULT_PACKET_ID, packet);
			
			if(inventories.isEmpty()){
				player.sendMessage( net.minecraft.text.Text.translatable("popup.re_search.no_matching_items_found"), true);
			}
		});
	}

	private void registerCommands() {
			CommandRegistrationCallback.EVENT.register(
				(dispatcher, registryAccess, environment) -> dispatcher.register(
						CommandManager.literal(MOD_ID)
								.then(reloadConfigCommand("reload"))
								.then(showConfigCommand("info"))));

	}

	private static LiteralArgumentBuilder<ServerCommandSource> reloadConfigCommand(String command) {
		return CommandManager.literal(command).requires(source -> source.hasPermissionLevel(2))
				.executes(context -> {
					Config.read();
					context.getSource().sendMessage(Text.of("reloaded config"));
					return Command.SINGLE_SUCCESS;
				});
	}

	private static LiteralArgumentBuilder<ServerCommandSource> showConfigCommand(String command) {
		return CommandManager.literal(command).executes(RegexSearch::showConfig);
	}
	
	private static int showConfig(CommandContext<ServerCommandSource> context) {
		ServerCommandSource source = context.getSource();

		List<String> lines = new ArrayList<String>();

		for (Field field : Config.class.getFields()) {
			String value = "(null)";
			try {
				value = field.get(null).toString();
			} catch (Exception e) {
			}
			if(!field.getName().equals("configClass"))
				lines.add(field.getName() + ": " + value);
		}

		source.sendMessage(Text.of(String.join("\n", lines)));

		return Command.SINGLE_SUCCESS;
	}
}