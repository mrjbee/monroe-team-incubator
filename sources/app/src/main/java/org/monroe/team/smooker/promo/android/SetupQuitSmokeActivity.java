package org.monroe.team.smooker.promo.android;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.monroe.team.smooker.promo.R;
import org.monroe.team.smooker.promo.common.constant.Settings;
import org.monroe.team.smooker.promo.common.quitsmoke.QuitSmokeDifficultLevel;


public class SetupQuitSmokeActivity extends SetupGeneralActivity {


    private String uiStateCheckSum;

    @Override
    protected int setup_layout() {
        return R.layout.setup_page_quit_smoking;
    }

    @Override
    protected int caption_string()  {
        return R.string.tile_quit_name;
    }

    @Override
    protected void action_start() {

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

        uiStateCheckSum = uiCheckSum();

    }

    private String uiCheckSum() {
        return ""+view_text(R.id.qs_start_edit).getText().toString().hashCode()+"" +
                ""+view_text(R.id.qs_end_edit).getText().toString().hashCode()+"" +
                ""+view(R.id.qs_level_seekBar,SeekBar.class).getProgress();
    }

    @Override
    protected void action_apply() {
        performQuitSmokeSetup();
    }

    @Override
    protected void action_revert() {
        action_start();
    }

    @Override
    protected void action_exit() {
        String existingUICheckSum = uiCheckSum();
        onBackPressed();
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
            case DISABLED: return getString(R.string.quit_program_disable);
            case LOWEST: return getString(R.string.quit_program_lowest);
            case LOW: return getString(R.string.quit_program_low);
            case SMART: return getString(R.string.quit_program_smart);
            case SMARTEST: return getString(R.string.quit_program_smarter);
            case HARD: return getString(R.string.quit_program_hard);
            case HARDEST: return getString(R.string.quit_program_hardest);
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
                Toast.makeText(this, getString(R.string.quit_warn_not_times_per_day), Toast.LENGTH_SHORT).show();
                return;
            }
            smokePerDay = Integer.parseInt(view(R.id.qs_start_edit,TextView.class).getText().toString());
        }

        String text = view_text(R.id.qs_end_edit).getText().toString();
        int desireSmokePerDayCount = 0;

        if (difficult.mayHaveDifferentTargetCount()) {
            if (text.trim().length() == 0) {
                Toast.makeText(this, getString(R.string.quit_warn_no_desire_count), Toast.LENGTH_LONG).show();
                return;
            }
            desireSmokePerDayCount = Integer.parseInt(text);
        }

        if (difficult.mayHaveDifferentTargetCount() && desireSmokePerDayCount > smokePerDay){
            Toast.makeText(this, getString(R.string.quit_warn_desire_more_then_average), Toast.LENGTH_LONG).show();
            return;
        }

        final Integer finalSmokePerDay = smokePerDay;
        final int finalDesireSmokePerDayCount = desireSmokePerDayCount;

        if (oldDifficult == QuitSmokeDifficultLevel.DISABLED){
            doActualUpdate(difficult, finalSmokePerDay, finalDesireSmokePerDayCount);
            finish();
        } else {

            String message = getString(R.string.quit_alert_text_program_cahnge);
            if (difficult == QuitSmokeDifficultLevel.DISABLED) {
                message = getString(R.string.quit_alert_text_program_disable);
            } else if(oldDifficult == difficult){
                message = getString(R.string.quit_alert_text_program_restart);
            }

            AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.quit_alert_change_caption))
                    .setMessage(message)
                    .setPositiveButton(getString(R.string.general_yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            doActualUpdate(difficult, finalSmokePerDay, finalDesireSmokePerDayCount);
                            finish();
                        }
                    }).setNegativeButton(getString(R.string.general_no), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }).show();
        }

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
    }

}