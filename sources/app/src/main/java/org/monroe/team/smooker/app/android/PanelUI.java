package org.monroe.team.smooker.app.android;


import android.view.View;
import android.widget.TextView;

import org.monroe.team.smooker.app.R;

final public class PanelUI {
    private PanelUI() {}

    public static void initLightPanel(View panel, String caption,
                                      String description, String action,
                                      View.OnClickListener onClickListener) {
        ((TextView)panel.findViewById(R.id.panel_caption)).setText(caption);
        ((TextView)panel.findViewById(R.id.panel_description)).setText(description);
        ((TextView)panel.findViewById(R.id.panel_action)).setText(action);
        ((TextView)panel.findViewById(R.id.panel_action)).setOnClickListener(onClickListener);
    }
}
