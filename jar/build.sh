#!/bin/env -S ${SHELL}
# ©2022 Andreas Kielkopf
# License: `GNU General Public License v3.0`
B=maxi
unset  _JAVA_OPTIONS
#echo "Joining $B"
cat ./$B.sh ./$B.jar > ./$B
ls -lA ./$B