package com.zbadev.emotizone;

import android.content.Context;

public class BuildConfig {
    public static String apiKey;

    public static void initialize(Context context) {
        apiKey = context.getString(R.string.gemini_api_key);
    }
}
