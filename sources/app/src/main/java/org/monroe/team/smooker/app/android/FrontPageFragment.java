package org.monroe.team.smooker.app.android;

import android.animation.Animator;
import android.os.Bundle;
import android.view.View;

import org.monroe.team.android.box.app.FragmentSupport;
import org.monroe.team.android.box.app.ui.AppearanceControllerOld;
import org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceController;
import org.monroe.team.android.box.data.DataProvider;
import org.monroe.team.android.box.event.Event;
import org.monroe.team.corebox.utils.Closure;
import org.monroe.team.smooker.app.R;
import org.monroe.team.smooker.app.uc.PrepareTodaySmokeDetails;

import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.animateAppearance;
import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.duration_constant;
import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.interpreter_decelerate;
import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.interpreter_overshot;
import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.scale;

public abstract class FrontPageFragment extends FragmentSupport<SmookerApplication> {

    private AppearanceController changeCountAC;
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

            changeCountAC = animateAppearance(view(R.id.today_value_text), scale(1f, 1.5f))
                    .showAnimation(duration_constant(200), interpreter_decelerate(0.4f))
                    .hideAnimation(duration_constant(300), interpreter_overshot())
                    .build();
            changeCountAC.showWithoutAnimation();
            onActivityCreatedSafe(savedInstanceState);
            view_button(R.id.add_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    application().addSmoke();
                }
            });
        }
    }

    private void fetchSmokeDetails(boolean requestUpdate, final boolean animate) {
        application().data_smokeDetails().fetch(requestUpdate, new DataProvider.FetchObserver<PrepareTodaySmokeDetails.TodaySmokeDetails>() {
            @Override
            public void onFetch(PrepareTodaySmokeDetails.TodaySmokeDetails smokeStatistic) {
                updateSmokeStatistic(animate, smokeStatistic);
            }

            @Override
            public void onError(DataProvider.FetchError fetchError) {
                activity().forceCloseWithErrorCode(100);
            }
        });
    }

    private void updateSmokeStatistic(boolean animate, PrepareTodaySmokeDetails.TodaySmokeDetails smokeStatistic) {
        final String newValue = Integer.toString(smokeStatistic.specialCount);
        if (!animate) {
            view_text(R.id.today_value_text).setText(newValue);
        } else {
            changeCountAC.hideAndCustomize(new AppearanceController.AnimatorCustomization() {
                @Override
                public void customize(Animator animator) {
                    animator.addListener(new AppearanceControllerOld.AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            view_text(R.id.today_value_text).setText(newValue);
                            changeCountAC.show();
                        }
                    });
                }
            });
        }
        String text = "";

        /*
        <string name="left_for_today">Left for today</string>
    <string name="over_limit">Over today limit</string>
         */

        switch (smokeStatistic.type){
            case NO_LIMIT:
                text = getString(R.string.today_smokes);
                break;
            case NO_LEFT:
            case BEFORE_LIMIT:
                text = getString(R.string.left_for_today);
                break;
            case AFTER_LIMIT:
                text = getString(R.string.over_limit);
                break;
            default:
                throw new UnsupportedOperationException();
        }
        //TODO: add animation too
        view_text(R.id.today_value_description_text).setText(text.toLowerCase());
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
                    onInvalidData(invalidDataClass);
                    return null;
                }
            });
            fetchSmokeDetails(true, false);
            onResumeSafe();
        }
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

}