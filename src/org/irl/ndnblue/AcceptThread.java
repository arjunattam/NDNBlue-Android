package org.irl.ndnblue;

import java.io.IOException;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class AcceptThread extends Thread {
	// Debugging
	private static final String TAG = "NDNBlue";
	private boolean D = true;
	
	// BT
	private BluetoothAdapter btAdapter;
	
	Handler _handler;
	String _prefix;
	Context _ctx;

	private final BluetoothServerSocket btServerSocket;
	public AcceptThread(BluetoothAdapter adapter, Handler handler, String prefix, Context ctx) {
		btAdapter = adapter;
		_prefix = prefix;
		_handler = handler;
		_ctx = ctx;
		BluetoothServerSocket tmp = null;
		try {
			tmp = btAdapter.listenUsingRfcommWithServiceRecord(Constants.APP_NAME, Constants.APP_UUID);
		} catch (IOException e) { }
		btServerSocket = tmp;
	}
	public void run() {
		BluetoothSocket btSocket = null;
		// Keep listening
		while (true) {
			try {
				btSocket = btServerSocket.accept();
			} catch (IOException e) {
				break;
			}
			// If connection is accepted
			if (btSocket != null) {
				if (D) Log.v(TAG, "Accepted");
				manageConnectedSocket(btSocket);
				try {
					btServerSocket.close();
				} catch (IOException e) { }
				break;
			}
		}
	}
	public void cancel() {
		try {
			btServerSocket.close();
		} catch (IOException e) { }
	}

	private void manageConnectedSocket(BluetoothSocket btSocket) {
		Message msg = new Message();
		if (btSocket.isConnected()) {
			_handler.obtainMessage(0,0,-1, "Connected").sendToTarget();
		} else {
			_handler.obtainMessage(0,0,-1, "Not connected").sendToTarget();
		}
		ConnectedThread btConnection = new ConnectedThread(btSocket, _handler, _prefix, _ctx);
		btConnection.start();
	}
}