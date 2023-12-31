package natte.re_search;

import java.util.List;

import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojang.brigadier.Command;

import natte.re_search.config.Config;
import natte.re_search.network.ItemSearchResultPacketS2C;
import natte.re_search.render.HighlightRenderer;
import natte.re_search.render.WorldRendering;
import natte.re_search.screen.SearchScreen;
import natte.re_search.search.MarkedInventory;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.PacketByteBuf;

@Environment(EnvType.CLIENT)
public class RegexSearchClient implements ClientModInitializer {

	public static final String MOD_ID = "re_search";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final MinecraftClient Game = MinecraftClient.getInstance();

	@Override
	public void onInitializeClient() {
		ClientPlayNetworking.registerGlobalReceiver(ItemSearchResultPacketS2C.PACKET_ID, (client, handler, packet, responseSender) -> RegexSearchClient.onItemSearchResult(packet));
		
		registerKeyBinds();
		
		WorldRendering.register();

		HighlightRenderer.register();
		
		registerCommands();

	}

	private static void onItemSearchResult(PacketByteBuf packet) {
		List<MarkedInventory> inventories = ItemSearchResultPacketS2C.readPackedByteBuf(packet);
		WorldRendering.setMarkedInventories(inventories);
	}

	private void registerKeyBinds() {

		KeyBinding keyBinding = KeyBindingHelper.registerKeyBinding(
				new KeyBinding("key.re_search.search", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_Y,
						"category.re_search.keybinds"));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (keyBinding.wasPressed()) {
				client.setScreen(new SearchScreen(client.currentScreen, client));
			}
		});
	}

	private void registerCommands(){
		ClientCommandRegistrationCallback.EVENT.register(
				(dispatcher, registryAccess) -> dispatcher.register(
						ClientCommandManager.literal(MOD_ID + "_client")
					.then(ClientCommandManager.literal("set_highlighter")
										.then(ClientCommandManager.literal("old").executes(context -> {
											Config.isOldHighlighter = true;
											return Command.SINGLE_SUCCESS;
										}))
										.then(ClientCommandManager.literal("default").executes(context -> {
											Config.isOldHighlighter = false;
											return Command.SINGLE_SUCCESS;
										})))));
	}
}