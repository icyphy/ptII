/* Singleton class for displaying exceptions, errors, warnings, and messages that
includes a button to open the actor that caused the problem

 Copyright (c) 2010-2014 The Regents of the University of California.
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
package ptolemy.actor.gui;

import java.util.Iterator;

import ptolemy.gui.GraphicalMessageHandler;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.KernelRuntimeException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// GraphicalMessageHandler

/**
 A message handler that optionally includes a button that opens the model
 that contains the actor that caused the exception.

 @author  Christopher Brooks
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class ActorGraphicalMessageHandler extends GraphicalMessageHandler {

    /** Under certain circumstances, add a "Go To Actor" button to the
     *  options array.  The button is added to the array if the
     *  throwable is a KernelException or a KernelRuntimeException
     *  with a non-null Nameable and the Nameable is not at the
     *  toplevel.
     *  @param options An array of Strings, suitable for passing to
     *  JOptionPane.showOptionDialog().
     *  @param throwable The throwable.
     *  @return An array of Strings.  If the throwable is an instance
     *  of KernelException and non-null, then the String "Go To Actor"
     *  is added to the array as the last element.  Otherwise, the
     *  options argument is returned.
     */
    @Override
    protected Object[] _checkThrowableNameable(Object[] options,
            Throwable throwable) {

        Object[] result = options;
        Nameable nameable1 = _getNameable(throwable);
        // If the throwable has a Nameable, and is not the top level,
        // then add a button
        if (nameable1 != null && nameable1 instanceof NamedObj
                && ((NamedObj) nameable1).toplevel() != nameable1) {
            result = new Object[options.length + 1];
            System.arraycopy(options, 0, result, 0, options.length);
            result[options.length] = "Go To Actor";
        }
        return result;
    }

    /** Given a throwable, return the first Nameable (if any).
     *  @param throwable The throwable that may or may not
     *  be an instance of KernelException or KernelRuntimeException.
     *  @return The first Nameable or null if the throwable
     *  is not a KernelException or KernelRuntimeException.
     */
    protected Nameable _getNameable(Throwable throwable) {
        Nameable nameable1 = null;
        if (throwable instanceof KernelException) {
            nameable1 = ((KernelException) throwable).getNameable1();
        } else if (throwable instanceof KernelRuntimeException) {
            Iterator nameables = ((KernelRuntimeException) throwable)
                    .getNameables().iterator();
            while (nameables.hasNext()) {
                Object object = nameables.next();
                if (object instanceof NamedObj) {
                    nameable1 = (NamedObj) object;
                    break;
                }
            }

        }
        return nameable1;
    }

    /** Open the level of hierarchy of the model that contains the
     *  Nameable referred to by the KernelException or KernelRuntimeException.
     *  @param throwable The throwable that may be a KernelException
     *  or KernelRuntimeException.
     */
    @Override
    protected void _showNameable(Throwable throwable) {
        Nameable nameable1 = _getNameable(throwable);
        if (nameable1 != null) {
            Effigy effigy = Configuration.findEffigy(((NamedObj) nameable1)
                    .toplevel());
            Configuration configuration = (Configuration) effigy.toplevel();
            try {
                Nameable container = nameable1;
                while (container != null
                        && !(container instanceof CompositeEntity)) {
                    container = container.getContainer();
                }
                if (container == null) {
                    // Hmm.  Could not find container?
                    container = nameable1;
                }
                configuration.openModel((NamedObj) container);
            } catch (KernelException ex) {
                // FIXME: Could be an endless loop here if
                // we keep failing to open container.
                throw new InternalErrorException(nameable1, ex,
                        "Could not open " + nameable1.getFullName());
            }
            return;
        }

        message("Internal Error: The throwable \"" + throwable
                + "\" is not a KernelException or KernelRuntimeException?");
    }
}
