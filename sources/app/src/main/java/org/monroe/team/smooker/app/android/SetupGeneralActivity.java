package org.monroe.team.smooker.app.android;

import android.animation.Animator;
import android.graphics.PointF;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import org.monroe.team.android.box.app.ActivitySupport;
import org.monroe.team.android.box.app.ui.AppearanceControllerOld;
import org.monroe.team.android.box.app.ui.animation.AnimatorListenerSupport;
import org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceController;
import org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder;
import org.monroe.team.android.box.app.ui.animation.apperrance.DefaultAppearanceController;
import org.monroe.team.smooker.app.R;
import org.monroe.team.smooker.app.android.view.CircleAppearanceRelativeLayout;

import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.alpha;
import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.animateAppearance;
import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.combine;
import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.duration_auto_fint;
import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.duration_constant;
import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.interpreter_accelerate;
import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.interpreter_decelerate;
import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.interpreter_overshot;
import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.rotate;
import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.scale;
import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.ySlide;

public abstract class SetupGeneralActivity extends ActivitySupport<SmookerApplication> {


    private AppearanceController baseContainerAC;
    private AppearanceController contentContainerAC;
    private AppearanceController exitBtnAC;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        crunch_requestNoAnimation();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_general);
        view(R.id.setup_quit_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        PointF position = getFromIntent("position" , null);
        CircleAppearanceRelativeLayout baseContainer= view(R.id.setup_base_container, CircleAppearanceRelativeLayout.class);
        baseContainer.setCenter(position);
        baseContainerAC = animateAppearance(baseContainer, circleGrowing())
                .showAnimation(duration_constant(500), interpreter_accelerate(0.8f))
                .hideAnimation(duration_constant(300), interpreter_decelerate(0.8f))
                .build();
        contentContainerAC = animateAppearance(view(R.id.setup_content_panel),alpha(1f,0f))
                .showAnimation(duration_constant(200), interpreter_accelerate(0.8f))
                .hideAnimation(duration_constant(200), interpreter_decelerate(0.8f))
                .hideAndGone()
                .build();

        exitBtnAC = combine(
                animateAppearance(view(R.id.setup_quit_btn), scale(1f, 0f))
                        .showAnimation(duration_constant(400), interpreter_overshot())
                        .hideAnimation(duration_constant(400), interpreter_accelerate(0.4f))
                        .hideAndInvisible(),
                animateAppearance(view(R.id.setup_quit_btn), rotate(360, 0))
                        .showAnimation(duration_constant(300), interpreter_overshot())
                        .hideAnimation(duration_constant(300), interpreter_accelerate(0.4f))
        );

        if (isFirstRun()){
            baseContainerAC.hideWithoutAnimation();
            contentContainerAC.hideWithoutAnimation();
            exitBtnAC.hideWithoutAnimation();
        }else {
            baseContainerAC.showWithoutAnimation();
            contentContainerAC.showWithoutAnimation();
            exitBtnAC.showWithoutAnimation();
            fillUI();
        }
    }

    private void fillUI() {
        getLayoutInflater().inflate(setup_layout(), (ViewGroup) view(R.id.setup_content_panel), true);
        initializeUI();
    }

    protected abstract void initializeUI();

    @Override
    protected void onResume() {
        super.onResume();
        if (isFirstRun()) {
            baseContainerAC.showAndCustomize(new AppearanceController.AnimatorCustomization() {
                @Override
                public void customize(Animator animator) {
                    animator.addListener(new AnimatorListenerSupport(){
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            fillUI();
                            contentContainerAC.showAndCustomize(new AppearanceController.AnimatorCustomization() {
                                @Override
                                public void customize(Animator ani) {
                                    ani.addListener(new AnimatorListenerSupport(){
                                        @Override
                                        public void onAnimationEnd(Animator animation) {
                                            exitBtnAC.show();
                                        }
                                    });
                                }
                            });
                        }
                    });
                }
            });
        }
    }


    private AppearanceControllerBuilder.TypeBuilder<Float> circleGrowing() {
        return new AppearanceControllerBuilder.TypeBuilder<Float>() {
            @Override
            public DefaultAppearanceController.ValueGetter<Float> buildValueGetter() {
                return new DefaultAppearanceController.ValueGetter<Float>() {
                    @Override
                    public Float getShowValue() {
                        return 1f;
                    }

                    @Override
                    public Float getHideValue() {
                        return 0f;
                    }

                    @Override
                    public Float getCurrentValue(View view) {
                        return ((CircleAppearanceRelativeLayout)view).getFraction();
                    }
                };
            }

            @Override
            public AppearanceControllerBuilder.TypedValueSetter<Float> buildValueSetter() {
                return new AppearanceControllerBuilder.TypedValueSetter<Float>(Float.class) {
                    @Override
                    public void setValue(View view, Float value) {
                        ((CircleAppearanceRelativeLayout)view).setFraction(value);
                        view.invalidate();
                    }
                };
            }
            };
    }

    protected abstract int setup_layout();

    @Override
    public void onBackPressed() {
        baseContainerAC.hideAndCustomize(new AppearanceController.AnimatorCustomization() {
            @Override
            public void customize(Animator animator) {
                animator.addListener(new AppearanceControllerOld.AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        SetupGeneralActivity.super.onBackPressed();
                    }
                });
            }
        });
    }
}
