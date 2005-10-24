/* A VRML model loader actor

Copyright (c) 2000-2005 The Regents of the University of California.
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
package ptolemy.domains.gr.lib.experimental;

import ptolemy.actor.*;
import ptolemy.actor.gui.Placeable;
import ptolemy.actor.lib.*;
import ptolemy.actor.lib.gui.Display;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.gr.lib.*;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;
import ptolemy.util.StringUtilities;

import com.sun.j3d.loaders.IncorrectFormatException;
import com.sun.j3d.loaders.ParsingErrorException;
import com.sun.j3d.loaders.Scene;
import com.sun.j3d.loaders.vrml97.VrmlLoader;
import com.sun.j3d.utils.geometry.*;
import com.sun.j3d.utils.universe.*;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;

import javax.media.j3d.*;
import javax.vecmath.*;


//////////////////////////////////////////////////////////////////////////
//// VrmlLoader3D

/**
   @author C. Fong
   @version $Id$
   @Pt.ProposedRating Red (chf)
   @Pt.AcceptedRating Red (cxh)
*/
public class VrmlLoader3D extends GRShadedShape {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public VrmlLoader3D(CompositeEntity container, String name)
        throws IllegalActionException, NameDuplicationException {
        super(container, name);
        filename = new Parameter(this, "filename",
                new StringToken("chopper.obj"));
    }

    public Parameter filename;

    protected Node _getNodeObject() {
        return (Node) obj.getSceneGroup();
    }

    protected void _createModel() throws IllegalActionException {
        String fileName = (String) ((StringToken) filename.getToken())
                    .stringValue();

        VrmlLoader loader = new VrmlLoader();
        URL loadUrl = null;
        String locString = StringUtilities.getProperty("user.dir") + "\\"
            + fileName;
        System.out.println("location:-->  " + locString);

        Scene scene = null;

        try {
            //loadUrl = new URL(fileName);
            loadUrl = new URL(locString);
        } catch (MalformedURLException e) {
            System.err.println(e);
            System.out.println("bad URL damn " + locString);
        }

        try {
            //scene = loader.load(fileName);
            scene = loader.load(locString);

            //scene = loader.load(loadUrl);
        } catch (FileNotFoundException e) {
            System.err.println(e);
            throw new IllegalActionException("File not found!");
        } catch (ParsingErrorException e) {
            System.err.println(e);
            throw new IllegalActionException("File is not a valid 3D OBJ file");
        } catch (IncorrectFormatException e) {
            System.err.println(e);
            throw new IllegalActionException("File is not a valid 3D OBJ file");
        }

        obj = scene;
    }

    private Scene obj;
}
