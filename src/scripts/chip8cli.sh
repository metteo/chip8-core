#!/bin/bash

/home/metteo/Apps/jdk-11.0.5+10/bin/java \
  -ea \
  -Dfile.encoding=UTF-8 \
  -p /home/metteo/Repos/chip8-core/target/classes:\
/home/metteo/.m2/repository/com/google/dagger/dagger/2.26/dagger-2.26.jar:\
/home/metteo/.m2/repository/javax/inject/javax.inject/1/javax.inject-1.jar:\
/home/metteo/.m2/repository/com/googlecode/lanterna/lanterna/3.0.2/lanterna-3.0.2.jar:\
/home/metteo/.m2/repository/com/google/code/findbugs/jsr305/3.0.2/jsr305-3.0.2.jar:\
/home/metteo/.m2/repository/org/checkerframework/checker-qual/3.1.0/checker-qual-3.1.0.jar \
  -m net.novaware.chip8.core/net.novaware.chip8.cli.Chip8Cli "$@"

#  -agentlib:jdwp=transport=dt_socket,address=127.0.0.1:43875,suspend=y,server=y \
#  -javaagent:/home/metteo/Apps/idea-IU-193.6015.39/plugins/java/lib/rt/debugger-agent.jar \