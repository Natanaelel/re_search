package natte.re_search.screen;

import org.lwjgl.glfw.GLFW;

import natte.re_search.Searcher;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class SearchScreen extends Screen {

    private Screen parent;
    private MinecraftClient client;
    
    private TextFieldWidget searchBox;


    public SearchScreen(Screen parent, MinecraftClient client) {
        super(Text.translatable("screen.re_search.label"));
        this.parent = parent;
        this.client = client;
    }

    @Override
    protected void init() {

        // super.init();
        int boxWidth =  120;
        int boxHeight =  18;
        int x =  width / 2 - boxWidth / 2;
        int y =  height - boxHeight / 2 - 200;
    
        Text text = Text.of("");

        searchBox = new TextFieldWidget(textRenderer, x, y, boxWidth, boxHeight, text);
        setInitialFocus(searchBox);
        addDrawableChild(searchBox);
        
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if(keyCode == GLFW.GLFW_KEY_ENTER){
            // System.out.println("searching...");

            Searcher.search(searchBox.getText(), this.client);
            close();
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    @Override

    public void close() {
        client.setScreen(this.parent);
    }

    @Override
    public void tick() {
        searchBox.tick();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
    
}
