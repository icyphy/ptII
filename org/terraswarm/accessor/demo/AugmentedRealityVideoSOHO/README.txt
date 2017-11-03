This directory contains the Augumented Reality demo that was shown at
the TerraSwarm 2017 Annual Meeting.

After setting up the demo and running it, when the camera sees various
tags, an accessor is downloaded from a key/value store and a user
interface is displayed in a web browser.

The demo requires various pieces of hardware, so it is not likely to
run for most people.

The hardware is:

* A SOHO gateway configured to statically set the IP addresses of the
  other devices.

* A SwarmGateway: 192.168.1.200 that is running a MQTT Mosquitto server.
  For more information, see
  https://www.terraswarm.org/urbanheartbeat/wiki/Main/UrbanHeartbeatKitMQTT

* A SwarmBox: 192.168.1.217 that acts as a key/value store.
  The SwarmBox is registered on the campus network as
  swarmnuc1017.eecs.berkeley.edu
  
  For more information, see
  https://www.terraswarm.org/testbeds/wiki/Main/Hardware

* A Hue basestation: 192.168.1.221 that controls the light bulbs.



== Running the Demo ==

To run the demo

1. Run

  $PTII/bin/capecode AugmentedRealityVideoSOHO.xml

2. It is necessary to upload the accessors to the key/value store each
   time, so click on the links for the accessors, typically the BLEE and
   Hue and run those models.

3. Open up tag36h11-19-29.pdf, which contains the Apriltags 19
   through 29. Either print them off, or take pictures of tag 21 and 22.

4. Run the model and hold one of the tags in front of the screen.

5. The accessor should be downloaded, run and a user interface
   should appear in a web browser


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

