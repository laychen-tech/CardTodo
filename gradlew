#!/bin/sh
# Gradle wrapper script
JAVA_HOME="${JAVA_HOME:-/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home}"
export JAVA_HOME
exec "$JAVA_HOME/bin/java" \
  -classpath "$(dirname "$0")/gradle/wrapper/gradle-wrapper.jar" \
  org.gradle.wrapper.GradleWrapperMain "$@"
