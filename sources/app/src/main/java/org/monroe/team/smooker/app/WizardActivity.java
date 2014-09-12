package org.monroe.team.smooker.app;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.monroe.team.smooker.app.common.Currency;
import org.monroe.team.smooker.app.common.SmokeQuitProgramDifficult;
import org.monroe.team.smooker.app.common.SupportActivity;
import org.monroe.team.smooker.app.common.SetupPage;
import org.monroe.team.smooker.app.uc.GetGeneralDetails;
import org.monroe.team.smooker.app.uc.UpdateGeneralDetails;

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
            case SMOKE_PER_DAYS: pageHandler = new SmokePerDayPageHandler();break;
            case QUIT_SMOKING: pageHandler = new QuitSmokingPageHandler();break;
            default: throw new IllegalStateException("Unsupported page "+ curPage);
        }
    }

    @Override
    public void onBackPressed() {
        performBack(null);
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
                   R.layout.welcome_setup_page);
        }
    }


    public static class SmokePerDayPageHandler extends SetupPageHandler {

        protected SmokePerDayPageHandler() {
            super("General",
                    "Please specify how much cost you spent per smoke and how much times you smoke usually in a day (If You have a " +
                            "doubt You could choose 'detect automatically option', so application will " +
                            "follow you few days and provide average count)",
                    R.layout.smoke_per_day_page);
        }

        @Override
        public void onCreateUI(final WizardActivity wizardActivity) {

            ((CheckBox) wizardActivity.findViewById(R.id.spd_auto_detect_check)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    wizardActivity.findViewById(R.id.spd_times_text).setEnabled(!isChecked);
                    wizardActivity.findViewById(R.id.spd_label).setEnabled(!isChecked);
                }
            });

            final Spinner spinner = wizardActivity.view(Spinner.class, R.id.spd_cur_spinner);

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


            final GetGeneralDetails.GeneralDetailsResponse generalDetails =
                    wizardActivity.model().execute(GetGeneralDetails.class, null);

            spinner.setSelection(Currency.supportedArrayIndex(generalDetails.currency));
            wizardActivity.view(EditText.class,R.id.spd_cost_edit).setText(Float.toString(generalDetails.costPerSmoke));

            if (generalDetails.isSmokingPerDay(
                    GetGeneralDetails.GeneralDetailsResponse.SMOKE_PER_DAY_UNDER_DETECT)) {
                ((CheckBox) wizardActivity.findViewById(R.id.spd_auto_detect_check)).setChecked(true);
            } else if (!generalDetails.isSmokingPerDay(
                    GetGeneralDetails.GeneralDetailsResponse.SMOKE_PER_DAY_UNDEFINED)) {
                wizardActivity.view(EditText.class,R.id.spd_times_text).setText(Integer.toString(generalDetails.smokePerDay));
            }
            wizardActivity.view(CheckBox.class,R.id.spd_recalculate_check).setVisibility(
                    generalDetails.hasFinancialHistory?View.VISIBLE:View.INVISIBLE);
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (!generalDetails.hasFinancialHistory) return;
                    Currency newCurrency = (Currency) spinner.getSelectedItem();
                    Currency wasCurrency = generalDetails.currency;
                    wizardActivity.view(CheckBox.class,R.id.spd_recalculate_check).setChecked(newCurrency!=wasCurrency);
                    wizardActivity.view(CheckBox.class,R.id.spd_recalculate_check).setEnabled(newCurrency == wasCurrency);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        }

        @Override
        public boolean persistsSetup(WizardActivity wizardActivity) {
            boolean autoDetect = ((CheckBox)wizardActivity.findViewById(R.id.spd_auto_detect_check)).isChecked();
            if (!autoDetect){
               String text = ((TextView)wizardActivity.findViewById(R.id.spd_times_text)).getText().toString();
               if (text.length() == 0){
                   Toast.makeText(wizardActivity,"Please specify times per day or check 'detect automatically'",Toast.LENGTH_SHORT).show();
                   return false;
               }
            }

            String text = ((TextView)wizardActivity.findViewById(R.id.spd_cost_edit)).getText().toString();
            if (text.isEmpty()){
                Toast.makeText(wizardActivity,"Please specify smoke break cost'",Toast.LENGTH_SHORT).show();
                return false;
            }

            updateDetails(wizardActivity, autoDetect);
            return true;
        }

        private void updateDetails(WizardActivity wizardActivity, boolean autoDetect) {
            int smokePerDay = 0;
            if (autoDetect){
                smokePerDay = GetGeneralDetails.GeneralDetailsResponse.SMOKE_PER_DAY_UNDER_DETECT;
            } else {
                smokePerDay = Integer.parseInt(wizardActivity
                        .view(TextView.class, R.id.spd_times_text).getText().toString());
            }

            float smokeCost = Math.abs(Float.parseFloat(wizardActivity
                    .view(TextView.class, R.id.spd_cost_edit).getText().toString()));

            Spinner spinner = wizardActivity.view(Spinner.class, R.id.spd_cur_spinner);

            wizardActivity.model().execute(UpdateGeneralDetails.class,
                    new UpdateGeneralDetails.DetailsUpdateRequest()
                            .withSmokePerDay(smokePerDay)
                            .withCostPerSmoke(smokeCost)
                            .withCurrency((Currency)spinner.getSelectedItem())
                            .withFinancialHistoryRecalculateRequest(wizardActivity.view(CheckBox.class, R.id.spd_recalculate_check).isChecked()));
        }
    }


    public static class QuitSmokingPageHandler extends SetupPageHandler {

        protected QuitSmokingPageHandler() {
            super("Quit Smoking",
                  "Please choose quit smoking program loyalty and final target. Please note that you " +
                  "would be able to change settings latter using setting menu",
                   R.layout.quit_smoking_setup_page);
        }

        @Override
        public void onCreateUI(final WizardActivity wizardActivity) {
            GetGeneralDetails.GeneralDetailsResponse generalDetails =
                    wizardActivity.model().execute(GetGeneralDetails.class, null);
            wizardActivity.view(EditText.class,R.id.qs_times_text).setText(String.valueOf(generalDetails.desireSmokePerDay));
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
            wizardActivity.view(SeekBar.class,R.id.qs_level_seekBar).setProgress(generalDetails.difficultLevel.toIndex());
            updateUIByDifficultLevel(generalDetails.difficultLevel,wizardActivity);
        }

        private void updateUIByDifficultLevel(SmokeQuitProgramDifficult difficult, WizardActivity wizardActivity) {
            wizardActivity.view(EditText.class, R.id.qs_times_text).setEnabled(difficult.mayHaveDifferentTargetCount());
            wizardActivity.view(TextView.class,R.id.qs_label).setEnabled(difficult.mayHaveDifferentTargetCount());
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
            String text = wizardActivity.view(EditText.class,R.id.qs_times_text).getText().toString();
            int desireSmokePerDayCount = 0;

            if (difficult.mayHaveDifferentTargetCount()) {
                if (text.isEmpty()) {
                    Toast.makeText(wizardActivity, "Please specify desire smoke per day. In case you want quit completely put zero", Toast.LENGTH_LONG).show();
                    return false;
                }
                desireSmokePerDayCount = Integer.parseInt(text);
            }

            wizardActivity.model().execute(UpdateGeneralDetails.class,
                    new UpdateGeneralDetails.DetailsUpdateRequest()
                            .withDesireSmokePerDay(desireSmokePerDayCount)
                            .withQuitDifficultLevel(difficult));

            return true;
        }
    }

}
