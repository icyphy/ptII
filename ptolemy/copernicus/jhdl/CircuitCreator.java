package ptolemy.copernicus.jhdl;

import java.io.*;

public class CircuitCreator {

    public static void main(String[] args) throws IOException {
        int[] edge_array = {0,1,2,1,1,2,1,3};
        String[] node_array = {"const1","add","reg","buf"};
        
        int num_wires = edge_array.length / 2;
        
        File outputFile = new File("outputPE.java");
        FileWriter out = new FileWriter(outputFile);

        write_header(out);

        for (int i=0; i<num_wires;i++) {
            out.write("    Wire wire" + i + " = wire(32);\r\n");
        }

        for (int i=0; i<node_array.length; i++) {
            if (node_array[i] == "reg") {
                int inwire = -1, outwire = -1;
                for (int j=0; j<num_wires;j++) {
                    if (edge_array[j*2] == i && outwire == -1)
                        outwire = j;
                    if (edge_array[j*2+1] == i)
                        inwire = j;
                }
                for (int j=0;j<num_wires;j++) {
                    if (edge_array[j*2] == edge_array[inwire*2]) {
                        inwire = j;
                        break;
                    }
                }
                write_reg(out,inwire,outwire);
            }
            if (node_array[i] == "const1") {
                int outwire = -1;
                for (int j=0;j<num_wires;j++) {
                    if (edge_array[j*2] == i)
                        outwire = j;
                }
                write_const1(out,outwire);
            }
            if (node_array[i] == "add") {
                int inwire1 = -1, inwire2 = -1, outwire = -1;
                for (int j=0; j<num_wires;j++) {
                    if (edge_array[j*2+1] == i)
                        if (inwire1 == -1)
                            inwire1 = j;
                        else
                            inwire2 = j;
                    if (edge_array[j*2] == i && outwire == -1)
                        outwire = j;
                }
                write_add(out, inwire1, inwire2, outwire);
                
            }
            if (node_array[i] == "buf") {
                int inwire = -1;
                for (int j=0; j<num_wires;j++) {
                    if (edge_array[j*2+1] == i)
                        inwire = j;
                }
                for (int j=0;j<num_wires;j++) {
                    if (edge_array[j*2] == edge_array[inwire*2]) {
                        inwire = j;
                        break;
                    }
                    write_buf(out,inwire);
                }
            }
        }
        write_footer(out);

        out.close();
    }

    static void write_reg(FileWriter out, int inwire, int outwire) 
            throws IOException {
        out.write("    regc_o(wire" + inwire + ", wire" + outwire + ");\r\n");
    }

    static void write_add(FileWriter out, int inwire1, 
            int inwire2, int outwire) throws IOException {
        if (outwire == -10)
            out.write("    add_o(wire" + inwire1 + ",wire" + inwire2 + ", LAD_Bus_Data_Out);\r\n");
        else
            out.write("    add_o(wire" + inwire1 + ",wire" + inwire2 + ",wire" + outwire + ");\r\n");
    }

    static void write_const1(FileWriter out, int outwire)
            throws IOException {
        
        out.write("    constant_o(wire" + outwire + ", 1);\r\n");
    }

    static void write_buf(FileWriter out, int inwire)
            throws IOException {
        
            out.write("    buf_o(wire" + inwire + ", LAD_Bus_Data_Out);\r\n");
    }

    static void write_header(FileWriter out)
             throws IOException {
        out.write("import byucc.jhdl.base.*;\r\n");
            out.write("import byucc.jhdl.Logic.*;\r\n");
            out.write("import byucc.jhdl.modgen.*;\r\n");
            out.write("import byucc.jhdl.platforms.wildcard.*;\r\n");
            out.write("import byucc.jhdl.platforms.wildcard.std_if.*;\r\n");
            out.write("import byucc.jhdl.platforms.util.*;\r\n");
            out.write("import byucc.jhdl.modgen.arrayMult.*;\r\n");

            out.write("public class outputPE extends LogicCore {\r\n");

            out.write("  public static CellInterface cell_interface[] = {\r\n");
            out.write("    clk(\"K_Clk\"),\r\n");
            out.write("    out(\"LAD_Bus_Data_Out\",32),\r\n");


            out.write("  };\r\n");

            out.write("  public outputPE(wc_pe parent){\r\n");
            out.write("    super(parent);\r\n");

            out.write("    Wire K_Clk=connect(\"K_Clk\",wa(\"K_Clk\"));\r\n");

            out.write("    Wire LAD_Bus_Data_Out = connect(\"LAD_Bus_Data_Out\",\r\n");
            out.write("		    wa(\"LAD_Bus_Data_Out\"));\r\n");


            out.write("    setDefaultClock(K_Clk);\r\n");
    }

    static void write_footer(FileWriter out)
            throws IOException {
        out.write("  }\r\n");
        
        out.write("  protected GenericInterfaceCell Clocks(GenericProcessingElement parent) {\r\n");
        out.write("    return new K_Clock_IF(parent,\"K_Clock_IF\",constraints);\r\n");
        out.write("  }\r\n");
        
        out.write("}\r\n");
    }
}
