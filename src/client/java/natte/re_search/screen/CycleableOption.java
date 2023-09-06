package natte.re_search.screen;

import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public interface CycleableOption {
    
    public Text getName();
    public Text getInfo();
    
    public int uOffset();
    public int vOffset();

    default public Tooltip getTooltip(){
        return Tooltip.of(getName().copy().append(Text.empty().append("\n").append(getInfo()).formatted(Formatting.DARK_GRAY)));
    }
}
