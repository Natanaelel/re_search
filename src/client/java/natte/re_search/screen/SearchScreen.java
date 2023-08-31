package natte.re_search.screen;

import org.lwjgl.glfw.GLFW;

import natte.re_search.network.ItemSearchPacketC2S;
import natte.re_search.network.NetworkingConstants;
import natte.re_search.render.WorldRendering;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class SearchScreen extends Screen {

    private static final Identifier WIDGET_TEXTURE = new Identifier("re_search", "textures/gui/widgets.png");

    private Screen parent;
    private MinecraftClient client;

    private TextFieldWidget searchBox;
    // private TexturedCyclingButtonWidget<CaseSensitivity> caseSensitivityButton;

    private CaseSensitivity caseSensitivity = CaseSensitivity.SENSITIVE;
    // private ButtonWidget searchModeButton;

    public SearchScreen(Screen parent, MinecraftClient client) {
        super(Text.translatable("screen.re_search.label"));
        this.parent = parent;
        this.client = client;
        // searchModeButton
    }

    @Override
    protected void init() {

        // super.init();
        int boxWidth = 120;
        int boxHeight = 18;
        int x = width / 2 - boxWidth / 2;
        int y = height - boxHeight / 2 - 200;

        Text text = Text.of("");

        searchBox = new TextFieldWidget(textRenderer, x, y, boxWidth, boxHeight, text);
        setInitialFocus(searchBox);
        addDrawableChild(searchBox);

        // this.addDrawableChild(
        // TexturedCyclingButtonWidget
        // .builder(value -> ((CaseSensitivity)value).name)
        // .values(CaseSensitivity.INSENSITIVE, CaseSensitivity.SENSITIVE)
        // .build(width / 2 - 61, y + 30, 20, 20,
        // Text.translatable("option.re_search.case_sensitivity"), (button, value) -> {
        // caseSensitivity = (CaseSensitivity) value;
        // }));
        this.addDrawableChild(new TexturedCyclingButtonWidget<CaseSensitivity>(CaseSensitivity.SENSITIVE,
                width / 2 - 61, y + 30, 20, 20, 0, 0, 20, 256, 256, WIDGET_TEXTURE, this::onCaseSensitiveButtonPress,
                mode -> null, mode -> mode.uOffset));
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER) {

            String text = searchBox.getText();
            if (text.isEmpty()) {
                WorldRendering.clearMarkedInventories();
            } else {
                ClientPlayNetworking.send(NetworkingConstants.ITEM_SEARCH_PACKET_ID,
                        ItemSearchPacketC2S.createPackedByteBuf(searchBox.getText()));
            }
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
        this.setFocused(searchBox);

    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    void onCaseSensitiveButtonPress(TexturedCyclingButtonWidget<CaseSensitivity> button) {
        System.out.println(button.state);
        this.caseSensitivity = CaseSensitivity.values()[(this.caseSensitivity.ordinal() + 1)
                % CaseSensitivity.values().length];
        button.state = this.caseSensitivity;
    }

}

enum CaseSensitivity {
    SENSITIVE("sensitive", 0),
    INSENSITIVE("insensitive", 20);

    public final Text name;
    public final Text info;
    public final int uOffset;

    private CaseSensitivity(String mode, int uOffset) {
        this.name = Text.translatable("option.re_search.case_sensitivity." + mode);
        this.info = Text.translatable("description.re_search.case_sensitivity." + mode);
        this.uOffset = uOffset;
    }
}