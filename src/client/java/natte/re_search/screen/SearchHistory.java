package natte.re_search.screen;

import java.util.ArrayList;
import java.util.List;

public class SearchHistory {

    private final List<String> searchHistory;
    private int historyIndex;
    private int historySize;

    public SearchHistory(int historySize) {
        this.searchHistory = new ArrayList<>();
        this.historyIndex = 0;
        this.historySize = historySize;
    }

    public String getCurrent() {
        if (searchHistory.isEmpty()) {
            return "";
        }
        if(historyIndex >= searchHistory.size()){
            return "";
        }
        return searchHistory.get(historyIndex);
    }

    public String getPrevious() {
        if (historyIndex > 0) {
            historyIndex -= 1;
        }
        return getCurrent();
    }

    public String getNext() {
        if (historyIndex + 1 < searchHistory.size()) {
            historyIndex += 1;
            return getCurrent();
        }
        return "";
    }

    public void add(String string){
        searchHistory.add(string);
        historyIndex += 1;
        if(searchHistory.size() > historySize){
            searchHistory.remove(0);
            historyIndex -= 1;
        }
    }
    public void resetPosition(){
        historyIndex = searchHistory.size();
    }
}
