#!/bin/bash

for f in $(find . -name "*.xml")
do
    python ./oneModelUpdate.py $f $f
done
	 
	
