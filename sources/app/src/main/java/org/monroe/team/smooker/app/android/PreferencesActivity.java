package org.monroe.team.smooker.app.android;

import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

import org.monroe.team.android.box.app.ActivitySupport;
import org.monroe.team.smooker.app.R;
import org.monroe.team.smooker.app.common.constant.Settings;

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
    }



    @Override
    protected void onResume() {
        super.onResume();
        view(R.id.sticky_notification_switch, Switch.class).setChecked(application().isStickyNotificationEnabled());
        view(R.id.assistant_notification_switch, Switch.class).setChecked(application().isAssistantNotificationEnabled());
    }
}
