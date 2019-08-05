#!/bin/sh

if [ -z $ANDROID_NDK_HOME ]; then
    if which ndk-build 2>&1 /dev/null; then
        ANDROID_NDK_HOME=`which ndk-build |  sed 's,/ndk-build,,'`
    else
        echo "ANDROID_NDK_HOME not set and 'ndk-build' not in PATH"
        exit
    fi
fi

##make -C external/InformaCam/external/IOCipher/external
##ndk-build -C external/InformaCam/external/IOCipher
${ANDROID_NDK_HOME}/ndk-build -C external/InformaCam

cd external/InformaCam/external/android-ffmpeg-java/external/android-ffmpeg/
./configure_make_everything.sh
