package org.monroe.team.smooker.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SmokeChartView extends View {

    Paint axisPaint;
    Paint axisLabelPaint;
    Paint transparentPaint;
    Paint backgroundStripePaint;
    Paint stripePaint;
    Paint limitPaint;
    Paint limitLabelPaint;

    float axisCaptionTextSize =30f;

    String verticalAxisName = "smokes count";
    String horizontalAxisName = "today hours";
    Rect verticalAxisTextBounds = new Rect();
    private float verticalAxisPadding;
    Rect horizontalAxisTextBounds = new Rect();
    private float horizontalAxisPadding;
    private float backgroundStripeHeight = 120f;
    private float stripeHeight = 24f;
    private List<Date> model = new ArrayList<Date>();

    private int limit = -1;

    public SmokeChartView(Context context) {
        super(context);
        initialize(context);
    }

    public SmokeChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    public SmokeChartView(Context context, AttributeSet attrs, int defStyleAttr) {
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

        axisLabelPaint.getTextBounds(verticalAxisName,0,verticalAxisName.length(), verticalAxisTextBounds);
        verticalAxisPadding = verticalAxisTextBounds.height()/2;

        axisLabelPaint.getTextBounds(horizontalAxisName,0,horizontalAxisName.length(), horizontalAxisTextBounds);
        horizontalAxisPadding = horizontalAxisTextBounds.height() * 2.5f;

        transparentPaint = new Paint();
        transparentPaint.setColor(Color.TRANSPARENT);

        backgroundStripePaint = new Paint();
        backgroundStripePaint.setColor(Color.BLACK);
        backgroundStripePaint.setAlpha(5);

        stripePaint = new Paint();
        stripePaint.setColor(Color.BLACK);
        stripePaint.setAlpha(30);

        limitPaint = new Paint();
        limitPaint.setColor(Color.RED);
        limitPaint.setStrokeWidth(4);

        limitLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.LINEAR_TEXT_FLAG);
        limitLabelPaint.setColor(Color.RED);
        limitLabelPaint.setTextSize(axisCaptionTextSize);


        //DEBUG
        limit = 15;
        model = new ArrayList<Date>(10);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawBackgroundStripes(canvas);
        float topPadding = drawStripes(canvas);
        drawAxis(canvas, topPadding);
        if (limit > 0){
            drawLimit(canvas);
        }
    }

    private void drawLimit(Canvas canvas) {
        float itemHeight = getItemHeight();
        float limitYPosition = getHeight()-horizontalAxisPadding-itemHeight*limit;

        Rect textBounds = new Rect();
        String limitAsText = Integer.toString(limit);
        limitLabelPaint.getTextBounds(limitAsText, 0, limitAsText.length(),textBounds);

        canvas.drawLine(verticalAxisPadding + textBounds.width() + verticalAxisTextBounds.height() * 1.5f,
                limitYPosition,
                getWidth(),
                limitYPosition,
                limitPaint);

        canvas.drawText(limitAsText, verticalAxisPadding + verticalAxisTextBounds.height(), limitYPosition  + textBounds.width() * 0.25f, limitLabelPaint);


    }

    private float drawStripes(Canvas canvas) {

        int stripeCount = maxItemsOnABoard();

        for (int i = 0; i < stripeCount; i++){
            canvas.drawLine(
                        verticalAxisPadding,
                        getHeight() - horizontalAxisPadding - (stripeHeight * i),
                        getWidth(),
                        getHeight() - horizontalAxisPadding - (stripeHeight * i),
                        stripePaint
                );

        }
        return getHeight() - (stripeCount-1)*stripeHeight - horizontalAxisPadding;
    }

    private int maxItemsOnABoard() {
        return (int) ((getHeight() - horizontalAxisPadding) / stripeHeight);
    }

    private void drawBackgroundStripes(Canvas canvas) {

        int backgroundStripeCount = (int) ((getHeight() - horizontalAxisPadding) / backgroundStripeHeight);

        for (int i = 0; i < backgroundStripeCount; i++){
            if (i % 2 == 0) {
                canvas.drawRect(
                        verticalAxisPadding,
                        getHeight() - horizontalAxisPadding - (backgroundStripeHeight * (i + 1)),
                        getWidth(),
                        getHeight() - horizontalAxisPadding - (backgroundStripeHeight * i),
                        backgroundStripePaint
                );
            }
        }
    }


    private void drawAxis(Canvas canvas, float topPadding) {
        //DEBUG
        //canvas.drawText(verticalAxisTextBounds.width()+":"+verticalAxisTextBounds.height(),200,200,axisPaint);

        if (topPadding < verticalAxisTextBounds.width() * 0.5f){
            canvas.drawLine(verticalAxisPadding, topPadding, verticalAxisPadding, verticalAxisTextBounds.width() * 0.5f, axisPaint);
        }
        canvas.drawLine(verticalAxisPadding, verticalAxisTextBounds.width() * 1.5f, verticalAxisPadding, getHeight() - horizontalAxisPadding, axisPaint);

        canvas.rotate(-90);
        canvas.drawText(verticalAxisName, -verticalAxisTextBounds.width() * 1.5f, verticalAxisTextBounds.height()*0.75f, axisLabelPaint);
        canvas.rotate(90);

        canvas.drawLine(verticalAxisPadding,
                        getHeight() - horizontalAxisPadding,
                        getWidth() - horizontalAxisTextBounds.width()*1.5f,
                        getHeight() - horizontalAxisPadding,
                axisPaint);

        canvas.drawLine(getWidth(),
                getHeight() - horizontalAxisPadding,
                getWidth() - horizontalAxisTextBounds.width()*.5f,
                getHeight() - horizontalAxisPadding,
                axisPaint);

        canvas.drawText(horizontalAxisName,
                getWidth() - horizontalAxisTextBounds.width() * 1.5f,
                getHeight() - horizontalAxisPadding + horizontalAxisTextBounds.height() * 0.25f,
                axisLabelPaint);
    }

    private float getItemHeight() {
       float max = maxItemsOnABoard();
       float maxToDraw = Math.max(limit,model.size());
       if (max >= maxToDraw){
           //1 stripe : 1 smoke
           return stripeHeight;
       } else {
           return stripeHeight / ((maxToDraw+maxToDraw/5) / max);
       }
    }
}
