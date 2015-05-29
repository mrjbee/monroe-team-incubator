package org.monroe.team.smooker.promo.android.view;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.os.Build;
import android.util.Property;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;

@Deprecated
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class TimerViewAnimation14APISupport extends TimerView.AnimationSupport {

    private Animator timeAnimator;
    private Animator timeOutAnimator;

    protected TimerViewAnimation14APISupport(TimerView owner) {
        super(owner);
    }

    @Override
    public void animateTimeProgress(float fromProgress, float toProgress) {
        if (fromProgress == toProgress) return;
        if (timeAnimator != null){
            timeAnimator.cancel();
        }
        if (fromProgress < 0 && toProgress >= 0){
            fromProgress = 0;
        }

        timeAnimator = ObjectAnimator.ofFloat(owner, new Property<TimerView, Float>(Float.class,"TimeProgress") {
            @Override
            public Float get(TimerView object) {
                return object.getTimeProgress();
            }

            @Override
            public void set(TimerView object, Float value) {
                if (value == null) throw new IllegalStateException("No no it`s couldn`t be null as time progress");
                object.setTimeProgress(value);
            }
        },fromProgress,toProgress);

        timeAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        timeAnimator.setDuration(800);
        timeAnimator.start();
    }

    @Override
    public void animateTimeOutProgress(float fromProgress, float toProgress) {
        if (fromProgress == toProgress) return;
        if (timeOutAnimator != null){
            timeOutAnimator.cancel();
        }
        timeOutAnimator = ObjectAnimator.ofFloat(owner, new Property<TimerView, Float>(Float.class,"TimeProgress") {
            @Override
            public Float get(TimerView object) {
                return object.getTimeOutProgress();
            }

            @Override
            public void set(TimerView object, Float value) {
                if (value == null) throw new IllegalStateException("No no it`s couldn`t be null as time progress");
                object.setTimeOutProgress(value);
            }
        },fromProgress,toProgress);

        long duration = (long) (Math.abs(fromProgress - toProgress) * 2000);
        if (duration < 1000){
            duration = 1000;
            timeOutAnimator.setInterpolator(new LinearInterpolator());
        } else {
            timeOutAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            timeOutAnimator.setDuration(duration);
        }
        timeOutAnimator.setDuration(duration);
        timeOutAnimator.start();
    }


}
