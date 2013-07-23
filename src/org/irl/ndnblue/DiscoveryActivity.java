package org.irl.ndnblue;

import java.util.Set;

import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class DiscoveryActivity extends ListActivity {

	// Debugging
	private final static boolean D = true;
	private final static String TAG = "NDNBlue";

	/*
	String[] DayOfWeek = {"Sunday", "Monday", "Tuesday",
			"Wednesday", "Thursday", "Friday", "Saturday"
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.main);
		setListAdapter(new ArrayAdapter<String>(this,
				R.layout.row, R.id.weekofday, DayOfWeek));
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		//super.onListItemClick(l, v, position, id);
		String selection = l.getItemAtPosition(position).toString();
		Toast.makeText(this, selection, Toast.LENGTH_LONG).show();
	}
	*/

	private TextView discoverStatus;
	private ListView discoverResults;
	private ArrayAdapter<String> discoverAdapter;

	private BluetoothAdapter btAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (D) Log.v(TAG, "--- ON CREATE ---");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_discovery);

		discoverResults = getListView();
		// discoverResults = (ListView)findViewById(R.id.discover_results);
		discoverStatus = (TextView)findViewById(R.id.discover_status);
		discoverAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
		discoverResults.setAdapter(discoverAdapter);

		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		registerReceiver(mReceiver, filter); 

		// TODO: Unregister receiver

		btAdapter = BluetoothAdapter.getDefaultAdapter();
		if (btAdapter.startDiscovery())
			discoverStatus.setText("Discovering devices");
		else
			discoverStatus.setText("Unable to start discovery");

		queryPaired();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.discovery, menu);
		return true;
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		//super.onListItemClick(l, v, position, id);
		String selection = l.getItemAtPosition(position).toString();
		String remoteAddress = selection.substring(selection.length() - 17);
		Intent intent = new Intent(DiscoveryActivity.this, MainActivity.class);
		unregisterReceiver(mReceiver);
		intent.putExtra("remoteAddress", remoteAddress);
		startActivity(intent);
	}

	private void queryPaired() {
		if (D) Log.v(TAG, "--- QUERY PAIRED ---");
		Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
		if (D) Log.v(TAG, "--- get bonded devices ---");
		// If there are paired devices
		if (pairedDevices.size() > 0) {
			if (D) Log.v(TAG, "--- found devices ---");
			// Loop through paired devices
			for (BluetoothDevice device : pairedDevices) {
				// Add the name and address to an array adapter to show in a ListView
				discoverAdapter.add(device.getName() + "\n" + device.getAddress());
			}
		}
	}

	// Create a BroadcastReceiver for ACTION_FOUND
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			if (D) Log.v(TAG, "--- ON RECEIVE ---");
			String action = intent.getAction();
			// When discovery finds a device
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				// Get the BluetoothDevice object from the Intent
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				// Add the name and address to an array adapter to show in a ListView
				discoverAdapter.add(device.getName() + "\n" + device.getAddress());
			}
		}
	};

}
