/*
 *      %Z%%M% %I% %E% %U%
 *
 * Copyright (c) 1996-2000 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Sun grants you ("Licensee") a non-exclusive, royalty free, license to use,
 * modify and redistribute this software in source and binary code form,
 * provided that i) this copyright notice and license appear on all copies of
 * the software; and ii) Licensee does not utilize the software in a manner
 * which is disparaging to Sun.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY
 * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING
 * OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS
 * LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,
 * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF
 * OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 *
 * This software is not designed or intended for use in on-line control of
 * aircraft, air traffic, aircraft navigation or aircraft communications; or in
 * the design, construction, operation or maintenance of any nuclear
 * facility. Licensee represents and warrants that it will not use or
 * redistribute the Software for such purposes.
 */

package vendors.vr;


import java.lang.*;
import java.io.*;
import java.util.*;
import java.net.*;

public class Context {
    Vector attrs = new Vector();
    Hashtable lookup = new Hashtable();
    String ident = "Java3D Volume Rendering Demo Attrs";
    URL codebase;

    public Context(URL initCodebase) {
	codebase = initCodebase;
    System.out.println("Read in files");
    }

    public void addAttr(Attr newAttr) {
	attrs.add(newAttr);
	lookup.put(newAttr.getName(), newAttr);
    }
    void reset() {
	for (Enumeration e = attrs.elements(); e.hasMoreElements(); ) {
	    Attr attr = (Attr)e.nextElement();
	    attr.reset();
	}
    }
    void save(String filename, String description) {
	try {
	    PrintWriter out = new PrintWriter(new FileWriter(filename));
	    out.println(ident);
	    out.println("Description: " + description);
	    for (Enumeration e = attrs.elements(); e.hasMoreElements(); ) {
		Attr attr = (Attr)e.nextElement();
		out.println(attr);
	    }
	    out.close();
	} catch (Exception e) {
	    System.out.println("Exception " + e + " writing attrs");
	}
    }
    String restore(String filename) {
	String description = null;
	reset();
	try {
	    URL vrsdat = null;
	    try {
		vrsdat = new URL( codebase + filename );
	    } catch (MalformedURLException e) {
		    System.out.println("VolFile: " + e.getMessage());
	    }
	    LineNumberReader in = new LineNumberReader(
				    new InputStreamReader(vrsdat.openStream()));
	    String line;
	    line = in.readLine();
	    if (!line.equals(ident)) {
		System.err.println("File " + filename +
			" doesn't have a valid header: \"" + line + "\"");
	    }
	    line = in.readLine();
	    int space = line.indexOf(' ');
	    description = line.substring(space+1);
	    if (description.equals("null")) {
		description = null;
	    }
	    while ((line = in.readLine()) != null) {
		space = line.indexOf(' ');
		String name = line.substring(0, space);
		String value = line.substring(space+1);
		//System.out.println("setting attr " + name + " to " + value);
		Attr attr = getAttr(name);
		if (attr != null) {
		    attr.set(value);
		    attr.updateComponent();
		} else {
		    System.err.println("restore: attr " + name + " not found");
		}
	    }
	} catch (Exception e) {
	    System.out.println("Exception " + e + " reading attrs");
	}
	return description;
    }

    Attr getAttr(String label) {
	String name = Attr.toName(label);
	Attr retval  = (Attr)lookup.get(name);

    //System.out.println(lookup);
    //System.out.println("in getAttr");

    if (retval == null) {
	    System.err.println("Attr not found: " + name);
	    // TODO: dump the names in the table
	    // TODO: throw a runtime error instead of NPE
	    throw new NullPointerException();
	}
	return retval;
    }

    URL getCodeBase() {
	return codebase;
    }


}
