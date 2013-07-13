package org.irl.ndnblue;

import org.ccnx.android.ccnlib.CCNxConfiguration;
import org.ccnx.android.ccnlib.CCNxServiceCallback;
import org.ccnx.android.ccnlib.CCNxServiceControl;
import org.ccnx.android.ccnlib.CCNxServiceStatus.SERVICE_STATUS;
import org.ccnx.android.ccnlib.CcndWrapper.CCND_OPTIONS;
import org.ccnx.android.ccnlib.RepoWrapper.REPO_OPTIONS;
import org.ccnx.ccn.CCNHandle;
import org.ccnx.ccn.CCNInterestHandler;
import org.ccnx.ccn.impl.CCNNetworkManager;
import org.ccnx.ccn.protocol.ContentName;
import org.ccnx.ccn.protocol.Interest;
import org.ccnx.ccn.protocol.MalformedContentNameStringException;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

public class BluetoothWorker implements Runnable, CCNxServiceCallback, CCNInterestHandler {
	public static final String TAG = "NDNBlue";

	private CCNxServiceControl _ccnxService;
	private Context _context;

	private Thread _thd;
	private CCNHandle _handle;
	private ContentName _prefix;
	private Handler _handler;
	private CCNNetworkManager _netManager;

	public BluetoothWorker(Context ctx, Handler handler, String prefix) throws MalformedContentNameStringException {
		_handler = handler;
		_prefix = ContentName.fromURI(prefix);
		// TODO: checks on Bluetooth

		this._context = ctx;
		CCNxConfiguration.config(ctx, false);
		_thd = new Thread(this, "BluetoothWorker");
	}

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
		_thd.start();
	}

	public void run() {
		initializeCCNx();
		Log.v(TAG, "Initialize done");
		// Message msg = new Message(); msg.obj = new String("CCN Initialized");
		// _handler.sendMessage(msg);
		try {
			_handler.obtainMessage(0,0,-1, "CCN Initialized").sendToTarget();
			_handle = CCNHandle.open();
			Log.v(TAG, "Handle opened");
			_handle.registerFilter(_prefix, this);
			_netManager = _handle.getNetworkManager();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			_handler.obtainMessage(0,0,-1, "Error opening CCNHandle").sendToTarget();
			e.printStackTrace();
		}
	}

	public void stop() {
		//TODO: sometimes unregistering is useless, and you have to force quit manually
		if (_handle != null && _prefix != null) {
			Log.i(TAG, "Unregistering namespace " + _prefix.toURIString());
			_handle.unregisterFilter(_prefix, this);
			_handle.close();
			_handle = null;
			_handler.obtainMessage(0,0,-1, "Unregistered prefix").sendToTarget();
		}
	}
	
	protected boolean initializeCCNx() {
		_ccnxService = new CCNxServiceControl(_context);
		_ccnxService.registerCallback(this);
		_ccnxService.setCcndOption(CCND_OPTIONS.CCND_DEBUG, "1");
		_ccnxService.setRepoOption(REPO_OPTIONS.REPO_DEBUG, "WARNING");
		return _ccnxService.startAll();
	}

	public boolean handleInterest(Interest interest) {
		Log.v(TAG, "Received interest");
		_handler.obtainMessage(0,0,-1, "Interest").sendToTarget();
		return false;
	}
}
