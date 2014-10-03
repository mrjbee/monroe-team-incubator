package org.monroe.team.smooker.app.android.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import org.monroe.team.smooker.app.R;

import java.lang.reflect.Method;

public class TimerView extends View{

    private float offset;
    private Paint timeTrackPaint;
    private Paint timePaint;
    private Paint timeOutPaint;
    private float timeProgress = 0.5f;
    private float timeOutProgress = 0.80f;

    public TimerView(Context context) {
        super(context);
        initialize();
    }

    public TimerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public TimerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    private void initialize() {
        offset = 10;

        timeTrackPaint = new Paint();
        timeTrackPaint.setAntiAlias(true);
        timeTrackPaint.setColor(Color.parseColor("#ff9a9698"));
        timeTrackPaint.setStrokeWidth(1);
        timeTrackPaint.setStrokeCap(Paint.Cap.ROUND);
        timeTrackPaint.setStyle(Paint.Style.STROKE);

        timePaint = new Paint();
        timePaint.setAntiAlias(true);
        timePaint.setColor(Color.parseColor("#9e0000"));
        timePaint.setStrokeWidth(6);
        timePaint.setStrokeCap(Paint.Cap.ROUND);
        timePaint.setStyle(Paint.Style.STROKE);

        timeOutPaint = new Paint();
        timeOutPaint.setAntiAlias(true);
        timeOutPaint.setColor(Color.parseColor("#f63225"));
        timeOutPaint.setStrokeWidth(20);
        timeOutPaint.setStrokeCap(Paint.Cap.SQUARE);
        timeOutPaint.setStyle(Paint.Style.STROKE);

        timeOutPaint.setShadowLayer(
                dimen(R.integer.chart_shadow_radius_float),
                dimen(R.integer.chart_shadow_dx_float),
                dimen(R.integer.chart_shadow_dy_float), Color.LTGRAY);
        try {
            Method method = this.getClass().getMethod("setLayerType",int.class,Paint.class);
            if (method!=null){
                method.invoke(this,LAYER_TYPE_SOFTWARE, timeOutPaint);
            }
        } catch (Exception e) {}

    }

    private float dimen(int id){
        return (float)getContext().getResources().getInteger(id)/100f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        PointF center = getCenter();
        canvas.drawOval(asOvalBounds(center, getTimeCircleRadius()), timeTrackPaint);
        canvas.drawArc(asOvalBounds(center, getTimeCircleRadius()), -90, 359 * timeProgress, false, timePaint);
        canvas.drawArc(asOvalBounds(center, getTimeOutCircleRadius()), -90, 359 * timeOutProgress, false, timeOutPaint);
    }

    private RectF asOvalBounds(PointF center, float radius) {
        RectF answer =new RectF(center.x - radius,center.y-radius,center.x+radius,center.y+radius);
        return answer;
    }


    private PointF getCenter() {
        return new PointF(getWidth()/2,getHeight()/2);
    }

    private float getBiggestRadius() {
        return Math.min(getWidth(),getHeight())/2 - offset*2;
    }

    public float getTimeCircleRadius() {
        return getBiggestRadius() - timePaint.getStrokeWidth() - timeOutPaint.getStrokeWidth()*2;
    }

    public float getTimeOutCircleRadius() {
        return getBiggestRadius() - timeOutPaint.getStrokeWidth();
    }

    public void setTimeProgress(float timeProgress) {
        this.timeProgress = timeProgress;
        invalidate();
    }

    public void setTimeOutProgress(float timeOutProgress) {
        this.timeOutProgress = timeOutProgress;
        invalidate();
    }
}
