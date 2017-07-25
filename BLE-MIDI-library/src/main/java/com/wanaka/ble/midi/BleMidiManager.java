package com.wanaka.ble.midi;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import jp.kshoji.blemidi.central.BleMidiCentralProvider;
import jp.kshoji.blemidi.device.MidiInputDevice;
import jp.kshoji.blemidi.device.MidiOutputDevice;
import jp.kshoji.blemidi.listener.OnMidiDeviceFoundListener;
import jp.kshoji.blemidi.listener.OnMidiDeviceAttachedListener;
import jp.kshoji.blemidi.listener.OnMidiDeviceDetachedListener;
import jp.kshoji.blemidi.listener.OnMidiDeviceStatusListener;
import jp.kshoji.blemidi.listener.OnMidiScanStatusListener;

import static jp.kshoji.blemidi.util.Constants.TAG;

/**
 * Created by jim on 2017/7/21.
 */

public class BleMidiManager {
    BleMidiCentralProvider mBleMidiCentralProvider;

    private boolean mIsConnected;

    MidiInputDevice midiInputDevice;

    MidiOutputDevice midiOutputDevice;

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
    }

    public void resume() {

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

    /**
     * Configure BleMidiCentralProvider instance
     */
    private void setupCentralProvider(Context context) {
        mBleMidiCentralProvider = new BleMidiCentralProvider(context);

        initStatusListeners();
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

                mIsConnected = true;
            }

            @Override
            public void onMidiOutputDeviceAttached(@NonNull MidiOutputDevice device) {
                Log.w(TAG, "onMidiOutputDeviceAttached![" + device + "]");
//                Message message = new Message();
//                message.arg1 = 0;
//                message.obj = midiOutputDevice;
//                midiOutputConnectionChangedHandler.sendMessage(message);

                midiOutputDevice = device;

                mIsConnected = true;
            }
        });

        mBleMidiCentralProvider.setOnMidiDeviceDetachedListener(new OnMidiDeviceDetachedListener() {
            @Override
            public void onMidiInputDeviceDetached(@NonNull MidiInputDevice device) {
                Log.w(TAG, "onMidiInputDeviceDetached![" + midiInputDevice + "]");
                // do nothing

                midiInputDevice = null;

                mIsConnected = false;
            }

            @Override
            public void onMidiOutputDeviceDetached(@NonNull MidiOutputDevice device) {
                Log.w(TAG, "onMidiOutputDeviceDetached![" + midiOutputDevice + "]");
//                Message message = new Message();
//                message.arg1 = 1;
//                message.obj = midiOutputDevice;
//                midiOutputConnectionChangedHandler.sendMessage(message);

                midiOutputDevice = null;

                mIsConnected = false;
            }
        });
    }

    private static class SingletonHolder{
        private static BleMidiManager sInstance = new BleMidiManager();
    }
}
