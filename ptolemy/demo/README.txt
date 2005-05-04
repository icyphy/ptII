ptolemy/demo/README.txt
$Id$

This directory contains a Ptolemy II model that executes a suite
of other Ptolemy II models. Executing this model effectively provides
automated demos.

A machine can be set up to autonomously continuously run through a
sequence of demos that is specified either in a local directory or
on a website.  To do this, you can set up a machine that:
  1) reboots itself periodically
  2) logs in as a specified user upon rebooting
  3) starts the demos executing upon logging in.


Rebooting automatically
=======================

Although the mechanism here is reasonably robust, if you are running
on Windows, it is advisable to periodically reboot the machine. You
can set up Windows to do this automatically, say every night.

To automatically reboot a Windows machine every night:

Schedulel a task with: Start -> Control Panel -> Scheduled Tasks. 
The shutdown binary is shutdown.bat in this same directory.
This script runs c:\windows\system32\shutdown -r -t0 -f,
which does a reboot in 0 seconds with a force shutdown of any
running commands. 

Logging in automatically
========================

TweakUI from Microsoft is a tool that enables automatic logon.
You can get it from:

   http://www.microsoft.com/windowsxp/pro/downloads/powertoys.asp 

Install this tool, start it, select "Logon" and "Autologon".
The rest is obvious.

Starting the demos
==================

The batch file rundemos.bat is used to execute a shell script
defined in the file startdemo.sh. Logged in as the user that will
be logged in automatically, you can add this batch file to
the directory obtained via the Start menu in Windows.
To add the batch file, browse to it in an explorer window and copy
it with Control-C.  Then select

  Start -> All Programs -> Startup

Right click on the Startup menu choice and select Explore. 
Paste the batch file into the Startup folder.
