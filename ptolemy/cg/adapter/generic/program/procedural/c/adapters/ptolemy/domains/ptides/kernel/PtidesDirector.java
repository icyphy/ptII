/* Code generator adapter class associated with the PtidesDirector class.

 Copyright (c) 2009-2013 The Regents of the University of California.
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

package ptolemy.cg.adapter.generic.program.procedural.c.adapters.ptolemy.domains.ptides.kernel;

import java.util.Map;

import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.SuperdenseDependency;
import ptolemy.cg.adapter.generic.program.procedural.c.adapters.ptolemy.domains.de.kernel.DEDirector;
import ptolemy.cg.kernel.generic.CodeGeneratorAdapter;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
////PtidesDirector

/**
* Code generator adapter associated with the PtidesDirector class.
* This adapter is highly experimental and extends the DE Director
* adapter.
* This class is also associated with a code generator.
*
*  @author William Lucas based on PtidesDirector.java by Patricia Derler, Edward A. Lee, Slobodan Matic, Mike Zimmer, Jia Zou
*  @version $Id$
*  @since Ptolemy II 10.0
*  @Pt.ProposedRating red (wlc)
*  @Pt.AcceptedRating red (wlc)
*/

public class PtidesDirector extends DEDirector {

    /** Construct the code generator adapter associated with the given
     *  PtidesDirector.
     *  @param ptidesDirector The associated
     *  ptolemy.domains.ptides.kernel.PtidesDirector
     */
    public PtidesDirector(
            ptolemy.domains.ptides.kernel.PtidesDirector ptidesDirector) {
        super(ptidesDirector);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Generate the constructor code for the specified director
     * In this class we initialize the director with its internal
     * parameters and fields.
     * Also we fill the hashmaps that the director needs :
     * superdenseDependencyPair, _inputEventQueue, _outputEventDeadlines,
     * _ptidesOutputPortEventQueue ...
     *
     * @return The generated constructor code
     * @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    public String generateConstructorCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer(super.generateConstructorCode());

        code.append(_eol + _sanitizedDirectorName
                + "->_superdenseDependencyPair = pblMapNewHashMap();");
        code.append(_eol + "PblMap* tempMap;");
        code.append(_eol + "struct SuperdenseDependency tempEntry;");
        ptolemy.domains.ptides.kernel.PtidesDirector director = (ptolemy.domains.ptides.kernel.PtidesDirector) getComponent();
        Map<TypedIOPort, Map<TypedIOPort, SuperdenseDependency>> superdenseDependencyPair = director
                .getSuperdenseDependencyPair();
        for (TypedIOPort port1 : superdenseDependencyPair.keySet()) {
            Map<TypedIOPort, SuperdenseDependency> value = superdenseDependencyPair
                    .get(port1);
            code.append(_eol + "tempMap = pblMapNewHashMap();");
            for (TypedIOPort port2 : value.keySet()) {
                SuperdenseDependency dependency = value.get(port2);

                code.append(_eol + "tempEntry.time = " + dependency.timeValue()
                        + ";");
                code.append(_eol + "tempEntry.microstep = "
                        + dependency.indexValue() + ";");
                String port2Name;
                if (port2.getContainer().equals(director.getContainer())) {
                    port2Name = port2.getName();
                } else {
                    port2Name = CodeGeneratorAdapter.generateName(port2
                            .getContainer()) + "_" + port2.getName();
                }
                code.append(_eol + "pblMapAdd(tempMap, &" + port2Name
                        + ", sizeof(struct TypedIOPort*), &tempEntry"
                        + ", sizeof(struct SuperdenseDependency));");
            }
            String port1Name;
            if (port1.getContainer().equals(director.getContainer())) {
                port1Name = port1.getName();
            } else {
                port1Name = CodeGeneratorAdapter.generateName(port1
                        .getContainer()) + "_" + port1.getName();
            }
            code.append(_eol + "pblMapAdd(" + _sanitizedDirectorName
                    + "->_superdenseDependencyPair, &" + port1Name
                    + ", sizeof(struct TypedIOPort*), tempMap"
                    + ", sizeof(PblMap));");
        }

        return code.toString();
    }
}
