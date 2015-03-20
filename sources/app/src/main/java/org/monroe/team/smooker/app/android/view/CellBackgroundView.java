package org.monroe.team.smooker.app.android.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;


public class CellBackgroundView extends View{

    private Paint cellPaint;
    private Paint weekPaint;

    public boolean paintTop = true;
    public boolean paintBottom = true;
    public boolean paintLeft = true;
    public boolean paintRight = true;
    public boolean paintWeekEnd = false;
    public boolean paintWeekStart = false;

    public CellBackgroundView(Context context) {
        super(context);
        init(context);
    }


    public CellBackgroundView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CellBackgroundView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CellBackgroundView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        cellPaint = new Paint();
        cellPaint.setColor(Color.parseColor("#f1f0f0"));
        cellPaint.setStyle(Paint.Style.STROKE);
        cellPaint.setStrokeWidth(1f);

        weekPaint = new Paint();
        weekPaint.setColor(Color.parseColor("#d02636"));
        weekPaint.setStyle(Paint.Style.STROKE);
        weekPaint.setStrokeWidth(6f);

    }

    public void resetAll(){
        paintTop = true;
        paintBottom = true;
        paintLeft = true;
        paintRight = true;

        paintWeekEnd = false;
        paintWeekStart = false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Paint paint = cellPaint;
        if (paintTop){
            drawTopBorder(canvas, paint);
        }
        if (paintBottom){
            drawBottomBorder(canvas, paint);
        }
        if (paintLeft){
            drawLeftBorder(canvas, paint);
        }
        if (paintRight){
            drawRightBorder(canvas, paint);
        }
        if (paintWeekEnd){
            drawBottomBorder(canvas, weekPaint);
        }
        if (paintWeekStart){
            drawTopBorder(canvas, weekPaint);
        }
    }

    private void drawRightBorder(Canvas canvas, Paint paint) {
        canvas.drawLine(getWidth()-paint.getStrokeWidth()/2,0,getWidth()-paint.getStrokeWidth()/2,getHeight(),paint);
    }

    private void drawLeftBorder(Canvas canvas, Paint paint) {
        canvas.drawLine(0+paint.getStrokeWidth()/2,0,0+paint.getStrokeWidth()/2,getHeight(),paint);
    }

    private void drawBottomBorder(Canvas canvas, Paint paint) {
        canvas.drawLine(0,
                getHeight()-paint.getStrokeWidth()/2,
                getWidth(),
                getHeight()-paint.getStrokeWidth()/2,paint);
    }

    private void drawTopBorder(Canvas canvas, Paint paint) {
        canvas.drawLine(0,0+paint.getStrokeWidth()/2,getWidth(),0+paint.getStrokeWidth()/2,paint);
    }
}
