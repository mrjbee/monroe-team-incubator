package org.monroe.team.smooker.app.android;

import android.animation.Animator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.apache.http.client.CircularRedirectException;
import org.monroe.team.android.box.app.ActivitySupport;
import org.monroe.team.android.box.app.ui.animation.AnimatorListenerSupport;
import org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceController;
import org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder;
import org.monroe.team.android.box.app.ui.animation.apperrance.DefaultAppearanceController;
import org.monroe.team.android.box.utils.DisplayUtils;
import org.monroe.team.smooker.app.R;
import org.monroe.team.smooker.app.android.view.CircleAppearanceRelativeLayout;
import org.monroe.team.smooker.app.common.constant.Settings;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.alpha;
import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.animateAppearance;
import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.duration_constant;
import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.interpreter_accelerate;
import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.interpreter_decelerate;
import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.interpreter_overshot;
import static org.monroe.team.android.box.app.ui.animation.apperrance.AppearanceControllerBuilder.ySlide;

public class BugSubmitActivity extends ActivitySupport<SmookerApplication> {

    private AppearanceController baseContainerAC;
    private AppearanceController shadowAC;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        crunch_requestNoAnimation();
        setContentView(R.layout.activity_bug_submit);
        baseContainerAC = animateAppearance(view(R.id.content), ySlide(0f, -DisplayUtils.screenHeight(getResources())))
                .showAnimation(duration_constant(600), interpreter_overshot())
                .hideAnimation(duration_constant(300), interpreter_decelerate(0.8f))
                .build();
        shadowAC = animateAppearance(view(R.id.background_shadow),alpha(0.5f,0f))
                .showAnimation(duration_constant(400), interpreter_accelerate(0.8f))
                .hideAnimation(duration_constant(400), interpreter_decelerate(0.8f))
                .hideAndGone()
                .build();

        if (isFirstRun(savedInstanceState)){
            baseContainerAC.hideWithoutAnimation();
            shadowAC.hideWithoutAnimation();
        }else{
           baseContainerAC.showWithoutAnimation();
           shadowAC.showWithoutAnimation();
        }

        view(R.id.skip_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doRethrow();
            }
        });

        view(R.id.skip_always_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                application().settings().set(Settings.ENABLED_BUG_SUBMISSION, false);
                doRethrow();
            }
        });

        view(R.id.submit_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto", "monroe.dev.team@gmail.com", null));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Smooker: Bug Report");
                emailIntent.putExtra(Intent.EXTRA_TEXT   , exceptionStackTrace());
                try {
                    startActivityForResult(Intent.createChooser(emailIntent, "Send email..."), 101);
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(BugSubmitActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private String exceptionStackTrace() {
        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            application().getAwaitingError().printStackTrace(pw);
            String stackTrace = sw.toString();
            return stackTrace;
        }catch (Exception error){
            return "No stack trace "+ application().getAwaitingError().getMessage();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isFirstRun()){
            shadowAC.show();
            baseContainerAC.showAndCustomize(new AppearanceController.AnimatorCustomization() {
                @Override
                public void customize(Animator animator) {
                    animator.setStartDelay(200);
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101){
            doRethrow();
        }
    }

    @Override
    public void onBackPressed() {
        doRethrow();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return doRethrow();
    }

    private boolean doRethrow() {
        throw new RuntimeException(application().getAwaitingError());
    }

}
