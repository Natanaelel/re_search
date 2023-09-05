package natte.re_search;

import java.util.List;

import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import natte.re_search.network.ItemSearchResultPacketS2C;
import natte.re_search.render.WorldRendering;
import natte.re_search.screen.SearchScreen;
import natte.re_search.search.MarkedInventory;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
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
		
	}

	private static void onItemSearchResult(PacketByteBuf packet) {
		List<MarkedInventory> inventories = ItemSearchResultPacketS2C.readPackedByteBuf(packet);
		WorldRendering.setMarkedInventories(inventories);
	}

	void registerKeyBinds() {

		KeyBinding keyBinding = KeyBindingHelper.registerKeyBinding(
				new KeyBinding("key.re_search.search", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_F,
						"category.re_search.keybinds"));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (keyBinding.wasPressed()) {
				client.setScreen(new SearchScreen(client.currentScreen, client));
			}
		});
	}
}