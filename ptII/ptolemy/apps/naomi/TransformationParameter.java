/*

 Copyright (c) 2008 The Regents of the University of California.
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
package ptolemy.apps.naomi;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.MoMLParser;

//////////////////////////////////////////////////////////////////////////
//// TransformationParameter

/**


 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class TransformationParameter extends NaomiParameter implements
        CompositeNaomiAttribute {

    /**
     *  @param container
     *  @param name
     *  @throws IllegalActionException
     *  @throws NameDuplicationException
     */
    public TransformationParameter(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    public String getResourceIdentifier() {
        return "Resource_" + getContainer().getName() + ".xml";
    }

    public void loadCompositeAttribute(Reader reader)
            throws IllegalActionException, IOException {
        MoMLParser parser = new MoMLParser();
        NamedObj container = getContainer();
        parser.setContext(container.getContainer());
        try {
            parser.parse(null, null, reader);
        } catch (IllegalActionException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalActionException(null, e,
                    "Unable to parse input resource.");
        }
    }

    public void saveCompositeAttribute(Writer writer)
            throws IllegalActionException, IOException {
        NamedObj container = getContainer();
        try {
            ((NamedObj) container.clone(new Workspace())).exportMoML(writer);
        } catch (CloneNotSupportedException e) {
            throw new IllegalActionException(null, e, "Unable to clone the "
                    + "associated object.");
        }
    }
}
