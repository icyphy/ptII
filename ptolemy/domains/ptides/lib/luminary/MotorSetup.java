/*
@Copyright (c) 2008-2014 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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

package ptolemy.domains.ptides.lib.luminary;

import ptolemy.domains.ptides.lib.ActuationDevice;
import ptolemy.domains.ptides.lib.ActuatorSetup;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
 * This is a class for generating motor outputs on the Luminary Micro.
 *
 * @author Jia Zou
 * @version $ld$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Yellow (jiazou)
 * @Pt.AcceptedRating Red (jiazou)
 *
 */
public class MotorSetup extends ActuatorSetup implements ActuationDevice {

    /**
     * Constructs a MotorOutputDevice  object.
     *
     * @param container The container.
     * @param name The name of this actor within the container.
     * @exception IllegalActionException if the super constructor throws it.
     * @exception NameDuplicationException if the super constructor throws it.
     */
    public MotorSetup(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }
}
