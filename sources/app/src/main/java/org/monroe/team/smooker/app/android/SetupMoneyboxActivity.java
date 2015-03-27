package org.monroe.team.smooker.app.android;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.monroe.team.smooker.app.R;
import org.monroe.team.smooker.app.common.constant.Currency;
import org.monroe.team.smooker.app.common.constant.Settings;
import org.monroe.team.smooker.app.common.quitsmoke.QuitSmokeDifficultLevel;


public class SetupMoneyboxActivity extends SetupGeneralActivity {


    private ArrayAdapter<Currency> adapter;

    @Override
    protected int setup_layout() {
        return R.layout.setup_page_moneybox;
    }

    @Override
    protected int caption_string()  {
        return R.string.moneybox;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Create an ArrayAdapter using the string array and a default spinner layout
        adapter = new ArrayAdapter<Currency>(this, android.R.layout.simple_spinner_item,Currency.SUPPORTED_CURRENCIES){
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
    }

    @Override
    protected void action_start() {
        final Spinner spinner = view(R.id.moneybox_cur_spinner,Spinner.class);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        spinner.setSelection(Currency.supportedArrayIndex(
                Currency.byId(application().getSetting(Settings.CURRENCY_ID))));
        view_text(R.id.moneybox_price_edit)
                .setText(application().getSettingAsString(Settings.SMOKE_PRICE));
        view_text(R.id.moneybox_start_smoke_edit)
                .setText(application().getSettingAsString(Settings.MONEYBOX_START_SMOKE));
        view_text(R.id.moneybox_price_something_edit)
                .setText(application().getSettingAsString(Settings.MONEYBOX_SOMETHING_PRICE));
        view_text(R.id.moneybox_title_edit)
                .setText(application().getSettingAsString(Settings.MONEYBOX_SOMETHING_TITLE));
        view_text(R.id.moneybox_description_edit)
                .setText(application().getSettingAsString(Settings.MONEYBOX_SOMETHING_DESCRIPTION));

    }

    @Override
    protected void action_apply() {
        Integer averageSmokeCount = getInt(view_text(R.id.moneybox_start_smoke_edit));
        if (averageSmokeCount == null || averageSmokeCount < 1){
            Toast.makeText(this, "Please specify valid 'smoke breaks per day'",Toast.LENGTH_LONG).show();
            return;
        }

        Float smokePrice = getFloat(view_text(R.id.moneybox_price_edit));
        if (smokePrice == null || smokePrice < 0){
            Toast.makeText(this, "Please specify valid 'price per smoke'",Toast.LENGTH_LONG).show();
            return;
        }

        Float thingPrice = getFloat(view_text(R.id.moneybox_price_something_edit));
        if (thingPrice == null || thingPrice < 0){
            Toast.makeText(this, "Please specify valid 'Price'",Toast.LENGTH_LONG).show();
            return;
        }

        String title = view_text(R.id.moneybox_title_edit).getText().toString();
        if (title.length() == 0 ){
            Toast.makeText(this, "Please specify 'Title'",Toast.LENGTH_LONG).show();
            return;
        }

        if (smokePrice > thingPrice){
            Toast.makeText(this, "Please specify  valid 'Price'",Toast.LENGTH_LONG).show();
            return;
        }

        String description = view_text(R.id.moneybox_description_edit).getText().toString();

        application().setSetting(Settings.SMOKE_PRICE, smokePrice);
        application().setSetting(Settings.MONEYBOX_START_SMOKE, averageSmokeCount);
        application().setSetting(Settings.MONEYBOX_SOMETHING_PRICE, thingPrice);
        application().setSetting(Settings.MONEYBOX_SOMETHING_TITLE, title);
        application().setSetting(Settings.MONEYBOX_SOMETHING_DESCRIPTION, description);
        application().setSetting(Settings.CURRENCY_ID, ((Currency) view(R.id.moneybox_cur_spinner, Spinner.class).getSelectedItem()).id);

        finish();
    }

    private Integer getInt(TextView textView) {
        String text = textView.getText().toString();
        if (text.length() == 0) return null;
        return Integer.parseInt(text);
    }

    private Float getFloat(TextView textView) {
        String text = textView.getText().toString();
        if (text.length() == 0) return null;
        return Float.parseFloat(text);
    }


    @Override
    protected void action_revert() {
        action_start();
    }

}
