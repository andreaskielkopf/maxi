#!/bin/env -S ${SHELL}
# ©2022 Andreas Kielkopf
# License: `GNU General Public License v3.0`
B=maxi
unset  _JAVA_OPTIONS
#echo "Joining $B"
jarsigner -tsa https://freetsa.org/tsr $B.jar andreas
cat ./$B.sh ./$B.jar > ./$B
ls -lA ./$B
chmod a+x ./$B
jarsigner -verbose -verify ./$B