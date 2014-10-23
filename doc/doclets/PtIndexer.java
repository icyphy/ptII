/* Create a searchable index of actor documentation.

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

package doc.doclets;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Create a searchable index.
 *
 *  <p>This class has a method {@link #append(String, String)} takes a
 *  key and value and splits the value into alpha-numeric words that
 *  are then saved in a data structure for later searching.  Common
 *  words are filtered out.</p>
 *
 *  <p>The {@link #write(String)} method saves the data structure to
 *  disk via serialization.</p>
 *
 *  <p>The {@link #read(String)} method reads the data structure.</p>
 *
 *  <p>The {@link #search(String)} returns a list of values that match a key</p>
 *
 *  @author Christopher Brooks, Contributor: Edward A. Lee
 *  @version $Id$
 *  @since Ptolemy II 10.0
 */
public class PtIndexer {
    /** Append all the words to the dictionary.
     *  <p>The value of the words parameter is split into words
     *  and a map entry is created where the word is the key
     *  and the location is an element in a List.
     *  @param location The dot separated classname where the
     *  value of the words parameter is found.
     *  @param words The words found in the location.
     *  @exception IOException If thrown by the tokenizer.
     */
    public void append(String location, String words) throws IOException {
        StreamTokenizer tokenizer = new StreamTokenizer(new StringReader(words));

        tokenizer.lowerCaseMode(true);
        while (tokenizer.nextToken() != StreamTokenizer.TT_EOF) {
            if (tokenizer.ttype == StreamTokenizer.TT_WORD) {
                String word = tokenizer.sval;
                if (!_common.contains(word)) {
                    // The dictionary consists of words as keys
                    // and locations as values.
                    // FIXME: would could keep a count of the
                    // number of words that show up.
                    Set<String> locations = _dictionary.get(word);
                    if (locations != null) {
                        // Add the location to the Set of locations
                        // for this word.
                        locations.add(location);
                    } else {
                        // Add the word to the dictionary and add
                        // a Set with an element that contains
                        // the location.
                        locations = new HashSet<String>();
                        locations.add(location);
                        _dictionary.put(word, locations);
                    }
                }
            }
        }
    }

    /** A test driver for the indexer.
     *  <p>Usage:</p>
     *  <pre>
     *  java -classpath $PTII doc.doclets.PtIndexer [-c] [target]
     *  </pre>
     *
     *  <p>If called with no arguments, create the dictionary
     *  PtIndexer.ser The standard input is read and assumed to be
     *  file names.  Each file is read and the dictionary is updated
     *  with words and locations.  The location is determined by
     *  substituting "." for "/" in the file name.  </p>
     *
     *  <p>Typically, the PtDoclet creates the dictionary, but this driver
     *  can be used for testing. To create the .xml file to be read:</p>
     *  <pre>
     *  cd $PTII/doc; make docs
     *  </pre>
     *
     *  <p>To run the tool on the .xml files and create the dictionary again:</p>
     *  <pre>
     *  cd codeDoc
     *  find . -name "*.xml" | java -classpath $PTII doc.doclets.PtIndexer
     *  </pre>
     *
     *  <p>If called with <code>-c</code> then use compression.</p>
     *
     *  <p> If called with one argument that is not <code>-c</code>,
     *  then the argument is assumed to be a target and the collection
     *  of places where target is found is returned.</p>
     *
     *  @param args The arguments, if any
     *  @exception IOException If thrown while reading the input files or dictionary.
     *  @exception ClassNotFoundException If the file does not contain the
     *  dictionary class.
     */
    public static void main(String args[]) throws IOException,
    ClassNotFoundException {
        String usage = "Usage: java -classpath $PTII doc.doclets.PtIndexer [target]";
        String dictionaryFile = "PtIndexer.ser";

        if (args.length > 2) {
            System.err.println(usage);
            return;
        }
        String target = null;
        if (args.length >= 1) {
            if (args[0].equals("-c")) {
                _useCompression = true;
                if (args.length == 2) {
                    target = args[1];
                }
            } else {
                target = args[0];
            }
        }
        if (_useCompression) {
            dictionaryFile = "PtIndexerCompressed.ser";
        }
        PtIndexer ptIndexer = new PtIndexer();
        if (target == null) {
            // Read file names from stdin and created PtIndexer.ser
            BufferedReader stdin = null;
            BufferedReader fileInput = null;
            int numberOfFiles = 0;
            int numberOfLines = 0;
            try {
                stdin = new BufferedReader(new InputStreamReader(System.in));
                String fileName = null;
                String line = null;
                while ((fileName = stdin.readLine()) != null) {
                    numberOfFiles++;
                    try {
                        fileInput = new BufferedReader(new InputStreamReader(
                                new FileInputStream(fileName)));
                        // Remove .xml
                        fileName = fileName.substring(0,
                                fileName.lastIndexOf("."));
                        // Replace "./" with "" and then "/" with "."
                        String className = fileName.replace("./", "").replace(
                                "/", ".");
                        while ((line = fileInput.readLine()) != null) {
                            numberOfLines++;
                            ptIndexer.append(className, line);
                        }
                    } finally {
                        if (fileInput != null) {
                            fileInput.close();
                        }
                    }
                }
                System.out.println("Read " + numberOfFiles + " files, "
                        + numberOfLines + " lines.");
            } finally {
                try {
                    if (stdin != null) {
                        stdin.close();
                    }
                } finally {
                    if (fileInput != null) {
                        fileInput.close();
                    }
                }
            }
            System.out.println(ptIndexer.statistics());
            ptIndexer.write(dictionaryFile);
        } else {
            ptIndexer.read(dictionaryFile);
            // Look up the argument.
            System.out.println(ptIndexer.search(target));
        }
    }

    /** Read the dictionary to a file or URL.
     *  @param fileName the file or URL of the dictionary
     *  @exception IOException If the dictionary cannot be written.
     *  @exception ClassNotFoundException If the file does not contain the
     *  dictionary class.
     */
    public void read(String fileName) throws IOException,
    ClassNotFoundException {
        FileInputStream fileInputStream = null;
        ObjectInputStream objectInputStream = null;
        try {
            fileInputStream = new FileInputStream(fileName);
            objectInputStream = new ObjectInputStream(fileInputStream);
            if (_useCompression) {
                _compressedDictionary = (Map<String, BigInteger>) objectInputStream
                        .readObject();
                _compressedDefinitions = (String[]) objectInputStream
                        .readObject();
                System.out.println("_compressedDictionary size: "
                        + _compressedDictionary.size());
                System.out.println("_compressedDefinitions size: "
                        + _compressedDefinitions.length);
            } else {
                _dictionary = (Map<String, Set<String>>) objectInputStream
                        .readObject();
            }
        } finally {
            try {
                if (objectInputStream != null) {
                    objectInputStream.close();
                }
            } finally {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
            }
        }
    }

    /** Search the dictionary for matches.
     *
     *  <p> The target is converted to lower case and then the
     *  dictionary is searched.</p>
     *
     *  @param target The string to match.
     *  @return a collection of matches or null if there are no matches
     */
    public Collection<String> search(String target) {
        // We return a Collection because eventually we should return
        // a list ordered by the number of occurances.
        // FIXME: should we copy this so it can be modified?

        // The StreamTokenizer converts to lowercase so the
        // dictionary is lower case.
        if (_useCompression) {
            BigInteger indices = _compressedDictionary
                    .get(target.toLowerCase());
            if (indices == null) {
                return null;
            }
            int lastBit = indices.bitLength();
            System.out.println(indices + " lastBit: " + lastBit + " size: "
                    + _compressedDefinitions.length);
            Set results = new HashSet();
            for (int i = 0; i <= lastBit; i++) {
                if (indices.testBit(i)) {
                    results.add(_compressedDefinitions[i]);
                }
            }
            return results;
        } else {
            return _dictionary.get(target.toLowerCase());
        }
    }

    /** Return statistics about the dictionary.
     *  @return Statistics about the dictionary
     */
    public String statistics() {
        // This is primarily useful for debugging.
        int dictionarySize = 0;
        int maximumDefinitions = -1;
        String maximumKey = "";
        int definitionsSum = 0;
        for (Map.Entry<String, Set<String>> entry : _dictionary.entrySet()) {
            dictionarySize++;
            Set<String> definitions = entry.getValue();
            int size = definitions.size();
            definitionsSum += size;
            if (size > maximumDefinitions) {
                maximumDefinitions = size;
                maximumKey = entry.getKey();
            }
        }

        return "Number of keys: " + dictionarySize
                + "\nKey with the most definitions: \"" + maximumKey
                + "\" with " + maximumDefinitions + " definitions."
                + "\nAverage number of definitions: " + definitionsSum
                / dictionarySize;
    }

    /** Write the dictionary to a file or URL.
     *  @param fileName the file or URL of the dictionary
     *  @exception IOException If the dictionary cannot be written.
     */
    public void write(String fileName) throws IOException {
        if (_useCompression) {
            _compress();
        }
        FileOutputStream fileOutputStream = null;
        ObjectOutputStream objectOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(fileName);
            objectOutputStream = new ObjectOutputStream(fileOutputStream);

            if (_useCompression) {
                objectOutputStream.writeObject(_compressedDictionary);
                objectOutputStream.writeObject(_compressedDefinitions);
                //             System.out.println("_dictionary.get(\"ramp\"): " + _dictionary.get("ramp"));
                //             System.out.println("_compressedDictionary size: " + _compressedDictionary.size());
                //             System.out.println("_compressedDictionary.get(\"ramp\") " + _compressedDictionary.get("ramp"));
                //             System.out.println("_compressedDefinitions size: " + _compressedDefinitions.length);
                //             System.out.println("_compressedDefinitions[1]: " + _compressedDefinitions[1]);
            } else {
                objectOutputStream.writeObject(_dictionary);
            }

        } finally {
            try {
                if (objectOutputStream != null) {
                    objectOutputStream.close();
                }
            } finally {
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            }
        }
        System.out.println("PtIndexer wrote " + fileName);
    }

    /** Compress _dictionary into to objects, one where the Key is
     * a String that is the value to be searched for and the Value
     * is an Integer, where each 1 bit in the binary
     * representation of BigInteger is the index of an element in the
     * _compressedDefinitions table.  Thus, if the value is 5, the
     * binary representation is 1001, which means that the key
     * is found in the 3rd and 1st elements in _compressedDefinitions.
     * Note that 0th element of _compressedDefinitions is not
     * used, as there is no way to specify the 0th element in
     * the Integer.
     */
    private void _compress() {

        int dictionarySize = _dictionary.size();
        // We don't use the 0th element because we can't create an Integer
        // with a binary representation that will refer to it.
        _compressedDefinitions = new String[dictionarySize + 1];

        // A map from the definition name (typically the class name)
        // to the index in _compressedDefinitions
        Map<String, Integer> definitionsMap = new HashMap<String, Integer>(
                dictionarySize);

        // The dictionary consists of keys (the word to be searched
        // for) and values (a set of locations, typically class
        // names).  Iterate through the dictionary.  For each new
        // location, add it to the _compressedDefinitions table of
        // locations and note the index.  (We keep track of locations
        // with a map from location to index for ease of retrieval.)

        // We don't use the 0th element, so we start at 1.
        int keyCount = 1;
        String key = "";
        // The uncompressed locations.
        Set<String> definitions = null;
        BigInteger indices = null;
        for (Map.Entry<String, Set<String>> entry : _dictionary.entrySet()) {
            key = entry.getKey();
            definitions = entry.getValue();
            for (String definition : definitions) {
                // See if the definition has already been mapped.
                Integer definitionIndex = definitionsMap.get(definition);
                if (definitionIndex == null) {
                    // The definition (typically the class path)
                    // is not in the map from definition name to index
                    // into _compressedDefinitions, so add it now.
                    definitionsMap.put(definition, keyCount);
                    // Add the definition to the array of definitions:
                    _compressedDefinitions[keyCount] = definition;
                    definitionIndex = new Integer(keyCount++);
                }
                // The value of definitionsIndex is now the index of
                // the element in _compressedDefinitions.

                indices = _compressedDictionary.get(key);
                if (indices == null) {
                    // _compressedDictionary does not have an element for key, add one now.
                    // Since definitionsIndex is the first index, we just add it.
                    indices = new BigInteger("0");
                    indices = indices.setBit(definitionIndex.intValue());
                    _compressedDictionary.put(key, indices);
                } else {
                    // _compressedDictionary already has an element for key.
                    indices = indices.setBit(definitionIndex.intValue());
                    _compressedDictionary.put(key, indices);
                }
            }
        }
    }

    /** The dictionary, where the key is the word and the value is
     *  a Set of Strings where each element names a place where the word
     *  is used.  For searching actors, the key element is a dot-separated
     *  classname.
     */
    private Map<String, Set<String>> _dictionary = new HashMap<String, Set<String>>();

    /** A compressed dictionary, where the key is a String that is the target to
     *  be searched for and the value is an BigInteger, where each 1 bit in the
     *  BigInteger corresponds with an index of an element in the _compressedDefinitions table.
     */
    private Map<String, BigInteger> _compressedDictionary = new HashMap<String, BigInteger>();

    /** The compressed table of definitions, where each element is
     *  typically a dot-separated class name (the location) where a key
     *  was found.
     */
    private String[] _compressedDefinitions = new String[] {};

    /** True if compression is used.
     */
    private static boolean _useCompression = false;

    /** The set of common words that are not indexed.
     *  One way to update this is with:
     * find $PTII/doc/codeDoc -name "*.xml"  | xargs cat | tr -cs "[:alpha:]" "\n" | tr "[:upper:]" "[:lower:]" | sort | uniq -c | sort -nr | head -100 | awk '{printf("       \"%s\", // %d\n", $2, $1)}'
     */
    static Set<String> _common = new HashSet(Arrays.asList("the", // 20622
            "gt", // 12103
            "lt", // 11901
            "ptolemy", // 9308
            "a", // 8364
            "is", // 7371
            "pt", // 7309
            "of", // 6723
            "i", // 6130
            "doc", // 5953
            "dtd", // 5905
            "version", // 5823
            "to", // 5519
            "actor", // 4654
            "this", // 4633
            "name", // 4555
            "xml", // 4233
            "description", // 3981
            "berkeley", // 3966
            "docml", // 3934
            "since", // 3860
            "author", // 3775
            "proposedrating", // 3655
            "acceptedrating", // 3654
            "in", // 3415
            "and", // 3371
            "that", // 3308
            "class", // 3206
            "property", // 3190
            "port", // 2844
            "quot", // 2763
            "parameter", // 2737
            "an", // 2709
            "be", // 2680
            "cxh", // 2648
            "for", // 2525
            "p", // 2443
            "java", // 2250
            "red", // 2207
            "input", // 2191
            "if", // 2108
            "http", // 2096
            "output", // 2036
            "public", // 2013
            "edu", // 1998
            "eecs", // 1995
            "standalone", // 1973
            "yes", // 1969
            "en", // 1969
            "uc", // 1968
            "doctype", // 1968
            "ii", // 1967
            "value", // 1930
            "id", // 1909
            "z", // 1903
            "by", // 1783
            "data", // 1752
            "it", // 1722
            "lib", // 1716
            "are", // 1545
            "with", // 1521
            "will", // 1362
            "on", // 1338
            "then", // 1300
            "expr", // 1265
            "type", // 1176
            "or", // 1117
            "code", // 1117
            "default", // 1114
            "time", // 1091
            "which", // 981
            "as", // 978
            "can", // 973
            "domains", // 969
            "model", // 957
            "not", // 939
            "has", // 847
            "token", // 832
            "at", // 823
            "yellow", // 767
            "each", // 762
            "from", // 742
            "green", // 681
            "one", // 666
            "set", // 662
            "number", // 656
            "director", // 656
            "when", // 655
            "lee", // 636
            "its", // 628
            "edward", // 614
            "used", // 588
            "true", // 578
            "file", // 578
            "eal", // 575
            "typedioport", // 569
            "kernel", // 559
            "any", // 555
            "method", // 548
            "tokens", // 543
            "pt.proposedrating", "pt.acceptedrating", "a."));
}
