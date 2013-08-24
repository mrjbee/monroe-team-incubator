package org.monroe.team.aas.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import org.monroe.team.aas.R;
import org.monroe.team.aas.ui.components.ApplicationDetailsComponent;

/**
 * User: MisterJBee
 * Date: 8/24/13 Time: 4:33 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class ApplicationDetailsFragment extends Fragment implements ApplicationDetailsComponent {

    private TextView mAppNameTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.app_details_fragment_layout, container, false);
        mAppNameTextView = (TextView) view.findViewById(R.id.m_app_details_caption_tv);
        return view;
    }

    @Override
    public void showApplicationDetails(String applicationId) {
        mAppNameTextView.setText("Application: "+applicationId);
    }
}
