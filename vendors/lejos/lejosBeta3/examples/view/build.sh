#!/bin/sh

CLASSPATH=.
export CLASSPATH
lejosc View.java
lejos View -o View.bin

echo "Run 'lejosrun View.bin' to download the program."
