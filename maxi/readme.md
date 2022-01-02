maxi
====

Description
-----------

A tool to help analysing why a manjaro installation might not boot.

The program collects information of the actual install (may also work inside chroot). It uses coreutils (ls cat sort du sha256sum), find and mhwd-kernel to gather these informations.

The program is intended to be a helpful hand to gather a lot of small info (as inxi does) and present it in a short overview. So that the insightful reader can decide what may be the problem with this installation, and how to proceed without wasting to much time.

While inxi concentrates on a system overview, this program concentrates only on things between power on and kernel is running


It displays in short form for every kernel:

 * is the kernel at /boot/vmlinuz... present
 * is a initramdisk present (show date)
 * is a fallback present
 * are the modules at /lib/modules/... present and complete
 * are extramodules present

Usage:
------
maxi [OPTIONS]

 *    list by mhwd-kernel installed kernels
 * -l list all kernels (not only installed) 
 * -w watch how everything changes
 
@todo:
 * -m list also modules and extramodules   
 * -c hash kernel and modules directory
 * -g show infos about grub
 * -e show efi bootloaders

Project Members
---------------

- [Andreas Kielkopf](https://gitlab.com/Arisa_Snowbell)

Depends on
----------

- java (1.8 or any newer)
- mhwd (mhwd-kernel)
- coreutils (ls cat sort du sha256sum
- findutils (find)
- ncurses (tput)

License
-------
The license is `GNU General Public License v3.0`

ToDos:
------

 * is /etc/default/grub present
 * is /boot/grub/grub.cfg present
 * is grub.cfg newer than grub

 * is /boot/efi or /efi present
 * list all *.efi there (show date)

And i hope there are other wishes to extend the program, so that it is an aid to resolve boot-problems fast.