package uk.co.yojan.kiara.android;

import android.content.Context;
import android.util.DisplayMetrics;

public class Utils {
    /**
     * Converting dp units to pixel units
     * http://developer.android.com/guide/practices/screens_support.html#dips-pels
     */
    public static int getPaddingPixels(Context context, int dpValue) {
        // Get the screen's density scale
        final float scale = context.getResources().getDisplayMetrics().density;
        // Convert the dps to pixels, based on density scale
        return (int) (dpValue * scale + 0.5f);
    }

    public static int dpToPixels(Context context, float dp) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return (int) (metrics.density * dp + 0.5f);
    }
}