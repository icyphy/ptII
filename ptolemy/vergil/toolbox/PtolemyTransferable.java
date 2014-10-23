/* A transferable object that contains a named object.

 Copyright (c) 2000-2014 The Regents of the University of California.
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

 */
package ptolemy.vergil.toolbox;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ptolemy.gui.PtGUIUtilities;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// PtolemyTransferable

/**
 A transferable object that contains a local JVM reference to a
 number of named objects.  To get a reference to an iterator on the
 objects, request data with the data flavor given in the static
 namedObjFlavor variable.  This class will also return a MoML
 representation of the objects, if data is requested with the
 DataFlavor.stringFlavor or DataFlavor.plainTextFlavor.

 @author Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (johnr)
 */
@SuppressWarnings("serial")
public class PtolemyTransferable implements Transferable, Serializable {
    // This class implements Serializable in hopes that
    // drag and drop will work under Mac OS X.

    /**
     * Create a new transferable object that contains no objects.
     */
    public PtolemyTransferable() {
        _objectList = new LinkedList();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Add the given named object to the objects contained in this
     * transferable.  If the object already exists in this transferable,
     * then do not add it again.
     * @param object The object to be added to this transferable.
     */
    public void addObject(NamedObj object) {
        if (!_objectList.contains(object)) {
            _objectList.add(object);
        }
    }

    /**
     * Return the data flavors that this transferable supports.
     * @return The data flavors.
     */
    @Override
    public synchronized DataFlavor[] getTransferDataFlavors() {
        return _flavors;
    }

    /**
     * Return true if the given data flavor is supported.
     * @param flavor The data flavor that is searched for.
     * @return true if the given data flavor is supported.
     */
    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        int i;

        for (i = 0; i < _flavors.length; i++) {
            if (_flavors[i].equals(flavor)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Return an object that represents the data contained within this
     * transferable with the given flavor.  If the flavor is namedObjFlavor,
     * return an iterator of the objects that this transferable refers to.
     * If the flavor is DataFlavor.plainTextFlavor, return an
     * InputStream that contains a MoML representation of the objects.
     * If the flavor is DataFlavor.stringFlavor return a string that
     * contains the MoML representation.
     *
     * @param flavor The data flavor.
     * @return An object with the given flavor.
     * @exception UnsupportedFlavorException If the given flavor is
     * not supported.
     * @exception IOException If thrown while creating the MoML.
     */
    @Override
    public Object getTransferData(DataFlavor flavor)
            throws UnsupportedFlavorException, IOException {
        if (flavor.equals(DataFlavor.plainTextFlavor)) {
            // plain text flavor is deprecated, but everybody still
            // implements it.  The problem is that all the implementations
            // differ from the docs.  *sigh*
            return new StringReader(_getMoML());
        } else if (flavor.equals(namedObjFlavor)) {
            return Collections.unmodifiableList(_objectList);
        } else if (flavor.equals(DataFlavor.stringFlavor)) {
            return _getMoML();
        }

        throw new UnsupportedFlavorException(flavor);
    }

    /**
     *  Remove the given object from this transferable.
     *  If the object does not exist in the transferable, then do nothing.
     *  @param object The object to be removed.
     */
    public void removeObject(NamedObj object) {
        if (_objectList.contains(object)) {
            _objectList.remove(object);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /**
     * The flavor that requests a local virtual machine
     * reference to the contained object.
     */
    public static final DataFlavor namedObjFlavor;

    static {
        // Under MacOS X 10.2, Java 1.4.1_01 we get a stack trace
        // when ever we drag and drop.  For details See
        // http://lists.apple.com/archives/java-dev/2003/Apr/16/classcastexceptionindrag.txt
        // FIXME: This change happened just before the release of 3.0.2,
        // so we only make the change under Mac OS.
        if (PtGUIUtilities.macOSLookAndFeel()) {
            namedObjFlavor = new DataFlavor(
                    DataFlavor.javaJVMLocalObjectMimeType
                    + ";class=ptolemy.kernel.util.NamedObj",
                    "Named Object");
        } else {
            namedObjFlavor = new DataFlavor(
                    DataFlavor.javaJVMLocalObjectMimeType
                    + "ptolemy.kernel.util.NamedObj", "Named Object");
        }
    }

    /** Return a string with a MoML description of all the objects in
     *  the list.
     *  @return the MoML description.
     *  @exception IOException If thrown while creating the MoML.
     */
    public String _getMoML() throws IOException {
        StringWriter buffer = new StringWriter();
        buffer.write("<group>\n");

        Iterator elements = Collections.unmodifiableList(_objectList)
                .iterator();

        while (elements.hasNext()) {
            NamedObj element = (NamedObj) elements.next();

            // first level to avoid obnoxiousness with toplevel translations.
            element.exportMoML(buffer, 1);
        }

        buffer.write("</group>\n");
        return buffer.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // The flavors that this node can return.
    private final DataFlavor[] _flavors = { DataFlavor.plainTextFlavor,
            DataFlavor.stringFlavor, namedObjFlavor, };

    //The object contained by this transferable.
    private List _objectList;
}
