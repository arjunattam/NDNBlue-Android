package org.irl.ndnblue;

import java.io.IOException;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.os.Message;

public class AcceptThread extends Thread {
	public static final String TAG = "NDNBlue";
	BluetoothAdapter btAdapter;
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
			tmp = btAdapter.listenUsingInsecureRfcommWithServiceRecord(Constants.APP_NAME, Constants.APP_UUID);
			// tmp = btAdapter.listenUsingRfcommWithServiceRecord(APP_NAME, APP_UUID);
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
			// TODO
			msg.obj = new String("Connected");
			_handler.sendMessage(msg);
		} else {
			msg.obj = new String("Not connected");
			_handler.sendMessage(msg);
		}
		ConnectedThread btConnection = new ConnectedThread(btSocket, _handler, _prefix, _ctx);
		btConnection.start();
	}
}