package org.monroe.team.smooker.app.android;

import android.animation.Animator;
import android.os.Bundle;

import org.monroe.team.android.box.app.FragmentSupport;
import org.monroe.team.android.box.app.ui.AppearanceControllerOld;
import org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceController;
import org.monroe.team.smooker.app.R;

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
            check_view(R.id.today_value_text);
            changeCountAC = animateAppearance(view(R.id.today_value_text), scale(1f, 2f))
                    .showAnimation(duration_constant(200), interpreter_decelerate(0.4f))
                    .hideAnimation(duration_constant(300), interpreter_overshot())
                    .build();
            changeCountAC.showWithoutAnimation();
            onActivityCreatedSafe(savedInstanceState);
        }
    }

    @Override
    final public void onResume() {
        super.onResume();
        if (isActive()){
            onResumeSafe();
        }
    }


    @Override
    final public void onPause() {
        super.onPause();
        if (isActive()){
            onPauseSafe();
        }
    }

    protected void onActivityCreatedSafe(Bundle savedInstanceState){}
    protected void onResumeSafe(){}
    protected void onPauseSafe(){}
    protected void onScreenSizeCalculatedSafe(int activityWidth, int activityHeight) {}
    protected boolean onBackPressedSafe(){return false;}

    final protected void updateSmokeCount(int count, boolean animate) {
        final String newValue = Integer.toString(count);
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
    }

    final private void check_view(int view_id){
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