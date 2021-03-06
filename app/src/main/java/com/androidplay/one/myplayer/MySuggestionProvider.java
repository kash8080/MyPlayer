package com.androidplay.one.myplayer;

import android.content.SearchRecentSuggestionsProvider;

/**
 * Created by Rahul on 06-01-2017.
 */

public class MySuggestionProvider extends SearchRecentSuggestionsProvider {
    public final static String AUTHORITY = "com.androidplay.one.myplayer.MySuggestionProvider";
    public final static int MODE = DATABASE_MODE_QUERIES;

    public MySuggestionProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }

}
