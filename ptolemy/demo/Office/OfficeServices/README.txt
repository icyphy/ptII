$Id$
$PTII/ptolemy/demo/Office/OfficeServices/README.txt

This directory contains a collection of office services that constitute a smart space.
Most of these services depend on hardware to be present in the room, such as Internet-
connected light bulbs, although at least one, Sound, will run on any hardware that
has the capability to produce sound. This can be used to test the setup without
installing any additional hardware.

The core service is KeyValueStoreServer.xml. This is an executable model that the
other services use to advertise their presence and publish an accessor that can be
instantiated to access the service.  For example, Sound.xml, another executable model,
when executed, will publish an accessor to the KeyValueStoreServer under key '21'.
The master list of services and their keys in this directory is here:

    tag36_11_00019 Sound server
    tag36_11_00020 Robot Service
    tag36_11_00021 Light bulb 


FIXME: They key-value store should contain this table and the services should be
indexed in the table by name.  The Augmented-reality demo should read the table and
do the translation.

This directory contains shell scripts for running each of the services.
For example, the KeyValueStoreServer can be started by executing its shell script:

  ./KeyValueStoreServer.sh

================ Making Services Persistent with pm2

To ensure that the server is persistent, run it under pm2,

  pm2 start KeyValueStoreServer.sh
  pm2 save
  
pm2 automatically restarts the model if it crashes for some reason.
To check the status of such models,

  pm2 list

Other useful pm2 commands:

  pm2 delete KeyValueStoreServer

pm2 puts log files in ~/.pm2/logs.
To install pm2, so this:

  cd
  mkdir node_modules
  npm   install pm2
  export PATH=~/node_modules/bin:${PATH}

Add the export line to your ~/.bashrc or ~/.bash_profile so that pm2 is in your path.

================ Starting pm2 on Reboot

As root, run the following, replacing /home/sbuser/src/ptII with the location of
your Ptolemy II installation and ~sbuser with ~yourusername:

  export PATH=/home/sbuser/src/ptII/bin:${PATH}
  export PTII=/home/sbuser/src/ptII
  ~sbuser/node_modules/.bin/pm2 startup
  
As yourself, run

  pm2 startup
  
This will instruct you to execute a command like the following:

  sudo env PATH=$PATH:/usr/local/bin /ptII/node_modules/pm2/bin/pm2 startup launchd -u eal --hp /Users/eal

After executing that command, save the current status:

  pm2 save
  
Now the services should automatically start up upon reboot.

================ Restarting the services periodically using cron

To restart the model every day so as to compensate for memory leaks and other failures,
create a file ~/myusername.cron containing, for example:

0 5 * * *  myusername -c '/home/myusername/node_modules/.bin/pm2 restart' >& /home/myusername/pm2Restart.log

This specifies to execute the pm2 restart command at 5AM every morning.
To get this to execute, do this:

  crontab ~/myusername.cron
