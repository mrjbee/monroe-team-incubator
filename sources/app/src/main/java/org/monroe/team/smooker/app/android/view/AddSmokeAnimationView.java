package org.monroe.team.smooker.app.android.view;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ComposeShader;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Property;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class AddSmokeAnimationView extends View {

    private Animator animator;

    private View sourceButton;
    private Bitmap sourceViewBitmap;
    private Bitmap tmpBitmap;
    private PointF sourceViewPosition;
    private PointF touchPoint;
    private Paint clearPaint;
    private Paint bitmapPaint;
    private float fraction = 1f;
    private float radius;

    public AddSmokeAnimationView(Context context) {
        super(context);
        init();
    }

    private void init() {
        clearPaint = new Paint();
        clearPaint.setStyle(Paint.Style.FILL);
        clearPaint.setColor(Color.BLACK);
        clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));


        bitmapPaint = new Paint();
        bitmapPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

    }

    public AddSmokeAnimationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AddSmokeAnimationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    public void setSourceButton(View sourceButton) {
        this.sourceButton = sourceButton;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (sourceViewBitmap != null){
            Canvas tmpCanvas = new Canvas(tmpBitmap);
            tmpCanvas.drawColor(Color.BLACK);
            tmpCanvas.drawCircle(touchPoint.x,touchPoint.y, radius * fraction, clearPaint);
            tmpCanvas.drawBitmap(sourceViewBitmap,0,0, bitmapPaint);


            canvas.drawBitmap(tmpBitmap,sourceViewPosition.x,sourceViewPosition.y, null);
        }
    }


    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public void onAddSmoke(MotionEvent event) {
        if (sourceViewBitmap == null) {
            sourceViewBitmap = Bitmap.createBitmap(sourceButton.getWidth(), sourceButton.getHeight(), Bitmap.Config.ARGB_8888);
            tmpBitmap = Bitmap.createBitmap(sourceButton.getWidth(), sourceButton.getHeight(), Bitmap.Config.ARGB_8888);
        }
        sourceButton.draw(new Canvas(sourceViewBitmap));
        sourceViewPosition = new PointF(sourceButton.getX(),sourceButton.getY());
        touchPoint = new PointF(event.getX(),event.getY());
        radius = calculateMaxRadius();
        fraction = 0;
        animator = ObjectAnimator.ofFloat(this,new Property<AddSmokeAnimationView, Float>(Float.class, "fraction") {
            @Override
            public Float get(AddSmokeAnimationView object) {
                return object.fraction;
            }

            @Override
            public void set(AddSmokeAnimationView object, Float value) {
                object.setFraction(value);
            }
        },1f);
        animator.setDuration(300);
        animator.setInterpolator(new AccelerateInterpolator());
        animator.start();

    }

    private float calculateMaxRadius() {
        float x_radius = Math.max(
                Math.abs(touchPoint.x - sourceViewBitmap.getWidth()),
                Math.abs(touchPoint.x));
        float y_radius = Math.max(
                Math.abs(touchPoint.y - sourceViewBitmap.getHeight()),
                Math.abs(touchPoint.y));

        return Math.max(x_radius,y_radius);
    }

    private void setFraction(float fraction) {
        this.fraction = fraction;
        invalidate();
    }
}
