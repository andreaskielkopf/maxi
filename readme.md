maxi
====
If your manjaro installation does not boot (any more), maxi may help to find what is missing. 
Just use it from commandline or from a chroot environment. If you need help interpreting the output, read [Understand the boot process](https://forum.manjaro.org/t/howto-understand-efi-boot-process-with-gpt-via-grub-to-manjaro/99740) or contact a forum like [manjaro forum](https://forum.manjaro.org/) and ask for help.

 `wget https://github.com/andreaskielkopf/maxi/raw/master/maxi ; chmod a+x maxi ; ./maxi -kmsgeif` 

Description
-----------

A tool to analyze why a Manjaro installation might not start.

The program collects information about the current installation (it can also work inside the chroot). It uses coreutils (ls cat sort du sha256sum), tput, find and mhwd-kernel to collect this information.

The program should be a helpful hand to collect a lot of small information (like inxi does) and present it in a short overview. So that the experienced reader can decide what could be the problem with this installation and how to proceed without wasting time.

While inxi focuses on a system overview, `maxi` only focuses on things between "power_on" and "kernel_is_running"

It displays in short form:

 * kernels at /boot/vmlinuz... (size, sha256)
 * initramdisks/fallback present (size, sha256)
 * modules at /lib/modules/... (size, sha256)
 * extramodules at /lib/modules/extra... (size, sha256)
 * grub, grub.cfg, efivars ...


Usage:
------
maxi [OPTIONS]

 * -h --help         help
 * -c --color        colorize output unconditionally
 * -k --kernel       installed kernels, initrd, chroot
 * -l --list-all     all kernels (not only installed)
 * -v --kver         kernelversion (includes -k)
 * -m --modules      list modules and extramodules
 * -s --shasum       produce short hash to compare kernel & modules
 * -w --watch [100]  watch how everything changes over time 
 * -g --grub         /boot/grub/grub.cfg, /etc/default/grub
 * -i --mkinitcpio   /etc/mkinitcpio.conf
 * -e --efi          efi bootloaders 
 * -f --forum        frame with backticks and [details] and copy to clipboard
 
   default equals to: -km
 

Project Members
---------------

- ©2022 [Andreas Kielkopf](https://github.com/andreaskielkopf)


Depends on
----------

- `java` (1.8 or any newer)
- `mhwd` (mhwd-kernel)
- `coreutils` (ls cat sort du sha256sum)
- `findutils` (find)
- `ncurses` (tput) or zsh (echoti)


License
-------
The license is `GNU General Public License v3.0`


ToDos:
------

 * list presets for mkinitcpio
 * schow boot-stanzas from UEFI
 * find all EFI-Partitions
 * show connected boot-stanzas
 * show all grub.cfg
 
And i hope there are [other wishes](https://forum.manjaro.org/t/maxi-call-for-testers-0-6-7/99763) to extend the program, so that it is an aid to resolve boot-problems fast.

Install:
--------
1. Download maxi from [github](https://github.com/andreaskielkopf/maxi) and save it where you want `wget https://github.com/andreaskielkopf/maxi/raw/master/maxi`
2. Check the sha256sum `sha256sum maxi`
3. Make the file executable `chmod -c a+x ./maxi`

 `wget https://github.com/andreaskielkopf/maxi/raw/master/maxi ; sha256sum maxi ; chmod -c a+x maxi` 

Use it:
-------
1. `./maxi -kmf**` or `**./maxi -kmsgeif**`
3. The output is already in the clipoboard. If you want to post it in [manjaro forum](https://forum.manjaro.org/) you only have to create the post, and press `[Ctrl-v]`
4. You may also have a look at [Understand the boot process](https://forum.manjaro.org/t/howto-understand-efi-boot-process-with-gpt-via-grub-to-manjaro/99740)
   
  
  

#### sha256sum of v0.6.8
8f176630cfa49bb8c98e4f5bedd0af1066c5f2fa761a8f37f8332640c9a7ce9f  maxi


