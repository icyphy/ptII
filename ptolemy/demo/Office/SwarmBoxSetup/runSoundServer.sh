#!/bin/sh
# Usage:
# pm2 start ./runSoundServer.sh

# Disable auto-update of accessors repo
export PT_NO_NET=true

# Sleep so that the KV store has a chance of running
echo "$0: Sleeping for 13 seconds: `date`"
sleep 13

model=$PTII/org/terraswarm/accessor/demo/AugmentedRealityVideoSOHO/SoundServerSOHO.xml
echo "$0: about to start sound server $model: `date`"
echo "$0: about to start sound server $model: `date`" 1>&2
$PTII/bin/ptinvoke ptolemy.moml.MoMLSimpleApplication $model
echo "$0: sound server exited: `date`"
echo "$0: sound server exited: `date`" 1>&2
