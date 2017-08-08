/* Reload the accessors in a model and save the updated version.

   Copyright (c) 2016 The Regents of the University of California.
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
   COPYRIGHTENDKEY 2
*/

package ptolemy.vergil.basic.imprt.accessor;

import java.io.File;
import java.lang.reflect.Method;

import javax.swing.SwingUtilities;

import ptolemy.actor.gui.ConfigurationApplication;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.kernel.CompositeEntity;
import ptolemy.util.StringUtilities;
import ptolemy.vergil.basic.BasicGraphFrame;


///////////////////////////////////////////////////////////////////
//// ReloadAccessors

/**
 *  Reload all the Acessors in a model and save the new updated model.
 *
 *  @author  Christopher Brooks
 *  @version $Id$
 *  @since Ptolemy II 11.0
 *  @Pt.ProposedRating Red (cxh)
 *  @Pt.AcceptedRating Red (cxh)
 */
@SuppressWarnings("serial")
public class ReloadAccessors {
    /** Reload the accessors in the command line arguments and save
     *  the updated models.
     *
     *  <p>To run this class, use $PTII/bin/ptinvoke, which sets the
     *  classpath properly:</p>
     *
     *  <pre>
     *  $PTII/bin/ptinvoke ptolemy.vergil.basic.imprt.accessor.ReloadAccessors $PTII/org/terraswarm/accessor/demo/Audio/Audio.xml
     *  </pre>
     *
     *  <p>To reload all the accessors, use
     *  <code>$PTII/bin/reloadAllAccessors</code>.  </p>
     *
     *  @param args The file names of the Ptolemy models.
     */
    public static void main(String [] args) {
        try {
            for (int i = 0; i < args.length; i++) {
                System.out.println("ReloadAccessors: " + args[i]);
                ReloadAccessors.reloadAccessors(args[i]);
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }


    /** Reload the accessors in a file and save the updated model.
     *  @param modelFileName The file name of the model.
     *  @exception Throwable If the model cannot be opened, accessors
     *  cannot be reloaded or the model saved.
     */
    public static void reloadAccessors(final String modelFileName) throws Throwable {
        String oldValue = StringUtilities.getProperty("ptolemy.ptII.doNotExit");
        System.setProperty("ptolemy.ptII.doNotExit", "true");
        Runnable reloadAccessorsAction = new Runnable() {
                @Override
                public void run() {
                    try {
                        // Open the model.
                        CompositeEntity model = ConfigurationApplication
                            .openModelOrEntity(modelFileName);

                        // Reload the accessors and don't reload
                        // accessors that have local modifications.
                        _accessorReloader.invoke(null, model, false);

                        // Save the model.
                        BasicGraphFrame basicGraphFrame = BasicGraphFrame.getBasicGraphFrame(model);
                        // Coverity Scan suggests that getBasicGraphFrame() could return null.
                        if (basicGraphFrame == null) {
                            throw new NullPointerException("The BasicGraphFrame for " + model + " is null?");
                        }
                            ((PtolemyEffigy) basicGraphFrame.getTableau()
                                .getContainer()).writeFile(new File(
                                                modelFileName));

                        // Close the model.
                        ConfigurationApplication
                            .closeModelWithoutSavingOrExiting(model);

                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                        throw new RuntimeException(throwable);
                    }
                }
            };
        SwingUtilities.invokeAndWait(reloadAccessorsAction);
        System.setProperty("ptolemy.ptII.doNotExit", oldValue);
    }

    static {
        try {
            Class accessorClass = Class.forName("org.terraswarm.accessor.JSAccessor");
            _accessorReloader = accessorClass.getDeclaredMethod("reloadAllAccessors", new Class [] {CompositeEntity.class, boolean.class});
        } catch (ClassNotFoundException ex) {
            throw new ExceptionInInitializerError(ex);
        } catch (NoSuchMethodException ex2) {
            throw new ExceptionInInitializerError(ex2);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The
     * org.terraswarm.accessor.JSAccessor.reloadAllAccesors(CompositeEntity)
     * method.
     */
    protected static final Method _accessorReloader; }

