
# OptionalJavaScriptModules

The following objects provide bundles of functions. An accessor that uses one or more of these modules must declare that requirement using the [require][1] tag or JavaScript function. 

As these modules are very much under development, these interface definitions may change. For the most up-to-date interface specifications, see the [auto-generated doc files][2] for the current version of the Ptolemy II/Nashorn host implementation of these modules. 



## Reasonably Well-Developed Networking Modules

*   [eventbus][3]: Provide publish and subscribe on a local network through a Vert.x event bus. 
*   [httpClient][4]: Provide support for HTTP clients. 
*   [socket][5]: TCP socket communication, including optional message framing. 
*   [webSocket][6]: Provide full-duplex web socket interfaces and functions for web socket servers. 



## Reasonably Well-Developed Image Processing Modules

*   [aprilTags][7]: Recognize and locate AprilTags in images. 
*   [cameras][8]: Capture images and video from cameras on the host (built in or USB connected). 
*   [imageFilters][9]: Filter images to create new images. 
*   [motionDetector][10]: Detect and locate motion in streams of images. 



## Node Modules

Some modules designed for Node.js are pure JavaScript with no particular dependency on Node. These can be required as well by an accessor. These include, at least, 

*   [querystring][11] 



## Unfinished Modules

*In case-insensitive alphabetical order, please* 

*   [audio][12]: Provide access to the host audio hardware. 
*   [ble][13]: Provide access to Bluetooth Low Energy peripherals. 
*   [browser][14]: Provide display in the default browser. 
*   [coapClient][15]: Provide support for CoAP clients. 
*   [discovery][16]: Provide device discovery for devices on the local area network. 
*   [GMTK][17]: GMTK accessor that requires some setup through the [webSocket][6] or shell accessors 
*   [IMUSensor][18]: Provides bluetooth connection and packet output from Roozbeh's IMU Sensors. 
*   [localStorage][19]: Provide persistent key-value storage based on local files. 
*   [mapManager][20]: Provides resources for creating maps and combining information across them. 
*   [mdnsClient][21]: mDNS and DNS-SD support. 
*   [mqtt][22]: Provide support for MQTT protocol clients. 
*   obd 
*   rabbitmq 
*   serial 
*   [ssdpClient][23]: UPnP device discovery. 
*   [textToSpeech][24]: Provide spoken word output. 
*   [UDPSocket][25]: Provide interfaces and functions for UDP sockets. 

To implement a module, see the [Module Specification][26]. 



* * *

[Back to accessor specification][27]

 [1]: https://www.terraswarm.org/accessors/wiki/Version0/Require
 [2]: https://chess.eecs.berkeley.edu/ptexternal/src/ptII/doc/codeDoc/js/index.html
 [3]: https://www.terraswarm.org/accessors/wiki/Version0/Eventbus
 [4]: https://www.terraswarm.org/accessors/wiki/Version0/HttpClient
 [5]: https://www.terraswarm.org/accessors/wiki/Version0/Socket
 [6]: https://www.terraswarm.org/accessors/wiki/Version0/WebSocket
 [7]: https://www.terraswarm.org/accessors/wiki/Version0/AprilTags
 [8]: https://www.terraswarm.org/accessors/wiki/Version0/Cameras
 [9]: https://www.terraswarm.org/accessors/wiki/Version0/ImageFilters
 [10]: https://www.terraswarm.org/accessors/wiki/Version0/MotionDetector
 [11]: https://nodejs.org/api/querystring.html
 [12]: https://www.terraswarm.org/accessors/wiki/Version0/Audio
 [13]: https://www.terraswarm.org/accessors/wiki/Version0/Ble
 [14]: https://www.terraswarm.org/accessors/wiki/Version0/Browser
 [15]: https://www.terraswarm.org/accessors/wiki/Version0/CoapClient
 [16]: https://www.terraswarm.org/accessors/wiki/Version0/Discovery
 [17]: https://www.terraswarm.org/accessors/wiki/Version0/GMTK
 [18]: https://www.terraswarm.org/accessors/wiki/Version0/IMUSensor
 [19]: https://www.terraswarm.org/accessors/wiki/Version0/LocalStorage
 [20]: https://www.terraswarm.org/accessors/wiki/Version0/MapManager?action=edit
 [21]: https://www.terraswarm.org/accessors/wiki/Version0/MdnsClient?action=edit
 [22]: https://www.terraswarm.org/accessors/wiki/Version0/Mqtt
 [23]: https://www.terraswarm.org/accessors/wiki/Version0/SsdpClient
 [24]: https://www.terraswarm.org/accessors/wiki/Version0/TextToSpeech
 [25]: https://www.terraswarm.org/accessors/wiki/Version0/UDPSocket
 [26]: https://www.terraswarm.org/accessors/wiki/Version0/ModuleSpecification
 [27]: https://www.terraswarm.org/accessors/wiki/Version1/AccessorSpecification

* * *

* *The text above was downloaded from https://www.terraswarm.org/accessors/wiki/Version0/OptionalJavaScriptModules?action=markdown -O OptionalJavaScriptModules.md *
* *The text below is from $PTII/doc/jsdoc/topREADMEEnd.md *

* * *

Where to find this page on the web
----------------------------------
[https://chess.eecs.berkeley.edu/ptexternal/src/ptII/doc/codeDoc/js/index.html](https://chess.eecs.berkeley.edu/ptexternal/src/ptII/doc/codeDoc/js/index.html)

How to get the list of Accessors from the TerraSwarm accessors wiki
-------------------------------------------------------------------
Currently, https://www.terraswarm.org/accessors/wiki/Version0/OptionalJavaScriptModules is not world readable.

However, we use the contents of that URL for the first part of this page.  What we do is use wget to get a markdown version of that page using cookies and then concatenate it with $PTII/doc/jsdoc/topREADMEEnd.md to create $PTII/doc/jsdoc/topREADME.md.  Running "cd $PTII; ant jsdoc" reads topREADME.md.

The script $PTII/doc/jsdoc/makeptjsdocREADME does this for us and is invoked by the continuous integration build.

If you invoke this script by hand, then you will need ~/.terracookies.  To create this, install the Cookies Export plugin into Firefox, log on to the TerraSwarm website and export your cookies to that file.  Note that the account that you use to log on should remain logged in for the wget command to work.

Then run $PTII/doc/jsdoc/makeptjsdocREADME.


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

