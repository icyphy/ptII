// JavaScript interface to PlotApplet
//
// @Author: Christopher Hylands
// @Version: $Id$
//
// @Copyright (c) 1997-1999 The Regents of the University of California.
// All rights reserved.
//
// Permission is hereby granted, without written agreement and without
// license or royalty fees, to use, copy, modify, and distribute this
// software and its documentation for any purpose, provided that the
// above copyright notice and the following two paragraphs appear in all
// copies of this software.
//
// IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
// FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
// ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
// THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
// SUCH DAMAGE.
//
// THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
// INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
// MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
// PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
// CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
// ENHANCEMENTS, OR MODIFICATIONS.
//
//                                                PT_COPYRIGHT_VERSION_2
//                                                COPYRIGHTENDKEY


//////////////////////////////////////////////////////////////////////////
// ptplot
// Generate html with parameters from theform.
//
function ptplot(theform) {
    var width = theform.width.value;
    var height = theform.height.value;
    var background = theform.background.value;
    var foreground = theform.foreground.value;
    var urlbase = theform.urlbase.value;
    fileObj = theform.fileSelection;
    var files = fileObj.options[fileObj.selectedIndex].text;
    var displayargs = theform.displayargs.checked;
    var otherargs = "";

    otherargs += ( theform.bar.checked ) ? "-bar ": "";
    otherargs += ( theform.binary.checked ) ? "-binary ": "";
    otherargs += ( theform.nolines.checked ) ? "-nl ": "";
    otherargs += ( theform.smallpoints.checked ) ? "-p ": "";
    otherargs += ( theform.largepoints.checked ) ? "-P ": "";
    otherargs += ( theform.reversevideo.checked ) ? "-rv ": "";
    otherargs += ( theform.ticks.checked ) ? "-tk ": "";
    otherargs += ( theform.title.value != "" ) ?
	"-t '" + theform.title.value + "' " : "";
    otherargs += "-tf " +
        theform.titleFontFamily.options[
                theform.titleFontFamily.selectedIndex].text +
        "-" +
        theform.titleFontStyle.options[
                theform.titleFontStyle.selectedIndex].text +
        "-" +
        theform.titleFontSize.options[
                theform.titleFontSize.selectedIndex].text +
        " ";

    otherargs += "-lf " +
        theform.labelFontFamily.options[
                theform.labelFontFamily.selectedIndex].text +
        "-" +
        theform.labelFontStyle.options[
                theform.labelFontStyle.selectedIndex].text +
        "-" +
        theform.labelFontSize.options[
                theform.labelFontSize.selectedIndex].text +
        " ";

    otherargs += ( theform.xaxis.value != "" ) ?
	"-x '" + theform.xaxis.value + "' " : "";
    otherargs += ( theform.yaxis.value != "" ) ?
	"-y '" + theform.yaxis.value + "' " : "";

    if (urlbase != "" && files != "(NONE)") {
        // Split filenames by spaces.  Note that this means we
        // cannot support filenames with spaces in the file selector
        filearray = files.split(" ");
        var tmpfiles = "";
        for (var i=0; i < filearray.length; i++) {
            tmpfiles+= urlbase + filearray[i] + " ";
        }
    }

    document.write("<html>");
    document.write("<head>");
    document.write("<title>Plot</title>");
    document.write("<body bgcolor=\"#faf0e6\">");
    document.write("<H1>Plot</H1>");

    document.write("<!-- The HTML below is for the Java Plugin.  \n");
    document.write("	The first section is for IE, \n");
    document.write("	the second is for Netscape Communicator\n");
    document.write(" -->\n");
    document.write("\n");
    document.write("<OBJECT classid=\"clsid:8AD9C840-044E-11D1-B3E9-00805F499D93\"\n");
    document.write("   name=\"JSPlot\"\n");
    document.write("   width=\"" + parseInt(width) + "\"\n");
    document.write("   height=\"" + parseInt(height) + "\"\n");
    document.write("   codebase=\"http://java.sun.com/products/plugin/1.2/jinstall-12-win32.cab#Version=1,2,0,0\">\n");
    document.write("<PARAM NAME=\"code\"       VALUE=\"ptolemy.plot.compat.PxgraphApplet.class\">\n");
    document.write("<PARAM NAME=\"codebase\"   VALUE=\"../../..\">\n");
    document.write("<PARAM NAME=\"archive\"    VALUE=\"ptolemy/plot/pxgraphapplet.jar\">\n");
    document.write("<PARAM NAME=\"type\"       VALUE=\"application/x-java-applet;version=1.2\">\n");
    if ( background != "" )
        document.write("<param name=\"background\" value=\"" +
                background +"\">\n");
    if ( foreground.value != "" )
        document.write("<param name=\"foreground\" value=\"" +
                foreground +"\">\n");
    document.write("<PARAM NAME=\"background\" VALUE=\"#faf0e6\">\n");
    document.write("<PARAM NAME=\"pxgraphargs\"    VALUE=\"" +
	otherargs + " " + tmpfiles +  "\">\n");
    document.write("<COMMENT>\n");
    document.write("<EMBED type=\"application/x-java-applet;version=1.2\"\n");
    document.write("   name=\"JSPlot\"\n");
    document.write("   width=\"" + parseInt(width) + "\"\n");
    document.write("   height=\"" + parseInt(height) + "\"\n");
    if ( background != "" )
        document.write("    background=\"" + background +"\"\n");
    if ( foreground.value != "" )
        document.write("    foreground=\"" + foreground +"\"\n");
    document.write("   code=\"ptolemy.plot.compat.PxgraphApplet.class\"\n");
    document.write("   codebase=\"../../..\"\n");
    document.write("   archive=\"ptolemy/plot/pxgraphapplet.jar\"\n");
    document.write("   pxgraphargs=\"" +
	otherargs + " " + tmpfiles +  "\"\n");
    document.write("   pluginspage=\"http://java.sun.com/products/plugin/1.2/plugin-install.html\">\n");
    document.write("</COMMENT>\n");
    document.write("<NOEMBED>\n");
    document.write("No JDK 1.2 support for applet!\n");
    document.write("</NOEMBED>\n");
    document.write("</EMBED>\n");
    document.write("</OBJECT>\n");



    document.write("<form name=\"aboutplot\">")
        document.write("<input type=\"button\" Value=\"About\"");
    document.write("onClick=\"alert(document.JSPlot.getAppletInfo())\">");
    document.write("</form>");
    if ( displayargs ) {
	document.write("<table border> <tr><th>Variable<th>Value");
	document.write("<tr><td>width<td>" + width);
	document.write("<tr><td>height<td>" + height);
	document.write("<tr><td>background<td>" + background);
	document.write("<tr><td>foreground<td>" + foreground);
	document.write("</table>");
    }
    document.write("</body>");
    document.write("</html>");
}
