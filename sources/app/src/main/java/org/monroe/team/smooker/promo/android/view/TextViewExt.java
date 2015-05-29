package org.monroe.team.smooker.promo.android.view;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.TextView;

import org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceController;

import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.animateAppearance;
import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.duration_auto_fint;
import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.duration_constant;
import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.interpreter_decelerate;
import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.interpreter_overshot;
import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.rotate;

public class TextViewExt extends TextView{

    private AppearanceController changeAnimation;

    public TextViewExt(Context context) {
        super(context);
        init(context);
    }

    public TextViewExt(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TextViewExt(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public TextViewExt(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        changeAnimation = animateAppearance(this,
                rotate(10f, 0f))
                .showAnimation(duration_constant(200), interpreter_overshot())
                .hideAnimation(duration_constant(100), interpreter_decelerate(0.3f))
                .build();
        changeAnimation.hideWithoutAnimation();
    }

    public final void setText(final String text, final boolean animation){
        if (text.equals(getText().toString())) return;

        if (!animation){
            setText(text);
        } else {
           changeAnimation.showAndCustomize(new AppearanceController.AnimatorCustomization() {
               @Override
               public void customize(Animator animator) {
                   animator.addListener(new AnimatorListenerAdapter() {
                       @Override
                       public void onAnimationEnd(Animator animation) {
                           setText(text);
                           changeAnimation.hide();
                       }
                   });
               }
           });
        }
    }



}
