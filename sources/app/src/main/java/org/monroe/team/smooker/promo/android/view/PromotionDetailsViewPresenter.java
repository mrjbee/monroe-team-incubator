package org.monroe.team.smooker.promo.android.view;

import android.animation.Animator;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import org.monroe.team.android.box.app.ActivitySupport;
import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.*;

import org.monroe.team.android.box.app.ui.animation.AnimatorListenerSupport;
import org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceController;
import org.monroe.team.android.box.utils.DisplayUtils;
import org.monroe.team.corebox.utils.DateUtils;
import org.monroe.team.smooker.promo.R;
import org.monroe.team.smooker.promo.android.SmookerApplication;
import org.monroe.team.smooker.promo.common.constant.Settings;

import java.util.Date;

public class PromotionDetailsViewPresenter {

    private final View mPromoPanel;
    private final ActivitySupport<SmookerApplication> mActivity;
    private final AppearanceController shadowAC;
    private final AppearanceController dialogAC;
    private boolean mPromoEnds = false;
    private boolean mVisible;

    public PromotionDetailsViewPresenter(View mPromoPanel, ActivitySupport<SmookerApplication> mActivity) {
        this.mPromoPanel = mPromoPanel;
        this.mActivity = mActivity;
        shadowAC = animateAppearance(mPromoPanel.findViewById(R.id.shadow),alpha(1f, 0f))
                .showAnimation(duration_constant(300), interpreter_decelerate(0.8f))
                .hideAnimation(duration_constant(200), interpreter_decelerate(0.5f))
                .hideAndGone()
                .build();

        dialogAC = animateAppearance(mPromoPanel.findViewById(R.id.content),ySlide(0f, -DisplayUtils.screenHeight(mActivity.getResources())))
                .showAnimation(duration_constant(400), interpreter_overshot())
                .hideAnimation(duration_constant(300), interpreter_accelerate(0.5f))
                .hideAndGone()
                .build();
        mPromoPanel.findViewById(R.id.shadow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        mPromoPanel.findViewById(R.id.action_continue).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onContinue();
            }
        });
        mPromoPanel.findViewById(R.id.action_paid_version).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onGoToPaid();
            }
        });
    }

    private void onGoToPaid() {
        String url = "https://play.google.com/store/apps/details?id=org.monroe.team.smooker.app";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        mActivity.startActivity(i);
    }

    private void onContinue() {
        mVisible = false;
        if (mPromoEnds){
            dialogAC.hideAndCustomize(new AppearanceController.AnimatorCustomization() {
                @Override
                public void customize(Animator animator) {
                    animator.addListener(new AnimatorListenerSupport(){
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mActivity.finish();
                        }
                    });
                }
            });
            shadowAC.hide();
        }else{
            dialogAC.hide();
            shadowAC.hide();
        }
    }

    public void showPromo() {

        Long lastPromoShownDate = mActivity.application().getSetting(Settings.DATE_LAST_PROMO_SHOWN);
        if (lastPromoShownDate != null && DateUtils.isToday(new Date(lastPromoShownDate))){
            return;
        }

        promoBusiness();


        shadowAC.showAndCustomize(new AppearanceController.AnimatorCustomization() {
            @Override
            public void customize(Animator animator) {
                animator.setStartDelay(1000);
                animator.addListener(new AnimatorListenerSupport(){
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        dialogAC.show();
                    }
                });
            }
        });

    }

    private void promoBusiness() {
        Date today = DateUtils.today();
        mVisible = true;
        Long promoStartDate = mActivity.application().getSetting(Settings.DATE_PROMO_START);
        if (promoStartDate == null){
            promoStartDate = today.getTime();
            mActivity.application().setSetting(Settings.DATE_PROMO_START, promoStartDate);
        }

        long pastDays = DateUtils.asDays(today.getTime() - promoStartDate, true);
        long leftDays = Math.max(7 - pastDays, 0 );

        ((TextView) mPromoPanel.findViewById(R.id.days_left)).setText(leftDays+" " + mActivity.getString(R.string.promo_day_left));

        mPromoEnds = leftDays <= 0;
        if (mPromoEnds){
            ((TextView) mPromoPanel.findViewById(R.id.action_continue)).setText(mActivity.getString(R.string.promo_exit));
        } else {
           mActivity.application().setSetting(Settings.DATE_LAST_PROMO_SHOWN, today.getTime());
        }
    }


    public void saveState(Bundle outState) {
        outState.putBoolean("promo_visible",mVisible);
    }

    public void restoreState(Bundle outState) {

        if (outState != null) {
            mVisible = outState.getBoolean("promo_visible", false);
        }else {
            mVisible = false;
        }


        if (mVisible){
            promoBusiness();
            shadowAC.showWithoutAnimation();
            dialogAC.showWithoutAnimation();
        }else {
            shadowAC.hideWithoutAnimation();
            dialogAC.hideWithoutAnimation();
        }
    }
}
