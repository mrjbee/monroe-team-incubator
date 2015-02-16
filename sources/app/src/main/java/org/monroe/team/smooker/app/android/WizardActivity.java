package org.monroe.team.smooker.app.android;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.monroe.team.android.box.services.SettingManager;
import org.monroe.team.smooker.app.R;
import org.monroe.team.smooker.app.common.constant.Currency;
import org.monroe.team.smooker.app.common.constant.Settings;
import org.monroe.team.smooker.app.common.quitsmoke.QuitSmokeDifficultLevel;
import org.monroe.team.smooker.app.common.SupportActivity;
import org.monroe.team.smooker.app.common.constant.SetupPage;
import org.monroe.team.smooker.app.uc.SetupQuitSmokeProgram;

import java.util.List;


public class WizardActivity extends SupportActivity {

    private AlertDialog.Builder TOAST_WARNING;
    private List<SetupPage> requestsStack;
    private int requestIndex=-1;
    private SetupPageHandler pageHandler;
    private boolean force =false;
    private boolean awareShown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TOAST_WARNING = new AlertDialog.Builder(this);
        awareShown = false;
        setContentView(R.layout.activity_wizard);
        fetchSetupData();
        updateHandlerUI();
    }

    private void updateHandlerUI() {
        initHandler();
        view(R.id.wizard_back_btn).setVisibility((requestIndex == 0 && force)?View.INVISIBLE:View.VISIBLE);
        ((TextView)findViewById(R.id.wizzard_setting_label)).setText(pageHandler.caption);
        ((TextView)findViewById(R.id.wizzard_description_label)).setText(pageHandler.description);
        View view = getLayoutInflater().inflate(
                pageHandler.layout,
                (android.view.ViewGroup) findViewById(R.id.wizzard_content_layout),
                false);

        ((ViewGroup) findViewById(R.id.wizzard_content_layout)).removeAllViews();
        ((ViewGroup) findViewById(R.id.wizzard_content_layout)).addView(view);
        pageHandler.onCreateUI(this);
        application().onSetupPageShown(requestsStack.get(requestIndex));
    }

    public void performBack(View v){
        if (requestIndex < 1){
            if (!force){
                setResult(RESULT_OK);
                finish();
            } else {
                if (!awareShown){
                    Toast.makeText(this,
                            getString(R.string.a_wizard_force_launch_exit),
                            Toast.LENGTH_LONG).show();
                    awareShown = true;
                } else {
                    setResult(RESULT_CANCELED);
                    finish();
                }
            }
        } else {
            requestIndex --;
            updateHandlerUI();
        }
    }
    public void performNext(View v) {
        performNext(true);
    }
    public void performNext(boolean doPageHandlerCheck){
        if (doPageHandlerCheck){
            AlertDialog.Builder builder = pageHandler.persistsSetup(this);
            if (builder != null){
                if (builder == TOAST_WARNING) return;
                builder.show();
                return;
            }
        }
        awareShown = false;
        requestIndex++;
        if (requestIndex > requestsStack.size()-1){
            setResult(RESULT_OK);
            finish();
        } else {
            updateHandlerUI();
        }
    }

    private void fetchSetupData() {
        if (getIntent() == null || getIntent().getExtras() == null){
            throw new IllegalStateException("No bundle data.");
        }
        Bundle bundle = getIntent().getExtras();
        requestsStack = (List<SetupPage>) bundle.getSerializable("PAGE_STACK");
        requestIndex = 0;
        force = bundle.getBoolean("FORCE",false);

        if (requestsStack == null || requestIndex < 0){
            throw new IllegalStateException("Not enough data: requestIndex = "+requestIndex+" ; request stack = "+requestsStack);
        }
    }

    public void initHandler() {
        SetupPage curPage = requestsStack.get(requestIndex);
        switch (curPage){
            case WELCOME_PAGE: pageHandler = new WelcomePageHandler();break;
            case GENERAL: pageHandler = new GeneralSetupHandler();break;
            case QUIT_PROGRAM: pageHandler = new QuitSmokingSetupHandler();break;
            case UI: pageHandler = new UIPageHandler();break;
            default: throw new IllegalStateException("Unsupported page "+ curPage);
        }
    }

    @Override
    public void onBackPressed() {
        performBack(null);
    }


    public <Type> Type getSetting(SettingManager.SettingItem<Type> item){
        return application().settings().get(item);
    }

    public <Type> String getSettingAsString(SettingManager.SettingItem<Type> item){
        Type answer = application().settings().get(item);
        return (answer == null)? "": String.valueOf(answer);
    }

    public <Type> void setSetting(SettingManager.SettingItem<Type> item, Type value){
        application().settings().set(item, value);
    }
    //===============================

    public abstract static class SetupPageHandler {

        private final String caption;
        private final String description;
        private final int layout;

        protected SetupPageHandler(String caption, String description, int layout) {
            this.caption = caption;
            this.description = description;
            this.layout = layout;
        }

        public AlertDialog.Builder persistsSetup(WizardActivity wizardActivity) {
            return null;
        }

        public void onCreateUI(WizardActivity wizardActivity){};
    }


    public  class WelcomePageHandler extends SetupPageHandler {

        protected WelcomePageHandler() {
            super(getString(R.string.welcome_page_title),
                   getString(R.string.welcome_page_about),
                   R.layout.setup_page_welcome
            );
        }

    }


    public class GeneralSetupHandler extends SetupPageHandler {

        protected GeneralSetupHandler() {
            super(getString(R.string.general_page_title),
                    getString(R.string.general_page_about),
                    R.layout.setup_page_general
            );
        }

        @Override
        public void onCreateUI(final WizardActivity wizardActivity) {

            final Spinner spinner = wizardActivity.view(Spinner.class, R.id.gs_cur_spinner);
            // Create an ArrayAdapter using the string array and a default spinner layout
            ArrayAdapter<Currency> adapter = new ArrayAdapter<Currency>(wizardActivity, android.R.layout.simple_spinner_item,Currency.SUPPORTED_CURRENCIES){
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    if(view instanceof TextView){
                        TextView textView = (TextView) view;
                        Currency currency = getItem(position);
                        textView.setText(currency.symbol);
                    }
                    return view;
                }
            };
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            // Apply the adapter to the spinner
            spinner.setAdapter(adapter);

            spinner.setSelection(Currency.supportedArrayIndex(
                    Currency.byId(wizardActivity.getSetting(Settings.CURRENCY_ID))));
            wizardActivity.view(EditText.class,R.id.gs_price_edit)
                    .setText(wizardActivity.getSettingAsString(Settings.SMOKE_PRICE));
        }

        @Override
        public AlertDialog.Builder persistsSetup(WizardActivity wizardActivity) {
            String text = ((TextView)wizardActivity.findViewById(R.id.gs_price_edit)).getText().toString();
            if (text.trim().length() == 0){
                Toast.makeText(wizardActivity,getString(R.string.general_page_smoke_cost_not_set),Toast.LENGTH_SHORT).show();
                return wizardActivity.TOAST_WARNING;
            }

            float smokeCost = Math.abs(Float.parseFloat(wizardActivity
                    .view(TextView.class, R.id.gs_price_edit).getText().toString()));

            Spinner spinner = wizardActivity.view(Spinner.class, R.id.gs_cur_spinner);

            wizardActivity.setSetting(Settings.SMOKE_PRICE,smokeCost);
            wizardActivity.setSetting(Settings.CURRENCY_ID, ((Currency) spinner.getSelectedItem()).id);

            return null;
        }
    }


    public class QuitSmokingSetupHandler extends SetupPageHandler {

        protected QuitSmokingSetupHandler() {
            super(getString(R.string.quit_page_title),
                  getString(R.string.quit_page_about),
                   R.layout.setup_page_quit_smoking
            );
        }

        @Override
        public void onCreateUI(final WizardActivity wizardActivity) {
            wizardActivity.view(EditText.class,R.id.qs_start_edit).setText(wizardActivity.getSettingAsString(Settings.QUITE_START_SMOKE));
            wizardActivity.view(EditText.class,R.id.qs_end_edit).setText(wizardActivity.getSettingAsString(Settings.QUITE_END_SMOKE));
            wizardActivity.view(SeekBar.class,R.id.qs_level_seekBar).setMax(QuitSmokeDifficultLevel.difficultCount()-1);
            wizardActivity.view(SeekBar.class,R.id.qs_level_seekBar).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    int difficultIndex = progress;
                    QuitSmokeDifficultLevel difficult = QuitSmokeDifficultLevel.levelByIndex(difficultIndex);
                    updateUIByDifficultLevel(difficult,wizardActivity);
                }

                @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                @Override public void onStopTrackingTouch(SeekBar seekBar) {}
            });
            wizardActivity.view(SeekBar.class,R.id.qs_level_seekBar).setProgress(wizardActivity.getSetting(Settings.QUIT_PROGRAM_INDEX));
            updateUIByDifficultLevel(QuitSmokeDifficultLevel.levelByIndex(wizardActivity.getSetting(Settings.QUIT_PROGRAM_INDEX)),
                    wizardActivity);
        }

        private void updateUIByDifficultLevel(QuitSmokeDifficultLevel difficult, WizardActivity wizardActivity) {
            wizardActivity.view(EditText.class, R.id.qs_end_edit).setEnabled(difficult.mayHaveDifferentTargetCount());
            wizardActivity.view(TextView.class,R.id.qs_end_text).setEnabled(difficult.mayHaveDifferentTargetCount());
            wizardActivity.view(EditText.class, R.id.qs_start_edit).setEnabled(difficult.mayHaveDifferentTargetCount());
            wizardActivity.view(TextView.class,R.id.qs_start_text).setEnabled(difficult.mayHaveDifferentTargetCount());
            String programDescription = getProgramDescription(difficult);
            wizardActivity.view(TextView.class,R.id.qs_level_label_description).setText(programDescription);
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

        @Override
        public AlertDialog.Builder persistsSetup(final WizardActivity wizardActivity) {
            //TODO: add aware of what is going on
            final QuitSmokeDifficultLevel difficult = QuitSmokeDifficultLevel.levelByIndex(wizardActivity.view(SeekBar.class, R.id.qs_level_seekBar).getProgress());
            final QuitSmokeDifficultLevel oldDifficult = QuitSmokeDifficultLevel.levelByIndex(wizardActivity.getSetting(Settings.QUIT_PROGRAM_INDEX));

            Integer smokePerDay = null;
            if (difficult.mayHaveDifferentTargetCount()) {
                String startText = ((TextView) wizardActivity.findViewById(R.id.qs_start_edit)).getText().toString();
                if (startText.trim().length() == 0) {
                    Toast.makeText(wizardActivity, getString(R.string.quit_page_not_set_per_day), Toast.LENGTH_SHORT).show();
                    return wizardActivity.TOAST_WARNING;
                }

                smokePerDay = Integer.parseInt(wizardActivity
                        .view(TextView.class, R.id.qs_start_edit).getText().toString());
            }


            String text = wizardActivity.view(EditText.class,R.id.qs_end_edit).getText().toString();
            int desireSmokePerDayCount = 0;

            if (difficult.mayHaveDifferentTargetCount()) {
                if (text.trim().length() == 0) {
                    Toast.makeText(wizardActivity, getString(R.string.quit_page_not_set_desire_count), Toast.LENGTH_LONG).show();
                    return wizardActivity.TOAST_WARNING;
                }
                desireSmokePerDayCount = Integer.parseInt(text);
            }

            if (difficult.mayHaveDifferentTargetCount() && desireSmokePerDayCount > smokePerDay){
                Toast.makeText(wizardActivity, getString(R.string.quit_page_desire_more_then_per_day), Toast.LENGTH_LONG).show();
                return wizardActivity.TOAST_WARNING;
            }

            final Integer finalSmokePerDay = smokePerDay;
            final int finalDesireSmokePerDayCount = desireSmokePerDayCount;

            if (!wizardActivity.getSetting(Settings.IS_SMOKE_QUIT_ACTIVE) ||
                    (oldDifficult == difficult && !oldDifficult.mayHaveDifferentTargetCount())){
                doActualUpdate(wizardActivity, difficult, finalSmokePerDay, finalDesireSmokePerDayCount);
                return null;
            }

            return new AlertDialog.Builder(wizardActivity)
                    .setTitle(getString(R.string.quit_page_change_program_alert_title))
                    .setMessage(getString(R.string.quit_page_change_program_alert_content))
                    .setPositiveButton(getString(R.string.quit_page_change_program_alert_yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            doActualUpdate(wizardActivity, difficult, finalSmokePerDay, finalDesireSmokePerDayCount);
                            wizardActivity.performNext(false);
                        }
                    }).setNegativeButton(getString(R.string.quit_page_change_program_alert_no), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            wizardActivity.performNext(false);
                        }
                    });
        }

        private void doActualUpdate(WizardActivity wizardActivity, QuitSmokeDifficultLevel difficult, Integer smokePerDay, int desireSmokePerDayCount) {

            wizardActivity.setSetting(Settings.QUIT_PROGRAM_INDEX, difficult.toIndex());
            if (difficult == QuitSmokeDifficultLevel.DISABLED){
                wizardActivity.setSetting(Settings.IS_SMOKE_QUIT_ACTIVE, false);
                wizardActivity.setSetting(Settings.QUITE_START_SMOKE, null);
            } else {
                wizardActivity.setSetting(Settings.IS_SMOKE_QUIT_ACTIVE,true);
                wizardActivity.setSetting(Settings.QUITE_START_SMOKE, smokePerDay);
            }
            wizardActivity.setSetting(Settings.QUITE_END_SMOKE, desireSmokePerDayCount);
            wizardActivity.model().execute(SetupQuitSmokeProgram.class,new SetupQuitSmokeProgram.QuitSmokeProgramRequest(
               difficult,
               smokePerDay == null?-1:smokePerDay,
               desireSmokePerDayCount
            ));
        }
    }

    public class UIPageHandler extends SetupPageHandler {

        protected UIPageHandler() {
            super(getString(R.string.ui_page_title),
                  getString(R.string.ui_page_about),
                   R.layout.setup_page_ui_setting);
        }


        @Override
        public void onCreateUI(final WizardActivity wizardActivity) {
            wizardActivity.view(CheckBox.class,R.id.ui_sticky_notif_check).setChecked(
                    wizardActivity.application().settings().get(Settings.ENABLED_STICKY_NOTIFICATION));

            wizardActivity.view(CheckBox.class,R.id.ui_status_notif_check).setChecked(
                    wizardActivity.application().settings().get(Settings.ENABLED_STATISTIC_NOTIFICATION));

            wizardActivity.view(CheckBox.class,R.id.ui_assistant_naif_check).setChecked(
                    wizardActivity.application().settings().get(Settings.ENABLED_ASSISTANCE_NOTIFICATION));


            wizardActivity.view(CheckBox.class,R.id.ui_sticky_notif_check).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    wizardActivity.application().updateStickyNotification(isChecked);
                }
            });
            wizardActivity.view(CheckBox.class,R.id.ui_status_notif_check).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        wizardActivity.setSetting(Settings.ENABLED_STATISTIC_NOTIFICATION,isChecked);
                }
            });


            wizardActivity.view(CheckBox.class,R.id.ui_assistant_naif_check).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    wizardActivity.setSetting(Settings.ENABLED_ASSISTANCE_NOTIFICATION,isChecked);
                }
            });
        }

    }


}
