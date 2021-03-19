#!/usr/bin/env bash 
# Copyright 2021 (c) all rights reserved 
# by SDRausty https://sdrausty.github.io
#####################################################################
set -Eeuo pipefail
shopt -s nullglob globstar
. "$RDR"/scripts/bash/shlibs/trap.bash 210 211 212
cd "$JDR"
# _AT_ username/repository commit
_AT_ xdtianyu/MyBrowser 7a6a0188e958c0eb9d01d67710ae1f9cc918aa5a
# ma.bash OEF
