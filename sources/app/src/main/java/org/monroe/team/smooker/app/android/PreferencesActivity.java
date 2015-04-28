package org.monroe.team.smooker.app.android;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import org.monroe.team.android.box.app.ActivitySupport;
import org.monroe.team.android.box.app.ApplicationSupport;
import org.monroe.team.smooker.app.R;

public class PreferencesActivity extends ActivitySupport<SmookerApplication>{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);
        view(R.id.setup_quit_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        view(R.id.sticky_notification_switch, Switch.class).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                application().updateStickyNotification(isChecked);
            }
        });

        view(R.id.assistant_notification_switch, Switch.class).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                application().enableAssistantNotifications(isChecked);
            }
        });
        view(R.id.remove_today_data).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog alertDialog = new AlertDialog.Builder(PreferencesActivity.this)
                        .setTitle("Data Deletion")
                        .setMessage("You are going to delete today smoke details. All data about added, skipped and postponed smoke breaks will be removed. " +
                                "Are you sure want to continue?")
                        .setPositiveButton("Yes, remove all data", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                removeTodayData();
                            }

                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).show();
            }
        });
    }

    private void removeTodayData() {
        application().removeData(true, new ApplicationSupport.ValueObserver<Void>() {
            @Override
            public void onSuccess(Void value) {
                successfullyRemovedToast();
            }

            @Override
            public void onFail(int errorCode) {
               forceCloseWithErrorCode(2);
            }
        });
    }

    private void successfullyRemovedToast() {
        Toast.makeText(this, "Data removed successfully!", Toast.LENGTH_SHORT).show();
    }


    @Override
    protected void onResume() {
        super.onResume();
        view(R.id.sticky_notification_switch, Switch.class).setChecked(application().isStickyNotificationEnabled());
        view(R.id.assistant_notification_switch, Switch.class).setChecked(application().isAssistantNotificationEnabled());
    }
}
