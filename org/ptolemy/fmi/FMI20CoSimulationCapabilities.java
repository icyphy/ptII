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
//// FMI20CoSimulationCapbilities

/**
 * An object that represents the the capabilities of a FMI co-simulation
 * slave for FMI-2.0.
 *
 * <p>
 * A Functional Mock-up Unit file is a .fmu file in zip format that
 * contains a .xml file named "modelDescription.xml".  In FMI-2.0, the xml
 * file may optionally contain a "CoSimulation" element that defines
 * the capabilities of the CoSimulation FMU.</p>
 *
 * <p>FMI documentation may be found at
 * <a href="https://fmi-standard.org/">https://fmi-standard.org/</a>.
 * </p>
 *
 * <p>FMI documentation may be found at
 * <a href="http://www.modelisar.com/fmi.html">http://www.modelisar.com/fmi.html</a>.
 * </p>
 *
 * @author Christopher Brooks
@version $Id$
@since Ptolemy II 10.0
 * @version $Id$
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class FMI20CoSimulationCapabilities extends FMICapabilities {

    /** Create an empty Capability. */
    public FMI20CoSimulationCapabilities() {
    }

    /** Create a FMICoSimulationCapability from an XML Element.
     *  @param element The XML Element that contains attributes.
     */
    public FMI20CoSimulationCapabilities(Element element) {
        super(element);
    }

    ///////////////////////////////////////////////////////////////////
    ////             public fields                                 ////

    /** True if only one FMU can be instantiated per process.
     *  The default value is false.
     */
    public boolean canBeInstantiatedOnlyOncePerProcess;

    /** True if the slave ignores the allocateMemory()
    /** True if the slave can handle a variable step size.
     *  The default value is false.
     */
    public boolean canHandleVariableCommunicationStepSize;

    /** True if the step size can be zero.
     *  The default value is false.
     */
    public boolean canHandleEvents;

    /** True if slave can interpolate inputs.
     *  The default value is false.
     */
    public boolean canInterpolateInputs;

    /** True if the slave ignores the allocateMemory()
     *  and freeMemory() callback functions and the
     *  slave uses its own memory management.
     *  The default value is false.
     */
    public boolean canNotUseMemoryManagementFunctions;

    /** True if the slave can run the fmiDoStep() call
     *  asynchronously.  The default value is false.
     */
    public boolean canRunAsynchronuously;

    /** True if the slave can discard and repeat a step.
     *  The default value is false.
     */
    public boolean canRejectSteps;

    /** True if the slave can signal events during a communication
     *  step.  If false, then the slave cannot signal events
     *  during the communication step.  The default value is
     *  false.
     */
    public boolean canSignalEvents;

    /** The slave can supply derivatives with a maximum order.n
     *  The default value is 0.
     */
    public int maxOutputDerivativeOrder;
}
