#!/bin/sh

TEST_CLASSES="Test15"
export TINYVMPATH=.

for i in $TEST_CLASSES
do
  echo ------------------ Compiling $i
  tvmc $i.java
  echo ------------------ Linking $i
  tvmld $i -o $i.tvm
  echo ------------------ Running $i
  tvm $i.tvm
  # give it a few seconds
  sleep 17
done
