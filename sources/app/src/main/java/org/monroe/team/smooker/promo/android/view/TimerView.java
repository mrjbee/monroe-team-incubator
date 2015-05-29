package org.monroe.team.smooker.promo.android.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;

import org.monroe.team.smooker.promo.R;

import java.lang.reflect.Method;

@Deprecated
public class TimerView extends View{

    private float offset;
    private Paint timeTrackPaint;
    private Paint timePaint;
    private Paint timeOutPaint;
    private float timeProgress = 0f;
    private float timeOutProgress = -0.1f;
    private AnimationSupport support;

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

    @Override
    protected Parcelable onSaveInstanceState() {
        return new TimerViewSavedState(super.onSaveInstanceState(),
                timeProgress,
                timeOutProgress);
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {

        TimerViewSavedState savedState = (TimerViewSavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());

        timeProgress = savedState.timeProgress;
        timeOutProgress = savedState.timeOutProgress;

    }


    private void initialize() {

        if (android.os.Build.VERSION.SDK_INT >= 14){
            //support animation
            support = new TimerViewAnimation14APISupport(this);
        } else {
            support = new NoAnimationSupport(this);
        }
        offset = dimen(R.integer.timer_offset);

        timeTrackPaint = new Paint();
        timeTrackPaint.setAntiAlias(true);
        timeTrackPaint.setColor(Color.parseColor("#ff9a9698"));
        timeTrackPaint.setStrokeWidth(1);
        timeTrackPaint.setStrokeCap(Paint.Cap.ROUND);
        timeTrackPaint.setStyle(Paint.Style.STROKE);

        timePaint = new Paint();
        timePaint.setAntiAlias(true);
        timePaint.setColor(Color.parseColor("#9e0000"));

        timePaint.setStrokeWidth(dimen(R.integer.timer_after_stroke));
        timePaint.setStrokeCap(Paint.Cap.ROUND);
        timePaint.setStyle(Paint.Style.STROKE);

        timeOutPaint = new Paint();
        timeOutPaint.setAntiAlias(true);
        timeOutPaint.setColor(Color.parseColor("#f63225"));

        timeOutPaint.setStrokeWidth(dimen(R.integer.timer_before_stroke));
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

        if (timeOutProgress > 0){
            canvas.drawArc(asOvalBounds(center, getTimeOutCircleRadius()), -90, 359 * timeOutProgress, false, timeOutPaint);
        }
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

    public void animateTimeProgress(float newTimeProgress) {
        support.animateTimeProgress(timeProgress, newTimeProgress);
    }

    public void animateTimeOutProgress(float newTimeOutProgress) {
        support.animateTimeOutProgress(timeOutProgress, newTimeOutProgress);
    }

    public void setTimeProgress(float timeProgress) {

        this.timeProgress = timeProgress;
        invalidate();
    }

    public float getTimeProgress() {
        return timeProgress;
    }

    public void setTimeOutProgress(float timeOutProgress) {
        this.timeOutProgress = timeOutProgress;
        invalidate();
    }

    public float getTimeOutProgress() {
        return timeOutProgress;
    }

    public static abstract class AnimationSupport  {

        protected final TimerView owner;


        protected AnimationSupport(TimerView owner) {
            this.owner = owner;
        }

        public abstract void animateTimeProgress(float fromProgress, float toProgress);

        public abstract void animateTimeOutProgress(float timeOutProgress, float newTimeOutProgress);

    }

    public static class NoAnimationSupport extends AnimationSupport{

        protected NoAnimationSupport(TimerView owner) {
            super(owner);
        }

        @Override
        public void animateTimeProgress(float fromProgress, float toProgress) {
            owner.setTimeProgress(toProgress);
        }

        @Override
        public void animateTimeOutProgress(float timeOutProgress, float newTimeOutProgress) {
            owner.setTimeOutProgress(newTimeOutProgress);
        }
    }


    protected static class TimerViewSavedState extends BaseSavedState{

        private final float timeProgress;
        private final float timeOutProgress;

        public TimerViewSavedState(Parcelable superState, float timeProgress, float timeOutProgress) {
            super(superState);
            this.timeProgress = timeProgress;
            this.timeOutProgress = timeOutProgress;
        }

        public TimerViewSavedState(Parcel source) {
            super(source);
            this.timeProgress = source.readFloat();
            this.timeOutProgress = source.readFloat();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeFloat(timeProgress);
            dest.writeFloat(timeOutProgress);
        }

        public static final Parcelable.Creator<TimerViewSavedState> CREATOR = new Creator<TimerViewSavedState>() {
            public TimerViewSavedState createFromParcel(Parcel in) {
                return new TimerViewSavedState(in);
            }
            public TimerViewSavedState[] newArray(int size) {
                return new TimerViewSavedState[size];
            }
        };

    }


}
