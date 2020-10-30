package com.timrface.watchfacelayout.layout.components.complications;

import android.content.Context;
import android.graphics.Rect;
import android.support.wearable.complications.ComplicationData;
import android.support.wearable.complications.ComplicationText;

import com.timrface.watchfacelayout.config.ComplicationSide;
import com.timrface.watchfacelayout.config.ComplicationType;
import com.timrface.watchfacelayout.config.Configuration;
import com.timrface.watchfacelayout.layout.components.Layout;

public abstract class Complication extends Layout {

    protected ComplicationSide complicationSide;

    public Complication(Configuration configuration) {
        super(configuration);
    }

    public abstract void onComplicationDataUpdate(ComplicationData complicationData, Context context);

    public abstract ComplicationType getComplicationType();

    protected String getComplicationTextOrDefault(ComplicationData complicationData, String defaultText, Context context) {
        ComplicationText shortText = complicationData.getShortText();
        if (shortText == null) {
            return defaultText;
        }
        CharSequence text = shortText.getText(context, System.currentTimeMillis());
        if (text == null) {
            return defaultText;
        }
        return text.toString();
    }

    public void setDrawSide(ComplicationSide complicationSide) {
        this.complicationSide = complicationSide;
    }

}
