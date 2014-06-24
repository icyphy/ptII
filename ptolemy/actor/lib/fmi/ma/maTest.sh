#!/bin/bash
# Script to test the master algorithm with ptplot output
# ptplot is required to be in PATH

echo "Master Algorithm Test"
make run
$PTII/bin/ptplot result.csv