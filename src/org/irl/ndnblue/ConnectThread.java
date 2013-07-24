package org.irl.ndnblue;

import java.io.IOException;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class ConnectThread extends Thread {

	// Debugging
	private static final String TAG = "NDNBlue";
	private boolean D = true;

	// BT 
	private final BluetoothSocket btSocket;
	private final BluetoothDevice btDevice;
	private BluetoothAdapter btAdapter;

	private String _prefix;
	private Context _ctx;
	private Handler _handler;

	public ConnectThread(BluetoothAdapter adapter, BluetoothDevice otherDevice, Handler handler, String prefix, Context ctx) {
		btAdapter = adapter;
		BluetoothSocket tmp = null;
		_handler = handler;
		_prefix = prefix;
		btDevice = otherDevice;
		_ctx = ctx;

		try {
			tmp = btDevice.createRfcommSocketToServiceRecord(Constants.APP_UUID);
		} catch (IOException e) { 
			if (D) Log.v(TAG, "Cannot start RFCOMM");
		}
		btSocket = tmp;
	}
	public void run() {
		btAdapter.cancelDiscovery();
		try {
			btSocket.connect();
		} catch (IOException e) {
			if (D) Log.v(TAG, "Unable to connect");
			_handler.obtainMessage(0,0,-1, "Cannot connect :(").sendToTarget();
			try {
				btSocket.close();
			} catch (IOException e_1) { }
			return;
		}
		manageConnectedSocket(btSocket);
	}
	public void cancel() {
		try {
			if (D) Log.v(TAG, "--- connectThread cancel ---");
			// btSocket.close();
		} catch (Exception e) {
			if (D) Log.v(TAG, e.toString());
		}
	}

	private void manageConnectedSocket(BluetoothSocket btSocket) {
		if (btSocket.isConnected()) {
			_handler.obtainMessage(0,0,-1, "Not connected").sendToTarget();
		} else {
			Log.e(TAG, "Socket is not connected!");
		}
		ConnectedThread btConnection = new ConnectedThread(btSocket, _handler, _prefix, _ctx);
		btConnection.start();
	}
}