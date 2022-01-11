#!/bin/env -S ${SHELL}
# ©2022 Andreas Kielkopf
# License: `GNU General Public License v3.0`
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

 -h --help         help
 -c --color        colorize output unconditionally
 -k --kernel       installed kernels, initrd
 -l --list-all     all kernels (not only installed)  
 -v --kver         kernelversion 
 -m --modules      list modules and extramodules    
 -s --shasum       produce hash to compare kernel. modules
 -w --watch [100]  watch how everything changes with time 
 -g --grub         /boot/grub/grub.cfg, /etc/default/grub
 -i --mkinitcpio   /etc/mkinitcpio.conf
 -e --efi          efi bootloaders 
 
   default equals to: -km
 
EOF
