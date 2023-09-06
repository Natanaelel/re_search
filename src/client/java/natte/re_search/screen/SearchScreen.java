package natte.re_search.screen;

import org.lwjgl.glfw.GLFW;

import natte.re_search.RegexSearch;
import natte.re_search.config.Config;
import natte.re_search.network.ItemSearchPacketC2S;
import natte.re_search.render.WorldRendering;
import natte.re_search.search.SearchOptions;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class SearchScreen extends Screen {

    private static final Identifier WIDGET_TEXTURE = new Identifier(RegexSearch.MOD_ID, "textures/gui/widgets.png");

    private Screen parent;
    private MinecraftClient client;

    private TextFieldWidget searchBox;

    private SyntaxHighlighter highlighter;

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

        int centerX = width / 2;
        int centerY = height / 2;

        searchBox = new TextFieldWidget(textRenderer, x, y, boxWidth, boxHeight, Text.empty());
        searchBox.setMaxLength(100);

        setInitialFocus(searchBox);
        addDrawableChild(searchBox);

        highlighter = new SyntaxHighlighter();
        highlighter.setMode(Config.searchMode);
        searchBox.setRenderTextProvider(highlighter::provideRenderText);
        searchBox.setChangedListener(highlighter::refresh);

        this.addDrawableChild(new TexturedCyclingButtonWidget<CaseSensitivity>(
                CaseSensitivity.getSensitivity(Config.isCaseSensitive),
                centerX - 61, centerY + 71, 20, 20, 20, WIDGET_TEXTURE, this::onCaseSensitiveButtonPress));

        this.addDrawableChild(
                new TexturedCyclingButtonWidget<SearchType>(SearchType.BOTH, centerX - 10, centerY + 71, 20,
                        20, 20, WIDGET_TEXTURE, this::onSearchTypeButtonPress));

        this.addDrawableChild(new TexturedCyclingButtonWidget<SearchMode>(SearchMode.values()[Config.searchMode],
                centerX + 41, centerY + 71, 20, 20, 20, WIDGET_TEXTURE, this::onSearchModeButtonPress));
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER) {

            String text = searchBox.getText();
            if (text.isEmpty()) {
                WorldRendering.clearMarkedInventories();
            } else {
                ItemSearchPacketC2S
                        .send(new SearchOptions(text, Config.isCaseSensitive, Config.searchMode,
                                Config.searchBlocks, Config.searchEntities));
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
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private void onCaseSensitiveButtonPress(TexturedCyclingButtonWidget<CaseSensitivity> button) {
        Config.isCaseSensitive = !Config.isCaseSensitive;
        Config.markDirty();

        button.state = Config.isCaseSensitive ? CaseSensitivity.SENSITIVE : CaseSensitivity.INSENSITIVE;
        button.refreshTooltip();
    }

    private void onSearchTypeButtonPress(TexturedCyclingButtonWidget<SearchType> button) {
        button.state = button.state == SearchType.BLOCKS ? SearchType.ENTITIES
                : button.state == SearchType.ENTITIES ? SearchType.BOTH : SearchType.BLOCKS;

        Config.searchBlocks = button.state.searchBlocks;
        Config.searchEntities = button.state.searchEntities;
        Config.markDirty();

        button.refreshTooltip();
    }

    private void onSearchModeButtonPress(TexturedCyclingButtonWidget<SearchMode> button) {

        Config.searchMode = (Config.searchMode + 1) % 3;
        Config.markDirty();

        highlighter.setMode(Config.searchMode);
        highlighter.refresh(searchBox.getText());
        button.state = SearchMode.values()[Config.searchMode];
        button.refreshTooltip();
    }
}

enum CaseSensitivity implements CycleableOption {
    SENSITIVE("sensitive", 0, 0),
    INSENSITIVE("insensitive", 20, 0);

    public final Text name;
    public final Text info;
    public final int uOffset;
    public final int vOffset;

    private CaseSensitivity(String mode, int uOffset, int vOffset) {
        this.name = Text.translatable("option.re_search.case_sensitivity." + mode);
        this.info = Text.translatable("description.re_search.case_sensitivity." + mode);
        this.uOffset = uOffset;
        this.vOffset = vOffset;
    }

    public int uOffset() {
        return this.uOffset;
    }

    public int vOffset() {
        return this.vOffset;
    }

    public Text getName() {
        return this.name;
    }

    public Text getInfo() {
        return this.info;
    }

    public static CaseSensitivity getSensitivity(boolean isCaseSensitive) {
        return isCaseSensitive ? SENSITIVE : INSENSITIVE;
    }
}



enum SearchType implements CycleableOption {
    BOTH("both", true, true, 0, 40),
    BLOCKS("blocks", true, false, 20, 40),
    ENTITIES("entities", false, true, 40, 40);

    public final boolean searchBlocks;
    public final boolean searchEntities;

    public final Text name;
    public final Text info;
    public final int uOffset;
    public final int vOffset;

    private SearchType(String mode, boolean searchBlocks, boolean searchEntities, int uOffset, int vOffset) {

        this.name = Text.translatable("option.re_search.search_type." + mode);
        this.info = Text.translatable("description.re_search.search_type." + mode);
        this.searchBlocks = searchBlocks;
        this.searchEntities = searchEntities;
        this.uOffset = uOffset;
        this.vOffset = vOffset;
    }

    public int uOffset() {
        return this.uOffset;
    }

    public int vOffset() {
        return this.vOffset;
    }

    public Text getName() {
        return this.name;
    }

    public Text getInfo() {
        return this.info;
    }
}

enum SearchMode implements CycleableOption {
    REGEX("regex", 0, 80),
    LITERAL("literal", 20, 80),
    EXTENDED("extended", 40, 80);

    public final Text name;
    public final Text info;
    public final int uOffset;
    public final int vOffset;

    private SearchMode(String mode, int uOffset, int vOffset) {
        this.name = Text.translatable("option.re_search.search_mode." + mode);
        this.info = Text.translatable("description.re_search.search_mode." + mode);
        this.uOffset = uOffset;
        this.vOffset = vOffset;
    }

    public int uOffset() {
        return this.uOffset;
    }

    public int vOffset() {
        return this.vOffset;
    }

    public Text getName() {
        return this.name;
    }

    public Text getInfo() {
        return this.info;
    }
}