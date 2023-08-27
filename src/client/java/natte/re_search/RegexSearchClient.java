package natte.re_search;

import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import natte.re_search.particle.ModParticles;
import natte.re_search.particle.custom.CustomParticle;
import natte.re_search.render.WorldRendering;
import natte.re_search.screen.SearchScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

@Environment(EnvType.CLIENT)
public class RegexSearchClient implements ClientModInitializer {

	public static final String MOD_ID = "re_search";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitializeClient() {
		registerKeyBinds();
		
		ParticleFactoryRegistry.getInstance().register(ModParticles.CUSTOM_PARTICLE, CustomParticle.Factory::new);
		WorldRendering.register();
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