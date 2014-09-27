package org.monroe.team.smooker.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;

import java.lang.reflect.Method;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SmokeHistogramView extends View {

    private final int barSpacing = 2;
    Paint axisPaint;
    Paint axisLabelPaint;
    Paint transparentPaint;
    Paint backgroundStripePaint;
    Paint stripePaint;

    Paint valuePaint;
    Paint selectionValuePaint;
    Paint selectionValuePaintWithoutShadow;


    Rect verticalAxisTextBounds = new Rect();
    private float verticalAxisPadding;
    Rect horizontalAxisTextBounds = new Rect();
    private float horizontalAxisPadding;
    private List<Pair<Date,Integer>> model = new ArrayList<Pair<Date, Integer>>();

    PointF originalTouch = null;

    private float backgroundStripeHeight = 100f;
    private float stripeHeight = 20f;
    private float axisCaptionTextSize =30f;

    String verticalAxisName = "smokes count";
    String horizontalAxisName = "today hours";


    public SmokeHistogramView(Context context) {
        super(context);
        initialize(context);
    }

    public SmokeHistogramView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    public SmokeHistogramView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context);
    }

    private float dimen(int id){
        return (float)getContext().getResources().getInteger(id)/100f;
    }

    private void initialize(Context context) {

        verticalAxisName = context.getResources().getString(R.string.chart_smokes_axis);
        horizontalAxisName = context.getResources().getString(R.string.chart_day_axis);


        backgroundStripeHeight = dimen(R.integer.chart_stripe_background_height_float);
        stripeHeight = dimen(R.integer.chart_stripe_height_float);
        axisCaptionTextSize = dimen(R.integer.chart_axis_text_size_float);

        axisPaint = new Paint();
        axisPaint.setColor(Color.BLACK);

        valuePaint = new Paint();
        valuePaint.setAntiAlias(true);
        valuePaint.setColor(Color.parseColor("#58cb1c"));
        valuePaint.setStyle(Paint.Style.STROKE);
        valuePaint.setStrokeWidth(dimen(R.integer.chart_stroke_default_float));

        selectionValuePaint= new Paint();
        selectionValuePaint.setAntiAlias(true);
        selectionValuePaint.setColor(Color.parseColor("#008cec"));
        selectionValuePaint.setStrokeWidth(dimen(R.integer.chart_stroke_boldest_float));
        selectionValuePaint.setTextSize(axisCaptionTextSize);
        selectionValuePaint.setShadowLayer(
                dimen(R.integer.chart_shadow_radius_float),
                dimen(R.integer.chart_shadow_dx_float),
                dimen(R.integer.chart_shadow_dy_float), Color.LTGRAY);

        selectionValuePaintWithoutShadow = new Paint();
        selectionValuePaintWithoutShadow.setAntiAlias(true);
        selectionValuePaintWithoutShadow.setColor(Color.parseColor("#008cec"));
        selectionValuePaintWithoutShadow.setStrokeWidth(dimen(R.integer.chart_stroke_boldest_float));
        selectionValuePaintWithoutShadow.setTextSize(axisCaptionTextSize);

        try {
            Method method = this.getClass().getMethod("setLayerType",int.class,Paint.class);
            if (method!=null){
                method.invoke(this,LAYER_TYPE_SOFTWARE, selectionValuePaint);
            }
        } catch (Exception e) {}

        axisLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.LINEAR_TEXT_FLAG);
        axisLabelPaint.setAntiAlias(true);
        axisLabelPaint.setColor(Color.BLACK);
        axisLabelPaint.setTextSize(axisCaptionTextSize);

        axisLabelPaint.getTextBounds(verticalAxisName, 0, verticalAxisName.length(), verticalAxisTextBounds);
        verticalAxisPadding = verticalAxisTextBounds.height() / 2;

        axisLabelPaint.getTextBounds(horizontalAxisName, 0, horizontalAxisName.length(), horizontalAxisTextBounds);
        horizontalAxisPadding = horizontalAxisTextBounds.height() * 1.5f;

        transparentPaint = new Paint();
        transparentPaint.setColor(Color.TRANSPARENT);

        backgroundStripePaint = new Paint();
        backgroundStripePaint.setColor(Color.BLACK);
        backgroundStripePaint.setAlpha(10);

        stripePaint = new Paint();
        stripePaint.setColor(Color.BLACK);
        stripePaint.setAlpha(30);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            originalTouch = null;
        } else {
            originalTouch = new PointF(event.getX(),event.getY());
        }
        invalidate();
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawBackgroundStripes(canvas);
        float topPadding = drawStripes(canvas);

        List<TempModelItem> valuePoints = new ArrayList<TempModelItem>();

        if(!model.isEmpty()){
            drawModel(canvas,valuePoints);
        }

        if(originalTouch != null){
            drawSelectionShadow(canvas, valuePoints);
            valuePoints.clear();
            drawModel(canvas,valuePoints);
        }

        drawAxis(canvas, topPadding);

        if(originalTouch != null){
            drawSelection(canvas, valuePoints);
        }
    }


    private void drawSelectionShadow(Canvas canvas,   List<TempModelItem> valuePoints) {

        PointF touch = new PointF(originalTouch.x, originalTouch.y);

        if (touch.x < verticalAxisPadding + 5) {
            touch.set(verticalAxisPadding + 5, touch.y);
        } else if (touch.x > getMaxXValue()) {
            touch.x = getMaxXValue();
        }

        TempModelItem modelItem = findSmoke(touch, valuePoints);
        if (modelItem != null) {
            touch.set(modelItem.valueAnchor.x, touch.y);
        }

        canvas.drawLine(touch.x, 0, touch.x, getHeight() - horizontalAxisPadding, selectionValuePaint);

        if (modelItem != null){
            canvas.drawCircle(modelItem.valueAnchor.x,modelItem.valueAnchor.y,stripeHeight / 2, selectionValuePaint);
        }
    }

    private void drawSelection(Canvas canvas,   List<TempModelItem> valuePoints) {

        PointF touch = new PointF(originalTouch.x,originalTouch.y);

        if (touch.x < verticalAxisPadding + 5){
            touch.set(verticalAxisPadding + 5,touch.y);
        } else if (touch.x > getMaxXValue()){
            touch.x = getMaxXValue();
        }

        TempModelItem  modelItem = findSmoke(touch, valuePoints);
        if (modelItem != null) {
            touch.set(modelItem.valueAnchor.x,touch.y);
        }

        canvas.drawLine(touch.x, 0, touch.x, getHeight() - horizontalAxisPadding, selectionValuePaintWithoutShadow);
        Rect textBounds = new Rect();
        String timeText = null;
        DateFormat format = DateFormat.getDateInstance();
        if (modelItem != null){
            timeText = format.format(modelItem.date);
        } else {
            timeText = format.format(getModelDate(touch.x,valuePoints));
        }

        if (timeText != null) {
            Rect timeText2Bounds = new Rect();
            selectionValuePaint.getTextBounds(timeText, 0, timeText.length(), textBounds);
            selectionValuePaint.getTextBounds("W", 0, 1, timeText2Bounds);

            float textXPosition = touch.x - textBounds.width() / 2;

            if (textXPosition < verticalAxisPadding) {
                textXPosition = verticalAxisPadding;
            } else if (textXPosition + textBounds.width() > getWidth()) {
                textXPosition = getWidth() - textBounds.width() - 5;
            }

            canvas.drawText(timeText, textXPosition, getHeight() - horizontalAxisPadding + timeText2Bounds.height() * 1.5f, selectionValuePaint);
        }

        if (modelItem != null){
            String text = modelItem.value.toString();
            selectionValuePaint.getTextBounds(text,0,text.length(),textBounds);
            canvas.drawText(text,modelItem.valueAnchor.x - stripeHeight-textBounds.width(), modelItem.valueAnchor.y -stripeHeight, selectionValuePaint);
            canvas.drawCircle(modelItem.valueAnchor.x,modelItem.valueAnchor.y,stripeHeight / 2, selectionValuePaintWithoutShadow);
        }
    }

    private Date getModelDate(float x, List<TempModelItem> items) {
        for (int i = items.size()-1; i > -1 ; i--) {
            if (x > (items.get(i).valueAnchor.x - getBarWidth()/2)){
                return model.get(i).first;
            }
        }
        return items.get(0).date;
    }


    private TempModelItem findSmoke(PointF touch,  List<TempModelItem> valuePoints) {
        for (int i = valuePoints.size()-1; i > -1; i--) {
            if (valuePoints.get(i).valueAnchor.x > touch.x-5 && valuePoints.get(i).valueAnchor.x < touch.x+5){
                return valuePoints.get(i);
            }
        }
        return null;
    }


    private void drawModel(Canvas canvas,   List<TempModelItem> valuePoints) {
        valuePaint.setStyle(Paint.Style.FILL);

        float barWidth = getBarWidth();

        RectF barRect = new RectF();
        for (int i = 0; i < model.size() ; i++) {
            barRect.set(verticalAxisPadding + i*barWidth + (i+1)*barSpacing,
                    getHeight() - horizontalAxisPadding - model.get(i).second * getSmokeHeight(),
                    verticalAxisPadding + (i+1)*barWidth + (i+1)*barSpacing,
                    getHeight() - horizontalAxisPadding);

            canvas.drawRect(barRect, valuePaint);
            valuePoints.add(new TempModelItem(
                    new PointF(verticalAxisPadding + i*barWidth + (i+1)*barSpacing + barWidth/2,
                            getHeight() - horizontalAxisPadding - model.get(i).second * getSmokeHeight()),
                    model.get(i).second,
                    model.get(i).first
            ));
        }
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
        return (int) ((getHeight() - horizontalAxisPadding) / stripeHeight)-1;
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

    private float getSmokeHeight() {
       float max = maxItemsOnABoard();
       float maxToDraw = getMaxSmokeInModel();
       if (max >= maxToDraw){
           //1 stripe : 1 smoke
           return stripeHeight;
       } else {
           return stripeHeight / ((maxToDraw+maxToDraw/5) / max);
       }
    }

    private float getMaxSmokeInModel() {
        //TODO: add cache here
        float maxToDraw = 0;
        for (Pair<Date, Integer> dateIntegerPair : model) {
            if (dateIntegerPair.second > maxToDraw) maxToDraw = dateIntegerPair.second;
        }
        return maxToDraw;
    }

    public void setModel(List<Pair<Date, Integer>> model) {
        this.model = model;
        invalidate();
    }

    public float getBarWidth() {
        float freeSpace = (float) (getWidth() - verticalAxisPadding);
        return freeSpace / (model.size() + 0.5f) - barSpacing;
    }

    private float getMaxXValue() {
        return verticalAxisPadding + model.size() * (getBarWidth()+barSpacing);
    }

    private static class TempModelItem{

        private final PointF valueAnchor;
        private final Integer value;
        private final Date date;

        private TempModelItem(PointF valueAnchor, Integer value, Date date) {
            this.valueAnchor = valueAnchor;
            this.value = value;
            this.date = date;
        }
    }

}
