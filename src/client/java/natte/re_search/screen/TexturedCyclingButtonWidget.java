package natte.re_search.screen;

import java.util.function.Consumer;
import java.util.function.Function;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.util.Identifier;


@Environment(EnvType.CLIENT)
public class TexturedCyclingButtonWidget<T> extends TexturedButtonWidget {

    // private SimpleOption.TooltipFactory<T> tooltipFactory;
    private Function<T, Integer> uOffsetFactory;

    public T state;

    @SuppressWarnings("unchecked") // cast to TexturedCyclingButtonWidget<T> line 27, 
    public TexturedCyclingButtonWidget(T state, int x, int y, int width, int height, int u, int v, int hoveredVOffset, int textureWidth,
            int textureHeight, Identifier texture,
            Consumer<TexturedCyclingButtonWidget<T>> pressAction, SimpleOption.TooltipFactory<T> tooltipFactory, Function<T, Integer> uOffsetFactory) {
            super(x, y, width, height, u, v, hoveredVOffset, texture, textureWidth, textureHeight, button -> pressAction.accept((TexturedCyclingButtonWidget<T>)button), ScreenTexts.EMPTY);
        
        // this.tooltipFactory = tooltipFactory;
        this.uOffsetFactory = uOffsetFactory;
        this.state = state;
    }

    @Override
    public void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
        this.drawTexture(context, this.texture, this.getX(), this.getY(), this.u + this.uOffsetFactory.apply(state), this.v, this.hoveredVOffset,
                this.width, this.height, this.textureWidth, this.textureHeight);
    }
}
