package org.monroe.team.smooker.app;

import android.animation.Animator;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.monroe.team.android.box.app.ActivitySupport;
import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.*;

import org.monroe.team.android.box.app.ui.AppearanceControllerOld;
import org.monroe.team.android.box.app.ui.SlideTouchGesture;
import org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceController;
import org.monroe.team.android.box.utils.DisplayUtils;
import org.monroe.team.corebox.utils.Closure;
import org.monroe.team.corebox.utils.Lists;
import org.monroe.team.smooker.app.android.SmookerApplication;

import java.util.ArrayList;
import java.util.List;


public class StartActivity extends ActivitySupport<SmookerApplication> {

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

    float height_px_close_dash;
    float height_px_tile;
    float titleSmallSize;
    float titleBigSize;

    List<TileController> tileControllerList = new ArrayList<>(3);
    List<HoleController> holeControllerList;
    private int currentTileIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        setupTilesControllers();
    }

    @Override
    protected void onActivitySize(int activityWidth, int activityHeight) {

        height_px_close_dash = activityHeight - dpToPx(height_dp_background_bottom(),height_dp_open_picker());

        titleSmallSize =  DisplayUtils.spToPx(20,getResources());
        titleBigSize = DisplayUtils.spToPx(30, getResources());

        bottomLayerAC = animateAppearance(view(R.id.start_bottom_layer),ySlide(- height_px_close_dash, 0))
                .showAnimation(duration_constant(400), interpreter_overshot())
                .hideAnimation(duration_auto_fint(0.5f), interpreter_decelerate(0.3f))
                .build();

        tileCaptionTextAC = animateAppearance(view(R.id.start_tile_caption_text),
                scale(1.3f, 1f))
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

        height_px_tile = activityHeight - dpToPx(height_dp_background_bottom() - height_dp_tile_title());
        tileSpaceWraperAC = animateAppearance(view(R.id.start_tile_space_wrap_panel),
                heightSlide(
                        (int) dpToPx(height_dp_open_picker(), height_dp_tile_title()),
                        (int) height_px_tile
                ))
                .showAnimation(duration_constant(200), interpreter_accelerate(0.3f))
                .hideAnimation(duration_auto_int(0.5f), interpreter_decelerate(0.3f)).build();

        float tileBigDataHeight = activityHeight - dpToPx(height_dp_open_picker() + height_dp_tile_title() * 1.3f + height_dp_tile_small_content() + height_dp_holes());
        tileBigContentAC = animateAppearance(view(R.id.start_tile_big_content), heightSlide((int) tileBigDataHeight, 0))
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
                xSlide(0,-DisplayUtils.screenWidth(getResources()))
        )
                .showAnimation(duration_constant(200), interpreter_overshot())
                .hideAnimation(duration_constant(200), interpreter_decelerate(0.5f))
                .hideAndGone().build();

        settingAlternativeBtnAC = combine(
                animateAppearance(view(R.id.start_setting_dublicate_btn), scale(1f,0f))
                .showAnimation(duration_constant(400), interpreter_overshot())
                .hideAnimation(duration_constant(400), interpreter_accelerate(0.4f))
                .hideAndInvisible(),
                animateAppearance(view(R.id.start_setting_dublicate_btn), rotate(180+90,0))
                .showAnimation(duration_constant(300), interpreter_overshot())
                .hideAnimation(duration_constant(300), interpreter_accelerate(0.4f))
                );

        tileShowFromLeftAC.showWithoutAnimation();
        tileShowFromRightAC.showWithoutAnimation();
        tileBigContentAC.hideWithoutAnimation();
        tileSpaceWraperAC.hideWithoutAnimation();
        pickerRotationAC.showWithoutAnimation();
        bottomLayerAC.hideWithoutAnimation();
        tileCaptionTextAC.hideWithoutAnimation();
        tileCaptionTextChangeAC.showWithoutAnimation();
        settingAlternativeBtnAC.hideWithoutAnimation();
        setupDashCloseState();

        setupTileBoard();
        applyTileContentUsing(0);
        setupTileCaption();
    }

    private float dpToPx(float ... values){
        int value = 0 ;
        for (float v : values) {
            value += v;
        }
        return DisplayUtils.dpToPx(value, getResources());
    }

    private int height_dp_open_picker(){
        return 33;
    }

    private int height_dp_tile_small_content(){
        return 100;
    }

    private int height_dp_holes(){
        return 20;
    }

    private int height_dp_tile_title(){
        return 60;
    }

    private int height_dp_background_bottom(){
        return 200;
    }

    private void setupTilesControllers() {
        tileControllerList.add(new StatisticTile());
        tileControllerList.add(new QuitSmokeTile());
        tileControllerList.add(new MotivationTile());
        holeControllerList = Lists.collect(tileControllerList, new Closure<TileController, HoleController>() {
            @Override
            public HoleController execute(TileController arg) {
                ViewGroup parent = (ViewGroup) view(R.id.start_tile_hole_place);
                View root_view = getLayoutInflater().inflate(R.layout.item_hole,
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
                AppearanceController hideAC = (slideValue >0)? tileShowFromLeftAC:tileShowFromRightAC;
                final AppearanceController showAC = (slideValue <0)? tileShowFromLeftAC:tileShowFromRightAC;
                final int nextTileIndex = calculateTileIndex((slideValue >0)?1:-1);
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
                                        changeAnimator.addListener(new AppearanceControllerOld.AnimatorListenerAdapter(){
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
               view(R.id.start_bottom_layer).setTranslationY((float) (-400 *(fraction)));
               view(R.id.start_tile_space_wrap_panel).getLayoutParams().height = (int) (height_px_tile - 400*fraction);
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
                                tileBigContentAC.showAndCustomize(new AppearanceController.AnimatorCustomization() {
                                    @Override
                                    public void customize(Animator animator) {
                                        animator.addListener(new AppearanceControllerOld.AnimatorListenerAdapter() {
                                            @Override
                                            public void onAnimationEnd(Animator animation) {
                                                settingAlternativeBtnAC.show();
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
                bottomLayerAC.hide();
                tileSpaceWraperAC.hide();
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
                float scaleFactor = 1.3f - 0.5f *fraction;
                view(R.id.start_tile_caption_text,TextView.class).setScaleX(scaleFactor);
                view(R.id.start_tile_caption_text,TextView.class).setScaleY(scaleFactor);
                view(R.id.start_tile_space_wrap_panel).getLayoutParams().height = (int) (startHeight + height_px_close_dash * fraction);
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
                settingAlternativeBtnAC.show();
                tileSpaceWraperAC.showAndCustomize(new AppearanceController.AnimatorCustomization() {
                    @Override
                    public void customize(Animator animator) {
                        animator.addListener(new AppearanceControllerOld.AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                tileBigContentAC.show();
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
        tileBigContentAC.hide();
        settingAlternativeBtnAC.hide();
    }

    private void closeDash() {
        tileSpaceWraperAC.hide();
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

    private int calculateTileIndex(int step) {
        int answer = currentTileIndex + step;
        if (answer < 0){
            answer = Lists.getLastIndex(tileControllerList);
        }
        if (answer > Lists.getLastIndex(tileControllerList)){
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
    }

    private void applyTileContentUsing(int tileControllerIndex) {
        currentTileIndex = tileControllerIndex;
        //TODO: place data to panels
    }

    private void setupTileCaption() {
        holeControllerList.get(currentTileIndex).select();
        String title =  tileControllerList.get(currentTileIndex).caption();
        view_text(R.id.start_tile_caption_text).setText(title);
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
    }

    class StatisticTile implements TileController{

        @Override
        public String caption() {
            return "Statistics";
        }
    }

    class MotivationTile implements TileController{

        @Override
        public String caption() {
            return "Motivation";
        }
    }

    class QuitSmokeTile implements TileController{

        @Override
        public String caption() {
            return "Quitting";
        }
    }
}
