/**
 * Copyright (C) 2015 The CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.android.settings.applications;

import android.app.IThemeCallback;
import android.app.ThemeManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceActivity;
import android.provider.Settings;

import com.android.settings.R;

public class AppOpsDetailsTop extends PreferenceActivity {

    private int mTheme;
    private ThemeManager mThemeManager;

    @Override
    public Intent getIntent() {
        Intent modIntent = new Intent(super.getIntent());
        modIntent.putExtra(EXTRA_SHOW_FRAGMENT, AppOpsDetails.class.getName());
        modIntent.putExtra(EXTRA_NO_HEADERS, true);
        return modIntent;
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        if (AppOpsDetails.class.getName().equals(fragmentName)) return true;
        return false;
    }

    private final IThemeCallback mThemeCallback = new IThemeCallback.Stub() {

        @Override
        public void onThemeChanged(int themeMode, int color) {
            onCallbackAdded(themeMode, color);
            AppOpsDetailsTop.this.runOnUiThread(() -> {
                AppOpsDetailsTop.this.recreate();
            });
        }

        @Override
        public void onCallbackAdded(int themeMode, int color) {
            mTheme = color;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        final int themeMode = Settings.Secure.getInt(getContentResolver(),
                Settings.Secure.THEME_PRIMARY_COLOR, 0);
        final int accentColor = Settings.Secure.getInt(getContentResolver(),
                Settings.Secure.THEME_ACCENT_COLOR, 0);
        mThemeManager = (ThemeManager) getSystemService(Context.THEME_SERVICE);
        if (mThemeManager != null) {
            mThemeManager.addCallback(mThemeCallback);
        }
        if (themeMode != 0 || accentColor != 0) {
            getTheme().applyStyle(mTheme, true);
        }
        if (themeMode == 2) {
            getTheme().applyStyle(R.style.settings_pixel_theme, true);
        }
        super.onCreate(savedInstanceState);
    }
}
