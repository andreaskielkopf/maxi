#!/bin/env -S bash
# ©2024 Andreas Kielkopf
# License: `GNU General Public License v3.0`
java -jar "$0" "$@"
EC="$?"
[ "$EC" = 0 ] && exit;
echo -n "$EC ==>"
[ "$EC" = 127 ] && echo "$0 needs a java runntime"
[ "$EC" = 1 ] && echo "$0 needs a java runtime with version 21"
{ cat; exit; } <<EOF

$0 [OPTIONS] $@
Maxi is made for manjaro linux
 
   default equals to: -km
   
 For sources see@ https://github.com/andreaskielkopf/Maxi and inside this file   
EOF
