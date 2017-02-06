# How to run the secure WebSocketChat client demo on a browser host
# Author: Hokeun Kim (hokeunkim@eecs.berkeley.edu)

*Adding the certificate to the browser for Secure Web Server demo

For FireFox on Mac OS X, in the menu tab

FireFox->Preferences->Advanced->Certificates
Click “View Certificates”

Click “Import”

Navigate and select “$PTII/org/terraswarm/accessor/demo/TCPSocket/certs/CACert.pem”

check “Trust this CA to identify websites.”

Click “OK”

* The new certificate will appear as "TerraSwarm > CA for SSL/TLS demos" in the list of certificates.

* Run SecureServer.xml in the Ptolemy host:

  cd $PTII/org/terraswarm/accessor/demo/WebSocketChat
  vergil SecureServer.xml &

* To run secure WebSocketChat client demo on the browser host:

Navigate your browser to http://accessors.org and find the browser host, or
go directly to:

  https://www.icyphy.org/accessors/hosts/browser/index.html
  
Select the WebSocketClient accessor within the net directory.
Set parameter “sslTls” true, set the server address to localhost,
and the port to 8080. Then click “react to inputs”.

