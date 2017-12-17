package com.itripatch.itri_gas;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.SharedPreferences;
import android.media.MediaScannerConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.itripatch.util.BleUtil;
import com.itripatch.util.DateUtil;
import com.itripatch.util.ScannedDevice;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class WorkActivity extends AppCompatActivity {
    private BluetoothAdapter mBTAdapter;
    private DeviceAdapter mDeviceAdapter;
    private boolean mIsScanning;
    private BluetoothLeScanner mLEScanner;
    private ScanSettings settings;
    private List<ScanFilter> filters;
    private Toolbar toolbar;
    private FrameLayout waveformLayout;
    private WaveformView waveformView = null;
    public static String mAddress = null;
    private boolean[] isLine = {true, false, false, false, false, false, false, false, false};
    private float gasLine[] = {-5, -5, -5, -5, -5, -5, -5, -5, -5};
    private TextView deviceAddress, deviceName, sensitivity, gasRatio, min, max, range, lastTime, startTime, updateTime;
    private TextView showNormal, showWarn, showCareful, showDanger;
    private EditText setStart, normal, warn, careful, danger;
    private Button reset;
    private JSONObject mAir;
    private SharedPreferences sp;
    private SharedPreferences.Editor ed;
    //儲存檔案
    private Button saveFile;
    private TextView saveTime;
    private boolean startSave = false;
    private long mSaveTime;
    private File myFile;
    //計時重啟掃描
    private Boolean startReadTimerOn = false;
    private TimerTask readtask;
    private Timer readtimer;

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
        deviceAddress = (TextView) findViewById(R.id.deviceAddress);
        deviceName = (TextView) findViewById(R.id.deviceName);
        sensitivity = (TextView) findViewById(R.id.sensitivity);
        gasRatio = (TextView) findViewById(R.id.gasRatio);
        min = (TextView) findViewById(R.id.min);
        max = (TextView) findViewById(R.id.max);
        range = (TextView) findViewById(R.id.range);
        lastTime = (TextView) findViewById(R.id.lastTime);
        startTime = (TextView) findViewById(R.id.startTime);
        updateTime = (TextView) findViewById(R.id.updateTime);
        sp = this.getSharedPreferences("sensorList", MODE_PRIVATE);
        normal = (EditText) findViewById(R.id.normal);
        showNormal = (TextView) findViewById(R.id.showNormal);
        warn = (EditText) findViewById(R.id.warn);
        showWarn = (TextView) findViewById(R.id.showWarn);
        careful = (EditText) findViewById(R.id.careful);
        showCareful = (TextView) findViewById(R.id.showCareful);
        danger = (EditText) findViewById(R.id.danger);
        showDanger = (TextView) findViewById(R.id.showDanger);
        setStart = (EditText) findViewById(R.id.setStart);
        reset = (Button) findViewById(R.id.reset);
        try {
            mAir = new JSONObject(sp.getString(mAddress, null));
            setStart.setText(mAir.getDouble("start") + "");
            normal.setText(mAir.getDouble("normal") + "");
            warn.setText(mAir.getDouble("warn") + "");
            careful.setText(mAir.getDouble("careful") + "");
            danger.setText(mAir.getDouble("danger") + "");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ed_change();
        saveTime = (TextView) findViewById(R.id.saveTime);
        saveFile = (Button) findViewById(R.id.saveFile);
        saveFile.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (saveFile.getText().equals("Record")) {
                    String fileName = ((EditText) findViewById(R.id.fileName)).getText().toString();
                    myFile = new File(Environment.getExternalStorageDirectory().getPath() + "/Log/" + fileName + ".csv");
                    if (myFile.exists()) {
                        startSave = false;
                        myFile = null;
                        Toast.makeText(getApplicationContext(), fileName + " 檔案已經存在", Toast.LENGTH_SHORT).show();
                    } else {
                        File dir = new File(Environment.getExternalStorageDirectory().getPath() + "/Log");
                        // ----如要在SD卡中建立數據庫文件，先做如下的判斷和建立相對應的目錄和文件----
                        if (!dir.exists()) { // 判斷目錄是否存在
                            dir.mkdirs(); // 建立目錄
                        } else {
                        }
                        try {
                            myFile.createNewFile();
                            FileOutputStream fOut = new FileOutputStream(myFile);
                            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut, "UTF-8");
                            String title = mDeviceAdapter.getDevice(0).getDisplayName() + "," + mDeviceAdapter.getDevice(0).getDevice().getAddress() + "\n" + "Last Updated,ADC,Min,Max,Range,Temperature,Humidity,Power";
                            myOutWriter.write(title + "\n");
                            myOutWriter.close();
                            fOut.close();
                            MediaScannerConnection.scanFile(getApplicationContext(), new String[]{Environment.getExternalStorageDirectory().getPath() + "/Log/" + fileName + ".csv"}, null, null);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        startSave = true;
                        mSaveTime = 0;
                        saveFile.setText("Stop");
                    }
                } else {
                    startSave = false;
                    String fileName = ((EditText) findViewById(R.id.fileName)).getText().toString();
                    MediaScannerConnection.scanFile(getApplicationContext(), new String[]{Environment.getExternalStorageDirectory().getPath() + "/Log/" + fileName + ".csv"}, null, null);
                    myFile = null;
                    saveFile.setText("Record");
                }
            }
        });
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
            ScanFilter filter = new ScanFilter.Builder().setDeviceAddress(mAddress).build();
            filters.add(filter);
            startScan();
            startReadTimer();
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
            if (!mDeviceAdapter.check(mAddress)) {
                mDeviceAdapter.add(newDeivce, newRssi, newScanRecord);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        deviceName.setText(mDeviceAdapter.getDevice(0).getDevice().getName());
                        deviceAddress.setText(mDeviceAdapter.getDevice(0).getDevice().getAddress());
                    }
                });
            } else {
                updateUI(newDeivce, newRssi, newScanRecord);
            }
        }
    };

    public void updateUI(final BluetoothDevice newDeivce, final int newRssi,
                         final byte[] newScanRecord) {
        String summary = mDeviceAdapter.update(newDeivce, newRssi, newScanRecord);
        if (summary != null) {
            final int g = Integer.parseInt(mDeviceAdapter.getDevice(0).getGas() + "");
            float draw = (float) (-5 + (g - 0) / (4000.0 - 0) * (5 + 5));
            gasLine[0] = draw + getSmooth(draw);
            final long mStartTime = mDeviceAdapter.getDevice(0).getStartTime();
            final long mUpdateTime = mDeviceAdapter.getDevice(0).getLastUpdatedMs();
            final double mGasRatio = mDeviceAdapter.getDevice(0).getGasRatio();
            final long mLastTime = mUpdateTime - mStartTime;
            final int mMin = mDeviceAdapter.getDevice(0).getGasMin();
            final int mMax = mDeviceAdapter.getDevice(0).getGasMax();
            final int mRange = mDeviceAdapter.getDevice(0).getGasRange();
            String str = setStart.getText() + "";
            if (str.equals(""))
                str = "0";
            double start = Double.parseDouble(str);
            str = normal.getText() + "";
            if (str.equals(""))
                str = "0";
            final int normal = (int) Math.floor(start + start * Double.parseDouble(str + "") / 100.0);
            str = warn.getText() + "";
            if (str.equals(""))
                str = "0";
            final int warn = (int) Math.floor(start + start * Double.parseDouble(str + "") / 100.0);
            str = careful.getText() + "";
            if (str.equals(""))
                str = "0";
            final int careful = (int) Math.floor(start + start * Double.parseDouble(str + "") / 100.0);
            str = danger.getText() + "";
            if (str.equals(""))
                str = "0";
            final int danger = (int) Math.floor(start + start * Double.parseDouble(str + "") / 100.0);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    sensitivity.setText(g + "");
                    min.setText(mMin + "");
                    max.setText(mMax + "");
                    range.setText(mRange + "");
                    lastTime.setText(DateUtil.get_lastTime(mLastTime));
                    startTime.setText(DateUtil.get_yyyyMMddHHmmssSSS(mStartTime));
                    updateTime.setText(DateUtil.get_yyyyMMddHHmmssSSS(mUpdateTime));
                    gasRatio.setText(mGasRatio + " %");
                    showNormal.setText(normal + "");
                    showWarn.setText(warn + "");
                    showCareful.setText(careful + "");
                    showDanger.setText(danger + "");
                    try {
                        waveformView.drawACC(gasLine[0], gasLine[1], gasLine[2], gasLine[3], gasLine[4], gasLine[5], gasLine[6], gasLine[7], gasLine[8], isLine);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
            long now = System.currentTimeMillis();
            if (startSave && (now - mSaveTime) >= 1000) {
                StringBuilder sb = new StringBuilder();
                sb.append(DateUtil.get_yyyyMMddHHmmss(mUpdateTime)).append(",");
                sb.append(g).append(",");
                sb.append(mMin).append(",");
                sb.append(mMax).append(",");
                sb.append(mRange).append(",");
                sb.append(mDeviceAdapter.getDevice(0).getTemperature()).append(",");
                sb.append(mDeviceAdapter.getDevice(0).getHumidity()).append(",");
                sb.append(mDeviceAdapter.getDevice(0).getPower());
                try {
                    FileOutputStream fOut = new FileOutputStream(myFile, true);
                    OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut, "UTF-8");
                    myOutWriter.write(sb.toString() + "\n");
                    myOutWriter.close();
                    fOut.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mSaveTime = now;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        saveTime.setText(DateUtil.get_yyyyMMddHHmmss(mSaveTime));
                    }
                });
            }
        }
    }

    public float getSmooth(float value) {
        float smooth = -0.2f;
        if (value >= -5 && value < -3)
            smooth = -0.2f;
        else if (value >= -3 && value < 0)
            smooth = -0.1f;
        else if (value >= 0 && value < 1)
            smooth = 0.0f;
        else if (value >= 1 && value < 3)
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
    private void ed_change() {
        reset.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                String str = sensitivity.getText() + "";
                if (str.equals(""))
                    str = "0";
                setStart.setText(str);
            }
        });
        setStart.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                String str = s + "";
                if (str.equals(""))
                    str = "0";
                try {
                    mAir.put("start", Double.parseDouble(str));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                ed = sp.edit();
                ed.putString(mAddress, mAir.toString());
                ed.commit();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        normal.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                String str = s + "";
                if (str.equals(""))
                    str = "0";
                try {
                    mAir.put("normal", Double.parseDouble(str));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                ed = sp.edit();
                ed.putString(mAddress, mAir.toString());
                ed.commit();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        warn.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                String str = s + "";
                if (str.equals(""))
                    str = "0";
                try {
                    mAir.put("warn", Double.parseDouble(str));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                ed = sp.edit();
                ed.putString(mAddress, mAir.toString());
                ed.commit();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        careful.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                String str = s + "";
                if (str.equals(""))
                    str = "0";
                try {
                    mAir.put("careful", Double.parseDouble(str));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                ed = sp.edit();
                ed.putString(mAddress, mAir.toString());
                ed.commit();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        danger.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                String str = s + "";
                if (str.equals(""))
                    str = "0";
                try {
                    mAir.put("danger", Double.parseDouble(str));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                ed = sp.edit();
                ed.putString(mAddress, mAir.toString());
                ed.commit();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }
}
