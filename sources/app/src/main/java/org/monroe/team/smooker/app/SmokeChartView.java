package org.monroe.team.smooker.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
    Paint valuePaint;
    Paint selectionValuePaint;
    Paint selectionValueTextPaint;

    float axisCaptionTextSize =30f;

    String verticalAxisName = "smokes count";
    String horizontalAxisName = "today hours";
    Rect verticalAxisTextBounds = new Rect();
    private float verticalAxisPadding;
    Rect horizontalAxisTextBounds = new Rect();
    private float horizontalAxisPadding;
    private float backgroundStripeHeight = 100f;
    private float stripeHeight = 20f;
    private List<Integer> model = new ArrayList<Integer>();
    private int limit = -1;

    PointF originalTouch = null;

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

        valuePaint = new Paint();
        valuePaint.setAntiAlias(true);
        valuePaint.setColor(Color.parseColor("#58cb1c"));
        valuePaint.setStyle(Paint.Style.STROKE);
        valuePaint.setStrokeWidth(2);

        selectionValuePaint= new Paint();
        selectionValuePaint.setAntiAlias(true);
        selectionValuePaint.setColor(Color.parseColor("#008cec"));
        selectionValuePaint.setStrokeWidth(4);
        selectionValuePaint.setTextSize(axisCaptionTextSize);

        selectionValuePaint.setShadowLayer(3, 4, 6, Color.LTGRAY);
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
        horizontalAxisPadding = horizontalAxisTextBounds.height() * 2.5f;

        transparentPaint = new Paint();
        transparentPaint.setColor(Color.TRANSPARENT);

        backgroundStripePaint = new Paint();
        backgroundStripePaint.setColor(Color.BLACK);
        backgroundStripePaint.setAlpha(10);

        stripePaint = new Paint();
        stripePaint.setColor(Color.BLACK);
        stripePaint.setAlpha(30);

        limitPaint = new Paint();
        limitPaint.setColor(Color.RED);
        limitPaint.setStrokeWidth(3);

        limitLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.LINEAR_TEXT_FLAG);
        limitLabelPaint.setColor(Color.RED);
        limitLabelPaint.setTextSize(axisCaptionTextSize);


        //DEBUG
        /*
        limit = 15;
        List<Date> model = new ArrayList<Date>(10);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
        try {
            model.add(simpleDateFormat.parse("08:00"));
            model.add(simpleDateFormat.parse("08:10"));
            model.add(simpleDateFormat.parse("08:10"));
            model.add(simpleDateFormat.parse("09:00"));
            model.add(simpleDateFormat.parse("12:00"));
            model.add(simpleDateFormat.parse("23:00"));
            model.add(simpleDateFormat.parse("23:11"));
            model.add(simpleDateFormat.parse("23:59"));
            setModel(model);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        */
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
        drawAxis(canvas, topPadding);
        if (limit > 0){
            drawLimit(canvas);
        }

        List<PointF> valuePoints = new ArrayList<PointF>();

        if(!model.isEmpty()){
            drawModel(canvas,valuePoints);
        }

        if(originalTouch != null){
            drawSelection(canvas, valuePoints);
        }
    }

    private void drawSelection(Canvas canvas, List<PointF> valuePoints) {

        PointF touch = new PointF(originalTouch.x,originalTouch.y);
        if (touch.x < verticalAxisPadding + 5){
            touch.set(verticalAxisPadding + 5,touch.y);
        } else if (touch.x > getMaxMinuteWidth()){
            touch.x = getMaxMinuteWidth();
        }

        Pair<Integer,PointF>  smokePoint = findSmoke(touch, valuePoints);
        if (smokePoint != null) {
            touch.set(smokePoint.second.x,touch.y);
        }


        canvas.drawLine(touch.x, 0, touch.x, getHeight() - horizontalAxisPadding, selectionValuePaint);
        int minutesTotal = Math.round((touch.x - verticalAxisPadding)/getMinuteWidth());
        int minutes = minutesTotal%60;
        int hours = minutesTotal/60;
        String time = ((hours == 0)? "00":hours) +":"+((minutes<10)?"0"+minutes:minutes);
        Rect timeBounds = new Rect();
        selectionValuePaint.getTextBounds(time,0,time.length(),timeBounds);
        float textXPosition = touch.x - timeBounds.width()/2;
        if (textXPosition < verticalAxisPadding){
            textXPosition = verticalAxisPadding;
        } else if (textXPosition + timeBounds.width()  > getWidth()){
            textXPosition = getWidth() - timeBounds.width() - 5;
        }
        canvas.drawText(time,textXPosition, getHeight()-horizontalAxisPadding+timeBounds.height()*1.5f,selectionValuePaint);


        if (smokePoint != null){
            String text = smokePoint.first.toString();
            selectionValuePaint.getTextBounds(text,0,text.length(),timeBounds);
            canvas.drawText(text,smokePoint.second.x - stripeHeight-timeBounds.width(), smokePoint.second.y -stripeHeight, selectionValuePaint);
            canvas.drawCircle(smokePoint.second.x,smokePoint.second.y,stripeHeight / 2, selectionValuePaint);
        }
    }

    private Pair<Integer, PointF> findSmoke(PointF touch, List<PointF> valuePoints) {
        for (int i = valuePoints.size()-1; i > 0; i--) {
            if (valuePoints.get(i).x > touch.x-5 && valuePoints.get(i).x < touch.x+5){
                return new Pair<Integer, PointF>(i,valuePoints.get(i));
            }
        }
        return null;
    }


    private void drawModel(Canvas canvas, List<PointF> valuePoints) {
        Path path = new Path();
        valuePaint.setStyle(Paint.Style.FILL);
        path.moveTo(verticalAxisPadding, getHeight() - horizontalAxisPadding);
        canvas.drawCircle(verticalAxisPadding, getHeight() - horizontalAxisPadding,
                stripeHeight / 4, valuePaint);
        valuePoints.add(new PointF(verticalAxisPadding, getHeight() - horizontalAxisPadding));
        for (int i = 0,j=1; i < model.size() ; i++,j++) {
            PointF pointF = new PointF(verticalAxisPadding + model.get(i) * getMinuteWidth(),
                    getHeight() - horizontalAxisPadding - j * getItemHeight());
            path.lineTo(pointF.x, pointF.y);
            canvas.drawCircle(pointF.x,pointF.y,stripeHeight / 4, valuePaint);
            valuePoints.add(pointF);
        }
        valuePaint.setStyle(Paint.Style.STROKE);
        canvas.drawPath(path,valuePaint);
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

    public void setModel(List<Date> values) {
        model = new ArrayList<Integer>(values.size());
        Calendar calendar = Calendar.getInstance();
        for (Date date : values) {
            calendar.setTime(date);
            int hours = calendar.get(Calendar.HOUR_OF_DAY);
            int minutes = calendar.get(Calendar.MINUTE);
            model.add(hours*60+minutes);
        }
        invalidate();
    }

    public float getMinuteWidth() {
        return ((float)(getWidth()-verticalAxisPadding)) / (25 * 60);
    }

    private float getMaxMinuteWidth() {
        return verticalAxisPadding + (24 * 60) * getMinuteWidth();
    }

    public void setLimit(int limit) {
        this.limit = limit;
        invalidate();
    }
}
