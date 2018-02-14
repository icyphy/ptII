$Id$
org/terraswarm/accessor/demo/AugmentedRealityVideoSOHO/README.txt

This directory contains the Augumented Reality demo that was shown at
the TerraSwarm 2017 Annual Meeting.

After setting up the demo and running it, when the camera sees various
tags, an accessor is downloaded from a key/value store and a user
interface is displayed in a web browser.

The demo requires various pieces of hardware, so it is not likely to
run for most people.

See https://www.terraswarm.org/testbeds/wiki/Main/Swarmnuc2008 for notes
about the hardware.

The hardware is:

* A SOHO gateway configured to statically set the IP addresses of the
  other devices. http://192.168.1.1

  Username: "admin",  you know what: printed on the side


* A SwarmGateway: http://192.168.1.200 that is running a MQTT Mosquitto server.
  For more information, see
  https://www.terraswarm.org/urbanheartbeat/wiki/Main/UrbanHeartbeatKitMQTT

  Username: "eal"  password: printed on the side


* A SwarmBox: 192.168.1.217 that acts as a key/value store.
  The SwarmBox is registered on the campus network as
  swarmnuc1017.eecs.berkeley.edu
  
  For more information, see
  https://www.terraswarm.org/testbeds/wiki/Main/Hardware

  Username: "eal"  password: printed on the side


* A Hue basestation: http://192.168.1.221 that controls the light bulbs.



== Running the Demo ==

To run the demo

1. Turn on the plug strip, press the power button on the SwarmBox

2. Run

  $PTII/bin/capecode AugmentedRealityVideoSOHO.xml

3. It is necessary to upload the accessors to the key/value store each
   time, so click on the links for the accessors, typically the BLEE and
   Hue and run those models.

4. Open up tag36h11-19-29.pdf, which contains the Apriltags 19
   through 29. Either print them off, or take pictures of tag 21 and 22.

5. Run the model and hold one of the tags in front of the screen.

6. The accessor should be downloaded, run and a user interface
   should appear in a web browser

== Debugging ==

* Try the websites:
** Netgear router: http://192.168.1.1
   Username: "admin",  you know what: printed on the side

** SwarmGateway: http://192.168.1.200
   Should show the Blee(s) and Powerblades.  If not, see ==SwarmGateway Details== below.

   To log in, use
   ssh eal@192.168.1.200
   The you know what is on top.

** Hue: http://192.16.1.221
   Should show a web page.

   Consider trying the Hue app from your phone.  Connect to the
   Netgear02 wireless network, the you know what is on the side of the Netgear box

** SwarmBox: http://192.168.1.217:8099/keyvalue/help
   Should see the help page for the Key-Value store, which is running under pm2.

   To log in use
   ssh eal@192.168.1.217
   The you know what is on top.

   If there are problems, then try becoming root and restarting the KeyStore model"

   sudo -i
   su - sbuser
   pm2 restart 0

Getting info about processes:

pm2 list               # Display all processes status
pm2 jlist              # Print process list in raw JSON
pm2 prettylist         # Print process list in beautified JSON

== Changing Light Bulbs ==
To try different lightbulbs:

1) Download the Hue app to your phone

2) Connect to the SOHO Gateway, the network name is NETGEAR02, see the
   side of the box for the key

3) Use the Hue app to find the bulbs and add them to a scene

4) Run the ../Hue/HueToggle.xml model.  The Hue IDs will be in the
   Display connected to the Hue accessor.

5) Edit hues.json and match the id with the atag.


== SwarmGateway Details ==

To view the MQTT devices that the SwarmGateway knows about, go to

http://192.168.1.200

If the SwarmGateway is not connected to the outside world and MQTT
devices are not shown, then plug it in to the campus network, and
devices should start appearing at http://192.168.1.200.

To update the cache, plug the SwarmGateway back in to the Netgear box,
log in to and copy the cache file:

     ssh debian@192.168.1.200
     password: (See the side of the BBB)

Then, copy:
     cp /cached_urls.json /cached_urls.json.bak


=== SwarmGateway Debugging ===

The SwarmGateway downloads a parser for the different types of MQTT
devices.  These parsers are cached so that the SwarmGateway can run
without a connection.  The url where the parser is found is also cached.

/cached_parsers.json
    Contains parsers 

/cached_urls.json
    Maps urls 

Copies of these files were made by hand:

/cached_parsers.json.bak

/cached_urls.json.bak


The file that does the caching is
/home/debian/gateway/software/node_modules/ble-gateway/ble-gateway.js

To make changes:

1. Plug an Ethernet cable in to the NetGear router
2. Run
     ssh debian@192.168.1.200
     password: (See the side of the BBB)

3. Become root with
     sudo -i

4. Edit /home/debian/gateway/software/node_modules/ble-gateway/ble-gateway.js

5. To restart the daemon:
      systemctl restart ble-gateway-mqtt

6. To enable debugging, edit
   /etc/systemd/system/ble-gateway-mqtt.service and uncomment
       Environment='DEBUG=ble-gateway':
 --start--
    [Unit]
    Description=Receive and parse BLE packets.
    After=bluetooth.target mosquitto.service

    [Service]
    # To enable debugging, uncomment the Environment line and then run:
    # systemctl restart ble-gateway-mqtt
    # and then look at /var/log/ble.log
    Environment='DEBUG=ble-gateway'
    ExecStart=/home/debian/gateway/software/ble-gateway-mqtt/ble-gateway-mqtt.js
    Restart=always
    StandardOutput=syslog
    StandardError=syslog


    SyslogIdentifier=ble-gateway-mqtt

    [Install]
    WantedBy=multi-user.target
 --end---

7.  Then run systemctl restart ble-gateway-mqtt and then look at
    /var/log/ble.log


See also
https://www.terraswarm.org/urbanheartbeat/wiki/Main/UrbanHeartbeatKitMQTT


=== Connecting to the SwarmGateway when it is on the campus network ===

To find the IP address, download and install the Summon app from
https://github.com/lab11/summon

The SwarmGateway name will start with "sg".  Note the IP address
and use ssh to connect to it as the debian user.  

     ssh debian@128.32.151.100
     password: (See the side of the BBB)



=== AprilTags ===

https://april.eecs.umich.edu/software/apriltag.html contains
https://april.eecs.umich.edu/media/apriltag/tag36h11.tgz, which is a
set of pregenerated tags as png and PostScript files.  However, these
are of low resolution.  To scale them, use linear interpolation to
avoid blurring.


  wget https://april.eecs.umich.edu/media/apriltag/tag36h11.tgz
  tar -zxf tag36h11.tgz
  cd tag36h11

Under Mac OS X, ImageMagik can be installed using MacPorts
(https://www.macports.org/)

  sudo port install imagemagick

For example, with ImageMagik, to increase the size of the images use:

  mogrify -scale 1000x1000 *.png

Or, search the web for "tag 36H11".

To create a pdf with all the images:

   convert *.png tag36h11.pdf

To annotate an image with a string: 

   convert tag36_11_00026.png label:'26' -gravity Center -append tag36_11_00026eled.png


Below is a script that will generate a pdf file with labels
--start--
#!/bin/sh
convert tag36_11_00019.png -size 800x label:'AprilTag 19: Sound Server' -gravity Center -append tag36_11_00019_labeled.png
convert tag36_11_00020.png -size 800x label:'AprilTag 20: Robot Service' -gravity Center -append tag36_11_00020_labeled.png
convert tag36_11_00021.png -size 800x label:'AprilTag 21: Light Bulb' -gravity Center -append tag36_11_00021_labeled.png
convert tag36_11_00022.png -size 800x label:'AprilTag 22: Blee c0:98:E5:30:00:B4' -gravity Center -append tag36_11_00022_labeled.png
convert tag36_11_00023.png -size 800x label:'April Tag 23: Blee c0:98:E5:30:00:5B Office' -gravity Center -append tag36_11_00023_labeled.png
convert tag36_11_00024.png -size 800x label:'April Tag 24: PowerBlade c0:98:e5:70:02:1e: Netgear N300' -gravity Center -append tag36_11_00024_labeled.png
convert tag36_11_00025.png -size 800x label:'April Tag 25: PowerBlade c0:98:e5:70:02:0b: Swarmnuc1017' -gravity Center -append tag36_11_00025_labeled.png
convert tag36_11_00026.png -size 800x label:'April Tag 26: PowerBlade c0:98:e5:70:02:3e: Light Bulb' -gravity Center -append tag36_11_00026_labeled.png
convert tag36_11_00027.png -size 800x label:'April Tag 27: PowerBlade c0:98:e5:80:02:3a: Office' -gravity Center -append tag36_11_00027_labeled.png
convert tag36_11_00028.png -size 800x label:'April Tag 28' -gravity Center -append tag36_11_00028_labeled.png
convert tag36_11_00029.png -size 800x label:'April Tag 29' -gravity Center -append tag36_11_00029_labeled.png

# Create a pdf with one page per tag
convert *labeled.png tag36h11-19-29.pdf 

# Create one page with 11 tiles
montage *labeled.png -geometry '1000x1200>+4+3' -tile 3x4 tag36h11-19-29-montage.png
--end--

tag36h11-19-29-montage.png has been checked in to the repository.

=== Key/Value Store ===

The Key/Value store is started up using pm2, see

See https://www.icyphy.org/accessors/wiki/Notes/KeyValueStoreOnTerra

The script runKVStore script looks like

--start--
#!/bin/sh                                                                                                                                                     
# pm2 start  --interpreter=bash ~/runKVStore                                                                                                                  
$PTII/bin/ptinvoke ptolemy.moml.MoMLSimpleApplication $PTII/org/terraswarm/accessor/demo/AugmentedRealityVideoSOHO/KeyValueStoreServerSOHO.xml
--end--


=== Audio ===

See https://www.terraswarm.org/testbeds/wiki/Main/Swarmnuc2008
