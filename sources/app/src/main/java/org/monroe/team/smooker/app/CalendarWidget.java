package org.monroe.team.smooker.app;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import org.monroe.team.smooker.app.common.Events;


/**
 * Implementation of App Widget functionality.
 */
public class CalendarWidget extends AppWidgetProvider {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Events.QUIT_SCHEDULE_REFRESH.getAction().equals(intent.getAction())||
            Events.QUIT_SCHEDULE_UPDATED.getAction().equals(intent.getAction())||
            Events.SMOKE_COUNT_CHANGED.getAction().equals(intent.getAction())) {

            RemoteViews views = createRemoteView(context);
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            appWidgetManager.updateAppWidget(new ComponentName(context,CalendarWidget.class),views);
        }
        super.onReceive(context, intent);
    }

    private static RemoteViews createRemoteView(Context context) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_calendar);
        CalendarWidgetUpdate update = SmookerApplication.instance.fetchCalendarWidgetContent();

        remoteViews.setTextViewText(R.id.wc_left_data_text, update.digitText);
        remoteViews.setTextViewText(R.id.wc_left_data_det_text, update.digitDetailsText);
        remoteViews.setTextViewText(R.id.wc_right_data_text, update.contentText);
        remoteViews.setTextViewText(R.id.wc_right_data_det_text, update.contentDetailsText);

        remoteViews.setOnClickPendingIntent(R.id.wc_right_data_text, DashboardActivity.openDashboard(context));
        remoteViews.setOnClickPendingIntent(R.id.wc_icon, RemoteControlNotificationReceiver.createAddSmokeIntent(context));
        return remoteViews;
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        appWidgetManager.updateAppWidget(new ComponentName(context,CalendarWidget.class),createRemoteView(context));
    }


    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }


    public static class CalendarWidgetUpdate {
        public final String digitText;
        public final String digitDetailsText;
        public final String contentText;
        public final String contentDetailsText;

        public CalendarWidgetUpdate(String digitText, String digitDetatilsText, String contentText, String contentDetatilsText) {
            this.digitText = digitText;
            this.digitDetailsText = digitDetatilsText;
            this.contentText = contentText;
            this.contentDetailsText = contentDetatilsText;
        }
    }
}


