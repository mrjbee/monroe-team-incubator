package org.monroe.team.smooker.app.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.monroe.team.corebox.utils.Closure;
import org.monroe.team.corebox.utils.DateUtils;
import org.monroe.team.smooker.app.R;
import org.monroe.team.smooker.app.common.constant.Currency;
import org.monroe.team.smooker.app.common.constant.Settings;

import java.io.FileNotFoundException;
import java.io.InputStream;


public class SetupMoneyboxActivity extends SetupGeneralActivity {


    private static final int PICK_IMAGE = 230;
    private ArrayAdapter<Currency> adapter;
    private String newImageId;


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

    private void performImageSelection() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra("return-data", false);
        try {
            startActivityForResult(Intent.createChooser(intent, "Image Picker"), PICK_IMAGE);
        } catch (ActivityNotFoundException e) {
            forceCloseWithErrorCode(808);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == PICK_IMAGE && data != null && resultCode == Activity.RESULT_OK) {
            Uri _uri = data.getData();
            if (_uri == null) return;
            try {
                showImageLoadingProgress();
                InputStream is = getContentResolver().openInputStream(_uri);
                application().saveImage(is, new SmookerApplication.OnSaveImageObserver() {
                    @Override
                    public void onResult(String imageId) {
                        loadImage(imageId, new Closure<String, Void>() {
                            @Override
                            public Void execute(String arg) {
                                newImageId = arg;
                                return null;
                            }
                        });
                    }

                    @Override
                    public void onFail() {
                        hideImageLoadingProgress();
                        Toast.makeText(application(),
                                "Error during loading image. Please try again", Toast.LENGTH_LONG).show();
                    }
                });
            } catch (FileNotFoundException e) {
                Toast.makeText(this,"Image not found. Please try again", Toast.LENGTH_LONG).show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void hideImageLoadingProgress() {
        view(R.id.moneybox_image_progress).setVisibility(View.GONE);
        view(R.id.moneybox_image_change_panel).setVisibility(View.VISIBLE);
    }

    private void showImageLoadingProgress() {
        view(R.id.moneybox_image_progress).setVisibility(View.VISIBLE);
        view(R.id.moneybox_image_change_panel).setVisibility(View.INVISIBLE);
    }

    private void loadImage(final String imageId, final Closure<String,Void> processImageId) {
        showImageLoadingProgress();
        if (view(R.id.moneybox_image).getHeight() == 0){
            runLastOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loadImage(imageId, processImageId);
                }
            }, 200);
            return;
        }
        application().loadToBitmap(imageId, view(R.id.moneybox_image).getHeight(),
                view(R.id.moneybox_image).getWidth(),new SmookerApplication.OnImageLoadedObserver() {
            @Override
            public void onResult(String imageId, Bitmap bitmap) {
                   hideImageLoadingProgress();
                   view(R.id.moneybox_image, ImageView.class).setImageBitmap(bitmap);
                   view(R.id.moneybox_image, ImageView.class).invalidate();
                   processImageId.execute(imageId);
            }

            @Override
            public void onFail() {
                hideImageLoadingProgress();
                Toast.makeText(application(),
                        "Error during loading image. Please try again", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void action_start() {
        view(R.id.moneybox_image_select_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performImageSelection();
            }
        });
        view(R.id.moneybox_disable_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show dialog
                AlertDialog alertDialog = new AlertDialog.Builder(SetupMoneyboxActivity.this)
                        .setTitle("Disable Moneybox")
                        .setMessage("You are going to disable Moneybox. All your progress and saved money information will be erased. Are you sure want to continue?")
                        .setPositiveButton("Yes, disable moneybox", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                              disable();
                            }


                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).show();
            }
        });

        view(R.id.moneybox_clear_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show dialog
                AlertDialog alertDialog = new AlertDialog.Builder(SetupMoneyboxActivity.this)
                        .setTitle("Clear Moneybox")
                        .setMessage("You are going to clear you current progress and all data about saved money." +
                                "Are you sure want to continue?")
                        .setPositiveButton("Yes, clear progress", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                clearProgress();
                            }


                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).show();
            }
        });

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

        String imageId = application().getSettingAsString(Settings.MONEYBOX_SOMETHING_IMAGE_ID);
        if (imageId != null && !imageId.trim().equals("")){
            loadImage(imageId,new Closure<String, Void>() {
                @Override
                public Void execute(String arg) {
                    return null;
                }
            });
        } else {
            view(R.id.moneybox_image, ImageView.class).setImageBitmap(null);
        }

        if(newImageId != null){
            application().deleteImage(newImageId);
            newImageId = null;
        }
    }

    private void clearProgress() {
        application().setSetting(Settings.MONEYBOX_START_DATE, DateUtils.today().getTime());
        application().changeMoneyBoxTarget();
        SetupMoneyboxActivity.this.finish();
    }

    private void disable() {
        if (newImageId!=null){
            application().deleteImage(newImageId);
        }
        String imageId = application().getSetting(Settings.MONEYBOX_SOMETHING_IMAGE_ID);
        if (imageId != null){
            application().deleteImage(imageId);
        }

        application().setSetting(Settings.MONEYBOX_START_DATE, null);
        application().setSetting(Settings.MONEYBOX_SOMETHING_IMAGE_ID, null);
        application().setSetting(Settings.MONEYBOX_SOMETHING_PRICE, null);
        application().setSetting(Settings.MONEYBOX_SOMETHING_TITLE, null);
        application().setSetting(Settings.MONEYBOX_SOMETHING_IMAGE_ID, null);
        application().setSetting(Settings.MONEYBOX_SOMETHING_DESCRIPTION, null);
        application().setSetting(Settings.MONEYBOX_START_SMOKE, null);

        application().changeMoneyBoxTargetDescription();
        application().changeMoneyBoxTarget();
        SetupMoneyboxActivity.this.finish();
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
            Toast.makeText(this, "Please specify valid 'Price'",Toast.LENGTH_LONG).show();
            return;
        }

        String imageId = newImageId;
        if (imageId == null){
            imageId = application().getSettingAsString(Settings.MONEYBOX_SOMETHING_IMAGE_ID);
        }

        if (imageId == null ||imageId.trim().equals("")){
            Toast.makeText(this, "Please choose and Image",Toast.LENGTH_LONG).show();
            return;
        }

        String description = view_text(R.id.moneybox_description_edit).getText().toString();

        application().setSetting(Settings.SMOKE_PRICE, smokePrice);
        application().setSetting(Settings.MONEYBOX_START_SMOKE, averageSmokeCount);
        application().setSetting(Settings.MONEYBOX_SOMETHING_PRICE, thingPrice);
        application().setSetting(Settings.MONEYBOX_SOMETHING_TITLE, title);
        application().setSetting(Settings.MONEYBOX_SOMETHING_IMAGE_ID, imageId);
        application().setSetting(Settings.MONEYBOX_SOMETHING_DESCRIPTION, description);
        application().setSetting(Settings.CURRENCY_ID, ((Currency) view(R.id.moneybox_cur_spinner, Spinner.class).getSelectedItem()).id);
        Long startDate = application().getSetting(Settings.MONEYBOX_START_DATE);
        if (startDate == null){
            startDate = DateUtils.today().getTime();
            application().setSetting(Settings.MONEYBOX_START_DATE, startDate);
        }

        application().changeMoneyBoxTargetDescription();
        application().changeMoneyBoxTarget();
        onBackPressed();
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
