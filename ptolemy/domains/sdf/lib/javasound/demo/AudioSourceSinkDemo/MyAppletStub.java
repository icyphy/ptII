package ptolemy.domains.sdf.lib.javasound.demo.AudioSourceSinkDemo;

import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.net.*;

public class MyAppletStub implements AppletStub {
                 private Hashtable _properties;
                 private Applet _applet;

                 /**
                  * Creates a new MyAppletStub instance and initializes the
                  * init parameters from the command line. Arguments are
                  * passed in as name=value pairs. Reading the command line
                  * arguments can be made more sophisciated depending on your
                  * needs, but the basic idea will likely remain the same.
                  * Also, this particular implementation doesn't deal very well with
                  * invalid name=value pairs.
                  * @param argv[] Command line arguments passed to Main
                  * @param an Applet instance.
                  */
                 public MyAppletStub (String argv[], Applet a) {
                   _applet = a;
                   _properties = new Hashtable();
		   
		   		   
		   _properties.put ("defaultIterations", "2000");
		   _properties.put ("background", "#bbbbbb");  // A nice grey background.

		   //System.out.println("Hash table iterations: " + _properties.get("defaultIterations"));

		   //    for ( int i = 0; i < argv.length; i++ ) {
		   //try {
		   //  StringTokenizer parser = new StringTokenizer (argv[i], "=");
		       
		       // This code does not get executed!
		       
                       // String name = parser.nextToken().toString(); 
		       // Above just dosn't work??? Do it manually:
		       // Set name, value so that getParameter() will not throw a
		       // nullpointer exception.
		       //String name = "background";
                       //String value = parser.nextToken("\"").toString();
		       // Above just dosn't work??? Do it manually:
		       //String value = "#faf0e6";
                       //value = value.substring(1);
                       //_properties.put (name, value);
		       //_properties.put ("iterations", "50");
		       //_properties.put ("background", "#faf0e6");
		       //_properties.put ("background", "#666666");
		       //_properties.put ("defaultIterations", "200");
		       //System.out.println("Hash table iterations: " + _properties.get(" defaultIterations"));
		   // } catch (NoSuchElementException e) {
		   //   e.printStackTrace();
		   // }
		   // }
                 }

                 /**
                  * Calls the applet's resize
                  * @param width
                  * @param height
                  * @return void
                  */
                 public void appletResize (int width, int height) {
                   _applet.resize (width, height);
                 }

                 /**
                  * Returns the applet's context, which is 
                  * null in this case. This is an area where more creative programming
                  * work can be done to try and provide a context
                  * @return AppletContext Always null
                  */ 
                 public AppletContext getAppletContext () {
                   return null;
                 }

                 /**
                  * Returns the CodeBase. If a host parameter isn't provided
                  * in the command line arguments, the URL is based
                  * on InetAddress.getLocalHost(). The protocol is "file:"
                  * @return URL
                  */
                 public java.net.URL getCodeBase() {
                   String host;
                   if ( (host=getParameter ("host")) == null ) {
                     try {
                       host = InetAddress.getLocalHost().getHostName();
                     } catch (UnknownHostException e) {
                       e.printStackTrace();
                     }
                   }
                     
                   java.net.URL u  = null;
                   try {
                     u = new java.net.URL ("file://"+host);
                   } catch (Exception e) { }
                   return u;
                 }

                 /**
                  * Returns getCodeBase
                  * @return URL
                  */
                 public java.net.URL getDocumentBase() {
                   return getCodeBase();
                 }

                 /**
                  * Returns the corresponding command line value
                  * @return String
                  */
                 public String getParameter (String p) {
                   return (String)_properties.get (p);
                 }

                 /**
                  * Applet is always true
                  * @return boolean True
                  */
                 public boolean isActive () {
                   return true;
                 }
               }
