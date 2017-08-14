package com.itripatch.itri_gas;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
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
    private Fragment currentFragment;
    private RelativeLayout relativeLayout;
    private ImageView imageView;
    private TextView airStatus;
    private TextView device_name;
    private TextView device_address;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_demo);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        toolbar.setLogo(R.drawable.itri);
        setSupportActionBar(toolbar);
        currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragDemo);
        init();
        alarmPopupView = getLayoutInflater().inflate(R.layout.alarm_button, null);
        device_name = (TextView) currentFragment.getView().findViewById(R.id.device_name);
        device_address = (TextView) currentFragment.getView().findViewById(R.id.device_address);
        relativeLayout = (RelativeLayout) currentFragment.getView().findViewById(R.id.relativeLayout);
        imageView = (ImageView) currentFragment.getView().findViewById(R.id.imageView);
        airStatus = (TextView) currentFragment.getView().findViewById(R.id.airStatus);
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
    public void onLeScan(BluetoothDevice newDeivce, int newRssi, byte[] newScanRecord) {
        if (newDeivce.getName() != null) {
            if (newDeivce.getName().contains("itri gas sensor")) {
                device_name.setText(newDeivce.getName());
                device_address.setText(newDeivce.getAddress());
                updateUI(newDeivce, newRssi, newScanRecord);
            }
        }
    }

    public void updateUI(final BluetoothDevice newDeivce, final int newRssi,
                         final byte[] newScanRecord) {
        String summary = mDeviceAdapter.update(newDeivce, newRssi, newScanRecord);
        if (summary != null) {
            String data[] = summary.split(",");
            final String total = data[0];
            final String em = data[1];
            final String gas[] = data[2].split(";");
            final String temp[] = data[3].split(";");
            final String hum[] = data[4].split(";");
            float gasLine[] = {-5, -5, -5, -5, -5, -5, -5, -5, -5};
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                    mDeviceAdapter.notifyDataSetChanged();
                    ((TextView) currentFragment.getView().findViewById(R.id.temperature)).setText(temp[0]);
                    ((TextView) currentFragment.getView().findViewById(R.id.humidity)).setText(hum[0]);

                    if (Integer.parseInt(gas[0]) < 2193) {
                        toolbar.setBackgroundColor(getResources().getColor(R.color.normal));
                        airStatus.setText(R.string.normal);
                        relativeLayout.setBackgroundColor(getResources().getColor(R.color.normal));
                        imageView.setBackground(getResources().getDrawable(R.drawable.normal));
                    } else if (Integer.parseInt(gas[0]) >= 2193 && Integer.parseInt(gas[0]) < 2479) {
                        toolbar.setBackgroundColor(getResources().getColor(R.color.warn));
                        airStatus.setText(R.string.warn);
                        relativeLayout.setBackgroundColor(getResources().getColor(R.color.warn));
                        imageView.setBackground(getResources().getDrawable(R.drawable.warn));
                    } else if (Integer.parseInt(gas[0]) >= 2479 && Integer.parseInt(gas[0]) < 2609) {
                        toolbar.setBackgroundColor(getResources().getColor(R.color.careful));
                        airStatus.setText(R.string.careful);
                        relativeLayout.setBackgroundColor(getResources().getColor(R.color.careful));
                        imageView.setBackground(getResources().getDrawable(R.drawable.careful));
                    } else {
                        toolbar.setBackgroundColor(getResources().getColor(R.color.danger));
                        airStatus.setText(R.string.danger);
                        relativeLayout.setBackgroundColor(getResources().getColor(R.color.danger));
                        imageView.setBackground(getResources().getDrawable(R.drawable.danger));
                    }

                    if (em.contains("true")) {
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
            switch (mode) {
                case "demo":
                    break;
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

        // init listview
//        ListView deviceListView = (ListView) findViewById(R.id.list);
        mDeviceAdapter = new DeviceAdapter(this, R.layout.listitem_device,
                new ArrayList<ScannedDevice>());
//        deviceListView.setAdapter(mDeviceAdapter);
//        deviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                mDeviceAdapter.getDevice(i).setIgnore();
//                if (mDeviceAdapter.getDevice(i).getIgnore())
//                    Toast.makeText(getApplicationContext(), "關閉警報", Toast.LENGTH_SHORT).show();
//                else
//                    Toast.makeText(getApplicationContext(), "開啟警報", Toast.LENGTH_SHORT).show();
//            }
//        });

        switch (mode) {
            case "demo":
                break;
        }
        stopScan();
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

//        mPopupWindow = new PopupWindow(popupView, AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.WRAP_CONTENT, true);
            alarmPopupWindow = new PopupWindow(alarmPopupView, 820, 300, true);
            alarmPopupWindow.setTouchable(false);
            alarmPopupWindow.setOutsideTouchable(false);

            //這一行會造成popWindow後. 按任意一個地方都會alarmPopupWindow.setOnDismissListener
//            alarmPopupWindow.setBackgroundDrawable(new BitmapDrawable(getResources(), (Bitmap) null));

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
                // end may TODO anything else
                //讓螢幕背景回復到原先的亮度
                WindowManager.LayoutParams params = getWindow().getAttributes();
                params.alpha = 1.0f;
                getWindow().setAttributes(params);
            }
        });//end of  mPopupWindow.setOnDismissListener

    }

    private void alarmButtonCancel() {

        if (alarmPopupWindowShow) {
            alarmPopupWindow.dismiss();
            // end may TODO anything else
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
}
