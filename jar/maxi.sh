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

Maxi is made for manjaro linux

Usage:
------
$0 [OPTIONS]

 -h --help         help
 -c --color        colorize output unconditionally
 -k --kernel       installed kernels, initrd, chroot
 -l --list-all     all kernels (not only installed)  
 -v --kver         kernelversion (includes -k)
 -m --modules      list modules and extramodules    
 -s --shasum       produce short hash to compare kernel & modules
 -w --watch [100]  watch how everything changes over time 
 -g --grub         /boot/grub/grub.cfg, /etc/default/grub
 -i --mkinitcpio   /etc/mkinitcpio.conf
 -e --efi          efi bootloaders 
 -f --forum        frame with backticks and [details] and copy to clipboard
 -p --partitions   info about visible partitions
 
   default equals to: -km
   
 compare sha256sum $0 at: https://github.com/andreaskielkopf/maxi
   
 For sources see@ https://github.com/andreaskielkopf/Maxi and inside this file   
EOF
