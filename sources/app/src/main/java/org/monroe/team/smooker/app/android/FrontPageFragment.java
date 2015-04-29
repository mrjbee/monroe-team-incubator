package org.monroe.team.smooker.app.android;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;

import org.monroe.team.android.box.app.FragmentSupport;
import org.monroe.team.android.box.app.ui.AppearanceControllerOld;
import org.monroe.team.android.box.app.ui.animation.AnimatorListenerSupport;
import org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceController;
import org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder;
import org.monroe.team.android.box.app.ui.animation.apperrance.DefaultAppearanceController;
import org.monroe.team.android.box.data.Data;
import org.monroe.team.android.box.data.DataProvider;
import org.monroe.team.android.box.event.Event;
import org.monroe.team.corebox.utils.Closure;
import org.monroe.team.smooker.app.R;
import org.monroe.team.smooker.app.android.view.LeftToRightTextView;
import org.monroe.team.smooker.app.uc.PrepareSmokeQuitDetails;
import org.monroe.team.smooker.app.uc.PrepareTodaySmokeDetails;

import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.alpha;
import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.animateAppearance;
import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.duration_constant;
import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.interpreter_accelerate;
import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.interpreter_decelerate;
import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.interpreter_overshot;
import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.scale;

public abstract class FrontPageFragment extends FragmentSupport<SmookerApplication> {

    private AppearanceController changeCountAC;
    private AppearanceController changeCountDescriptionAC;
    private Boolean isActiveCreation = null;

    final public boolean isActive() {
        if (isActiveCreation == null){
           isActiveCreation = ((FrontPageActivity)activity()).isFragmentActive(this.getClass());
        }
        return isActiveCreation;
    }

    @Override
    final public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (isActive()) {

            require_view(R.id.today_value_text);
            require_view(R.id.add_btn);
            require_view(R.id.today_value_description_text);


            changeCountAC = animateAppearance(view(R.id.today_value_text), alpha(1f, 0f))
                    .showAnimation(duration_constant(100), interpreter_decelerate(0.4f))
                    .hideAnimation(duration_constant(100), interpreter_overshot())
                    .build();

            changeCountDescriptionAC = animateAppearance(view(R.id.today_value_description_text), leftToRight())
                    .showAnimation(duration_constant(200), interpreter_accelerate(0.6f))
                    .hideAnimation(duration_constant(100), interpreter_decelerate(0.8f))
                    .build();

            changeCountAC.showWithoutAnimation();
            changeCountDescriptionAC.showWithoutAnimation();
            fetchSmokeDetails(true, false);

            onActivityCreatedSafe(savedInstanceState);
            fetchQuitSmokeDetails(false);
            view_button(R.id.add_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    application().addSmoke();
                }
            });

            view(R.id.start_setting_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openSettings();
                }
            });
        }
    }

    private void openSettings() {
        startActivity(new Intent(application(), PreferencesActivity.class));
    }

    private AppearanceControllerBuilder.TypeBuilder<Float> leftToRight() {
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
                        return ((LeftToRightTextView)view).getFraction();
                    }
                };
            }

            @Override
            public AppearanceControllerBuilder.TypedValueSetter<Float> buildValueSetter() {
                return new AppearanceControllerBuilder.TypedValueSetter<Float>(Float.class) {
                    @Override
                    public void setValue(View view, Float value) {
                        ((LeftToRightTextView)view).setFraction(value);
                        view.invalidate();
                    }
                };
            }
        };
    }

    private void fetchSmokeDetails(boolean requestUpdate, final boolean animate) {
        application().data_smokeDetails().fetch(requestUpdate, new DataProvider.FetchObserver<PrepareTodaySmokeDetails.TodaySmokeDetails>() {
            @Override
            public void onFetch(PrepareTodaySmokeDetails.TodaySmokeDetails smokeStatistic) {
                updateSmokeStatistic(animate, smokeStatistic);
            }

            @Override
            public void onError(DataProvider.FetchError fetchError) {
                activity().forceCloseWithErrorCode(new Data.FetchException(fetchError));
            }
        });
    }

    private void updateSmokeStatistic(boolean animate, PrepareTodaySmokeDetails.TodaySmokeDetails smokeStatistic) {
        Pair<String,String> statisticStrings = toSimpleString(getActivity(), smokeStatistic);

        String newDescriptionText = statisticStrings.second;
        final String newCountText = statisticStrings.first;
        if (!animate) {
            view_text(R.id.today_value_text).setText(newCountText);
            view_text(R.id.today_value_description_text).setText(newDescriptionText);
        } else {
            String oldDescriptionText = view_text(R.id.today_value_description_text).getText().toString();
            if (oldDescriptionText.equals(newDescriptionText)){
                String oldCountText = view_text(R.id.today_value_text).getText().toString();
                if (!oldCountText.equals(newCountText)){
                    changeCountAC.hideAndCustomize(new AppearanceController.AnimatorCustomization() {
                        @Override
                        public void customize(Animator animator) {
                            animator.addListener(new AnimatorListenerSupport(){
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    view_text(R.id.today_value_text).setText(newCountText);
                                    changeCountAC.show();
                                }
                            });
                        }
                    });
                }
                return;
            }

            final String finalText = newDescriptionText;
            changeCountAC.hideAndCustomize(new AppearanceController.AnimatorCustomization() {
                @Override
                public void customize(Animator animator) {
                    animator.addListener(new AppearanceControllerOld.AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            changeCountDescriptionAC.hideAndCustomize(new AppearanceController.AnimatorCustomization() {
                                @Override
                                public void customize(Animator descriptionAnimator) {
                                    descriptionAnimator.addListener(new AnimatorListenerSupport(){
                                        @Override
                                        public void onAnimationEnd(Animator animation) {
                                            view_text(R.id.today_value_description_text).setText(finalText);
                                            changeCountDescriptionAC.showAndCustomize(new AppearanceController.AnimatorCustomization() {
                                                @Override
                                                public void customize(Animator a) {
                                                    a.addListener(new AnimatorListenerSupport(){
                                                        @Override
                                                        public void onAnimationEnd(Animator animation) {
                                                            view_text(R.id.today_value_text).setText(newCountText);
                                                            changeCountAC.show();
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
            });
        }
     }

    public static Pair<String,String> toSimpleString(Context context, PrepareTodaySmokeDetails.TodaySmokeDetails smokeStatistic) {
       final String newCountText = Integer.toString(smokeStatistic.specialCount);
       String newDescriptionText = "";

        switch (smokeStatistic.type){
            case NO_LIMIT:
                newDescriptionText = context.getString(R.string.today_smokes);
                break;
            case NO_LEFT:
            case BEFORE_LIMIT:
                newDescriptionText = context.getString(R.string.left_for_today);
                break;
            case AFTER_LIMIT:
                newDescriptionText = context.getString(R.string.over_limit);
                break;
            default:
                throw new UnsupportedOperationException();
        }
        return new Pair<>(newCountText, newDescriptionText);
    }

    @Override
    final public void onStart() {
        super.onStart();
        if (isActive()){
            onStartSafe();
        }
    }
    @Override
    final public void onResume() {
        super.onResume();
        if (isActive()){
            Event.subscribeOnEvent(activity(), this, DataProvider.INVALID_DATA, new Closure<Class, Void>() {
                @Override
                public Void execute(Class invalidDataClass) {
                    if (PrepareTodaySmokeDetails.TodaySmokeDetails.class == invalidDataClass) {
                        fetchSmokeDetails(true, true);
                    }
                    if (PrepareSmokeQuitDetails.Details.class == invalidDataClass) {
                        fetchQuitSmokeDetails(true);
                    }
                    onInvalidData(invalidDataClass);
                    return null;
                }
            });
            fetchQuitSmokeDetails(true);
            fetchSmokeDetails(true, true);
            onResumeSafe();
        }
    }

    private void fetchQuitSmokeDetails(final boolean animation) {
        application().data_smokeQuit().fetch(true, new DataProvider.FetchObserver<PrepareSmokeQuitDetails.Details>() {
            @Override
            public void onFetch(PrepareSmokeQuitDetails.Details details) {

            }

            @Override
            public void onError(DataProvider.FetchError fetchError) {
                activity().forceCloseWithErrorCode(new Data.FetchException(fetchError));
            }
        });
    }

    protected void onInvalidData(Class invalidDataClass) {}


    @Override
    final public void onPause() {
        super.onPause();
        if (isActive()){
            Event.unSubscribeFromEvents(activity(), this);
            onPauseSafe();
        }
    }

    protected void onActivityCreatedSafe(Bundle savedInstanceState){}
    protected void onStartSafe() {}
    protected void onResumeSafe(){}
    protected void onPauseSafe(){}
    protected void onScreenSizeCalculatedSafe(int activityWidth, int activityHeight) {}
    protected boolean onBackPressedSafe(){return false;}



    final private void require_view(int view_id){
        if (null == view(view_id)){
            throw new IllegalStateException("View not found");
        }
    }

    final public void onScreenSizeCalculated(int activityWidth, int activityHeight){
      if (isActive()) {
          onScreenSizeCalculatedSafe(activityWidth, activityHeight);
      }
    }


    final public boolean onBackPressed(){
        if (isActive()) {
            return onBackPressedSafe();
        }
        return false;
    }

    public void onMenuPressed() {
        openSettings();
    }
}