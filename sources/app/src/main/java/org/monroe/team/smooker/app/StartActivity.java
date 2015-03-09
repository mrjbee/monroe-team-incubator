package org.monroe.team.smooker.app;

import android.animation.Animator;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.monroe.team.android.box.app.ActivitySupport;
import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.*;

import org.monroe.team.android.box.app.ui.AppearanceControllerOld;
import org.monroe.team.android.box.app.ui.SlideTouchGesture;
import org.monroe.team.android.box.app.ui.animation.ViewAnimatorFactorySupport;
import org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceController;
import org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder;
import org.monroe.team.android.box.app.ui.animation.apperrance.DefaultAppearanceController;
import org.monroe.team.android.box.utils.DisplayUtils;
import org.monroe.team.smooker.app.android.SmookerApplication;


public class StartActivity extends ActivitySupport<SmookerApplication> {

    AppearanceController bottomLayerAC;
    AppearanceController titleTextAC;
    AppearanceController pickerRotationAC;
    AppearanceController tileBigDataSpaceAC;
    AppearanceController tileBigDataAC;

    float dashDelta;
    float titleSmallSize;
    float titleBigSize;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        titleSmallSize =  DisplayUtils.spToPx(20,getResources());
        titleBigSize = DisplayUtils.spToPx(30, getResources());
        dashDelta = DisplayUtils.dpToPx(100 + 250, getResources());
        setContentView(R.layout.activity_start);
        bottomLayerAC = animateAppearance(view(R.id.start_bottom_layer),ySlide(-dashDelta,0))
                .showAnimation(duration_constant(400), interpreter_overshot())
                .hideAnimation(duration_auto_fint(), interpreter_decelerate(0.3f))
                .build();
        titleTextAC = animateAppearance(view(R.id.start_title_text),
                scale(1.3f, 0.9f))
                .showAnimation(duration_constant(300), interpreter_overshot())
                .hideAnimation(duration_auto_fint(), interpreter_decelerate(0.3f))
                .build();
        pickerRotationAC = animateAppearance(view(R.id.start_open_picker_arrow_image), rotate(0f, 180f))
                .showAnimation(duration_constant(300), interpreter_accelerate(0.3f))
                .hideAnimation(duration_constant(300), interpreter_decelerate(0.3f))
                .build();

        tileBigDataSpaceAC = animateAppearance(view(R.id.start_tile_space_wrap_panel), heightSlide(
                (int) DisplayUtils.dpToPx(23+60,getResources()),
                (int) DisplayUtils.dpToPx(420,getResources())))
                .showAnimation(duration_constant(200), interpreter_accelerate(0.3f))
                .hideAnimation(duration_auto_int(), interpreter_decelerate(0.3f)).build();

        tileBigDataAC = animateAppearance(view(R.id.start_tile_big_content), heightSlide(
                (int) DisplayUtils.dpToPx(300,getResources()),0))
                .showAnimation(duration_constant(200), interpreter_overshot())
                .hideAnimation(duration_constant(300), interpreter_decelerate(0.5f))
                .hideAndGone().build();

        tileBigDataAC.hide();
        tileBigDataSpaceAC.hide();
        pickerRotationAC.show();
        bottomLayerAC.hideWithoutAnimation();
        titleTextAC.hideWithoutAnimation();
        setupDashCloseState();
    }


    //when picker is down
    private void setupDashCloseState() {
        view(R.id.start_open_picker_panel).setOnTouchListener(new SlideTouchGesture(400, SlideTouchGesture.Axis.Y_UP) {
            @Override
            protected void onProgress(float x, float y, float slideValue, float fraction) {
               view(R.id.start_bottom_layer).setTranslationY((float) (-400 *(fraction)));
               view(R.id.start_tile_space_wrap_panel).getLayoutParams().height = (int) (DisplayUtils.dpToPx(420,getResources())-400*fraction);
               view(R.id.start_tile_space_wrap_panel).requestLayout();
            }

            @Override
            protected void onApply(float x, float y, float slideValue, float fraction) {
                bottomLayerAC.showAndCustomize(new AppearanceController.AnimatorCustomization() {
                    @Override
                    public void customize(Animator animator) {
                        animator.addListener(new AppearanceControllerOld.AnimatorListenerAdapter(){
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                setupDashOpenState();
                                tileBigDataAC.show();
                            }
                        });
                    }
                });
                titleTextAC.show();
                pickerRotationAC.hide();
                tileBigDataSpaceAC.show();
            }

            @Override
            protected void onCancel(float x, float y, float slideValue, float fraction) {
                bottomLayerAC.hide();
                tileBigDataSpaceAC.hide();
            }

        });
    }

    private void setupDashOpenState() {
        final float startTranslation = view(R.id.start_bottom_layer).getTranslationY();
        final int startHeight = view(R.id.start_tile_space_wrap_panel).getLayoutParams().height;
        view(R.id.start_open_picker_panel).setOnTouchListener(new SlideTouchGesture(dashDelta, SlideTouchGesture.Axis.Y_DOWN) {
            @Override
            protected void onProgress(float x, float y, float slideValue, float fraction) {
                view(R.id.start_bottom_layer).setTranslationY((float) (startTranslation + dashDelta * fraction));
                float scaleFactor = 1.3f - 0.5f *fraction;
                view(R.id.start_title_text,TextView.class).setScaleX(scaleFactor);
                view(R.id.start_title_text,TextView.class).setScaleY(scaleFactor);
                view(R.id.start_tile_space_wrap_panel).getLayoutParams().height = (int) (startHeight + dashDelta * fraction);
                view(R.id.start_tile_space_wrap_panel).requestLayout();
            }

            @Override
            protected void onApply(float x, float y, float slideValue, float fraction) {
                tileBigDataSpaceAC.hide();
                bottomLayerAC.hideAndCustomize(new AppearanceController.AnimatorCustomization() {
                    @Override
                    public void customize(Animator animator) {
                        animator.addListener(new AppearanceControllerOld.AnimatorListenerAdapter(){
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                setupDashCloseState();
                            }
                        });
                    }
                });
                titleTextAC.hide();
                pickerRotationAC.show();
            }

            @Override
            protected void onCancel(float x, float y, float slideValue, float fraction) {
                bottomLayerAC.show();
                titleTextAC.show();
                tileBigDataSpaceAC.showAndCustomize(new AppearanceController.AnimatorCustomization() {
                    @Override
                    public void customize(Animator animator) {
                        animator.addListener(new AppearanceControllerOld.AnimatorListenerAdapter(){
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                tileBigDataAC.show();
                            }
                        });
                    }
                });
            }

            @Override
            protected void onStart(float x, float y) {
                tileBigDataAC.hide();
            }
        });
    }


}
