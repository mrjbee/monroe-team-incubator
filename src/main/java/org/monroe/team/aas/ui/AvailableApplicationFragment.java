package org.monroe.team.aas.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import org.monroe.team.aas.R;

import java.util.LinkedList;
import java.util.List;

/**
 * User: MisterJBee
 * Date: 8/24/13 Time: 4:33 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class AvailableApplicationFragment extends Fragment {

    private ListView mAppsListView;
    private List<String> mAvailableAppsList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.available_apps_fragment_layout, container, false);
        mAppsListView = (ListView) view.findViewById(R.id.m_ava_aaps_lv);
        mAvailableAppsList = new LinkedList<String>();
        for (int i=0;i<100;i++){
            mAvailableAppsList.add(i + ":here should be name of application");
        }
        mAppsListView.setAdapter(new ArrayAdapter<String>(getActivity(), R.layout.app_details_item_layout, mAvailableAppsList));
        mAppsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                 AvailableApplicationFragment.this.onApplicationSelected(mAvailableAppsList.get((int) l));
            }
        });
        return view;
    }

    public AvailableApplicationListPresenter getPresenter(){
        return (AvailableApplicationListPresenter) getActivity();
    }

    private void onApplicationSelected(String applicationId) {
        getPresenter().onApplicationSelected(applicationId);
    }

    public interface AvailableApplicationListPresenter{
        public void onApplicationSelected(String appId);
    }

}
