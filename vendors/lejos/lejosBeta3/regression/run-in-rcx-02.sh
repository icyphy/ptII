#!/bin/sh

TEST_CLASSES="Test17"
export TINYVMPATH=.

for i in $TEST_CLASSES
do
  echo ------------------ Compiling $i
  tvmc $i.java
  echo ------------------ Linking $i
  tvmld $i -o $i.tvm
  echo ------------------ Running $i
  tvm $i.tvm
done
