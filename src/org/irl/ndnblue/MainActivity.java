package org.irl.ndnblue;

import java.io.IOException;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {

	// Debugging
	private final static boolean D = true;
	private final static String TAG = "NDNBlue";

	// Hard-coded strings
	private final static String addressOne = "C8:19:F7:0B:25:D3";
	private final static String addressTwo = "04:FE:31:6C:59:4F";
	private String myAddress;
	private String otherAddress;

	private String prefix = "/ndn/blue-test/";

	// Declare graphical elements
	TextView status;
	Button discoverButton;
	Button serverButton;
	Button clientButton;
	Button workerButton;
	Button serviceButton;
	EditText remoteAddress;
	EditText prefixAddress;

	// Declare BT adapter
	BluetoothAdapter btAdapter;

	// For user to enable BT
	private final static int REQUEST_ENABLE_BT = 1;

	// Threads
	private AcceptThread acceptThread = null;
	private ConnectThread connectThread = null;

	private BluetoothWorker btWorker;

	// Handler
	private Handler btHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			String text = (String)msg.obj;
			status.setText(text);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (D) Log.v(TAG, "--- ON CREATE ---");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		status 			= (TextView)findViewById(R.id.status);
		discoverButton 	= (Button)findViewById(R.id.discover_button);
		serverButton 	= (Button)findViewById(R.id.server_button);
		clientButton	= (Button)findViewById(R.id.client_button);
		workerButton	= (Button)findViewById(R.id.worker_button);
		serviceButton	= (Button)findViewById(R.id.service_button);
		remoteAddress	= (EditText)findViewById(R.id.remote_address);
		prefixAddress	= (EditText)findViewById(R.id.prefix_address);
		
		btAdapter 		= BluetoothAdapter.getDefaultAdapter();

		discoverButton.setOnClickListener(discoverListener);
		serverButton.setOnClickListener(serverListener);
		clientButton.setOnClickListener(clientListener);
		workerButton.setOnClickListener(workerListener);
		serviceButton.setOnClickListener(serviceListener);
		btStatus();

		// Hard-coded solution
		myAddress = btAdapter.getAddress();
		if (myAddress.equals(addressOne)) otherAddress = addressTwo;
		else otherAddress = addressOne;
		
		remoteAddress.setText(otherAddress, TextView.BufferType.EDITABLE);
		prefixAddress.setText(prefix, TextView.BufferType.EDITABLE);

		btWorker = new BluetoothWorker(getApplicationContext(), btHandler);
	}

	@Override
	protected void onDestroy() {
		if (D) Log.v(TAG, "--- ON DESTROY ---");
		super.onDestroy();
		if (acceptThread != null) acceptThread.cancel();
		if (connectThread != null) connectThread.cancel();
	}

	private void btStatus() {
		if (D) Log.v(TAG, "--- btStatus() ---");
		if (btAdapter == null) {
			status.setText("Bluetooth not supported");
		} else {
			if (btAdapter.isEnabled()) {
				if (btAdapter.isDiscovering()) {
					status.setText("Discovering");
				} else {
					status.setText("Bluetooth is enabled");
				}
			} else {
				status.setText("Bluetooth is disabled");
				Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
			}
		}
	}

	public Button.OnClickListener discoverListener
	= new Button.OnClickListener() {
		@Override
		public void onClick(View view) {
			if (D) Log.v(TAG, "--- discover onClick ---");
			// TODO
		}
	};

	public Button.OnClickListener serverListener
	= new Button.OnClickListener() {
		@Override
		public void onClick(View view) {
			if (D) Log.v(TAG, "--- server onClick ---");
			acceptThread = new AcceptThread(btAdapter, btHandler);
			acceptThread.start();
			status.setText("Listening at " + myAddress);
		}
	};

	public Button.OnClickListener clientListener
	= new Button.OnClickListener() {
		@Override
		public void onClick(View view) {
			if (D) Log.v(TAG, "--- client onClick ---");
			otherAddress = remoteAddress.getText().toString();
			BluetoothDevice otherDevice = btAdapter.getRemoteDevice(otherAddress);
			connectThread = new ConnectThread(btAdapter, otherDevice, btHandler);
			connectThread.start();
			status.setText("Trying to connect " + otherAddress);
		}
	};

	public Button.OnClickListener workerListener
	= new Button.OnClickListener() {
		@Override
		public void onClick(View view) {
			if (D) Log.v(TAG, "--- worker onClick ---");

			//Intent workerIntent = new Intent(_context, WorkerActivity.class);
			//startActivity(workerIntent);
			btWorker.start();
			
		}
	};
	
	public Button.OnClickListener serviceListener
	= new Button.OnClickListener() {
		@Override
		public void onClick(View view) {
			if (D) Log.v(TAG, "--- service onClick ---");
			
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}