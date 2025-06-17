//package de.uhingen.kielkopf.andreas.maxi;

// #!/usr/bin/env python
// import gzip
// from pathlib import Path
// import platform
// import re
// import subprocess
// import sys
// from urllib import request, error
//
//

/**
 * 
 * @author Andreas Kielkopf This is a java conversion of a script of papajoke and tracyanne @ manjaro forum
 *         {@link https://forum.manjaro.org/t/what-kernel-does-manjaro-use/178933/16}l
 */
//public class Papajoke {
void main() {
   IO.println("Hello papajoke and tracyanne !");
   // class Kernel:
   // def __init__(self, pkg_name):
   // self.name = pkg_name
   // self.is_rt = False
   // self.major, self.minor = self._parse_version(pkg_name)
   // self.version = f"{self.major}.{self.minor}"
   //
   // self.installed = False
   // self.recommended = False
   // self.lts = False
   // self.eol = False
   //
   // def _parse_version(self, name):
   // name = name.removeprefix("linux")
   // if name.endswith("-rt"):
   // self.is_rt = True
   // name = name.removesuffix("-rt")
   // return int(name[0]), int(name[1:])
   //
   // def get_version(self) -> str:
   // ver = f"{self.major}.{self.minor}"
   // return f"{ver:4} {'LTS' if self.lts else ' ' * 3}"
   //
   // def __str__(self) -> str:
   // rec = "*" if self.recommended else " "
   // inst = "(i)" if self.installed else " " * 3
   // eol = "EOL" if self.eol else " " * 3
   // return f"{rec} {self.name:12} {self.get_version()} {inst} {eol}"
   //
   // def __lt__(self, other: "Kernel"):
   // """for good sort"""
   // if self.is_rt != other.is_rt:
   // return self.is_rt and not other.is_rt
   // a, b = self.major, other.major
   // if a != b:
   // return a < b
   // a, b = self.minor, other.minor
   // if a != b:
   // return a < b
   //
   //
   // class EolManager:
   // DB_FILE = Path("/tmp/core.db")
   //
   // def __init__(self):
   // self.state = 0
   // self.unstable = []
   // self.kernels = []
   // if self.download():
   // self.state = 1
   // self.unstable = sorted(self.filter_kernels(self.get_pkgs(self.DB_FILE)))
   // if not self.unstable:
   // return
   //
   // def __del__(self):
   // self.DB_FILE.unlink(True)
   // pass
   //
   // def get_eol(self) -> tuple[str]:
   // if not self.state or not self.unstable:
   // return []
   // if not self.kernels:
   // self.kernels = sorted(list(self._get_current_kernels()))
   // # print("# --dev : for test, add fake EOL kernels : 66 and 54\n")
   // if "--dev" in sys.argv:
   // if i := self.unstable.index("linux66"):
   // del self.unstable[i]
   // if i := self.unstable.index("linux54"):
   // del self.unstable[i]
   //
   // return tuple(k for k in self.kernels if k not in self.unstable)
   //
   // def download(self) -> bool:
   // if self.DB_FILE.exists():
   // return True
   // url = self._get_url()
   // if not url:
   // return False
   // try:
   // with request.urlopen(url, timeout=4) as response:
   // with open(self.DB_FILE, "wb") as download:
   // download.write(response.read())
   // # print(" #", self.DB_FILE, "downloaded")
   // return True
   // except Exception:
   // return False
   //
   // @staticmethod
   // def _get_current_kernels():
   // output = subprocess.run(
   // "pacman -Ssq | grep -E '^linux[0-9]{2,3}(-rt)?$'", capture_output=True, shell=True, text=True, timeout=3
   // ).stdout
   // return output.splitlines()
   //
   // @staticmethod
   // def get_pkgs(file_):
   // """parse pacman database gz file"""
   // """
   // %NAME%
   // linux612
   // %VERSION%
   // 6.12.32-1
   // %BUILDDATE%
   // 1749062743
   // """
   // with gzip.open(file_, "rt") as f:
   // for line in f:
   // if not line.startswith("%NAME%"):
   // continue
   // name = next(f)
   // if not name.startswith("linux"):
   // continue
   // yield name.rstrip()
   //
   // @staticmethod
   // def filter_kernels(names):
   // # filter only kernels
   // reg = re.compile(r"^linux[0-9]{2,3}(-rt)?$")
   // yield from (n for n in names if reg.match(n))
   //
   // @staticmethod
   // def _get_url() -> str:
   // # search unstable url for core
   // output = subprocess.run(
   // "pacman-conf -r core | grep '^Server' -m1", capture_output=True, shell=True, text=True, timeout=3
   // ).stdout
   // if not output or "/unstable/" in output:
   // return None
   // url = output.split("=")[1].strip()
   // url = url.replace("/stable/", "/unstable/")
   // url = url.replace("/testing/", "/unstable/")
   // return f"{url}/core.db"
   //
   //
   // def get_kernels() -> dict[str, Kernel]:
   // reg = r"LANG=en pacman -Si | grep -E '^Name' | grep -E 'Name.*linux[0-9]{2,3}(-rt)?$' -A1"
   //
   // output = subprocess.run(reg, capture_output=True, shell=True, text=True, timeout=30).stdout
   // lines = iter(l for l in output.split("\n") if l and not l.startswith("--"))
   // kernels = {}
   // for name, version in zip(lines, lines):
   // name = name.split(":")[1].strip()
   // kernels[name] = Kernel(name)
   // return kernels
   //
   //
   // def get_installeds() -> dict[str, Kernel]:
   // reg = r"pacman -Qq | grep -E '^linux[0-9]{2,3}(-rt)?$'"
   // output = subprocess.run(reg, capture_output=True, shell=True, text=True, timeout=30).stdout
   // return [l.strip() for l in output.split("\n")]
   //
   //
   // def get_lts_from_kernel_org() -> list[str, str]:
   // import xml.etree.ElementTree as ET
   //
   // results = []
   // with request.urlopen(
   // "https://www.kernel.org/feeds/kdist.xml",
   // timeout=3,
   // ) as response:
   // root = ET.fromstring(response.read().decode("utf-8"))[0]
   // for title in root.iter("title"):
   // if "longterm" not in title.text:
   // continue
   // ver = title.text.split(".")[0:2]
   // results.append(f"linux{ver[0]}{ver[1]}")
   // return results
   //
   //
   // def get_kernels_from_gitlab() -> dict[str, list[str]]:
   // """read manjaro datas from gitlab .c file"""
   //
   // def filter_line(line) -> bool:
   // if "::getLtsKernels" in line or "::getRecommendedKernels" in line:
   // return True
   // if "return QStringList() <<" in line:
   // return True
   // return False
   //
   // results = []
   // with request.urlopen(
   // "https://gitlab.manjaro.org/applications/manjaro-settings-manager/-/raw/master/src/libmsm/KernelModel.cpp",
   // timeout=3,
   // ) as response:
   // content = [l.replace('"', "").replace(";", "") for l in response.read().decode("utf-8").split("\n") if filter_line(l)]
   // results = [k.strip() for k in content[3].split("<<")[1:]]
   // return results
   //
   //
   // def sort_kernels(kernels):
   // result = {}
   // for k in sorted(kernels.values(), reverse=True):
   // result[k.name] = k
   // return result
   //
   //
   // if platform.freedesktop_os_release()["ID"].lower() != "manjaro":
   // exit(66)
   // if platform.machine() != "x86_64":
   // exit(67)
   //
   // if __name__ == "__main__":
   // print("# recommended, package, version, lts, installed, eol")
   // print()
   // kernels = get_kernels()
   //
   // lts = get_lts_from_kernel_org()
   // for kernel_lts in lts:
   // if kernel := kernels.get(kernel_lts, None):
   // kernel.lts = True
   //
   // for kernel_rec in get_kernels_from_gitlab():
   // if kernel := kernels.get(kernel_rec, None):
   // kernel.recommended = True
   //
   // for kernel_inst in get_installeds():
   // if kernel := kernels.get(kernel_inst, None):
   // kernel.installed = True
   //
   // manager = EolManager()
   // for kernel_eol in manager.get_eol():
   // if kernel := kernels.get(kernel_eol, None):
   // kernel.eol = True
   //
   // kernels = sort_kernels(kernels)
   // for kernel in kernels.values():
   // print(kernel)
}
