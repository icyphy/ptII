/*

 Copyright (c) 2001-2003 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

                                        PT_COPYRIGHT_VERSION_2
                                        COPYRIGHTENDKEY
@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.copernicus.jhdl.util;

import java.io.FileWriter;
import java.io.IOException;

import ptolemy.graph.DirectedGraph;

public abstract class GraphToDotty {

    public static final String MYEOL = "\n";

    public static String convert(DirectedGraph g, String ename) {
	return null;
    }

    /**
     * Print a .dot file header
     **/
    public static String header(String creatorName, String graphName) {
	StringBuffer sb = new StringBuffer();
	sb.append("//Dotfile created by "+creatorName+"\r\n");
	sb.append("digraph "+graphName+" {\r\n");
	sb.append("\tsize=\"8,11\"\r\n");
	sb.append("\t// Vertices\r\n");
	return sb.toString();
    }

    /**
     * Converts all the special characters in <code>str</code> (like newlines
     * and quotes) to escape sequences (like \n)
     *
     * Courtesy of Nathan Kitchen
     */
    public static String convertSpecialsToEscapes(String str) {
	StringBuffer strBuf = new StringBuffer();
	for (int i = 0; i < str.length(); i++) {
	    char c = str.charAt(i);
	    switch (c) {
	    case '\n':
		strBuf.append("\\n");
		break;
	    case '\t':
		strBuf.append("\\t");
		break;
  	    case '\r':
		// determine use of \r based on current MYEOL value
		if (MYEOL.equals("\r\n"))
		    strBuf.append("\\r");
  		break;
	    case '\"':
		strBuf.append("\\\"");
		break;
	    case '\'':
		strBuf.append("\\\'");
		break;
	    case '\b':
		strBuf.append("\\b");
		break;
	    case '\f':
		strBuf.append("\\f");
		break;
	    case '\\':
		strBuf.append("\\\\");
		break;
	    default:
		strBuf.append(c);
	    }
	}
	return strBuf.toString();
    }

    public static String validFileName(String basename) {
	byte bbytes[] = basename.getBytes();
	byte nbytes[] = new byte[bbytes.length];
	if (!((bbytes[0] >= 'a' && bbytes[0] <= 'z') ||
	      (bbytes[0] >= 'A' && bbytes[0] <= 'Z')))
	    bbytes[0] = 'A';
	for (int i=0;i<bbytes.length;i++) {
	    switch(bbytes[i]) {
	    case ':':
		nbytes[i] = '_';
		break;
	    default:
		nbytes[i] = bbytes[i];
		break;
	    }
	}
	return new String(nbytes);
    }

    /*
    public static void writeDotFile(String basename, DirectedGraph g) {
	String filename = validFileName(basename) + ".dot";
	System.out.println("Writing "+filename);
	try {
	    FileWriter dotFile=new FileWriter(filename);
	    dotFile.write(convert(g,basename));
	    dotFile.close();
	} catch (IOException e){
	    System.out.println(e);
	}
    }
    */

}
