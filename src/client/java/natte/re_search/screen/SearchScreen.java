package natte.re_search.screen;

import org.lwjgl.glfw.GLFW;

import natte.re_search.RegexSearch;
import natte.re_search.config.Config;
import natte.re_search.network.ItemSearchPacketC2S;
import natte.re_search.render.WorldRendering;
import natte.re_search.search.SearchOptions;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class SearchScreen extends Screen {

    private static final Identifier WIDGET_TEXTURE = new Identifier(RegexSearch.MOD_ID, "textures/gui/widgets.png");

    private Screen parent;
    private MinecraftClient client;

    private TextFieldWidget searchBox;


    public SearchScreen(Screen parent, MinecraftClient client) {
        super(Text.translatable("screen.re_search.label"));
        this.parent = parent;
        this.client = client;
        // searchModeButton
    }

    @Override
    protected void init() {

        int boxWidth = 120;
        int boxHeight = 18;
        int x = width / 2 - boxWidth / 2;
        int y = height / 2 - boxHeight / 2 + 50;

        Text text = Text.of("");

        searchBox = new TextFieldWidget(textRenderer, x, y, boxWidth, boxHeight, text);
        searchBox.setMaxLength(100);
        
        setInitialFocus(searchBox);
        addDrawableChild(searchBox);

        SyntaxHighlighter highlighter = new SyntaxHighlighter();
        searchBox.setRenderTextProvider(highlighter::provideRenderText);
        searchBox.setChangedListener(highlighter::refresh);

        this.addDrawableChild(new TexturedCyclingButtonWidget<CaseSensitivity>(Config.isCaseSensitive ? CaseSensitivity.SENSITIVE : CaseSensitivity.INSENSITIVE,
                width / 2 - 61, y + 30, 20, 20, 0, 0, 20, 256, 256, WIDGET_TEXTURE, this::onCaseSensitiveButtonPress,
                mode -> Tooltip.of(mode.name.copy().append(Text.empty().append("\n").append(mode.info).formatted(Formatting.DARK_GRAY))), mode -> mode.uOffset));

        this.addDrawableChild(new TexturedCyclingButtonWidget<SearchMode>(SearchMode.values()[Config.searchMode],
                width / 2 + 41, y + 30, 20, 20, 0, 0, 20, 256, 256, WIDGET_TEXTURE, button -> {
                    Config.searchMode = (Config.searchMode + 1) % 3;
                    Config.markDirty();
                    button.state = SearchMode.values()[Config.searchMode];
                    button.refreshTooltip();
                },
                mode -> Tooltip.of(mode.name.copy().append(Text.empty().append("\n").append(mode.info).formatted(Formatting.DARK_GRAY))), mode -> mode.uOffset));
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER) {

            String text = searchBox.getText();
            if (text.isEmpty()) {
                WorldRendering.clearMarkedInventories();
            } else {
                ClientPlayNetworking.send(ItemSearchPacketC2S.PACKET_ID,
                        ItemSearchPacketC2S.createPackedByteBuf(new SearchOptions(searchBox.getText(), Config.isCaseSensitive, Config.searchMode)));
            }
            close();
            return true;
        }
        
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override

    public void close() {
        Config.save();
        client.setScreen(this.parent);
    }

    @Override
    public void tick() {
        searchBox.tick();
        // this.setFocused(searchBox);

    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    void onCaseSensitiveButtonPress(TexturedCyclingButtonWidget<CaseSensitivity> button) {
        Config.isCaseSensitive = !Config.isCaseSensitive;
        Config.markDirty();

        button.state = Config.isCaseSensitive ? CaseSensitivity.SENSITIVE : CaseSensitivity.INSENSITIVE;
        button.refreshTooltip();
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

enum SearchMode {
    REGEX("regex", 40),
    LITERAL("literal", 60),
    EXTENDED("extended", 80);

    public final Text name;
    public final Text info;
    public final int uOffset;

    private SearchMode(String mode, int uOffset) {
        this.name = Text.translatable("option.re_search.search_mode." + mode);
        this.info = Text.translatable("description.re_search.search_mode." + mode);
        this.uOffset = uOffset;
    }
}