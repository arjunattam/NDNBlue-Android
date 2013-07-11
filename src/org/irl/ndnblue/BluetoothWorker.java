package org.irl.ndnblue;

import java.io.IOException;

import org.ccnx.android.ccnlib.CCNxConfiguration;
import org.ccnx.android.ccnlib.CCNxServiceCallback;
import org.ccnx.android.ccnlib.CCNxServiceControl;
import org.ccnx.android.ccnlib.CCNxServiceStatus.SERVICE_STATUS;
import org.ccnx.android.ccnlib.CcndWrapper.CCND_OPTIONS;
import org.ccnx.android.ccnlib.RepoWrapper.REPO_OPTIONS;
import org.ccnx.ccn.CCNHandle;
import org.ccnx.ccn.CCNInterestHandler;
import org.ccnx.ccn.config.ConfigurationException;
import org.ccnx.ccn.protocol.ContentName;
import org.ccnx.ccn.protocol.Interest;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class BluetoothWorker implements Runnable, CCNxServiceCallback, CCNInterestHandler {
	public static final String TAG = "NDNBlue";

	private CCNxServiceControl _ccnxService;
	private Context _context;

	private Thread _thd;
	private CCNHandle _handle;
	private ContentName _responseName;
	private Handler _handler;

	public BluetoothWorker(Context ctx, Handler handler) {
		_handler = handler;
		// TODO: checks on Bluetooth

		this._context = ctx;
		CCNxConfiguration.config(ctx, false);
		_thd = new Thread(this, "BluetoothWorker");
	}

	public void start() {
		_thd.start();
	}

	public void run() {
		if (initializeCCNx()) {
			Log.v(TAG, "Initialize done, inside try");
			Message msg = new Message(); msg.obj = new String("CCN Initialized");
			_handler.sendMessage(msg);

		} else {
			Log.v(TAG, "Error in initializeCCNx()");
		}
	}

	protected boolean initializeCCNx() {
		_ccnxService = new CCNxServiceControl(_context);
		_ccnxService.registerCallback(this);
		_ccnxService.setCcndOption(CCND_OPTIONS.CCND_DEBUG, "1");
		_ccnxService.setRepoOption(REPO_OPTIONS.REPO_DEBUG, "WARNING");
		return _ccnxService.startAll();
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
		default:
			Log.e(TAG, "Something is wrong");
			break;
		}
	}

	public boolean handleInterest(Interest interest) {
		// TODO
		return false;
	}
}
