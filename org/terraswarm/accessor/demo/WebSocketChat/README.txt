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

* Run SecureServer.xml in the Ptolemy host

* To run secure WebSocketChat client demo on the browser host

Open WebSocketClient accessor on web browser, set parameter “sslTls” true.
Check if server address and port number is correct, then hit “react to inputs”.

