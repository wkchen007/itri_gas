package com.itripatch.util;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.util.Log;

import com.radiusnetworks.ibeacon.IBeacon;

public class ScannedDevice {
    private static final String UNKNOWN = "Unknown";

    private BluetoothDevice mDevice;
    private IBeacon mIBeacon;
    private String mDisplayName;
    private byte[] mScanRecord;
    private int mRssi;
    private long mStartTime, mLastUpdatedMs;
    private int mPower, mGas, mGasMax, mGasMin, mGasRange;
    private boolean mGasAlarm, mEmergencyBit;
    private double mTemperature, mHumidity;
    private boolean ignore = false;

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
        setGasAlarm();
        mGasMax = 0;
        mGasMin = 4095;
        mGasRange = mGasMax - mGasMin;
        setGas();
        setTemperature();
        setHumidity();
        checkIBeacon();
    }

    public BluetoothDevice getDevice() {
        return mDevice;
    }

    public IBeacon getIBeacon() {
        return mIBeacon;
    }

    private void checkIBeacon() {
        if (mScanRecord != null) {
            mIBeacon = IBeacon.fromScanData(mScanRecord, mRssi);
        }
    }

    public String getDisplayName() {
        return mDisplayName;
    }

    public void setDisplayName(String displayName) {
        mDisplayName = displayName;
    }

    public byte[] getScanRecord() {
        return mScanRecord;
    }

    public void setScanRecord(byte[] scanRecord) {
        mScanRecord = scanRecord;
        checkIBeacon();
    }

    public String getScanRecordHexString() {
        return ScannedDevice.asHex(mScanRecord);
    }

    public int getRssi() {
        return mRssi;
    }

    public void setRssi(int rssi) {
        mRssi = rssi;
    }

    public long getStartTime() {
        return mStartTime;
    }

    public long getLastUpdatedMs() {
        return mLastUpdatedMs;
    }

    public void setLastUpdatedMs(long lastUpdatedMs) {
        mLastUpdatedMs = lastUpdatedMs;
    }

    public int getPower() {
        return mPower;
    }

    public void setPower() {
        String hexString = getScanRecordHexString();
        int power = (short) Integer.parseInt(hexString.substring(10, 12), 16);
        mPower = power;
    }

    public boolean getGasAlarm() {
        return mGasAlarm;
    }

    public void setGasAlarm() {
        if (!ignore) {
            String hexString = getScanRecordHexString();
            try {
                if (Integer.parseInt(hexString.substring(12, 14)) == 1)
                    mGasAlarm = true;
                else
                    mGasAlarm = false;
            } catch (NumberFormatException e) {
                mGasAlarm = false;
            }
        } else
            mGasAlarm = false;
    }

    public int getGas() {
        return mGas;
    }

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

    public int getGasMax() {
        return mGasMax;
    }

    public int getGasMin() {
        return mGasMin;
    }

    public int getGasRange() {
        return mGasRange;
    }

    public double getTemperature() {
        return mTemperature;
    }

    public void setTemperature() {
        String hexString = getScanRecordHexString();
        double temperature = Integer.parseInt(hexString.substring(18, 26), 16) / 100.0;
        mTemperature = Math.floor(temperature * 100) / 100.0;
    }

    public double getHumidity() {
        return mHumidity;
    }

    public void setHumidity() {
        String hexString = getScanRecordHexString();
        double humidity = Integer.parseInt(hexString.substring(26, 34), 16) / 1024.0;
        mHumidity = Math.floor(humidity * 100) / 100.0;
    }

    public boolean getEmergencyBit() {
        return mEmergencyBit;
    }

    public void setEmergencyBit() {
        String hexString = getScanRecordHexString();
        try {
            if (Integer.parseInt(hexString.substring(34, 36)) == 1)
                mEmergencyBit = true;
            else
                mEmergencyBit = false;
        } catch (NumberFormatException e) {
            mEmergencyBit = false;
        }
    }

    public boolean getIgnore() {
        return ignore;
    }

    public void setIgnore() {
        ignore = !ignore;
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
