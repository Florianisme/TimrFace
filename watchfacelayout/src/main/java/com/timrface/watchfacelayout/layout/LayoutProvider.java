package com.timrface.watchfacelayout.layout;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.*;
import com.timrface.watchfacelayout.R;
import com.timrface.watchfacelayout.config.Configuration;
import com.timrface.watchfacelayout.layout.components.*;

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
        Typeface robotoMedium = Typeface.createFromAsset(context.getAssets(), "Roboto-Medium.ttf");

        this.configuration = configuration;
        layoutList.add(new ChinLayout(configuration));
        layoutList.add(new BackgroundLayout(configuration));
        layoutList.add(new TickLayout(configuration, robotoMedium));
        layoutList.add(buildShadowPaint(configuration, context));
        layoutList.add(buildArrowLayout(configuration, context));
        layoutList.add(new TimeDigits(configuration, robotoLight, robotoThin));
        layoutList.add(new DateLayout(configuration, robotoLight));
        layoutList.add(new BatteryLayout(configuration, context, robotoMedium, robotoLight));
        layoutList.add(new AmPmLayout(configuration, robotoLight));

        return this;
    }

    private Layout buildArrowLayout(Configuration configuration, Context context) {
        Bitmap indicatorBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.indicator);
        indicatorBitmap = Bitmap.createScaledBitmap(indicatorBitmap, 50, 25, true);

        return new ArrowLayout(configuration, indicatorBitmap, context);
    }

    private Layout buildShadowPaint(Configuration configuration, Context context) {
        Bitmap shadowBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.indicator_shadow);
        shadowBitmap = Bitmap.createScaledBitmap(shadowBitmap, 50, 25, true);
        return new ShadowLayout(configuration, shadowBitmap);
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
        final float infoTextSize = resources.getDimension(R.dimen.info_size);
        final float timeTextSize = resources.getDimension(R.dimen.text_size);
        final float tickTextSize = resources.getDimension(R.dimen.tick_size);

        WindowInsets windowInsets = new WindowInsets(timeTextSize, infoTextSize, tickTextSize);

        for (Layout layout : layoutList) {
            layout.applyWindowInsets(windowInsets);
        }
    }
}
