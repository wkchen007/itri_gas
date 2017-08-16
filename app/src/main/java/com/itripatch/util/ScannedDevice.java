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

package com.itripatch.util;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.util.Log;

import com.radiusnetworks.ibeacon.IBeacon;

/**
 * LeScanned Bluetooth Device
 */
public class ScannedDevice {
    private static final String UNKNOWN = "Unknown";
    /**
     * BluetoothDevice
     */
    private BluetoothDevice mDevice;
    /**
     * RSSI
     */
    private int mRssi;
    /**
     * Display Name
     */
    private String mDisplayName;
    /**
     * Advertise Scan Record
     */
    private byte[] mScanRecord;
    /**
     * parsed iBeacon Data
     */
    private IBeacon mIBeacon;
    /**
     * last updated (Advertise scanned)
     */
    private long mLastUpdatedMs;
    /**
     * start time (Advertise scanned)
     */
    private long mStartTime;
    /**
     * emergency bit
     */
    private boolean mEmergencyBit;
    /**
     * gas
     */
    private int mGas;

    private int mGasMax, mGasMin, mGasRange;

    private boolean ignore = false;

    private double mTemperature;
    private double mHumidity;
    private int mPower;
    public ScannedDevice(BluetoothDevice device, int rssi, byte[] scanRecord, long now) {
        if (device == null) {
            throw new IllegalArgumentException("BluetoothDevice is null");
        }
        mLastUpdatedMs = now;
        mStartTime = now;
        mDevice = device;
        mDisplayName = device.getName();
        if ((mDisplayName == null) || (mDisplayName.length() == 0)) {
            mDisplayName = UNKNOWN;
        }
        mRssi = rssi;
        mScanRecord = scanRecord;
        setPower();
        setEmergencyBit();
        mGasMax = 0;mGasMin = 4095;
        mGasRange = mGasMax - mGasMin;
        setGas();
        setTemperature();
        setHumidity();
        checkIBeacon();
    }

    private void checkIBeacon() {
        if (mScanRecord != null) {
            mIBeacon = IBeacon.fromScanData(mScanRecord, mRssi);
        }
    }

    public BluetoothDevice getDevice() {
        return mDevice;
    }

    public int getRssi() {
        return mRssi;
    }

    public void setRssi(int rssi) {
        mRssi = rssi;
    }

    public long getLastUpdatedMs() {
        return mLastUpdatedMs;
    }

    public long getStartTime() {
        return mStartTime;
    }

    public boolean getEmergencyBit() {
        return mEmergencyBit;
    }

    public void setIgnore() {
        ignore = !ignore;
    }

    public boolean getIgnore() {
        return ignore;
    }

    public void setEmergencyBit() {
        if (!ignore) {
            String hexString = getScanRecordHexString();
            try {
                if (Integer.parseInt(hexString.substring(34, 36)) == 1)
                    mEmergencyBit = true;
                else
                    mEmergencyBit = false;
            } catch (NumberFormatException e) {
                mEmergencyBit = false;
            }
        } else
            mEmergencyBit = false;
    }

    public void setLastUpdatedMs(long lastUpdatedMs) {
        mLastUpdatedMs = lastUpdatedMs;
    }

    public byte[] getScanRecord() {
        return mScanRecord;
    }

    public String getScanRecordHexString() {
        return ScannedDevice.asHex(mScanRecord);
    }

    public void setScanRecord(byte[] scanRecord) {
        mScanRecord = scanRecord;
        checkIBeacon();
    }

    public IBeacon getIBeacon() {
        return mIBeacon;
    }

    public String getDisplayName() {
        return mDisplayName;
    }

    public void setDisplayName(String displayName) {
        mDisplayName = displayName;
    }

    public int getGas() { return mGas; }

    public int getGasMax() { return mGasMax; }

    public int getGasMin() { return mGasMin; }

    public int getGasRange() { return mGasRange; }

    public void setGas() {
        String hexString = getScanRecordHexString();
        int gas = (short) Integer.parseInt(hexString.substring(14, 18), 16);
        if (gas >= 0)
            mGas = gas;
        else
            mGas = 0;
        if (mGas > mGasMax)
            mGasMax = mGas;
        if (mGas < mGasMin)
            mGasMin = mGas;
        mGasRange = mGasMax - mGasMin;
    }
    public double getTemperature() { return mTemperature; }
    public void setTemperature() {
        String hexString = getScanRecordHexString();
        double temperature = Integer.parseInt(hexString.substring(18, 26), 16) / 100.0;
        mTemperature = Math.floor(temperature * 100) / 100.0;
    }
    public double getHumidity() { return mHumidity; }
    public void setHumidity() {
        String hexString = getScanRecordHexString();
        double humidity = Integer.parseInt(hexString.substring(26, 34), 16) / 1024.0;
        mHumidity = Math.floor(humidity * 100) / 100.0;
    }
    public int getPower() { return mPower; }
    public void setPower() {
        String hexString = getScanRecordHexString();
        int power = (short) Integer.parseInt(hexString.substring(10, 12), 16);
        mPower = power;
    }
    public String toCsv() {
        StringBuilder sb = new StringBuilder();
        // DisplayName,MAC Addr,RSSI,Last Updated,iBeacon flag,Proximity UUID,major,minor,TxPower
        sb.append(mDisplayName).append(",");
        sb.append(mDevice.getAddress()).append(",");
        sb.append(mRssi).append(",");
        sb.append(DateUtil.get_yyyyMMddHHmmssSSS(mLastUpdatedMs)).append(",");
        if (mIBeacon == null) {
            sb.append("false,,0,0,0");
        } else {
            sb.append("true").append(",");
            sb.append(mIBeacon.toCsv());
        }
        return sb.toString();
    }

    /**
     * バイト配列を16進数の文字列に変換する。 http://d.hatena.ne.jp/winebarrel/20041012/p1
     *
     * @param bytes バイト配列
     * @return 16進数の文字列
     */
    @SuppressLint("DefaultLocale")
    public static String asHex(byte bytes[]) {
        if ((bytes == null) || (bytes.length == 0)) {
            return "";
        }

        // バイト配列の２倍の長さの文字列バッファを生成。
        StringBuffer sb = new StringBuffer(bytes.length * 2);

        // バイト配列の要素数分、処理を繰り返す。
        for (int index = 0; index < bytes.length; index++) {
            // バイト値を自然数に変換。
            int bt = bytes[index] & 0xff;

            // バイト値が0x10以下か判定。
            if (bt < 0x10) {
                // 0x10以下の場合、文字列バッファに0を追加。
                sb.append("0");
            }

            // バイト値を16進数の文字列に変換して、文字列バッファに追加。
            sb.append(Integer.toHexString(bt).toUpperCase());
        }

        /// 16進数の文字列を返す。
        return sb.toString();
    }
}
