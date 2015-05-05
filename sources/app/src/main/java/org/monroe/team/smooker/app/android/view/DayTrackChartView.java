package org.monroe.team.smooker.app.android.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;

import org.monroe.team.corebox.utils.DateUtils;
import org.monroe.team.corebox.utils.Lists;
import org.monroe.team.smooker.app.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DayTrackChartView extends View {

    private String string_limit = "Limit";
    Paint axisPaint;
    Paint axisLabelPaint;
    Paint transparentPaint;
    Paint stripePaint;

    Paint limitPaint;
    Paint limitLabelPaint;


    Paint valuePaint;
    Paint valueShadowPaint;
    Paint futureValuePaint;
    Paint selectionValuePaint;


    Rect verticalAxisTextBounds = new Rect();
    private float verticalAxisPadding;
    Rect horizontalAxisTextBounds = new Rect();
    private float horizontalAxisPadding;
    private List<Integer> model = new ArrayList<Integer>();
    private int limit = -1;

    PointF originalTouch = null;

    private float backgroundStripeHeight = 100f;
    private float stripeHeight = 20f;
    private float axisCaptionTextSize =30f;

    String verticalAxisName = "smokes count";
    String horizontalAxisName = "today hours";
    private List<Integer> futureModel;


    public DayTrackChartView(Context context) {
        super(context);
        initialize(context);
    }

    public DayTrackChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    public DayTrackChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context);
    }

    private float dimen(int id){
        return (float)getContext().getResources().getInteger(id)/100f;
    }

    private void initialize(Context context) {

        string_limit = context.getString(R.string.general_limit)+" ";
        backgroundStripeHeight = dimen(R.integer.chart_stripe_background_height_float);
        stripeHeight = dimen(R.integer.chart_stripe_height_float);
        axisCaptionTextSize = dimen(R.integer.chart_axis_text_size_float);

        axisPaint = new Paint();
        axisPaint.setColor(Color.BLACK);

        valuePaint = new Paint();
        valuePaint.setAntiAlias(true);
        valuePaint.setColor(Color.parseColor("#ec0000"));
        valuePaint.setStyle(Paint.Style.STROKE);
        valuePaint.setStrokeWidth(dimen(R.integer.chart_stroke_default_float));

        valueShadowPaint = new Paint();
        valueShadowPaint.setAntiAlias(true);
        valueShadowPaint.setColor(Color.parseColor("#000000"));
        valueShadowPaint.setAlpha(50);
        valueShadowPaint.setStyle(Paint.Style.STROKE);
        valueShadowPaint.setStrokeWidth(dimen(R.integer.chart_stroke_default_float));


        futureValuePaint = new Paint();
        futureValuePaint.setAntiAlias(true);
        futureValuePaint.setColor(Color.parseColor("#008cec"));
        //futureValuePaint.setAlpha(200);
        futureValuePaint.setStyle(Paint.Style.STROKE);
        futureValuePaint.setStrokeWidth(dimen(R.integer.chart_stroke_predict_float));


        selectionValuePaint= new Paint();
        selectionValuePaint.setAntiAlias(true);
        selectionValuePaint.setColor(Color.parseColor("#3c3c3c"));
        selectionValuePaint.setStrokeWidth(dimen(R.integer.chart_stroke_boldest_float));
        selectionValuePaint.setTextSize(axisCaptionTextSize);

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

        stripePaint = new Paint();
        stripePaint.setColor(Color.BLACK);
        stripePaint.setAlpha(10);
        stripePaint.setStrokeWidth(1);


        limitPaint = new Paint();
        limitPaint.setColor(Color.parseColor("#ec0000"));
        limitPaint.setStrokeWidth(dimen(R.integer.chart_stroke_bolder_float));

        limitLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.LINEAR_TEXT_FLAG);
        limitLabelPaint.setColor(Color.WHITE);
        limitLabelPaint.setTextSize(axisCaptionTextSize);
        limitLabelPaint.setStrokeWidth(3);
        //DEBUG
        if (false){
           List<Date> model = new ArrayList<>(9);
           List<Date> futureModel = new ArrayList<>(4);
           Date itDate = DateUtils.dateOnly(DateUtils.now());
           itDate = DateUtils.mathMinutes(itDate, 60);
           model.add(itDate);
           for (int i = 0; i< 8; i++){
                itDate = DateUtils.mathMinutes(itDate, (int) (Math.random()*100));
                model.add(itDate);
           }
           for (int i = 0; i< 4; i++){
                itDate = DateUtils.mathMinutes(itDate, (int) (Math.random()*100));
                futureModel.add(itDate);
           }
           setModel(model);
           setFutureModel(futureModel);
           setLimit(model.size()+futureModel.size());
        }
    }

    private OnTouchListener touch_listener;

    @Override
    public void setOnTouchListener(OnTouchListener listener) {
        this.touch_listener = listener;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (touch_listener != null) touch_listener.onTouch(this, event);

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
        float topPadding = drawStripes(canvas);
        if (limit > 0) drawLimit(canvas);

        List<PointF> valuePoints = new ArrayList<PointF>();
        List<PointF> valueFuturePoints = new ArrayList<PointF>();
        addStartPoint(valuePoints);
        collectValuePoints(valuePoints, model, 0);

        if (futureModel != null){
            collectValuePoints(valueFuturePoints, futureModel, valuePoints.size()-1);
        }

        List<PointF> selectionValueList = new ArrayList<>(valuePoints);
        selectionValueList.addAll(valuePoints.size(), valueFuturePoints);

        PointF offset = new PointF(2, 4);
        if(originalTouch != null){
            drawSelection(canvas, selectionValueList, true, offset, valueShadowPaint);
        }

        if(!model.isEmpty()) {
            drawModelNew(canvas, valuePoints, valueShadowPaint, true, offset);
            if (futureModel != null && !futureModel.isEmpty()) {
                //add HEADER point from real values
                valueFuturePoints.add(0, Lists.getLast(valuePoints));
                drawModelNew(canvas, valueFuturePoints, valueShadowPaint, false, offset);
                drawModelNew(canvas, valueFuturePoints, futureValuePaint, false, new PointF(0, 0));
                //remove HEADER point from real values
                valueFuturePoints.remove(0);
            }
            drawModelNew(canvas, valuePoints, valuePaint, true, new PointF(0,0));
        }

        if(originalTouch != null){
            drawSelection(canvas, selectionValueList, false, new PointF(0,0), selectionValuePaint);
        }
    }

    private void collectValuePoints(List<PointF> valuePoints, List<Integer> contentModel, int lastPoint) {
        for (int i = 0,j=1; i < contentModel.size() ; i++,j++) {
            PointF pointF = new PointF(verticalAxisPadding + contentModel.get(i) * getMinuteWidth(),
                    getHeight() - horizontalAxisPadding - (lastPoint + j) * getItemHeight());
            valuePoints.add(pointF);
        }
    }

    private void drawModelNew(Canvas canvas, List<PointF> valuePoints, Paint paint, boolean drawStart, PointF offset) {
        Path path = new Path();
        PointF valuePoint = valuePoints.get(0);
        path.moveTo(valuePoint.x + offset.x, valuePoint.y + offset.y);
        paint.setStyle(Paint.Style.FILL);
        if(drawStart) {
            canvas.drawCircle(valuePoint.x + offset.x, valuePoint.y + offset.y,
                    stripeHeight / 4, paint);
        }
        for (int i = 1;i<valuePoints.size();i++){
            valuePoint = valuePoints.get(i);
            path.lineTo(valuePoint.x + offset.x, valuePoint.y + offset.y);
            canvas.drawCircle(valuePoint.x + offset.x, valuePoint.y + offset.y, stripeHeight / 2, paint);
        }
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawPath(path, paint);
    }


    private void drawSelection(Canvas canvas, List<PointF> valuePoints, boolean shadow, PointF offset, Paint paint) {
        paint.setStyle(Paint.Style.FILL);
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

        canvas.drawLine(touch.x+offset.x, 0+offset.y, touch.x+offset.x, getHeight() - horizontalAxisPadding+offset.y, paint);
        int minutesTotal = Math.round((touch.x - verticalAxisPadding)/getMinuteWidth());
        int minutes = minutesTotal%60;
        int hours = minutesTotal/60;
        String timeText = ((hours == 0)? "00":hours) +":"+((minutes<10)?"0"+minutes:minutes);
        Rect timeTextBounds = new Rect();
        Rect timeText2Bounds = new Rect();
        paint.getTextBounds(timeText,0,timeText.length(),timeTextBounds);
        paint.getTextBounds("W",0,1,timeText2Bounds);
        float textXPosition = touch.x - timeTextBounds.width()/2;
        if (textXPosition < verticalAxisPadding){
            textXPosition = verticalAxisPadding;
        } else if (textXPosition + timeTextBounds.width()  > getWidth()){
            textXPosition = getWidth() - timeTextBounds.width() - 5;
        }
        if (!shadow) canvas.drawText(timeText,textXPosition, getHeight()-2,paint);

        if (smokePoint != null){
            String text = smokePoint.first.toString();
            paint.getTextBounds(text,0,text.length(),timeTextBounds);
            float valueTextX = smokePoint.second.x - stripeHeight-timeTextBounds.width();
            if (valueTextX < (verticalAxisPadding*2)){
                valueTextX = smokePoint.second.x + stripeHeight;
            }
            if (!shadow) canvas.drawText(text,valueTextX, smokePoint.second.y -stripeHeight, paint);
            canvas.drawCircle(smokePoint.second.x+offset.x, smokePoint.second.y+offset.y, stripeHeight, paint);
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

    private void addStartPoint(List<PointF> valuePoints) {
        valuePoints.add(new PointF(verticalAxisPadding, getHeight() - horizontalAxisPadding));
    }

    private void drawLimit(Canvas canvas) {
        float itemHeight = getItemHeight();
        float limitYPosition = getHeight()-horizontalAxisPadding-itemHeight* limit;

        Rect textBounds = new Rect();
        String limitAsText = string_limit +Integer.toString(limit);
        limitLabelPaint.getTextBounds(limitAsText, 0, limitAsText.length(), textBounds);
        canvas.drawLine(
                verticalAxisPadding + verticalAxisTextBounds.height(),
                limitYPosition,
                getWidth(),
                limitYPosition,
                limitPaint);
        canvas.drawRoundRect(
                new RectF(
                    verticalAxisPadding + verticalAxisTextBounds.height() - stripeHeight/2,
                    limitYPosition - textBounds.height() / 2 - stripeHeight/2,
                    verticalAxisPadding + verticalAxisTextBounds.height() + textBounds.width()+stripeHeight/2,
                    limitYPosition + textBounds.height() / 2 + stripeHeight/2),
                5, 5,
                limitPaint);
        canvas.drawText(limitAsText,
                verticalAxisPadding + verticalAxisTextBounds.height(),
                limitYPosition + textBounds.height()/3,
                limitLabelPaint);
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

    private float getItemHeight() {
       float max = maxItemsOnABoard();
       float maxToDraw = Math.max(limit, model.size());
       if (max >= maxToDraw){
           //1 stripe : 1 smoke
           return stripeHeight;
       } else {
           return stripeHeight / ((maxToDraw+maxToDraw/5) / max);
       }
    }

    public void setModel(List<Date> values) {
        List<Integer> minutesModel = convertToMinutes(values);
        model = minutesModel;
    }

    public List<Integer> getModel() {
        return model;
    }

    private List<Integer> convertToMinutes(List<Date> values) {
        List<Integer> minutesModel = new ArrayList<Integer>(values.size());
        Calendar calendar = Calendar.getInstance();
        for (Date date : values) {
            calendar.setTime(date);
            int hours = calendar.get(Calendar.HOUR_OF_DAY);
            int minutes = calendar.get(Calendar.MINUTE);
            minutesModel.add(hours * 60 + minutes);
        }
        return minutesModel;
    }

    public float getMinuteWidth() {
        return ((float)(getWidth()-verticalAxisPadding)) / (25 * 60);
    }

    private float getMaxMinuteWidth() {
        return verticalAxisPadding + (24 * 60) * getMinuteWidth();
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public void setFutureModel(List<Date> futureModelDateList) {
        if (futureModelDateList == null){
            futureModel = null;
            return;
        }
        futureModel = convertToMinutes(futureModelDateList);
    }
}
