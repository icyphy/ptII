/* A class that writes JHDL files.

 Copyright (c) 2001-2003 The Regents of the University of California.
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
@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.copernicus.jhdl;

import java.io.*;
import java.util.*;
import soot.util.*;
import soot.toolkits.graph.*;

//////////////////////////////////////////////////////////////////////////
//// CircuitCreator
/**

@author Steve Neuendorffer and Ben Warlick
@version $Id$
@since Ptolemy II 2.0
*/
public class CircuitCreator {

    public static void create(HashMutableDirectedGraph operatorGraph,
            String outDir, String packageName,
            String className) throws IOException {
        String fileName = outDir + "/" + className + ".java";
        System.out.println("Creating JHDL file: " + fileName);
        File outputFile = new File(fileName);
        FileWriter writer = new FileWriter(outputFile);

        write_header(writer, packageName, className);

        for (Iterator nodes = operatorGraph.getNodes().iterator();
             nodes.hasNext();) {
            Object node = nodes.next();
            writer.write("    Wire " + _getWireName(node) + " = wire(32);\r\n");
        }

        for (Iterator nodes = operatorGraph.getNodes().iterator();
             nodes.hasNext();) {
            Object node = nodes.next();

            if (node.toString().startsWith("delay")) {
                Object pred = operatorGraph.getPredsOf(node).iterator().next();
                write_reg(writer, pred, node);
            } else if (node.toString().startsWith("FIR")) {
                Object pred = operatorGraph.getPredsOf(node).iterator().next();
                write_fir(writer, pred, node);
            }
            else if (node.toString().startsWith("add")) {
                Iterator preds = operatorGraph.getPredsOf(node).iterator();
                Object in1 = preds.next();
                Object in2 = preds.next();

                write_add(writer, in1, in2, node);

            } else if (node.toString().startsWith("buf")) {
                Object pred = operatorGraph.getPredsOf(node).iterator().next();
                write_buf(writer, pred);
            } else {
                try {
                    write_const(writer, node.toString(),
                            Integer.parseInt(node.toString()));
                } catch (Exception ex) {
                }
            }
        }
        write_footer(writer);

        writer.close();
    }

    static void write_reg(FileWriter writer, Object in, Object out)
            throws IOException {
        writer.write("    regc_o(" + _getWireName(in) + ", " +
                _getWireName(out) + ");\r\n");
    }

    static void write_fir(FileWriter writer, Object in, Object out)
            throws IOException {
        writer.write("    new byucc.ptolemy.domains.jhdl.lib.JHDLSimpleFir(this,"
                + _getWireName(in) + ", " +
                _getWireName(out) + ");\r\n");
    }

    static void write_add(FileWriter writer, Object in1,
            Object in2, Object out) throws IOException {
        writer.write("    add_o(" + _getWireName(in1) + ", " +
                _getWireName(in2) + ", " +
                _getWireName(out) + ");\r\n");
    }

    static void write_const(FileWriter writer, Object out, int value)
            throws IOException {

        writer.write("    constant_o(" + _getWireName(out) + ", " +
                value + ");\r\n");
    }

    static void write_buf(FileWriter writer, Object in)
            throws IOException {
        writer.write("    buf_o(" + _getWireName(in) +
                ", LAD_Bus_Data_Out);\r\n");
    }

    static void write_header(FileWriter writer,
            String packageName, String className)
            throws IOException {
        writer.write("package " + packageName + ";\r\n");
        writer.write("import byucc.jhdl.base.*;\r\n");
        writer.write("import byucc.jhdl.Logic.*;\r\n");
        writer.write("import byucc.jhdl.modgen.*;\r\n");
        writer.write("import byucc.jhdl.platforms.wildcard.*;\r\n");
        writer.write("import byucc.jhdl.platforms.wildcard.std_if.*;\r\n");
        writer.write("import byucc.jhdl.platforms.util.*;\r\n");
        writer.write("import byucc.jhdl.modgen.arrayMult.*;\r\n");

        writer.write("public class " + className + " extends LogicCore {\r\n");

        writer.write("  public static CellInterface cell_interface[] = {\r\n");
        writer.write("    clk(\"K_Clk\"),\r\n");
        writer.write("    out(\"LAD_Bus_Data_Out\",32),\r\n");


        writer.write("  };\r\n");

        writer.write("  public " + className + "(wc_pe parent){\r\n");
        writer.write("    super(parent);\r\n");

        writer.write("    Wire K_Clk=connect(\"K_Clk\",wa(\"K_Clk\"));\r\n");

        writer.write("    Wire LAD_Bus_Data_Out = connect(\"LAD_Bus_Data_Out\",\r\n");
        writer.write("                    wa(\"LAD_Bus_Data_Out\"));\r\n");


        writer.write("    setDefaultClock(K_Clk);\r\n");
    }

    static void write_footer(FileWriter writer)
            throws IOException {
        writer.write("  }\r\n");

        writer.write("  protected GenericInterfaceCell Clocks(GenericProcessingElement parent) {\r\n");
        writer.write("    return new K_Clock_IF(parent,\"K_Clock_IF\",constraints);\r\n");
        writer.write("  }\r\n");

        writer.write("}\r\n");
    }


    private static String _getWireName(Object node) {
        return "wire" + node.toString();
    }
}
