package com.irl.ndnblue;

import java.io.IOException;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;

public class ConnectThread extends Thread {
	private final BluetoothSocket btSocket;
	private final BluetoothDevice btDevice;

	// Declare BT adapter
	BluetoothAdapter btAdapter;
	
	Handler _handler;

	public ConnectThread(BluetoothAdapter adapter, BluetoothDevice otherDevice, Handler handler) {
		btAdapter = adapter;
		BluetoothSocket tmp = null;
		_handler = handler;
		btDevice = otherDevice;

		try {
			tmp = btDevice.createInsecureRfcommSocketToServiceRecord(Constants.APP_UUID);
			// tmp = btDevice.createRfcommSocketToServiceRecord(APP_UUID);
		} catch (IOException e) { }
		btSocket = tmp;
	}
	public void run() {
		btAdapter.cancelDiscovery();
		try {
			btSocket.connect();
		} catch (IOException e) {
			// Unable to connect - close and get out
			try {
				btSocket.close();
			} catch (IOException e_1) { }
			return;
		}
		manageConnectedSocket(btSocket);
	}
	public void cancel() {
		try {
			btSocket.close();
		} catch (IOException e) { }
	}

	private void manageConnectedSocket(BluetoothSocket btSocket) {
		Message msg = new Message();
		if (btSocket.isConnected()) {
			// TODO
			msg.obj = new String("Connected");
			_handler.sendMessage(msg);
		} else {
			// textToChange = "Socket is not connected";
		}
		// ConnectedThread btConnection = new ConnectedThread(btSocket);
	}
}