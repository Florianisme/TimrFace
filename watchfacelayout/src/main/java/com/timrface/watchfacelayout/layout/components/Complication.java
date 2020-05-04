package com.timrface.watchfacelayout.layout.components;

import android.content.Context;
import android.support.wearable.complications.ComplicationData;
import com.timrface.watchfacelayout.config.Configuration;

public abstract class Complication extends Layout {

    public Complication(Configuration configuration) {
        super(configuration);
    }

    public abstract void onComplicationDataUpdate(ComplicationData complicationData, Context context);

}
