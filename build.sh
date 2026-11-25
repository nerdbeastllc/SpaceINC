#!/bin/bash
set -e
cd "$(dirname "$0")"
mkdir -p out
JAVA25="$HOME/.sdkman/candidates/java/25.0.5.0+8-zulu/bin"
"$JAVA25/javac" --release 25 -cp "lib/*" -d out $(find src/main/java -name "*.java")
cp -r src/main/resources/* out/
echo "Build succeeded."
