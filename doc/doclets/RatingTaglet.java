/* Utilities used to manipulate classes

 Copyright (c) 2003-2014 The Regents of the University of California.
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

import java.lang.reflect.Constructor;
import java.util.Map;

import com.sun.javadoc.Tag;
import com.sun.tools.doclets.Taglet;

//////////////////////////////////////////////////////////////////////////
//// RatingTaglet

/**
 A taglet that deals with Ptolemy code rating tags.

 This class was based on Sun's example taglets.

 @author Steve Neuendorffer, Contributor: Christopher Brooks
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Red (neuendor)
 @Pt.AcceptedRating Red (neuendor)
 */
public class RatingTaglet implements Taglet {
    /** Create a new RatingTaglet that deals with tags of the given name that
     *  has the given tagName as a string.
     *  @param name The name of the taglet.
     *  @param tagName The tagName.
     */
    public RatingTaglet(String name, String tagName) {
        _name = name;
        _tagName = tagName;
    }

    /**
     * Return the name of this custom tag.
     */
    @Override
    public String getName() {
        return _name;
    }

    /**
     * Return true if the tag can annotate a field.
     * @return false.
     */
    @Override
    public boolean inField() {
        return false;
    }

    /**
     * Return true if the tag can annotate a constructor.
     * @return false.
     */
    @Override
    public boolean inConstructor() {
        return false;
    }

    /**
     * Return true if the tag can annotate a method.
     * @return false.
     */
    @Override
    public boolean inMethod() {
        return false;
    }

    /**
     * Return true if the tag can annotate a class.
     * @return true.
     */
    @Override
    public boolean inOverview() {
        return true;
    }

    /**
     * Return true if the tag can annotate a package.
     * @return true.
     */
    @Override
    public boolean inPackage() {
        return true;
    }

    /**
     * Return true if the tag can annotate a class or interface.
     * @return true.
     */
    @Override
    public boolean inType() {
        return true;
    }

    /**
     * Return true if the tag is an inline tag.
     * @return false.
     */
    @Override
    public boolean isInlineTag() {
        return false;
    }

    /**
     * Register this Taglet.
     * @param tagletMap  the map to register this tag to.
     */
    public static void register(Map tagletMap) {
        try {
            _register(tagletMap, new RatingTaglet("Pt.AcceptedRating",
                    "Accepted Rating"));
            _register(tagletMap, new RatingTaglet("Pt.ProposedRating",
                    "Proposed Rating"));
        } catch (Throwable throwable) {
            // Print the stack trace so the user has a clue.
            throwable.printStackTrace();
            throw new RuntimeException(throwable);
        }
    }

    /**
     * Given the <code>Tag</code> representation of this custom
     * tag, return its string representation.
     * @param tag the <code>Tag</code> representation of this custom tag.
     * @return The string representation.
     */
    @Override
    public String toString(Tag tag) {
        String color = tag.text();

        // Assume the first thing on the line is the color.
        int spaceIndex = color.indexOf(' ');

        if (spaceIndex != -1) {
            // Deal with the fact that somebody might just have put a
            // color.
            color = color.substring(0, spaceIndex);
        }

        return "<DT><B>" + _tagName + ":</B><DD>"
                + "<table cellpadding=2 cellspacing=0><tr><td bgcolor=\""
                + color.toLowerCase() + "\">" + tag.text()
                + "</td></tr></table></DD>\n";
    }

    /**
     * Given an array of <code>Tag</code>s representing this custom
     * tag, return its string representation.
     * @param tags  the array of <code>Tag</code>s representing of this custom tag.
     * @return The string representation.
     */
    @Override
    public String toString(Tag[] tags) {
        if (tags.length == 0) {
            return null;
        }

        String color = tags[0].text();

        // Assume the first thing on the line is the color.
        int spaceIndex = color.indexOf(' ');

        if (spaceIndex != -1) {
            // Deal with the fact that somebody might just have put a
            // color.
            color = color.substring(0, spaceIndex);
        }

        StringBuffer result = new StringBuffer("\n<DT><B>" + _tagName
                + ":</B><DD>");
        result.append("<table cellpadding=2 cellspacing=0><tr><td bgcolor=\""
                + color.toLowerCase() + "\">");

        for (int i = 0; i < tags.length; i++) {
            if (i > 0) {
                result.append(", ");
            }

            result.append(tags[i].text());
        }

        return result.toString() + "</td></tr></table></DD>\n";
    }

    // Remove any previous entry in the given map with the given name
    // and add an entry in the given map with the given name as the key
    // and the given taglet as the value.
    private static void _register(Map tagletMap, Taglet taglet) {
        final String tagName = taglet.getName();

        if (tagletMap.containsKey(tagName)) {
            tagletMap.remove(tagName);
        }

        String javaSpecificationVersion = System
                .getProperty("java.specification.version");

        if (javaSpecificationVersion != null
                && javaSpecificationVersion.equals("1.4")) {
            tagletMap.put(taglet.getName(), taglet);
        } else {
            String legacyTagletClassName = "com.sun.tools.doclets.internal.toolkit.taglets.LegacyTaglet";

            try {
                // Use reflection so that this code will compile under jdk1.4.
                Class legacyTagletClass = Class.forName(legacyTagletClassName);
                Constructor legacyTagletConstructor = legacyTagletClass
                        .getConstructor(new Class[] { Taglet.class });
                Object legacyTagletObject = legacyTagletConstructor
                        .newInstance(new Object[] { taglet });
                tagletMap.put(tagName, legacyTagletObject);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                throw new RuntimeException("Problem with the '"
                        + legacyTagletClassName + "' class: ", throwable);
            }
        }
    }

    // The name of this taglet.
    private String _name;

    // The tag used for this taglet.
    private String _tagName;
}
