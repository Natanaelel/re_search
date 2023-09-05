package natte.re_search.screen;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;

public class SyntaxHighlighter {

    private static final Style SPACE_STYLE = Style.EMPTY;
    private static final Style NAME_STYLE = Style.EMPTY;
    private static final Style ID_STYLE = Style.EMPTY.withColor(0x8d7eed);
    private static final Style MOD_STYLE = Style.EMPTY.withColor(0xffa8f3);
    private static final Style TOOLTIP_STYLE = Style.EMPTY.withColor(0xffe0ad);
    private static final Style TAG_STYLE = Style.EMPTY.withColor(0x9efff4);
    private static final Style SPECIAL_STYLE = Style.EMPTY.withColor(0xf6bf57);
    private static final Style NEGATE_STYLE = Style.EMPTY.withColor(0xe43b3b);

    private List<OrderedText> styledChars = new ArrayList<>();

    public OrderedText provideRenderText(String original, int firstCharacterIndex) {
        return OrderedText.concat(styledChars.subList(firstCharacterIndex, firstCharacterIndex + original.length()));
    }

    public void refresh(String string) {

        Style style = SPACE_STYLE;
        boolean isSpaceStyle = true;
        styledChars.clear();
        for (char c : string.toCharArray()) {
            boolean special = (!isSpaceStyle) && (c == '^' || c == '$');
            boolean isNegate = isSpaceStyle && c == '-';

            if (isSpaceStyle) {
                isSpaceStyle = false;
                if (c == '@') {
                    style = MOD_STYLE;
                } else if (c == '*') {
                    style = ID_STYLE;
                } else if (c == '#') {
                    style = TOOLTIP_STYLE;
                } else if (c == '$') {
                    style = TAG_STYLE;
                }else if(c == '-'){
                    isNegate = true;
                    isSpaceStyle = true;
                } 
                else {
                    style = NAME_STYLE;
                }
            } else if (c == ' ') {
                style = SPACE_STYLE;
                isSpaceStyle = true;
                isNegate = false;
            }
            styledChars.add(OrderedText.styledForwardsVisitedString(String.valueOf(c),
                    special ? SPECIAL_STYLE : isNegate ? NEGATE_STYLE : style));

        }
    }

}