/* An adapter class for ptolemy.actor.lib.fmi.FMUImport

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
package ptolemy.cg.adapter.generic.program.procedural.fmima.adapters.ptolemy.actor.lib.fmi;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.cg.kernel.generic.program.procedural.fmima.FMIMACodeGeneratorAdapter;
import ptolemy.kernel.util.IllegalActionException;

import java.util.List;
import java.util.Enumeration;
import java.util.zip.*;
import java.io.*;

import org.ptolemy.fmi.FMIScalarVariable;

import com.microstar.xml.XmlParser;

//////////////////////////////////////////////////////////////////////////
//// FMUImport

/**
 An adapter class for ptolemy.actor.lib.fmi.FMUImport.

 @author Christopher Brooks
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class FMUImport extends FMIMACodeGeneratorAdapter {
    /**
     *  Construct the FMUImport adapter.
     *  @param actor the associated actor
     */
    public FMUImport(ptolemy.actor.lib.fmi.FMUImport actor) {
        super(actor);
    }

    /** Generate FMIMA code.
     *  @return The generated FMIMA.
     *  @exception IllegalActionException If there is a problem getting the adapter, getting
     *  the director or generating FMIMA for the director.
     */
    @Override
    public String generateFMIMA() throws IllegalActionException {
//        StringBuffer code = new StringBuffer();
//        
//        ptolemy.actor.lib.fmi.FMUImport actor = (ptolemy.actor.lib.fmi.FMUImport) getComponent();
//        code.append(getCodeGenerator()
//                .comment(
//                        "ptolemy/cg/adapter/generic/program/procedural/fmima/adapters/ptolemy/actor/lib/fmi/FMUImport.java "
//                                + _eol
//                                + "   "
//                                + actor.getName()
//                                + " is a FMUImport: "));
//        
//        
//        for (TypedIOPort input : actor.inputPortList()) {
//        	
//        	List connected_ports = input.connectedPortList();
//        	
//        	String input_module_name = input.getContainer().getName();
//        	        	
//        	for (int port_idx = 0; port_idx < connected_ports.size(); port_idx++)
//        	{
//        		TypedIOPort output = (TypedIOPort)connected_ports.get(port_idx);
//        		ptolemy.actor.lib.fmi.FMUImport source_actor = (ptolemy.actor.lib.fmi.FMUImport) output.getContainer();
//        		String out_module_name = output.getContainer().getName();
//        		
//        		code.append("connections[" + out_module_name + "_" + output.getName() + "].sourceFMU = &fmus[" + out_module_name + "];\n"
//        				+ "connections[" + out_module_name + "_" + output.getName() + "].sourcePort = getValueReference(getScalarVariable(fmus["
//        					+ out_module_name + "].modelDescription, " + source_actor.getValueReference(output.getName()) + "));\n"
//                        + "connections[" + out_module_name + "_" + output.getName() + "].sourceType = " + source_actor.getTypeOfPort(output.getName()) + ";\n"
//                        + "connections[" + out_module_name + "_" + output.getName() + "].sinkFMU = &fmus[" + input_module_name + "];\n"
//                        + "connections[" + out_module_name + "_" + output.getName() + "].sinkPort = getValueReference(getScalarVariable(fmus["
//                        	+ input_module_name + "].modelDescription, " + actor.getValueReference(input.getName()) + "));\n"
//                        + "connections[" + out_module_name + "_" + output.getName() + "].sinkType = " + actor.getTypeOfPort(input.getName()) + ";\n");
//        	}
//
//        }        
//        return /*processCode(code.toString())*/code.toString();
    	return "";
    }
}
