#!/bin/sh

TEST_CLASSES="Test04 Test06 Test07 Test11 Test12 Test13 Test14 Test26 Test28 Test33"
OUT_FILE=regression.log
GOLD_FILE=regression.gold
export CLASSPATH=.

rm $OUT_FILE
for i in $TEST_CLASSES
do
  echo ------------------ Compiling $i
  lejosc $i.java
  echo ------------------ Linking $i
  emu-lejos $i -o $i.tvm
  echo ------------------ Running $i
  echo "----------------- Run of $i.tvm" >> $OUT_FILE
  emu-lejosrun $i.tvm >> $OUT_FILE
done

if [ ! -f $GOLD_FILE ]; 
then
  echo "##### CREATED GOLD FILE #####"
  cp $OUT_FILE $GOLD_FILE
else
  echo "##### BEGIN REGRESSION DIFF #####"
  diff $OUT_FILE $GOLD_FILE
  echo "#####  END REGRESSION DIFF  #####"
fi
