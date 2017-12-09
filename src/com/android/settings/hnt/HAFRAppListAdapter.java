package com.android.settings.hnt;


import java.util.LinkedList;
import java.util.Map;

import android.app.ThemeManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.settings.R;

public abstract class HAFRAppListAdapter extends BaseAdapter {

    final Context mContext;
    final Handler mHandler;
    final PackageManager mPackageManager;
    final LayoutInflater mLayoutInflater;

    protected LinkedList<PackageItem> mApps = new LinkedList<PackageItem>();

    public HAFRAppListAdapter(Context context, Map<String, Integer> list) {
        mContext = context;
        mHandler = new Handler();
        mPackageManager = context.getPackageManager();
        mLayoutInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        update(list);
    }

    public void update(final Map<String, Integer> app_array) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (mApps) {
                    final PackageItem[] array = new PackageItem[app_array.size() * 2];
                    for (String pkg_name : app_array.keySet()) {
                        try {
                            ApplicationInfo ai = mPackageManager.getApplicationInfo(pkg_name, 0);
                            final PackageItem item = new PackageItem();
                            item.title = ai.loadLabel(mPackageManager);
                            item.icon = ai.loadIcon(mPackageManager);
                            item.packageName = ai.packageName;
                            array[app_array.get(pkg_name)] = item;
                        } catch (Exception e) {
                        }
                    }

                    final LinkedList<PackageItem> temp = new LinkedList<PackageItem>();
                    for (int x = 0; x < array.length; x++) {
                        if (array[x] != null) {
                            temp.add(array[x]);
                        }
                    }
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mApps.clear();
                            mApps = temp;
                            notifyDataSetChanged();
                        }
                    });
                }
            }
        }).start();
    }

    @Override
    public int getCount() {
        return mApps.size();
    }

    public LinkedList<PackageItem> getList() {
        return mApps;
    }

    @Override
    public PackageItem getItem(int position) {
        return mApps.get(position);
    }

    @Override
    public long getItemId(int position) {
        if (position < 0 || position >= mApps.size()) {
            return -1;
        }
        return mApps.get(position).hashCode();
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final int themeMode = Settings.Secure.getInt(mContext.getContentResolver(),
            Settings.Secure.THEME_PRIMARY_COLOR, 0);
        ViewHolder holder;
        if (convertView != null) {
            holder = (ViewHolder) convertView.getTag();
        } else {
            convertView = mLayoutInflater.inflate(R.layout.view_package_list, parent, false);
            holder = new ViewHolder();
            ImageButton RemoveIcon = (ImageButton) convertView.findViewById(R.id.removeButton);
            if (!ThemeManager.isOverlayEnabled()) {
                if (themeMode == 1 || themeMode == 3) {
                    RemoveIcon.setColorFilter(Color.parseColor("#FFFFFF"), PorterDuff.Mode.SRC_ATOP);
                } else {
                    RemoveIcon.clearColorFilter();
                }
            }
            holder.name = (TextView) convertView.findViewById(android.R.id.title);
            holder.icon = (ImageView) convertView.findViewById(android.R.id.icon);
            holder.pkg = (TextView) convertView.findViewById(android.R.id.message);
            holder.remove = RemoveIcon;
            convertView.setTag(holder);
        }
        final PackageItem appInfo = getItem(position);

        holder.name.setText(appInfo.title);
        holder.pkg.setText(appInfo.packageName);
        holder.icon.setImageDrawable(appInfo.icon);
        holder.remove.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onRemoveButtonPress(appInfo);
            }
        });
        return convertView;
    }

    public abstract void onRemoveButtonPress(PackageItem app_info);

    public class PackageItem implements Comparable<PackageItem> {
        public CharSequence title;
        public String packageName;
        public Drawable icon;

        @Override
        public int compareTo(PackageItem another) {
            return this.title.toString().compareTo(another.title.toString());
        }
    }

    static class ViewHolder {
        TextView name;
        ImageView icon;
        TextView pkg;
        ImageButton remove;
    }
}
