package natte.re_search.screen;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedArgument;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.command.CommandSource;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;

public class SyntaxHighlighter {

    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("(\\s+)");
    private static final Style ERROR_STYLE = Style.EMPTY.withColor(Formatting.RED);
    private static final Style INFO_STYLE = Style.EMPTY.withColor(Formatting.GRAY);
    private static final List<Style> HIGHLIGHT_STYLES = Stream.of(Formatting.AQUA, Formatting.YELLOW, Formatting.GREEN, Formatting.LIGHT_PURPLE, Formatting.GOLD).map(arg_0 -> ((Style)Style.EMPTY).withColor(arg_0)).collect(ImmutableList.toImmutableList());
    
    private ParseResults<CommandSource> parse;
    private MinecraftClient client;


    public SyntaxHighlighter(MinecraftClient client){
        this.client = client;
    }

    public void init(String string){
        StringReader stringReader = new StringReader(string);
        CommandDispatcher<CommandSource> commandDispatcher = this.client.player.networkHandler.getCommandDispatcher();
        if (this.parse == null) {
            this.parse = commandDispatcher.parse(stringReader, (CommandSource)this.client.player.networkHandler.getCommandSource());
        }

    }

    public OrderedText provideRenderText(String original, int firstCharacterIndex) {
        if (this.parse != null) {
            return SyntaxHighlighter.highlight(this.parse, original, firstCharacterIndex);
        }
        this.init(original);
        return OrderedText.styledForwardsVisitedString((String)original, (Style)Style.EMPTY);
    }


    private static OrderedText highlight(ParseResults<CommandSource> parse, String original, int firstCharacterIndex) {
        int m;
        ArrayList<OrderedText> list = Lists.newArrayList();
        int i = 0;
        int j = -1;
        CommandContextBuilder<CommandSource> commandContextBuilder = parse.getContext().getLastChild();
        for (ParsedArgument<CommandSource, ?> parsedArgument : commandContextBuilder.getArguments().values()) {
            int k;
            if (++j >= HIGHLIGHT_STYLES.size()) {
                j = 0;
            }
            if ((k = Math.max(parsedArgument.getRange().getStart() - firstCharacterIndex, 0)) >= original.length())
                break;
            int l = Math.min(parsedArgument.getRange().getEnd() - firstCharacterIndex, original.length());
            if (l <= 0)
                continue;
            list.add(OrderedText.styledForwardsVisitedString((String) original.substring(i, k), (Style) INFO_STYLE));
            list.add(OrderedText.styledForwardsVisitedString((String) original.substring(k, l),
                    (Style) HIGHLIGHT_STYLES.get(j)));
            i = l;
        }
        if (parse.getReader().canRead()
                && (m = Math.max(parse.getReader().getCursor() - firstCharacterIndex, 0)) < original.length()) {
            int n = Math.min(m + parse.getReader().getRemainingLength(), original.length());
            list.add(OrderedText.styledForwardsVisitedString((String) original.substring(i, m), (Style) INFO_STYLE));
            list.add(OrderedText.styledForwardsVisitedString((String) original.substring(m, n), (Style) ERROR_STYLE));
            i = n;
        }
        list.add(OrderedText.styledForwardsVisitedString((String) original.substring(i), (Style) INFO_STYLE));
        return OrderedText.concat(list);
    }

}