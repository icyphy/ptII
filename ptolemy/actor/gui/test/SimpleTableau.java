/* A simple Tableau that does not do much.

 Copyright (c) 2006-2014 The Regents of the University of California.
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
package ptolemy.actor.gui.test;

import java.io.File;
import java.io.IOException;

import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.gui.TableauFactory;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelRuntimeException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// SimpleTableau

/**
 A simple tableau.


 @author Steve Neuendorffer and Edward A. Lee
 @version $Id$
 @since Ptolemy II 5.2
 @Pt.ProposedRating Red (neuendor)
 @Pt.AcceptedRating Red (neuendor)
 */
public class SimpleTableau extends Tableau {
    /** Create a new run control panel for the model with the given
     *  effigy.  The tableau is itself an entity contained by the effigy
     *  and having the specified name.  The frame is not made visible
     *  automatically.  You must call show() to make it visible.
     *  @param container The containing effigy.
     *  @param name The name of this tableau within the specified effigy.
     *  @exception IllegalActionException If the tableau is not acceptable
     *   to the specified container.
     *  @exception NameDuplicationException If the container already contains
     *   an entity with the specified name.
     */
    public SimpleTableau(PtolemyEffigy container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    /** Close this tableau.
     *  @return Always return true.
     */
    @Override
    public boolean close() {
        Effigy effigy = (Effigy) getContainer();
        System.out.println("SimpleTableau.close(): effigy: " + effigy);
        if (effigy.isModified()) {
            File file = effigy.getWritableFile();
            System.out.println("Writing " + file);
            try {
                effigy.writeFile(file);
                effigy.setModified(false);
            } catch (IOException ex) {
                throw new KernelRuntimeException(effigy, "Failed to write "
                        + file);
            }
        }
        return true;
    }

    /** A factory that creates run control panel tableaux for Ptolemy models.
     */
    public static class Factory extends TableauFactory {
        /** Create a factory with the given name and container.
         *  @param container The container.
         *  @param name The name.
         *  @exception IllegalActionException If the container is incompatible
         *   with this attribute.
         *  @exception NameDuplicationException If the name coincides with
         *   an attribute already in the container.
         */
        public Factory(NamedObj container, String name)
                throws IllegalActionException, NameDuplicationException {
            super(container, name);
        }

        ///////////////////////////////////////////////////////////////////
        ////                         public methods                    ////

        /** If the specified effigy already contains a tableau named
         *  "simpleTableau", then return that tableau; otherwise, create
         *  a new instance of SimpleTableau for the effigy, and
         *  name it "simpleTableau".  If the specified effigy is not an
         *  instance of PtolemyEffigy, then do not create a tableau
         *  and return null. It is the responsibility of callers of
         *  this method to check the return value and call show().
         *
         *  @param effigy The model effigy.
         *  @return A new run tableau if the effigy is a PtolemyEffigy,
         *    or null otherwise.
         *  @exception Exception If the factory should be able to create a
         *   tableau for the effigy, but something goes wrong.
         */
        @Override
        public Tableau createTableau(Effigy effigy) throws Exception {
            if (effigy instanceof PtolemyEffigy) {
                // First see whether the effigy already contains a SimpleTableau.
                SimpleTableau tableau = (SimpleTableau) effigy
                        .getEntity("simpleTableau");

                if (tableau == null) {
                    tableau = new SimpleTableau((PtolemyEffigy) effigy,
                            "simpleTableau");
                }

                // Don't call show() here, it is called for us in
                // TableauFrame.ViewMenuListener.actionPerformed()
                return tableau;
            } else {
                return null;
            }
        }
    }

    /** A factory that creates run control panel tableaux for the model
     *  associated with a top-level effigy (one that has a file
     *  representation).
     */
    public static class TopFactory extends Factory {
        /** Create a factory with the given name and container.
         *  @param container The container.
         *  @param name The name.
         *  @exception IllegalActionException If the container is incompatible
         *   with this attribute.
         *  @exception NameDuplicationException If the name coincides with
         *   an attribute already in the container.
         */
        public TopFactory(NamedObj container, String name)
                throws IllegalActionException, NameDuplicationException {
            super(container, name);
        }

        ///////////////////////////////////////////////////////////////////
        ////                         public methods                    ////

        /** Create a tableau to run the model associated with the specified
         *  effigy.  The top-level effigy, as returned by
         *  {@link Effigy#masterEffigy()}, is the one that is run.
         *  If that effigy already contains a tableau named
         *  "simpleTableau", then return that tableau; otherwise, create
         *  a new instance of SimpleTableau for the top effigy, and
         *  name it "simpleTableau".  If the specified effigy is not an
         *  instance of PtolemyEffigy, then do not create a tableau
         *  and return null. It is the responsibility of callers of
         *  this method to check the return value and call show().
         *
         *  @param effigy The model effigy.
         *  @return A new run tableau if the effigy is a PtolemyEffigy,
         *    or null otherwise.
         *  @exception Exception If the factory should be able to create a
         *   tableau for the effigy, but something goes wrong.
         */
        @Override
        public Tableau createTableau(Effigy effigy) throws Exception {
            return super.createTableau(effigy.masterEffigy());
        }
    }
}
