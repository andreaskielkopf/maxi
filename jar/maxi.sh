#!/bin/env -S bash
# ©2023 Andreas Kielkopf
# License: `GNU General Public License v3.0`
java -jar "$0" "$@"
EC="$?"
[ "$EC" = 0 ] && exit;
echo -n "$EC ==>"
[ "$EC" = 127 ] && echo "$0 needs a java runntime"
[ "$EC" = 1 ] && echo "$0 needs a java runtime with at least version 1.8"
{ cat; exit; } <<EOF

Maxi is made for manjaro linux only
This is version 0.7.7 from 1.1.2024

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
 
   default equals to: -km
   
   compare sha256sum $0 at: https://github.com/andreaskielkopf/maxi
   
EOF
