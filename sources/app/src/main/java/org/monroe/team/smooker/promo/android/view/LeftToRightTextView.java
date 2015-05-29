package org.monroe.team.smooker.promo.android.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.TextView;

public class LeftToRightTextView extends TextView{

    private float fraction = 1f;

    public LeftToRightTextView(Context context) {
        super(context);
    }

    public LeftToRightTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LeftToRightTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public LeftToRightTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public float getFraction() {
        return fraction;
    }

    public void setFraction(float fraction) {
        this.fraction = fraction;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.clipRect(0,0,getWidth()*fraction,getHeight());
        super.onDraw(canvas);
    }
}
