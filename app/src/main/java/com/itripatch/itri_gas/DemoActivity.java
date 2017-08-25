package com.itripatch.itri_gas;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
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

import java.util.ArrayList;

public class DemoActivity extends AppCompatActivity implements BluetoothAdapter.LeScanCallback {
    private BluetoothAdapter mBTAdapter;
    private DeviceAdapter mDeviceAdapter;
    private boolean mIsScanning;
    public static String mode = "demo"; // demo:展示;work:工程
    private View alarmPopupView;
    private Boolean alarmPopupWindowShow = false;
    private PopupWindow alarmPopupWindow;
    private Boolean emStart = false;
    private MediaPlayer mp;
    private Toolbar toolbar;
    private DemoFragment[] demo = new DemoFragment[5];
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        if (mp != null) {
            mp.pause();
            mp.setLooping(false); //停止音樂播放
            mp = null;
        }
        super.onDestroy();
    }

    public void tabWork(View v) {
        Toast.makeText(getApplicationContext(), "tabWork", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLeScan(final BluetoothDevice newDeivce, int newRssi, byte[] newScanRecord) {
        if (newDeivce.getName() != null) {
            if (newDeivce.getName().contains("itri gas sensor") && mDeviceAdapter.getSize() < 5) {
                if (!mDeviceAdapter.check(newDeivce.getAddress())) {
                    mDeviceAdapter.add(newDeivce, newRssi, newScanRecord);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            demo[mDeviceAdapter.getSize() - 1] = DemoFragment.newInstance(newDeivce.getName(), newDeivce.getAddress());
                            mSectionsPagerAdapter.notifyDataSetChanged();
                        }
                    });
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
            float gasLine[] = {-5, -5, -5, -5, -5, -5, -5, -5, -5};
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
                            demo[i].update(Integer.parseInt(battery[i]), Integer.parseInt(gas[i]), Double.parseDouble(temp[i]), Double.parseDouble(hum[i]));
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
            for (int i = 0; i < gas.length; i++) {
                int g = Integer.parseInt(gas[i]);
                float draw = (float) (-5 + (g - 0) / (4000.0 - 0) * (5 + 5));
                gasLine[i] = draw + getSmooth(draw);
            }
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

    private void alarmButtonClick() {
        if (!alarmPopupWindowShow) {
            //讓螢幕背景的亮度只有50%
            WindowManager.LayoutParams params = getWindow().getAttributes();
            params.alpha = 0.5f;
            getWindow().setAttributes(params);
            final View curView = getLayoutInflater().inflate(R.layout.activity_demo, null);
            TextView alarmMsg = (TextView) alarmPopupView.findViewById(R.id.alarm_msg);
            alarmMsg.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.flash_leave_now));  //Start Animation
            alarmPopupWindow = new PopupWindow(alarmPopupView, 820, 300, true);
            alarmPopupWindow.setTouchable(false);
            alarmPopupWindow.setOutsideTouchable(false);
            alarmPopupWindow.setAnimationStyle(R.style.slow_Display_Window);
            alarmPopupWindow.showAtLocation(curView, Gravity.CENTER, 650, -80);
            //Play Sound
            setVolumeControlStream(AudioManager.STREAM_MUSIC);
            mp = MediaPlayer.create(DemoActivity.this, R.raw.police3);
            mp.setLooping(true);
            mp.seekTo(0);
            mp.start();

            switch (mode) {
                case "demo":
                    break;
            }
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
            switch (mode) {
                case "demo":
                    break;
            }
            alarmPopupWindowShow = false;
        }
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
