package natte.re_search.config;

import com.google.gson.annotations.Expose;

public class Config extends ConfigManager {

    @Expose public static boolean isCaseSensitive = false;

    @Expose public static int range = 12;

    @Expose public static int recursionLimit = -1;

    @Expose public static int searchMode = 0;

}
