package org.monroe.team.smooker.app.android.view;

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

    public RoundSegmentImageView(Context context) {
        super(context);
    }

    public RoundSegmentImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RoundSegmentImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public RoundSegmentImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
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

        Paint paint = new Paint();
        paint.setColor(Color.BLACK);

        RectF archRect = new RectF(0,0,getWidth(),getHeight());
        float startAngle = -90;

        Bitmap originImageBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        super.onDraw(new Canvas(originImageBitmap));

        Bitmap shadowImageBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas shadowImageCanvas = new Canvas(shadowImageBitmap);

        shadowImageCanvas.drawArc(archRect, startAngle, angle, true, paint);

        final Paint shadowPaint = new Paint();
        shadowPaint.setAntiAlias(true);
        shadowPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        shadowImageCanvas.drawBitmap(originImageBitmap, 0, 0, shadowPaint);

        if (false) {
            //DEBUG
            Paint debug = new Paint();
            debug.setColor(Color.RED);
            canvas.drawArc(archRect, startAngle, angle, true, debug);
        }
        canvas.drawBitmap(shadowImageBitmap, 0, 0, null);

    }
}
