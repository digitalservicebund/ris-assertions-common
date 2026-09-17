#!/bin/bash
# SDKMAN users
export SDKMAN_DIR="$HOME/.sdkman"
[ -s "$SDKMAN_DIR/bin/sdkman-init.sh" ] && . "$SDKMAN_DIR/bin/sdkman-init.sh"

# Fallback: JAVA_HOME already set or java already on PATH — do nothing
./gradlew spotlessApply