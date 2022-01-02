#!/bin/env -S ${SHELL}
#(C) 2022 Andreas Kielkopf
nice java -jar "$0" "$@"
EC="$?"
[ "$EC" = 0 ] && exit;
echo -n "$EC ==>"
[ "$EC" = 127 ] && echo "$0 needs a java runntime"
[ "$EC" = 1 ] && echo "$0 needs a java runtime with at least version 1.8"
{cat;exit;}  <<EOF

Usage:
------
$0 [OPTIONS]

 -l list all kernels [not only installed]
 -m list also modules and extramodules 
 -c hash kernel and modules directory
 -w watch how everything changes
 -g show infos about grub
 -e show efi bootloaders
 
EOF
