#!/bin/sh

set -e

if ! which android > /dev/null; then
    if [ -z $ANDROID_HOME ]; then
        if [ -e ~/.android/bashrc ]; then
            . ~/.android/bashrc
        else
            echo "'android' not found, ANDROID_HOME must be set!"
            exit
        fi
    else
        export PATH="${ANDROID_HOME}/tools:$PATH"
    fi
fi

projectname=`sed -n 's,.*name="app_name">\(.*\)<.*,\1,p' app/res/values/strings.xml`
# fetch target from project.properties
eval `grep '^target=' app/project.properties`

echo "Setting up build for $projectname"
echo ""

for f in `find * -name project.properties`; do
    projectdir=`dirname $f`
    echo "Updating ant setup in $projectdir:"
    android update lib-project -p $projectdir -t $target
done
android update project -p . --subprojects --name "$projectname" --target $target

cp external/InformaCam/libs/android-support-v4.jar external/ActionBarSherlock/actionbarsherlock/libs/android-support-v4.jar
cp external/InformaCam/libs/android-support-v4.jar external/InformaCam/external/OnionKit/libnetcipher/libs/android-support-v4.jar
cp external/InformaCam/libs/android-support-v4.jar external/InformaCam/external/CacheWord/cachewordlib/libs/android-support-v4.jar
cp external/InformaCam/libs/iocipher.jar external/InformaCam/external/CacheWord/cachewordlib/libs/iocipher.jar


rm -rf external/ActionBarSherlock/actionbarsherlock-samples
rm -rf app/bin/dexedLibs/android-support-v4*

