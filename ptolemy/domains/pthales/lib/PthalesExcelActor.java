/* Generic actor for Pthales objects.

 Copyright (c) 2009 The Regents of the University of California.
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
package ptolemy.domains.pthales.lib;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;

import jxl.Workbook;
import jxl.write.Number;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.FloatToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

/**
 An element of ArrayOL in Ptolemy.

 <p>ArrayOL "is a high-level visual language dedicated to
 multidimensional intensive signal processing applications."

 <p>In the name of this actor, "Generic" means that the same actor
 is used to implement different functions. This actor calls a JNI
 function when fired, with arguments in correct orders. These function
 and arguments are parameters of the actor.

 <p>For details about ArrayOL, see:
 P. Boulet, <a href="http://hal.inria.fr/inria-00128840/en">Array-OL Revisited, Multidimensional Intensive Signal Processing Specification</a>,INRIA, Sophia Antipolis, France, 2007.

 @author Remi Barrere
 @see ptolemy.actor.TypedAtomicActor
 @version $Id$
 @since Ptolemy II 8.2
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class PthalesExcelActor extends PthalesAtomicActor {

    /** Construct an actor in the default workspace with an empty string
     *  as its name.  The object is added to the workspace directory.
     *  Increment the version number of the workspace.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public PthalesExcelActor() throws IllegalActionException,
            NameDuplicationException {
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
    public PthalesExcelActor(Workspace workspace)
            throws IllegalActionException, NameDuplicationException {
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
    public PthalesExcelActor(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
     }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Read the contents of the array and then call JNI function.
     *  @exception IllegalActionException If there is no director, or the
     *  input can not be read, or the output can not be sent.
     */
    public void fire() throws IllegalActionException {

        // Increment iteration counter
        _iterationCount++;

        // File name
        String destFileName = ((Parameter) getAttribute("file"))
                .getExpression();
        if (PthalesGenericActor.getIteration(this) > 0) {
            String[] name = destFileName.split("\\.");
            destFileName = name[0] + "_" + _iterationCount + "." + name[1];
        }
        // Macro used
        String macro = ((Parameter) getAttribute("macro")).getExpression();

        // Variables
        IOPort portIn = null;

        // Input ports 
        portIn = (IOPort) getPort("in");

        // BEFORE CALLING TASK //
        // Input ports created and filled before elementary task called 
        int dataSize = PthalesIOPort.getDataProducedSize(portIn)
                * PthalesIOPort.getNbTokenPerData(portIn);
        // Token Arrays from simulation
        Token[] tokensIn = new Token[dataSize];
        tokensIn = portIn.get(0, dataSize);

        // Excel open
        WritableWorkbook workbook = null;
        WritableSheet sheet = null;
        try {
            workbook = Workbook.createWorkbook((new File(destFileName)));
            sheet = workbook.createSheet("Res", 0);

        } catch (IOException e) {
            e.printStackTrace();
        }

        LinkedHashMap<String, Integer> sizes = PthalesIOPort.getArraySizes(portIn);
        int size = PthalesIOPort.getDataProducedSize(portIn)
                * PthalesIOPort.getNbTokenPerData(portIn);

        Object[] dims = sizes.keySet().toArray();
        int reps[] = new int[dims.length + 1];

        Number n = null;
        int posX = 0;
        double module = 0;

        int[] jump = new int[dims.length - 1];
        jump[0] = 1;
        for (int j = 2; j < dims.length; j++) {
            jump[j - 1] = jump[j - 2] * sizes.get(dims[j - 1]);
        }

        // Module output
        for (int i = 0; i < size / 2; i++) {

            // Module computation (type dependent)
            if (tokensIn[i] instanceof FloatToken) {
                module = Math.sqrt(((FloatToken) tokensIn[2 * i]).floatValue()
                        * ((FloatToken) tokensIn[2 * i]).floatValue()
                        + ((FloatToken) tokensIn[2 * i + 1]).floatValue()
                        * ((FloatToken) tokensIn[2 * i + 1]).floatValue());
            }
            if (tokensIn[i] instanceof IntToken) {
                module = Math.sqrt(((IntToken) tokensIn[2 * i]).intValue()
                        * ((IntToken) tokensIn[2 * i]).intValue()
                        + ((IntToken) tokensIn[2 * i + 1]).intValue()
                        * ((IntToken) tokensIn[2 * i + 1]).intValue());
            }

            // Setting module to a cell
            n = new Number(posX, reps[0], module);

            try {
                // Add the cell to the sheet
                sheet.addCell(n);
            } catch (RowsExceededException e) {
                e.printStackTrace();
            } catch (WriteException e) {
                e.printStackTrace();
            }

            reps[0]++;

            posX = 0;
            for (int nRep = 0; nRep < dims.length; nRep++) {
                if (reps[nRep] == sizes.get(dims[nRep])) {
                    reps[nRep] = 0;
                    reps[nRep + 1]++;
                }

                if (nRep >= 1) {
                    posX += reps[nRep] * jump[nRep - 1];
                }
            }
        }

        try {
            // Excel output
            workbook.write();

            // Call to macro 
            Runtime.getRuntime().exec(
                    "cmd /c start " + macro + " " + destFileName);

        } catch (IOException e) {
            e.printStackTrace();
        }
        
        
        try {
            // Close Excel file
            workbook.close();
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
    public boolean postfire() throws IllegalActionException {
        // FIXME: This should either call super.postfire()
        // or else print the debugging message as AtomicActor.postfire() does.
        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////
    protected int _iterationCount = 0;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    protected void _initialize() throws IllegalActionException,
            NameDuplicationException {
        super._initialize();

        // Reset iteration number (used for excel file name)
        _iterationCount = 0;

        // Only has an input port
        PthalesIOPort.initialize(new TypedIOPort(this, "in", true, false));

        if (getAttribute("file") == null) {
            Parameter file = new StringParameter(this, "file");
            file.setExpression("");
        }

        if (getAttribute("macro") == null) {
            Parameter macro = new StringParameter(this, "macro");
            macro.setExpression("");
        }
    }
}
