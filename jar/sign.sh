#!/bin/env -S ${SHELL}
# ©2025 Andreas Kielkopf
# License: `GNU General Public License v3.0`
B=maxi
unset  _JAVA_OPTIONS
#echo "Joining $B"
jarsigner -tsa http://timestamp.digicert.com/tsr $B.jar andreas &&
cat ./$B.sh ./$B.jar > ./$B &&
ls -lA ./$B &&
chmod a+x ./$B &&
/usr/lib/jvm/java-21-openjdk/bin/jarsigner -verbose -verify ./$B &&
sha256sum ./$B