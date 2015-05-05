package org.monroe.team.smooker.app.android;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import org.monroe.team.android.box.app.ActivitySupport;
import org.monroe.team.smooker.app.R;
import org.monroe.team.smooker.app.uc.common.ActionDetails;

import java.text.DateFormat;

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
                        .setTitle(getString(R.string.options_alert_data_deletion_caption))
                        .setMessage(getString(R.string.options_alert_data_today_delete_about))
                        .setPositiveButton(getString(R.string.options_alert_data_today_delete_yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                removeTodayData(true);
                            }

                        }).setNegativeButton(getString(R.string.general_no), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).show();
            }
        });

        view(R.id.remove_all_data).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog alertDialog = new AlertDialog.Builder(PreferencesActivity.this)
                        .setTitle(getString(R.string.options_alert_data_deletion_caption))
                        .setMessage(getString(R.string.options_alert_data_all_text))
                        .setPositiveButton(getString(R.string.options_alert_data_all_yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                removeTodayData(false);
                            }

                        }).setNegativeButton(getString(R.string.general_no), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).show();
            }
        });

        view(R.id.remove_last_action).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view(R.id.remove_last_action).setVisibility(View.INVISIBLE);
                application().getLastLoggedAction(new SmookerApplication.Observer<ActionDetails>(){
                    @Override
                    public void onSuccess(final ActionDetails action) {
                        view(R.id.remove_last_action).setVisibility(View.VISIBLE);
                        if (action == null){
                            Toast.makeText(application(),getString(R.string.options_data_deletion_no_action_toast),Toast.LENGTH_LONG).show();
                        }else{
                            String detailsString = "Unknown";
                            switch (action.type){
                                case SMOKE_BREAK:
                                    detailsString = getString(R.string.options_data_deletion_action_smoke_added);
                                    break;
                                case SMOKE_CANCEL_POSTPONED:
                                    detailsString = getString(R.string.options_data_deletion_action_smoke_posponed);
                                    break;
                                case SMOKE_CANCEL_SKIP:
                                    detailsString = getString(R.string.options_data_deletion_action_smoke_skipped);
                                    break;

                            }

                            AlertDialog alertDialog = new AlertDialog.Builder(PreferencesActivity.this)
                                    .setTitle(getString(R.string.options_alert_data_deletion_caption))
                                    .setMessage(getString(R.string.options_alert_data_remove_action_before_text)+" "+detailsString+" "
                                            +getString(R.string.options_alert_data_remove_action_after_text)+" "+DateFormat.getDateTimeInstance().format(action.date)+" "+
                                            getString(R.string.options_alert_data_remove_action_last_text))
                                    .setPositiveButton(getString(R.string.options_alert_data_remove_action_yes), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            removeAction(action);
                                        }

                                    }).setNegativeButton(getString(R.string.general_no), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    }).show();

                        }
                    }

                });
            }
        });

        view(R.id.community_link).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://plus.google.com/communities/102324574498196765190";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });

        view(R.id.rate_link).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://play.google.com/store/apps/details?id=org.monroe.team.smooker.app";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });

        view(R.id.mail_link).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto","monroe.dev.team@gmail.com", null));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Smooker: Ask or Suggest");
                emailIntent.putExtra(Intent.EXTRA_TEXT   , "Hi, I got a question (or suggestion).");
                try {
                    startActivity(Intent.createChooser(emailIntent, "Send email..."));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(PreferencesActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void removeAction(ActionDetails action) {
        application().removeLoggedAction(action, new SmookerApplication.Observer<Void>(){
            @Override
            public void onSuccess(Void value) {
                successfullyRemovedToast();
            }
        });
    }

    private void removeTodayData(boolean todayOnly) {
        application().removeData(todayOnly, new SmookerApplication.Observer<Void>() {
            @Override
            public void onSuccess(Void value) {
                successfullyRemovedToast();
            }
        });
    }

    private void successfullyRemovedToast() {
        Toast.makeText(this, getString(R.string.options_data_removed_toast), Toast.LENGTH_SHORT).show();
    }


    @Override
    protected void onResume() {
        super.onResume();
        view(R.id.sticky_notification_switch, Switch.class).setChecked(application().isStickyNotificationEnabled());
        view(R.id.assistant_notification_switch, Switch.class).setChecked(application().isAssistantNotificationEnabled());
    }
}
