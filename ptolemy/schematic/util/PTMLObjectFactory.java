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

//////////////////////////////////////////////////////////////////////////
//// XMLObjectFactory
/**
An XMLObjectFactory supports the creation of useful objects (IconLibraries, 
DomainLibraries, etc.) from XMLElements that represent the root elements
of the correspinding PTML file.

@author Steve Neuendorffer, John Reekie
@version $Id$
*/
public class XMLObjectFactory {

    /** 
     * Create the root IconLibrary from an XMLElement that was parsed from 
     * the root IconLibrary.  
     * @exception If the XML element does not have a type of "iconlibrary"
     */
    public static IconLibrary createIconLibrary(XMLElement e) 
            throws IllegalActionException {

        _checkElement(e, "iconlibrary");

        IconLibrary iconlibrary = new IconLibrary();
        Enumeration children = e.childElements();
        while(child.hasMoreElements()) {
            XMLElement child = children.nextElement();
            String etype = child.getElementType();
            if(etype.equals("icon")) {
                // if it's an Icon, then create it, 
                // and add it to the list of icons.
                iconlibrary.addIcon(_createIcon(child));
            } else if(etype.equals("sublibrary")) {
                // if it's a sublibrary, then add it to the 
                // list of sublibraries.
                String name = e.getAttribute("name");
                _sublibraries.putAt(name,e);

                /* } else if(e.getElementType().equals("header")) {
                // Remove the old header and swap in the new one.
                // if the new header does not contain a description, then
                // keep the old description 
                if(!e.hasChildElement(_description)) {
                    _header.removeChildElement(_description);
                    e.addChildElement(_description);
                }
                removeChildElement(_header);
                _header = e;
                */
            } else if(etype.equals("description")) {
                setDescription(child.getPCData());
            } else if(etype.equals("terminalstyle")) {
            }
        }
            
    }

    /** 
     * Create the root IconLibrary from an XMLElement that was parsed from 
     * the root IconLibrary.  
     * @exception If the XML element does not have a type of "iconlibrary"
     */
    public static DomainLibrary createDomainLibrary(XMLElement e)
    throws IllegalActionException {
        
    }
   
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
    private static Icon _createIcon(XMLElement e) {

        _verifyElement(e, "icon");

        IconLibrary iconlibrary = new IconLibrary();
        Enumeration children = e.childElements();
        while(child.hasMoreElements()) {
            XMLElement child = children.nextElement();
            String etype = child.getElementType();
            if(etype.equals("icon")) {
                // if it's an Icon, then create it, 
                // and add it to the list of icons.
                iconlibrary.addIcon(_createIcon(child));
            } else if(etype.equals("sublibrary")) {
                // if it's a sublibrary, then add it to the 
                // list of sublibraries.
                String name = e.getAttribute("name");
                _sublibraries.putAt(name,e);

                /* } else if(e.getElementType().equals("header")) {
                // Remove the old header and swap in the new one.
                // if the new header does not contain a description, then
                // keep the old description 
                if(!e.hasChildElement(_description)) {
                    _header.removeChildElement(_description);
                    e.addChildElement(_description);
                }
                removeChildElement(_header);
                _header = e;
                */
            } else if(etype.equals("description")) {
                setDescription(child.getPCData());
            } else if(etype.equals("terminalstyle")) {
            }
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

