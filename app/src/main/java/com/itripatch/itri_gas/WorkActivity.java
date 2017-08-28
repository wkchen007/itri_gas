package com.itripatch.itri_gas;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.itripatch.util.BleUtil;
import com.itripatch.util.ScannedDevice;

import java.util.ArrayList;

public class WorkActivity extends AppCompatActivity implements BluetoothAdapter.LeScanCallback {
    private BluetoothAdapter mBTAdapter;
    private DeviceAdapter mDeviceAdapter;
    private boolean mIsScanning;
    private Toolbar toolbar;
    private FrameLayout waveformLayout;
    private WaveformView waveformView = null;
    public static boolean[] isLine = {false, false, false, false, false, false, false, false, false};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_work);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        toolbar.setLogo(R.drawable.itri);
        setSupportActionBar(toolbar);
        init();
        waveformLayout = (FrameLayout) findViewById(R.id.wave_frame);
        waveformView = new WaveformView(this, null);
        waveformLayout.addView(waveformView);
        Bundle bundle = getIntent().getExtras();
        Toast.makeText(this, bundle.getString("address"), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if ((mBTAdapter != null) && (!mBTAdapter.isEnabled())) {
            Toast.makeText(this, R.string.bt_not_enabled, Toast.LENGTH_SHORT).show();
            invalidateOptionsMenu();
        }
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
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onLeScan(final BluetoothDevice newDeivce, int newRssi, byte[] newScanRecord) {
        if (newDeivce.getName() != null) {
            if (newDeivce.getName().contains("itri gas sensor") && mDeviceAdapter.getSize() < 5) {
                if (!mDeviceAdapter.check(newDeivce.getAddress())) {
                    mDeviceAdapter.add(newDeivce, newRssi, newScanRecord);
                } else
                    updateUI(newDeivce, newRssi, newScanRecord);
            }
        }
    }

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
            final float gasLine[] = {-5, -5, -5, -5, -5, -5, -5, -5, -5};
            for (int i = 0; i < gas.length; i++) {
                int g = Integer.parseInt(gas[i]);
                float draw = (float) (-5 + (g - 0) / (4000.0 - 0) * (5 + 5));
                gasLine[i] = draw + getSmooth(draw);
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        waveformView.drawACC(gasLine[0], gasLine[1], gasLine[2], gasLine[3], gasLine[4], gasLine[5], gasLine[6], gasLine[7], gasLine[8], isLine);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    public float getSmooth(float value) {
        float smooth = -0.2f;
        if (value >= -5 && value < -3)
            smooth = -0.2f;
        else if (value >= -3 && value < 0)
            smooth = -0.1f;
        else if (value >= 0 && value < 3)
            smooth = 0.1f;
        else if (value >= 3 && value < 5)
            smooth = 0.2f;
        else if (value >= 5)
            smooth = 0.3f;
        return smooth;
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
        startScan();
    }

    private void startScan() {
        if ((mBTAdapter != null) && (!mIsScanning)) {
            mBTAdapter.startLeScan(this);
            mIsScanning = true;
            setProgressBarIndeterminateVisibility(true);
            invalidateOptionsMenu();
        }
    }

    private void stopScan() {
        if (mBTAdapter != null) {
            mBTAdapter.stopLeScan(this);
        }
        mIsScanning = false;
        setProgressBarIndeterminateVisibility(false);
        invalidateOptionsMenu();
    }

}
