/* A class that keeps track of compiler options.

 Copyright (c) 2003 The University of Maryland.
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

@ProposedRating Red (ankush@glue.umd.edu)
@AcceptedRating Red (ssb@eng.umd.edu)
*/

package ptolemy.copernicus.c;

import java.util.Hashtable;

//////////////////////////////////////////////////////////////////////////
//// Options

/**
A class that keeps track of compiler options. All options are stored as
key-value string pairs. Possible options are:

<p>
<b> verbose </b><br>
<i>true/false</i> Turns verbose mode on or off.

<p>
<b> compileMode </b> <br>
<i>singleClass</i> compiles only the given class, <br>
<i>full</i> generates all required files.

<p>
<b> pruneLevel </b> <br>
<i> 0 </i> no code pruning done. <br>
<i> 1 </i> Code Pruning done by CallGraphPruner.

<p>
<b> vta </b> <br>
<i> true/false </i> Whether or not to perform Variable Type Analysis.

<p>
<b> lib </b> <br>
stores the path to the directory where library of generated files is
stored.
</DL>

<p>
<b> gcDir </b> <br>
stores the path to the directory containing the garbage colloector. Not
using this option turns the collector off.

<p>
<b> target </b> <br>
<i> C6000 </i> The TMS320C6xxx series of processors. <br>
The target platform. A blank refers to a generic unix-like system. This
includes Cygwin installations on windows.

<p>
<b> runtimeDir </b> <br>
The path to the runtime directory.

@author Ankush Varma
@version $Id$
@since Ptolemy II 2.0
*/
public class Options {

    /** Default constructor. Initializes default key-value pairs.
     */
    public Options() {
        _optionTable.put("verbose", "false");
        _optionTable.put("compileMode", "full");
        _optionTable.put("pruneLevel", "1");
        _optionTable.put("lib", "j2c_lib");
        _optionTable.put("gcDir", "");
        _optionTable.put("target", "");
        _optionTable.put("runtimeDir", "../runtime");
        _optionTable.put("vta", "true");
    }


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get the value corresponding to a key from the options table.

        @param key The name of the option to be looked up.
        @return The value corresponding to this option.
    */
    public String get(String key) {
        return (String)_optionTable.get(key);
    }

    /** Get a boolean value corresponding to a given key. This returns true
     * or false depending on whether the value is the string "true" or
     * "false".
     *
     * @param key The name of the option to be looked up.
     * @return The boolean corresponding to the value of this key.
     * @exception RuntimeException If a lookup of a value that is not
     * "true" or "false" occurs.
     */
    public boolean getBoolean(String key) {
        String value = get(key);
        if (value.compareTo("true") == 0) {
            return true;
        }
        else if (value.compareTo("false") == 0) {
            return false;
        }
        else {
            throw new RuntimeException(
                    "Stored value cannot be converted to boolean.");
        }
    }

    /** Get the integer value corresponding to a given key.
     * @param key The name of the option to be looked up.
     * @return The integer value corresponding to this key.
     */
    public int getInt(String key) {
        return Integer.valueOf(get(key)).intValue();
    }

    /** Checks if a given string is a valid Option.
     * @param string The string to be checked.
     * @return True if it is a valid Option flag.
     */
     public static boolean isValidOption(String string) {
         if (string.equals("lib")
               || string.equals("verbose")
               || string.equals("compileMode")
               || string.equals("pruneLevel")
               || string.equals("gcDir")
               || string.equals("target")
               || string.equals("runtimeDir")
               || string.equals("vta")) {
             return true;
         }
         else {
             return false;
         }
     }

     /** Checks if a given string is a valid option flag. A valid option
      * flag is a "-" followed by a valid Option String.
      * @param string The string to be checked.
      * @return True if it is a valid option flag.
      */
    public static boolean isValidFlag(String string) {
        if (string.startsWith("-")
                && isValidOption(string.substring(1))) {
            return true;
        }
        else {
            return false;
        }
    }


    /** Put a key-value pair into the options table.
        @param key The name of the option to be set.
        @param value The value assigned to this option.
    */
    public void put(String key, String value) {
        _optionTable.put(key, value);
    }

    /** Convert the option table to a String.
     *  @return A string showing the used options.
     */
    public String toString() {
        return _optionTable.toString();
    }

    /** Return a static version of this class.
     *  @return A static Options object.
     */
    public static Options v() {
        return _v;
    }



    ///////////////////////////////////////////////////////////////////
    ////                         public fields                     ////


    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////


    ///////////////////////////////////////////////////////////////////
    ////                       protected fields                    ////


    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////


    ///////////////////////////////////////////////////////////////////
    ////                         private fields                    ////
    /** The table of the values assigned to each key.
     */
    private Hashtable _optionTable = new Hashtable();

    /** Provides a static version of this class.
     */
    private static Options _v = new Options();

}
