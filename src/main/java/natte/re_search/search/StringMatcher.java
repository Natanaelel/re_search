package natte.re_search.search;

import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class StringMatcher {

    public static Predicate<String> preparePredicate(String string, SearchOptions options) {
        return StringMatcher.overCaseFold(StringMatcher.parseBoundaries(string),
                options.isCaseSensitive, StringMatcher.trimBoundary(string));

    }

    private static BiPredicate<String, String> parseBoundaries(String string) {
        if(string.length() == 0){
            return (filterString, itemString) -> itemString.contains(filterString);
        }
        boolean matchStart = string.charAt(0) == '^';
        boolean matchEnd = string.charAt(string.length() - 1) == '$';
        
        if (matchStart && matchEnd)
            return (filterString, itemString) -> itemString.equals(filterString);
        else if (matchStart && !matchEnd)
            return (filterString, itemString) -> itemString.startsWith(filterString);
        else if (!matchStart && matchEnd)
            return (filterString, itemString) -> itemString.endsWith(filterString);
        else
            return (filterString, itemString) -> itemString.contains(filterString);

    }

    public static String trimBoundary(String string) {
        if (string.startsWith("^"))
            string = string.substring(1);
        if (string.endsWith("$"))
            string = string.substring(0, string.length() - 1);
        return string;
    }

    public static Predicate<String> overCaseFold(BiPredicate<String, String> function, boolean isCaseSensitive,
            String string) {
        if (isCaseSensitive)
            return a -> function.test(string, a);
        else {
            String lower = string.toLowerCase();
            return a -> function.test(lower, a.toLowerCase());
        }
    }
}
