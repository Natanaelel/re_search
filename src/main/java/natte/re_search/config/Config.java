package natte.re_search.config;

import com.google.gson.annotations.Expose;

public class Config extends ConfigManager {

    
    @Expose public static int range = 12;
    @Expose public static int recursionLimit = -1;
    
    @Expose public static boolean isCaseSensitive = false;
    @Expose public static int searchMode = 0;

    @Expose public static int maxInventories = 10;
    @Expose public static int maxSearchResults = -1;
    @Expose public static int maxSearchResultsPerInventory = 81;

    @Expose public static boolean searchBlocks = true;    
    @Expose public static boolean searchEntities = true;

    @Expose public static boolean autoSelect = true;
    @Expose public static boolean keepLast = true;

    @Expose public static int autoHideTime = 20;

    public static boolean isOldHighlighter = false;
}
