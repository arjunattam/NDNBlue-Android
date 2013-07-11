ANDROID_LIB = ${HOME}/ccnx-master/android/CCNx-Android-Lib/
JAVASRC = ${HOME}/ccnx-master/javasrc/

all:
	ant debug

setup:
	mkdir -p libs
	ln -s ${JAVASRC}/ccn.jar libs/ccn.jar
	ln -s ${ANDROID_LIB}/bin/classes.jar libs/ccnx-android-lib.jar
	android update project --name NDNBlue -p . -t android-14

clean:
	ant clean
	rm -f -r libs/ 
	rm -f  *.properties build.xml proguard.cfg
