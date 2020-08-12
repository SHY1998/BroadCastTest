package com.example.broadcasttest;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.broadcasttest.Bean.ReceiveMessage;
import com.example.broadcasttest.Bean.SendMessage;
import com.example.broadcasttest.util.BlueToothUtil;
import com.example.broadcasttest.zxing.android.CaptureActivity;

public class FirstPageScan extends AppCompatActivity implements View.OnClickListener{

    private static final int REQUEST_CODE_SCAN = 0X0000;
    private static final String DECODED_CONTENT_KEY = "codedContent";
    private static final String DECODED_BITMAP_KEY = "codedBitmap";
    private static final String TAG = "FirstPageScan";

    //目的mac
    private String targetName;
    //延迟处理
    private Handler mHandler;
    //当前的操作类型
    private int curOp;
    private int previousOp;
    private int successOp;
    private ReceiveMessage totalMessage;

    private String simulationMsg = "0D09303030313032303330343035 0416000300";


    //状态参数
    //是否在检测
    private boolean mScanning;
    //是否找到指定mac的设备
    private boolean found = false;
    private int curNum = -1;

    //页面组件
    private TextView tv_scanResult;
    private RelativeLayout loadPart;
    private ImageView imgPgbar;
    private AnimationDrawable ad;
    private Button btn_search;
    //操作类型
    private static final int find = 0X00;
    private static final int cfgSet = 0X01;
    private static final int cfgGet = 0X02;
    private static final int editionGet = 0X03;
    private static final int powerGet = 0X04;
    private static final int conOut = 0XFF;
    //蓝牙配置信息
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private BluetoothLeAdvertiser mBluetoothLeAdvertiser;
    private static final String BROADCAST_SERVICE = "00001802-0000-1000-8000-00805f9b34fb";
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.firstpage);
        totalMessage = new ReceiveMessage();
        initUI();
        initBroad();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void bleScan() {
        //检测是否支持
        if( !mBluetoothAdapter.isEnabled()) {
            BlueToothUtil.showDialog(this,"该设备不支持蓝牙！");
        }
            if (Build.VERSION.SDK_INT >= 21) {
                if (mBluetoothLeScanner == null) {
                    mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
                }
                mBluetoothLeScanner.startScan(null, createScanSetting(), mScanCallback);
            } else {
                BlueToothUtil.showDialog(FirstPageScan.this, "手机版本过低， 该软件不支持！");
            }
        conResult();
    }

    private void initUI(){
        //二维码扫描按钮
        Button btn_scan = findViewById(R.id.btn_scan);
        //设备搜索按钮
        btn_search = findViewById(R.id.conTest);
        //设置点击事件
        btn_search.setOnClickListener(this);
        btn_scan.setOnClickListener(this);
        //mac显示按钮
        tv_scanResult = findViewById(R.id.tv_scanResult);
        //加载条父组件
        loadPart = findViewById(R.id.loadPart);
        //加载框
        imgPgbar = findViewById(R.id.loading);
        ad = (AnimationDrawable) imgPgbar.getDrawable();
        //设置动画
        imgPgbar.postDelayed(new Runnable() {
            @Override
            public void run() {
                ad.start();
            }
        },100);
    }

    /**
     * 初始化蓝牙配置
     */
    @SuppressLint("NewApi")
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void initBroad() {
        mHandler = new Handler();
        //判断是否支持蓝牙
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this,"不支持", Toast.LENGTH_LONG).show();
            finish();
        }

        mBluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();

        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent,1);
            Toast.makeText(this,"未配对蓝牙", Toast.LENGTH_LONG).show();
            finish();
        }

        mBluetoothLeAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();

        if (mBluetoothLeAdvertiser == null ) {
            Toast.makeText(this, "不支持BLE Peripheral", Toast.LENGTH_SHORT).show();
            finish();
        }

        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
    }

    /**
     * 扫描设定
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private ScanSettings createScanSetting() {
        ScanSettings.Builder mSettingsBuilder = new ScanSettings.Builder();
        mSettingsBuilder.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);
        if (Build.VERSION.SDK_INT >= 23) {
            mSettingsBuilder.setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES);
            mSettingsBuilder.setMatchMode(ScanSettings.MATCH_MODE_STICKY);
        }
        if (mBluetoothAdapter.isOffloadedScanBatchingSupported()) {
            mSettingsBuilder.setReportDelay(0L);
        }
        return mSettingsBuilder.build();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void stopScan(){
        mBluetoothLeScanner.stopScan(mScanCallback);
    }

    /**
     * 广播设置
     * @param connectable 是否可以连接
     * @param timeoutMillis 广播时长
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static AdvertiseSettings createAdvSettings(boolean connectable, int timeoutMillis) {
        //初始化广播设置
        AdvertiseSettings.Builder mSettingsbuilder = new AdvertiseSettings.Builder();
        //设置广播模式，以控制广播的功率和延迟。 ADVERTISE_MODE_LOW_LATENCY为高功率，低延迟
        mSettingsbuilder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED);
        //设置广告类型是可连接还是不可连接。
        mSettingsbuilder.setConnectable(connectable);
        //广播时限。最多180000毫秒。值为0将禁用时间限制。（不设置则为无限广播时长）
        mSettingsbuilder.setTimeout(timeoutMillis);
        //设置蓝牙广播发射功率级别
        mSettingsbuilder.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH);

        //初始化广播设置
        AdvertiseSettings mAdvertiseSettings = mSettingsbuilder.build();

        //如果广播设置不为
        if (mAdvertiseSettings == null) {
            Log.e(TAG, "mAdvertiseSettings == null");
        }
        return mAdvertiseSettings;
    }

    /**
     * 广播报文设置
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private AdvertiseData createAdvertiseData() {
        SendMessage sendMessage = new SendMessage(targetName, ++curNum, curOp,null);
        AdvertiseData mAdvertiseData = new AdvertiseData.Builder()
                .addServiceData(ParcelUuid.fromString(BROADCAST_SERVICE), BlueToothUtil.fieldShaping(sendMessage))
                .build();
        return mAdvertiseData;
    }

    /**
     * 扫描回调
     */
    @SuppressLint("NewApi")
    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            if (result != null) {
                if (result.getScanRecord().getBytes() != null) {
                    String reDataStr = BlueToothUtil.bytesToHexString(result.getScanRecord().getBytes());
                    Log.d(TAG, "\nonScanResult:  = " + reDataStr);
                    reDataStr = "0D093030303130323033303430350416000300";
                    if(BlueToothUtil.initTest(simulationMsg, targetName)) {
//                        found = true;
                        parseMsg(simulationMsg);
                    }

                }
            }
        }
    };

    /**
     * 广播回调
     */
    @SuppressLint("NewApi")
    private AdvertiseCallback mAdvertiseCallback = new AdvertiseCallback() {
        @Override
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        //成功
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
            if (settingsInEffect != null) {
                bleScan();
                Log.d(TAG, "onStartSuccess TxPowerLv=" + settingsInEffect.getTxPowerLevel() + " mode=" + settingsInEffect.getMode()
                        + " timeout=" + settingsInEffect.getTimeout());
            } else {
                Log.e(TAG, "onStartSuccess, settingInEffect is null");
            }
            Log.e(TAG, "onStartSuccess settingsInEffect" + settingsInEffect);
        }
        //失败
        @Override
        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);
            Log.e(TAG, "onStartFailure errorCode" + errorCode);//返回的错误码
            if (errorCode == ADVERTISE_FAILED_DATA_TOO_LARGE) {
                Log.e(TAG, "数据大于31个字节");
            } else if (errorCode == ADVERTISE_FAILED_TOO_MANY_ADVERTISERS) {
                Log.e(TAG, "未能开始广播，没有广播实例");
            } else if (errorCode == ADVERTISE_FAILED_ALREADY_STARTED) {
                Log.e(TAG, "正在连接的，无法再次连接");
            } else if (errorCode == ADVERTISE_FAILED_INTERNAL_ERROR) {
                Log.e(TAG, "由于内部错误操作失败");
            } else if (errorCode == ADVERTISE_FAILED_FEATURE_UNSUPPORTED) {
                Log.e(TAG, "不支持此功能");
            }
        }
    };

    @Override
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_scan:
                if (ContextCompat.checkSelfPermission(FirstPageScan.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(FirstPageScan.this, new String[]{Manifest.permission.CAMERA}, 1);
                } else {

                    codeScan();
                }
                break;
            case R.id.conTest:
                targetName = tv_scanResult.getText().toString();
                if (targetName.equals("")) {
                    Toast.makeText(this,"请先输入想要接收的广播名",Toast.LENGTH_SHORT).show();
                } else {
                    //设置查找操作
                    btn_search.setEnabled(true);
                    curOp = find;
                    curNum ++ ;
                    bleScan();
                    loadPart.setVisibility(View.VISIBLE);
                }
            default:
                break;
        }

    }

    /**
     * 二维码扫描
     */
    private void codeScan() {
        Intent intent = new Intent(FirstPageScan.this, CaptureActivity.class);
        startActivityForResult(intent, REQUEST_CODE_SCAN);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    codeScan();
                } else {
                    Toast.makeText(this, "你拒绝了权限申请，可能无法打开相机扫码哟！", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 扫描二维码/条码回传
        if (requestCode == REQUEST_CODE_SCAN && resultCode == RESULT_OK) {
            if (data != null) {
                //返回的文本内容
                String content = data.getStringExtra(DECODED_CONTENT_KEY);
                //返回的BitMap图像
                Bitmap bitmap = data.getParcelableExtra(DECODED_BITMAP_KEY);

                tv_scanResult.setText(content);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void parseMsg(String msg) {
        Log.d(TAG, "parseMsg: 当前的Op" + curOp);
        Log.d(TAG, "parseMsg: 当前的num" + curNum);
        switch (curOp) {
            case find:
                found = true;
                successOp = curOp;
                getCfg(cfgGet);
                break;
            case cfgGet:
                Log.d(TAG, "parseMsg: = cfgGet");
                try {
                    ReceiveMessage receiveMessage = new ReceiveMessage(msg);
                    if (receiveMessage.getExeResult() == 0) {
                        Log.d(TAG, "当前的模拟数据" + msg);
                        Log.d(TAG, "当前的receiveMessage" + receiveMessage);
                        if (BlueToothUtil.messageCorrect(receiveMessage, curNum, targetName, curOp)) {
                            successOp = curOp;
                            totalMessage.setBroadType(receiveMessage.getBroadType());
                            totalMessage.setPowerSet(receiveMessage.getPowerSet());
                            totalMessage.setRepFre(receiveMessage.getRepFre());
                            totalMessage.setPowerAlarm(receiveMessage.getPowerAlarm());
                            totalMessage.setSignAlarm(receiveMessage.getSignAlarm());
                            getCfg(editionGet);
                        } else {
                            Log.d(TAG, "当前的信息: curNum =" + curNum + "curOp=" + curOp);
                        }
                        
                    }
                } catch (Exception e) {
                    Toast.makeText(FirstPageScan.this,e.toString(), Toast.LENGTH_SHORT).show();
                }
                break;
            case editionGet:
                Log.d(TAG, "parseMsg: = editionGet");
                try {
                    ReceiveMessage receiveMessage = new ReceiveMessage(msg);
                    if (receiveMessage.getExeResult() == 0) {
                        if (BlueToothUtil.messageCorrect(receiveMessage, curNum, targetName, curOp)) {
                            successOp =curOp;
                            totalMessage.setEdition(receiveMessage.getEdition());
                            getCfg(powerGet);
                        } else {
                            Log.d(TAG, "当前的信息: curNum =" + curNum + "curOp=" + curOp);
                        }
                    }
                }catch (Exception e) {
                    Toast.makeText(FirstPageScan.this,e.toString(), Toast.LENGTH_SHORT).show();
                }
                break;
            case powerGet:
                Log.d(TAG, "parseMsg: = powerGet");
                try {
                    ReceiveMessage receiveMessage = new ReceiveMessage(msg);
                    if (receiveMessage.getExeResult() == 0) {
                        if (BlueToothUtil.messageCorrect(receiveMessage, curNum, targetName, curOp)) {
                            successOp = curOp;
                            totalMessage.setBattery(receiveMessage.getBattery());
//                            getCfg(powerGet);
                            stopScan();
                            Toast.makeText(FirstPageScan.this, "读取完成",Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(this,SecondPageMain.class);
                            intent.putExtra("info",totalMessage.toString());
                            startActivity(intent);
//                            BlueToothUtil.showDialog(FirstPageScan.this,"全部读取完成");


                        } else {
                            Log.d(TAG, "当前的信息: curNum =" + curNum + "curOp=" + curOp);
                        }
                    }
                }catch (Exception e) {
                    Toast.makeText(FirstPageScan.this,e.toString(), Toast.LENGTH_SHORT).show();
                }
                break;

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void getCfg(int Op){
        switch (Op) {
            case find:
                Log.d(TAG, "getCfg: find");
                previousOp = curOp;
                Log.d(TAG, " previousOp = " + previousOp + "successOp=" + successOp );
                if (previousOp == successOp) {
                    Log.d(TAG, "getCfg: 成功进入对比");
                    curOp = Op;
                    simulationMsg = "0D09303030313032303330343035 0A16 0101000101001F1F1F";
                    mBluetoothLeAdvertiser.startAdvertising(createAdvSettings(false, 0),createAdvertiseData(), mAdvertiseCallback);
                }
                break;
            case cfgGet:
                Log.d(TAG, "getCfg: cfgGet");
                previousOp = curOp;
                Log.d(TAG, " previousOp = " + previousOp + "successOp=" + successOp );
                if (previousOp == successOp) {
                    Log.d(TAG, "getCfg: 成功进入对比");
                    curOp = Op;
                    simulationMsg = "0D09303030313032303330343035 0A160102000101001F1F1F";
//                    mBluetoothLeAdvertiser.stopAdvertising(mAdvertiseCallback);
                    mBluetoothLeAdvertiser.startAdvertising(createAdvSettings(false, 0),createAdvertiseData(), mAdvertiseCallback);
                }
                break;
            case editionGet:
                Log.d(TAG, "getCfg: editionGet");
                Log.d(TAG, "getCfg: 进入case Edition");
                Log.d(TAG, "getCfg: 当前的操作码 = " + curNum);
                previousOp = curOp;
                Log.d(TAG, " previousOp = " + previousOp + "successOp=" + successOp );
                if (previousOp == successOp) {
                    curOp = Op;
                    simulationMsg = "0D09303030313032303330343035 0916 02 03000431303130";
//                    mBluetoothLeAdvertiser.stopAdvertising(mAdvertiseCallback);
                    mBluetoothLeAdvertiser.startAdvertising(createAdvSettings(false, 0),createAdvertiseData(), mAdvertiseCallback);
                }
                break;
            case powerGet:
                Log.d(TAG, "getCfg: 进入case powerGet");
                previousOp = curOp;
                if (previousOp == successOp) {
                    curOp = Op;
                    simulationMsg = "0D093030303130323033303430350616030400001F";
                    mBluetoothLeAdvertiser.stopAdvertising(mAdvertiseCallback);
                    mBluetoothLeAdvertiser.startAdvertising(createAdvSettings(false, 0),createAdvertiseData(), mAdvertiseCallback);
                }
                break;
        }
    }

    private void conResult() {
        if (curOp == find) {
            //10s后结束扫描
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(Build.VERSION.SDK_INT >= 21) {
                        if (curOp == find) {
                            //如果操作码依然是find，那么说明没有找到，则表示无法找到设备，所以关闭扫描
                            mBluetoothLeScanner.stopScan(mScanCallback);
                            BlueToothUtil.showDialog(FirstPageScan.this,"未找到设备，请滑动正确手势或者重新输入Mac");
                            btn_search.setEnabled(false);
                        } else {
                            Toast.makeText(FirstPageScan.this,"已找到设备",Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            },5000);
        }
    }

}