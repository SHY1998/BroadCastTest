package com.example.broadcasttest;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
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
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.os.PersistableBundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.broadcasttest.Bean.ReceiveMessage;
import com.example.broadcasttest.Bean.SendMessage;
import com.example.broadcasttest.util.BlueToothUtil;

public class SecondPageMain extends AppCompatActivity {

    private static final int cfgSet = 0X01;
    private static final int cfgGet = 0X02;
    private static final int editionGet = 0X03;
    private static final int powerGet = 0X04;
    private static final int conOut = 0XFF;


    private String targetName;
    private Handler Handler;
    //当前操作数
    private int curNum = -1;

    //当前操作
    private int curOp;
    //上一个操作
    private int previousOp;
    //上一个成功的操作
    private int successOp;
    TextView textView;



    //蓝牙配置信息
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private BluetoothLeAdvertiser mBluetoothLeAdvertiser;
    private static final String BROADCAST_SERVICE = "00001802-0000-1000-8000-00805f9b34fb";
    private static final String TAG = "SecondPageMain";

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.secondpage);
        Intent intent1 = getIntent();
        String msg =intent1.getStringExtra("info");
        textView = findViewById(R.id.TV_msg);
        textView.setText(msg);
        initBroad();
//        getInfo();
    }













    private void bleScan() {
        if (!mBluetoothAdapter.isEnabled()) {
            BlueToothUtil.showDialog(SecondPageMain.this, "该设备不支持蓝牙！");
        } else {
            if (Build.VERSION.SDK_INT >= 21) {
                if (mBluetoothLeScanner == null) {
                    mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
                }
                mBluetoothLeScanner.startScan(null, createScanSettings(), mScanCallback);
            }
        }
    }

    /**
     * 判断扫描到的报文是否为所需报文
     * @param returnData
     */
    private void parseMsg(byte[] returnData) {
        String returnDataStr = BlueToothUtil.bytesToHexString(returnData);
        if (BlueToothUtil.initTest(returnDataStr, targetName)) {
            try {
                ReceiveMessage receiveMessage = new ReceiveMessage(returnDataStr);
                if (receiveMessage.getExeResult() == 0) {
                    exeMsg(receiveMessage);
                }
            } catch (Exception e) {
                Toast.makeText(SecondPageMain.this,e.toString(),Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void exeMsg(ReceiveMessage receiveMessage) {
        if (BlueToothUtil.messageCorrect(receiveMessage,curNum, targetName, curOp)) {
            Log.d(TAG, "接收到的报文对象：" + receiveMessage);
//            changeOp();
        }
    }
    /**
     * 初始化蓝牙配置
     */
    @SuppressLint("NewApi")
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void initBroad(){
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
     * 组装发送报文
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private AdvertiseData createAdvertiseData(){
        SendMessage sendMessage = new SendMessage(targetName,++curNum,curOp,null);
        AdvertiseData mAdvertiseData = new AdvertiseData.Builder()
                .addServiceData(ParcelUuid.fromString(BROADCAST_SERVICE), BlueToothUtil.fieldShaping(sendMessage))
                .build();
        return mAdvertiseData;
    }

    /**
     * 查询流程
     * @param Op
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void startProcess(int Op) {
        //判断是否是第一个报文
        if (Op != cfgGet) {
        } else {
            curOp = Op;
            previousOp = Op;
            curNum = curNum % 0XFF;
            mBluetoothLeAdvertiser.startAdvertising(createAdvSettings(false, 0), createAdvertiseData(), mAdvertiseCallback);

        }

    }

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
     * 扫描设定
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private ScanSettings createScanSettings() {
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

    /**
     * 扫描回调
     */
    @SuppressLint("NewApi")
    private ScanCallback mScanCallback = new ScanCallback() {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            if (result != null) {
                if (result.getScanRecord().getBytes() != null) {
                    parseMsg(result.getScanRecord().getBytes());
                }
            }
        }
    };

//

}
