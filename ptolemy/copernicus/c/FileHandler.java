/*
A class that takes care of common File I/O functions.

Copyright (c) 2001-2002 The University of Maryland.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the above
copyright notice and the following two paragraphs appear in all copies
of this software.

IN NO EVENT SHALL THE UNIVERSITY OF MARYLAND BE LIABLE TO ANY PARTY
FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
THE UNIVERSITY OF MARYLAND HAS BEEN ADVISED OF THE POSSIBILITY OF
SUCH DAMAGE.

THE UNIVERSITY OF MARYLAND SPECIFICALLY DISCLAIMS ANY WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
MARYLAND HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
ENHANCEMENTS, OR MODIFICATIONS.


@ProposedRating Red (ankush@eng.umd.edu)
@AcceptedRating Red (ankush@eng.umd.edu)
*/

package ptolemy.copernicus.c;

import java.io.PrintWriter;
import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;

/* A class that takes care of common File I/O functions

   @author Ankush Varma
   @version $Id$
*/

public class FileHandler
{
    public static boolean exists(String fileName)
    {
        File f = new File(fileName);
        return(f.exists());
    }

    public static void write(String fileName, String code)
    {
        try
        {
            PrintWriter out = new PrintWriter(new FileOutputStream(fileName));
            out.println(code.toString());
            out.close();
        }
        catch(IOException e)
        {
            System.err.println("ERROR: Could not create file: "+fileName);
        }
    }

}


