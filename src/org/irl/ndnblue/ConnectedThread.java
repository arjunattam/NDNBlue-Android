package org.irl.ndnblue;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.ccnx.android.ccnlib.CCNxConfiguration;
import org.ccnx.android.ccnlib.CCNxServiceCallback;
import org.ccnx.android.ccnlib.CCNxServiceControl;
import org.ccnx.android.ccnlib.CCNxServiceStatus.SERVICE_STATUS;
import org.ccnx.android.ccnlib.CcndWrapper.CCND_OPTIONS;
import org.ccnx.android.ccnlib.RepoWrapper.REPO_OPTIONS;
import org.ccnx.ccn.CCNHandle;
import org.ccnx.ccn.CCNInterestHandler;
import org.ccnx.ccn.impl.CCNNetworkChannel;
import org.ccnx.ccn.impl.CCNNetworkManager;
import org.ccnx.ccn.protocol.ContentName;
import org.ccnx.ccn.protocol.Interest;
import org.ccnx.ccn.protocol.MalformedContentNameStringException;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

public class ConnectedThread implements Runnable, CCNxServiceCallback, CCNInterestHandler {
	public static final String TAG = "NDNBlue";
	public final BluetoothSocket _socket;
	public final InputStream mmInStream;
	public final OutputStream mmOutStream;
	private CCNNetworkChannel _channel;
	private ContentName _prefix;
	private Thread _thd;
	private Context _ctx;
	private CCNxServiceControl _ccnxService;

	CCNHandle _handle;
	CCNNetworkManager _netManager;
	CCNNetworkChannel _netChannel;

	public BluetoothSocket get_socket() {
		return _socket;
	}
	public void set_socket() {

	}


	public CCNNetworkChannel get_channel() {
		return _channel;
	}

	public void set_channel(CCNNetworkChannel _channel) {
		this._channel = _channel;
	}

	Handler _handler;

	public ConnectedThread(BluetoothSocket socket, Handler handler, String prefix, Context ctx) {
		_socket = socket;
		_handler = handler;
		_ctx = ctx;
		InputStream tmpIn = null;
		OutputStream tmpOut = null;
		try {
			_prefix = ContentName.fromURI(prefix);
		} catch (MalformedContentNameStringException e1) {
			Log.e(TAG, "MalformedContentName");
			e1.printStackTrace();
		}

		// Get the input and output streams, using temp objects because
		// member streams are final
		try {
			tmpIn = socket.getInputStream();
			tmpOut = socket.getOutputStream();
			Log.v(TAG, "Streams in place");
		} catch (IOException e) { }

		mmInStream = tmpIn;
		mmOutStream = tmpOut;

		this._ctx = ctx;
		CCNxConfiguration.config(ctx, false);

		_thd = new Thread(this, "ConnectedThread");
	}

	public void run() {
		if (!initializeCCNx()) {
			_handler.obtainMessage(0,0,-1, "Cannot initialize CCNx").sendToTarget();
		} else {
			_handler.obtainMessage(0,0,-1, "CCN Initialized").sendToTarget();
			try {
				_handle = CCNHandle.open();
				Log.v(TAG, "Handle opened");
				_handle.registerFilter(_prefix, this);
				_netManager = _handle.getNetworkManager();
				_netChannel = _netManager.get_channel();
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				Log.e(TAG, "Error!");
				e.printStackTrace();
			}
			_netManager = _handle.getNetworkManager();
			_netChannel = _netManager.get_channel();
		}
	}

	protected boolean initializeCCNx() {
		_ccnxService = new CCNxServiceControl(_ctx);
		_ccnxService.registerCallback(this);
		_ccnxService.setCcndOption(CCND_OPTIONS.CCND_DEBUG, "1");
		_ccnxService.setRepoOption(REPO_OPTIONS.REPO_DEBUG, "WARNING");
		return _ccnxService.startAll();
	}

	/*
    public void run() {
        byte[] buffer = new byte[1024];  // buffer store for the stream
        int bytes; // bytes returned from read()

        String hello = "Hello!!";
        write(hello.getBytes());
        Log.v(TAG, "Write done");

        // Keep listening to the InputStream until an exception occurs
        while (true) {
            try {
                // Read from the InputStream
                bytes = mmInStream.read(buffer);
                int MESSAGE_READ = 131;
                Log.v(TAG, "Read");
				// Send the obtained bytes to the UI activity

                _handler.obtainMessage(MESSAGE_READ , bytes, -1, buffer)
                        .sendToTarget();

                Message msg = new Message();
                msg.obj = new String(buffer);
    			_handler.sendMessage(msg);
    			Log.v(TAG, "Handler sent");
            } catch (IOException e) {
                break;
            }
        }
    }
	 */

	/* Call this from the main activity to send data to the remote device */
	public void write(byte[] bytes) {
		try {
			mmOutStream.write(bytes);
		} catch (IOException e) { }
	}

	/* Call this from the main activity to shutdown the connection */
	public void cancel() {
		try {
			_socket.close();
		} catch (IOException e) { }
	}
	@Override
	public void newCCNxStatus(SERVICE_STATUS st) {
		//TODO: implement callback to main activity
		// Check for BT connection state
		switch(st) {
		case START_ALL_DONE:
			Log.i(TAG, "CCNx Services is ready");
			break;
		case START_ALL_ERROR:
			Log.e(TAG, "CCNx Services are not ready");
			break;
		}
	}

	public void start() {
		// TODO Auto-generated method stub
		_thd.run();
	}
	@Override
	public boolean handleInterest(Interest interest) {
		// TODO Auto-generated method stub
		Log.v(TAG, "Received interest");
		Log.i(TAG, interest.toString());
		return false;
	}
}
