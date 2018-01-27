#!/bin/sh
# Usage:
# pm2 start KeyValueStoreServer.sh
#
# Disable updating of the accessors repo.
export PT_NO_NET=true

echo "$0: Sleeping for 3 seconds: `date`"
sleep 3

model=$PTII/ptolemy/demo/Office/OfficeServices/KeyValueStoreServer.xml
echo "$0: about to start $model: `date`"
echo "$0: about to start $model: `date`" 1>&2
$PTII/bin/ptinvoke ptolemy.moml.MoMLSimpleApplication $model
echo "$0: $model exited: `date`"
echo "$0: $model exited: `date`" 1>&2

