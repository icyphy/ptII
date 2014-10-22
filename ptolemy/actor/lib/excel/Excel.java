/* Read Excel files.

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
package ptolemy.actor.lib.excel;

import java.io.File;
import java.io.IOException;

import jxl.Workbook;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

/**
 Read Excel files.

 @author Remi Barrere
 @see ptolemy.actor.TypedAtomicActor
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class Excel extends TypedAtomicActor {

    /** Construct an actor in the default workspace with an empty string
     *  as its name.  The object is added to the workspace directory.
     *  Increment the version number of the workspace.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Excel() throws IllegalActionException, NameDuplicationException {
        super();
    }

    /** Construct an actor in the specified workspace with an empty
     *  string as a name. You can then change the name with setName().
     *  If the workspace argument is null, then use the default workspace.
     *  The object is added to the workspace directory.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the entity.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Excel(Workspace workspace) throws IllegalActionException,
            NameDuplicationException {
        super(workspace);
    }

    /** Create a new actor in the specified container with the specified
     *  name.  The name must be unique within the container or an exception
     *  is thrown. The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If this actor cannot be contained
     *   by the proposed container (see the setContainer() method).
     *  @exception NameDuplicationException If the name coincides with
     *   an entity already in the container.
     */
    public Excel(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Read the contents of the array and then call JNI function.
     *  @exception IllegalActionException If there is no director, or the
     *  input can not be read, or the output can not be sent.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();

        // Increment iteration counter
        _iterationCount++;

        // File name
        String destFileName = ((Parameter) getAttribute("file"))
                .getExpression();
        //         if (PthalesGenericActor.getIteration(this) > 0) {
        //             String[] name = destFileName.split("\\.");
        //             destFileName = name[0] + "_" + _iterationCount + "." + name[1];
        //         }
        // Macro used
        String macro = ((Parameter) getAttribute("macro")).getExpression();

        // Variables
        //IOPort portIn = null;

        // Input ports
        //portIn = (IOPort) getPort("in");

        // BEFORE CALLING TASK //
        // Input ports created and filled before elementary task called
        //        int dataSize = PthalesIOPort.getDataProducedSize(portIn)
        //                * PthalesIOPort.getNbTokenPerData(portIn);
        // Token Arrays from simulation
        //        Token[] tokensIn = new Token[dataSize];
        //        tokensIn = portIn.get(0, dataSize);

        // Excel open
        WritableWorkbook workbook = null;
        //WritableSheet sheet = null;
        try {
            workbook = Workbook.createWorkbook(new File(destFileName));
            /*sheet =*/workbook.createSheet("Res", 0);

        } catch (IOException e) {
            e.printStackTrace();
        }

        //         LinkedHashMap<String, Integer> sizes = PthalesIOPort.getArraySizes(portIn);
        //         int size = PthalesIOPort.getDataProducedSize(portIn)
        //                 * PthalesIOPort.getNbTokenPerData(portIn);

        //         Object[] dims = sizes.keySet().toArray();
        //         int reps[] = new int[dims.length + 1];

        //         Number n = null;
        //         int posX = 0;
        //         double module = 0;

        //         int[] jump = new int[dims.length - 1];
        //         jump[0] = 1;
        //         for (int j = 2; j < dims.length; j++) {
        //             jump[j - 1] = jump[j - 2] * sizes.get(dims[j - 1]);
        //         }

        //         // Module output
        //         for (int i = 0; i < size / 2; i++) {

        //             // Module computation (type dependent)
        //             if (tokensIn[i] instanceof FloatToken) {
        //                 module = Math.sqrt(((FloatToken) tokensIn[2 * i]).floatValue()
        //                         * ((FloatToken) tokensIn[2 * i]).floatValue()
        //                         + ((FloatToken) tokensIn[2 * i + 1]).floatValue()
        //                         * ((FloatToken) tokensIn[2 * i + 1]).floatValue());
        //             }
        //             if (tokensIn[i] instanceof IntToken) {
        //                 module = Math.sqrt(((IntToken) tokensIn[2 * i]).intValue()
        //                         * ((IntToken) tokensIn[2 * i]).intValue()
        //                         + ((IntToken) tokensIn[2 * i + 1]).intValue()
        //                         * ((IntToken) tokensIn[2 * i + 1]).intValue());
        //             }

        //             // Setting module to a cell
        //             n = new Number(posX, reps[0], module);

        //             try {
        //                 // Add the cell to the sheet
        //                 sheet.addCell(n);
        //             } catch (RowsExceededException e) {
        //                 e.printStackTrace();
        //             } catch (WriteException e) {
        //                 e.printStackTrace();
        //             }

        //             reps[0]++;

        //             posX = 0;
        //             for (int nRep = 0; nRep < dims.length; nRep++) {
        //                 if (reps[nRep] == sizes.get(dims[nRep])) {
        //                     reps[nRep] = 0;
        //                     reps[nRep + 1]++;
        //                 }

        //                 if (nRep >= 1) {
        //                     posX += reps[nRep] * jump[nRep - 1];
        //                 }
        //             }
        //        }

        try {
            if (workbook == null) {
                throw new NullPointerException(
                        "The workbook was not initialized?");
            } else {
                // Excel output
                workbook.write();
            }

            // Call to macro
            Runtime.getRuntime().exec(
                    "cmd /c start " + macro + " " + destFileName);

        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            // Close Excel file
            if (workbook != null) {
                workbook.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (WriteException e) {
            e.printStackTrace();
        }
    }

    /** Always return true in this base class, indicating
     *  that execution can continue into the next iteration.
     *  @return Always return true in this base class, indicating
     *  that execution can continue into the next iteration.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        if (!super.postfire()) {
            return false;
        }
        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The number of iterations, used for the Excel file name. */
    protected int _iterationCount = 0;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Initialize the actor.
     * @exception IllegalActionException If three is a problem getting
     * or setting the <i>file</i> or <i>macro</i> attribute.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    protected void _initialize() throws IllegalActionException,
            NameDuplicationException {
        //super._initialize();

        // Only has an input port
        //        PthalesIOPort.initialize(new TypedIOPort(this, "in", true, false));

        if (getAttribute("file") == null) {
            Parameter file = new StringParameter(this, "file");
            file.setExpression("");
        }

        if (getAttribute("macro") == null) {
            Parameter macro = new StringParameter(this, "macro");
            macro.setExpression("");
        }
    }

    /** Initialize this actor.  Derived classes override this method
     *  to perform actions that should occur once at the beginning of
     *  an execution, but after type resolution.  Derived classes can
     *  produce output data and schedule events.
     *
     *  @exception IllegalActionException If a derived class throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();

        // Reset iteration number (used for excel file name)
        _iterationCount = 0;

    }

}
