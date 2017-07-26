package com.wanaka.ble.midi;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import jp.kshoji.blemidi.central.BleMidiCentralProvider;
import jp.kshoji.blemidi.device.MidiInputDevice;
import jp.kshoji.blemidi.device.MidiOutputDevice;
import jp.kshoji.blemidi.listener.OnMidiDataListener;
import jp.kshoji.blemidi.listener.OnMidiDeviceAttachedListener;
import jp.kshoji.blemidi.listener.OnMidiDeviceDetachedListener;
import jp.kshoji.blemidi.listener.OnMidiDeviceFoundListener;
import jp.kshoji.blemidi.listener.OnMidiDeviceStatusListener;
import jp.kshoji.blemidi.listener.OnMidiScanStatusListener;

import static jp.kshoji.blemidi.util.Constants.TAG;

/**
 * Created by jim on 2017/7/21.
 */

public class BleMidiManager {
    BleMidiCentralProvider mBleMidiCentralProvider;

    private boolean mIsConnected;

    private MidiInputDevice midiInputDevice;

    private MidiOutputDevice midiOutputDevice;

    private BleConnection mBleConnection;

    private OnMidiDataListener midiDataListener;

    private BleMidiManager() {
    }

    public static BleMidiManager getInstance() {
        return SingletonHolder.sInstance;
    }

    public void init(Context context) {
        setupCentralProvider(context);
    }

    public void open() {
        mBleMidiCentralProvider.startScanDevice(5000);
    }

    public void close() {
        mBleMidiCentralProvider.stopScanDevice();
    }

    public void suspend() {
        // TODO
    }

    public void resume() {
        // TODO
    }

    public boolean isConnected() {
        return mIsConnected;
    }

    public void connect(@NonNull BluetoothDevice device) {
        mBleMidiCentralProvider.connect(device);
    }

    public void disconnect() {
        if (isConnected()) {
            mBleMidiCentralProvider.disconnect(midiInputDevice, midiOutputDevice);
        }
    }

    public void sendMsg(final byte data[]) {
        if (mBleConnection != null) {
            mBleConnection.sendMsg(data);
        }
    }

    public void setOnBluetoothDeviceFoundListener(OnMidiDeviceFoundListener listener) {
        mBleMidiCentralProvider.setOnBluetoothDeviceFoundListener(listener);
    }

    public void setOnMidiScanStatusListener(OnMidiScanStatusListener listener) {
        mBleMidiCentralProvider.setOnMidiScanStatusListener(listener);
    }

    public void setOnMidiDeviceStatusListener(OnMidiDeviceStatusListener listener) {
        mBleMidiCentralProvider.setOnMidiDeviceStatusListener(listener);
    }

    public void setOnMidiDataListener(OnMidiDataListener listener) {
        this.midiDataListener = listener;
    }

    /**
     * Configure BleMidiCentralProvider instance
     */
    private void setupCentralProvider(Context context) {
        mBleMidiCentralProvider = new BleMidiCentralProvider(context);

        initStatusListeners();

        initConnection();
    }

    /**
     * Init all listeners
     */
    private void initStatusListeners() {
        mBleMidiCentralProvider.setOnMidiDeviceAttachedListener(new OnMidiDeviceAttachedListener() {
            @Override
            public void onMidiInputDeviceAttached(@NonNull MidiInputDevice device) {
                Log.w(TAG, "onMidiInputDeviceAttached![" + device + "]");
                //midiInputDevice.setOnMidiInputEventListener(onMidiInputEventListener);

                midiInputDevice = device;

                mBleConnection.inputDevice = device;

                mIsConnected = true;
            }

            @Override
            public void onMidiOutputDeviceAttached(@NonNull MidiOutputDevice device) {
                Log.w(TAG, "onMidiOutputDeviceAttached![" + device + "]");

                midiOutputDevice = device;

                mBleConnection.outputDevice = device;

                mIsConnected = true;
            }
        });

        mBleMidiCentralProvider.setOnMidiDeviceDetachedListener(new OnMidiDeviceDetachedListener() {
            @Override
            public void onMidiInputDeviceDetached(@NonNull MidiInputDevice device) {
                Log.w(TAG, "onMidiInputDeviceDetached![" + midiInputDevice + "]");

                midiInputDevice = null;

                mBleConnection.inputDevice = null;

                mIsConnected = false;
            }

            @Override
            public void onMidiOutputDeviceDetached(@NonNull MidiOutputDevice device) {
                Log.w(TAG, "onMidiOutputDeviceDetached![" + midiOutputDevice + "]");

                midiOutputDevice = null;

                mBleConnection.outputDevice = null;

                mIsConnected = false;
            }
        });

        mBleMidiCentralProvider.setOnMidiDataListener(new OnMidiDataListener() {
            @Override
            public void onMidiData(MidiInputDevice device, byte[] data) {
                Log.w(TAG, "onMidiData!!!!!" + device);

                if (midiDataListener != null) {
                    midiDataListener.onMidiData(device, data);
                }
            }
        });
    }

    /**
     * Init ble connection
     */
    private void initConnection() {
        mBleConnection = new BleConnection();
    }

    // 实现 Connect 接口
    static private class BleConnection {
        MidiOutputDevice outputDevice = null;
        MidiInputDevice inputDevice = null;
        boolean validated = false;

        public void suspend() {
            // TODO
        }

        public void resume() {
            // TODO
        }

        public void sendMsg(final byte data[]) {
            // 根据data长度来判定是系统消息还是普通消息
            if (data.length > 6) {
                long c = ((long)(data[0] + 256) << 32) | (data[1] << 24) | (data[2] << 16) | (data[3] << 8) | data[4];
                if (c == 0xf000202b69L) {
                    outputDevice.sendMidiSystemExclusive(data);
                    return;
                }
            }

            // TODO
            //outputDevice.sendMidiSystemCommonMessage(0, data);
        }

        public boolean isValidated() {
            return validated;
        }
    }

    private static class SingletonHolder{
        private static BleMidiManager sInstance = new BleMidiManager();
    }
}
