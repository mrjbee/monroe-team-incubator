package org.monroe.team.smooker.app;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

import org.monroe.team.android.box.app.FragmentSupport;
import org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceController;
import org.monroe.team.android.box.utils.DisplayUtils;
import org.monroe.team.smooker.app.android.SmookerApplication;

import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.animateAppearance;
import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.duration_constant;
import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.interpreter_accelerate;
import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.interpreter_decelerate;
import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.interpreter_overshot;
import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.scale;

public class TrackerFragment extends FragmentSupport<SmookerApplication>{

    private AppearanceController addSmokeBtnAC;

    @Override
    protected int getLayoutId() {
        return R.layout.fragement_tracker;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        addSmokeBtnAC =  animateAppearance(view(R.id.tracker_add_btn), scale(1f,0f))
                .showAnimation(duration_constant(300), interpreter_overshot())
                .hideAnimation(duration_constant(200), interpreter_decelerate(0.4f))
                .hideAndInvisible()
                .build();

    }

    public void onScreenSizeCalculated(int width, int height) {
        final float show_edge = width/2;
        final float hide_edge = width - DisplayUtils.dpToPx(50+70+40, getResources());
        view(R.id.tracker_day_chart_view).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP){
                    addSmokeBtnAC.show();
                } else {
                    if (event.getX() > hide_edge){
                        addSmokeBtnAC.hide();
                    } else if (event.getX() < show_edge){
                        addSmokeBtnAC.show();
                    }
                }
                return false;
            }
        });
    }
}
