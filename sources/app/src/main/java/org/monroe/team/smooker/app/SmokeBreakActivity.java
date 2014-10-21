package org.monroe.team.smooker.app;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import org.monroe.team.smooker.app.actors.ActorSmoker;
import org.monroe.team.smooker.app.common.SupportActivity;


public class SmokeBreakActivity extends SupportActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //in case no action
        application().getSuggestionsController().scheduleFallback();

        setContentView(R.layout.activity_smoke_break);
        View.OnTouchListener listener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN){
                    v.setBackgroundColor(getResources().getColor(R.color.choice_selection));
                }

                if (event.getAction() == MotionEvent.ACTION_UP){
                    v.setBackgroundColor(Color.TRANSPARENT);
                    onOption(v.getId());
                }

                if (event.getAction() == MotionEvent.ACTION_CANCEL){
                    v.setBackgroundColor(Color.TRANSPARENT);
                }
                v.invalidate();
                return true;
            }
        };

        view(R.id.sb_option_smoke_layout).setOnTouchListener(listener);
        view(R.id.sb_option_skip_layout).setOnTouchListener(listener);
        view(R.id.sb_option_postpone_layout).setOnTouchListener(listener);
        view(R.id.sb_option_smoke_layout).setOnTouchListener(listener);
    }

    public void performBackButton(View view){
        finish();
    }

    private void onOption(int id) {
        Intent intent = null;
        switch (id){
            case R.id.sb_option_postpone_layout:{
                intent = ActorSmoker.create(this, ActorSmoker.POSTPONE_SMOKE).toast().buildIntent();
                break;
            }
            case R.id.sb_option_skip_layout:{
                intent = ActorSmoker.create(this, ActorSmoker.SKIP_SMOKE).toast().buildIntent();
                break;
            }
            case R.id.sb_option_smoke_layout:{
                intent = ActorSmoker.create(this, ActorSmoker.ADD_SMOKE).toast().buildIntent();
                break;
            }
        }
       sendBroadcast(intent);
       finish();
    }

}
