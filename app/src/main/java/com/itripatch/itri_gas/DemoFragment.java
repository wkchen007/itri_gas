package com.itripatch.itri_gas;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DemoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DemoFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private Toolbar toolbar;
    private RelativeLayout relativeLayout;
    private ImageView imageView;
    private TextView device_name, device_address, temperature, humidity, airStatus;

    public DemoFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DemoFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DemoFragment newInstance(String param1, String param2) {
        DemoFragment fragment = new DemoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.content_demo, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        relativeLayout = (RelativeLayout) getView().findViewById(R.id.relativeLayout);
        imageView = (ImageView) getView().findViewById(R.id.imageView);
        device_name = (TextView) getView().findViewById(R.id.device_name);
        device_address = (TextView) getView().findViewById(R.id.device_address);
        temperature = (TextView) getView().findViewById(R.id.temperature);
        humidity = (TextView) getView().findViewById(R.id.humidity);
        airStatus = (TextView) getView().findViewById(R.id.airStatus);
    }

    public void update(String name, String address, int gas, double temp, double hum) {
        device_name.setText(name);
        device_address.setText(address);
        temperature.setText(temp + "");
        humidity.setText(hum + "");

        if (gas < 2193) {
            toolbar.setBackgroundColor(getResources().getColor(R.color.normal));
            airStatus.setText(R.string.normal);
            relativeLayout.setBackgroundColor(getResources().getColor(R.color.normal));
            imageView.setBackground(getResources().getDrawable(R.drawable.normal));
        } else if (gas >= 2193 && gas < 2479) {
            toolbar.setBackgroundColor(getResources().getColor(R.color.warn));
            airStatus.setText(R.string.warn);
            relativeLayout.setBackgroundColor(getResources().getColor(R.color.warn));
            imageView.setBackground(getResources().getDrawable(R.drawable.warn));
        } else if (gas >= 2479 && gas < 2609) {
            toolbar.setBackgroundColor(getResources().getColor(R.color.careful));
            airStatus.setText(R.string.careful);
            relativeLayout.setBackgroundColor(getResources().getColor(R.color.careful));
            imageView.setBackground(getResources().getDrawable(R.drawable.careful));
        } else {
            toolbar.setBackgroundColor(getResources().getColor(R.color.danger));
            airStatus.setText(R.string.danger);
            relativeLayout.setBackgroundColor(getResources().getColor(R.color.danger));
            imageView.setBackground(getResources().getDrawable(R.drawable.danger));
        }
    }
}
