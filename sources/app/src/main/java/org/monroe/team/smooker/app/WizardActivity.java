package org.monroe.team.smooker.app;

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

import org.monroe.team.smooker.app.common.Currency;
import org.monroe.team.smooker.app.common.Settings;
import org.monroe.team.smooker.app.common.SmokeQuitProgramDifficult;
import org.monroe.team.smooker.app.common.SupportActivity;
import org.monroe.team.smooker.app.common.SetupPage;

import java.util.List;


public class WizardActivity extends SupportActivity {

    private List<SetupPage> requestsStack;
    private int requestIndex=-1;
    private SetupPageHandler pageHandler;
    private boolean force =false;
    private boolean awareShown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                            "Please pass wizard or click one time more for exit.",
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

    public void performNext(View v){
        if (!pageHandler.persistsSetup(this)) return;
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


    public <Type> Type getSetting(Settings.SettingItem<Type> item){
        return application().settings().get(item);
    }

    public <Type> String getSettingAsString(Settings.SettingItem<Type> item){
        Type answer = application().settings().get(item);
        return (answer == null)? "": String.valueOf(answer);
    }

    public <Type> void setSetting(Settings.SettingItem<Type> item, Type value){
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

        public boolean persistsSetup(WizardActivity wizardActivity) {
            return true;
        }

        public void onCreateUI(WizardActivity wizardActivity){};
    }


    public static class WelcomePageHandler extends SetupPageHandler {

        protected WelcomePageHandler() {
            super("Welcome",
                   "Please follow setup wizard in order to configure application. Information " +
                           "will be used in future for calculation and etc.",
                   R.layout.setup_page_welcome
            );
        }

    }


    public static class GeneralSetupHandler extends SetupPageHandler {

        protected GeneralSetupHandler() {
            super("General",
                    "Please specify how much cost you spent per smoke and how much times you smoke usually in a day (If You have a " +
                            "doubt You could choose 'detect automatically option', so application will " +
                            "follow you few days and provide average count)",
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
        public boolean persistsSetup(WizardActivity wizardActivity) {
            String text = ((TextView)wizardActivity.findViewById(R.id.gs_price_edit)).getText().toString();
            if (text.trim().length() == 0){
                Toast.makeText(wizardActivity,"Please specify smoke break cost'",Toast.LENGTH_SHORT).show();
                return false;
            }

            float smokeCost = Math.abs(Float.parseFloat(wizardActivity
                    .view(TextView.class, R.id.gs_price_edit).getText().toString()));

            Spinner spinner = wizardActivity.view(Spinner.class, R.id.gs_cur_spinner);

            wizardActivity.setSetting(Settings.SMOKE_PRICE,smokeCost);
            wizardActivity.setSetting(Settings.CURRENCY_ID, ((Currency) spinner.getSelectedItem()).id);

            return true;
        }
    }


    public static class QuitSmokingSetupHandler extends SetupPageHandler {

        protected QuitSmokingSetupHandler() {
            super("Quit Smoking",
                  "Please choose quit smoking program loyalty and final target. Please note that you " +
                  "would be able to change settings latter using setting menu",
                   R.layout.setup_page_quit_smoking
            );
        }

        @Override
        public void onCreateUI(final WizardActivity wizardActivity) {
            wizardActivity.view(EditText.class,R.id.qs_start_edit).setText(wizardActivity.getSettingAsString(Settings.QUITE_START_SMOKE));
            wizardActivity.view(EditText.class,R.id.qs_end_edit).setText(wizardActivity.getSettingAsString(Settings.QUITE_END_SMOKE));
            wizardActivity.view(SeekBar.class,R.id.qs_level_seekBar).setMax(SmokeQuitProgramDifficult.difficultCount()-1);
            wizardActivity.view(SeekBar.class,R.id.qs_level_seekBar).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    int difficultIndex = progress;
                    SmokeQuitProgramDifficult difficult = SmokeQuitProgramDifficult.levelByIndex(difficultIndex);
                    updateUIByDifficultLevel(difficult,wizardActivity);
                }

                @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                @Override public void onStopTrackingTouch(SeekBar seekBar) {}
            });
            wizardActivity.view(SeekBar.class,R.id.qs_level_seekBar).setProgress(wizardActivity.getSetting(Settings.QUIT_PROGRAM_INDEX));
            updateUIByDifficultLevel(SmokeQuitProgramDifficult.levelByIndex(wizardActivity.getSetting(Settings.QUIT_PROGRAM_INDEX)),
                    wizardActivity);
        }

        private void updateUIByDifficultLevel(SmokeQuitProgramDifficult difficult, WizardActivity wizardActivity) {
            wizardActivity.view(EditText.class, R.id.qs_end_edit).setEnabled(difficult.mayHaveDifferentTargetCount());
            wizardActivity.view(TextView.class,R.id.qs_end_text).setEnabled(difficult.mayHaveDifferentTargetCount());
            wizardActivity.view(EditText.class, R.id.qs_start_edit).setEnabled(difficult.mayHaveDifferentTargetCount());
            wizardActivity.view(TextView.class,R.id.qs_start_text).setEnabled(difficult.mayHaveDifferentTargetCount());
            String programDescription = getProgramDescription(difficult);
            wizardActivity.view(TextView.class,R.id.qs_level_label_description).setText(programDescription);
        }

        private String getProgramDescription(SmokeQuitProgramDifficult difficult) {
            switch (difficult){
                case DISABLED: return "Disabled - for people who want to get smoke statistic only without quiting smoking";
                case LOWEST: return "Lowest - most loyal program forcing you to pass up one smoke break per month";
                case LOW: return "Low - quite loyal program forcing you to pass up one smoke break per week";
                case SMART: return "Smart - clever program forcing you to pass up one smoke break first day, second in next two day, third in next three days and so on";
                case SMARTEST: return "Smartest - most clever program forcing you to pass up one smoke break per day at the beginning and ending with one break per month.";
                case HARD: return "Hard - program which forcing you to pass up one smoke break per day, unless you quit.";
                case HARDEST: return "Hardest - program which forcing you to quite today and for forever.";
            }
            throw new IllegalStateException();
        }

        @Override
        public boolean persistsSetup(WizardActivity wizardActivity) {

            SmokeQuitProgramDifficult difficult = SmokeQuitProgramDifficult.levelByIndex(wizardActivity.view(SeekBar.class,R.id.qs_level_seekBar).getProgress());
            Integer smokePerDay = null;
            if (difficult.mayHaveDifferentTargetCount()) {
                String startText = ((TextView) wizardActivity.findViewById(R.id.qs_start_edit)).getText().toString();
                if (startText.trim().length() == 0) {
                    Toast.makeText(wizardActivity, "Please specify your current smokes per day", Toast.LENGTH_SHORT).show();
                    return false;
                }

                smokePerDay = Integer.parseInt(wizardActivity
                        .view(TextView.class, R.id.qs_start_edit).getText().toString());
            }

            String text = wizardActivity.view(EditText.class,R.id.qs_end_edit).getText().toString();
            int desireSmokePerDayCount = 0;

            if (difficult.mayHaveDifferentTargetCount()) {
                if (text.trim().length() == 0) {
                    Toast.makeText(wizardActivity, "Please specify desire smoke per day. In case you want quit completely put zero", Toast.LENGTH_LONG).show();
                    return false;
                }
                desireSmokePerDayCount = Integer.parseInt(text);
            }

            wizardActivity.setSetting(Settings.QUITE_START_SMOKE, smokePerDay);
            wizardActivity.setSetting(Settings.QUITE_END_SMOKE, desireSmokePerDayCount);
            wizardActivity.setSetting(Settings.QUIT_PROGRAM_INDEX, difficult.toIndex());

            return true;
        }
    }

    public static class UIPageHandler extends SetupPageHandler {

        protected UIPageHandler() {
            super("UI Settings",
                    "Please specify which of user interface extension you would like to use",
                    R.layout.setup_page_ui_setting);
        }


        @Override
        public void onCreateUI(final WizardActivity wizardActivity) {
            wizardActivity.view(CheckBox.class,R.id.ui_sticky_notif_check).setChecked(
                    wizardActivity.application().settings().get(Settings.ENABLED_STICKY_NOTIFICATION));
            wizardActivity.view(CheckBox.class,R.id.ui_sticky_notif_check).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    wizardActivity.application().updateStickyNotification(isChecked);
                }
            });
        }

    }


}
