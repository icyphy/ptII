#!/bin/sh
# Usage:
# pm2 start ~/runKVStore.sh
#
# Disable updating of the accessors repo.
export PT_NO_NET=true

echo "$0: Sleeping for 3 seconds: `date`"
sleep 3

model=$PTII/org/terraswarm/accessor/demo/AugmentedRealityVideoSOHO/KeyValueStoreServerSOHO.xml
echo "$0: about to start sound server $model: `date`"
echo "$0: about to start sound server $model: `date`" 1>&2
$PTII/bin/ptinvoke ptolemy.moml.MoMLSimpleApplication $model
echo "$0: sound server exited: `date`"
echo "$0: sound server exited: `date`" 1>&2

