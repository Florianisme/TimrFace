package com.timrface.watchfacelayout.util;

import android.content.BroadcastReceiver;
import android.content.Context;

public abstract class FilteredBroadcastReceiver extends BroadcastReceiver implements ConfigurationAwareBroadcastReceiver{

    public abstract void register(Context context);

    public void unregister(Context context) {
        context.unregisterReceiver(this);
    }

}
