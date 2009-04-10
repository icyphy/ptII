package ptolemy.codegen.c.kernel.type.parameterizedtemplates;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

public class InstantiateFromTemplate {

    static HashMap<String, String> replaceMap = new HashMap<String, String>();

    /** NOTE: META SUBSTITUTION SYMBOLS
     * $Type: Int, Char, Array, etc.
     * $type_q: %d, %s, etc.
     * $type: int, char, etc.
     * $print_size: 12(int), 22(long), 22(double), 6(boolean)
     */
    
    public static void main(String[] args) throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader(new File(args[0])));
        
        StringBuffer templateCode = new StringBuffer();
        String line = reader.readLine();
        while (line != null) {
            templateCode.append(line + "\r\n");
            line = reader.readLine();
        }
        reader.close();
        
        String filename = args[1] + "\\DoubleArray.c";
        
        replaceMap.put("\\$Type", "Double");
        replaceMap.put("\\$type_q", "%g");
        replaceMap.put("\\$type", "double");
        replaceMap.put("\\$print_size", "22");
        filename = args[1] + "\\DoubleArray.c";
        _replaceAndPrintContent(templateCode, filename);

        replaceMap.put("\\$Type", "Boolean");
        replaceMap.put("\\$type_q", "%b");
        replaceMap.put("\\$type", "boolean");
        replaceMap.put("\\$print_size", "6");
        filename = args[1] + "\\BooleanArray.c";
        _replaceAndPrintContent(templateCode, filename);

        replaceMap.put("\\$Type", "Int");
        replaceMap.put("\\$type_q", "%d");
        replaceMap.put("\\$type", "int");
        replaceMap.put("\\$print_size", "12");
        filename = args[1] + "\\IntArray.c";
        _replaceAndPrintContent(templateCode, filename);

        replaceMap.put("\\$Type", "String");
        replaceMap.put("\\$type_q", "%s");
        replaceMap.put("\\$type", "string");
        replaceMap.put("\\$print_size", "100");
        filename = args[1] + "\\StringArray.c";
        _replaceAndPrintContent(templateCode, filename);
    }

    private static void _replaceAndPrintContent(StringBuffer templateCode,
            String filename) throws IOException {
        String codeString = templateCode.toString();

        for (String key : replaceMap.keySet()) {
            codeString = codeString.replaceAll(key, replaceMap.get(key));            
        }
        
        FileWriter writer = new FileWriter(new File(filename));
        writer.write(codeString);
        writer.close();
    }    
}
