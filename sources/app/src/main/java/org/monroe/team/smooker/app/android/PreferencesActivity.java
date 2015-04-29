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
import org.monroe.team.android.box.app.ApplicationSupport;
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
                        .setTitle("Data Deletion")
                        .setMessage("You are going to delete today smoke details. All data about added, skipped and postponed smoke breaks will be removed. " +
                                "Are you sure want to continue?")
                        .setPositiveButton("Yes, remove today data", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                removeTodayData(true);
                            }

                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
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
                        .setTitle("Data Deletion")
                        .setMessage("You are going to delete all collected data. All data including added, skipped and postponed smoke breaks, your smoke quit progress and moneybox progress will be removed. " +
                                "Are you sure want to continue?")
                        .setPositiveButton("Yes, remove all data", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                removeTodayData(false);
                            }

                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
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
                application().getLastLoggedAction(new ApplicationSupport.ValueObserver<ActionDetails>(){
                    @Override
                    public void onSuccess(final ActionDetails action) {
                        view(R.id.remove_last_action).setVisibility(View.VISIBLE);
                        if (action == null){
                            Toast.makeText(application(),"No action was done today! Nothing to delete.",Toast.LENGTH_LONG).show();
                        }else{
                            String detailsString = "Unknown";
                            switch (action.type){
                                case SMOKE_BREAK:
                                    detailsString = "Smoke Added";
                                    break;
                                case SMOKE_CANCEL_POSTPONED:
                                    detailsString = "Smoke Postponed";
                                    break;
                                case SMOKE_CANCEL_SKIP:
                                    detailsString = "Smoke Skipped";
                                    break;

                            }

                            AlertDialog alertDialog = new AlertDialog.Builder(PreferencesActivity.this)
                                    .setTitle("Data Deletion")
                                    .setMessage("You are going to delete last action '"+detailsString+"' which was done " + DateFormat.getDateTimeInstance().format(action.date)+
                                            " Are you sure want to continue?")
                                    .setPositiveButton("Yes, remove it", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            removeAction(action);
                                        }

                                    }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    }).show();

                        }
                    }

                    @Override
                    public void onFail(int errorCode) {
                        view(R.id.remove_last_action).setVisibility(View.VISIBLE);
                        forceCloseWithErrorCode(2034);
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
        application().removeLoggedAction(action, new ApplicationSupport.ValueObserver<Void>(){
            @Override
            public void onSuccess(Void value) {
                successfullyRemovedToast();
            }
            @Override
            public void onFail(int errorCode) {
                forceCloseWithErrorCode(205);
            }
        });
    }

    private void removeTodayData(boolean todayOnly) {
        application().removeData(todayOnly, new ApplicationSupport.ValueObserver<Void>() {
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
