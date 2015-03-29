package org.monroe.team.smooker.app.android;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.monroe.team.android.box.app.ui.AppearanceControllerOld;
import org.monroe.team.android.box.app.ui.GetViewImplementation;
import org.monroe.team.android.box.app.ui.SlideTouchGesture;
import org.monroe.team.android.box.app.ui.animation.AnimatorListenerSupport;
import org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceController;
import org.monroe.team.android.box.data.DataProvider;
import org.monroe.team.android.box.utils.DisplayUtils;
import org.monroe.team.corebox.utils.Closure;
import org.monroe.team.corebox.utils.DateUtils;
import org.monroe.team.corebox.utils.Lists;
import org.monroe.team.smooker.app.R;
import org.monroe.team.smooker.app.android.controller.SmokeQuitCalendarDisplayManager;
import org.monroe.team.smooker.app.android.view.CellBackgroundView;
import org.monroe.team.smooker.app.android.view.DateListAdapter;
import org.monroe.team.smooker.app.android.view.RelativeLayoutExt;
import org.monroe.team.smooker.app.android.view.RoundSegmentImageView;
import org.monroe.team.smooker.app.android.view.SmokePeriodHistogramView;
import org.monroe.team.smooker.app.android.view.TextViewExt;
import org.monroe.team.smooker.app.common.SmookerModel;
import org.monroe.team.smooker.app.common.constant.Settings;
import org.monroe.team.smooker.app.uc.GetSmokeQuitSchedule;
import org.monroe.team.smooker.app.uc.PrepareMoneyBoxProgress;
import org.monroe.team.smooker.app.uc.PreparePeriodStatistic;
import org.monroe.team.smooker.app.uc.PrepareSmokeClockDetails;
import org.monroe.team.smooker.app.uc.PrepareSmokeQuitDetails;
import org.monroe.team.smooker.app.uc.PrepareTodaySmokeDetails;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.alpha;
import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.animateAppearance;
import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.combine;
import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.duration_auto_fint;
import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.duration_auto_int;
import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.duration_constant;
import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.heightSlide;
import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.interpreter_accelerate;
import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.interpreter_decelerate;
import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.interpreter_overshot;
import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.rotate;
import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.scale;
import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.xSlide;
import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.ySlide;

public class TilesFragment extends FrontPageFragment {

    AppearanceController bottomLayerAC;
    AppearanceController tileCaptionTextAC;
    AppearanceController pickerRotationAC;
    AppearanceController tileSpaceWraperAC;
    AppearanceController tileBigContentAC;
    AppearanceController tileShowFromLeftAC;
    AppearanceController tileShowFromRightAC;
    AppearanceController tileShowAC;
    AppearanceController tileCaptionTextChangeAC;
    AppearanceController settingAlternativeBtnAC;
    AppearanceController addSmokeBtnAC;
    AppearanceController timePanelAC;

    float height_px_close_dash;
    float height_px_tile;

    List<TileController> tileControllerList = new ArrayList<>(3);
    List<HoleController> holeControllerList;
    private int currentTileIndex;

    private Timer clockTimer;
    private long msSinceLastSmoke = -1;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_tiles;
    }


    @Override
    public void onActivityCreatedSafe(Bundle savedInstanceState) {
        setupTilesControllers();
    }


    @Override
    protected void onInvalidData(Class invalidDataClass) {
        if (PrepareSmokeClockDetails.SmokeClockDetails.class == invalidDataClass) {
            fetchClockData(true);
        }
        getTileController(currentTileIndex).onInvalidData(invalidDataClass);
    }

    @Override
    public void onResumeSafe() {
        clockTimer = new Timer();
        clockTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (msSinceLastSmoke == -1) return;
                updateClock(true);
            }
        }, 0, 300);

        fetchClockData(false);
        getTileController(currentTileIndex).onResume();
    }

    private void fetchClockData(final boolean animation) {
        application().data_smokeClock().fetch(true, new DataProvider.FetchObserver<PrepareSmokeClockDetails.SmokeClockDetails>() {
            @Override
            public void onFetch(PrepareSmokeClockDetails.SmokeClockDetails smokeClockDetails) {
                setupClock(smokeClockDetails);
                updateClock(animation);
            }

            @Override
            public void onError(DataProvider.FetchError fetchError) {
                activity().forceCloseWithErrorCode(3);
            }
        });
    }

    private synchronized void setupClock(PrepareSmokeClockDetails.SmokeClockDetails smokeClockDetails) {
        msSinceLastSmoke = smokeClockDetails.msSinceLastSmoke;
    }

    private synchronized void updateClock(final boolean animation) {
        long[] ls = DateUtils.splitPeriod(DateUtils.now(), new Date(msSinceLastSmoke));

        final String timeString = twoDigitString(ls[1]) + ":" + twoDigitString(ls[2]);
        final String daysString = ls[0] + " " + getString(R.string.days);

        long ms = System.currentTimeMillis() - msSinceLastSmoke;
        long delta = ms % (60 * 1000);
        final float angle = 360 * delta / (60 * 1000);
        runLastOnUiThread(new Runnable() {
            @Override
            public void run() {
                view(R.id.start_clock_value_panel, RoundSegmentImageView.class).setAngle(angle);
                view(R.id.start_clock_value_panel, RoundSegmentImageView.class).invalidate();
                view_text(R.id.clock_day_value).setText(daysString);
                view(R.id.clock_time_value, TextViewExt.class).setText(timeString, animation);
            }
        });
    }

    private String twoDigitString(long val) {
        String textValue = Long.toString(val);
        if (textValue.length() < 2) {
            textValue = "0" + textValue;
        }
        return textValue;
    }

    @Override
    public void onPauseSafe() {
        clockTimer.cancel();
        clockTimer.purge();
        clockTimer = null;

    }


    @Override
    public void onScreenSizeCalculatedSafe(int activityWidth, int activityHeight) {

        height_px_close_dash = activityHeight - dpToPx(height_dp_background_bottom(), height_dp_open_picker(), height_dp_action_bar());

        bottomLayerAC = animateAppearance(view(R.id.start_bottom_layer), ySlide(-height_px_close_dash, 0))
                .showAnimation(duration_constant(400), interpreter_overshot())
                .hideAnimation(duration_auto_fint(0.5f), interpreter_decelerate(0.3f))
                .build();

        tileCaptionTextAC = animateAppearance(view(R.id.start_tile_caption_text),
                scale(1.2f, 1f))
                .showAnimation(duration_constant(300), interpreter_overshot())
                .hideAnimation(duration_auto_fint(), interpreter_decelerate(0.3f))
                .build();

        tileCaptionTextChangeAC = animateAppearance(view(R.id.start_tile_caption_text),
                alpha(1f, 0.2f))
                .showAnimation(duration_constant(400), interpreter_decelerate(null))
                .hideAnimation(duration_constant(200), interpreter_accelerate(0.3f))
                .build();

        pickerRotationAC = animateAppearance(view(R.id.start_open_picker_arrow_image), rotate(0f, 180f))
                .showAnimation(duration_constant(300), interpreter_accelerate(0.3f))
                .hideAnimation(duration_constant(300), interpreter_decelerate(0.3f))
                .build();

        height_px_tile = activityHeight - dpToPx(height_dp_background_bottom() - height_dp_tile_title());
        tileSpaceWraperAC = animateAppearance(view(R.id.start_tile_space_wrap_panel),
                heightSlide(
                        (int) dpToPx(height_dp_open_picker(), height_dp_tile_title(), height_dp_action_bar()),
                        (int) height_px_tile
                ))
                .showAnimation(duration_constant(200), interpreter_accelerate(0.3f))
                .hideAnimation(duration_auto_int(0.5f), interpreter_decelerate(0.3f)).build();

        float tileBigDataHeight =
                activityHeight - dpToPx(height_dp_open_picker() + height_dp_tile_title() * 1.2f + height_dp_action_bar() + height_dp_tile_small_content() + height_dp_holes());

        tileBigContentAC = animateAppearance(view(R.id.start_tile_big_content_wrapper), heightSlide((int) tileBigDataHeight, 0))
                .showAnimation(duration_constant(200), interpreter_overshot())
                .hideAnimation(duration_constant(150), interpreter_decelerate(0.5f))
                .hideAndGone().build();


        tileShowFromLeftAC = animateAppearance(view(R.id.start_tile_content),
                xSlide(0, -DisplayUtils.screenWidth(getResources()))
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
                xSlide(0, -DisplayUtils.screenWidth(getResources()))
        )
                .showAnimation(duration_constant(200), interpreter_overshot())
                .hideAnimation(duration_constant(200), interpreter_decelerate(0.5f))
                .hideAndGone().build();

        settingAlternativeBtnAC = combine(
                animateAppearance(view(R.id.start_setting_dublicate_btn), rotate(180 + 90, 0))
                        .showAnimation(duration_constant(300), interpreter_overshot())
                        .hideAnimation(duration_constant(300), interpreter_accelerate(0.4f)),
                animateAppearance(view(R.id.start_setting_dublicate_btn), scale(1f, 0f))
                        .showAnimation(duration_constant(400), interpreter_overshot())
                        .hideAnimation(duration_constant(400), interpreter_accelerate(0.4f))
                        .hideAndInvisible()
                );

        addSmokeBtnAC = animateAppearance(view(R.id.add_btn), scale(1f, 0f))
                .showAnimation(duration_constant(300), interpreter_overshot())
                .hideAnimation(duration_constant(300), interpreter_accelerate(0.4f))
                .hideAndInvisible().build();


        timePanelAC = combine(

                animateAppearance(view(R.id.start_time_panel), alpha(1f, 0f))
                        .showAnimation(duration_constant(200), interpreter_accelerate(null))
                        .hideAnimation(duration_constant(200), interpreter_accelerate(0.4f)),

                animateAppearance(view(R.id.start_ornament_panel), alpha(1f, 0f))
                        .showAnimation(duration_constant(200), interpreter_accelerate(null))
                        .hideAnimation(duration_constant(200), interpreter_accelerate(0.4f)));

        view(R.id.start_bottom_layer, RelativeLayoutExt.class).setTranslationListener(new RelativeLayoutExt.TranslationListener() {
            @Override
            public void onX(View source, float translationX) {
            }

            @Override
            public void onY(View view, float translationY) {
                view(R.id.start_ornament_panel).setTranslationY(-translationY + translationY * 0.4f);
            }
        });

        tileShowFromLeftAC.showWithoutAnimation();
        tileShowFromRightAC.showWithoutAnimation();
        tileBigContentAC.hideWithoutAnimation();
        tileSpaceWraperAC.hideWithoutAnimation();
        pickerRotationAC.showWithoutAnimation();
        bottomLayerAC.hideWithoutAnimation();
        tileCaptionTextAC.hideWithoutAnimation();
        tileCaptionTextChangeAC.showWithoutAnimation();
        settingAlternativeBtnAC.hideWithoutAnimation();
        timePanelAC.showWithoutAnimation();
        setupDashCloseState();

        setupTileBoard();
        applyTileContentUsing(0);
        setupTileCaption();
        applyTileContentUsing(currentTileIndex);
        view(R.id.start_setting_dublicate_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performTileSetup();
            }
        });
    }

    private void performTileSetup() {
        Class<? extends Activity> activityClass = getTileController(currentTileIndex).getSetupActivityClass();
        if (activityClass == null){
            Toast.makeText(getActivity(), "No setup yet.", Toast.LENGTH_SHORT).show();
        }else{
            final Intent intent = new Intent(getActivity(), activityClass);
            View btn = view(R.id.start_setting_dublicate_btn);
            int[] root_location = new int[2];
            int[] location = new int[2];
            view(R.id.star_root).getLocationOnScreen(root_location);
            btn.getLocationOnScreen(location);
            intent.putExtra("position", new PointF(
                    location[0] - root_location[0]
                            + btn.getWidth() / 2,
                    location[1] - root_location[1]
                            - btn.getHeight() / 2));
            settingAlternativeBtnAC.hideAndCustomize(new AppearanceController.AnimatorCustomization() {
                @Override
                public void customize(Animator animator) {
                    animator.addListener(new AnimatorListenerSupport() {

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            startActivityForResult(intent, 220);
                        }
                    });
                }
            });
        }
    }

    private void showDetailsForDate(View ownedView, Date date, DateDetailsActivity.Theme theme, PointF pointF) {
        final Intent intent = new Intent(getActivity(), DateDetailsActivity.class);
        int[] root_location = new int[2];
        view(R.id.star_root).getLocationOnScreen(root_location);
        pointF.offset(-root_location[0], -root_location[1]);
        intent.putExtra("position", pointF);
        intent.putExtra("theme", theme);
        intent.putExtra("date", date);
        startActivity(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 220){
            settingAlternativeBtnAC.show();
        }
    }

    private float dpToPx(float... values) {
        int value = 0;
        for (float v : values) {
            value += v;
        }
        return DisplayUtils.dpToPx(value, getResources());
    }

    private int height_dp_action_bar() {
        return 50;
    }

    private int height_dp_open_picker() {
        return 33;
    }

    private int height_dp_tile_small_content() {
        return 100;
    }

    private int height_dp_holes() {
        return 20;
    }

    private int height_dp_tile_title() {
        return 60;
    }

    private int height_dp_background_bottom() {
        return 200;
    }

    private void setupTilesControllers() {
        tileControllerList.add(new StatisticTile());
        tileControllerList.add(new QuitSmokeTile());
        tileControllerList.add(new MoneyBoxTile());
        holeControllerList = Lists.collect(tileControllerList, new Closure<TileController, HoleController>() {
            @Override
            public HoleController execute(TileController arg) {
                ViewGroup parent = (ViewGroup) view(R.id.start_tile_hole_place);
                View root_view = getActivity().getLayoutInflater().inflate(R.layout.item_hole,
                        parent, false);
                parent.addView(root_view, parent.getChildCount());
                return new HoleController(root_view);
            }
        });
    }

    private void setupTileBoard() {
        view(R.id.start_tile_content).setOnTouchListener(new SlideTouchGesture(DisplayUtils.dpToPx(400, getResources()), SlideTouchGesture.Axis.X) {
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
                AppearanceController hideAC = (slideValue > 0) ? tileShowFromLeftAC : tileShowFromRightAC;
                final AppearanceController showAC = (slideValue < 0) ? tileShowFromLeftAC : tileShowFromRightAC;
                final int nextTileIndex = calculateTileIndex((slideValue > 0) ? 1 : -1);
                hideAC.hideAndCustomize(new AppearanceController.AnimatorCustomization() {
                    @Override
                    public void customize(Animator animator) {
                        animator.addListener(new AppearanceControllerOld.AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(final Animator animation) {
                                changeTileContentUsing(nextTileIndex);
                                showAC.hideWithoutAnimation();
                                showAC.show();
                                tileCaptionTextChangeAC.hideAndCustomize(new AppearanceController.AnimatorCustomization() {
                                    @Override
                                    public void customize(Animator changeAnimator) {
                                        changeAnimator.addListener(new AppearanceControllerOld.AnimatorListenerAdapter() {
                                            @Override
                                            public void onAnimationEnd(Animator animation) {
                                                setupTileCaption();
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
                view(R.id.start_bottom_layer).setTranslationY((float) (-400 * (fraction)));
                view(R.id.start_tile_space_wrap_panel).getLayoutParams().height = (int) (height_px_tile - 400 * fraction);
                view(R.id.start_tile_space_wrap_panel).requestLayout();
                view(R.id.start_time_panel).setAlpha(1f - 0.6f * fraction);
            }

            @Override
            protected void onApply(float x, float y, float slideValue, float fraction) {
                timePanelAC.hide();
                bottomLayerAC.showAndCustomize(new AppearanceController.AnimatorCustomization() {
                    @Override
                    public void customize(Animator animator) {
                        animator.addListener(new AppearanceControllerOld.AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                setupDashOpenState();
                                tileBigContentAC.showAndCustomize(new AppearanceController.AnimatorCustomization() {
                                    @Override
                                    public void customize(Animator animator) {
                                        animator.addListener(new AppearanceControllerOld.AnimatorListenerAdapter() {
                                            @Override
                                            public void onAnimationEnd(Animator animation) {
                                                settingAlternativeBtnAC.show();
                                                view(R.id.start_tile_big_content).setVisibility(View.VISIBLE);
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }
                });
                tileCaptionTextAC.show();
                pickerRotationAC.hide();
                tileSpaceWraperAC.show();
            }

            @Override
            protected void onCancel(float x, float y, float slideValue, float fraction) {
                timePanelAC.show();
                bottomLayerAC.hide();
                tileSpaceWraperAC.hide();
                addSmokeBtnAC.show();
            }

            @Override
            protected void onStart(float x, float y) {
                addSmokeBtnAC.hide();
            }
        });
    }

    private void setupDashOpenState() {
        final float startTranslation = view(R.id.start_bottom_layer).getTranslationY();
        final int startHeight = view(R.id.start_tile_space_wrap_panel).getLayoutParams().height;
        view(R.id.start_open_picker_panel).setOnTouchListener(new SlideTouchGesture(height_px_close_dash, SlideTouchGesture.Axis.Y_DOWN) {
            @Override
            protected void onProgress(float x, float y, float slideValue, float fraction) {
                view(R.id.start_bottom_layer).setTranslationY((float) (startTranslation + height_px_close_dash * fraction));
                float scaleFactor = 1.2f - 0.5f * fraction;
                view(R.id.start_tile_caption_text, TextView.class).setScaleX(scaleFactor);
                view(R.id.start_tile_caption_text, TextView.class).setScaleY(scaleFactor);
                view(R.id.start_tile_space_wrap_panel).getLayoutParams().height = (int) (startHeight + height_px_close_dash * fraction);
                view(R.id.start_tile_space_wrap_panel).requestLayout();
                view(R.id.start_time_panel).setAlpha(1f * fraction);
                float alpha = fraction * 2;
                if (alpha > 1f) {
                    alpha = 1f;
                }
                view(R.id.start_ornament_panel).setAlpha(alpha);
            }

            @Override
            protected void onApply(float x, float y, float slideValue, float fraction) {
                closeDash();
            }

            @Override
            protected void onCancel(float x, float y, float slideValue, float fraction) {
                timePanelAC.hide();
                bottomLayerAC.show();
                tileCaptionTextAC.show();
                settingAlternativeBtnAC.show();
                tileSpaceWraperAC.showAndCustomize(new AppearanceController.AnimatorCustomization() {
                    @Override
                    public void customize(Animator animator) {
                        animator.addListener(new AppearanceControllerOld.AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                tileBigContentAC.showAndCustomize(new AppearanceController.AnimatorCustomization() {
                                    @Override
                                    public void customize(Animator ani) {
                                        ani.addListener(new AnimatorListenerAdapter() {
                                            @Override
                                            public void onAnimationEnd(Animator animation) {
                                                view(R.id.start_tile_big_content).setVisibility(View.VISIBLE);
                                            }
                                        });
                                    }
                                });
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
        view(R.id.start_tile_big_content).setVisibility(View.GONE);
        tileBigContentAC.hide();
        settingAlternativeBtnAC.hide();
    }

    private void closeDash() {
        timePanelAC.show();
        tileSpaceWraperAC.hide();
        bottomLayerAC.hideAndCustomize(new AppearanceController.AnimatorCustomization() {
            @Override
            public void customize(Animator animator) {
                animator.addListener(new AppearanceControllerOld.AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(final Animator animation) {
                        setupDashCloseState();
                        addSmokeBtnAC.showAndCustomize(new AppearanceController.AnimatorCustomization() {
                            @Override
                            public void customize(Animator addSmokeAnimator) {
                                addSmokeAnimator.setStartDelay(200);
                            }
                        });
                    }
                });
            }
        });
        tileCaptionTextAC.hide();
        pickerRotationAC.show();
    }

    public boolean onBackPressedSafe() {
        if (view(R.id.start_tile_big_content_wrapper).getVisibility() == View.VISIBLE) {
            onStartingCloseDash();
            closeDash();
            return true;
        } else {
            return false;
        }
    }

    private int calculateTileIndex(int step) {
        int answer = currentTileIndex + step;
        if (answer < 0) {
            answer = Lists.getLastIndex(tileControllerList);
        }
        if (answer > Lists.getLastIndex(tileControllerList)) {
            answer = 0;
        }
        return answer;
    }


    private void changeTileContentUsing(int tileControllerIndex) {
        destroyTileContentUsing(currentTileIndex);
        applyTileContentUsing(tileControllerIndex);
    }

    private void destroyTileContentUsing(int tileControllerIndex) {
        holeControllerList.get(tileControllerIndex).unselect();
        getTileController(tileControllerIndex).onPause();
    }

    private void applyTileContentUsing(int tileControllerIndex) {
        currentTileIndex = tileControllerIndex;
        View view = getTileController(currentTileIndex).getSmallContent(activity().getLayoutInflater(), view(R.id.start_tile_small_content, ViewGroup.class));
        setupSmallTileView(view);
        view = getTileController(currentTileIndex).getBigContent(activity().getLayoutInflater(), view(R.id.start_tile_small_content, ViewGroup.class));
        setupBigTileView(view);
        getTileController(tileControllerIndex).onResume();
    }

    private void setupTileCaption() {
        holeControllerList.get(currentTileIndex).select();
        String title =  getTileController(currentTileIndex).caption();
        view_text(R.id.start_tile_caption_text).setText(title);
    }

    private TileController getTileController(int index) {
        return tileControllerList.get(index);
    }


    class HoleController{

        private final View panel;
        private final View upLayer;
        private final View downLayer;
        final AppearanceController selectionAC;

        HoleController(View root) {
            this.panel = root;
            this.upLayer = root.findViewById(R.id.hole_up);
            this.downLayer = root.findViewById(R.id.hole_bottom);
            selectionAC = animateAppearance(upLayer, scale(1f,0.2f))
                    .showAnimation(duration_constant(400),interpreter_overshot())
                    .hideAnimation(duration_constant(200))
                    .hideAndInvisible()
                    .build();
            selectionAC.hideWithoutAnimation();
        }


        public void select() {
            selectionAC.show();
        }

        public void unselect() {
            selectionAC.hide();
        }
    }


    private static interface TileController{
        String caption();
        View getSmallContent(LayoutInflater layoutInflater, ViewGroup parentView);
        void onPause();
        void onResume();
        View getBigContent(LayoutInflater layoutInflater, ViewGroup view);
        void onInvalidData(Class dataClass);
        Class<? extends Activity> getSetupActivityClass();
    }

    private abstract class AbstractTileController implements TileController{

        protected View smallContentView;
        protected View bigContentView;


        @Override
        public Class<? extends Activity> getSetupActivityClass() {
            return null;
        }

        protected int smallTileId() {
            return R.layout.tile_small_stub;
        }

        protected int bigTileId() {
            return R.layout.tile_small_stub;
        }

        @Override
        final public View getSmallContent(LayoutInflater layoutInflater, ViewGroup parentView) {
            if (smallContentView == null) {
                smallContentView = layoutInflater.inflate(smallTileId(), parentView, false);
                init_smallContent(smallContentView, layoutInflater);
            }
            return smallContentView;
        }

        protected void init_smallContent(View smallContentView, LayoutInflater layoutInflater) {}
        protected void init_bigContent(View bigContentView, LayoutInflater layoutInflater) {}

        @Override
        final public View getBigContent(LayoutInflater layoutInflater, ViewGroup parentView) {
            if (bigContentView == null) {
                bigContentView = layoutInflater.inflate(bigTileId(), parentView, false);
                init_bigContent(bigContentView, layoutInflater);
            }
            return bigContentView;
        }

        @Override
        public void onPause() {

        }

        @Override
        public void onResume() {

        }

        @Override
        public void onInvalidData(Class dataClass) {

        }
    }

    private void setupSmallTileView(View smallContentView) {
        ViewGroup layout = view(R.id.start_tile_small_content, ViewGroup.class);
        layout.removeAllViews();
        layout.addView(smallContentView);
    }

    private void setupBigTileView(View bigContentView) {
        ViewGroup layout = view(R.id.start_tile_big_content, ViewGroup.class);
        layout.removeAllViews();
        layout.addView(bigContentView);
    }

    class StatisticTile extends AbstractTileController{

        private TextView averageValueText;
        private TextView totalValueText;
        private TextViewExt todaySmokeCountText;
        private SmokePeriodHistogramView histogramView;

        @Override
        public String caption() {
            return getString(R.string.statistics);
        }

        @Override
        protected int smallTileId() {
            return R.layout.tile_small_stat;
        }

        @Override
        protected int bigTileId() {
            return R.layout.tile_big_stat;
        }

        @Override
        protected void init_smallContent(View smallContentView, LayoutInflater layoutInflater) {
            averageValueText = (TextView) smallContentView.findViewById(R.id.stat_average_text_value);
            totalValueText = (TextView) smallContentView.findViewById(R.id.stat_total_smokes_value);
            todaySmokeCountText = (TextViewExt) smallContentView.findViewById(R.id.stat_today_smokes);
        }

        @Override
        protected void init_bigContent(View bigContentView, LayoutInflater layoutInflater) {
            histogramView = (SmokePeriodHistogramView) bigContentView.findViewById(R.id.stat_chart);
        }

        @Override
        public void onResume() {
            fetchSmokeDetails();
            fetchPeriodData();
        }

        private void fetchSmokeDetails() {
            application().data_smokeDetails().fetch(true, new DataProvider.FetchObserver<PrepareTodaySmokeDetails.TodaySmokeDetails>() {
                @Override
                public void onFetch(PrepareTodaySmokeDetails.TodaySmokeDetails todaySmokeDetails) {
                     totalValueText.setText(""+todaySmokeDetails.total+" "+getString(R.string.smokes));
                     if (todaySmokeDetails.avarage != -1){
                         averageValueText.setText(""+todaySmokeDetails.avarage+" "+getString(R.string.smokes));
                     } else {
                         averageValueText.setText(getString(R.string.not_enough_data));
                     }
                    todaySmokeCountText.setText("" + todaySmokeDetails.todaySmokes, true);
                }

                @Override
                public void onError(DataProvider.FetchError fetchError) {
                    activity().forceCloseWithErrorCode(201);
                }
            });
        }

        private void fetchPeriodData() {
            if (histogramView != null) {
                application().data_periodStat().fetch(true, new DataProvider.FetchObserver<PreparePeriodStatistic.PeriodStatistic>() {
                    @Override
                    public void onFetch(PreparePeriodStatistic.PeriodStatistic periodStatistic) {
                        histogramView.setModel(periodStatistic.smokesPerDayList);
                    }

                    @Override
                    public void onError(DataProvider.FetchError fetchError) {
                        activity().forceCloseWithErrorCode(203);
                    }
                });
            }
        }

        @Override
        public void onInvalidData(Class dataClass) {
            if (histogramView == null) return;
            if (dataClass == PrepareTodaySmokeDetails.TodaySmokeDetails.class){
                fetchSmokeDetails();
            } else if(dataClass == PreparePeriodStatistic.PeriodStatistic.class){
                fetchPeriodData();
            }
        }
    }

    class QuitSmokeTile extends AbstractTileController{

        private ViewGroup dataContent;
        private GridView calendarGrid;
        private View shadow_top;
        private View shadow_bottom;
        private TextView endDateText;
        private TextView endCountText;
        private TextView progressText;
        private final DateFormat dateFormater = DateFormat.getDateInstance();
        private RoundSegmentImageView progressView;

        @Override
        public String caption() {
            return getString(R.string.quit_page_title);
        }

        @Override
        protected int bigTileId() {
            return R.layout.tile_big_quit;
        }

        @Override
        protected int smallTileId() {
            return R.layout.tile_small_quit;
        }

        @Override
        protected void init_smallContent(View smallContentView, LayoutInflater layoutInflater) {
           endDateText  = (TextView) smallContentView.findViewById(R.id.quit_end_date_value);
           endCountText  = (TextView) smallContentView.findViewById(R.id.quit_target_value);
           progressText  = (TextView) smallContentView.findViewById(R.id.quit_progress_value);
           progressView = (RoundSegmentImageView) smallContentView.findViewById(R.id.quit_progress_round_image);
        }

        @Override
        protected void init_bigContent(View bigContentView, LayoutInflater layoutInflater) {
            ViewGroup dayCaptionPanel = (ViewGroup) bigContentView.findViewById(R.id.quit_day_caption_panel);
            List<Pair<String,Boolean>> weekDays = application().getSmockQuitDataManager().weekDaysNames();
            for (Pair<String,Boolean> dayName:weekDays){
                View view = layoutInflater.inflate(R.layout.item_day_caption,dayCaptionPanel, false);
                TextView tv = (TextView) view.findViewById(R.id.item_text);
                tv.setText(dayName.first);
                if (dayName.second){
                    tv.setTypeface(null, Typeface.BOLD);
                }
                dayCaptionPanel.addView(view, dayCaptionPanel.getChildCount());
            }
            dayCaptionPanel.requestLayout();
            shadow_top = bigContentView.findViewById(R.id.quit_top_shadow);
            shadow_bottom = bigContentView.findViewById(R.id.quit_bottom_shadow);
            calendarGrid = (GridView) bigContentView.findViewById(R.id.quit_grid);
            calendarGrid.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {

                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    View firstChildView = view.getChildAt(0);
                    if (firstVisibleItem == 0 && firstChildView != null && firstChildView.getTop() == 0){
                       shadow_top.setVisibility(View.INVISIBLE);
                    }else {
                       shadow_top.setVisibility(View.VISIBLE);
                    }

                    int lastVisiblePosition =view.getLastVisiblePosition();
                    if (visibleItemCount >= totalItemCount){
                        shadow_bottom.setVisibility(View.INVISIBLE);
                    } else if (lastVisiblePosition != totalItemCount-1) {
                        shadow_bottom.setVisibility(View.VISIBLE);
                    } else {
                        View lastChildView = view.getChildAt(view.getChildCount()-1);
                        int delta = view.getHeight() - lastChildView.getBottom();
                        if (Math.abs(delta) < 3){
                            shadow_bottom.setVisibility(View.INVISIBLE);
                        }
                    }
                }
            });
            ;
            dataContent = (ViewGroup) bigContentView.findViewById(R.id.quit_data_panel);
            dataContent.getChildAt(0).setVisibility(View.GONE);
            dataContent.getChildAt(1).setVisibility(View.VISIBLE);
            shadow_top.setVisibility(View.INVISIBLE);
            shadow_bottom.setVisibility(View.INVISIBLE);
            PanelUI.initLightPanel(
                    dataContent.getChildAt(1),
                    getString(R.string.no_quit_program),
                    getString(R.string.quit_program_choose_prompt),
                    getString(R.string.quit_program_prompt_action), new View.OnClickListener(){
                        @Override
                        public void onClick(View v) {
                           performTileSetup();
                        }
                    });
            fetchQuitSchedule();
        }
        private String datesString = null;

        long lastUsedId = Long.MAX_VALUE;
        private void fetchQuitSchedule() {
            application().getSmockQuitDataManager().calculateCalendarLimits(new SmokeQuitCalendarDisplayManager.OnLimitsCalculated() {
                @Override
                public void onLimit(long id, Date startDate, Date endDate) {
                    if (lastUsedId == id) return;
                    lastUsedId = id;

                    if (startDate ==null) {
                        dataContent.getChildAt(0).setVisibility(View.GONE);
                        dataContent.getChildAt(1).setVisibility(View.VISIBLE);
                        shadow_top.setVisibility(View.INVISIBLE);
                        shadow_bottom.setVisibility(View.INVISIBLE);
                        return;
                    }

                    final ListAdapter adapter = new SmokeQuitCalendarAdapter(activity(),
                            startDate,
                            endDate, calendarItemViewFactory());
                    calendarGrid.setAdapter(adapter);
                    dataContent.getChildAt(0).setVisibility(View.VISIBLE);
                    dataContent.getChildAt(1).setVisibility(View.GONE);
                }

                @Override
                public void onError(Exception e) {
                    activity().forceCloseWithErrorCode(401);
                }
            });
        }

        @Override
        public Class<? extends Activity> getSetupActivityClass() {
            return SetupQuitSmokeActivity.class;
        }

        @Override
        public void onInvalidData(Class dataClass) {
            if (GetSmokeQuitSchedule.QuitSchedule.class == dataClass){
                fetchQuitSchedule();
            }else if(PrepareSmokeQuitDetails.Details.class == dataClass){
                fetchSmallContentData();
            }

        }

        @Override
        public void onResume() {
            if (calendarGrid != null){
                fetchQuitSchedule();
            }
            fetchSmallContentData();
        }

        private void fetchSmallContentData() {
            application().data_smokeQuit().fetch(true, new DataProvider.FetchObserver<PrepareSmokeQuitDetails.Details>() {
                @Override
                public void onFetch(PrepareSmokeQuitDetails.Details details) {
                    if (details.todayLimit != -1) {
                        endDateText.setText(details.dayLeftCount+" "+getString(R.string.days));
                        endCountText.setText("" + details.todayLimit + " " + getString(R.string.smokes));
                        progressText.setText("" + details.progress + "%");

                    } else {
                        endDateText.setText(getString(R.string.disabled));
                        endCountText.setText(getString(R.string.disabled));
                        progressText.setText("0%");
                    }
                    progressView.setAngle(360f * ((float) details.progress / 100f));
                    progressView.invalidate();
                }

                @Override
                public void onError(DataProvider.FetchError fetchError) {
                    activity().forceCloseWithErrorCode(401);
                }
            });
        }

        public class SmokeQuitCalendarAdapter extends DateListAdapter{

            private final GetViewImplementation<Date,GetViewImplementation.ViewHolder<Date>> getViewImplementation;

            public SmokeQuitCalendarAdapter(Context context, Date startDate, Date endDate,
                                            GetViewImplementation.ViewHolderFactory<GetViewImplementation.ViewHolder<Date>> viewFactory) {
                super(startDate, endDate);
                this.getViewImplementation = new GetViewImplementation<>(context, this, viewFactory, R.layout.item_quit_smoke_calendar);
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                return getViewImplementation.getView(position,convertView,parent);
            }
        }

        private GetViewImplementation.ViewHolderFactory<GetViewImplementation.ViewHolder<Date>> calendarItemViewFactory() {
            return new GetViewImplementation.ViewHolderFactory<GetViewImplementation.ViewHolder<Date>>() {
                @Override
                public GetViewImplementation.ViewHolder<Date> create(final View forView) {
                    return new GetViewImplementation.ViewHolder<Date>() {

                        private View owner = forView;
                        private View backgroundView = forView.findViewById(R.id.item_background);
                        private TextView mainTextView = (TextView) forView.findViewById(R.id.item_text);
                        private CellBackgroundView cellBackgroundView = (CellBackgroundView) forView.findViewById(R.id.item_cell_background);
                        private View overCrossImage = forView.findViewById(R.id.item_date_past);
                        private View todayImage = forView.findViewById(R.id.item_date_today);

                        private final int text_color_light =  getResources().getColor(R.color.font_white);
                        private final int text_color_dark =  getResources().getColor(R.color.font_dark_light);

                        @Override
                        public void update(final Date date, int position) {
                            cellBackgroundView.resetAll();
                            cellBackgroundView.paintBottom = false;
                            cellBackgroundView.paintLeft = false;
                            if (position < 7) {
                                cellBackgroundView.paintTop = false;
                            }
                            if ((position+1)%7==0){
                                cellBackgroundView.paintRight = false;
                            }
                            SmokeQuitCalendarDisplayManager.DisplayDetails displayDetails = application().getSmockQuitDataManager().getSmokeQuitDateDisplayDetails(date);

                            mainTextView.setTypeface(null, displayDetails.isWeekEnd? Typeface.BOLD:Typeface.NORMAL);
                            mainTextView.setText(displayDetails.mainText);

                            overCrossImage.setVisibility(
                                    (!displayDetails.isPassed
                                            && !displayDetails.isFuture
                                            && !displayDetails.isOutsideQuitProgram) ?  View.VISIBLE:View.GONE);
                            todayImage.setVisibility(displayDetails.isToday?View.VISIBLE:View.GONE);
                            if (displayDetails.isMonthEndWeek){
                                cellBackgroundView.paintWeekEnd = true;
                            }else if (displayDetails.isMonthStartWeek){
                                cellBackgroundView.paintWeekStart = true;
                            }
                            cellBackgroundView.invalidate();

                            DateDetailsActivity.Theme theme = DateDetailsActivity.Theme.WHITE;
                            int backgroundResource = 0;
                            int textColor = text_color_dark;
                            float alpha = 1f;
                            if (displayDetails.isOutsideQuitProgram){
                                alpha = 0.2f;
                            }
                            mainTextView.setAlpha(alpha);
                            //background
                            if (displayDetails.isMonthStart){
                                textColor = text_color_light;
                                if (displayDetails.isNewLimitDay){
                                    theme = DateDetailsActivity.Theme.BLUE;
                                    backgroundResource = R.drawable.background_cal_month_limit;
                                }else {
                                    theme = DateDetailsActivity.Theme.RED;
                                    backgroundResource = R.drawable.background_cal_month;
                                }
                            } else if (displayDetails.isNewLimitDay){
                                textColor = text_color_light;
                                backgroundResource = R.drawable.background_cal_day_limit;
                                theme = DateDetailsActivity.Theme.BLUE;
                            }
                            backgroundView.setBackgroundResource(backgroundResource);
                            mainTextView.setTextColor(textColor);

                            if (!displayDetails.isOutsideQuitProgram) {
                                final DateDetailsActivity.Theme finalTheme = theme;
                                owner.setOnTouchListener(new View.OnTouchListener() {
                                    @Override
                                    public boolean onTouch(View v, MotionEvent event) {
                                        if (event.getAction() == MotionEvent.ACTION_UP) {
                                            showDetailsForDate(v, date, finalTheme, new PointF(event.getRawX(), event.getRawY()));
                                        }
                                        return true;
                                    }
                                });
                            }
                            owner.invalidate();
                        }

                        @Override public void cleanup() {
                            owner.setOnTouchListener(null);
                        }
                    };
                }
            };
        }

    }

    class MoneyBoxTile extends AbstractTileController{

        private ViewGroup contentPanel;
        private TextView savedMoneyView;
        private TextView titleView;
        private TextView descriptionView;
        private TextView priceView;
        private ImageView imageView;

        @Override
        public String caption() {
            return getString(R.string.moneybox);
        }

        @Override
        protected int smallTileId() {
            return R.layout.tile_small_moneybox;
        }

        @Override
        protected int bigTileId() {
            return R.layout.tile_big_moneybox;
        }

        @Override
        protected void init_smallContent(View smallContentView, LayoutInflater layoutInflater) {
            savedMoneyView = (TextView) smallContentView.findViewById(R.id.money_saved_value_text);
            super.init_smallContent(smallContentView, layoutInflater);
        }

        @Override
        public void onResume() {
            fetchProgress();
            if (contentPanel!= null){
                fetchTargetDescription();
            }
            super.onResume();
        }

        @Override
        public void onInvalidData(Class dataClass) {
            if (PrepareMoneyBoxProgress.MoneyBoxProgress.class == dataClass){
                fetchProgress();
            }
            if (SmookerModel.MoneyBoxTargetDescription.class == dataClass){
                if (contentPanel != null){
                    fetchTargetDescription();
                }
            }
        }

        private String imageId = null;
        private void fetchTargetDescription() {
            application().data_moneyBoxTarget().fetch(true,new DataProvider.FetchObserver<SmookerModel.MoneyBoxTargetDescription>() {
                @Override
                public void onFetch(SmookerModel.MoneyBoxTargetDescription description) {
                    if (!description.isActivated()){
                        imageView.setImageBitmap(null);
                        titleView.setText("");
                        return;
                    }
                    titleView.setText(description.title);
                    descriptionView.setText(description.description);
                    if (imageId == null || !imageId.equals(description.imageId)){
                        fetchImage(description.imageId);
                    }
                }

                @Override
                public void onError(DataProvider.FetchError fetchError) {
                    activity().forceCloseWithErrorCode(31);
                }
            });
        }

        private void fetchImage(final String imageId) {
            if (imageView.getHeight() == 0){
                activity().runLastOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        fetchImage(imageId);
                    }
                },100);
                return;
            }
            application().loadToBitmap(imageId, imageView.getWidth(), imageView.getHeight(), new SmookerApplication.OnImageLoadedObserver() {
                @Override
                public void onResult(String imageId, Bitmap bitmap) {
                    MoneyBoxTile.this.imageId = imageId;
                    imageView.setImageBitmap(bitmap);
                }

                @Override
                public void onFail() {
                     activity().forceCloseWithErrorCode(44);
                }
            });
        }

        private void fetchProgress() {
            application().data_moneyBoxProgress().fetch(true,new DataProvider.FetchObserver<PrepareMoneyBoxProgress.MoneyBoxProgress>() {
                @Override
                public void onFetch(PrepareMoneyBoxProgress.MoneyBoxProgress moneyBoxProgress) {
                    if (moneyBoxProgress.isDisabled()){
                        savedMoneyView.setText(R.string.disabled);
                        if (contentPanel != null){
                            getMainContentPanel().setVisibility(View.INVISIBLE);
                            getSuggestSetupPanel().setVisibility(View.VISIBLE);
                        }
                    }else{
                        if (contentPanel != null) {
                            getMainContentPanel().setVisibility(View.VISIBLE);
                            getSuggestSetupPanel().setVisibility(View.INVISIBLE);
                            priceView.setText(asMoneyString(moneyBoxProgress.totalPrice));
                        }
                        savedMoneyView.setText(asMoneyString(moneyBoxProgress.savedMoney));
                    }
                    //TODO: show progress
                }

                @Override
                public void onError(DataProvider.FetchError fetchError) {
                    activity().forceCloseWithErrorCode(30);
                }
            });
        }

        private String asMoneyString(Float money) {
            DecimalFormat df = new DecimalFormat();
            org.monroe.team.smooker.app.common.constant.Currency currency = org.monroe.team.smooker.app.common.constant.Currency.byId(application().getSetting(Settings.CURRENCY_ID));
            df.setCurrency(currency.nativeInstance);
            df.setMaximumFractionDigits(2);
            df.setMinimumFractionDigits(2);
            return df.format(money) +" "+currency.symbol;
        }

        @Override
        public Class<? extends Activity> getSetupActivityClass() {
            return SetupMoneyboxActivity.class;
        }

        @Override
        protected void init_bigContent(View bigContentView, LayoutInflater layoutInflater) {
            contentPanel = (ViewGroup) bigContentView.findViewById(R.id.money_content_panel);
            titleView = (TextView) bigContentView.findViewById(R.id.money_title_text);
            descriptionView = (TextView) bigContentView.findViewById(R.id.money_description_text);
            priceView = (TextView) bigContentView.findViewById(R.id.money_price_text);
            imageView = (ImageView) bigContentView.findViewById(R.id.money_image);
            PanelUI.initLightPanel(
                    getSuggestSetupPanel(),
                    "Save your money on something",
                    "Choose your currency, price and start saving",
                    "Setup", new View.OnClickListener(){
                        @Override
                        public void onClick(View v) {
                            performTileSetup();
                        }
                    });
        }

        private View getMainContentPanel() {
            return contentPanel.getChildAt(0);
        }

        private View getSuggestSetupPanel() {
            return contentPanel.getChildAt(1);
        }
    }

}
