NDNBlue
=======

NDNBlue extends CCNx on Android to run on Bluetooth.

### Prerequisites ###
* CCNx apps on Android should be installed and running
* Android SDK with ant build
* Recommended: Android version should be 4.0 or later.

### Configuration ###
* Edit first three lines of the Makefile with the locations of the jar files CCNx Android lib, CCNx Java source and Bouncy Castle (generally found in libs for CCNx Android Services app)
* Verify file names of jars in the Makefile
* Run
	+ make setup
	+ make
* Install apk on device using adb install
* Alternatively, use run.sh to install on all connected devices

### Usage ###
* Ensure that CCNx Services are running on the device
* Open app on device
* Enter prefix
* Start server or client. Clients can also discover nearby Bluetooth enabled devices.

### Related ###
* NDNBlue also works on BlueZ on Linux machines.