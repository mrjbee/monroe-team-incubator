package org.monroe.team.smooker.app.android;

import android.animation.Animator;
import android.graphics.PointF;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import org.monroe.team.android.box.app.ActivitySupport;
import org.monroe.team.android.box.app.ui.AppearanceControllerOld;
import org.monroe.team.android.box.app.ui.animation.AnimatorListenerSupport;
import org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceController;
import org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder;
import org.monroe.team.android.box.app.ui.animation.apperrance.DefaultAppearanceController;
import org.monroe.team.android.box.utils.DisplayUtils;
import org.monroe.team.corebox.utils.DateUtils;
import org.monroe.team.smooker.app.R;
import org.monroe.team.smooker.app.android.view.CircleAppearanceRelativeLayout;
import org.monroe.team.smooker.app.uc.PrepareSmokeQuitDateDetails;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.animateAppearance;
import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.combine;
import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.duration_constant;
import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.interpreter_accelerate;
import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.interpreter_decelerate;
import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.interpreter_overshot;
import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.rotate;
import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.scale;
import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.ySlide;

public class DateDetailsActivity extends ActivitySupport<SmookerApplication> {

    private AppearanceController baseContainerAC;
    private AppearanceController contentContainerAC;
    private AppearanceController exitBtnAC;
    private Date date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_date_details);
        PointF position = getFromIntent("position" , null);
        Theme theme = getFromIntent("theme",Theme.RED);
        applyTheme(theme);

        date = getFromIntent("date",null);
        applyCaption(date);

        CircleAppearanceRelativeLayout baseContainer= view(R.id.date_root, CircleAppearanceRelativeLayout.class);
        baseContainer.setCenter(position);
        baseContainerAC = animateAppearance(baseContainer, circleGrowing())
                .showAnimation(duration_constant(500), interpreter_accelerate(0.8f))
                .hideAnimation(duration_constant(300), interpreter_decelerate(0.8f))
                .build();

        contentContainerAC = animateAppearance(view(R.id.date_content),
                ySlide(0, DisplayUtils.screenHeight(getResources())))
                .showAnimation(duration_constant(200), interpreter_accelerate(null))
                .hideAnimation(duration_constant(200), interpreter_decelerate(0.8f))
                .hideAndGone()
                .build();

        exitBtnAC = combine(
                animateAppearance(view(R.id.date_quit_btn), scale(1f, 0f))
                        .showAnimation(duration_constant(400), interpreter_overshot())
                        .hideAnimation(duration_constant(400), interpreter_accelerate(0.4f))
                        .hideAndInvisible(),
                animateAppearance(view(R.id.date_quit_btn), rotate(360, 0))
                        .showAnimation(duration_constant(300), interpreter_overshot())
                        .hideAnimation(duration_constant(300), interpreter_accelerate(0.4f))
        );

        view(R.id.date_quit_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exitBtnAC.hideAndCustomize(new AppearanceController.AnimatorCustomization() {
                    @Override
                    public void customize(Animator animator) {
                        animator.addListener(new AnimatorListenerSupport(){
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                onBackPressed();
                            }
                        });
                    }
                });
            }
        });

        if (isFirstRun()){
            baseContainerAC.hideWithoutAnimation();
            contentContainerAC.hideWithoutAnimation();
            exitBtnAC.hideWithoutAnimation();
        }else {
            baseContainerAC.showWithoutAnimation();
            contentContainerAC.showWithoutAnimation();
            exitBtnAC.showWithoutAnimation();
        }
    }

    private final DateFormat dateFormatFull = DateFormat.getDateInstance();
    private final DateFormat dayOnlyFormat = new SimpleDateFormat("EEEE");

    private void applyCaption(Date date) {
        String dateFull = dateFormatFull.format(date);
        String dayOnly = getString(R.string.today);
        if (!DateUtils.isToday(date)){
          dayOnly = dayOnlyFormat.format(date);
        }
        view_text(R.id.date_caption_text).setText(dateFull);
        view_text(R.id.date_description_text).setText(dayOnly);
    }

    private void applyTheme(Theme theme) {

        int fontColorId = R.color.font_white;
        int fontValueColorId = R.color.font_white;
        int fontCaptionColorId = R.color.font_white;
        int closeImageId = R.drawable.cancel_logo;

        int headerColorId = R.color.background_dark;
        int contentColorId = R.color.white;
        switch (theme){
            case RED:
                headerColorId = R.color.background_main;
                break;
            case BLUE:
                headerColorId = R.color.selection_main;
                break;
            case WHITE:
                closeImageId = R.drawable.cancel_logo_dark;
                fontColorId = R.color.font_dark_light;
                fontCaptionColorId = R.color.font_dark;
                fontValueColorId = R.color.font_dark;
                break;
        }


        view(R.id.date_header).setBackgroundResource(headerColorId);
        view(R.id.date_body).setBackgroundResource(headerColorId);
        view(R.id.date_content).setBackgroundResource(contentColorId);

        /*
        view(R.id.date_quit_btn, ImageView.class).setImageResource(closeImageId);
        int textValueColor = getResources().getColor(fontValueColorId);
        int textColor = getResources().getColor(fontColorId);
        int textCaptionColor = getResources().getColor(fontCaptionColorId);

        view_text(R.id.date_caption_text).setTextColor(textColor);
        view_text(R.id.date_description_text).setTextColor(textColor);

        view_text(R.id.date_smoke_count_value_text).setTextColor(textValueColor);
        view_text(R.id.date_limit_value_text).setTextColor(textValueColor);
        view_text(R.id.date_status_value_text).setTextColor(textValueColor);

        view_text(R.id.date_smoke_count_text).setTextColor(textCaptionColor);
        view_text(R.id.date_limit_text).setTextColor(textCaptionColor);
        */
    }

    @Override
    protected void onResume() {
        super.onResume();
        startAnimation();
    }

    private void startAnimation() {
        if (isFirstRun()) {
            baseContainerAC.showAndCustomize(new AppearanceController.AnimatorCustomization() {
                @Override
                public void customize(Animator animator) {
                    animator.addListener(new AnimatorListenerSupport(){
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            exitBtnAC.show();
                            application().getSmokeQuitDetailsForDate(date, new SmookerApplication.OnDateDetailsObserver() {
                                @Override
                                public void onResult(PrepareSmokeQuitDateDetails.DateDetails details) {
                                    //set result
                                    view(R.id.date_smoke_count_text).setVisibility(details.isFuture() ? View.GONE : View.VISIBLE);
                                    view(R.id.date_smoke_count_value_text).setVisibility(details.isFuture() ? View.GONE : View.VISIBLE);

                                    view(R.id.date_status_value_text).setVisibility(details.isFuture()|| details.isPassed() ? View.GONE : View.VISIBLE);

                                    if (details.isLimitChanged()){
                                        view_text(R.id.date_limit_text).setText(R.string.new_day_limit);
                                    } else {
                                        view_text(R.id.date_limit_text).setText(R.string.smoke_limit);
                                    }

                                    if (!details.isPassed()){
                                        view_text(R.id.date_status_value_text).setText(R.string.limit_is_exceeded);
                                    }

                                    view_text(R.id.date_limit_value_text).setText("" + details.getLimit() + " " + getString(R.string.times));
                                    view_text(R.id.date_smoke_count_value_text).setText("" + details.getSmokeCounts() + " " + getString(R.string.times));
                                    contentContainerAC.show();
                                }

                                @Override
                                public void onFail() {
                                    forceCloseWithErrorCode(404);
                                }
                            });
                        }
                    });
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        contentContainerAC.hideAndCustomize(new AppearanceController.AnimatorCustomization() {
            @Override
            public void customize(Animator a) {
                a.addListener(new AnimatorListenerSupport(){
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        baseContainerAC.hideAndCustomize(new AppearanceController.AnimatorCustomization() {
                            @Override
                            public void customize(Animator animator) {
                                animator.addListener(new AppearanceControllerOld.AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        DateDetailsActivity.super.onBackPressed();
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });
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

    public static enum Theme {
        BLUE, RED, WHITE
    }
}
