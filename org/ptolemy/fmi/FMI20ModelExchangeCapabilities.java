/* A Function Mock-up Interface Co-Simulation Capabilities object.

   Copyright (c) 2012-2014 The Regents of the University of California.
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
package org.ptolemy.fmi;

import org.w3c.dom.Element;

///////////////////////////////////////////////////////////////////
//// FMI20ModelExchangeCapabilities

/**
 * An object that represents the the capabilities of a FMI Model
 * Exchange FMU.  for FMI-2.0.
 *
 * <p>
 * A Functional Mock-up Unit file is a .fmu file in zip format that
 * contains a .xml file named "modelDescription.xml".  In FMI-2.0, the xml
 * file may optionally contain a "ModelExchange" element that defines
 * the capabilities of the Model Exchange FMU.</p>
 *
 * <p>FMI documentation may be found at
 * <a href="https://fmi-standard.org/">https://fmi-standard.org/</a>.
 * </p>
 *
 * @author Christopher Brooks
@version $Id$
@since Ptolemy II 10.0
 * @version $Id$
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class FMI20ModelExchangeCapabilities extends FMICapabilities {

    /** Create an empty Capability. */
    public FMI20ModelExchangeCapabilities() {
    }

    /** Create a FMIModelExchangeCapability from an XML Element.
     *  @param element The XML Element that contains attributes.
     */
    public FMI20ModelExchangeCapabilities(Element element) {
        super(element);
    }

    ///////////////////////////////////////////////////////////////////
    ////             public fields                                 ////

    /** The underscore separated class name.  This is the only
     *  required attribute.
     */
    public String modelIdentifier;

    /** True if only one FMU can be instantiated per process.
     *  The default value is false.
     */
    public boolean canBeInstantiatedOnlyOncePerProcess;

    /** If true, then the FMU can get and set state.  The follow
     *  functions are supported: fmiGetFMUstate(), fmiSetFMUstate(),
     *  and fmiFreeFMUstate().
     *  The default value is false.
     */
    public boolean canGetAndSetFMUstate;

    /** True if the slave ignores the allocateMemory()
     *  and freeMemory() callback functions and the
     *  slave uses its own memory management.
     *  The default value is false.
     */
    public boolean canNotUseMemoryManagementFunctions;

    /** True if the environment can serialize the internal FMU state,
     *  meaning that the following functions are supported:
     *  fmiDeSerializeFMUstate(), fmiSerialFMUState() and
     *  fmiSerializeFMUstateSize() are supported.  If this flag is
     *  true, then the canGetAndSetFMUstate flag must also be true.
     *  The default value is false.
     */
    public boolean canSerializeFMUstate;

    /** True if fmiCompletedIntegratorStep() does not need to be
     *  called.  "If it is called, it has no effect".  The default
     *  value is false, meaning that fmiCompletedIntegratorStep()
     *  "must be called after every integration step."
     */
    public boolean completedIntegratorStepNotNeeded;

    /** The FMU contains only what is necessary to communicate
     *  with the external tool.
     *  The default value is false.
     */
    public boolean needsExecutionTool;

    /** True if the fmiGetDirectionalDerivativMethod can be used to
     * compute the direction derivative.  The default value is false.
     */
    public boolean providesDirectionalDerivative;
}
