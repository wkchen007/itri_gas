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
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.itripatch.util.BleUtil;
import com.itripatch.util.DateUtil;
import com.itripatch.util.ScannedDevice;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

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
    private int size = 5;
    private TextView[] deviceAddress = new TextView[size], sensitivity = new TextView[size],
            temperature = new TextView[size], humidity = new TextView[size], saveTime = new TextView[size];
    private EditText[] fn = new EditText[size];
    //儲存檔案
    private Button saveFile;
    private boolean startSave = false;
    private long mSaveTime;
    private File[] myFile;
    //計時重啟掃描
    private Boolean startReadTimerOn = false;
    private TimerTask readtask;
    private Timer readtimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_log);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        toolbar.setLogo(R.drawable.itri);
        setSupportActionBar(toolbar);
        deviceAddress[0] = (TextView) findViewById(R.id.deviceAddress1);
        deviceAddress[1] = (TextView) findViewById(R.id.deviceAddress2);
        deviceAddress[2] = (TextView) findViewById(R.id.deviceAddress3);
        deviceAddress[3] = (TextView) findViewById(R.id.deviceAddress4);
        deviceAddress[4] = (TextView) findViewById(R.id.deviceAddress5);
        sensitivity[0] = (TextView) findViewById(R.id.sensitivity1);
        sensitivity[1] = (TextView) findViewById(R.id.sensitivity2);
        sensitivity[2] = (TextView) findViewById(R.id.sensitivity3);
        sensitivity[3] = (TextView) findViewById(R.id.sensitivity4);
        sensitivity[4] = (TextView) findViewById(R.id.sensitivity5);
        temperature[0] = (TextView) findViewById(R.id.temperature1);
        temperature[1] = (TextView) findViewById(R.id.temperature2);
        temperature[2] = (TextView) findViewById(R.id.temperature3);
        temperature[3] = (TextView) findViewById(R.id.temperature4);
        temperature[4] = (TextView) findViewById(R.id.temperature5);
        humidity[0] = (TextView) findViewById(R.id.humidity1);
        humidity[1] = (TextView) findViewById(R.id.humidity2);
        humidity[2] = (TextView) findViewById(R.id.humidity3);
        humidity[3] = (TextView) findViewById(R.id.humidity4);
        humidity[4] = (TextView) findViewById(R.id.humidity5);
        fn[0] = (EditText) findViewById(R.id.fileName1);
        fn[1] = (EditText) findViewById(R.id.fileName2);
        fn[2] = (EditText) findViewById(R.id.fileName3);
        fn[3] = (EditText) findViewById(R.id.fileName4);
        fn[4] = (EditText) findViewById(R.id.fileName5);
        saveTime[0] = (TextView) findViewById(R.id.saveTime1);
        saveTime[1] = (TextView) findViewById(R.id.saveTime2);
        saveTime[2] = (TextView) findViewById(R.id.saveTime3);
        saveTime[3] = (TextView) findViewById(R.id.saveTime4);
        saveTime[4] = (TextView) findViewById(R.id.saveTime5);
        saveFile = (Button) findViewById(R.id.saveFile);
        saveFile.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (saveFile.getText().equals("Record")) {
                    myFile = new File[size];
                    boolean fnCheck = true;
                    for (int i = 0; i < mDeviceAdapter.getSize(); i++) {
                        String fileName = fn[i].getText().toString();
                        myFile[i] = new File(Environment.getExternalStorageDirectory().getPath() + "/Log/" + fileName + ".csv");
                        if (myFile[i].exists()) {
                            fnCheck = false;
                            myFile[i] = null;
                            Toast.makeText(getApplicationContext(), fileName + " 檔案已經存在", Toast.LENGTH_SHORT).show();
                        }
                    }
                    if (fnCheck) {
                        for (int i = 0; i < mDeviceAdapter.getSize(); i++) {
                            File dir = new File(Environment.getExternalStorageDirectory().getPath() + "/Log");
                            // ----如要在SD卡中建立數據庫文件，先做如下的判斷和建立相對應的目錄和文件----
                            if (!dir.exists()) { // 判斷目錄是否存在
                                dir.mkdirs(); // 建立目錄
                            } else {
                            }
                            try {
                                String fileName = fn[i].getText().toString();
                                myFile[i].createNewFile();
                                FileOutputStream fOut = new FileOutputStream(myFile[i]);
                                OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut, "UTF-8");
                                String title = mDeviceAdapter.getDevice(i).getDisplayName() + "," + mDeviceAdapter.getDevice(i).getDevice().getAddress() + "\n" + "Last Updated,ADC,Min,Max,Range,Temperature,Humidity,Power";
                                myOutWriter.write(title + "\n");
                                myOutWriter.close();
                                fOut.close();
                                MediaScannerConnection.scanFile(getApplicationContext(), new String[]{Environment.getExternalStorageDirectory().getPath() + "/Log/" + fileName + ".csv"}, null, null);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        startSave = true;
                        mSaveTime = 0;
                        saveFile.setText("Stop");
                    }
                } else {
                    startSave = false;
                    for (int i = 0; i < mDeviceAdapter.getSize(); i++) {
                        String fileName = fn[i].getText().toString();
                        MediaScannerConnection.scanFile(getApplicationContext(), new String[]{Environment.getExternalStorageDirectory().getPath() + "/Log/" + fileName + ".csv"}, null, null);
                    }
                    myFile = null;
                    saveFile.setText("Record");
                }
            }
        });

        init();
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
            startReadTimer();
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
        stopReadTimer();
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
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            deviceAddress[mDeviceAdapter.getSize() - 1].setText(mAddress);
                        }
                    });
                }
            } else
                updateUI(newDeivce, newRssi, newScanRecord);
        }
    };
    int count = 0;
    public void updateUI(final BluetoothDevice newDeivce, final int newRssi,
                         final byte[] newScanRecord) {
        String summary = mDeviceAdapter.update(newDeivce, newRssi, newScanRecord);
        if (summary != null) {
            String data[] = summary.split(",");
            final String gas[] = data[2].split(";");
            final String temp[] = data[3].split(";");
            final String hum[] = data[4].split(";");
            long now = System.currentTimeMillis();
            if (startSave && (now - mSaveTime) >= 1000) {
                for (int i = 0; i < mDeviceAdapter.getSize(); i++) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(DateUtil.get_yyyyMMddHHmmss(mDeviceAdapter.getDevice(i).getLastUpdatedMs())).append(",");
                    sb.append(mDeviceAdapter.getDevice(i).getGas()).append(",");
                    sb.append(mDeviceAdapter.getDevice(i).getGasMin()).append(",");
                    sb.append(mDeviceAdapter.getDevice(i).getGasMax()).append(",");
                    sb.append(mDeviceAdapter.getDevice(i).getGasRange()).append(",");
                    sb.append(mDeviceAdapter.getDevice(i).getTemperature()).append(",");
                    sb.append(mDeviceAdapter.getDevice(i).getHumidity()).append(",");
                    sb.append(mDeviceAdapter.getDevice(i).getPower());
                    try {
                        FileOutputStream fOut = new FileOutputStream(myFile[i], true);
                        OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut, "UTF-8");
                        myOutWriter.write(sb.toString() + "\n");
                        myOutWriter.close();
                        fOut.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                mSaveTime = now;
                count++;
                Log.i("TTT", "updateUI: " + count);
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < mDeviceAdapter.getSize(); i++) {
                        sensitivity[i].setText(Integer.parseInt(gas[i]) + "");
                        temperature[i].setText(Double.parseDouble(temp[i]) + " 度");
                        humidity[i].setText(Double.parseDouble(hum[i]) + " %");
                        if (startSave)
                            saveTime[i].setText(DateUtil.get_yyyyMMddHHmmss(mSaveTime));
                    }
                }
            });
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

    public void startReadTimer() {
        if (readtimer == null) {
            readtimer = new Timer();
        }
        if (readtask == null) {
            readtask = new TimerTask() {
                public void run() {
                    mLEScanner.stopScan(mScanCallback);
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    mLEScanner.startScan(filters, settings, mScanCallback);
                }
            };
        }
        if (readtimer != null && readtask != null && !startReadTimerOn) {
            readtimer.schedule(readtask, 1, 1500000);   //Period must = 25 min to refresh bluetooth connect
            startReadTimerOn = true;
        }
    }

    public void stopReadTimer() {
        if (readtimer != null) {
            readtimer.cancel();
            readtimer = null;
        }
        if (readtask != null) {
            readtask.cancel();
            readtask = null;
        }
        startReadTimerOn = false;
    }
}
