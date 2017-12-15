package com.itripatch.itri_gas;

import android.Manifest;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.itripatch.util.BleUtil;
import com.itripatch.util.ScannedDevice;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class LogActivity extends AppCompatActivity {
    private BluetoothAdapter mBTAdapter;
    private DeviceAdapter mDeviceAdapter;
    private static final int MY_PERMISSIONS_REQUEST = 1;
    private static final int GO_WORK = 2;
    private boolean mIsScanning;
    private BluetoothLeScanner mLEScanner;
    private ScanSettings settings;
    private List<ScanFilter> filters;
    private Toolbar toolbar;
    private SharedPreferences sp;
    private SharedPreferences.Editor ed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_log);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        toolbar.setLogo(R.drawable.itri);
        setSupportActionBar(toolbar);
        init();
        sp = this.getSharedPreferences("sensorList", MODE_PRIVATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if ((mBTAdapter != null) && (!mBTAdapter.isEnabled())) {
            Toast.makeText(this, R.string.bt_not_enabled, Toast.LENGTH_SHORT).show();
            invalidateOptionsMenu();
            finish();
        } else {
            mLEScanner = mBTAdapter.getBluetoothLeScanner();
            settings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .build();
            filters = new ArrayList<ScanFilter>();
            ScanFilter filter = new ScanFilter.Builder().setDeviceName("itri gas sensor 1.1").build();
            ScanFilter filter2 = new ScanFilter.Builder().setDeviceName("itri gas sensor 2.0").build();
            filters.add(filter);
            filters.add(filter2);
            startScan();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            ArrayList<String> mStringList = new ArrayList<String>();

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                mStringList.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                mStringList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if (mStringList.size() != 0) {
                String[] mStringArray = new String[mStringList.size()];
                mStringArray = mStringList.toArray(mStringArray);
                requestPermissions(mStringArray,
                        MY_PERMISSIONS_REQUEST);
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        if (requestCode == MY_PERMISSIONS_REQUEST) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else {
                // Permission Denied
                Toast.makeText(this, R.string.error_ACCESS_COARSE_LOCATION, Toast.LENGTH_LONG).show();
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_demo, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mIsScanning) {
            menu.findItem(R.id.action_scan).setVisible(false);
            menu.findItem(R.id.action_stop).setVisible(true);
        } else {
            menu.findItem(R.id.action_scan).setEnabled(true);
            menu.findItem(R.id.action_scan).setVisible(true);
            menu.findItem(R.id.action_stop).setVisible(false);
        }
        if ((mBTAdapter == null) || (!mBTAdapter.isEnabled())) {
            menu.findItem(R.id.action_scan).setEnabled(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            // ignore
            return true;
        } else if (itemId == R.id.action_scan) {
            startScan();
            return true;
        } else if (itemId == R.id.action_stop) {
            stopScan();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        stopScan();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void tabWork(View v) {
        stopScan();
        final String address = (String) v.getTag();
        Intent i = new Intent(this, WorkActivity.class);
        WorkActivity.mAddress = address;
        startActivityForResult(i, GO_WORK);
    }

    public void delayMS(int delayValue) {
        try {
            Thread.sleep(delayValue); // do nothing for 1000 miliseconds (1 second)
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice newDeivce = result.getDevice();
            int newRssi = result.getRssi();
            byte[] newScanRecord = result.getScanRecord().getBytes();
            if (!mDeviceAdapter.check(newDeivce.getAddress())) {
                if (mDeviceAdapter.getSize() < 5) {
                    mDeviceAdapter.add(newDeivce, newRssi, newScanRecord);
                    final String mName = newDeivce.getName();
                    final String mAddress = newDeivce.getAddress();
                    String airConfig = null;
                    if (sp.getString(mAddress, null) != null)
                        airConfig = sp.getString(mAddress, null);
                    else {
                        ed = sp.edit();
                        try {
                            airConfig = "{\"start\":1500,\"normal\":1,\"warn\":2,\"careful\":3,\"danger\":4}";
                            ed.putString(mAddress, new JSONObject(airConfig).toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        ed.commit();
                    }
                }
            } else
                updateUI(newDeivce, newRssi, newScanRecord);
        }
    };

    public void updateUI(final BluetoothDevice newDeivce, final int newRssi,
                         final byte[] newScanRecord) {
        String summary = mDeviceAdapter.update(newDeivce, newRssi, newScanRecord);
        if (summary != null) {
            String data[] = summary.split(",");
            final String battery[] = data[0].split(";");
            final String gasEm = data[1];
            final String gas[] = data[2].split(";");
            final String temp[] = data[3].split(";");
            final String hum[] = data[4].split(";");
            final String gasMin[] = data[5].split(";");
        }
    }

    private void init() {
        // BLE check
        if (!BleUtil.isBLESupported(this)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        // BT check
        BluetoothManager manager = BleUtil.getManager(this);
        if (manager != null) {
            mBTAdapter = manager.getAdapter();
        }
        if (mBTAdapter == null) {
            Toast.makeText(this, R.string.bt_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        mDeviceAdapter = new DeviceAdapter(this, new ArrayList<ScannedDevice>());
    }

    private void startScan() {
        if ((mBTAdapter != null) && (!mIsScanning)) {
            mLEScanner.startScan(filters, settings, mScanCallback);
            mIsScanning = true;
            setProgressBarIndeterminateVisibility(true);
            invalidateOptionsMenu();
        }
    }

    private void stopScan() {
        if (mBTAdapter != null) {
            mLEScanner.stopScan(mScanCallback);
        }
        mIsScanning = false;
        setProgressBarIndeterminateVisibility(false);
        invalidateOptionsMenu();
    }
}
