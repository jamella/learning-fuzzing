#!/bin/zsh
BUILD_DIR=build/classes/main
AFL_DIR=../afl

set -ex
./gradlew build
pushd $BUILD_DIR
javah net.praseodym.activelearner.AFL
popd
mv $BUILD_DIR/net_praseodym_activelearner_AFL.h $AFL_DIR/