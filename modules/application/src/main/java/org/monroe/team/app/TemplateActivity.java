package org.monroe.team.app;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

/**
 * User: MisterJBee
 * Date: 9/21/13 Time: 8:08 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class TemplateActivity extends FragmentActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
    }
}
