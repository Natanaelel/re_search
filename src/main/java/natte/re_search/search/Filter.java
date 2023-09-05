package natte.re_search.search;

import java.util.function.Predicate;
import java.util.regex.Pattern;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;

public class Filter {

    private SearchOptions searchOptions;
    private ServerPlayerEntity player;
    private Predicate<ItemStack> predicate;

    public Filter(SearchOptions searchOptions, ServerPlayerEntity player) {
        this.searchOptions = searchOptions;
        this.player = player;
        this.predicate = itemStack -> !itemStack.isOf(Items.AIR);

        parseFilterExpression();
    }

    private void parseFilterExpression() {
        if (searchOptions.searchMode == 0) {
            Pattern pattern = Pattern.compile(searchOptions.expression,
                    searchOptions.isCaseSensitive ? 0 : Pattern.CASE_INSENSITIVE);

            add(itemStack -> {
                String name = itemStack.getItem().getName().getString();
                return pattern.matcher(name).find();
            });

        } else if (searchOptions.searchMode == 1) {
            String string = searchOptions.expression;
            Predicate<String> predicate = StringMatcher.overCaseFold((a, b) -> b.contains(a),
                    this.searchOptions.isCaseSensitive, string);
            Predicate<ItemStack> p = name(predicate);
            p = p.or(mod(predicate));
            p = p.or(id(predicate));
            p = p.or(tag(predicate));
            p = p.or(tooltip(predicate));
            add(p);
        }
        if (searchOptions.searchMode == 2) {
            String[] words = searchOptions.expression.split(" ");
            for (String word : words) {
                if (word.length() == 0)
                    continue;
                add(parseWord(word));
            }
        }
    }

    private Predicate<ItemStack> parseWord(String word) {
        if (word.length() == 0) {
            return itemStack -> true;
        }
        char c = word.charAt(0);
        String string = word.substring(1);
        if (c == '-') {
            return negate(parseWord(string));
        }
        Predicate<String> predicate = StringMatcher.preparePredicate(string, this.searchOptions);

        if (c == '@') {
            return string.contains(":") ? modColonId(predicate) : mod(predicate);
        }
        if (c == '*') {
            return id(predicate);
        }
        if (c == '$') {
            return string.contains(":") ? tagColonId(predicate) : tag(predicate);
        }
        if (c == '#') {
            return tooltip(predicate);
        } else {
            return name(StringMatcher.preparePredicate(word, this.searchOptions));
        }

    }

    public boolean test(ItemStack itemStack) {
        return this.predicate.test(itemStack);
    }

    private void add(Predicate<ItemStack> predicate) {
        this.predicate = this.predicate.and(predicate);
    }

    public Predicate<ItemStack> mod(Predicate<String> predicate) {
        // @mod
        return itemStack -> predicate.test(Registries.ITEM.getId(itemStack.getItem()).getNamespace());
    }

    public Predicate<ItemStack> modColonId(Predicate<String> predicate) {
        // @mod:item
        return itemStack -> predicate.test(Registries.ITEM.getId(itemStack.getItem()).toString());
    }

    public Predicate<ItemStack> id(Predicate<String> predicate) {
        // *item
        return itemStack -> predicate.test(Registries.ITEM.getId(itemStack.getItem()).getPath());
    }

    public Predicate<ItemStack> tag(Predicate<String> predicate) {
        // $tag
        return itemStack -> itemStack.streamTags().anyMatch(tag -> predicate.test(tag.id().getPath()));
    }

    public Predicate<ItemStack> tagColonId(Predicate<String> predicate) {
        // $mod:tag
        return itemStack -> itemStack.streamTags().anyMatch(tag -> predicate.test(tag.id().toString()));
    }

    public Predicate<ItemStack> tooltip(Predicate<String> predicate) {
        return itemStack -> itemStack.getTooltip(player, TooltipContext.ADVANCED).stream()
                .anyMatch(line -> predicate.test(line.getString()));

    }

    public Predicate<ItemStack> name(Predicate<String> predicate) {
        return itemStack -> predicate.test(itemStack.getName().getString());
    }

    public Predicate<ItemStack> negate(Predicate<ItemStack> predicate) {
        return itemStack -> !predicate.test(itemStack);
    }

}
