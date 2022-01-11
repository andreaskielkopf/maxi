maxi
====

Description
-----------

A tool to help analysing why a manjaro installation might not boot.

The program collects information of the actual install (may also work inside chroot). It uses coreutils (ls cat sort du sha256sum), tput, find and mhwd-kernel to gather these informations.

The program is intended to be a helpful hand to gather a lot of small info (as inxi does) and present it in a short overview. So that the insightful reader can decide what may be the problem with this installation, and how to proceed without wasting to much time.

While inxi concentrates on a system overview, this program concentrates only on things between power_on and kernel_is_running


It displays in short form for every kernel:

 * is a kernel at /boot/vmlinuz... present
 * is a initramdisk present (show size)
 * is a fallback present
 * are the modules at /lib/modules/... present and complete (size)
 * are extramodules present (size)


Usage:
------
maxi [OPTIONS]

 * -h --help         help
 * -c --color        colorize output unconditionally
 * -k --kernel       installed kernels, initrd
 * -l --list-all     all kernels (not only installed)
 * -v --kver         kernelversion (includes -k)
 * -m --modules      list modules and extramodules    
 * -s --shasum       produce hash to compare kernel. modules
 * -w --watch [100]  watch how everything changes with time 
 * -g --grub         /boot/grub/grub.cfg, /etc/default/grub
 * -i --mkinitcpio   /etc/mkinitcpio.conf
 * -e --efi          efi bootloaders 
 
   default equals to: -km
 
@todo:
   ? 

Project Members
---------------

- ©2022 [Andreas Kielkopf](https://github.com/andreaskielkopf)


Depends on
----------

- java (1.8 or any newer)
- mhwd (mhwd-kernel)
- coreutils (ls cat sort du sha256sum)
- findutils (find)
- ncurses (tput) or zsh (echoti)


License
-------
The license is `GNU General Public License v3.0`


ToDos:
------

 * list presets for mkinitcpio?


And i hope there are other wishes to extend the program, so that it is an aid to resolve boot-problems fast.