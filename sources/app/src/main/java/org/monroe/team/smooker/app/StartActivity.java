package org.monroe.team.smooker.app;

import android.animation.Animator;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import org.monroe.team.android.box.app.ActivitySupport;
import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.*;

import org.monroe.team.android.box.app.ui.AppearanceControllerOld;
import org.monroe.team.android.box.app.ui.SlideTouchGesture;
import org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceController;
import org.monroe.team.android.box.utils.DisplayUtils;
import org.monroe.team.smooker.app.android.SmookerApplication;


public class StartActivity extends ActivitySupport<SmookerApplication> {

    AppearanceController bottomLayerAC;
    AppearanceController tileCaptionTextAC;
    AppearanceController pickerRotationAC;
    AppearanceController tileBigDataSpaceAC;
    AppearanceController tileBigDataAC;
    AppearanceController tileShowFromLeftAC;
    AppearanceController tileShowFromRightAC;
    AppearanceController tileShowAC;

    float dashDelta;
    float titleSmallSize;
    float titleBigSize;
    private AppearanceController tileCaptionTextChangeAC;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        titleSmallSize =  DisplayUtils.spToPx(20,getResources());
        titleBigSize = DisplayUtils.spToPx(30, getResources());
        dashDelta = DisplayUtils.dpToPx(100 + 250, getResources());
        setContentView(R.layout.activity_start);
        bottomLayerAC = animateAppearance(view(R.id.start_bottom_layer),ySlide(-dashDelta,0))
                .showAnimation(duration_constant(400), interpreter_overshot())
                .hideAnimation(duration_auto_fint(0.5f), interpreter_decelerate(0.3f))
                .build();
        tileCaptionTextAC = animateAppearance(view(R.id.start_tile_caption_text),
                scale(1.3f, 0.9f))
                .showAnimation(duration_constant(300), interpreter_overshot())
                .hideAnimation(duration_auto_fint(), interpreter_decelerate(0.3f))
                .build();
        tileCaptionTextChangeAC = animateAppearance(view(R.id.start_tile_caption_text),
                rotate(0f,90f))
                .showAnimation(duration_constant(200), interpreter_overshot())
                .hideAnimation(duration_constant(100), interpreter_accelerate(0.3f))
                .build();
        pickerRotationAC = animateAppearance(view(R.id.start_open_picker_arrow_image), rotate(0f, 180f))
                .showAnimation(duration_constant(300), interpreter_accelerate(0.3f))
                .hideAnimation(duration_constant(300), interpreter_decelerate(0.3f))
                .build();

        tileBigDataSpaceAC = animateAppearance(view(R.id.start_tile_space_wrap_panel), heightSlide(
                (int) DisplayUtils.dpToPx(23 + 60, getResources()),
                (int) DisplayUtils.dpToPx(420, getResources())))
                .showAnimation(duration_constant(200), interpreter_accelerate(0.3f))
                .hideAnimation(duration_auto_int(0.5f), interpreter_decelerate(0.3f)).build();

        tileBigDataAC = animateAppearance(view(R.id.start_tile_big_content), heightSlide(
                (int) DisplayUtils.dpToPx(300, getResources()), 0))
                .showAnimation(duration_constant(200), interpreter_overshot())
                .hideAnimation(duration_constant(300), interpreter_decelerate(0.5f))
                .hideAndGone().build();

        tileShowFromLeftAC = animateAppearance(view(R.id.start_tile_content),
                    xSlide(0,-DisplayUtils.screenWidth(getResources()))
                )
                .showAnimation(duration_constant(200), interpreter_decelerate(0.5f))
                .hideAnimation(duration_constant(200), interpreter_decelerate(0.5f))
                .hideAndGone().build();

        tileShowFromRightAC = animateAppearance(view(R.id.start_tile_content),
                   xSlide(0, DisplayUtils.screenWidth(getResources()))
                )
                .showAnimation(duration_constant(200), interpreter_decelerate(0.5f))
                .hideAnimation(duration_constant(100), interpreter_accelerate(0.2f))
                .hideAndGone().build();

        tileShowAC = animateAppearance(view(R.id.start_tile_content),
                xSlide(0,-DisplayUtils.screenWidth(getResources()))
        )
                .showAnimation(duration_constant(200), interpreter_overshot())
                .hideAnimation(duration_constant(200), interpreter_decelerate(0.5f))
                .hideAndGone().build();


        tileShowFromLeftAC.showWithoutAnimation();
        tileShowFromRightAC.showWithoutAnimation();
        tileBigDataAC.hide();
        tileBigDataSpaceAC.hide();
        pickerRotationAC.show();
        bottomLayerAC.hideWithoutAnimation();
        tileCaptionTextAC.hideWithoutAnimation();
        tileCaptionTextChangeAC.show();
        setupDashCloseState();

        view(R.id.start_tile_content).setOnTouchListener(new SlideTouchGesture(DisplayUtils.dpToPx(400,getResources()), SlideTouchGesture.Axis.X) {
            @Override
            protected void onProgress(float x, float y, float slideValue, float fraction) {
                view(R.id.start_tile_content).setTranslationX(-slideValue);
            }

            @Override
            protected void onCancel(float x, float y, float slideValue, float fraction) {
                //AppearanceController ac = (slideValue >0)? tileShowFromLeftAC:tileShowFromRightAC;
                AppearanceController ac = tileShowAC;
                ac.show();
            }

            @Override
            protected void onApply(float x, float y, float slideValue, float fraction) {
                AppearanceController hideAC = (slideValue >0)? tileShowFromLeftAC:tileShowFromRightAC;
                final AppearanceController showAC = (slideValue <0)? tileShowFromLeftAC:tileShowFromRightAC;
                hideAC.hideAndCustomize(new AppearanceController.AnimatorCustomization() {
                    @Override
                    public void customize(Animator animator) {
                        animator.addListener(new AppearanceControllerOld.AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(final Animator animation) {
                                showAC.hideWithoutAnimation();
                                showAC.show();
                                tileCaptionTextChangeAC.hideAndCustomize(new AppearanceController.AnimatorCustomization() {
                                    @Override
                                    public void customize(Animator changeAnimator) {
                                        changeAnimator.addListener(new AppearanceControllerOld.AnimatorListenerAdapter(){
                                            @Override
                                            public void onAnimationEnd(Animator animation) {
                                                String text = view_text(R.id.start_tile_caption_text).getText().toString();
                                                if (text.equals("Motivation")){
                                                   view_text(R.id.start_tile_caption_text).setText("Statistics");
                                                }else{
                                                    view_text(R.id.start_tile_caption_text).setText("Motivation");
                                                }
                                                tileCaptionTextChangeAC.show();
                                            }
                                        });
                                    }
                                });
                            }

                        });
                    }
                });
            }
        });
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
                tileCaptionTextAC.show();
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
                view(R.id.start_tile_caption_text,TextView.class).setScaleX(scaleFactor);
                view(R.id.start_tile_caption_text,TextView.class).setScaleY(scaleFactor);
                view(R.id.start_tile_space_wrap_panel).getLayoutParams().height = (int) (startHeight + dashDelta * fraction);
                view(R.id.start_tile_space_wrap_panel).requestLayout();
            }

            @Override
            protected void onApply(float x, float y, float slideValue, float fraction) {
                closeDash();
            }

            @Override
            protected void onCancel(float x, float y, float slideValue, float fraction) {
                bottomLayerAC.show();
                tileCaptionTextAC.show();
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
                onStartingCloseDash();
            }
        });
    }

    private void onStartingCloseDash() {
        tileBigDataAC.hide();
    }

    private void closeDash() {
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
        tileCaptionTextAC.hide();
        pickerRotationAC.show();
    }

    @Override
    public void onBackPressed() {
        if (view(R.id.start_tile_big_content).getVisibility() == View.VISIBLE){
            onStartingCloseDash();
            closeDash();
        }else{
            super.onBackPressed();
        }
    }
}
