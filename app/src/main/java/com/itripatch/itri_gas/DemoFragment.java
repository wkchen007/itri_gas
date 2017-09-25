package com.itripatch.itri_gas;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DemoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DemoFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_NAME = "ARG_NAME";
    private static final String ARG_ADDRESS = "ARG_ADDRESS";
    private static final String ARG_AIR = "ARG_AIR";

    private String mName, mAddress;
    private JSONObject mAir;
    private int mStart,mNormal, mWarn, mCareful, mDanger;
    private boolean acCreated = false;
    private RelativeLayout relativeLayout;
    private ImageView imageView;
    private TextView deviceName, deviceAddress, temperature, humidity, airStatus, battery;
    private LinearLayout goWork;

    public DemoFragment() {
        // Required empty public constructor
    }

    public static DemoFragment newInstance(String name, String address, String airConfig) {
        DemoFragment fragment = new DemoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_NAME, name);
        args.putString(ARG_ADDRESS, address);
        args.putString(ARG_AIR, airConfig);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mName = getArguments().getString(ARG_NAME);
            mAddress = getArguments().getString(ARG_ADDRESS);
            try {
                mAir = new JSONObject(getArguments().getString(ARG_AIR));
                mStart = mAir.getInt("start");
                mNormal = mAir.getInt("normal");
                mWarn = mAir.getInt("warn");
                mCareful = mAir.getInt("careful");
                mDanger = mAir.getInt("danger");
            } catch (JSONException e) {
                e.printStackTrace();
            }
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
        deviceName = (TextView) getView().findViewById(R.id.deviceName);
        deviceAddress = (TextView) getView().findViewById(R.id.deviceAddress);
        battery = (TextView) getView().findViewById(R.id.battery);
        goWork = (LinearLayout) getView().findViewById(R.id.goWork);

        deviceName.setText(mName);
        deviceAddress.setText(mAddress);
        goWork.setTag(mAddress);
        acCreated = true;
    }

    public boolean getAcCreated() {
        return acCreated;
    }

    public void setNA() {
        airStatus.setText(R.string.NA);
        temperature.setText(R.string.NA);
        humidity.setText(R.string.NA);
    }

    public void setEm() {
        deviceName.setTextColor(getResources().getColor(R.color.danger));
        deviceAddress.setTextColor(getResources().getColor(R.color.danger));
    }

    public void setUnEm() {
        deviceName.setTextColor(getResources().getColor(android.R.color.white));
        deviceAddress.setTextColor(getResources().getColor(android.R.color.white));
    }

    public void setAir(String air) {
        try {
            mAir = new JSONObject(air);
            mStart = mAir.getInt("start");
            mNormal = mAir.getInt("normal");
            mWarn = mAir.getInt("warn");
            mCareful = mAir.getInt("careful");
            mDanger = mAir.getInt("danger");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void update(int bat, int gas, int gasMin, double temp, double hum) {
        battery.setText(bat + " %");
        temperature.setText(temp + "");
        humidity.setText(hum + "");
        int normal = (int) Math.floor(mStart + mStart * mNormal / 100.0);
        int warn = (int) Math.floor(mStart + mStart * mWarn / 100.0);
        int careful = (int) Math.floor(mStart + mStart * mCareful / 100.0);
        int danger = (int) Math.floor(mStart + mStart * mDanger / 100.0);
        if (gas < normal) {
            airStatus.setText(R.string.normal);
            relativeLayout.setBackgroundColor(getResources().getColor(R.color.normal));
            imageView.setImageResource(R.drawable.bottle_n);
        } else if (gas >= normal && gas < warn) {
            airStatus.setText(R.string.warn);
            relativeLayout.setBackgroundColor(getResources().getColor(R.color.warn));
            imageView.setImageResource(R.drawable.bottle_w);
        } else if (gas >= warn && gas < careful) {
            airStatus.setText(R.string.careful);
            relativeLayout.setBackgroundColor(getResources().getColor(R.color.careful));
            imageView.setImageResource(R.drawable.bottle_c);
        } else {
            airStatus.setText(R.string.danger);
            relativeLayout.setBackgroundColor(getResources().getColor(R.color.danger));
            imageView.setImageResource(R.drawable.bottle_d);
        }
    }
}
