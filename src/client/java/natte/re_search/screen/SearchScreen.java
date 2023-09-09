package natte.re_search.screen;

import java.util.ArrayList;

import org.lwjgl.glfw.GLFW;

import natte.re_search.RegexSearch;
import natte.re_search.config.Config;
import natte.re_search.network.ItemSearchPacketC2S;
import natte.re_search.render.HighlightRenderer;
import natte.re_search.render.WorldRendering;
import natte.re_search.search.MarkedInventory;
import natte.re_search.search.SearchOptions;
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

    private static final Identifier WIDGET_TEXTURE = new Identifier(RegexSearch.MOD_ID, "textures/gui/widgets.png");

    private Screen parent;
    private MinecraftClient client;

    private TextFieldWidget searchBox;

    private SyntaxHighlighter highlighter;

    private static SearchHistory searchHistory = new SearchHistory(100);

    public SearchScreen(Screen parent, MinecraftClient client) {
        super(Text.translatable("screen.re_search.label"));
        this.parent = parent;
        this.client = client;
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

        KeepMode keepMode = Config.keepLast ? Config.autoSelect ? KeepMode.HIGHLIGHT : KeepMode.KEEP : KeepMode.CLEAR;
        this.addDrawableChild(
                new TexturedCyclingButtonWidget<KeepMode>(keepMode, centerX - 27, centerY + 71, 20,
                        20, 20, WIDGET_TEXTURE, this::onKeepModeButtonPress));

        SearchType searchType = Config.searchBlocks ? Config.searchEntities ? SearchType.BOTH : SearchType.BLOCKS : SearchType.ENTITIES;
        this.addDrawableChild(
                new TexturedCyclingButtonWidget<SearchType>(searchType, centerX + 7, centerY + 71, 20,
                        20, 20, WIDGET_TEXTURE, this::onSearchTypeButtonPress));

        this.addDrawableChild(new TexturedCyclingButtonWidget<SearchMode>(SearchMode.values()[Config.searchMode],
                centerX + 41, centerY + 71, 20, 20, 20, WIDGET_TEXTURE, this::onSearchModeButtonPress));

        searchHistory.resetPosition();
        if (Config.keepLast) {
            searchBox.setText(searchHistory.getPrevious());
            if (Config.autoSelect) {
                searchBox.setSelectionStart(searchBox.getText().length());
                searchBox.setSelectionEnd(0);
            }
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER) {

            String text = searchBox.getText();
            if (text.isEmpty()) {
                WorldRendering.clearMarkedInventories();
                HighlightRenderer.setRenderedItems(new ArrayList<MarkedInventory>());
            } else {
                ClientPlayNetworking.send(ItemSearchPacketC2S.PACKET_ID, new SearchOptions(text, Config.isCaseSensitive, Config.searchMode,
                                Config.searchBlocks, Config.searchEntities).createPacketByteBuf());
  
                searchHistory.add(text);
                HighlightRenderer.startRender();
            }
            close();
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_UP) {
            searchBox.setText(searchHistory.getPrevious());
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_DOWN) {
            searchBox.setText(searchHistory.getNext());
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

    private void onKeepModeButtonPress(TexturedCyclingButtonWidget<KeepMode> button){
        button.state = button.state == KeepMode.CLEAR ? KeepMode.KEEP
                : button.state == KeepMode.KEEP ? KeepMode.HIGHLIGHT : KeepMode.CLEAR;

        Config.keepLast = button.state.keepLast;
        Config.autoSelect = button.state.autoSelect;
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

enum KeepMode implements CycleableOption {
    CLEAR("clear", false, false, 0, 120),
    KEEP("keep", true, false, 20, 120),
    HIGHLIGHT("keep_highlight", true, true, 40, 120);

    public final boolean keepLast;
    public final boolean autoSelect;


    public final Text name;
    public final Text info;
    public final int uOffset;
    public final int vOffset;

    private KeepMode(String mode, boolean keepLast, boolean autoSelect, int uOffset, int vOffset) {
        this.keepLast = keepLast;
        this.autoSelect = autoSelect;

        this.name = Text.translatable("option.re_search.keep_mode." + mode);
        this.info = Text.translatable("description.re_search.keep_mode." + mode);
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