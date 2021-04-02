#!/usr/bin/env bash 
# Copyright 2021 (c) all rights reserved 
# by SDRausty https://sdrausty.github.io
#####################################################################
set -Eeuo pipefail
shopt -s nullglob globstar
. "$RDR"/scripts/bash/shlibs/trap.bash 210 211 212
cd "$JDR"
# _AT_ username/repository commit
_AT_ Sjith/Magellan a3cfe75ec31c9cff917bd68d9a476a8416f30d89
printf "\\n%s\\n" "<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />" Sjith/Magellan/Sjith-Magellan-a3cfe75/AndroidManifest.xml
_AT_ ch-ms/See 45ee117937edd6b7271e440fc91e97c16c1c1340
sed -i 's/my awesome blog at //g' ch-ms/See/ch-ms-See-45ee117/assets/about.html
sed -i 's/forallx.ru/www.github.com\/buildAPKs/g' ch-ms/See/ch-ms-See-45ee117/assets/about.html
_AT_ eonj/Android-Resource-Browser 6b56a5090fb67c65a945d29a4bcdbab3cb4d8e69
_AT_ jrudolph/object-browser 9c3adfa87fb3f295f5c50972cea14211d7706b42
_AT_ kalkin/FileBrowser 714d1e037b88781c40e1d9481c668f69d5096eb2
_AT_ palazzem/AndroidBrowser 45ea4000bee5702c593d50d5ef3fb0cf67fb6044
sed -i 's/unipg.it/github.com\/buildAPKs/g' palazzem/AndroidBrowser/palazzem-AndroidBrowser-45ea400/src/res/values/strings.xml
[ -f "$RDR/opt/pic.png" ] && cp -r "$RDR/opt/pic.png" palazzem/AndroidBrowser/palazzem-AndroidBrowser-45ea400/src/res/drawable/logo_unipg.png
_AT_ scott-steadman/android-demo f39147045b71480f5f8d5e86208d7ccd8bf10550
_AT_ vaal12/AndroidFileBrowser 16d913ba03c2916897cd5076fbda6e5c33927091
_AT_ xdtianyu/MyBrowser 7a6a0188e958c0eb9d01d67710ae1f9cc918aa5a
sed -i 's/google.com.hk/github.com\/buildAPKs/g' xdtianyu/MyBrowser/xdtianyu-MyBrowser-7a6a018/res/values/strings.xml
# ma.bash OEF
