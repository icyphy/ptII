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
package ptolemy.data.properties;

import ptolemy.actor.gt.controller.GTEvent;
import ptolemy.data.Token;
import ptolemy.domains.ptera.kernel.PteraDebugEvent;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// AnalyzeModel

/**


 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class AnalyzeModel extends GTEvent {

    /**
     *  @param container
     *  @param name
     *  @exception IllegalActionException
     *  @exception NameDuplicationException
     */
    public AnalyzeModel(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        _analyzerWrapper = new AnalyzerAttribute(this, "_analyzerWrapper");
    }

    public RefiringData fire(Token arguments) throws IllegalActionException {
        RefiringData data = super.fire(arguments);

        _debug(new PteraDebugEvent(this, "Start analysis."));

        long start = System.currentTimeMillis();

        CompositeEntity entity = getModelParameter().getModel();

        _analyzerWrapper.analyze(entity);

        getModelParameter().setModel(entity);

        long elapsed = System.currentTimeMillis() - start;
        if (data == null) {
            _debug(new PteraDebugEvent(this, "Finish analysis (" +
                    (double) elapsed / 1000 + " sec)."));
        } else {
            _debug(new PteraDebugEvent(this, "Request refire (" +
                    (double) elapsed / 1000 + " sec)."));
        }

        return data;
    }


    private AnalyzerAttribute _analyzerWrapper;
}
