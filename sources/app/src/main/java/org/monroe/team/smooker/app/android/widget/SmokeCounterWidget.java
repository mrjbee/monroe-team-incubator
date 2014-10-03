package org.monroe.team.smooker.app.android.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import org.monroe.team.smooker.app.R;
import org.monroe.team.smooker.app.actors.ActorSmoker;
import org.monroe.team.smooker.app.common.constant.Events;
import org.monroe.team.smooker.app.uc.GetStatisticState;
import org.monroe.team.smooker.app.android.DashboardActivity;
import org.monroe.team.smooker.app.android.SmookerApplication;


/**
 * Implementation of App Widget functionality.
 */
public class SmokeCounterWidget extends AppWidgetProvider {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Events.SMOKE_COUNT_CHANGED.getAction().equals(intent.getAction())) {
            int smokeCount = Events.SMOKE_COUNT_CHANGED.extractValue(intent);
            RemoteViews views = createRemoteView(context,smokeCount);
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            appWidgetManager.updateAppWidget(new ComponentName(context,SmokeCounterWidget.class),views);
        }
        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        final int N = appWidgetIds.length;
        for (int i=0; i<N; i++) {
            updateAppWidget(context, appWidgetManager, appWidgetIds[i]);
        }
    }

    @Override
    public void onEnabled(final Context context) {}

    @Override
    public void onDisabled(Context context) {}

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {

        GetStatisticState.StatisticState statisticState = SmookerApplication.instance.getModel().execute(GetStatisticState.class,new GetStatisticState.StatisticRequest().with(GetStatisticState.StatisticName.SMOKE_TODAY));
        int count = statisticState.getTodaySmokeDates().size();
        RemoteViews views = createRemoteView(context, count);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    private static RemoteViews createRemoteView(Context context, int count) {
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_smoke_counter);
        String countText = ""+count;
        views.setTextViewText(R.id.ws_count_text,countText);
        views.setOnClickPendingIntent(R.id.ws_add_btn,
                ActorSmoker.create(context, ActorSmoker.ADD_SMOKE).buildDefault());
        views.setOnClickPendingIntent(R.id.widget_root,
                DashboardActivity.openDashboard(context));
        return views;
    }
}


