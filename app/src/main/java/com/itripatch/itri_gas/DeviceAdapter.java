package com.itripatch.itri_gas;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;

import com.itripatch.util.ScannedDevice;

import java.util.List;

/**
 * スキャンされたBLEデバイスリストのAdapter
 */
public class DeviceAdapter {
    private List<ScannedDevice> mList;
    private LayoutInflater mInflater;

    public DeviceAdapter(Context context, List<ScannedDevice> objects) {
        mList = objects;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public boolean check(String address) {
        boolean contains = false;
        for (ScannedDevice device : mList) {
            if (address.equals(device.getDevice().getAddress())) {
                contains = true;
                break;
            }
        }
        return contains;
    }

    public void add(BluetoothDevice newDevice, int rssi, byte[] scanRecord) {
        long now = System.currentTimeMillis();
        mList.add(new ScannedDevice(newDevice, rssi, scanRecord, now));
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
        long now = System.currentTimeMillis();
        for (ScannedDevice device : mList) {
            if (newDevice.getAddress().equals(device.getDevice().getAddress())) {
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
        String battery = "";
        String em = "";
        String gas = "";
        String temp = "";
        String hum = "";
        for (ScannedDevice device : mList) {
            battery += device.getPower() + ";";
            em += device.getEmergencyBit() + ";";
            gas += device.getGas() + ";";
            temp += device.getTemperature() + ";";
            hum += device.getHumidity() + ";";
        }
        battery = battery.substring(0, battery.length() - 1);
        em = em.substring(0, em.length() - 1);
        gas = gas.substring(0, gas.length() - 1);
        temp = temp.substring(0, temp.length() - 1);
        hum = hum.substring(0, hum.length() - 1);
        String summary = battery + "," + em + "," + gas + "," + temp + "," + hum;

        return summary;
    }

    public List<ScannedDevice> getList() {
        return mList;
    }

    public int getSize() {
        return (mList == null) ? 0 : mList.size();
    }

    public ScannedDevice getDevice(int position) {
        return mList.get(position);
    }
}
