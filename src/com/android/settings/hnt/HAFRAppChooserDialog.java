package com.android.settings.hnt;

import android.app.Dialog;
import android.app.ThemeManager;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.PorterDuff;
import android.provider.Settings;
import android.support.annotation.ColorInt;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import java.lang.reflect.Field;

import com.android.settings.R;

public abstract class HAFRAppChooserDialog extends Dialog {

    final HAFRAppChooserAdapter dAdapter;
    final ProgressBar dProgressBar;
    final ListView dListView;
    final EditText dSearch;
    final ImageButton dButton;
    final Context mContext;

    private int mId;

    private ThemeManager mThemeManager;
    private int mPrimaryColor;
    private int mAccentColor;

    public HAFRAppChooserDialog(Context context) {
        super(context);

        mContext = context;

        final TypedArray ta = context.obtainStyledAttributes(new int[]{
            android.R.attr.colorAccent,
            android.R.attr.colorPrimary});
        mAccentColor = ta.getColor(0, 0);
        mPrimaryColor = ta.getColor(1, 0);
        ta.recycle();

        final int themeMode = Settings.Secure.getInt(context.getContentResolver(),
            Settings.Secure.THEME_PRIMARY_COLOR, 0);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_app_chooser_list);

        if (!ThemeManager.isOverlayEnabled()) {
            if (themeMode == 1 || themeMode == 3) {
                getWindow().getDecorView().getBackground().setColorFilter(mPrimaryColor, PorterDuff.Mode.SRC_ATOP);
            }
        }

        ImageButton SearchButton = (ImageButton) findViewById(R.id.searchButton);
        EditText SearchText = (EditText) findViewById(R.id.searchText);
        if (!ThemeManager.isOverlayEnabled()) {
            if (themeMode == 1 || themeMode == 3) {
                SearchText.getBackground().setColorFilter(mAccentColor, PorterDuff.Mode.SRC_ATOP);
                setCursorColor(SearchText, mAccentColor);
                SearchButton.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
            } else {
                SearchText.getBackground().clearColorFilter();
                SearchButton.clearColorFilter();
            }
        }

        dListView = (ListView) findViewById(R.id.listView1);
        dSearch = SearchText;
        dButton = SearchButton;
        dProgressBar = (ProgressBar) findViewById(R.id.progressBar1);

        dAdapter = new HAFRAppChooserAdapter(context) {
            @Override
            public void onStartUpdate() {
                dProgressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFinishUpdate() {
                dProgressBar.setVisibility(View.GONE);
            }
        };

        dListView.setAdapter(dAdapter);
        dListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> av, View v, int pos, long id) {
                HAFRAppChooserAdapter.AppItem info = (HAFRAppChooserAdapter.AppItem) av
                        .getItemAtPosition(pos);
                onListViewItemClick(info, mId);
                dismiss();
            }
        });

        dButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dAdapter.getFilter().filter(dSearch.getText().toString(), new Filter.FilterListener() {
                    public void onFilterComplete(int count) {
                        dAdapter.update();
                    }
                });
            }
        });

        dAdapter.update();
    }

    private void setCursorColor(EditText view, @ColorInt int color) {
        try {
            // Get the cursor resource id
            Field field = TextView.class.getDeclaredField("mCursorDrawableRes");
            field.setAccessible(true);
            int drawableResId = field.getInt(view);

            // Get the editor
            field = TextView.class.getDeclaredField("mEditor");
            field.setAccessible(true);
            Object editor = field.get(view);

            // Get the drawable and set a color filter
            Drawable drawable = mContext.getDrawable(drawableResId);
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
            Drawable[] drawables = {drawable, drawable};

            // Set the drawables
            field = editor.getClass().getDeclaredField("mCursorDrawable");
            field.setAccessible(true);
            field.set(editor, drawables);
        } catch (Exception ignored) {
            //Do nothing
        }
    }

    public void show(int id) {
        mId = id;
        show();
    }

    public abstract void onListViewItemClick(HAFRAppChooserAdapter.AppItem info, int id);
}
