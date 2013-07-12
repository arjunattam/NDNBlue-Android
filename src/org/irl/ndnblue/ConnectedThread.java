package org.irl.ndnblue;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class ConnectedThread extends Thread {
	public static final String TAG = "NDNBlue";
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    
    Handler _handler;
 
    public ConnectedThread(BluetoothSocket socket, Handler handler) {
        mmSocket = socket;
        _handler = handler;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;
 
        // Get the input and output streams, using temp objects because
        // member streams are final
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
            Log.v(TAG, "Streams in place");
        } catch (IOException e) { }
 
        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }
 
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
                /*
                _handler.obtainMessage(MESSAGE_READ , bytes, -1, buffer)
                        .sendToTarget();
                        */
                Message msg = new Message();
                msg.obj = new String(buffer);
    			_handler.sendMessage(msg);
    			Log.v(TAG, "Handler sent");
            } catch (IOException e) {
                break;
            }
        }
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
            mmSocket.close();
        } catch (IOException e) { }
    }
}
