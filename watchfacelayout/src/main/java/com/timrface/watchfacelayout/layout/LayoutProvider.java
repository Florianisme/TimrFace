package com.timrface.watchfacelayout.layout;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.*;
import android.support.wearable.complications.ComplicationData;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;
import com.timrface.watchfacelayout.R;
import com.timrface.watchfacelayout.config.Configuration;
import com.timrface.watchfacelayout.layout.components.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class LayoutProvider {

    private final List<Layout> layoutList = new ArrayList<>();
    private final List<Complication> complicationList = new ArrayList<>();
    private boolean inAmbientMode;

    public LayoutProvider init(Configuration configuration, Context context) {
        Typeface robotoLight = Typeface.createFromAsset(context.getAssets(), "Roboto-Light.ttf");
        Typeface robotoThin = Typeface.createFromAsset(context.getAssets(), "Roboto-Thin.ttf");
        Typeface robotoMedium = Typeface.createFromAsset(context.getAssets(), "Roboto-Medium.ttf");

        layoutList.add(new ChinLayout(configuration));
        layoutList.add(new BackgroundLayout(configuration));
        layoutList.add(new TickLayout(configuration, robotoMedium));
        layoutList.add(buildShadowPaint(configuration, context));
        layoutList.add(buildArrowLayout(configuration, context));
        layoutList.add(new TimeDigits(configuration, robotoLight, robotoThin));
        layoutList.add(new DateLayout(configuration, robotoLight));
        layoutList.add(new BatteryLayout(configuration, context, robotoMedium, robotoLight));
        layoutList.add(new AmPmLayout(configuration, robotoLight));

        complicationList.add(buildUnreadNotificationComplication(configuration, context, robotoLight, robotoMedium));

        return this;
    }

    private UnreadNotificationsLayout buildUnreadNotificationComplication(Configuration configuration, Context context, Typeface robotoLight, Typeface robotoMedium) {
        VectorDrawableCompat notifications = VectorDrawableCompat.create(context.getResources(), R.drawable.ic_notifications, context.getTheme());
        VectorDrawableCompat notificationsOutline = VectorDrawableCompat.create(context.getResources(), R.drawable.ic_notifications_outline, context.getTheme());
        return new UnreadNotificationsLayout(configuration, notifications, notificationsOutline, robotoMedium, robotoLight);
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
        for (Layout layout : layoutList) {
            layout.updateConfiguration(configuration);
        }
    }

    public void onAmbientModeChanged(final boolean inAmbientMode) {
        this.inAmbientMode = inAmbientMode;
        for (Layout layout : layoutList) {
            layout.updateAmbientMode(inAmbientMode);
        }
        for (Complication complication : complicationList) {
            if (!inAmbientMode || complication.drawWhenInAmbientMode()) {
                complication.updateAmbientMode(inAmbientMode);
            }
        }
    }

    public void update(Canvas canvas, float centerX, float centerY, Calendar calendar) {
        if (inAmbientMode) {
            canvas.drawColor(Color.BLACK);
        }
        for (Layout layout : layoutList) {
            if (!inAmbientMode || layout.drawWhenInAmbientMode()) {
                layout.update(canvas, centerX, centerY, calendar);
            }
        }
        for (Complication complication : complicationList) {
            if (!inAmbientMode || complication.drawWhenInAmbientMode()) {
                complication.update(canvas, centerX, centerY, calendar);
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
        for (Complication complication : complicationList) {
            complication.applyWindowInsets(windowInsets);
        }
    }

    public void updateComplicationData(ComplicationData complicationData, Context context) {
        for (Complication complication : complicationList) {
            complication.onComplicationDataUpdate(complicationData, context);
        }
    }

    public void onSurfaceChanged(int width, int height) {
        for (Complication complication : complicationList) {
            complication.onSurfaceChanged(width, height);
        }
    }
}
