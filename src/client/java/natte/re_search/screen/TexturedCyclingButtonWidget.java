package natte.re_search.screen;

import java.util.function.Consumer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.util.Identifier;


@Environment(EnvType.CLIENT)
public class TexturedCyclingButtonWidget<T extends CycleableOption> extends TexturedButtonWidget {

    public T state;

    private static final int textureWidth = 256;
    private static final int textureHeight = 256;

    @SuppressWarnings("unchecked") // cast to TexturedCyclingButtonWidget<T> line 27, 
    public TexturedCyclingButtonWidget(T state, int x, int y, int width, int height, int hoveredVOffset, Identifier texture,
            Consumer<TexturedCyclingButtonWidget<T>> pressAction) {
            super(x, y, width, height, 0, 0, hoveredVOffset, texture, textureWidth, textureHeight, button -> pressAction.accept((TexturedCyclingButtonWidget<T>)button), ScreenTexts.EMPTY);
        
        this.state = state;
        this.refreshTooltip();
        this.setTooltipDelay(700);
    }
    
    @Override
    public void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
        this.drawTexture(context, this.texture, this.getX(), this.getY(), this.state.uOffset(), this.state.vOffset(), this.hoveredVOffset,
                this.width, this.height, textureWidth, textureHeight);
    }

    public void refreshTooltip(){
        this.setTooltip(state.getTooltip());
    }

}
