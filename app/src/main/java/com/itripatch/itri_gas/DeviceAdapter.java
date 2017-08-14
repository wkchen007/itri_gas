/*
 * Copyright (C) 2013 youten
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.itripatch.itri_gas;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.itripatch.util.ScannedDevice;

import java.util.List;

/**
 * スキャンされたBLEデバイスリストのAdapter
 */
public class DeviceAdapter extends ArrayAdapter<ScannedDevice> {
    //    private static final String PREFIX_RSSI = "RSSI:";
//    private static final String PREFIX_STARTTIME = "Start Time:";
//    private static final String PREFIX_LASTUPDATED = "Last Updated:";
//    private static final String PREFIX_GAS = "ADC:";
//    private static final String PREFIX_GASMAX = "Max:";
//    private static final String PREFIX_GASMIN = "Min:";
//    private static final String PREFIX_GASRANGE = "Range:";
//    private static final String PREFIX_TEMPERATURE = "Temperature:";
//    private static final String PREFIX_HUMIDITY = "Humidity:";
//    private static final String PREFIX_POWER = "Power:";
    private List<ScannedDevice> mList;
    private LayoutInflater mInflater;
    private int mResId;
    private int mIsLineIndex = 0;

    public DeviceAdapter(Context context, int resId, List<ScannedDevice> objects) {
        super(context, resId, objects);
        mResId = resId;
        mList = objects;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ScannedDevice item = (ScannedDevice) getItem(position);

        if (convertView == null) {
            convertView = mInflater.inflate(mResId, null);
        }
        TextView name = (TextView) convertView.findViewById(R.id.device_name);
        name.setText(item.getDisplayName());
        TextView address = (TextView) convertView.findViewById(R.id.device_address);
        address.setText(item.getDevice().getAddress());
//        TextView rssi = (TextView) convertView.findViewById(R.id.device_rssi);
//        rssi.setText(PREFIX_RSSI + Integer.toString(item.getRssi()));
//        TextView lastupdated = (TextView) convertView.findViewById(R.id.device_lastupdated);
//        lastupdated.setText(PREFIX_LASTUPDATED + DateUtil.get_yyyyMMddHHmmssSSS(item.getLastUpdatedMs()));
//        TextView startTime = (TextView) convertView.findViewById(R.id.device_startTime);
//        startTime.setText(PREFIX_STARTTIME + DateUtil.get_yyyyMMddHHmmssSSS(item.getStartTime()));
//        TextView scanRecord = (TextView) convertView.findViewById(R.id.device_scanrecord);
//        scanRecord.setText(PREFIX_GAS + item.getGas());
//        TextView gasMax = (TextView) convertView.findViewById(R.id.gas_max);
//        gasMax.setText(PREFIX_GASMAX + item.getGasMax());
//        TextView gasMin = (TextView) convertView.findViewById(R.id.gas_min);
//        gasMin.setText(PREFIX_GASMIN + item.getGasMin());
//        TextView gasRange = (TextView) convertView.findViewById(R.id.gas_range);
//        gasRange.setText(PREFIX_GASRANGE + item.getGasRange());
        TextView temperature = (TextView) convertView.findViewById(R.id.temperature);
        temperature.setText(item.getTemperature() + "");
        TextView humidity = (TextView) convertView.findViewById(R.id.humidity);
        humidity.setText(item.getHumidity() + "");
//        TextView power = (TextView) convertView.findViewById(R.id.power);
//        power.setText(PREFIX_POWER + item.getPower());
//        if (item.getEmergencyBit()) {
//            name.setTextColor(Color.RED);
//            address.setTextColor(Color.RED);
//        } else {
//            name.setTextColor(Color.BLACK);
//            address.setTextColor(Color.BLACK);
//        }
//        switch (position) {
//            case 0:
//                scanRecord.setTextColor(Color.BLUE);
//                break;
//            case 1:
//                scanRecord.setTextColor(Color.YELLOW);
//                break;
//            case 2:
//                scanRecord.setTextColor(Color.GREEN);
//                break;
//            case 3:
//                scanRecord.setTextColor(Color.CYAN);
//                break;
//            case 4:
//                scanRecord.setTextColor(Color.RED);
//                break;
//        }
        switch (DemoActivity.mode) {
            case "demo":
                break;
        }
        return convertView;
    }

    /**
     * add or update BluetoothDevice List
     *
     * @param newDevice  Scanned Bluetooth Device
     * @param rssi       RSSI
     * @param scanRecord advertise data
     * @return summary ex. "iBeacon:3 (Total:10)"
     */
    public String update(BluetoothDevice newDevice, int rssi, byte[] scanRecord) {
        if ((newDevice == null) || (newDevice.getAddress() == null)) {
            return "";
        }
        long now = System.currentTimeMillis();

        boolean contains = false;
        for (ScannedDevice device : mList) {
            if (newDevice.getAddress().equals(device.getDevice().getAddress())) {
                contains = true;
                // update
                device.setRssi(rssi);
                device.setLastUpdatedMs(now);
                device.setScanRecord(scanRecord);
                device.setEmergencyBit();
                device.setGas();
                device.setTemperature();
                device.setHumidity();
                device.setPower();
                break;
            }
        }
        if (!contains && mList.size() < 1) {
            // add new BluetoothDevice
            mList.add(new ScannedDevice(newDevice, rssi, scanRecord, now));
            mIsLineIndex++;
        }
        /*
        // sort by RSSI
        Collections.sort(mList, new Comparator<ScannedDevice>() {
            @Override
            public int compare(ScannedDevice lhs, ScannedDevice rhs) {
                if (lhs.getRssi() == 0) {
                    return 1;
                } else if (rhs.getRssi() == 0) {
                    return -1;
                }
                if (lhs.getRssi() > rhs.getRssi()) {
                    return -1;
                } else if (lhs.getRssi() < rhs.getRssi()) {
                    return 1;
                }
                return 0;
            }
        });
        */
        // create summary
        int totalCount = 0;
        int iBeaconCount = 0;
        if (mList != null) {
            totalCount = mList.size();
            for (ScannedDevice device : mList) {
                if (device.getIBeacon() != null) {
                    iBeaconCount++;
                }
            }
        }
        String em = "";
        String gas = "";
        String temp = "";
        String hum = "";
        for (ScannedDevice device : mList) {
            em += device.getEmergencyBit() + ";";
            gas += device.getGas() + ";";
            temp += device.getTemperature() + ";";
            hum += device.getHumidity() + ";";
        }
        em = em.substring(0, em.length() - 1);
        gas = gas.substring(0, gas.length() - 1);
        temp = temp.substring(0, temp.length() - 1);
        hum = hum.substring(0, hum.length() - 1);
        String summary = "Beacon:" + totalCount + "," + em + "," + gas + "," + temp + "," + hum;

        return summary;
    }

    public List<ScannedDevice> getList() {
        return mList;
    }

    public ScannedDevice getDevice(int position) {
        return mList.get(position);
    }
}
