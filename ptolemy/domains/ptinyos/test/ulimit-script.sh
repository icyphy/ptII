#!/bin/bash

# This script will increase the maximum number of open files allowed
# in the shell.  It will drop you into a new instance of a bash shell.

# You must run this script using sudo or as root.

# Change the login id below from celaine to your login id.

ulimit -n 20000
su celaine

