Optional Java Script Modules
============================
The following objects provide bundles of functions. An accessor that uses one or more of these modules must declare that requirement using the [require](https://www.terraswarm.org/accessors/wiki/Version0/Require) tag or JavaScript function. 

Reasonably Well-Developed Modules
---------------------------------

* [eventbus](module-eventbus.html): Provide publish and subscribe on a local network through a Vert.x event bus.
* [httpClient](module-httpClient.html): Provide support for HTTP clients.
* [webSocket](module-webSocket.html): Provide full-duplex web socket interfaces and functions for web socket servers.

Unfinished Modules
------------------

* [audio](module-audio.html): Provide access to the host audio hardware.
* [ble](module-ble.html): Provide access to Bluetooth Low Energy peripherals.
* [browser](module-browser.html): Provide display in the default browser.
* [coapClient](module-coapClient.html): Provide support for CoAP clients.
* [discovery](module-discovery.html): Provide device discovery for devices on the local area network.
* [localStorage](module-localStorage.html): Provide persistent key-value storage based on local files.
* obd
* [mqtt](module-mqtt.html): Provide support for MQTT protocol clients.
* rabbitmq
* [textToSpeech](module-textToSpeech.html): Provide spoken word output.
* serial
* [UDPSocket](module-UDPSocket.html): Provide interfaces and functions for UDP sockets.

To implement a module, see the [Module Specification](https://www.terraswarm.org/accessors/wiki/Version0/ModuleSpecification).

The following objects provide bundles of functions and should be built in to any accessor host.


Built-In Java Script Modules
============================
* [console](module-console.html): Provides various utilities for formatting and displaying data.
* [events](module-events.html): Provides an event emitter design pattern (requires util).
* [util](module-util.html): Provides various utility functions.

Top-level Functions
===================
* [basicFunctions](module-basicFunctions.html)
* [localFunctions](module-localFunctions.html)

Where to find this page on the web
----------------------------------
[https://chess.eecs.berkeley.edu/ptexternal/src/ptII/doc/codeDoc/js/index.html](https://chess.eecs.berkeley.edu/ptexternal/src/ptII/doc/codeDoc/js/index.html)

How to update the JSDoc output in your ptII tree
------------------------------------------------

The JSDoc output is in $PTII/doc/js.

To regenerate that directory:

cd $PTII/vendors; git clone https://github.com/jsdoc3/jsdoc.git
cd $PTII
./configure
ant jsdoc

How to update the JSDoc output on the web
-----------------------------------------
The ptII continuous integration build at [http://terra.eecs.berkeley.edu:8080/job/ptIIci/](http://terra.eecs.berkeley.edu:8080/job/ptIIci/) checks the ptII svn repository every 5 minutes and if there is a change, then "ant jsdoc" is run and then the codeDoc/ directory is copied to [https://chess.eecs.berkeley.edu/ptexternal/src/ptII/doc/codeDoc/js/index.html](https://chess.eecs.berkeley.edu/ptexternal/src/ptII/doc/codeDoc/js/index.html).

See Also
--------
* [https://chess.eecs.berkeley.edu/ptexternal/wiki/Main/JSDocSystems](https://chess.eecs.berkeley.edu/ptexternal/wiki/Main/JSDocSystems) - Overview of JSDoc systems
* [https://www.terraswarm.org/accessors/wiki/Main/JSDoc](https://www.terraswarm.org/accessors/wiki/Main/JSDoc) - Information for Accessor writers (TerraSwarm membership required)
* [https://www.terraswarm.org/accessors/doc/jsdoc/index.html](https://www.terraswarm.org/accessors/doc/jsdoc/index.html) - JavaScript documentation of Accessors.

How to update this file
-----------------------
The source for this file is at $PTII/doc/jsdoc/topREADME.md.

It is copied to $PTII/doc/jsdoc/index.html when JSDoc is invoked with -R $PTII/doc/jsdoc/topREADME.md

$Id$

