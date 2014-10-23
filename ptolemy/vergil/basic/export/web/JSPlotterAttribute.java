/* Attribute for generating the HTML file with JavaScript to plot simulation
   results. This base class cannot be instanced.

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

package ptolemy.vergil.basic.export.web;

import java.util.HashMap;

import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
////BasicJSPlotter
/**
 * Base class for attributes generating the HTML file with JavaScript to plot
 * simulation results for "Export to Web". This class provides parameters that
 * control how the figure should be plot. The generated HTML page consists of
 * three parts: the head, the body and the JavaScript in the head. Methods are
 * provided to modify the contents of these parts.
 *
 * @author Baobing (Brian) Wang
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public abstract class JSPlotterAttribute extends WebContent implements
WebExportable {

    /** Create an instance for each parameter.
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If the superclass throws it.
     *  @exception NameDuplicationException If the superclass throws it.
     */
    public JSPlotterAttribute(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _icon.setIconText("JS");
        displayText.setDisplayName("Graph title");
        displayText.setExpression("Ptolemy II Simulation Result");
        setExpression("Customize by clicking \"Configure\"");
        setVisibility(NOT_EDITABLE);

        width.setVisibility(NONE);
        height.setExpression("1");
        height.setVisibility(NONE);

        outputHTMLFile = new StringParameter(this, "outputHTMLFile");
        outputHTMLFile.setDisplayName("Output HTML file name");
        outputHTMLFile.setExpression("JSPlotter.html");

        linkTarget = new LinkTarget(this, "linkTarget");
        linkTarget.setDisplayName("The location to open the HTML page");
        linkTarget.setExpression("_blank");

        dataJSON = new StringParameter(this, "dataJSON");
        dataJSON.setDisplayName("Input data series in JSON format");

        eventsJSON = new StringParameter(this, "eventsJSON");
        eventsJSON.setDisplayName("Input event series in JSON format");

        saveDataToFile = new Parameter(this, "saveDataToFile");
        saveDataToFile.setDisplayName("Save data and event series to a "
                + "separate file");
        saveDataToFile.setExpression("false");
        saveDataToFile.setTypeEquals(BaseType.BOOLEAN);

        outputDataFile = new StringParameter(this, "outputDataFile");
        outputDataFile.setDisplayName("Output data file name");
        outputDataFile.setExpression("simulationResult.txt");

        graphWidth = new Parameter(this, "graphWidth");
        graphWidth.setTypeEquals(BaseType.INT);
        graphWidth.setDisplayName("Graph min width");
        graphWidth.setExpression("400");

        graphHeight = new Parameter(this, "graphHeight");
        graphHeight.setTypeEquals(BaseType.INT);
        graphHeight.setDisplayName("Graph min height");
        graphHeight.setExpression("500");

        autoResize = new Parameter(this, "autoResize");
        autoResize.setDisplayName("Auto-resize the graph");
        autoResize.setExpression("true");
        autoResize.setTypeEquals(BaseType.BOOLEAN);

        enableLegend = new Parameter(this, "enableLegend");
        enableLegend.setDisplayName("Show Legend");
        enableLegend.setExpression("true");
        enableLegend.setTypeEquals(BaseType.BOOLEAN);

        horizontalAlign = new StringParameter(this, "horizontalAlign");
        horizontalAlign.setDisplayName("Legend horizontal align");
        horizontalAlign.addChoice("center");
        horizontalAlign.addChoice("left");
        horizontalAlign.addChoice("right");
        horizontalAlign.setExpression("center");

        verticalAlign = new StringParameter(this, "verticalAlign");
        verticalAlign.setDisplayName("Legend vertical align");
        verticalAlign.addChoice("top");
        verticalAlign.addChoice("middle");
        verticalAlign.addChoice("bottom");
        verticalAlign.setExpression("bottom");

        dataConnectWidth = new Parameter(this, "dataConnectWidth");
        dataConnectWidth.setDisplayName("Data series connect line width");
        dataConnectWidth.setExpression("2");
        dataConnectWidth.setTypeEquals(BaseType.INT);

        enableDataMarker = new Parameter(this, "enableDataMarker");
        enableDataMarker.setDisplayName("Enable data series point marker");
        enableDataMarker.setExpression("false");
        enableDataMarker.setTypeEquals(BaseType.BOOLEAN);

        dataMarkerRadius = new Parameter(this, "dataMarkerRadius");
        dataMarkerRadius.setDisplayName("Data series marker radius");
        dataMarkerRadius.setExpression("3");
        dataMarkerRadius.setTypeEquals(BaseType.INT);

        eventsConnectWidth = new Parameter(this, "eventsConnectWidth");
        eventsConnectWidth.setDisplayName("Event series connect line width");
        eventsConnectWidth.setExpression("0");
        eventsConnectWidth.setTypeEquals(BaseType.INT);

        enableEventsMarker = new Parameter(this, "enableEventsMarker");
        enableEventsMarker.setDisplayName("Enable event series point marker");
        enableEventsMarker.setExpression("true");
        enableEventsMarker.setTypeEquals(BaseType.BOOLEAN);

        eventsMarkerRadius = new Parameter(this, "eventsMarkerRadius");
        eventsMarkerRadius.setDisplayName("Event series marker radius");
        eventsMarkerRadius.setExpression("3");
        eventsMarkerRadius.setTypeEquals(BaseType.INT);

        xAxisMode = new StringParameter(this, "xAxisMode");
        xAxisMode.setDisplayName("X axis type");
        xAxisMode.addChoice("linear");
        xAxisMode.addChoice("datetime");
        xAxisMode.setExpression("linear");

        drawVerticalGridLine = new Parameter(this, "drawVerticalGridLine");
        drawVerticalGridLine.setDisplayName("Draw vertical grid line");
        drawVerticalGridLine.setExpression("false");
        drawVerticalGridLine.setTypeEquals(BaseType.BOOLEAN);

        xAxisTitle = new StringParameter(this, "xAxisTitle");
        xAxisTitle.setDisplayName("X axis title");
        xAxisTitle.setExpression("X Axis");

        yAxisMode = new StringParameter(this, "yAxisMode");
        yAxisMode.setDisplayName("Y axis type");
        yAxisMode.addChoice("linear");
        yAxisMode.addChoice("logarithmic");
        yAxisMode.setExpression("linear");

        drawHorizontalGridLine = new Parameter(this, "drawHorizontalGridLine");
        drawHorizontalGridLine.setDisplayName("Draw horizontal grid line");
        drawHorizontalGridLine.setExpression("true");
        drawHorizontalGridLine.setTypeEquals(BaseType.BOOLEAN);

        yAxisTitle = new StringParameter(this, "yAxisTitle");
        yAxisTitle.setDisplayName("Y axis title");
        yAxisTitle.setExpression("Y Axis");

        customContent = new StringParameter(this, "customContent");
        customContent.setDisplayName("Custom content");
        customContent.setExpression("");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** Parameter specifying if the figure should be auto-resized based on the
     *  window size. This is a boolean that defaults to true.
     */
    public Parameter autoResize;

    /** Parameter specifying text to be inserted into dygraph constructor call.
     *  Workaround for specifying complicated options not covered by other
     *  parameters, for example, series-specific parameters.
     */
    public StringParameter customContent;

    /** Parameter specifying the width of the stroke connecting data points
     *  This is an int that defaults to 2.
     */
    public Parameter dataConnectWidth;

    /** Parameter giving the data traces in JSON format to be plot.
     *  This defaults to ""
     */
    public StringParameter dataJSON;

    /** Parameter specifying if the horizontal grid line should be drawn.
     *  This is a boolean that defaults to true.
     */
    public Parameter drawHorizontalGridLine;

    /** Parameter specifying the radius of markers for data points.
     *  This is an int that defaults to 3.
     */
    public Parameter dataMarkerRadius;

    /** Parameter specifying if the vertical grid line should be drawn.
     *  This is a boolean that defaults to false.
     */
    public Parameter drawVerticalGridLine;

    /** Parameter specifying if markers should be drawn for data points.
     *  This is a boolean that defaults to false.
     */
    public Parameter enableDataMarker;

    /** Parameter specifying if markers should be drawn for event points.
     *  This is a boolean that defaults to true.
     */
    public Parameter enableEventsMarker;

    /** Parameter specifying if the legend should be shown on the figure.
     *  This a boolean that defaults to true.
     */
    public Parameter enableLegend;

    /** Parameter specifying the width of the stroke connecting event points
     *  This is an int that defaults to 0.
     */
    public Parameter eventsConnectWidth;

    /** Parameter specifying the radius of markers for event points.
     *  This is an int that defaults to 3.
     */
    public Parameter eventsMarkerRadius;

    /** Parameter giving the event traces in JSON format to be plot.
     *  This defaults to ""
     */
    public StringParameter eventsJSON;

    /** Parameter specifying the width of the figure.
     *  This is an int that defaults to 400px.
     */
    public Parameter graphWidth;

    /** Parameter specifying the height of the figure.
     *  This is an int that defaults to 500px.
     */
    public Parameter graphHeight;

    /** Parameter specifying the horizontal position of the legend on the figure.
     *  This defaults to "center".
     */
    public StringParameter horizontalAlign;

    /** Parameter specifying the target for the link.
     *  The possibilities are:
     *  <ul>
     *  <li><b>_lightbox</b>: Open in a lightbox-style popup frame.
     *  <li><b>_blank</b>: Open in a new window or tab.
     *  <li><b>_self</b>: Open in the same frame as it was clicked.
     *  <li><b>_parent</b>: Open in the parent frameset.
     *  <li><b>_top</b>: Open in the full body of the window.
     *  <li><b><i>framename</i></b>: Open in a named frame.
     *  </ul>
     *  The default is "_lightbox".
     */
    public LinkTarget linkTarget;

    /** Parameter specifying the name of the file to store the data and event traces.
     *  This defaults to "simulationResult.txt".
     */
    public StringParameter outputDataFile;

    /** Parameter specifying the name of the generated HTML file.
     *  This defaults to "JSPlotter.html".
     */
    public StringParameter outputHTMLFile;

    /** Parameter specifying if the data and event traces should be saved in a
     *  separated file. This is a boolean that defaults to false.
     */
    public Parameter saveDataToFile;

    /** Parameter specifying the vertical position of the legend on the figure.
     *  This defaults to "bottom".
     */
    public StringParameter verticalAlign;

    /** Parameter specifying how to parse the X axis value.
     *  This defaults to "linear".
     */
    public StringParameter xAxisMode;

    /** Parameter specifying the title of the X axis.
     *  This defaults to "X Axis".
     */
    public StringParameter xAxisTitle;

    /** Parameter specifying how to parse the Y axis value.
     *  This defaults to "linear".
     */
    public StringParameter yAxisMode;

    /** Parameter specifying the title of the Y axis.
     *  This defaults to "Y Axis".
     */
    public StringParameter yAxisTitle;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** BasicJSPlotter is of type text/html.
     *  @return The string "text/html"
     */
    @Override
    public String getMimeType() {
        return "text/html";
    }

    /** Return true, since new content should overwrite old.
     *  @return true, since new content should overwrite old
     */
    @Override
    public boolean isOverwriteable() {
        return true;
    }

    /** Get the parameter values from the GUI input.
     *  @return The hash map containing the values of all parameters
     *  @exception IllegalActionException
     */
    public HashMap<String, String> getBasicConfig()
            throws IllegalActionException {
        _config.put("outputHTMLFile", outputHTMLFile.stringValue().trim());
        _config.put("dataJSON", dataJSON.stringValue().trim());
        _config.put("eventsJSON", eventsJSON.stringValue().trim());
        _config.put("saveDataToFile",
                ((BooleanToken) saveDataToFile.getToken()).toString().trim());
        _config.put("outputDataFile", outputDataFile.stringValue().trim());

        _config.put("graphTitle", displayText.stringValue().trim());
        _config.put("graphWidth", ((IntToken) graphWidth.getToken()).toString()
                .trim());
        _config.put("graphHeight", ((IntToken) graphHeight.getToken())
                .toString().trim());
        _config.put("autoResize", ((BooleanToken) autoResize.getToken())
                .toString().trim());

        _config.put("enableLegend", ((BooleanToken) enableLegend.getToken())
                .toString().trim());
        _config.put("horizontalAlign", horizontalAlign.stringValue().trim());
        _config.put("verticalAlign", verticalAlign.stringValue().trim());

        _config.put("dataConnectWidth",
                ((IntToken) dataConnectWidth.getToken()).toString().trim());
        _config.put("enableDataMarker",
                ((BooleanToken) enableDataMarker.getToken()).toString().trim());
        _config.put("dataMarkerRadius",
                ((IntToken) dataMarkerRadius.getToken()).toString().trim());

        _config.put("eventsConnectWidth",
                ((IntToken) eventsConnectWidth.getToken()).toString().trim());
        _config.put("enableEventsMarker", ((BooleanToken) enableEventsMarker
                .getToken()).toString().trim());
        _config.put("eventsMarkerRadius",
                ((IntToken) eventsMarkerRadius.getToken()).toString().trim());

        _config.put("xAxisMode", xAxisMode.stringValue().trim());
        _config.put("drawVerticalGridLine",
                ((BooleanToken) drawVerticalGridLine.getToken()).toString()
                .trim());
        _config.put("xAxisTitle", xAxisTitle.stringValue().trim());

        _config.put("yAxisMode", yAxisMode.stringValue().trim());
        _config.put("drawHorizontalGridLine",
                ((BooleanToken) drawHorizontalGridLine.getToken()).toString()
                .trim());
        _config.put("yAxisTitle", yAxisTitle.stringValue().trim());
        return _config;
    }

    /** Get the body content.
     *  @return The string containing the body content
     */
    public String getBodyContent() {
        return _bodyContent.toString();
    }

    /** Get the head content.
     *  @return The string containing the head content
     */
    public String getHeaderContent() {
        _headerContent.append("\t\t<script>\n");
        _headerContent.append(_scriptContent.toString());
        _headerContent.append("\t\t</script>\n");
        return _headerContent.toString();
    }

    /** Get the content of the whole HTML page.
     *  @return The string containing the content of the whole HTML page
     */
    public String getHTMLPageContent() {
        StringBuffer pageContent = new StringBuffer();
        pageContent
        .append("<!DOCTYPE HTML>\n<html>\n\t<head>\n\t\t<meta charset=\"utf-8\">\n");
        pageContent.append(getHeaderContent());
        pageContent.append("\t</head>\n\n\t<body>\n");
        pageContent.append(getBodyContent());
        pageContent.append("\t</body>\n</html>");
        return pageContent.toString();
    }

    /** Insert a string to the body with auto-indent.
     * @param content The content to be inserted
     */
    public void insertBodyContent(String content) {
        _bodyContent.append("\t\t" + content);
    }

    /** Insert a string to the header with auto-indent.
     *  @param isJavaScript Whether the inserted content is JavaScript code
     *  @param autoIndent Whether the content should be auto-indented
     *  @param content the content to be inserted
     */
    public void insertHeaderContent(boolean isJavaScript, boolean autoIndent,
            String content) {
        if (isJavaScript && autoIndent) {
            _scriptContent.append("\t\t\t" + content);
        } else if (!isJavaScript & autoIndent) {
            _headerContent.append("\t\t" + content);
        } else if (isJavaScript && !autoIndent) {
            _scriptContent.append(content);
        } else {
            _headerContent.append(content);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Provide content to the specified web exporter to be
     *  included in a web page for the container of this object.
     *  This class defines an href attribute to associate with
     *  the area of the image map corresponding to its container.
     *
     *  @param exporter  The web exporter to write content to
     *  @exception IllegalActionException If evaluating the value
     *   of this parameter fails, or creating a web attribute fails.
     */
    @Override
    protected void _provideAttributes(WebExporter exporter)
            throws IllegalActionException {
        WebAttribute webAttribute;
        NamedObj container = getContainer();
        _config = new HashMap<String, String>();
        _scriptContent = new StringBuffer();
        _headerContent = new StringBuffer();
        _bodyContent = new StringBuffer();

        if (container != null) {
            // Last argument specifies to overwrite any previous value defined.
            if (!outputHTMLFile.stringValue().trim().equals("")) {
                // Create link attribute and add to exporter.
                // Content should only be added once (onceOnly -> true).
                webAttribute = WebAttribute.createWebAttribute(container,
                        "hrefWebAttribute", "href");
                webAttribute.setExpression(outputHTMLFile.stringValue());
                exporter.defineAttribute(webAttribute, true);
            }

            String targetValue = linkTarget.stringValue();
            if (!targetValue.trim().equals("")) {
                if (targetValue.equals("_lightbox")) {
                    // Strangely, the class has to be "iframe".
                    // I don't understand why it can't be "lightbox".

                    // Create class attribute and add to exporter.
                    // Content should only be added once (onceOnly -> true).
                    webAttribute = WebAttribute.appendToWebAttribute(container,
                            "classWebAttribute", "class", "iframe");
                    exporter.defineAttribute(webAttribute, true);
                } else {

                    // Create target attribute and add to exporter.
                    // Content should only be added once (onceOnly -> true).
                    webAttribute = WebAttribute.createWebAttribute(
                            getContainer(), "targetWebAttribute", "target");
                    webAttribute.setExpression(targetValue);
                    exporter.defineAttribute(webAttribute, true);
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // String buffer storing the body content
    private StringBuffer _bodyContent;

    // HashMap storing the values of all parameters
    private HashMap<String, String> _config;

    // String buff storing the head content
    private StringBuffer _headerContent;

    // String buffer storing the JavaScript content in the head
    private StringBuffer _scriptContent;
}
