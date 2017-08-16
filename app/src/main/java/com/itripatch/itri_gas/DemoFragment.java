package com.itripatch.itri_gas;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_NAME = "ARG_NAME";
    private static final String ARG_ADDRESS = "ARG_ADDRESS";

    private String mName, mAddress;

    private boolean acCreated = false;
    private RelativeLayout relativeLayout;
    private ImageView imageView;
    private TextView device_name, device_address, temperature, humidity, airStatus, battery;

    public DemoFragment() {
        // Required empty public constructor
    }

    public static DemoFragment newInstance(String name, String address) {
        DemoFragment fragment = new DemoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_NAME, name);
        args.putString(ARG_ADDRESS, address);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mName = getArguments().getString(ARG_NAME);
            mAddress = getArguments().getString(ARG_ADDRESS);
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
        relativeLayout = (RelativeLayout) getView().findViewById(R.id.relativeLayout);
        imageView = (ImageView) getView().findViewById(R.id.imageView);
        temperature = (TextView) getView().findViewById(R.id.temperature);
        humidity = (TextView) getView().findViewById(R.id.humidity);
        airStatus = (TextView) getView().findViewById(R.id.airStatus);
        device_name = (TextView) getView().findViewById(R.id.device_name);
        device_address = (TextView) getView().findViewById(R.id.device_address);
        battery = (TextView) getView().findViewById(R.id.battery);
        device_name.setText(mName);
        device_address.setText(mAddress);
        acCreated = true;
    }

    public boolean getAcCreated() {
        return acCreated;
    }

    public void setEm() {
        device_name.setTextColor(getResources().getColor(R.color.danger));
        device_address.setTextColor(getResources().getColor(R.color.danger));
    }

    public void setUnEm() {
        device_name.setTextColor(getResources().getColor(android.R.color.white));
        device_address.setTextColor(getResources().getColor(android.R.color.white));
    }

    public void update(int bat, int gas, double temp, double hum) {
        battery.setText(bat + " %");
        temperature.setText(temp + "");
        humidity.setText(hum + "");

        if (gas < 2000) {
            airStatus.setText(R.string.normal);
            relativeLayout.setBackgroundColor(getResources().getColor(R.color.normal));
            imageView.setBackground(getResources().getDrawable(R.drawable.normal));
        } else if (gas >= 2000 && gas < 3000) {
            airStatus.setText(R.string.warn);
            relativeLayout.setBackgroundColor(getResources().getColor(R.color.warn));
            imageView.setBackground(getResources().getDrawable(R.drawable.warn));
        } else if (gas >= 3000 && gas < 3500) {
            airStatus.setText(R.string.careful);
            relativeLayout.setBackgroundColor(getResources().getColor(R.color.careful));
            imageView.setBackground(getResources().getDrawable(R.drawable.careful));
        } else {
            airStatus.setText(R.string.danger);
            relativeLayout.setBackgroundColor(getResources().getColor(R.color.danger));
            imageView.setBackground(getResources().getDrawable(R.drawable.danger));
        }
    }
}
