/* A PTMLPrinter can write an XMLElement tree to a PTML file

 Copyright (c) 1998 The Regents of the University of California.
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

@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.schematic;

import ptolemy.kernel.util.*;
import java.util.Enumeration;
import collections.*;
import java.io.*;
import com.microstar.xml.*;

//////////////////////////////////////////////////////////////////////////
//// PTMLPrinter
/**

@author Steve Neuendorffer, John Reekie
@version $Id$
*/
public class PTMLPrinter {

    PTMLPrinter(String url, XMLElement e) {
        super();
        url=newurl;
        root=e;
    } 

    public void print() {
    }
    
    public void print(OutputStream os) {
        os.write(xmlheader.toCharArray());
        printXMLElement(os, root);
    }
    
    protected void printXMLElement(OutputStream os, e) {
        os.write("<");
        os.write(e.getElementName().toCharArray());
        os.write("\n");
        Enumeration attribs = e.attributes();
        while(attribs.hasMoreElements()) {
            String name = attribs.nextElement();
            String value = e.getAttributes(name);
            os.write("\t");
            os.write(name.toCharArray());
            os.write("=\"");
            os.write(value.toCharArray());
            os.write("\"\n");
        }
        os.write(">");
        Enumeration children = getChildElements(e);
        while(children.hasMoreElements()) {
            XMLElement child = (XMLElement) children.nextElement();
            PrintXMLElement(os,child);
        }
        os.write("</");
        os.write(e.getElementName().toCharArray());
        os.write(">\n");
        
    }

    static final String xmlheader(
            "<?xml version=\"1.0\" standalone=\"no\"?>\n" + 
            "<!DOCTYPE ptolemyicon SYSTEM \"ptolemyicon.dtd\">\n");
    private XMLElement current;
    private XMLElement root;

}
