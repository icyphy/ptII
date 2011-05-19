# $Id$
# Tcl script that creates a pubSub model.
# We invoke this is a separate script to avoid memory leaks.

# Usage: $PTII/bin/ptjacl pubSubAggModel.tcl 2 2 ptolemy.actor.TypedCompositeActor true
#
source PublisherCommon.tcl 
pubSubAggModel [lindex $argv 0] [lindex $argv 1] [lindex $argv 2] [lindex $argv 3]



