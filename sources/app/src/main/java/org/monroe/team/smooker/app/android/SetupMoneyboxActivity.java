package org.monroe.team.smooker.app.android;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.monroe.team.smooker.app.R;
import org.monroe.team.smooker.app.common.constant.Settings;
import org.monroe.team.smooker.app.common.quitsmoke.QuitSmokeDifficultLevel;


public class SetupMoneyboxActivity extends SetupGeneralActivity {


    @Override
    protected int setup_layout() {
        return R.layout.setup_page_moneybox;
    }

    @Override
    protected int caption_string()  {
        return R.string.moneybox;
    }

    @Override
    protected void action_start() {
    }

    @Override
    protected void action_apply() {

    }

    @Override
    protected void action_revert() {

    }

}
