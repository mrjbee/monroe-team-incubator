package org.monroe.team.smooker.promo.android.view;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Build;
import android.util.Property;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;


@Deprecated
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class AddSmokeAnimation14ApiSupport implements AddSmokeAnimationView.Support{

    private Animator animator;
    private AddSmokeAnimationView owner;
    private View sourceButton;
    private Bitmap sourceViewBitmap;
    private Bitmap tmpBitmap;
    private PointF sourceViewPosition;
    private PointF touchPoint;
    private Paint clearPaint;
    private Paint bitmapPaint;
    private float radius;

    public AddSmokeAnimation14ApiSupport(AddSmokeAnimationView owner, Context context) {
        this.owner = owner;
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

    public void setSourceButton(View sourceButton) {
        this.sourceButton = sourceButton;
    }

    @Override
    public void doDraw(Canvas canvas) {
       if (sourceViewBitmap != null){
            Canvas tmpCanvas = new Canvas(tmpBitmap);
            tmpCanvas.drawColor(Color.BLACK);
            tmpCanvas.drawCircle(touchPoint.x,touchPoint.y, radius * owner.getFraction() , clearPaint);
            tmpCanvas.drawBitmap(sourceViewBitmap,0,0, bitmapPaint);
            canvas.drawBitmap(tmpBitmap,sourceViewPosition.x,sourceViewPosition.y, null);
        }
    }


    public void onAddSmoke(MotionEvent event) {
        if (sourceViewBitmap == null) {
            sourceViewBitmap = Bitmap.createBitmap(sourceButton.getWidth(), sourceButton.getHeight(), Bitmap.Config.ARGB_8888);
            tmpBitmap = Bitmap.createBitmap(sourceButton.getWidth(), sourceButton.getHeight(), Bitmap.Config.ARGB_8888);
        }
        sourceButton.draw(new Canvas(sourceViewBitmap));
        sourceViewPosition = new PointF(sourceButton.getX(),sourceButton.getY());
        touchPoint = new PointF(event.getX(),event.getY());
        radius = calculateMaxRadius();
        owner.setFraction(0);
        animator = ObjectAnimator.ofFloat(owner, new Property<AddSmokeAnimationView, Float>(Float.class, "fraction") {
            @Override
            public Float get(AddSmokeAnimationView object) {
                return object.getFraction();
            }

            @Override
            public void set(AddSmokeAnimationView object, Float value) {
                object.setFraction(value);
            }
        }, 1f);
        animator.setDuration(300);
        animator.setInterpolator(new AccelerateInterpolator());
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                owner.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                owner.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                owner.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
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

}
