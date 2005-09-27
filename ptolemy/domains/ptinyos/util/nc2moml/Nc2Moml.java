package ptolemy.domains.ptinyos.util.nc2moml;

import net.tinyos.nesc.dump.xml.*;
import net.tinyos.nesc.dump.*;

import org.jdom.DocType;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import java.io.*;
import java.util.*;
import org.xml.sax.*;

/** 
 * Generate a .moml file for each .nc file in the input list.
 * 
  Usage:
    Nc2Moml <xml input prefix> <xml input suffix> <nc sub prefix> <moml output prefix>
           [short path to .nc files]
    
  Example: Nc2Moml /home/celaine/trash/todayoutput .ncxml \'$CLASSPATH\' /home/celaine/trash/todayoutput tos/lib/Counters/Counter.nc
    
    Expects an xml dump of:
      interfaces(file(filename.nc))
*/
public class Nc2Moml {
    public static void generateComponent(
            String sourcePath, String componentName, String outputFile)
            throws Exception {
        Element root = new Element("class");
        // Set the name of this class.
        root.setAttribute("name", componentName);

        Element source = new Element("property");
        source.setAttribute("name", "source");
        source.setAttribute("value", sourcePath);
        root.addContent(source);

        root.setAttribute("extends", "ptolemy.domains.ptinyos.lib.NCComponent");
        
        DocType plot = new DocType("plot",
                "-//UC Berkeley//DTD MoML 1//EN",
                "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd");
        Document doc = new Document(root, plot);
        
        
        // FIXME comment

        ListIterator interfaces = Xnesc.interfaceList.listIterator();

        while (interfaces.hasNext()) {
            Xinterface intf = (Xinterface)interfaces.next();

            // Add the info for this interface
            Element port = new Element("port");
            port.setAttribute("name", intf.name);
            port.setAttribute("class", "ptolemy.actor.IOPort");
            
            Element portType = new Element("property");
            String portTypeValue = (intf.provided) ? "input" : "output";
            portType.setAttribute("name", portTypeValue);
            port.addContent(portType);
            
            if (intf.parameters != null) {
                Element multiport = new Element("property");
                multiport.setAttribute("name", "multiport");
                port.addContent(multiport);
            }
            
            Element showName = new Element("property");
            showName.setAttribute("name", "_showName");
            showName.setAttribute("class", "ptolemy.kernel.util.SingletonAttribute");
            port.addContent(showName);

            root.addContent(port);
        }


        // FIXME comment
        // serialize it into a file
        try {
            FileOutputStream out = null;
            if (outputFile != null) {
                out = new FileOutputStream(outputFile);
            }
            XMLOutputter serializer = new XMLOutputter(Format.getPrettyFormat());
            
            Format format = serializer.getFormat();
            format.setOmitEncoding(true);
            format.setLineSeparator("\n");
            
            serializer.setFormat(format);

            if (out != null) {
                serializer.output(doc, out);
                out.flush();
                out.close();
            } else {
                serializer.output(doc, System.out);
            }
        }
        catch (IOException e) {
            System.err.println(e);
        }
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 3) {
            System.err.println("Usage: java Nc2Moml <xml input prefix> <xml input suffix> <nc sub prefix> <moml output prefix> [short path to .nc files]");
            return;
        }

        int index = 0;
        String inputPrefix = args[index++].trim();
        String inputSuffix = args[index++].trim();
        String subPrefix = args[index++].trim();
        String outputPrefix = args[index++].trim();

        for (int i = index; i < args.length; i++) {
            String xmlSuffix = args[i].replaceFirst("\\.nc$", inputSuffix);
            String xmlInputFile = inputPrefix + _FILESEPARATOR + xmlSuffix;

            String pathToNCFile = subPrefix + _FILESEPARATOR + args[i];

            String[] subdirs = args[i].split(_FILESEPARATOR);
            String componentName = subdirs[subdirs.length - 1];
            componentName = componentName.replaceFirst("\\.nc$", "");
            
            String momlSuffix = args[i].replaceFirst("\\.nc$", "\\.moml");
            String momlOutputFile = outputPrefix + _FILESEPARATOR + momlSuffix;
            
            try {
                if (new NDReader().parse(xmlInputFile)) {
                    System.out.println("parse ok: " + xmlInputFile);
                } else {
                    System.out.println("parse exceptions occurred: " + xmlInputFile);
                }

                try {
                    generateComponent(pathToNCFile, componentName, momlOutputFile);
                } catch (Exception e) {
                    System.err.println("Errors while generating " + momlOutputFile
                            + "because of exception: " + e);
                }
            } catch (SAXException e) {
                System.err.println("No xml reader found for" + xmlInputFile);
            } catch (FileNotFoundException e) {
                System.err.println("Could not find file " + xmlInputFile);
            } catch (Exception e) {
                System.err.println("Did not complete nc2moml for file: "
                        + xmlInputFile);
                System.err.println("because of exception: " + e);
            }
        }
    }

    private static String _FILESEPARATOR = "/";
}
