/*

 Copyright (c) 2008-2009 The Regents of the University of California.
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
package ptolemy.actor.gt.controller;

import java.io.IOException;
import java.io.Writer;

import ptolemy.data.ArrayToken;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.domains.ptera.kernel.PteraErrorEvent;
import ptolemy.domains.ptera.kernel.PteraDebugEvent;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// ExportMoML

/**


 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class WriteModel extends GTEvent {

    /**
     * @param container
     * @param name
     * @exception IllegalActionException
     * @exception NameDuplicationException
     */
    public WriteModel(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        modelFile = new FileParameter(this, "modelFile");
    }

    public RefiringData fire(ArrayToken arguments) throws IllegalActionException {
        RefiringData data = super.fire(arguments);

        CompositeEntity model = getModelParameter().getModel();
        Writer writer = modelFile.openForWriting();
        try {
            String fileName = modelFile.asFile().getName();
            int period = fileName.indexOf('.');
            String modelName;
            if (period >= 0) {
                modelName = fileName.substring(0, period);
            } else {
                modelName = fileName;
            }
            model.exportMoML(writer, 0, modelName);
            writer.close();
            _debug(new PteraDebugEvent(this, "Write file " + modelFile.asURL()));
        } catch (IOException e) {
            _debug(new PteraErrorEvent(this, "Unable to write file " +
                    modelFile.asURL()));
            throw new IllegalActionException(this, e, "Unable to output " +
                    "to file \"" + modelFile.stringValue().trim() + "\".");
        }

        return data;
    }

    public FileParameter modelFile;

    public StringParameter moml;
}
