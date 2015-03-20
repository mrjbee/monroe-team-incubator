package org.monroe.team.smooker.app.android;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.monroe.team.android.box.app.ActivitySupport;
import org.monroe.team.smooker.app.R;
import org.monroe.team.smooker.app.android.SmookerApplication;
import org.monroe.team.smooker.app.common.constant.Settings;
import org.monroe.team.smooker.app.common.quitsmoke.QuitSmokeDifficultLevel;


public class SetupQuitSmokeActivity extends ActivitySupport<SmookerApplication> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_general);
        getLayoutInflater().inflate(R.layout.setup_page_quit_smoking, (ViewGroup)view(R.id.setup_content_panel), true);

        view_text(R.id.qs_start_edit).setText(application().getSettingAsString(Settings.QUITE_START_SMOKE));
        view_text(R.id.qs_end_edit).setText(application().getSettingAsString(Settings.QUITE_END_SMOKE));

        view(R.id.qs_level_seekBar,SeekBar.class).setMax(QuitSmokeDifficultLevel.difficultCount()-1);
        view(R.id.qs_level_seekBar,SeekBar.class).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int difficultIndex = progress;
                QuitSmokeDifficultLevel difficult = QuitSmokeDifficultLevel.levelByIndex(difficultIndex);
                updateUIByDifficultLevel(difficult);
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        view(R.id.qs_level_seekBar,SeekBar.class).setProgress(application().getSetting(Settings.QUIT_PROGRAM_INDEX));
        updateUIByDifficultLevel(QuitSmokeDifficultLevel.levelByIndex(application().getSetting(Settings.QUIT_PROGRAM_INDEX)));
        view_button(R.id.qs_apply_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performQuitSmokeSetup();
            }
        });
    }

    private void updateUIByDifficultLevel(QuitSmokeDifficultLevel difficult) {
        view(R.id.qs_end_edit).setEnabled(difficult.mayHaveDifferentTargetCount());
        view(R.id.qs_end_text).setEnabled(difficult.mayHaveDifferentTargetCount());
        view(R.id.qs_start_edit).setEnabled(difficult.mayHaveDifferentTargetCount());
        view_text(R.id.qs_start_text).setEnabled(difficult.mayHaveDifferentTargetCount());
        String programDescription = getProgramDescription(difficult);
        view_text(R.id.qs_level_label_description).setText(programDescription);
    }


    private String getProgramDescription(QuitSmokeDifficultLevel difficult) {
        switch (difficult){
            case DISABLED: return getString(R.string.quit_page_program_description_disabled);
            case LOWEST: return getString(R.string.quit_page_program_description_lowest);
            case LOW: return getString(R.string.quit_page_program_description_low);
            case SMART: return getString(R.string.quit_page_program_description_smart);
            case SMARTEST: return getString(R.string.quit_page_program_description_smartest);
            case HARD: return getString(R.string.quit_page_program_description_hard);
            case HARDEST: return getString(R.string.quit_page_program_description_hardest);
        }
        throw new IllegalStateException();
    }

    public void performQuitSmokeSetup() {

        final QuitSmokeDifficultLevel difficult = QuitSmokeDifficultLevel.levelByIndex(view(R.id.qs_level_seekBar,SeekBar.class).getProgress());
        final QuitSmokeDifficultLevel oldDifficult = QuitSmokeDifficultLevel.levelByIndex(application().getSetting(Settings.QUIT_PROGRAM_INDEX));

        Integer smokePerDay = null;
        if (difficult.mayHaveDifferentTargetCount()) {
            String startText = ((TextView) findViewById(R.id.qs_start_edit)).getText().toString();
            if (startText.trim().length() == 0) {
                Toast.makeText(this, getString(R.string.quit_page_not_set_per_day), Toast.LENGTH_SHORT).show();
                return;
            }
            smokePerDay = Integer.parseInt(view(R.id.qs_start_edit,TextView.class).getText().toString());
        }

        String text = view_text(R.id.qs_end_edit).getText().toString();
        int desireSmokePerDayCount = 0;

        if (difficult.mayHaveDifferentTargetCount()) {
            if (text.trim().length() == 0) {
                Toast.makeText(this, getString(R.string.quit_page_not_set_desire_count), Toast.LENGTH_LONG).show();
                return;
            }
            desireSmokePerDayCount = Integer.parseInt(text);
        }

        if (difficult.mayHaveDifferentTargetCount() && desireSmokePerDayCount > smokePerDay){
            Toast.makeText(this, getString(R.string.quit_page_desire_more_then_per_day), Toast.LENGTH_LONG).show();
            return;
        }

        final Integer finalSmokePerDay = smokePerDay;
        final int finalDesireSmokePerDayCount = desireSmokePerDayCount;

        if (!application().getSetting(Settings.IS_SMOKE_QUIT_ACTIVE) ||
                (oldDifficult == difficult && !oldDifficult.mayHaveDifferentTargetCount())){
            doActualUpdate(difficult, finalSmokePerDay, finalDesireSmokePerDayCount);
            return;
        }

        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.quit_page_change_program_alert_title))
                .setMessage(getString(R.string.quit_page_change_program_alert_content))
                .setPositiveButton(getString(R.string.quit_page_change_program_alert_yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        doActualUpdate(difficult, finalSmokePerDay, finalDesireSmokePerDayCount);
                        finish();
                    }
                }).setNegativeButton(getString(R.string.quit_page_change_program_alert_no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {}
                }).show();
    }

    private void doActualUpdate(QuitSmokeDifficultLevel difficult, Integer smokePerDay, int desireSmokePerDayCount) {
        application().setSetting(Settings.QUIT_PROGRAM_INDEX, difficult.toIndex());
        if (difficult == QuitSmokeDifficultLevel.DISABLED){
            application().setSetting(Settings.IS_SMOKE_QUIT_ACTIVE, false);
            application().setSetting(Settings.QUITE_START_SMOKE, null);
        } else {
            application().setSetting(Settings.IS_SMOKE_QUIT_ACTIVE, true);
            application().setSetting(Settings.QUITE_START_SMOKE, smokePerDay);
        }
        application().setSetting(Settings.QUITE_END_SMOKE, desireSmokePerDayCount);
        application().changeQuitSmokeProgram(
            difficult,
            smokePerDay == null? -1:smokePerDay,
            desireSmokePerDayCount
        );

        //TODO: implement actual smoke quit program activation
        /*
            model().execute(SetupQuitSmokeProgram.class,new SetupQuitSmokeProgram.QuitSmokeProgramRequest(
                    difficult,
                    smokePerDay == null?-1:smokePerDay,
                    desireSmokePerDayCount
            ));
        */
    }

}
