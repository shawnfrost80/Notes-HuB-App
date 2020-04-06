package com.example.lakshayanoteshub;

import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class windowFile {
    Window window;
    int color;

    windowFile(Window window, int color) {
        this.window = window;
        this.color = color;
    }


    public windowFile setStatusBar() {
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        window.setStatusBarColor(color);
        return null;
    }
}
