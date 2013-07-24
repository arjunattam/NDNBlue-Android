package org.irl.ndnblue;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class BluetoothService extends Service {

	// Debugging
	private final static boolean D = true;
	private final static String TAG = "NDNBlue";
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		Log.v(TAG, "Service running");
	}
	
	@Override
	public void onDestroy() {
		Log.v(TAG, "Service destroy");
	}

}
