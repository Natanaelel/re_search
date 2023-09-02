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
        if(searchOptions.searchMode == 0){
            Pattern pattern = Pattern.compile(searchOptions.expression, searchOptions.isCaseSensitive ? 0 : Pattern.CASE_INSENSITIVE);

            add(itemStack -> {
                String name = itemStack.getItem().getName().getString();
                return pattern.matcher(name).find();
            });

        }
        else if(searchOptions.searchMode == 1){
            // TODO: literal search
            System.err.println("Not Yet implemented literal search");
        }
        if (searchOptions.searchMode == 2) {
            String[] words = this.searchOptions.expression.split(" ");
            for (String word : words) {
                if (word.length() == 0)
                    continue;
                add(parseWord(word));
            }
        }
        System.out.println(searchOptions.searchMode);
    }

    private Predicate<ItemStack> parseWord(String word) {
        if (word.length() == 0)
            return itemStack -> true;
        char c = word.charAt(0);
        String string = word.substring(1);
        if (c == '@') {
            return mod(string.toLowerCase());
        }
        if (c == '*') {
            return id(string.toLowerCase());
        }
        if (c == '$') {
            return tag(string.toLowerCase());
        }
        if (c == '#') {
            return tooltip(string);
        }
        if (c == '-') {
            return negate(parseWord(string));
        } else {
            return name(word);
        }
    }

    public boolean test(ItemStack itemStack) {
        return this.predicate.test(itemStack);
    }

    private void add(Predicate<ItemStack> predicate) {
        this.predicate = this.predicate.and(predicate);
    }

    public Predicate<ItemStack> mod(String string) {
        // @mod:item
        if (string.contains(":")) {
            return itemStack -> Registries.ITEM.getId(itemStack.getItem()).toString().contains(string);
        }
        // @mod
        return itemStack -> Registries.ITEM.getId(itemStack.getItem()).getNamespace().contains(string);
    }

    public Predicate<ItemStack> id(String string) {
        // *item
        return itemStack -> Registries.ITEM.getId(itemStack.getItem()).getPath().contains(string);
    }

    public Predicate<ItemStack> tag(String string) {
        // $mod:tag
        if (string.contains(":")) {
            return itemStack -> itemStack.streamTags().anyMatch(tag -> tag.id().toString().contains(string));
        }
        // $tag
        return itemStack -> itemStack.streamTags().anyMatch(tag -> tag.id().getPath().contains(string));
    }

    public Predicate<ItemStack> tooltip(String string) {
        if (searchOptions.isCaseSensitive) {
            return itemStack -> itemStack.getTooltip(player, TooltipContext.ADVANCED).stream()
                    .anyMatch(line -> line.getString().contains(string));
        } else {
            return itemStack -> itemStack.getTooltip(player, TooltipContext.ADVANCED).stream()
                    .anyMatch(line -> line.getString().toLowerCase().contains(string.toLowerCase()));
        }
    }

    public Predicate<ItemStack> name(String string) {
        if (searchOptions.isCaseSensitive) {
            return itemStack -> itemStack.getName().getString().contains(string);
        } else {
            return itemStack -> itemStack.getName().getString().toLowerCase().contains(string.toLowerCase());
        }
    }

    public Predicate<ItemStack> negate(Predicate<ItemStack> predicate) {
        return itemStack -> !predicate.test(itemStack);
    }

}
