// TimerDemo.java: demonstration application showing time elapsed for parse.
// NO WARRANTY! See README, and copyright below.
// $Id$
// Modified 11/8/98 to add package statement.
package com.microstar.xml.demo;


/**
  * Demonstration application showing time elapsed for parse.
  * <p>Usage: <code>java TimerDemo &lt;url&gt;</code>
  * <p>Or, use it as an applet, supplying the URL as the <code>url</code>
  * parameter.
  * @author Copyright (c) 1997, 1998 by Microstar Software Ltd.;
  * @author written by David Megginson &lt;dmeggins@microstar.com&gt;
  * @version 1.1
  * @since Ptolemy II 0.2
  * @see com.microstar.xml.XmlParser
  * @see com.microstar.xml.XmlHandler
  * @see XmlApp
  */
public class TimerDemo extends XmlApp {
    /**
      * Entry point for an application.
      */
    public static void main(String[] args) throws java.lang.Exception {
        long start;
        long end;
        TimerDemo demo = new TimerDemo();

        if (args.length != 1) {
            System.err.println("Usage: java TimerDemo <uri>");
            System.exit(1);
        }

        System.out.println("Starting parse...");
        start = System.currentTimeMillis();
        demo.doParse(args[0]);
        end = System.currentTimeMillis();
        System.out.println("Elapsed time: " + (end - start) + " ms");
    }
}

// End of TimerDemo.java
