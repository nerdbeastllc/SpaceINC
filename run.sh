#!/bin/bash
set -e
cd "$(dirname "$0")"
./build.sh
JAVA25="$HOME/.sdkman/candidates/java/25.0.5.0+8-zulu/bin"
"$JAVA25/java" -XstartOnFirstThread --enable-native-access=ALL-UNNAMED -cp "out:lib/*" spaceinc.Main
