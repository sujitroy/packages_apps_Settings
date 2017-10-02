package com.android.settings.hnt;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v14.preference.SwitchPreference;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import com.android.settings.hnt.CustomSeekBarPreference;

public class AmbientSettings extends SettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener {
    private static final String TAG = "AmbientSettings";

    private static final String KEY_DOZE = "doze";

    private static final String AMBIENT_DOZE_AUTO_BRIGHTNESS = "ambient_doze_auto_brightness";
    private static final String AMBIENT_DOZE_CUSTOM_BRIGHTNESS = "ambient_doze_custom_brightness";

    private CustomSeekBarPreference mAmbientDozeCustomBrightness;
    private SwitchPreference mAmbientDozeAutoBrightness;
    private SwitchPreference mDozePreference;

    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.ACTION_AMBIENT_DISPLAY;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Activity activity = getActivity();
        ContentResolver resolver = getActivity().getContentResolver();

        addPreferencesFromResource(R.xml.ambient_settings);

        mDozePreference = (SwitchPreference) findPreference(KEY_DOZE);
        mDozePreference.setOnPreferenceChangeListener(this);

        if (isDozeAvailable(activity)) {
            mAmbientDozeCustomBrightness = (CustomSeekBarPreference) findPreference(AMBIENT_DOZE_CUSTOM_BRIGHTNESS);
            int minValue = getResources().getInteger(
                    com.android.internal.R.integer.config_screenBrightnessSettingMinimum);
            int defaultValue = getResources().getInteger(
                    com.android.internal.R.integer.config_screenBrightnessDoze);
            int brightness = Settings.System.getIntForUser(resolver,
                    Settings.System.AMBIENT_DOZE_CUSTOM_BRIGHTNESS, defaultValue, UserHandle.USER_CURRENT);
            mAmbientDozeCustomBrightness.setMin(minValue);
            mAmbientDozeCustomBrightness.setValue(brightness);
            mAmbientDozeCustomBrightness.setOnPreferenceChangeListener(this);

            mAmbientDozeAutoBrightness = (SwitchPreference) findPreference(AMBIENT_DOZE_AUTO_BRIGHTNESS);
            boolean defaultAmbientDozeAutoBrighthness = getResources().getBoolean(
                    com.android.internal.R.bool.config_allowAutoBrightnessWhileDozing);
            boolean isAmbientDozeAutoBrighthness = Settings.System.getIntForUser(resolver,
                    Settings.System.AMBIENT_DOZE_AUTO_BRIGHTNESS, defaultAmbientDozeAutoBrighthness ? 1 : 0,
                    UserHandle.USER_CURRENT) == 1;
            mAmbientDozeAutoBrightness.setChecked(isAmbientDozeAutoBrighthness);
            mAmbientDozeAutoBrightness.setOnPreferenceChangeListener(this);
            mAmbientDozeCustomBrightness.setEnabled(!isAmbientDozeAutoBrighthness);
        } else {
            removePreference(AMBIENT_DOZE_CUSTOM_BRIGHTNESS);
            removePreference(AMBIENT_DOZE_AUTO_BRIGHTNESS);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateState();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void updateState() {
        // Update doze if it is available.
        if (mDozePreference != null) {
            int value = Settings.Secure.getInt(getContentResolver(), Settings.Secure.DOZE_ENABLED, 1);
            mDozePreference.setChecked(value != 0);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (preference == mDozePreference) {
            boolean value = (Boolean) objValue;
            Settings.Secure.putInt(getContentResolver(), Settings.Secure.DOZE_ENABLED, value ? 1 : 0);
        } else if (preference == mAmbientDozeCustomBrightness) {
            int brightness = (Integer) objValue;
            Settings.System.putIntForUser(getActivity().getContentResolver(),
                    Settings.System.AMBIENT_DOZE_CUSTOM_BRIGHTNESS, brightness, UserHandle.USER_CURRENT);
        } else if (preference == mAmbientDozeAutoBrightness) {
            boolean value = (Boolean) objValue;
            Settings.System.putIntForUser(getActivity().getContentResolver(),
                    Settings.System.AMBIENT_DOZE_AUTO_BRIGHTNESS, value ? 1 : 0, UserHandle.USER_CURRENT);
            mAmbientDozeCustomBrightness.setEnabled(!value);
        }
        return true;
    }

    private static boolean isDozeAvailable(Context context) {
        String name = Build.IS_DEBUGGABLE ? SystemProperties.get("debug.doze.component") : null;
        if (TextUtils.isEmpty(name)) {
            name = context.getResources().getString(
                    com.android.internal.R.string.config_dozeComponent);
        }
        return !TextUtils.isEmpty(name);
    }
} 
