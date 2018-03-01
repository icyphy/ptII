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
*   [computerVision][9] : OpenCV image processing library, ported to Javascript by UC Irvine and Intel. 
*   [imageFilters][10]: Filter images to create new images. 
*   [motionDetector][11]: Detect and locate motion in streams of images. 



## Node Modules

Some modules designed for Node.js are pure JavaScript with no particular dependency on Node. These can be required as well by an accessor. These include, at least, 

*   [querystring][12] 



## Unfinished Modules

*In case-insensitive alphabetical order, please* 

*   [audio][13]: Provide access to the host audio hardware. 
*   [ble][14]: Provide access to Bluetooth Low Energy peripherals. 
*   [browser][15]: Provide display in the default browser. 
*   [coapClient][16]: Provide support for CoAP clients. 
*   [discovery][17]: Provide device discovery for devices on the local area network. 
*   [GMTK][18]: GMTK accessor that requires some setup through the [webSocket][6] or shell accessors 
*   [IMUSensor][19]: Provides bluetooth connection and packet output from Roozbeh's IMU Sensors. 
*   [localStorage][20]: Provide persistent key-value storage based on local files. 
*   [mapManager][21][?][21]: Provides resources for creating maps and combining information across them. 
*   [mdnsClient][22][?][22]: mDNS and DNS-SD support. 
*   [mqtt][23]: Provide support for MQTT protocol clients. 
*   obd 
*   rabbitmq 
*   [serial][24] : Provide connection to serial ports. 
*   [ssdpClient][25]: UPnP device discovery. 
*   [textToSpeech][26]: Provide spoken word output. 
*   [UDPSocket][27]: Provide interfaces and functions for UDP sockets. 

To implement a module, see the [Module Specification][28]. 



* * *

[Back to accessor specification][29]

 [1]: https://wiki.eecs.berkeley.edu/accessors/Version0/Require
 [2]: https://chess.eecs.berkeley.edu/ptexternal/src/ptII/doc/codeDoc/js/index.html
 [3]: https://wiki.eecs.berkeley.edu/accessors/Version0/Eventbus
 [4]: https://wiki.eecs.berkeley.edu/accessors/Version0/HttpClient
 [5]: https://wiki.eecs.berkeley.edu/accessors/Version0/Socket
 [6]: https://wiki.eecs.berkeley.edu/accessors/Version0/WebSocket
 [7]: https://wiki.eecs.berkeley.edu/accessors/Version0/AprilTags
 [8]: https://wiki.eecs.berkeley.edu/accessors/Version0/Cameras
 [9]: https://wiki.eecs.berkeley.edu/accessors/Version0/ComputerVision
 [10]: https://wiki.eecs.berkeley.edu/accessors/Version0/ImageFilters
 [11]: https://wiki.eecs.berkeley.edu/accessors/Version0/MotionDetector
 [12]: https://nodejs.org/api/querystring.html
 [13]: https://wiki.eecs.berkeley.edu/accessors/Version0/Audio
 [14]: https://wiki.eecs.berkeley.edu/accessors/Version0/Ble
 [15]: https://wiki.eecs.berkeley.edu/accessors/Version0/Browser
 [16]: https://wiki.eecs.berkeley.edu/accessors/Version0/CoapClient
 [17]: https://wiki.eecs.berkeley.edu/accessors/Version0/Discovery
 [18]: https://wiki.eecs.berkeley.edu/accessors/Version0/GMTK
 [19]: https://wiki.eecs.berkeley.edu/accessors/Version0/IMUSensor
 [20]: https://wiki.eecs.berkeley.edu/accessors/Version0/LocalStorage
 [21]: https://wiki.eecs.berkeley.edu/accessors/Version0/MapManager?action=edit
 [22]: https://wiki.eecs.berkeley.edu/accessors/Version0/MdnsClient?action=edit
 [23]: https://wiki.eecs.berkeley.edu/accessors/Version0/Mqtt
 [24]: https://wiki.eecs.berkeley.edu/accessors/Version0/Serial
 [25]: https://wiki.eecs.berkeley.edu/accessors/Version0/SsdpClient
 [26]: https://wiki.eecs.berkeley.edu/accessors/Version0/TextToSpeech
 [27]: https://wiki.eecs.berkeley.edu/accessors/Version0/UDPSocket
 [28]: https://wiki.eecs.berkeley.edu/accessors/Version0/ModuleSpecification
 [29]: https://wiki.eecs.berkeley.edu/accessors/Version1/AccessorSpecification