/* A Factory to create useful objects from XMLElements.

 Copyright (c) 1998-1999 The Regents of the University of California.
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

package ptolemy.schematic.util;

import ptolemy.kernel.util.*;
import java.util.Enumeration;
import collections.*;
import java.io.*;
import ptolemy.schematic.xml.*;

//////////////////////////////////////////////////////////////////////////
//// XMLObjectFactory
/**
An XMLObjectFactory supports the creation of useful objects (IconLibraries, 
DomainLibraries, etc.) from XMLElements that represent the root elements
of the correspinding PTML file.

@author Steve Neuendorffer, John Reekie
@version $Id$
*/
public class PTMLObjectFactory {

    /** 
     * Create the root IconLibrary from an XMLElement that was parsed from 
     * the root IconLibrary.  
     * @exception If the XML element does not have a type of "iconlibrary"
     */
    public static IconLibrary createIconLibrary(XMLElement e) 
            throws IllegalActionException {

        PTMLParser parser = null;
        _checkElement(e, "iconlibrary");

        IconLibrary iconlibrary = new IconLibrary();
        Enumeration children = e.childElements();
        while(children.hasMoreElements()) {
            XMLElement child = (XMLElement) children.nextElement();
            String etype = child.getElementType();
            if(etype.equals("icon")) {
                // if it's an Icon, then create it, 
                // and add it to the list of icons.
                iconlibrary.addIcon(_createIcon(child));
            } else if(etype.equals("sublibrary")) {
                // if it's a sublibrary, then add it to the 
                // list of sublibraries.

                String url = "";
                try {
                    url = child.getAttribute("link");
                    if(parser == null) parser = new PTMLParser();
                    
                    XMLElement sublibtree = parser.parse(url);
                    IconLibrary sublib = createIconLibrary(sublibtree); 
                    iconlibrary.addSubLibrary(sublib);
                }
                catch (Exception ex) {
                    System.out.println("Couldn't parse iconlibrary from url "+
                            url);
                    System.out.println(ex.getMessage());
                }

            } else if(etype.equals("description")) {
                iconlibrary.setDescription(child.getPCData());
            } else if(etype.equals("terminalstyle")) {
            } else {
                _unknownElementType(etype, "iconlibrary");
            }
        }
        return iconlibrary;
            
    }

    /** 
     * Create the root IconLibrary from an XMLElement that was parsed from 
     * the root IconLibrary.  
     * @exception If the XML element does not have a type of "iconlibrary"
     */
    /*    public static DomainLibrary createDomainLibrary(XMLElement e)
    throws IllegalActionException {
        
    }*/
   
    /**
     * Check the validity of the XML element.  This method is used to 
     * check external inputs to this class, which should not be flagged
     * as fatal errors (although the calling code may interpret them as such).
     *
     * @exception IllegalActionException If the element is null, or its
     * type is not equal to the given type.
     */
    private static void _checkElement(XMLElement e, String elementtype) 
            throws IllegalActionException {
        if(e == null) {
            throw new IllegalActionException("_checkElement: " +
                    "Received null XMLElement");
        }
        if(!e.getElementType().equals("iconlibrary")) {
            throw new IllegalActionException("createIconLibrary: " +
                    "Element type " + e.getElementType() + 
                    "differs from expected iconlibrary.");
        }
    }

    private static GraphicElement _createGraphicElement(XMLElement e)
        throws IllegalActionException {

        _verifyElement(e, "xmlgraphic");

        String name = e.getElementType();
        GraphicElement graphic = new GraphicElement(name);
        Enumeration children = e.childElements();
        while(children.hasMoreElements()) {
            XMLElement child = (XMLElement)children.nextElement();
            String etype = child.getElementType();
            _unknownElementType(etype, "xmlgraphic");
        }
        
        Enumeration attributes = e.attributeNames();
        while(attributes.hasMoreElements()) {
            String n = (String) attributes.nextElement();
            String v = e.getAttribute(n);        
            graphic.setAttribute(n, v);
        }
        return graphic;
    }

    private static Icon _createIcon(XMLElement e)
        throws IllegalActionException {

        _verifyElement(e, "icon");

        Icon icon = new Icon();
        Enumeration children = e.childElements();
        while(children.hasMoreElements()) {
            XMLElement child = (XMLElement)children.nextElement();
            String etype = child.getElementType();
            if(etype.equals("tclscript")) {
                // if it's an Icon, then create it, 
                // and add it to the list of icons.
            } else if(etype.equals("description")) {
                icon.setDescription(child.getPCData());
            } else if(etype.equals("terminal")) {
            } else if(etype.equals("xmlgraphic")) {
                GraphicElement g = _createGraphicElement(child);
                icon.addGraphicElement(g);
            } else {
                _unknownElementType(etype, "icon");
            }    
        }
        return icon;
    }

    /** 
     * Print a message about the unknown element
     */
    private static void _unknownElementType(String etype, String parent) {
                System.out.println("Unrecognized element type = " +
                    etype + " found in " + parent);
    }
        
        /**
     * Check the validity of the XML element.   This method is very similar 
     * to _checkElement, except that it is used internally to check state
     * that should already be true, unless the code is broken.
     *
     * @exception InternalErrorException If the element is null, or its
     * type is not equal to the given type.
     */
     private static void _verifyElement(XMLElement e, String elementtype) {
        if(e == null) {
            throw new InternalErrorException("_checkElement: " +
                    "Received null XMLElement");
        }
        if(!e.getElementType().equals("iconlibrary")) {
            throw new InternalErrorException("createIconLibrary: " +
                    "Element type " + e.getElementType() + 
                    "differs from expected iconlibrary.");
        }
     }
                   
}

