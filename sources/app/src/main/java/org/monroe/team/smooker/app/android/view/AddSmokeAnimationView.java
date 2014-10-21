package org.monroe.team.smooker.app.android.view;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class AddSmokeAnimationView extends View {


    private Support support;
    private float fraction = 0f;

    public AddSmokeAnimationView(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= 14){
            //support animation
            support = new AddSmokeAnimation14ApiSupport(this,context);
        } else {
            support = new NoOpSupport();
        }
    }


    public AddSmokeAnimationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public AddSmokeAnimationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }


    public void setSourceButton(View sourceButton) {
        support.setSourceButton(sourceButton);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        support.doDraw(canvas);
    }


    public void onAddSmoke(MotionEvent event) {
        support.onAddSmoke(event);
    }


    void setFraction(float fraction) {
        this.fraction = fraction;
        invalidate();
    }

    public float getFraction() {
        return fraction;
    }

    public static interface Support{
        void setSourceButton(View sourceButton);
        void doDraw(Canvas canvas);
        void onAddSmoke(MotionEvent event);
    }

    public static class NoOpSupport implements Support{
        @Override
        public void setSourceButton(View sourceButton) {

        }

        @Override
        public void doDraw(Canvas canvas) {

        }

        @Override
        public void onAddSmoke(MotionEvent event) {

        }
    }
}
