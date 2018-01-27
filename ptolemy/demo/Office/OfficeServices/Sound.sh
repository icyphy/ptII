#!/bin/sh
# Usage:
# pm2 start Sound.sh

# Disable auto-update of accessors repo
export PT_NO_NET=true

# Sleep so that the KV store has a chance of running
echo "$0: Sleeping for 13 seconds: `date`"
sleep 13

model=$PTII/ptolemy/demo/Office/OfficeServices/Sound.xml
echo "$0: about to start $model: `date`"
echo "$0: about to start $model: `date`" 1>&2
$PTII/bin/ptinvoke ptolemy.moml.MoMLSimpleApplication $model
echo "$0: $model exited: `date`"
echo "$0: $model exited: `date`" 1>&2
