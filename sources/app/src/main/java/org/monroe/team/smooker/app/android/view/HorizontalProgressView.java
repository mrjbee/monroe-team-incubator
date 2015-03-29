package org.monroe.team.smooker.app.android.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import org.monroe.team.smooker.app.R;

public class HorizontalProgressView extends View{
    private Paint linePaint;
    private Paint valuePaint;
    private float progress = 0.3f;

    public HorizontalProgressView(Context context) {
        super(context);
        init(context);
    }

    public HorizontalProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public HorizontalProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public HorizontalProgressView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        linePaint = new Paint();
        linePaint.setStrokeWidth(2);
        linePaint.setColor(Color.parseColor("#ff606060"));

        valuePaint = new Paint();
        valuePaint.setStrokeWidth(15);
        valuePaint.setColor(Color.parseColor("#d71326"));
    }


    @Override
    protected void onDraw(Canvas canvas) {
        valuePaint.setStrokeWidth(getHeight()/4);
        canvas.drawLine(0,getHeight()/2,getWidth(),getHeight()/2, linePaint);
        canvas.drawLine(0,getHeight()/2,getWidth()*progress, getHeight()/2, valuePaint);
    }

    public float getProgress() {
        return progress;
    }

    public void setProgress(float progress) {
        this.progress = progress;
        this.invalidate();
    }
}
