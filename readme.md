# maxi
If your manjaro installation does not boot (any more), maxi may help to find what is missing. 
Just use it from commandline or from a chroot environment. If you need help interpreting the output, read [Understand the boot process](https://forum.manjaro.org/t/howto-understand-efi-boot-process-with-gpt-via-grub-to-manjaro/99740) or contact a forum like [manjaro forum](https://forum.manjaro.org/) and ask for help.

 `sudo wget https://github.com/andreaskielkopf/maxi/raw/java_21/jar/maxi -O /usr/local/bin/maxi; sudo chmod a+x /usr/local/bin/maxi; maxi -kmsgeif` 

## Description
A tool to analyze why a Manjaro installation might not start.

The program collects information about the current installation (it can also work inside `chroot`). It uses `coreutils (ls cat sort du sha256sum), tput, find` and `mhwd-kernel` to collect information.

The program should be a helpful hand to collect a lot of small information (like `inxi` does) and present it in a short overview. So that the experienced reader can decide what could be the problem with this installation and how to proceed without wasting time.

While `inxi` focuses on a system overview, `maxi` focuses on things between "power_on" and "kernel_is_running"

It displays in short form:
 * kernels at /boot/vmlinuz... (size, sha256)
 * initramdisks/fallback present (size, sha256)
 * modules at /lib/modules/... (size, sha256)
 * extramodules at /lib/modules/extra... (size, sha256)
 * grub, grub.cfg, efivars ...

```
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
 * -p --partitions   info about visible partitions
 
   default equals to: -km
```

### Project Members
- ©2022 [Andreas Kielkopf](https://github.com/andreaskielkopf)

### Depends on
- `java-21-openjdk`
- `mhwd` (mhwd-kernel)
- `coreutils` (ls cat sort du sha256sum)
- `findutils` (find)
- `ncurses` (tput) or zsh (echoti)
- `lsblk`

<img src="needsJava21.png" width=7% height=7%>

License: The license is `GNU General Public License v3.0`

#### ToDos:
 * show connected boot-stanzas
 
And i hope there are [other wishes](https://forum.manjaro.org/t/maxi-call-for-testers-0-6-7/99763) to extend the program, so that it is an aid to resolve boot-problems fast.

## Install:

1. Download maxi from [github](https://github.com/andreaskielkopf/maxi) and save it where you want
    `sudo wget https://github.com/andreaskielkopf/maxi/raw/java_21/jar/maxi -O /usr/local/bin/maxi`

2. Check the sha256sum

   `sha256sum /usr/local/bin/maxi`

3. Make the file executable

   `sudo chmod -c a+x /usr/local/bin/maxi`

All at once:

   `sudo wget https://github.com/andreaskielkopf/maxi/raw/java_21/jar/maxi -O /usr/local/bin/maxi; sha256sum /usr/local/bin/maxi; sudo chmod -c a+x /usr/local/bin/maxi` 

## Use it:

1. `maxi -kmf` or `maxi -kmsgeif`
1. The output is already in the clipoboard. If you want to post it in [manjaro forum](https://forum.manjaro.org/) you only have to create the post, and press `[Ctrl-v]`
1. You may also have a look at [Understand the boot process](https://forum.manjaro.org/t/howto-understand-efi-boot-process-with-gpt-via-grub-to-manjaro/99740)

#### sha256sum of V0.7.18 (17.12.2024)
ae2ef03e8a9c1d0dad818438cd01f11e656ea53834be105f6ddbf4b5a9ab3949  maxi

P.S. If current developments trouble you, you can 
[Find Peace](https://www.jw.org/en/library/series/more-topics/russia-invades-ukraine-bible-meaning-hope/) -
[Find Real Hope](https://www.jw.org/en/jehovahs-witnesses/memorial/)

--------------

# maxi
Wenn deine Manjaro-Installation nicht (mehr) bootet, kann `maxi` helfen, heraus zu finden, was fehlt.
Verwende es einfach von der Befehlszeile oder von einer `chroot`-Umgebung aus. Wenn du Hilfe beim Interpretieren der Ausgabe benötigst, lies [Den Boot-Prozess verstehen](https://forum.manjaro.org/t/howto-understand-efi-boot-process-with-gpt-via-grub-to-manjaro/99740) oder kontaktiere ein Forum wie [manjaro forum](https://forum.manjaro.org/) und bitte um Hilfe.

 `sudo wget https://github.com/andreaskielkopf/maxi/raw/java__21/jar/maxi -O /usr/local/bin/maxi; sudo chmod a+x /usr/local/bin/maxi ; maxi -kmsgeif`

## Beschreibung
Ein Tool zur Analyse, warum eine Manjaro-Installation möglicherweise nicht startet.

Das Programm sammelt Informationen über die aktuelle Installation (es kann auch innerhalb einer `Chroot`-Umgebung funktionieren). Es verwendet `coreutils` (ls cat sort du sha256sum), `tput`, `find` und `mhwd-kernel`, um diese Informationen zu sammeln.

Das Programm soll eine helfende Hand sein, um viele kleine Informationen zu sammeln (wie es `inxi` tut) und in einer kurzen Übersicht darzustellen. Damit der erfahrene Leser ohne Zeitverlust entscheiden kann, wo das Problem bei dieser Installation liegen könnte und wie er vorgehen soll.

Während sich `inxi` auf eine Systemübersicht konzentriert, konzentriert sich `maxi` auf Dinge zwischen "power_on" und "kernel_is_running"

Es zeigt in Kurzform:

 * Kernel unter /boot/vmlinuz... (Größe, sha256)
 * Initramdisks/Fallback vorhanden (Größe, sha256)
 * Module unter /lib/modules/... (Größe, sha256)
 * Extramodule unter /lib/modules/extra... (Größe, sha256)
 * grub, grub.cfg, efivars ...


#### Verwendung:
Maxi [OPTIONEN]
 * -h --help         Hilfe
 * -c --color        Ausgabe bedingungslos einfärben
 * -k --kernel       installierte Kernel, initrd, chroot
 * -l --list-all     alle Kernel (nicht nur installierte)
 * -v --kver         Kernelversion (enthält -k)
 * -m --modules      listet Module und Extramodule auf
 * -s --shasum       erzeugt einen kurzen Hash, um Kernel und Module zu vergleichen
 * -w --watch [100]  beobachte, wie sich alles mit der Zeit verändert
 * -g --grub         /boot/grub/grub.cfg, /etc/default/grub
 * -i --mkinitcpio   /etc/mkinitcpio.conf
 * -e --efi          EFI-Bootloader
 * -f --forum        Frame mit Backticks und [Details] in die Zwischenablage kopieren
 * -p --partitions   Info über sichtbare Partitionen
 
   Standardwert ist: -km
 

#### Projektmitglieder
- ©2023,2025 [Andreas Kielkopf](https://github.com/andreaskielkopf)

#### Abhängig von:
- `java-21-openjdk`
- `mhwd` (mhwd-Kernel)
- `coreutils` (ls cat sort du sha256sum)
- `findutils` (finden)
- `ncurses` (tput) oder zsh (echoti)


#### Lizenz
Die Lizenz lautet „GNU General Public License v3.0“.


#### Aufgaben:
 * verbundene Boot-Programme anzeigen
 
Und ich hoffe, es gibt [weiter Wünsche](https://forum.manjaro.org/t/maxi-call-for-testers-0-6-7/99763) um das Programm zu erweitern, so dass es eine Hilfe ist Boot-Probleme schnell lösen.

### Installieren:
1. Laden Sie Maxi von [github](https://github.com/andreaskielkopf/maxi) herunter und speichern Sie es, wo Sie möchten

 `sudo wget https://github.com/andreaskielkopf/maxi/raw/java_21/jar/maxi -O /usr/local/bin/maxi`
 
2. Überprüfen Sie die Prüfsumme
 
 `sha256sum /usr/local/bin/maxi`
 
3. Machen Sie die Datei ausführbar
 
 `sudo chmod -c a+x /usr/local/bin/maxi`

 `sudo wget https://github.com/andreaskielkopf/maxi/raw/java_21/jar/maxi -O /usr/local/bin/maxi; sha256sum /usr/local/bin/maxi; sudo chmod -c a+x /usr/local/bin/maxi`

#### Benutzung:
1. `maxi -kmf` oder `maxi -kmsgeif`
2. Die Ausgabe befindet sich danach bereits in der Zwischenablage. Wenn Sie sie im [manjaro-Forum](https://forum.manjaro.org/) posten möchten, müssen Sie nur den Beitrag erstellen und `[Strg-v]` drücken
3. Sie können sich auch [Verstehe den boot-prozess](https://forum.manjaro.org/t/howto-understand-efi-boot-process-with-gpt-via-grub-to-manjaro/99740) ansehen

P.S. Wenn dich die aktuellen Entwicklungen beunruhigen kannst du 
[Frieden finden](https://www.jw.org/de/bibliothek/artikelserien/weitere-themen/russland-marschiert-in-ukraine-ein-biblische-bedeutung-hoffnung/) -
[Eine echte Hoffnung](https://www.jw.org/de/jehovas-zeugen/erinnerung-an-jesu-tod/)
