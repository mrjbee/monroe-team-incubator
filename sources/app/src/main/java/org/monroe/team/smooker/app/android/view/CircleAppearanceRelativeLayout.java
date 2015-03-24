package org.monroe.team.smooker.app.android.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public class CircleAppearanceRelativeLayout extends RelativeLayout {

    private Bitmap originImageBitmap;
    private Bitmap finalImageBitmap;
    private RectF componentSize;

    private Paint maskDefinePaint;
    private Paint maskApplyPaint;

    private float fraction = 1f;
    private PointF center = new PointF(100,100);

    public CircleAppearanceRelativeLayout(Context context) {
        super(context);
        init();
    }

    private void init() {
        setBackgroundColor(Color.TRANSPARENT);

        maskDefinePaint = new Paint();
        maskDefinePaint.setColor(Color.BLACK);

        maskApplyPaint = new Paint();
        maskApplyPaint.setAntiAlias(true);
        maskApplyPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
    }

    public CircleAppearanceRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CircleAppearanceRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CircleAppearanceRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    @Override
    public void draw(Canvas canvas) {
        if (fraction == 1f){
            componentSize = null;
            super.draw(canvas);
            return;
        }

        if (componentSize == null ||
                componentSize.width() != getWidth() ||
                componentSize.height() != getHeight()){
            componentSize = new RectF(0,0,getWidth(),getHeight());
            finalImageBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            originImageBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            super.draw(new Canvas(originImageBitmap));
        }
        finalImageBitmap.eraseColor(Color.TRANSPARENT);
        Canvas finalCanvas = new Canvas(finalImageBitmap);
        finalCanvas.drawCircle(center.x, center.y, calculateRadius(componentSize), maskDefinePaint);
        finalCanvas.drawBitmap(originImageBitmap, 0, 0, maskApplyPaint);
        canvas.drawBitmap(finalImageBitmap, 0, 0, null);
    }

    private float calculateRadius(RectF componentSize) {
        return fraction * Math.max(componentSize.width(), componentSize.height());
    }

    public float getFraction() {
        return fraction;
    }

    public void setFraction(float fraction) {
        this.fraction = fraction;
    }

    public PointF getCenter() {
        return center;
    }

    public void setCenter(PointF center) {
        this.center = center;
    }

    public void setCenter(float x, float y) {
        this.center.set(x,y);
    }
}
