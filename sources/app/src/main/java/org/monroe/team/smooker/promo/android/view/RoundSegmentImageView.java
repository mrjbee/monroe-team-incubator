package org.monroe.team.smooker.promo.android.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ImageView;

public class RoundSegmentImageView extends ImageView{

    private float angle;
    private Bitmap originImageBitmap;
    private Bitmap finalImageBitmap;
    private RectF lastUsedArchRect;

    private Paint maskDefinePaint;
    private Paint maskApplyPaint;

    public RoundSegmentImageView(Context context) {
        super(context);
        init();
    }

    public RoundSegmentImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RoundSegmentImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public RoundSegmentImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        maskDefinePaint = new Paint();
        maskDefinePaint.setColor(Color.BLACK);

        maskApplyPaint = new Paint();
        maskApplyPaint.setAntiAlias(true);
        maskApplyPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
    }

    public float getAngle() {
        return angle;
    }

    public void setAngle(float angle) {
        this.angle = angle;
    }

    //TODO: require optimization
    @Override
    protected void onDraw(Canvas canvas) {
        float startAngle = -90;
        if (lastUsedArchRect == null ||
                lastUsedArchRect.width() != getWidth() ||
                lastUsedArchRect.height() != getHeight()){
            lastUsedArchRect = new RectF(0,0,getWidth(),getHeight());
            finalImageBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            originImageBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            super.onDraw(new Canvas(originImageBitmap));
        }

        finalImageBitmap.eraseColor(Color.TRANSPARENT);
        Canvas finalCanvas = new Canvas(finalImageBitmap);
        finalCanvas.drawArc(lastUsedArchRect, startAngle, angle, true, maskDefinePaint);
        finalCanvas.drawBitmap(originImageBitmap, 0, 0, maskApplyPaint);

        debug(canvas, startAngle);

        canvas.drawBitmap(finalImageBitmap, 0, 0, null);
    }

    private void debug(Canvas canvas, float startAngle) {
        if (false) {
            //DEBUG
            Paint debug = new Paint();
            debug.setColor(Color.RED);
            canvas.drawArc(lastUsedArchRect, startAngle, angle, true, debug);
        }
    }
}
