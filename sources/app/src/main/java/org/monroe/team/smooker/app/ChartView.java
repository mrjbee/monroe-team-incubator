package org.monroe.team.smooker.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class ChartView extends View {

    Paint axisPaint;
    Paint axisLabelPaint;
    Paint transparentPaint;
    Paint stripePaint;

    float axisPadding = 70f;
    float stripeHeight = 70f;
    float axisCaptionTextSize =30f;

    String verticalAxisName = "smokes count";
    String horizontalAxisName = "today hours";
    Rect axisVerticalTextBounds = new Rect();
    private float verticalAxisPadding;
    Rect axisHorizontalTextBounds = new Rect();
    private float horisontalAxisPadding;


    public ChartView(Context context) {
        super(context);
        initialize(context);
    }

    public ChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    public ChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context);
    }

    private void initialize(Context context) {
        axisPaint = new Paint();
        axisPaint.setColor(Color.BLACK);

        axisLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.LINEAR_TEXT_FLAG);
        axisLabelPaint.setAntiAlias(true);
        axisLabelPaint.setColor(Color.BLACK);
        axisLabelPaint.setTextSize(axisCaptionTextSize);

        axisLabelPaint.getTextBounds(verticalAxisName,0,verticalAxisName.length(), axisVerticalTextBounds);
        verticalAxisPadding = axisVerticalTextBounds.height()/2;

        axisLabelPaint.getTextBounds(horizontalAxisName,0,horizontalAxisName.length(),axisHorizontalTextBounds);
        horisontalAxisPadding = axisHorizontalTextBounds.height() * 2.5f;

        transparentPaint = new Paint();
        transparentPaint.setColor(Color.TRANSPARENT);

        stripePaint = new Paint();
        stripePaint.setColor(Color.RED);
        stripePaint.setAlpha(50);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawAxis(canvas);
    }


    private void drawAxis(Canvas canvas) {

        //DEBUG
        //canvas.drawText(axisVerticalTextBounds.width()+":"+axisVerticalTextBounds.height(),200,200,axisPaint);

        canvas.drawLine(verticalAxisPadding, 0, verticalAxisPadding, axisVerticalTextBounds.width() * 0.5f, axisPaint);
        canvas.drawLine(verticalAxisPadding, axisVerticalTextBounds.width() * 1.5f, verticalAxisPadding, getHeight() - horisontalAxisPadding, axisPaint);

        canvas.rotate(-90);
        canvas.drawText(verticalAxisName, -axisVerticalTextBounds.width() * 1.5f, axisVerticalTextBounds.height()*0.75f, axisLabelPaint);
        canvas.rotate(90);

        canvas.drawLine(verticalAxisPadding,
                        getHeight() - horisontalAxisPadding,
                        getWidth() - axisHorizontalTextBounds.width()*1.5f,
                        getHeight() - horisontalAxisPadding,
                axisPaint);

        canvas.drawLine(getWidth(),
                getHeight() - horisontalAxisPadding,
                getWidth() - axisHorizontalTextBounds.width()*.5f,
                getHeight() - horisontalAxisPadding,
                axisPaint);

        canvas.drawText(horizontalAxisName,
                getWidth() - axisHorizontalTextBounds.width()*1.5f,
                getHeight() - horisontalAxisPadding + axisHorizontalTextBounds.height()*0.25f,
                axisLabelPaint);
    }
}
