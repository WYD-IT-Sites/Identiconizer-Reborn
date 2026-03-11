package com.germainz.identiconizer;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;

public class ThemeHelper {

    public static final int MODE_FOLLOW_SYSTEM = 0;
    public static final int MODE_LIGHT = 1;
    public static final int MODE_DARK = 2;

    public static void applyActivityTheme(Activity activity) {
        Config config = Config.getInstance(activity);
        int mode = config.getThemeMode();
        boolean dark = shouldUseDark(activity, mode);
        activity.setTheme(dark ? R.style.AppTheme_Dark : R.style.AppTheme_Light);
    }

    public static boolean shouldUseDark(Context context, int mode) {
        if (mode == MODE_DARK) return true;
        if (mode == MODE_LIGHT) return false;
        int nightModeFlags = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES;
    }
}
