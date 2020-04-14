package com.timrface.layout;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.*;
import com.timrface.Configuration;
import com.timrface.R;
import com.timrface.layout.components.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class LayoutProvider {

    private final List<Layout> layoutList = new ArrayList<>();
    private Configuration configuration;
    private boolean inAmbientMode;

    public LayoutProvider init(Configuration configuration, Context context) {
        Typeface robotoLight = Typeface.createFromAsset(context.getAssets(), "Roboto-Light.ttf");
        Typeface robotoThin = Typeface.createFromAsset(context.getAssets(), "Roboto-Thin.ttf");

        this.configuration = configuration;
        layoutList.add(new BackgroundLayout(configuration));
        layoutList.add(new ChinLayout(configuration));
        layoutList.add(buildShadowPaint(configuration, context));
        layoutList.add(buildTickLayout(configuration, context));
        layoutList.add(new TimeDigits(configuration, robotoLight, robotoThin));
        layoutList.add(new DateLayout(configuration, robotoLight));
        layoutList.add(buildBatteryLayout(context, robotoLight));
        layoutList.add(new AmPmLayout(configuration, robotoLight));

        return this;
    }

    private Layout buildShadowPaint(Configuration configuration, Context context) {
        Bitmap shadowBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.indicator_shadow);
        shadowBitmap = Bitmap.createScaledBitmap(shadowBitmap, 50, 25, true);
        return new ShadowLayout(configuration, shadowBitmap);
    }

    private Layout buildTickLayout(Configuration configuration, Context context) {
        Resources resources = context.getResources();

        Bitmap scaleBitmap = BitmapFactory.decodeResource(resources, R.drawable.scale);
        scaleBitmap = Bitmap.createScaledBitmap(scaleBitmap, 2000, 55, true);

        Bitmap indicatorBitmap = BitmapFactory.decodeResource(resources, R.drawable.indicator);
        indicatorBitmap = Bitmap.createScaledBitmap(indicatorBitmap, 50, 25, true);
        return new TickLayout(configuration, scaleBitmap, indicatorBitmap, context);
    }

    private Layout buildBatteryLayout(Context context, Typeface robotoLight) {
        return new BatteryLayout(configuration, context, robotoLight);
    }

    public void onConfigurationChange(final Configuration configuration) {
        this.configuration = configuration;
        for (Layout layout : layoutList) {
            layout.updateConfiguration(configuration);
        }
    }

    public void onAmbientModeChanged(final boolean inAmbientMode) {
        this.inAmbientMode = inAmbientMode;
        for (Layout layout : layoutList) {
            layout.updateAmbientMode(inAmbientMode);
        }
    }

    public void update(Canvas canvas, float width, float height, Calendar calendar) {
        if (inAmbientMode) {
            canvas.drawColor(Color.BLACK);
        }
        for (Layout layout : layoutList) {
            if (inAmbientMode && !layout.drawWhenInAmbientMode()) {
                continue;
            } else {
                layout.update(canvas, width, height, calendar);
            }
        }
    }

    public void applyWindowInsets(Resources resources) {
        float infoTextSize = resources.getDimension(R.dimen.info_size);
        final float timeTextSize = resources.getDimension(R.dimen.text_size);

        for (Layout layout : layoutList) {
            layout.applyWindowInsets(timeTextSize, infoTextSize);
        }
    }
}
