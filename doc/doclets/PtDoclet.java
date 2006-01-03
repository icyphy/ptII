package doc.doclets;

import com.sun.javadoc.*;

/** Generate PtDoc output
 */
public class PtDoclet {
    public static boolean start(RootDoc root) {
        StringBuffer results = new StringBuffer(_header);
        ClassDoc[] classes = root.classes();
        for (int i = 0; i < classes.length; i++) {
            String className = classes[i].toString();
            String shortClassName = null;
            if (className.lastIndexOf(".") == -1) {
                shortClassName = className;
            } else {
                shortClassName = 
                    className.substring(className.lastIndexOf(".") + 1);
            }
            results.append("<doc name=\"" + shortClassName + " class=\""
                    + className + "\"\n"
                    + "<description>\n"
                    // FIXME: need to escape commentText
                    + classes[i].commentText() + "\n"
                    + "</description>\n");

            // Handle other class tags
            String [] classTags = {"author", "version", "since", "Pt.ProposedRating", "Pt.AcceptedRating"};
            for (int j = 0; j< classTags.length; j++) {
                results.append("<" + classTags[j] + ">" 
                        + classes[i].tags(classTags[j]) + ">\n");
            }
        }
        // FIXME: print to a file, use the -d option for directory
        System.out.println(results.toString());
        return true;
    }

    /** Header string for XML PtDoc output. */
    private static String _header = "<?xml version=\"1.0\" standalone=\"yes\"?>\n<!DOCTYPE doc PUBLIC \"-//UC Berkeley//DTD DocML 1//EN\"\n\"http://ptolemy.eecs.berkeley.edu/xml/dtd/DocML_1.dtd\">\n";

}
