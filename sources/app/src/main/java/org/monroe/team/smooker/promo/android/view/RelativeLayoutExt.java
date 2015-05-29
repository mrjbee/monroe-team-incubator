package org.monroe.team.smooker.promo.android.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

public class RelativeLayoutExt extends RelativeLayout{


    private TranslationListener translationListener = new TranslationListener(){
        @Override
        public void onX(View source, float translationX) {}

        @Override
        public void onY(View view, float translationY) {}
    };

    public RelativeLayoutExt(Context context) {
        super(context);
    }

    public RelativeLayoutExt(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RelativeLayoutExt(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public RelativeLayoutExt(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    @Override
    public void setTranslationX(float translationX) {
        super.setTranslationX(translationX);
        translationListener.onX(this, translationX);
    }

    @Override
    public void setTranslationY(float translationY) {
        super.setTranslationY(translationY);
        translationListener.onY(this, translationY);
    }


    public TranslationListener getTranslationListener() {
        return translationListener;
    }

    public void setTranslationListener(TranslationListener translationListener) {
        this.translationListener = translationListener;
    }

    public interface TranslationListener{
        void onX(View source, float translationX);
        void onY(View view, float translationY);
    }

}
