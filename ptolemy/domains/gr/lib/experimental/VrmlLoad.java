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
import org.jdesktop.j3d.loaders.vrml97.VrmlLoader;
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
   A VRML model loader actor.

   <p>This class uses the VRML loader from
   <a href="https://j3d-vrml97.dev.java.net/">https://j3d-vrml97.dev.java.net/</a>

   <p>To install, download and untar:
   <pre>
   wget --no-check-certificate https://j3d-vrml97.dev.java.net/files/documents/2124/3
   tar -zxf j3d-vrml97-06-04-20.tar.gz 
   </pre>

   <p>To install on the Mac:
   <pre>
   sudo cp j3d-vrml97/j3d-vrml97.jar /System/Library/Java/Extensions/
   </pre>

   @author C. Fong
   @version $Id$
   @Pt.ProposedRating Red (chf)
   @Pt.AcceptedRating Red (cxh)
*/
public class VrmlLoad extends GRPickActor {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public VrmlLoad(CompositeEntity container, String name)
        throws IllegalActionException, NameDuplicationException {
        super(container, name);
        filename = new Parameter(this, "filename", new StringToken("mwave.wrl"));
    }

    public Parameter filename;

    protected BranchGroup _getBranchGroup() {
        return (BranchGroup) obj.getSceneGroup();
    }

    protected Node _getNodeObject() {
        return (Node) obj.getSceneGroup();
    }

    protected void _createModel() throws IllegalActionException {
        String fileName = (String) ((StringToken) filename.getToken())
                    .stringValue();

        VrmlLoader loader = new VrmlLoader();
        URL loadUrl = null;
        String locString = StringUtilities.getProperty("user.dir") + File.separator
            + fileName;
        System.out.println("location:-->  " + locString);

        Scene scene = null;

//         try {
//             //loadUrl = new URL(fileName);
//             loadUrl = new URL(locString);
//         } catch (MalformedURLException ex) {
//             throw new IllegalActionException(this, ex, "Bad URL: \"" + locString
//                                              + "\"" );
//         }

        try {
            //scene = loader.load(fileName);
            scene = loader.load(locString);

            //scene = loader.load(loadUrl);
        } catch (FileNotFoundException ex) {
            throw new IllegalActionException(this, ex, "File \"" + locString
                                             + "\" not found!");
        } catch (ParsingErrorException ex) {
            throw new IllegalActionException(this, ex, "File \"" + locString
                                             + "\" is not a valid 3D OBJ file");
        } catch (IncorrectFormatException ex) {
            throw new IllegalActionException(this, ex, "File \"" + locString
                                             + "\" is not a valid 3D OBJ file");
        }

        obj = scene;
        branchGroup = obj.getSceneGroup();
    }

    public void processCallback() {
        super.processCallback();

        try {
            System.out.println("call "
                + ((StringToken) filename.getToken()).stringValue());
        } catch (Exception e) {
            System.out.println("process call back exception");
        }
    }

    private Scene obj;
}
