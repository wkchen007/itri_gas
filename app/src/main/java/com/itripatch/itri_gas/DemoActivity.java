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
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.itripatch.util.BleUtil;
import com.itripatch.util.ScannedDevice;

import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;
import org.altbeacon.beacon.service.scanner.NonBeaconLeScanCallback;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class DemoActivity extends AppCompatActivity implements BeaconConsumer {
    private BluetoothAdapter mBTAdapter;
    private DeviceAdapter mDeviceAdapter;
    private static final int MY_PERMISSIONS_REQUEST = 1;
    private static final int GO_WORK = 2;
    private boolean mIsScanning;
    //private BluetoothLeScanner mLEScanner;
    //private ScanSettings settings;
    //private List<ScanFilter> filters;
    private View alarmPopupView;
    private Boolean alarmPopupWindowShow = false;
    private PopupWindow alarmPopupWindow;
    private Boolean emStart = false;
    private MediaPlayer mp;
    private Toolbar toolbar;
    private DemoFragment[] demo = new DemoFragment[5];
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private SharedPreferences sp;
    private SharedPreferences.Editor ed;

    private static final long DEFAULT_FOREGROUND_SCAN_PERIOD = 1000L;
    private static final long DEFAULT_FOREGROUND_BETWEEN_SCAN_PERIOD = 1000L;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private BeaconManager beaconManager;
    //public static final String IBEACON_FORMAT = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24";
    //private BackgroundPowerSaver backgroundPowerSaver;
    private Region mRegion = new Region("all-region-beacon", null, null, null);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //backgroundPowerSaver = new BackgroundPowerSaver(this);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_demo);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        toolbar.setLogo(R.drawable.itri);
        setSupportActionBar(toolbar);
        init();
        mSectionsPagerAdapter = new SectionsPagerAdapter(
                getSupportFragmentManager());
        // 設定 ViewPager 和 Pager Adapter.
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        alarmPopupView = getLayoutInflater().inflate(R.layout.alarm_button, null);
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
            //the following method is recommended to use for getting BluetoothAdapter
            final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            mBTAdapter = bluetoothManager.getAdapter();


            //獲取beaconManager實例對象
            beaconManager = BeaconManager.getInstanceForApplication(this);
            //設置搜索的時間間隔和週期
            beaconManager.setForegroundBetweenScanPeriod(DEFAULT_FOREGROUND_BETWEEN_SCAN_PERIOD);
            beaconManager.setForegroundScanPeriod(DEFAULT_FOREGROUND_SCAN_PERIOD);
            //設置beacon數據包格式
//            beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(IBEACON_FORMAT));
            beaconManager.setNonBeaconLeScanCallback(new NonBeaconLeScanCallback() {
                @Override
                public void onNonBeaconLeScan(BluetoothDevice bluetoothDevice, int rssi, byte[] scanRecord) {
                    if (bluetoothDevice.getName() != null) {
                        if (bluetoothDevice.getName().contains("itri gas sensor")) {
                            Log.i("TTT", bluetoothDevice.getAddress() + "");
                            BluetoothDevice newDeivce = bluetoothDevice;
                            int newRssi = rssi;
                            byte[] newScanRecord = scanRecord;
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
                                    final String finalAirConfig = airConfig;
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            demo[mDeviceAdapter.getSize() - 1] = DemoFragment.newInstance(mName, mAddress, finalAirConfig);
                                            mSectionsPagerAdapter.notifyDataSetChanged();
                                        }
                                    });
                                }
                            } else
                                updateUI(newDeivce, newRssi, newScanRecord);
                        }
                    }
                }
            });
            //將activity與庫中的BeaconService綁定到一起,服務準備完成後就會自動回調下面的onBeaconServiceConnect方法
            beaconManager.bind(this);
            /*
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
            */
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
        if (mp != null) {
            mp.pause();
            mp.setLooping(false); //停止音樂播放
            mp = null;
        }
        super.onDestroy();
    }

    public void tabWork(View v) {
        stopScan();
        final String address = (String) v.getTag();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < mDeviceAdapter.getSize(); i++) {
                    if (demo[i].getAcCreated())
                        demo[i].setNA();
                }
            }
        });
        Intent i = new Intent(this, WorkActivity.class);
        WorkActivity.mAddress = address;
        startActivityForResult(i, GO_WORK);
    }

    public void tabLog(View v) {
        stopScan();
        Intent i = new Intent(this, LogActivity.class);
        startActivity(i);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case GO_WORK:
                for (int i = 0; i < mDeviceAdapter.getSize(); i++) {
                    if (mDeviceAdapter.getDevice(i).getDevice().getAddress().equals(WorkActivity.mAddress))
                        demo[i].setAir(sp.getString(WorkActivity.mAddress, null));
                }
                startScan();
                break;
        }
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
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String demoGasEm[] = gasEm.split(";");
                    for (int i = 0; i < mDeviceAdapter.getSize(); i++) {
                        if (demo[i].getAcCreated()) {
                            if (demoGasEm[i].equals("true"))
                                demo[i].setEm();
                            else
                                demo[i].setUnEm();
                            demo[i].update(Integer.parseInt(battery[i]), Integer.parseInt(gas[i]), Integer.parseInt(gasMin[i]), Double.parseDouble(temp[i]), Double.parseDouble(hum[i]));
                            mSectionsPagerAdapter.notifyDataSetChanged();
                        }
                    }
                    if (gasEm.contains("true")) {
                        if (!emStart) {
                            alarmButtonClick();
                            emStart = true;
                        }
                    } else {
                        if (emStart) {
                            alarmButtonCancel();
                            emStart = false;
                        }
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
            //mLEScanner.startScan(filters, settings, mScanCallback);
            try {
//            別忘了啓動搜索,不然不會調用didRangeBeaconsInRegion方法
                beaconManager.startRangingBeaconsInRegion(mRegion);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            mIsScanning = true;
            setProgressBarIndeterminateVisibility(true);
            invalidateOptionsMenu();
        }
    }

    private void stopScan() {
        if (mBTAdapter != null) {
            //mLEScanner.stopScan(mScanCallback);
            try {
                beaconManager.stopRangingBeaconsInRegion(mRegion);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        mIsScanning = false;
        setProgressBarIndeterminateVisibility(false);
        invalidateOptionsMenu();
    }

    private void alarmButtonClick() {
        if (!alarmPopupWindowShow) {
            //讓螢幕背景的亮度只有50%
            WindowManager.LayoutParams params = getWindow().getAttributes();
            params.alpha = 0.5f;
            getWindow().setAttributes(params);
            final View curView = getLayoutInflater().inflate(R.layout.activity_demo, null);
            TextView alarmMsg = (TextView) alarmPopupView.findViewById(R.id.alarm_msg);
            alarmMsg.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.flash_leave_now));  //Start Animation
            alarmPopupWindow = new PopupWindow(alarmPopupView, 820, 150, true);
            alarmPopupWindow.setTouchable(false);
            alarmPopupWindow.setOutsideTouchable(false);
            alarmPopupWindow.setAnimationStyle(R.style.slow_Display_Window);
            alarmPopupWindow.showAtLocation(curView, Gravity.CENTER, 0, 100);
            //Play Sound
            setVolumeControlStream(AudioManager.STREAM_MUSIC);
            mp = MediaPlayer.create(DemoActivity.this, R.raw.police3);
            mp.setLooping(true);
            mp.seekTo(0);
            mp.start();
            alarmPopupWindowShow = true;
        }
        alarmPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                alarmPopupWindowShow = false;
                alarmPopupWindow.dismiss();
                //讓螢幕背景回復到原先的亮度
                WindowManager.LayoutParams params = getWindow().getAttributes();
                params.alpha = 1.0f;
                getWindow().setAttributes(params);
            }
        });
    }

    private void alarmButtonCancel() {
        if (alarmPopupWindowShow) {
            alarmPopupWindow.dismiss();
            //讓螢幕背景回復到原先的亮度
            WindowManager.LayoutParams params = getWindow().getAttributes();
            params.alpha = 1.0f;
            getWindow().setAttributes(params);

            mp.pause();
            mp.setLooping(false); //停止音樂播放
            alarmPopupWindowShow = false;
        }
    }

    @Override
    public void onBeaconServiceConnect() {

    }

    private class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // 根據目前tab標籤頁的位置，傳回對應的fragment物件
            return demo[position];
        }

        @Override
        public int getCount() {
            return mDeviceAdapter.getSize();
        }
    }
}
