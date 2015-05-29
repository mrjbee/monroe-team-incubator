package org.monroe.team.smooker.promo.android;

import android.animation.Animator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

import org.monroe.team.android.box.app.ActivitySupport;
import org.monroe.team.android.box.app.ui.animation.AnimatorListenerSupport;
import org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceController;
import org.monroe.team.smooker.promo.R;
import org.monroe.team.smooker.promo.actors.ActorNotification;

import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.animateAppearance;
import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.combine;
import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.duration_constant;
import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.interpreter_accelerate;
import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.interpreter_overshot;
import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.rotate;
import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.scale;


public class SmokeBreakActivity extends ActivitySupport<SmookerApplication> {

    private AppearanceController exitBtnAC;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smoke_break);

        exitBtnAC = combine(
                animateAppearance(view(R.id.sb_back_btn), scale(1f, 0f))
                        .showAnimation(duration_constant(400), interpreter_overshot())
                        .hideAnimation(duration_constant(400), interpreter_accelerate(0.4f))
                        .hideAndInvisible(),
                animateAppearance(view(R.id.sb_back_btn), rotate(360, 0))
                        .showAnimation(duration_constant(300), interpreter_overshot())
                        .hideAnimation(duration_constant(300), interpreter_accelerate(0.4f))
        );

        if (isFirstRun(savedInstanceState)){
            exitBtnAC.hideWithoutAnimation();
        }else {
            exitBtnAC.showWithoutAnimation();
        }

        //in case no action
        application().getSuggestionsController().scheduleFallback();

        View.OnTouchListener listener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN){
                    v.setBackgroundColor(getResources().getColor(R.color.background_main));
                }

                if (event.getAction() == MotionEvent.ACTION_UP){
                    v.setBackgroundColor(Color.TRANSPARENT);
                    onOption(v.getId());
                }

                if (event.getAction() == MotionEvent.ACTION_CANCEL){
                    v.setBackgroundColor(Color.TRANSPARENT);
                }
                v.invalidate();
                return true;
            }
        };

        view(R.id.sb_option_smoke_layout).setOnTouchListener(listener);
        view(R.id.sb_option_skip_layout).setOnTouchListener(listener);
        view(R.id.sb_option_postpone_layout).setOnTouchListener(listener);
        view(R.id.sb_option_smoke_layout).setOnTouchListener(listener);

        view(R.id.sb_back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exitBtnAC.hideAndCustomize(new AppearanceController.AnimatorCustomization() {
                    @Override
                    public void customize(Animator animator) {
                        animator.addListener(new AnimatorListenerSupport(){
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                finish();
                            }
                        });
                    }
                });
            }
        });
    }

    private void onOption(int id) {
        Intent intent = null;
        switch (id){
            case R.id.sb_option_postpone_layout:{
                intent = ActorNotification.create(this, ActorNotification.POSTPONE_SMOKE).toast().buildIntent();
                break;
            }
            case R.id.sb_option_skip_layout:{
                intent = ActorNotification.create(this, ActorNotification.SKIP_SMOKE).toast().buildIntent();
                break;
            }
            case R.id.sb_option_smoke_layout:{
                intent = ActorNotification.create(this, ActorNotification.ADD_SMOKE).toast().buildIntent();
                break;
            }
        }
       sendBroadcast(intent);
       finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(isFirstRun()){
            runLastOnUiThread(new Runnable() {
                @Override
                public void run() {
                    exitBtnAC.show();
                }
            },1000);
        }
    }
}
