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
import org.ccnx.ccn.CCNContentHandler;
import org.ccnx.ccn.CCNHandle;
import org.ccnx.ccn.CCNInterestHandler;
import org.ccnx.ccn.impl.CCNNetworkChannel;
import org.ccnx.ccn.impl.CCNNetworkManager;
import org.ccnx.ccn.impl.encoding.BinaryXMLDecoder;
import org.ccnx.ccn.impl.encoding.XMLEncodable;
import org.ccnx.ccn.protocol.ContentName;
import org.ccnx.ccn.protocol.ContentObject;
import org.ccnx.ccn.protocol.Interest;
import org.ccnx.ccn.protocol.MalformedContentNameStringException;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

public class ConnectedThread implements Runnable, CCNxServiceCallback, CCNInterestHandler, CCNContentHandler {

	// Debugging
	private static final String TAG = "NDNBlue";
	private boolean D = true;

	// BT streams
	public final BluetoothSocket _socket;
	public final InputStream mmInStream;
	public final OutputStream mmOutStream;
	
	// CCNx
	private ContentName _prefix;
	private Thread _thd;
	private Context _ctx;
	private CCNxServiceControl _ccnxService;

	private CCNHandle _handle;
	private CCNNetworkManager _netManager;
	private CCNNetworkChannel _netChannel;

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
			if (D) Log.v(TAG, "Streams in place");
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
				if (D) Log.v(TAG, "Handle opened");
				_handle.registerFilter(_prefix, this);
				_netManager = _handle.getNetworkManager();
				if (D) Log.v(TAG, "Channel done");

				BinaryXMLDecoder _decoder = new BinaryXMLDecoder();

				while (true) {
					XMLEncodable packet;
					_decoder.beginDecoding(mmInStream);
					packet = _decoder.getPacket(); 
					if (packet != null) {
						if (D) Log.v(TAG, "---- Bluetooth: Received packet");
						if (packet instanceof ContentObject) {
							if (D) Log.v(TAG, "---- Bluetooth: Decoded content object");
							ContentObject co = (ContentObject) packet;
							_handle.put(co);
						}
						if (packet instanceof Interest) {
							if (D) Log.v(TAG, "---- Bluetooth: Decoded interest");
							Interest interest = (Interest) packet;
							_handle.expressInterest(interest, this);
						}
						if (D) Log.v(TAG, packet.toString());
					}
				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				Log.e(TAG, "Error!");
				e.printStackTrace();
			}	        
		}
	}

	protected boolean initializeCCNx() {
		_ccnxService = new CCNxServiceControl(_ctx);
		_ccnxService.registerCallback(this);
		_ccnxService.setCcndOption(CCND_OPTIONS.CCND_DEBUG, "1");
		_ccnxService.setRepoOption(REPO_OPTIONS.REPO_DEBUG, "WARNING");
		return _ccnxService.startAll();
	}

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
			_handle.unregisterFilter(_prefix, this);
			_handle.close();
			_handle = null;
		} catch (IOException e) { }
	}
	@Override
	public void newCCNxStatus(SERVICE_STATUS st) {
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
		_thd.run();
	}
	@Override
	public boolean handleInterest(Interest interest) {
		if (D) Log.v(TAG, "---- CCN: Received interest");
		if (D) Log.i(TAG, interest.toString());
		try {
			byte[] encoded = interest.encode();
			if (D) Log.v(TAG, encoded.toString());
			mmOutStream.write(encoded);
		} catch (Exception e) {
			if (D) Log.e(TAG, "Error handle interest");
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public Interest handleContent(ContentObject content, Interest interest) {
		if (D) Log.v(TAG, "---- CCN: Received content");
		if (D) Log.i(TAG, content.toString());
		try {
			byte[] encoded = content.encode();
			if (D) Log.v(TAG, encoded.toString());
			mmOutStream.write(encoded);
		} catch (Exception e) {
			Log.e(TAG, "Error handle content");
			e.printStackTrace();
		}
		return null;
	}
}
