package ModelicaServices
  "ModelicaServices (Default implementation) - Models and functions used in the Modelica Standard Library requiring a tool specific implementation"
extends Modelica.Icons.Package;

package Machine

  final constant Real eps=1.e-15 "Biggest number such that 1.0 + eps = 1.0";

  final constant Real small=1.e-60
    "Smallest number such that small and -small are representable on the machine";
  annotation (Documentation(info="<html>
<p>
Package in which processor specific constants are defined that are needed
by numerical algorithms. Typically these constants are not directly used,
but indirectly via the alias definition in
<a href=\"modelica://Modelica.Constants\">Modelica.Constants</a>.
</p>
</html>"));
end Machine;
annotation (
  Protection(access=Access.hide),
  preferredView="info",
  version="3.2.2",
  versionBuild=0,
  versionDate="2016-01-15",
  dateModified = "2016-01-15 08:44:41Z",
  revisionId="$Id:: package.mo 9141 2016-03-03 19:26:06Z #$",
  uses(Modelica(version="3.2.2")),
  conversion(
    noneFromVersion="1.0",
    noneFromVersion="1.1",
    noneFromVersion="1.2",
    noneFromVersion="3.2.1"),
  Documentation(info="<html>
<p>
This package contains a set of functions and models to be used in the
Modelica Standard Library that requires a tool specific implementation.
These are:
</p>

<ul>
<li> <a href=\"modelica://ModelicaServices.Animation.Shape\">Shape</a>
     provides a 3-dim. visualization of elementary
     mechanical objects. It is used in
<a href=\"modelica://Modelica.Mechanics.MultiBody.Visualizers.Advanced.Shape\">Modelica.Mechanics.MultiBody.Visualizers.Advanced.Shape</a>
     via inheritance.</li>

<li> <a href=\"modelica://ModelicaServices.Animation.Surface\">Surface</a>
     provides a 3-dim. visualization of
     moveable parameterized surface. It is used in
<a href=\"modelica://Modelica.Mechanics.MultiBody.Visualizers.Advanced.Surface\">Modelica.Mechanics.MultiBody.Visualizers.Advanced.Surface</a>
     via inheritance.</li>

<li> <a href=\"modelica://ModelicaServices.ExternalReferences.loadResource\">loadResource</a>
     provides a function to return the absolute path name of an URI or a local file name. It is used in
<a href=\"modelica://Modelica.Utilities.Files.loadResource\">Modelica.Utilities.Files.loadResource</a>
     via inheritance.</li>

<li> <a href=\"modelica://ModelicaServices.Machine\">ModelicaServices.Machine</a>
     provides a package of machine constants. It is used in
<a href=\"modelica://Modelica.Constants\">Modelica.Constants</a>.</li>

<li> <a href=\"modelica://ModelicaServices.Types.SolverMethod\">Types.SolverMethod</a>
     provides a string defining the integration method to solve differential equations in
     a clocked discretized continuous-time partition (see Modelica 3.3 language specification).
     It is not yet used in the Modelica Standard Library, but in the Modelica_Synchronous library
     that provides convenience blocks for the clock operators of Modelica version &ge; 3.3.</li>
</ul>

<p>
This implementation is targeted for Dymola.
</p>

<p>
<b>Licensed by DLR and Dassault Syst&egrave;mes AB under the Modelica License 2</b><br>
Copyright &copy; 2009-2016, DLR and Dassault Syst&egrave;mes AB.
</p>

<p>
<i>This Modelica package is <u>free</u> software and the use is completely at <u>your own risk</u>; it can be redistributed and/or modified under the terms of the Modelica License 2. For license conditions (including the disclaimer of warranty) see <a href=\"modelica://Modelica.UsersGuide.ModelicaLicense2\">Modelica.UsersGuide.ModelicaLicense2</a> or visit <a href=\"https://www.modelica.org/licenses/ModelicaLicense2\"> https://www.modelica.org/licenses/ModelicaLicense2</a>.</i>
</p>

</html>"));
end ModelicaServices;

package Modelica "Modelica Standard Library - Version 3.2.2"
extends Modelica.Icons.Package;

  package Blocks
  "Library of basic input/output control blocks (continuous, discrete, logical, table blocks)"
  import SI = Modelica.SIunits;
  extends Modelica.Icons.Package;

    package Interfaces
    "Library of connectors and partial models for input/output blocks"
      import Modelica.SIunits;
      extends Modelica.Icons.InterfacesPackage;

      connector RealInput = input Real "'input Real' as connector" annotation (
        defaultComponentName="u",
        Icon(graphics={
          Polygon(
            lineColor={0,0,127},
            fillColor={0,0,127},
            fillPattern=FillPattern.Solid,
            points={{-100.0,100.0},{100.0,0.0},{-100.0,-100.0}})},
          coordinateSystem(extent={{-100.0,-100.0},{100.0,100.0}},
            preserveAspectRatio=true,
            initialScale=0.2)),
        Diagram(
          coordinateSystem(preserveAspectRatio=true,
            initialScale=0.2,
            extent={{-100.0,-100.0},{100.0,100.0}}),
            graphics={
          Polygon(
            lineColor={0,0,127},
            fillColor={0,0,127},
            fillPattern=FillPattern.Solid,
            points={{0.0,50.0},{100.0,0.0},{0.0,-50.0},{0.0,50.0}}),
          Text(
            lineColor={0,0,127},
            extent={{-10.0,60.0},{-10.0,85.0}},
            textString="%name")}),
        Documentation(info="<html>
<p>
Connector with one input signal of type Real.
</p>
</html>"));

      connector RealOutput = output Real "'output Real' as connector" annotation (
        defaultComponentName="y",
        Icon(
          coordinateSystem(preserveAspectRatio=true,
            extent={{-100.0,-100.0},{100.0,100.0}}),
            graphics={
          Polygon(
            lineColor={0,0,127},
            fillColor={255,255,255},
            fillPattern=FillPattern.Solid,
            points={{-100.0,100.0},{100.0,0.0},{-100.0,-100.0}})}),
        Diagram(
          coordinateSystem(preserveAspectRatio=true,
            extent={{-100.0,-100.0},{100.0,100.0}}),
            graphics={
          Polygon(
            lineColor={0,0,127},
            fillColor={255,255,255},
            fillPattern=FillPattern.Solid,
            points={{-100.0,50.0},{0.0,0.0},{-100.0,-50.0}}),
          Text(
            lineColor={0,0,127},
            extent={{30.0,60.0},{30.0,110.0}},
            textString="%name")}),
        Documentation(info="<html>
<p>
Connector with one output signal of type Real.
</p>
</html>"));

      partial block SO "Single Output continuous control block"
        extends Modelica.Blocks.Icons.Block;

        RealOutput y "Connector of Real output signal" annotation (Placement(
              transformation(extent={{100,-10},{120,10}})));
        annotation (Documentation(info="<html>
<p>
Block has one continuous Real output signal.
</p>
</html>"));

      end SO;
      annotation (Documentation(info="<html>
<p>
This package contains interface definitions for
<b>continuous</b> input/output blocks with Real,
Integer and Boolean signals. Furthermore, it contains
partial models for continuous and discrete blocks.
</p>

</html>",     revisions="<html>
<ul>
<li><i>Oct. 21, 2002</i>
       by <a href=\"http://www.robotic.dlr.de/Martin.Otter/\">Martin Otter</a>
       and Christian Schweiger:<br>
       Added several new interfaces.</li>
<li><i>Oct. 24, 1999</i>
       by <a href=\"http://www.robotic.dlr.de/Martin.Otter/\">Martin Otter</a>:<br>
       RealInputSignal renamed to RealInput. RealOutputSignal renamed to
       output RealOutput. GraphBlock renamed to BlockIcon. SISOreal renamed to
       SISO. SOreal renamed to SO. I2SOreal renamed to M2SO.
       SignalGenerator renamed to SignalSource. Introduced the following
       new models: MIMO, MIMOs, SVcontrol, MVcontrol, DiscreteBlockIcon,
       DiscreteBlock, DiscreteSISO, DiscreteMIMO, DiscreteMIMOs,
       BooleanBlockIcon, BooleanSISO, BooleanSignalSource, MI2BooleanMOs.</li>
<li><i>June 30, 1999</i>
       by <a href=\"http://www.robotic.dlr.de/Martin.Otter/\">Martin Otter</a>:<br>
       Realized a first version, based on an existing Dymola library
       of Dieter Moormann and Hilding Elmqvist.</li>
</ul>
</html>"));
    end Interfaces;

    package Sources
    "Library of signal source blocks generating Real and Boolean signals"
      import Modelica.Blocks.Interfaces;
      import Modelica.SIunits;
      extends Modelica.Icons.SourcesPackage;

      block Constant "Generate constant signal of type Real"
        parameter Real k(start=1) "Constant output value";
        extends Interfaces.SO;

      equation
        y = k;
        annotation (
          defaultComponentName="const",
          Icon(coordinateSystem(
              preserveAspectRatio=true,
              extent={{-100,-100},{100,100}}), graphics={
              Line(points={{-80,68},{-80,-80}}, color={192,192,192}),
              Polygon(
                points={{-80,90},{-88,68},{-72,68},{-80,90}},
                lineColor={192,192,192},
                fillColor={192,192,192},
                fillPattern=FillPattern.Solid),
              Line(points={{-90,-70},{82,-70}}, color={192,192,192}),
              Polygon(
                points={{90,-70},{68,-62},{68,-78},{90,-70}},
                lineColor={192,192,192},
                fillColor={192,192,192},
                fillPattern=FillPattern.Solid),
              Line(points={{-80,0},{80,0}}),
              Text(
                extent={{-150,-150},{150,-110}},
                lineColor={0,0,0},
                textString="k=%k")}),
          Diagram(coordinateSystem(
              preserveAspectRatio=true,
              extent={{-100,-100},{100,100}}), graphics={
              Polygon(
                points={{-80,90},{-86,68},{-74,68},{-80,90}},
                lineColor={95,95,95},
                fillColor={95,95,95},
                fillPattern=FillPattern.Solid),
              Line(points={{-80,68},{-80,-80}}, color={95,95,95}),
              Line(
                points={{-80,0},{80,0}},
                color={0,0,255},
                thickness=0.5),
              Line(points={{-90,-70},{82,-70}}, color={95,95,95}),
              Polygon(
                points={{90,-70},{68,-64},{68,-76},{90,-70}},
                lineColor={95,95,95},
                fillColor={95,95,95},
                fillPattern=FillPattern.Solid),
              Text(
                extent={{-83,92},{-30,74}},
                lineColor={0,0,0},
                textString="y"),
              Text(
                extent={{70,-80},{94,-100}},
                lineColor={0,0,0},
                textString="time"),
              Text(
                extent={{-101,8},{-81,-12}},
                lineColor={0,0,0},
                textString="k")}),
          Documentation(info="<html>
<p>
The Real output y is a constant signal:
</p>

<p>
<img src=\"modelica://Modelica/Resources/Images/Blocks/Sources/Constant.png\"
     alt=\"Constant.png\">
</p>
</html>"));
      end Constant;

      block Pulse "Generate pulse signal of type Real"
        parameter Real amplitude=1 "Amplitude of pulse";
        parameter Real width(
          final min=Modelica.Constants.small,
          final max=100) = 50 "Width of pulse in % of period";
        parameter Modelica.SIunits.Time period(final min=Modelica.Constants.small,
            start=1) "Time for one period";
        parameter Integer nperiod=-1
          "Number of periods (< 0 means infinite number of periods)";
        parameter Real offset=0 "Offset of output signals";
        parameter Modelica.SIunits.Time startTime=0
          "Output = offset for time < startTime";
        extends Modelica.Blocks.Interfaces.SO;
    protected
        Modelica.SIunits.Time T_width=period*width/100;
        Modelica.SIunits.Time T_start "Start time of current period";
        Integer count "Period count";
      initial algorithm
        count := integer((time - startTime)/period);
        T_start := startTime + count*period;
      equation
        when integer((time - startTime)/period) > pre(count) then
          count = pre(count) + 1;
          T_start = time;
        end when;
        y = offset + (if (time < startTime or nperiod == 0 or (nperiod > 0 and
          count >= nperiod)) then 0 else if time < T_start + T_width then amplitude
           else 0);
        annotation (
          Icon(coordinateSystem(
              preserveAspectRatio=true,
              extent={{-100,-100},{100,100}}), graphics={
              Line(points={{-80,68},{-80,-80}}, color={192,192,192}),
              Polygon(
                points={{-80,90},{-88,68},{-72,68},{-80,90}},
                lineColor={192,192,192},
                fillColor={192,192,192},
                fillPattern=FillPattern.Solid),
              Line(points={{-90,-70},{82,-70}}, color={192,192,192}),
              Polygon(
                points={{90,-70},{68,-62},{68,-78},{90,-70}},
                lineColor={192,192,192},
                fillColor={192,192,192},
                fillPattern=FillPattern.Solid),
              Line(points={{-80,-70},{-40,-70},{-40,44},{0,44},{0,-70},{40,-70},{40,
                    44},{79,44}}),
              Text(
                extent={{-147,-152},{153,-112}},
                lineColor={0,0,0},
                textString="period=%period")}),
          Diagram(coordinateSystem(
              preserveAspectRatio=true,
              extent={{-100,-100},{100,100}}), graphics={
              Polygon(
                points={{-80,90},{-85,68},{-75,68},{-80,90}},
                lineColor={95,95,95},
                fillColor={95,95,95},
                fillPattern=FillPattern.Solid),
              Line(points={{-80,68},{-80,-80}}, color={95,95,95}),
              Line(points={{-90,-70},{82,-70}}, color={95,95,95}),
              Polygon(
                points={{90,-70},{68,-65},{68,-75},{90,-70}},
                lineColor={95,95,95},
                fillColor={95,95,95},
                fillPattern=FillPattern.Solid),
              Polygon(
                points={{-34,0},{-37,-13},{-31,-13},{-34,0}},
                lineColor={95,95,95},
                fillColor={95,95,95},
                fillPattern=FillPattern.Solid),
              Line(points={{-34,0},{-34,-70}},  color={95,95,95}),
              Polygon(
                points={{-34,-70},{-37,-57},{-31,-57},{-34,-70},{-34,-70}},
                lineColor={95,95,95},
                fillColor={95,95,95},
                fillPattern=FillPattern.Solid),
              Text(
                extent={{-78,-24},{-35,-36}},
                lineColor={0,0,0},
                textString="offset"),
              Text(
                extent={{-30,-72},{16,-81}},
                lineColor={0,0,0},
                textString="startTime"),
              Text(
                extent={{-82,96},{-49,79}},
                lineColor={0,0,0},
                textString="y"),
              Text(
                extent={{66,-80},{87,-90}},
                lineColor={0,0,0},
                textString="time"),
              Line(points={{-10,0},{-10,-70}}, color={95,95,95}),
              Line(
                points={{-80,0},{-10,0},{-10,50},{30,50},{30,0},{50,0},{50,50},{90,
                    50}},
                color={0,0,255},
                thickness=0.5),
              Line(points={{-10,88},{-10,50}}, color={95,95,95}),
              Line(points={{30,74},{30,50}}, color={95,95,95}),
              Line(points={{50,88},{50,50}}, color={95,95,95}),
              Line(points={{-10,83},{50,83}}, color={95,95,95}),
              Line(points={{-10,69},{30,69}}, color={95,95,95}),
              Text(
                extent={{-3,93},{39,84}},
                lineColor={0,0,0},
                textString="period"),
              Text(
                extent={{-7,78},{30,69}},
                lineColor={0,0,0},
                textString="width"),
              Line(points={{-43,50},{-10,50}}, color={95,95,95}),
              Line(points={{-34,50},{-34,0}}, color={95,95,95}),
              Text(
                extent={{-77,30},{-37,21}},
                lineColor={0,0,0},
                textString="amplitude"),
              Polygon(
                points={{-34,50},{-37,37},{-31,37},{-34,50}},
                lineColor={95,95,95},
                fillColor={95,95,95},
                fillPattern=FillPattern.Solid),
              Polygon(
                points={{-34,0},{-37,13},{-31,13},{-34,0},{-34,0}},
                lineColor={95,95,95},
                fillColor={95,95,95},
                fillPattern=FillPattern.Solid),
              Line(
                points={{90,50},{90,0},{100,0}},
                color={0,0,255},
                thickness=0.5),
              Polygon(
                points={{-10,69},{-1,71},{-1,67},{-10,69}},
                lineColor={95,95,95},
                fillColor={95,95,95},
                fillPattern=FillPattern.Solid),
              Polygon(
                points={{30,69},{22,71},{22,67},{30,69}},
                lineColor={95,95,95},
                fillColor={95,95,95},
                fillPattern=FillPattern.Solid),
              Polygon(
                points={{-10,83},{-1,85},{-1,81},{-10,83}},
                lineColor={95,95,95},
                fillColor={95,95,95},
                fillPattern=FillPattern.Solid),
              Polygon(
                points={{50,83},{42,85},{42,81},{50,83}},
                lineColor={95,95,95},
                fillColor={95,95,95},
                fillPattern=FillPattern.Solid)}),
          Documentation(info="<html>
<p>
The Real output y is a pulse signal:
</p>

<p>
<img src=\"modelica://Modelica/Resources/Images/Blocks/Sources/Pulse.png\"
     alt=\"Pulse.png\">
</p>
</html>"));
      end Pulse;
      annotation (Documentation(info="<html>
<p>
This package contains <b>source</b> components, i.e., blocks which
have only output signals. These blocks are used as signal generators
for Real, Integer and Boolean signals.
</p>

<p>
All Real source signals (with the exception of the Constant source)
have at least the following two parameters:
</p>

<table border=1 cellspacing=0 cellpadding=2>
  <tr><td valign=\"top\"><b>offset</b></td>
      <td valign=\"top\">Value which is added to the signal</td>
  </tr>
  <tr><td valign=\"top\"><b>startTime</b></td>
      <td valign=\"top\">Start time of signal. For time &lt; startTime,
                the output y is set to offset.</td>
  </tr>
</table>

<p>
The <b>offset</b> parameter is especially useful in order to shift
the corresponding source, such that at initial time the system
is stationary. To determine the corresponding value of offset,
usually requires a trimming calculation.
</p>
</html>",     revisions="<html>
<ul>
<li><i>October 21, 2002</i>
       by <a href=\"http://www.robotic.dlr.de/Martin.Otter/\">Martin Otter</a>
       and Christian Schweiger:<br>
       Integer sources added. Step, TimeTable and BooleanStep slightly changed.</li>
<li><i>Nov. 8, 1999</i>
       by <a href=\"mailto:clauss@eas.iis.fhg.de\">Christoph Clau&szlig;</a>,
       <a href=\"mailto:Andre.Schneider@eas.iis.fraunhofer.de\">Andre.Schneider@eas.iis.fraunhofer.de</a>,
       <a href=\"http://www.robotic.dlr.de/Martin.Otter/\">Martin Otter</a>:<br>
       New sources: Exponentials, TimeTable. Trapezoid slightly enhanced
       (nperiod=-1 is an infinite number of periods).</li>
<li><i>Oct. 31, 1999</i>
       by <a href=\"http://www.robotic.dlr.de/Martin.Otter/\">Martin Otter</a>:<br>
       <a href=\"mailto:clauss@eas.iis.fhg.de\">Christoph Clau&szlig;</a>,
       <a href=\"mailto:Andre.Schneider@eas.iis.fraunhofer.de\">Andre.Schneider@eas.iis.fraunhofer.de</a>,
       All sources vectorized. New sources: ExpSine, Trapezoid,
       BooleanConstant, BooleanStep, BooleanPulse, SampleTrigger.
       Improved documentation, especially detailed description of
       signals in diagram layer.</li>
<li><i>June 29, 1999</i>
       by <a href=\"http://www.robotic.dlr.de/Martin.Otter/\">Martin Otter</a>:<br>
       Realized a first version, based on an existing Dymola library
       of Dieter Moormann and Hilding Elmqvist.</li>
</ul>
</html>"));
    end Sources;

    package Icons "Icons for Blocks"
        extends Modelica.Icons.IconsPackage;

        partial block Block "Basic graphical layout of input/output block"

          annotation (
            Icon(coordinateSystem(preserveAspectRatio=true, extent={{-100,-100},{
                  100,100}}), graphics={Rectangle(
                extent={{-100,-100},{100,100}},
                lineColor={0,0,127},
                fillColor={255,255,255},
                fillPattern=FillPattern.Solid), Text(
                extent={{-150,150},{150,110}},
                textString="%name",
                lineColor={0,0,255})}),
          Documentation(info="<html>
<p>
Block that has only the basic icon for an input/output
block (no declarations, no equations). Most blocks
of package Modelica.Blocks inherit directly or indirectly
from this block.
</p>
</html>"));

        end Block;
    end Icons;
  annotation (Icon(coordinateSystem(preserveAspectRatio=true, extent={{-100.0,-100.0},{100.0,100.0}}), graphics={
        Rectangle(
          origin={0.0,35.1488},
          fillColor={255,255,255},
          extent={{-30.0,-20.1488},{30.0,20.1488}}),
        Rectangle(
          origin={0.0,-34.8512},
          fillColor={255,255,255},
          extent={{-30.0,-20.1488},{30.0,20.1488}}),
        Line(
          origin={-51.25,0.0},
          points={{21.25,-35.0},{-13.75,-35.0},{-13.75,35.0},{6.25,35.0}}),
        Polygon(
          origin={-40.0,35.0},
          pattern=LinePattern.None,
          fillPattern=FillPattern.Solid,
          points={{10.0,0.0},{-5.0,5.0},{-5.0,-5.0}}),
        Line(
          origin={51.25,0.0},
          points={{-21.25,35.0},{13.75,35.0},{13.75,-35.0},{-6.25,-35.0}}),
        Polygon(
          origin={40.0,-35.0},
          pattern=LinePattern.None,
          fillPattern=FillPattern.Solid,
          points={{-10.0,0.0},{5.0,5.0},{5.0,-5.0}})}), Documentation(info="<html>
<p>
This library contains input/output blocks to build up block diagrams.
</p>

<dl>
<dt><b>Main Author:</b></dt>
<dd><a href=\"http://www.robotic.dlr.de/Martin.Otter/\">Martin Otter</a><br>
    Deutsches Zentrum f&uuml;r Luft und Raumfahrt e. V. (DLR)<br>
    Oberpfaffenhofen<br>
    Postfach 1116<br>
    D-82230 Wessling<br>
    email: <A HREF=\"mailto:Martin.Otter@dlr.de\">Martin.Otter@dlr.de</A><br></dd>
</dl>
<p>
Copyright &copy; 1998-2016, Modelica Association and DLR.
</p>
<p>
<i>This Modelica package is <u>free</u> software and the use is completely at <u>your own risk</u>; it can be redistributed and/or modified under the terms of the Modelica License 2. For license conditions (including the disclaimer of warranty) see <a href=\"modelica://Modelica.UsersGuide.ModelicaLicense2\">Modelica.UsersGuide.ModelicaLicense2</a> or visit <a href=\"https://www.modelica.org/licenses/ModelicaLicense2\"> https://www.modelica.org/licenses/ModelicaLicense2</a>.</i>
</p>
</html>",   revisions="<html>
<ul>
<li><i>June 23, 2004</i>
       by <a href=\"http://www.robotic.dlr.de/Martin.Otter/\">Martin Otter</a>:<br>
       Introduced new block connectors and adapted all blocks to the new connectors.
       Included subpackages Continuous, Discrete, Logical, Nonlinear from
       package ModelicaAdditions.Blocks.
       Included subpackage ModelicaAdditions.Table in Modelica.Blocks.Sources
       and in the new package Modelica.Blocks.Tables.
       Added new blocks to Blocks.Sources and Blocks.Logical.
       </li>
<li><i>October 21, 2002</i>
       by <a href=\"http://www.robotic.dlr.de/Martin.Otter/\">Martin Otter</a>
       and Christian Schweiger:<br>
       New subpackage Examples, additional components.
       </li>
<li><i>June 20, 2000</i>
       by <a href=\"http://www.robotic.dlr.de/Martin.Otter/\">Martin Otter</a> and
       Michael Tiller:<br>
       Introduced a replaceable signal type into
       Blocks.Interfaces.RealInput/RealOutput:
<pre>
   replaceable type SignalType = Real
</pre>
       in order that the type of the signal of an input/output block
       can be changed to a physical type, for example:
<pre>
   Sine sin1(outPort(redeclare type SignalType=Modelica.SIunits.Torque))
</pre>
      </li>
<li><i>Sept. 18, 1999</i>
       by <a href=\"http://www.robotic.dlr.de/Martin.Otter/\">Martin Otter</a>:<br>
       Renamed to Blocks. New subpackages Math, Nonlinear.
       Additional components in subpackages Interfaces, Continuous
       and Sources. </li>
<li><i>June 30, 1999</i>
       by <a href=\"http://www.robotic.dlr.de/Martin.Otter/\">Martin Otter</a>:<br>
       Realized a first version, based on an existing Dymola library
       of Dieter Moormann and Hilding Elmqvist.</li>
</ul>
</html>"));
  end Blocks;

  package Media "Library of media property models"
  extends Modelica.Icons.Package;
  import SI = Modelica.SIunits;
  import Cv = Modelica.SIunits.Conversions;

  package Interfaces "Interfaces for media models"
    extends Modelica.Icons.InterfacesPackage;

    partial package PartialMedium
      "Partial medium properties (base package of all media packages)"
      extends Modelica.Media.Interfaces.Types;
      extends Modelica.Icons.MaterialPropertiesPackage;

      // Constants to be set in Medium
      constant Modelica.Media.Interfaces.Choices.IndependentVariables
        ThermoStates "Enumeration type for independent variables";
      constant String mediumName="unusablePartialMedium" "Name of the medium";
      constant String substanceNames[:]={mediumName}
        "Names of the mixture substances. Set substanceNames={mediumName} if only one substance.";
      constant String extraPropertiesNames[:]=fill("", 0)
        "Names of the additional (extra) transported properties. Set extraPropertiesNames=fill(\"\",0) if unused";
      constant Boolean singleState
        "= true, if u and d are not a function of pressure";
      constant Boolean reducedX=true
        "= true if medium contains the equation sum(X) = 1.0; set reducedX=true if only one substance (see docu for details)";
      constant Boolean fixedX=false
        "= true if medium contains the equation X = reference_X";
      constant AbsolutePressure reference_p=101325
        "Reference pressure of Medium: default 1 atmosphere";
      constant Temperature reference_T=298.15
        "Reference temperature of Medium: default 25 deg Celsius";
      constant MassFraction reference_X[nX]=fill(1/nX, nX)
        "Default mass fractions of medium";
      constant AbsolutePressure p_default=101325
        "Default value for pressure of medium (for initialization)";
      constant Temperature T_default=Modelica.SIunits.Conversions.from_degC(20)
        "Default value for temperature of medium (for initialization)";
      constant SpecificEnthalpy h_default=specificEnthalpy_pTX(
              p_default,
              T_default,
              X_default)
        "Default value for specific enthalpy of medium (for initialization)";
      constant MassFraction X_default[nX]=reference_X
        "Default value for mass fractions of medium (for initialization)";

      final constant Integer nS=size(substanceNames, 1) "Number of substances"
        annotation (Evaluate=true);
      constant Integer nX=nS "Number of mass fractions" annotation (Evaluate=true);
      constant Integer nXi=if fixedX then 0 else if reducedX then nS - 1 else nS
        "Number of structurally independent mass fractions (see docu for details)"
        annotation (Evaluate=true);

      final constant Integer nC=size(extraPropertiesNames, 1)
        "Number of extra (outside of standard mass-balance) transported properties"
        annotation (Evaluate=true);
      constant Real C_nominal[nC](min=fill(Modelica.Constants.eps, nC)) = 1.0e-6*
        ones(nC) "Default for the nominal values for the extra properties";
      replaceable record FluidConstants =
          Modelica.Media.Interfaces.Types.Basic.FluidConstants
        "Critical, triple, molecular and other standard data of fluid";

      replaceable record ThermodynamicState
        "Minimal variable set that is available as input argument to every medium function"
        extends Modelica.Icons.Record;
      end ThermodynamicState;

      replaceable partial model BaseProperties
        "Base properties (p, d, T, h, u, R, MM and, if applicable, X and Xi) of a medium"
        InputAbsolutePressure p "Absolute pressure of medium";
        InputMassFraction[nXi] Xi(start=reference_X[1:nXi])
          "Structurally independent mass fractions";
        InputSpecificEnthalpy h "Specific enthalpy of medium";
        Density d "Density of medium";
        Temperature T "Temperature of medium";
        MassFraction[nX] X(start=reference_X)
          "Mass fractions (= (component mass)/total mass  m_i/m)";
        SpecificInternalEnergy u "Specific internal energy of medium";
        SpecificHeatCapacity R "Gas constant (of mixture if applicable)";
        MolarMass MM "Molar mass (of mixture or single fluid)";
        ThermodynamicState state
          "Thermodynamic state record for optional functions";
        parameter Boolean preferredMediumStates=false
          "= true if StateSelect.prefer shall be used for the independent property variables of the medium"
          annotation (Evaluate=true, Dialog(tab="Advanced"));
        parameter Boolean standardOrderComponents=true
          "If true, and reducedX = true, the last element of X will be computed from the other ones";
        SI.Conversions.NonSIunits.Temperature_degC T_degC=
            Modelica.SIunits.Conversions.to_degC(T)
          "Temperature of medium in [degC]";
        SI.Conversions.NonSIunits.Pressure_bar p_bar=
            Modelica.SIunits.Conversions.to_bar(p)
          "Absolute pressure of medium in [bar]";

        // Local connector definition, used for equation balancing check
        connector InputAbsolutePressure = input SI.AbsolutePressure
          "Pressure as input signal connector";
        connector InputSpecificEnthalpy = input SI.SpecificEnthalpy
          "Specific enthalpy as input signal connector";
        connector InputMassFraction = input SI.MassFraction
          "Mass fraction as input signal connector";

      equation
        if standardOrderComponents then
          Xi = X[1:nXi];

          if fixedX then
            X = reference_X;
          end if;
          if reducedX and not fixedX then
            X[nX] = 1 - sum(Xi);
          end if;
          for i in 1:nX loop
            assert(X[i] >= -1.e-5 and X[i] <= 1 + 1.e-5, "Mass fraction X[" +
              String(i) + "] = " + String(X[i]) + "of substance " +
              substanceNames[i] + "\nof medium " + mediumName +
              " is not in the range 0..1");
          end for;

        end if;

        assert(p >= 0.0, "Pressure (= " + String(p) + " Pa) of medium \"" +
          mediumName + "\" is negative\n(Temperature = " + String(T) + " K)");
        annotation (Icon(coordinateSystem(preserveAspectRatio=true, extent={{-100,
                  -100},{100,100}}), graphics={Rectangle(
                extent={{-100,100},{100,-100}},
                fillColor={255,255,255},
                fillPattern=FillPattern.Solid,
                lineColor={0,0,255}), Text(
                extent={{-152,164},{152,102}},
                textString="%name",
                lineColor={0,0,255})}), Documentation(info="<html>
<p>
Model <b>BaseProperties</b> is a model within package <b>PartialMedium</b>
and contains the <b>declarations</b> of the minimum number of
variables that every medium model is supposed to support.
A specific medium inherits from model <b>BaseProperties</b> and provides
the equations for the basic properties.</p>
<p>
The BaseProperties model contains the following <b>7+nXi variables</b>
(nXi is the number of independent mass fractions defined in package
PartialMedium):
</p>
<table border=1 cellspacing=0 cellpadding=2>
  <tr><td valign=\"top\"><b>Variable</b></td>
      <td valign=\"top\"><b>Unit</b></td>
      <td valign=\"top\"><b>Description</b></td></tr>
  <tr><td valign=\"top\">T</td>
      <td valign=\"top\">K</td>
      <td valign=\"top\">temperature</td></tr>
  <tr><td valign=\"top\">p</td>
      <td valign=\"top\">Pa</td>
      <td valign=\"top\">absolute pressure</td></tr>
  <tr><td valign=\"top\">d</td>
      <td valign=\"top\">kg/m3</td>
      <td valign=\"top\">density</td></tr>
  <tr><td valign=\"top\">h</td>
      <td valign=\"top\">J/kg</td>
      <td valign=\"top\">specific enthalpy</td></tr>
  <tr><td valign=\"top\">u</td>
      <td valign=\"top\">J/kg</td>
      <td valign=\"top\">specific internal energy</td></tr>
  <tr><td valign=\"top\">Xi[nXi]</td>
      <td valign=\"top\">kg/kg</td>
      <td valign=\"top\">independent mass fractions m_i/m</td></tr>
  <tr><td valign=\"top\">R</td>
      <td valign=\"top\">J/kg.K</td>
      <td valign=\"top\">gas constant</td></tr>
  <tr><td valign=\"top\">M</td>
      <td valign=\"top\">kg/mol</td>
      <td valign=\"top\">molar mass</td></tr>
</table>
<p>
In order to implement an actual medium model, one can extend from this
base model and add <b>5 equations</b> that provide relations among
these variables. Equations will also have to be added in order to
set all the variables within the ThermodynamicState record state.</p>
<p>
If standardOrderComponents=true, the full composition vector X[nX]
is determined by the equations contained in this base class, depending
on the independent mass fraction vector Xi[nXi].</p>
<p>Additional <b>2 + nXi</b> equations will have to be provided
when using the BaseProperties model, in order to fully specify the
thermodynamic conditions. The input connector qualifier applied to
p, h, and nXi indirectly declares the number of missing equations,
permitting advanced equation balance checking by Modelica tools.
Please note that this doesn't mean that the additional equations
should be connection equations, nor that exactly those variables
should be supplied, in order to complete the model.
For further information, see the Modelica.Media User's guide, and
Section 4.7 (Balanced Models) of the Modelica 3.0 specification.</p>
</html>"));
      end BaseProperties;

      replaceable partial function setState_pTX
        "Return thermodynamic state as function of p, T and composition X or Xi"
        extends Modelica.Icons.Function;
        input AbsolutePressure p "Pressure";
        input Temperature T "Temperature";
        input MassFraction X[:]=reference_X "Mass fractions";
        output ThermodynamicState state "Thermodynamic state record";
      end setState_pTX;

      replaceable partial function setState_phX
        "Return thermodynamic state as function of p, h and composition X or Xi"
        extends Modelica.Icons.Function;
        input AbsolutePressure p "Pressure";
        input SpecificEnthalpy h "Specific enthalpy";
        input MassFraction X[:]=reference_X "Mass fractions";
        output ThermodynamicState state "Thermodynamic state record";
      end setState_phX;

      replaceable partial function setState_psX
        "Return thermodynamic state as function of p, s and composition X or Xi"
        extends Modelica.Icons.Function;
        input AbsolutePressure p "Pressure";
        input SpecificEntropy s "Specific entropy";
        input MassFraction X[:]=reference_X "Mass fractions";
        output ThermodynamicState state "Thermodynamic state record";
      end setState_psX;

      replaceable partial function setState_dTX
        "Return thermodynamic state as function of d, T and composition X or Xi"
        extends Modelica.Icons.Function;
        input Density d "Density";
        input Temperature T "Temperature";
        input MassFraction X[:]=reference_X "Mass fractions";
        output ThermodynamicState state "Thermodynamic state record";
      end setState_dTX;

      replaceable partial function setSmoothState
        "Return thermodynamic state so that it smoothly approximates: if x > 0 then state_a else state_b"
        extends Modelica.Icons.Function;
        input Real x "m_flow or dp";
        input ThermodynamicState state_a "Thermodynamic state if x > 0";
        input ThermodynamicState state_b "Thermodynamic state if x < 0";
        input Real x_small(min=0)
          "Smooth transition in the region -x_small < x < x_small";
        output ThermodynamicState state
          "Smooth thermodynamic state for all x (continuous and differentiable)";
        annotation (Documentation(info="<html>
<p>
This function is used to approximate the equation
</p>
<pre>
    state = <b>if</b> x &gt; 0 <b>then</b> state_a <b>else</b> state_b;
</pre>

<p>
by a smooth characteristic, so that the expression is continuous and differentiable:
</p>

<pre>
   state := <b>smooth</b>(1, <b>if</b> x &gt;  x_small <b>then</b> state_a <b>else</b>
                      <b>if</b> x &lt; -x_small <b>then</b> state_b <b>else</b> f(state_a, state_b));
</pre>

<p>
This is performed by applying function <b>Media.Common.smoothStep</b>(..)
on every element of the thermodynamic state record.
</p>

<p>
If <b>mass fractions</b> X[:] are approximated with this function then this can be performed
for all <b>nX</b> mass fractions, instead of applying it for nX-1 mass fractions and computing
the last one by the mass fraction constraint sum(X)=1. The reason is that the approximating function has the
property that sum(state.X) = 1, provided sum(state_a.X) = sum(state_b.X) = 1.
This can be shown by evaluating the approximating function in the abs(x) &lt; x_small
region (otherwise state.X is either state_a.X or state_b.X):
</p>

<pre>
    X[1]  = smoothStep(x, X_a[1] , X_b[1] , x_small);
    X[2]  = smoothStep(x, X_a[2] , X_b[2] , x_small);
       ...
    X[nX] = smoothStep(x, X_a[nX], X_b[nX], x_small);
</pre>

<p>
or
</p>

<pre>
    X[1]  = c*(X_a[1]  - X_b[1])  + (X_a[1]  + X_b[1])/2
    X[2]  = c*(X_a[2]  - X_b[2])  + (X_a[2]  + X_b[2])/2;
       ...
    X[nX] = c*(X_a[nX] - X_b[nX]) + (X_a[nX] + X_b[nX])/2;
    c     = (x/x_small)*((x/x_small)^2 - 3)/4
</pre>

<p>
Summing all mass fractions together results in
</p>

<pre>
    sum(X) = c*(sum(X_a) - sum(X_b)) + (sum(X_a) + sum(X_b))/2
           = c*(1 - 1) + (1 + 1)/2
           = 1
</pre>

</html>"));
      end setSmoothState;

      replaceable partial function dynamicViscosity "Return dynamic viscosity"
        extends Modelica.Icons.Function;
        input ThermodynamicState state "Thermodynamic state record";
        output DynamicViscosity eta "Dynamic viscosity";
      end dynamicViscosity;

      replaceable partial function thermalConductivity
        "Return thermal conductivity"
        extends Modelica.Icons.Function;
        input ThermodynamicState state "Thermodynamic state record";
        output ThermalConductivity lambda "Thermal conductivity";
      end thermalConductivity;

      replaceable function prandtlNumber "Return the Prandtl number"
        extends Modelica.Icons.Function;
        input ThermodynamicState state "Thermodynamic state record";
        output PrandtlNumber Pr "Prandtl number";
      algorithm
        Pr := dynamicViscosity(state)*specificHeatCapacityCp(state)/
          thermalConductivity(state);
      end prandtlNumber;

      replaceable partial function pressure "Return pressure"
        extends Modelica.Icons.Function;
        input ThermodynamicState state "Thermodynamic state record";
        output AbsolutePressure p "Pressure";
      end pressure;

      replaceable partial function temperature "Return temperature"
        extends Modelica.Icons.Function;
        input ThermodynamicState state "Thermodynamic state record";
        output Temperature T "Temperature";
      end temperature;

      replaceable partial function density "Return density"
        extends Modelica.Icons.Function;
        input ThermodynamicState state "Thermodynamic state record";
        output Density d "Density";
      end density;

      replaceable partial function specificEnthalpy "Return specific enthalpy"
        extends Modelica.Icons.Function;
        input ThermodynamicState state "Thermodynamic state record";
        output SpecificEnthalpy h "Specific enthalpy";
      end specificEnthalpy;

      replaceable partial function specificInternalEnergy
        "Return specific internal energy"
        extends Modelica.Icons.Function;
        input ThermodynamicState state "Thermodynamic state record";
        output SpecificEnergy u "Specific internal energy";
      end specificInternalEnergy;

      replaceable partial function specificEntropy "Return specific entropy"
        extends Modelica.Icons.Function;
        input ThermodynamicState state "Thermodynamic state record";
        output SpecificEntropy s "Specific entropy";
      end specificEntropy;

      replaceable partial function specificGibbsEnergy
        "Return specific Gibbs energy"
        extends Modelica.Icons.Function;
        input ThermodynamicState state "Thermodynamic state record";
        output SpecificEnergy g "Specific Gibbs energy";
      end specificGibbsEnergy;

      replaceable partial function specificHelmholtzEnergy
        "Return specific Helmholtz energy"
        extends Modelica.Icons.Function;
        input ThermodynamicState state "Thermodynamic state record";
        output SpecificEnergy f "Specific Helmholtz energy";
      end specificHelmholtzEnergy;

      replaceable partial function specificHeatCapacityCp
        "Return specific heat capacity at constant pressure"
        extends Modelica.Icons.Function;
        input ThermodynamicState state "Thermodynamic state record";
        output SpecificHeatCapacity cp
          "Specific heat capacity at constant pressure";
      end specificHeatCapacityCp;

      function heatCapacity_cp = specificHeatCapacityCp
        "Alias for deprecated name";

      replaceable partial function specificHeatCapacityCv
        "Return specific heat capacity at constant volume"
        extends Modelica.Icons.Function;
        input ThermodynamicState state "Thermodynamic state record";
        output SpecificHeatCapacity cv
          "Specific heat capacity at constant volume";
      end specificHeatCapacityCv;

      function heatCapacity_cv = specificHeatCapacityCv
        "Alias for deprecated name";

      replaceable partial function isentropicExponent
        "Return isentropic exponent"
        extends Modelica.Icons.Function;
        input ThermodynamicState state "Thermodynamic state record";
        output IsentropicExponent gamma "Isentropic exponent";
      end isentropicExponent;

      replaceable partial function isentropicEnthalpy
        "Return isentropic enthalpy"
        extends Modelica.Icons.Function;
        input AbsolutePressure p_downstream "Downstream pressure";
        input ThermodynamicState refState "Reference state for entropy";
        output SpecificEnthalpy h_is "Isentropic enthalpy";
        annotation (Documentation(info="<html>
<p>
This function computes an isentropic state transformation:
</p>
<ol>
<li> A medium is in a particular state, refState.</li>
<li> The enthalpy at another state (h_is) shall be computed
     under the assumption that the state transformation from refState to h_is
     is performed with a change of specific entropy ds = 0 and the pressure of state h_is
     is p_downstream and the composition X upstream and downstream is assumed to be the same.</li>
</ol>

</html>"));
      end isentropicEnthalpy;

      replaceable partial function velocityOfSound "Return velocity of sound"
        extends Modelica.Icons.Function;
        input ThermodynamicState state "Thermodynamic state record";
        output VelocityOfSound a "Velocity of sound";
      end velocityOfSound;

      replaceable partial function isobaricExpansionCoefficient
        "Return overall the isobaric expansion coefficient beta"
        extends Modelica.Icons.Function;
        input ThermodynamicState state "Thermodynamic state record";
        output IsobaricExpansionCoefficient beta "Isobaric expansion coefficient";
        annotation (Documentation(info="<html>
<pre>
beta is defined as  1/v * der(v,T), with v = 1/d, at constant pressure p.
</pre>
</html>"));
      end isobaricExpansionCoefficient;

      function beta = isobaricExpansionCoefficient
        "Alias for isobaricExpansionCoefficient for user convenience";

      replaceable partial function isothermalCompressibility
        "Return overall the isothermal compressibility factor"
        extends Modelica.Icons.Function;
        input ThermodynamicState state "Thermodynamic state record";
        output SI.IsothermalCompressibility kappa "Isothermal compressibility";
        annotation (Documentation(info="<html>
<pre>

kappa is defined as - 1/v * der(v,p), with v = 1/d at constant temperature T.

</pre>
</html>"));
      end isothermalCompressibility;

      function kappa = isothermalCompressibility
        "Alias of isothermalCompressibility for user convenience";

      // explicit derivative functions for finite element models
      replaceable partial function density_derp_h
        "Return density derivative w.r.t. pressure at const specific enthalpy"
        extends Modelica.Icons.Function;
        input ThermodynamicState state "Thermodynamic state record";
        output DerDensityByPressure ddph "Density derivative w.r.t. pressure";
      end density_derp_h;

      replaceable partial function density_derh_p
        "Return density derivative w.r.t. specific enthalpy at constant pressure"
        extends Modelica.Icons.Function;
        input ThermodynamicState state "Thermodynamic state record";
        output DerDensityByEnthalpy ddhp
          "Density derivative w.r.t. specific enthalpy";
      end density_derh_p;

      replaceable partial function density_derp_T
        "Return density derivative w.r.t. pressure at const temperature"
        extends Modelica.Icons.Function;
        input ThermodynamicState state "Thermodynamic state record";
        output DerDensityByPressure ddpT "Density derivative w.r.t. pressure";
      end density_derp_T;

      replaceable partial function density_derT_p
        "Return density derivative w.r.t. temperature at constant pressure"
        extends Modelica.Icons.Function;
        input ThermodynamicState state "Thermodynamic state record";
        output DerDensityByTemperature ddTp
          "Density derivative w.r.t. temperature";
      end density_derT_p;

      replaceable partial function density_derX
        "Return density derivative w.r.t. mass fraction"
        extends Modelica.Icons.Function;
        input ThermodynamicState state "Thermodynamic state record";
        output Density[nX] dddX "Derivative of density w.r.t. mass fraction";
      end density_derX;

      replaceable partial function molarMass
        "Return the molar mass of the medium"
        extends Modelica.Icons.Function;
        input ThermodynamicState state "Thermodynamic state record";
        output MolarMass MM "Mixture molar mass";
      end molarMass;

      replaceable function specificEnthalpy_pTX
        "Return specific enthalpy from p, T, and X or Xi"
        extends Modelica.Icons.Function;
        input AbsolutePressure p "Pressure";
        input Temperature T "Temperature";
        input MassFraction X[:]=reference_X "Mass fractions";
        output SpecificEnthalpy h "Specific enthalpy";
      algorithm
        h := specificEnthalpy(setState_pTX(
                p,
                T,
                X));
        annotation (inverse(T=temperature_phX(
                      p,
                      h,
                      X)));
      end specificEnthalpy_pTX;

      replaceable function specificEntropy_pTX
        "Return specific enthalpy from p, T, and X or Xi"
        extends Modelica.Icons.Function;
        input AbsolutePressure p "Pressure";
        input Temperature T "Temperature";
        input MassFraction X[:]=reference_X "Mass fractions";
        output SpecificEntropy s "Specific entropy";
      algorithm
        s := specificEntropy(setState_pTX(
                p,
                T,
                X));

        annotation (inverse(T=temperature_psX(
                      p,
                      s,
                      X)));
      end specificEntropy_pTX;

      replaceable function density_pTX "Return density from p, T, and X or Xi"
        extends Modelica.Icons.Function;
        input AbsolutePressure p "Pressure";
        input Temperature T "Temperature";
        input MassFraction X[:] "Mass fractions";
        output Density d "Density";
      algorithm
        d := density(setState_pTX(
                p,
                T,
                X));
      end density_pTX;

      replaceable function temperature_phX
        "Return temperature from p, h, and X or Xi"
        extends Modelica.Icons.Function;
        input AbsolutePressure p "Pressure";
        input SpecificEnthalpy h "Specific enthalpy";
        input MassFraction X[:]=reference_X "Mass fractions";
        output Temperature T "Temperature";
      algorithm
        T := temperature(setState_phX(
                p,
                h,
                X));
      end temperature_phX;

      replaceable function density_phX "Return density from p, h, and X or Xi"
        extends Modelica.Icons.Function;
        input AbsolutePressure p "Pressure";
        input SpecificEnthalpy h "Specific enthalpy";
        input MassFraction X[:]=reference_X "Mass fractions";
        output Density d "Density";
      algorithm
        d := density(setState_phX(
                p,
                h,
                X));
      end density_phX;

      replaceable function temperature_psX
        "Return temperature from p,s, and X or Xi"
        extends Modelica.Icons.Function;
        input AbsolutePressure p "Pressure";
        input SpecificEntropy s "Specific entropy";
        input MassFraction X[:]=reference_X "Mass fractions";
        output Temperature T "Temperature";
      algorithm
        T := temperature(setState_psX(
                p,
                s,
                X));
        annotation (inverse(s=specificEntropy_pTX(
                      p,
                      T,
                      X)));
      end temperature_psX;

      replaceable function density_psX "Return density from p, s, and X or Xi"
        extends Modelica.Icons.Function;
        input AbsolutePressure p "Pressure";
        input SpecificEntropy s "Specific entropy";
        input MassFraction X[:]=reference_X "Mass fractions";
        output Density d "Density";
      algorithm
        d := density(setState_psX(
                p,
                s,
                X));
      end density_psX;

      replaceable function specificEnthalpy_psX
        "Return specific enthalpy from p, s, and X or Xi"
        extends Modelica.Icons.Function;
        input AbsolutePressure p "Pressure";
        input SpecificEntropy s "Specific entropy";
        input MassFraction X[:]=reference_X "Mass fractions";
        output SpecificEnthalpy h "Specific enthalpy";
      algorithm
        h := specificEnthalpy(setState_psX(
                p,
                s,
                X));
      end specificEnthalpy_psX;

      type MassFlowRate = SI.MassFlowRate (
          quantity="MassFlowRate." + mediumName,
          min=-1.0e5,
          max=1.e5) "Type for mass flow rate with medium specific attributes";

      // Only for backwards compatibility to version 3.2 (
      // (do not use these definitions in new models, but use Modelica.Media.Interfaces.Choices instead)
      package Choices = Modelica.Media.Interfaces.Choices annotation (obsolete=
            "Use Modelica.Media.Interfaces.Choices");

      annotation (Documentation(info="<html>
<p>
<b>PartialMedium</b> is a package and contains all <b>declarations</b> for
a medium. This means that constants, models, and functions
are defined that every medium is supposed to support
(some of them are optional). A medium package
inherits from <b>PartialMedium</b> and provides the
equations for the medium. The details of this package
are described in
<a href=\"modelica://Modelica.Media.UsersGuide\">Modelica.Media.UsersGuide</a>.
</p>
</html>",   revisions="<html>

</html>"));
    end PartialMedium;

    partial package PartialMixtureMedium
      "Base class for pure substances of several chemical substances"
      extends PartialMedium(redeclare replaceable record FluidConstants =
            Modelica.Media.Interfaces.Types.IdealGas.FluidConstants);

      redeclare replaceable record extends ThermodynamicState
        "Thermodynamic state variables"
        AbsolutePressure p "Absolute pressure of medium";
        Temperature T "Temperature of medium";
        MassFraction[nX] X(start=reference_X)
          "Mass fractions (= (component mass)/total mass  m_i/m)";
      end ThermodynamicState;

      constant FluidConstants[nS] fluidConstants "Constant data for the fluid";

      replaceable function gasConstant
        "Return the gas constant of the mixture (also for liquids)"
        extends Modelica.Icons.Function;
        input ThermodynamicState state "Thermodynamic state";
        output SI.SpecificHeatCapacity R "Mixture gas constant";
      end gasConstant;

      function moleToMassFractions "Return mass fractions X from mole fractions"
        extends Modelica.Icons.Function;
        input SI.MoleFraction moleFractions[:] "Mole fractions of mixture";
        input MolarMass[:] MMX "Molar masses of components";
        output SI.MassFraction X[size(moleFractions, 1)]
          "Mass fractions of gas mixture";
    protected
        MolarMass Mmix=moleFractions*MMX "Molar mass of mixture";
      algorithm
        for i in 1:size(moleFractions, 1) loop
          X[i] := moleFractions[i]*MMX[i]/Mmix;
        end for;
        annotation (smoothOrder=5);
      end moleToMassFractions;

      function massToMoleFractions "Return mole fractions from mass fractions X"
        extends Modelica.Icons.Function;
        input SI.MassFraction X[:] "Mass fractions of mixture";
        input SI.MolarMass[:] MMX "Molar masses of components";
        output SI.MoleFraction moleFractions[size(X, 1)]
          "Mole fractions of gas mixture";
    protected
        Real invMMX[size(X, 1)] "Inverses of molar weights";
        SI.MolarMass Mmix "Molar mass of mixture";
      algorithm
        for i in 1:size(X, 1) loop
          invMMX[i] := 1/MMX[i];
        end for;
        Mmix := 1/(X*invMMX);
        for i in 1:size(X, 1) loop
          moleFractions[i] := Mmix*X[i]/MMX[i];
        end for;
        annotation (smoothOrder=5);
      end massToMoleFractions;

    end PartialMixtureMedium;

    partial package PartialCondensingGases
      "Base class for mixtures of condensing and non-condensing gases"
      extends PartialMixtureMedium(ThermoStates=Modelica.Media.Interfaces.Choices.IndependentVariables.pTX);

      replaceable partial function saturationPressure
        "Return saturation pressure of condensing fluid"
        extends Modelica.Icons.Function;
        input Temperature Tsat "Saturation temperature";
        output AbsolutePressure psat "Saturation pressure";
      end saturationPressure;

      replaceable partial function enthalpyOfVaporization
        "Return vaporization enthalpy of condensing fluid"
        extends Modelica.Icons.Function;
        input Temperature T "Temperature";
        output SpecificEnthalpy r0 "Vaporization enthalpy";
      end enthalpyOfVaporization;

      replaceable partial function enthalpyOfLiquid
        "Return liquid enthalpy of condensing fluid"
        extends Modelica.Icons.Function;
        input Temperature T "Temperature";
        output SpecificEnthalpy h "Liquid enthalpy";
      end enthalpyOfLiquid;

      replaceable partial function enthalpyOfGas
        "Return enthalpy of non-condensing gas mixture"
        extends Modelica.Icons.Function;
        input Temperature T "Temperature";
        input MassFraction[:] X "Vector of mass fractions";
        output SpecificEnthalpy h "Specific enthalpy";
      end enthalpyOfGas;

      replaceable partial function enthalpyOfCondensingGas
        "Return enthalpy of condensing gas (most often steam)"
        extends Modelica.Icons.Function;
        input Temperature T "Temperature";
        output SpecificEnthalpy h "Specific enthalpy";
      end enthalpyOfCondensingGas;

      replaceable partial function enthalpyOfNonCondensingGas
        "Return enthalpy of the non-condensing species"
        extends Modelica.Icons.Function;
        input Temperature T "Temperature";
        output SpecificEnthalpy h "Specific enthalpy";
      end enthalpyOfNonCondensingGas;
    end PartialCondensingGases;

    package Choices "Types, constants to define menu choices"
      extends Modelica.Icons.Package;

      type IndependentVariables = enumeration(
        T   "Temperature",
        pT   "Pressure, Temperature",
        ph   "Pressure, Specific Enthalpy",
        phX   "Pressure, Specific Enthalpy, Mass Fraction",
        pTX   "Pressure, Temperature, Mass Fractions",
        dTX   "Density, Temperature, Mass Fractions")
        "Enumeration defining the independent variables of a medium";
      annotation (Documentation(info="<html>
<p>
Enumerations and data types for all types of fluids
</p>

<p>
Note: Reference enthalpy might have to be extended with enthalpy of formation.
</p>
</html>"));
    end Choices;

    package Types "Types to be used in fluid models"
      extends Modelica.Icons.Package;

      type AbsolutePressure = SI.AbsolutePressure (
          min=0,
          max=1.e8,
          nominal=1.e5,
          start=1.e5)
        "Type for absolute pressure with medium specific attributes";

      type Density = SI.Density (
          min=0,
          max=1.e5,
          nominal=1,
          start=1) "Type for density with medium specific attributes";

      type DynamicViscosity = SI.DynamicViscosity (
          min=0,
          max=1.e8,
          nominal=1.e-3,
          start=1.e-3)
        "Type for dynamic viscosity with medium specific attributes";

      type MassFraction = Real (
          quantity="MassFraction",
          final unit="kg/kg",
          min=0,
          max=1,
          nominal=0.1) "Type for mass fraction with medium specific attributes";

      type MolarMass = SI.MolarMass (
          min=0.001,
          max=0.25,
          nominal=0.032) "Type for molar mass with medium specific attributes";

      type MolarVolume = SI.MolarVolume (
          min=1e-6,
          max=1.0e6,
          nominal=1.0) "Type for molar volume with medium specific attributes";

      type IsentropicExponent = SI.RatioOfSpecificHeatCapacities (
          min=1,
          max=500000,
          nominal=1.2,
          start=1.2)
        "Type for isentropic exponent with medium specific attributes";

      type SpecificEnergy = SI.SpecificEnergy (
          min=-1.0e8,
          max=1.e8,
          nominal=1.e6)
        "Type for specific energy with medium specific attributes";

      type SpecificInternalEnergy = SpecificEnergy
        "Type for specific internal energy with medium specific attributes";

      type SpecificEnthalpy = SI.SpecificEnthalpy (
          min=-1.0e10,
          max=1.e10,
          nominal=1.e6)
        "Type for specific enthalpy with medium specific attributes";

      type SpecificEntropy = SI.SpecificEntropy (
          min=-1.e7,
          max=1.e7,
          nominal=1.e3)
        "Type for specific entropy with medium specific attributes";

      type SpecificHeatCapacity = SI.SpecificHeatCapacity (
          min=0,
          max=1.e7,
          nominal=1.e3,
          start=1.e3)
        "Type for specific heat capacity with medium specific attributes";

      type Temperature = SI.Temperature (
          min=1,
          max=1.e4,
          nominal=300,
          start=288.15) "Type for temperature with medium specific attributes";

      type ThermalConductivity = SI.ThermalConductivity (
          min=0,
          max=500,
          nominal=1,
          start=1)
        "Type for thermal conductivity with medium specific attributes";

      type PrandtlNumber = SI.PrandtlNumber (
          min=1e-3,
          max=1e5,
          nominal=1.0) "Type for Prandtl number with medium specific attributes";

      type VelocityOfSound = SI.Velocity (
          min=0,
          max=1.e5,
          nominal=1000,
          start=1000)
        "Type for velocity of sound with medium specific attributes";

      type ExtraProperty = Real (min=0.0, start=1.0)
        "Type for unspecified, mass-specific property transported by flow";

      type IsobaricExpansionCoefficient = Real (
          min=0,
          max=1.0e8,
          unit="1/K")
        "Type for isobaric expansion coefficient with medium specific attributes";

      type DipoleMoment = Real (
          min=0.0,
          max=2.0,
          unit="debye",
          quantity="ElectricDipoleMoment")
        "Type for dipole moment with medium specific attributes";

      type DerDensityByPressure = SI.DerDensityByPressure
        "Type for partial derivative of density with respect to pressure with medium specific attributes";

      type DerDensityByEnthalpy = SI.DerDensityByEnthalpy
        "Type for partial derivative of density with respect to enthalpy with medium specific attributes";

      type DerDensityByTemperature = SI.DerDensityByTemperature
        "Type for partial derivative of density with respect to temperature with medium specific attributes";

      package Basic
      "The most basic version of a record used in several degrees of detail"
        extends Icons.Package;

        record FluidConstants
          "Critical, triple, molecular and other standard data of fluid"
          extends Modelica.Icons.Record;
          String iupacName
            "Complete IUPAC name (or common name, if non-existent)";
          String casRegistryNumber
            "Chemical abstracts sequencing number (if it exists)";
          String chemicalFormula
            "Chemical formula, (brutto, nomenclature according to Hill";
          String structureFormula "Chemical structure formula";
          MolarMass molarMass "Molar mass";
        end FluidConstants;
      end Basic;

      package IdealGas
      "The ideal gas version of a record used in several degrees of detail"
        extends Icons.Package;

        record FluidConstants "Extended fluid constants"
          extends Modelica.Media.Interfaces.Types.Basic.FluidConstants;
          Temperature criticalTemperature "Critical temperature";
          AbsolutePressure criticalPressure "Critical pressure";
          MolarVolume criticalMolarVolume "Critical molar Volume";
          Real acentricFactor "Pitzer acentric factor";
          //   Temperature triplePointTemperature "Triple point temperature";
          //   AbsolutePressure triplePointPressure "Triple point pressure";
          Temperature meltingPoint "Melting point at 101325 Pa";
          Temperature normalBoilingPoint "Normal boiling point (at 101325 Pa)";
          DipoleMoment dipoleMoment
            "Dipole moment of molecule in Debye (1 debye = 3.33564e10-30 C.m)";
          Boolean hasIdealGasHeatCapacity=false
            "True if ideal gas heat capacity is available";
          Boolean hasCriticalData=false "True if critical data are known";
          Boolean hasDipoleMoment=false "True if a dipole moment known";
          Boolean hasFundamentalEquation=false "True if a fundamental equation";
          Boolean hasLiquidHeatCapacity=false
            "True if liquid heat capacity is available";
          Boolean hasSolidHeatCapacity=false
            "True if solid heat capacity is available";
          Boolean hasAccurateViscosityData=false
            "True if accurate data for a viscosity function is available";
          Boolean hasAccurateConductivityData=false
            "True if accurate data for thermal conductivity is available";
          Boolean hasVapourPressureCurve=false
            "True if vapour pressure data, e.g., Antoine coefficients are known";
          Boolean hasAcentricFactor=false
            "True if Pitzer accentric factor is known";
          SpecificEnthalpy HCRIT0=0.0
            "Critical specific enthalpy of the fundamental equation";
          SpecificEntropy SCRIT0=0.0
            "Critical specific entropy of the fundamental equation";
          SpecificEnthalpy deltah=0.0
            "Difference between specific enthalpy model (h_m) and f.eq. (h_f) (h_m - h_f)";
          SpecificEntropy deltas=0.0
            "Difference between specific enthalpy model (s_m) and f.eq. (s_f) (s_m - s_f)";
        end FluidConstants;
      end IdealGas;
    end Types;
    annotation (Documentation(info="<html>
<p>
This package provides basic interfaces definitions of media models for different
kind of media.
</p>
</html>"));
  end Interfaces;

  package Common
    "Data structures and fundamental functions for fluid properties"
    extends Modelica.Icons.Package;

    function smoothStep
      "Approximation of a general step, such that the characteristic is continuous and differentiable"
      extends Modelica.Icons.Function;
      input Real x "Abscissa value";
      input Real y1 "Ordinate value for x > 0";
      input Real y2 "Ordinate value for x < 0";
      input Real x_small(min=0) = 1e-5
        "Approximation of step for -x_small <= x <= x_small; x_small > 0 required";
      output Real y "Ordinate value to approximate y = if x > 0 then y1 else y2";
    algorithm
      y := smooth(1, if x > x_small then y1 else if x < -x_small then y2 else if
        abs(x_small) > 0 then (x/x_small)*((x/x_small)^2 - 3)*(y2 - y1)/4 + (y1
         + y2)/2 else (y1 + y2)/2);

      annotation (
        Inline=true,
        smoothOrder=1,
        Documentation(revisions="<html>
<ul>
<li><i>April 29, 2008</i>
    by <a href=\"mailto:Martin.Otter@DLR.de\">Martin Otter</a>:<br>
    Designed and implemented.</li>
<li><i>August 12, 2008</i>
    by <a href=\"mailto:Michael.Sielemann@dlr.de\">Michael Sielemann</a>:<br>
    Minor modification to cover the limit case <code>x_small -> 0</code> without division by zero.</li>
</ul>
</html>",   info="<html>
<p>
This function is used to approximate the equation
</p>
<pre>
    y = <b>if</b> x &gt; 0 <b>then</b> y1 <b>else</b> y2;
</pre>

<p>
by a smooth characteristic, so that the expression is continuous and differentiable:
</p>

<pre>
   y = <b>smooth</b>(1, <b>if</b> x &gt;  x_small <b>then</b> y1 <b>else</b>
                 <b>if</b> x &lt; -x_small <b>then</b> y2 <b>else</b> f(y1, y2));
</pre>

<p>
In the region -x_small &lt; x &lt; x_small a 2nd order polynomial is used
for a smooth transition from y1 to y2.
</p>

<p>
If <b>mass fractions</b> X[:] are approximated with this function then this can be performed
for all <b>nX</b> mass fractions, instead of applying it for nX-1 mass fractions and computing
the last one by the mass fraction constraint sum(X)=1. The reason is that the approximating function has the
property that sum(X) = 1, provided sum(X_a) = sum(X_b) = 1
(and y1=X_a[i], y2=X_b[i]).
This can be shown by evaluating the approximating function in the abs(x) &lt; x_small
region (otherwise X is either X_a or X_b):
</p>

<pre>
    X[1]  = smoothStep(x, X_a[1] , X_b[1] , x_small);
    X[2]  = smoothStep(x, X_a[2] , X_b[2] , x_small);
       ...
    X[nX] = smoothStep(x, X_a[nX], X_b[nX], x_small);
</pre>

<p>
or
</p>

<pre>
    X[1]  = c*(X_a[1]  - X_b[1])  + (X_a[1]  + X_b[1])/2
    X[2]  = c*(X_a[2]  - X_b[2])  + (X_a[2]  + X_b[2])/2;
       ...
    X[nX] = c*(X_a[nX] - X_b[nX]) + (X_a[nX] + X_b[nX])/2;
    c     = (x/x_small)*((x/x_small)^2 - 3)/4
</pre>

<p>
Summing all mass fractions together results in
</p>

<pre>
    sum(X) = c*(sum(X_a) - sum(X_b)) + (sum(X_a) + sum(X_b))/2
           = c*(1 - 1) + (1 + 1)/2
           = 1
</pre>
</html>"));
    end smoothStep;

    package OneNonLinearEquation
      "Determine solution of a non-linear algebraic equation in one unknown without derivatives in a reliable and efficient way"
      extends Modelica.Icons.Package;

      replaceable record f_nonlinear_Data
        "Data specific for function f_nonlinear"
        extends Modelica.Icons.Record;
      end f_nonlinear_Data;

      replaceable partial function f_nonlinear
        "Nonlinear algebraic equation in one unknown: y = f_nonlinear(x,p,X)"
        extends Modelica.Icons.Function;
        input Real x "Independent variable of function";
        input Real p=0.0 "Disregarded variables (here always used for pressure)";
        input Real[:] X=fill(0, 0)
          "Disregarded variables (her always used for composition)";
        input f_nonlinear_Data f_nonlinear_data
          "Additional data for the function";
        output Real y "= f_nonlinear(x)";
        // annotation(derivative(zeroDerivative=y)); // this must hold for all replaced functions
      end f_nonlinear;

      replaceable function solve
        "Solve f_nonlinear(x_zero)=y_zero; f_nonlinear(x_min) - y_zero and f_nonlinear(x_max)-y_zero must have different sign"
        import Modelica.Utilities.Streams.error;
        extends Modelica.Icons.Function;
        input Real y_zero
          "Determine x_zero, such that f_nonlinear(x_zero) = y_zero";
        input Real x_min "Minimum value of x";
        input Real x_max "Maximum value of x";
        input Real pressure=0.0
          "Disregarded variables (here always used for pressure)";
        input Real[:] X=fill(0, 0)
          "Disregarded variables (here always used for composition)";
        input f_nonlinear_Data f_nonlinear_data
          "Additional data for function f_nonlinear";
        input Real x_tol=100*Modelica.Constants.eps
          "Relative tolerance of the result";
        output Real x_zero "f_nonlinear(x_zero) = y_zero";
    protected
        constant Real eps=Modelica.Constants.eps "Machine epsilon";
        constant Real x_eps=1e-10
          "Slight modification of x_min, x_max, since x_min, x_max are usually exactly at the borders T_min/h_min and then small numeric noise may make the interval invalid";
        Real x_min2=x_min - x_eps;
        Real x_max2=x_max + x_eps;
        Real a=x_min2 "Current best minimum interval value";
        Real b=x_max2 "Current best maximum interval value";
        Real c "Intermediate point a <= c <= b";
        Real d;
        Real e "b - a";
        Real m;
        Real s;
        Real p;
        Real q;
        Real r;
        Real tol;
        Real fa "= f_nonlinear(a) - y_zero";
        Real fb "= f_nonlinear(b) - y_zero";
        Real fc;
        Boolean found=false;
      algorithm
        // Check that f(x_min) and f(x_max) have different sign
        fa := f_nonlinear(
                x_min2,
                pressure,
                X,
                f_nonlinear_data) - y_zero;
        fb := f_nonlinear(
                x_max2,
                pressure,
                X,
                f_nonlinear_data) - y_zero;
        fc := fb;
        if fa > 0.0 and fb > 0.0 or fa < 0.0 and fb < 0.0 then
          error(
            "The arguments x_min and x_max to OneNonLinearEquation.solve(..)\n"
             + "do not bracket the root of the single non-linear equation:\n" +
            "  x_min  = " + String(x_min2) + "\n" + "  x_max  = " + String(x_max2)
             + "\n" + "  y_zero = " + String(y_zero) + "\n" +
            "  fa = f(x_min) - y_zero = " + String(fa) + "\n" +
            "  fb = f(x_max) - y_zero = " + String(fb) + "\n" +
            "fa and fb must have opposite sign which is not the case");
        end if;

        // Initialize variables
        c := a;
        fc := fa;
        e := b - a;
        d := e;

        // Search loop
        while not found loop
          if abs(fc) < abs(fb) then
            a := b;
            b := c;
            c := a;
            fa := fb;
            fb := fc;
            fc := fa;
          end if;

          tol := 2*eps*abs(b) + x_tol;
          m := (c - b)/2;

          if abs(m) <= tol or fb == 0.0 then
            // root found (interval is small enough)
            found := true;
            x_zero := b;
          else
            // Determine if a bisection is needed
            if abs(e) < tol or abs(fa) <= abs(fb) then
              e := m;
              d := e;
            else
              s := fb/fa;
              if a == c then
                // linear interpolation
                p := 2*m*s;
                q := 1 - s;
              else
                // inverse quadratic interpolation
                q := fa/fc;
                r := fb/fc;
                p := s*(2*m*q*(q - r) - (b - a)*(r - 1));
                q := (q - 1)*(r - 1)*(s - 1);
              end if;

              if p > 0 then
                q := -q;
              else
                p := -p;
              end if;

              s := e;
              e := d;
              if 2*p < 3*m*q - abs(tol*q) and p < abs(0.5*s*q) then
                // interpolation successful
                d := p/q;
              else
                // use bi-section
                e := m;
                d := e;
              end if;
            end if;

            // Best guess value is defined as "a"
            a := b;
            fa := fb;
            b := b + (if abs(d) > tol then d else if m > 0 then tol else -tol);
            fb := f_nonlinear(
                    b,
                    pressure,
                    X,
                    f_nonlinear_data) - y_zero;

            if fb > 0 and fc > 0 or fb < 0 and fc < 0 then
              // initialize variables
              c := a;
              fc := fa;
              e := b - a;
              d := e;
            end if;
          end if;
        end while;
      end solve;

      annotation (Documentation(info="<html>
<p>
This function should currently only be used in Modelica.Media,
since it might be replaced in the future by another strategy,
where the tool is responsible for the solution of the non-linear
equation.
</p>

<p>
This library determines the solution of one non-linear algebraic equation \"y=f(x)\"
in one unknown \"x\" in a reliable way. As input, the desired value y of the
non-linear function has to be given, as well as an interval x_min, x_max that
contains the solution, i.e., \"f(x_min) - y\" and \"f(x_max) - y\" must
have a different sign. If possible, a smaller interval is computed by
inverse quadratic interpolation (interpolating with a quadratic polynomial
through the last 3 points and computing the zero). If this fails,
bisection is used, which always reduces the interval by a factor of 2.
The inverse quadratic interpolation method has superlinear convergence.
This is roughly the same convergence rate as a globally convergent Newton
method, but without the need to compute derivatives of the non-linear
function. The solver function is a direct mapping of the Algol 60 procedure
\"zero\" to Modelica, from:
</p>

<dl>
<dt> Brent R.P.:</dt>
<dd> <b>Algorithms for Minimization without derivatives</b>.
     Prentice Hall, 1973, pp. 58-59.</dd>
</dl>

<p>
Due to current limitations of the
Modelica language (not possible to pass a function reference to a function),
the construction to use this solver on a user-defined function is a bit
complicated (this method is from Hans Olsson, Dassault Syst&egrave;mes AB). A user has to
provide a package in the following way:
</p>

<pre>
  <b>package</b> MyNonLinearSolver
    <b>extends</b> OneNonLinearEquation;

    <b>redeclare record extends</b> Data
      // Define data to be passed to user function
      ...
    <b>end</b> Data;

    <b>redeclare function extends</b> f_nonlinear
    <b>algorithm</b>
       // Compute the non-linear equation: y = f(x, Data)
    <b>end</b> f_nonlinear;

    // Dummy definition that has to be present for current Dymola
    <b>redeclare function extends</b> solve
    <b>end</b> solve;
  <b>end</b> MyNonLinearSolver;

  x_zero = MyNonLinearSolver.solve(y_zero, x_min, x_max, data=data);
</pre>
</html>"));
    end OneNonLinearEquation;
    annotation (Documentation(info="<html><h4>Package description</h4>
      <p>Package Modelica.Media.Common provides records and functions shared by many of the property sub-packages.
      High accuracy fluid property models share a lot of common structure, even if the actual models are different.
      Common data structures and computations shared by these property models are collected in this library.
   </p>

</html>",   revisions="<html>
      <ul>
      <li>First implemented: <i>July, 2000</i>
      by Hubertus Tummescheit
      for the ThermoFluid Library with help from Jonas Eborn and Falko Jens Wagner
      </li>
      <li>Code reorganization, enhanced documentation, additional functions: <i>December, 2002</i>
      by Hubertus Tummescheit and move to Modelica
                            properties library.</li>
      <li>Inclusion into Modelica.Media: September 2003 </li>
      </ul>

      <address>Author: Hubertus Tummescheit, <br>
      Lund University<br>
      Department of Automatic Control<br>
      Box 118, 22100 Lund, Sweden<br>
      email: hubertus@control.lth.se
      </address>
</html>"));
  end Common;

    package IdealGases
    "Data and models of ideal gases (single, fixed and dynamic mixtures) from NASA source"
      extends Modelica.Icons.VariantsPackage;

      package Common "Common packages and data for the ideal gas models"
      extends Modelica.Icons.Package;

      record DataRecord
        "Coefficient data record for properties of ideal gases based on NASA source"
        extends Modelica.Icons.Record;
        String name "Name of ideal gas";
        SI.MolarMass MM "Molar mass";
        SI.SpecificEnthalpy Hf "Enthalpy of formation at 298.15K";
        SI.SpecificEnthalpy H0 "H0(298.15K) - H0(0K)";
        SI.Temperature Tlimit "Temperature limit between low and high data sets";
        Real alow[7] "Low temperature coefficients a";
        Real blow[2] "Low temperature constants b";
        Real ahigh[7] "High temperature coefficients a";
        Real bhigh[2] "High temperature constants b";
        SI.SpecificHeatCapacity R "Gas constant";
        annotation (Documentation(info="<html>
<p>
This data record contains the coefficients for the
ideal gas equations according to:
</p>
<blockquote>
  <p>McBride B.J., Zehe M.J., and Gordon S. (2002): <b>NASA Glenn Coefficients
  for Calculating Thermodynamic Properties of Individual Species</b>. NASA
  report TP-2002-211556</p>
</blockquote>
<p>
The equations have the following structure:
</p>
<IMG src=\"modelica://Modelica/Resources/Images/Media/IdealGases/singleEquations.png\">
<p>
The polynomials for h(T) and s0(T) are derived via integration from the one for cp(T)  and contain the integration constants b1, b2 that define the reference specific enthalpy and entropy. For entropy differences the reference pressure p0 is arbitrary, but not for absolute entropies. It is chosen as 1 standard atmosphere (101325 Pa).
</p>
<p>
For most gases, the region of validity is from 200 K to 6000 K.
The equations are split into two regions that are separated
by Tlimit (usually 1000 K). In both regions the gas is described
by the data above. The two branches are continuous and in most
gases also differentiable at Tlimit.
</p>
</html>"));
      end DataRecord;

        package FluidData "Critical data, dipole moments and related data"
          extends Modelica.Icons.Package;
          import Modelica.Media.Interfaces.PartialMixtureMedium;
          import Modelica.Media.IdealGases.Common.SingleGasesData;

          constant Modelica.Media.Interfaces.Types.IdealGas.FluidConstants N2(
                               chemicalFormula =        "N2",
                               iupacName =              "unknown",
                               structureFormula =       "unknown",
                               casRegistryNumber =      "7727-37-9",
                               meltingPoint =            63.15,
                               normalBoilingPoint =      77.35,
                               criticalTemperature =    126.20,
                               criticalPressure =        33.98e5,
                               criticalMolarVolume =     90.10e-6,
                               acentricFactor =           0.037,
                               dipoleMoment =             0.0,
                               molarMass =              SingleGasesData.N2.MM,
                               hasDipoleMoment =       true,
                               hasIdealGasHeatCapacity=true,
                               hasCriticalData =       true,
                               hasAcentricFactor =     true);

          constant Modelica.Media.Interfaces.Types.IdealGas.FluidConstants H2O(
                               chemicalFormula =        "H2O",
                               iupacName =              "oxidane",
                               structureFormula =       "H2O",
                               casRegistryNumber =      "7732-18-5",
                               meltingPoint =           273.15,
                               normalBoilingPoint =     373.124,
                               criticalTemperature =    647.096,
                               criticalPressure =       220.64e5,
                               criticalMolarVolume =     55.95e-6,
                               acentricFactor =           0.344,
                               dipoleMoment =             1.8,
                               molarMass =              SingleGasesData.H2O.MM,
                               hasDipoleMoment =       true,
                               hasIdealGasHeatCapacity=true,
                               hasCriticalData =       true,
                               hasAcentricFactor =     true);
          annotation (Documentation(info="<html>
<p>
This package contains FluidConstants data records for the following 37 gases
(see also the description in
<a href=\"modelica://Modelica.Media.IdealGases\">Modelica.Media.IdealGases</a>):
</p>
<pre>
Argon             Methane          Methanol       Carbon Monoxide  Carbon Dioxide
Acetylene         Ethylene         Ethanol        Ethane           Propylene
Propane           1-Propanol       1-Butene       N-Butane         1-Pentene
N-Pentane         Benzene          1-Hexene       N-Hexane         1-Heptane
N-Heptane         Ethylbenzene     N-Octane       Chlorine         Fluorine
Hydrogen          Steam            Helium         Ammonia          Nitric Oxide
Nitrogen Dioxide  Nitrogen         Nitrous        Oxide            Neon Oxygen
Sulfur Dioxide    Sulfur Trioxide
</pre>

</html>"));
        end FluidData;

        package SingleGasesData
        "Ideal gas data based on the NASA Glenn coefficients"
          extends Modelica.Icons.Package;

          constant IdealGases.Common.DataRecord Air(
            name="Air",
            MM=0.0289651159,
            Hf=-4333.833858403446,
            H0=298609.6803431054,
            Tlimit=1000,
            alow={10099.5016,-196.827561,5.00915511,-0.00576101373,1.06685993e-005,-7.94029797e-009,
                2.18523191e-012},
            blow={-176.796731,-3.921504225},
            ahigh={241521.443,-1257.8746,5.14455867,-0.000213854179,7.06522784e-008,-1.07148349e-011,
                6.57780015e-016},
            bhigh={6462.26319,-8.147411905},
            R=287.0512249529787);

          constant IdealGases.Common.DataRecord H2O(
            name="H2O",
            MM=0.01801528,
            Hf=-13423382.81725291,
            H0=549760.6476280135,
            Tlimit=1000,
            alow={-39479.6083,575.573102,0.931782653,0.00722271286,-7.34255737e-006,
                4.95504349e-009,-1.336933246e-012},
            blow={-33039.7431,17.24205775},
            ahigh={1034972.096,-2412.698562,4.64611078,0.002291998307,-6.836830479999999e-007,
                9.426468930000001e-011,-4.82238053e-015},
            bhigh={-13842.86509,-7.97814851},
            R=461.5233290850878);

          constant IdealGases.Common.DataRecord N2(
            name="N2",
            MM=0.0280134,
            Hf=0,
            H0=309498.4543111511,
            Tlimit=1000,
            alow={22103.71497,-381.846182,6.08273836,-0.00853091441,1.384646189e-005,-9.62579362e-009,
                2.519705809e-012},
            blow={710.846086,-10.76003744},
            ahigh={587712.406,-2239.249073,6.06694922,-0.00061396855,1.491806679e-007,-1.923105485e-011,
                1.061954386e-015},
            bhigh={12832.10415,-15.86640027},
            R=296.8033869505308);
          annotation ( Documentation(info="<html>
<p>This package contains ideal gas models for the 1241 ideal gases from</p>
<blockquote>
  <p>McBride B.J., Zehe M.J., and Gordon S. (2002): <b>NASA Glenn Coefficients
  for Calculating Thermodynamic Properties of Individual Species</b>. NASA
  report TP-2002-211556</p>
</blockquote>

<pre>
 Ag        BaOH+           C2H4O_ethylen_o DF      In2I4    Nb      ScO2
 Ag+       Ba_OH_2         CH3CHO_ethanal  DOCl    In2I6    Nb+     Sc2O
 Ag-       BaS             CH3COOH         DO2     In2O     Nb-     Sc2O2
 Air       Ba2             OHCH2COOH       DO2-    K        NbCl5   Si
 Al        Be              C2H5            D2      K+       NbO     Si+
 Al+       Be+             C2H5Br          D2+     K-       NbOCl3  Si-
 Al-       Be++            C2H6            D2-     KAlF4    NbO2    SiBr
 AlBr      BeBr            CH3N2CH3        D2O     KBO2     Ne      SiBr2
 AlBr2     BeBr2           C2H5OH          D2O2    KBr      Ne+     SiBr3
 AlBr3     BeCl            CH3OCH3         D2S     KCN      Ni      SiBr4
 AlC       BeCl2           CH3O2CH3        e-      KCl      Ni+     SiC
 AlC2      BeF             CCN             F       KF       Ni-     SiC2
 AlCl      BeF2            CNC             F+      KH       NiCl    SiCl
 AlCl+     BeH             OCCN            F-      KI       NiCl2   SiCl2
 AlCl2     BeH+            C2N2            FCN     Kli      NiO     SiCl3
 AlCl3     BeH2            C2O             FCO     KNO2     NiS     SiCl4
 AlF       BeI             C3              FO      KNO3     O       SiF
 AlF+      BeI2            C3H3_1_propynl  FO2_FOO KNa      O+      SiFCl
 AlFCl     BeN             C3H3_2_propynl  FO2_OFO KO       O-      SiF2
 AlFCl2    BeO             C3H4_allene     F2      KOH      OD      SiF3
 AlF2      BeOH            C3H4_propyne    F2O     K2       OD-     SiF4
 AlF2-     BeOH+           C3H4_cyclo      F2O2    K2+      OH      SiH
 AlF2Cl    Be_OH_2         C3H5_allyl      FS2F    K2Br2    OH+     SiH+
 AlF3      BeS             C3H6_propylene  Fe      K2CO3    OH-     SiHBr3
 AlF4-     Be2             C3H6_cyclo      Fe+     K2C2N2   O2      SiHCl
 AlH       Be2Cl4          C3H6O_propylox  Fe_CO_5 K2Cl2    O2+     SiHCl3
 AlHCl     Be2F4           C3H6O_acetone   FeCl    K2F2     O2-     SiHF
 AlHCl2    Be2O            C3H6O_propanal  FeCl2   K2I2     O3      SiHF3
 AlHF      Be2OF2          C3H7_n_propyl   FeCl3   K2O      P       SiHI3
 AlHFCl    Be2O2           C3H7_i_propyl   FeO     K2O+     P+      SiH2
 AlHF2     Be3O3           C3H8            Fe_OH_2 K2O2     P-      SiH2Br2
 AlH2      Be4O4           C3H8O_1propanol Fe2Cl4  K2O2H2   PCl     SiH2Cl2
 AlH2Cl    Br              C3H8O_2propanol Fe2Cl6  K2SO4    PCl2    SiH2F2
 AlH2F     Br+             CNCOCN          Ga      Kr       PCl2-   SiH2I2
 AlH3      Br-             C3O2            Ga+     Kr+      PCl3    SiH3
 AlI       BrCl            C4              GaBr    li       PCl5    SiH3Br
 AlI2      BrF             C4H2_butadiyne  GaBr2   li+      PF      SiH3Cl
 AlI3      BrF3            C4H4_1_3-cyclo  GaBr3   li-      PF+     SiH3F
 AlN       BrF5            C4H6_butadiene  GaCl    liAlF4   PF-     SiH3I
 AlO       BrO             C4H6_1butyne    GaCl2   liBO2    PFCl    SiH4
 AlO+      OBrO            C4H6_2butyne    GaCl3   liBr     PFCl-   SiI
 AlO-      BrOO            C4H6_cyclo      GaF     liCl     PFCl2   SiI2
 AlOCl     BrO3            C4H8_1_butene   GaF2    liF      PFCl4   SiN
 AlOCl2    Br2             C4H8_cis2_buten GaF3    liH      PF2     SiO
 AlOF      BrBrO           C4H8_isobutene  GaH     liI      PF2-    SiO2
 AlOF2     BrOBr           C4H8_cyclo      GaI     liN      PF2Cl   SiS
 AlOF2-    C               C4H9_n_butyl    GaI2    liNO2    PF2Cl3  SiS2
 AlOH      C+              C4H9_i_butyl    GaI3    liNO3    PF3     Si2
 AlOHCl    C-              C4H9_s_butyl    GaO     liO      PF3Cl2  Si2C
 AlOHCl2   CBr             C4H9_t_butyl    GaOH    liOF     PF4Cl   Si2F6
 AlOHF     CBr2            C4H10_n_butane  Ga2Br2  liOH     PF5     Si2N
 AlOHF2    CBr3            C4H10_isobutane Ga2Br4  liON     PH      Si3
 AlO2      CBr4            C4N2            Ga2Br6  li2      PH2     Sn
 AlO2-     CCl             C5              Ga2Cl2  li2+     PH2-    Sn+
 Al_OH_2   CCl2            C5H6_1_3cyclo   Ga2Cl4  li2Br2   PH3     Sn-
 Al_OH_2Cl CCl2Br2         C5H8_cyclo      Ga2Cl6  li2F2    PN      SnBr
 Al_OH_2F  CCl3            C5H10_1_pentene Ga2F2   li2I2    PO      SnBr2
 Al_OH_3   CCl3Br          C5H10_cyclo     Ga2F4   li2O     PO-     SnBr3
 AlS       CCl4            C5H11_pentyl    Ga2F6   li2O+    POCl3   SnBr4
 AlS2      CF              C5H11_t_pentyl  Ga2I2   li2O2    POFCl2  SnCl
 Al2       CF+             C5H12_n_pentane Ga2I4   li2O2H2  POF2Cl  SnCl2
 Al2Br6    CFBr3           C5H12_i_pentane Ga2I6   li2SO4   POF3    SnCl3
 Al2C2     CFCl            CH3C_CH3_2CH3   Ga2O    li3+     PO2     SnCl4
 Al2Cl6    CFClBr2         C6D5_phenyl     Ge      li3Br3   PO2-    SnF
 Al2F6     CFCl2           C6D6            Ge+     li3Cl3   PS      SnF2
 Al2I6     CFCl2Br         C6H2            Ge-     li3F3    P2      SnF3
 Al2O      CFCl3           C6H5_phenyl     GeBr    li3I3    P2O3    SnF4
 Al2O+     CF2             C6H5O_phenoxy   GeBr2   Mg       P2O4    SnI
 Al2O2     CF2+            C6H6            GeBr3   Mg+      P2O5    SnI2
 Al2O2+    CF2Br2          C6H5OH_phenol   GeBr4   MgBr     P3      SnI3
 Al2O3     CF2Cl           C6H10_cyclo     GeCl    MgBr2    P3O6    SnI4
 Al2S      CF2ClBr         C6H12_1_hexene  GeCl2   MgCl     P4      SnO
 Al2S2     CF2Cl2          C6H12_cyclo     GeCl3   MgCl+    P4O6    SnO2
 Ar        CF3             C6H13_n_hexyl   GeCl4   MgCl2    P4O7    SnS
 Ar+       CF3+            C6H14_n_hexane  GeF     MgF      P4O8    SnS2
 B         CF3Br           C7H7_benzyl     GeF2    MgF+     P4O9    Sn2
 B+        CF3Cl           C7H8            GeF3    MgF2     P4O10   Sr
 B-        CF4             C7H8O_cresol_mx GeF4    MgF2+    Pb      Sr+
 BBr       CH+             C7H14_1_heptene GeH4    MgH      Pb+     SrBr
 BBr2      CHBr3           C7H15_n_heptyl  GeI     MgI      Pb-     SrBr2
 BBr3      CHCl            C7H16_n_heptane GeO     MgI2     PbBr    SrCl
 BC        CHClBr2         C7H16_2_methylh GeO2    MgN      PbBr2   SrCl+
 BC2       CHCl2           C8H8_styrene    GeS     MgO      PbBr3   SrCl2
 BCl       CHCl2Br         C8H10_ethylbenz GeS2    MgOH     PbBr4   SrF
 BCl+      CHCl3           C8H16_1_octene  Ge2     MgOH+    PbCl    SrF+
 BClOH     CHF             C8H17_n_octyl   H       Mg_OH_2  PbCl2   SrF2
 BCl_OH_2  CHFBr2          C8H18_n_octane  H+      MgS      PbCl3   SrH
 BCl2      CHFCl           C8H18_isooctane H-      Mg2      PbCl4   SrI
 BCl2+     CHFClBr         C9H19_n_nonyl   HAlO    Mg2F4    PbF     SrI2
 BCl2OH    CHFCl2          C10H8_naphthale HAlO2   Mn       PbF2    SrO
 BF        CHF2            C10H21_n_decyl  HBO     Mn+      PbF3    SrOH
 BFCl      CHF2Br          C12H9_o_bipheny HBO+    Mo       PbF4    SrOH+
 BFCl2     CHF2Cl          C12H10_biphenyl HBO2    Mo+      PbI     Sr_OH_2
 BFOH      CHF3            Ca              HBS     Mo-      PbI2    SrS
 BF_OH_2   CHI3            Ca+             HBS+    MoO      PbI3    Sr2
 BF2       CH2             CaBr            HCN     MoO2     PbI4    Ta
 BF2+      CH2Br2          CaBr2           HCO     MoO3     PbO     Ta+
 BF2-      CH2Cl           CaCl            HCO+    MoO3-    PbO2    Ta-
 BF2Cl     CH2ClBr         CaCl+           HCCN    Mo2O6    PbS     TaCl5
 BF2OH     CH2Cl2          CaCl2           HCCO    Mo3O9    PbS2    TaO
 BF3       CH2F            CaF             HCl     Mo4O12   Rb      TaO2
 BF4-      CH2FBr          CaF+            HD      Mo5O15   Rb+     Ti
 BH        CH2FCl          CaF2            HD+     N        Rb-     Ti+
 BHCl      CH2F2           CaH             HDO     N+       RbBO2   Ti-
 BHCl2     CH2I2           CaI             HDO2    N-       RbBr    TiCl
 BHF       CH3             CaI2            HF      NCO      RbCl    TiCl2
 BHFCl     CH3Br           CaO             HI      ND       RbF     TiCl3
 BHF2      CH3Cl           CaO+            HNC     ND2      RbH     TiCl4
 BH2       CH3F            CaOH            HNCO    ND3      RbI     TiO
 BH2Cl     CH3I            CaOH+           HNO     NF       RbK     TiO+
 BH2F      CH2OH           Ca_OH_2         HNO2    NF2      Rbli    TiOCl
 BH3       CH2OH+          CaS             HNO3    NF3      RbNO2   TiOCl2
 BH3NH3    CH3O            Ca2             HOCl    NH       RbNO3   TiO2
 BH4       CH4             Cd              HOF     NH+      RbNa    U
 BI        CH3OH           Cd+             HO2     NHF      RbO     UF
 BI2       CH3OOH          Cl              HO2-    NHF2     RbOH    UF+
 BI3       CI              Cl+             HPO     NH2      Rb2Br2  UF-
 BN        CI2             Cl-             HSO3F   NH2F     Rb2Cl2  UF2
 BO        CI3             ClCN            H2      NH3      Rb2F2   UF2+
 BO-       CI4             ClF             H2+     NH2OH    Rb2I2   UF2-
 BOCl      CN              ClF3            H2-     NH4+     Rb2O    UF3
 BOCl2     CN+             ClF5            HBOH    NO       Rb2O2   UF3+
 BOF       CN-             ClO             HCOOH   NOCl     Rb2O2H2 UF3-
 BOF2      CNN             ClO2            H2F2    NOF      Rb2SO4  UF4
 BOH       CO              Cl2             H2O     NOF3     Rn      UF4+
 BO2       CO+             Cl2O            H2O+    NO2      Rn+     UF4-
 BO2-      COCl            Co              H2O2    NO2-     S       UF5
 B_OH_2    COCl2           Co+             H2S     NO2Cl    S+      UF5+
 BS        COFCl           Co-             H2SO4   NO2F     S-      UF5-
 BS2       COF2            Cr              H2BOH   NO3      SCl     UF6
 B2        COHCl           Cr+             HB_OH_2 NO3-     SCl2    UF6-
 B2C       COHF            Cr-             H3BO3   NO3F     SCl2+   UO
 B2Cl4     COS             CrN             H3B3O3  N2       SD      UO+
 B2F4      CO2             CrO             H3B3O6  N2+      SF      UOF
 B2H       CO2+            CrO2            H3F3    N2-      SF+     UOF2
 B2H2      COOH            CrO3            H3O+    NCN      SF-     UOF3
 B2H3      CP              CrO3-           H4F4    N2D2_cis SF2     UOF4
 B2H3_db   CS              Cs              H5F5    N2F2     SF2+    UO2
 B2H4      CS2             Cs+             H6F6    N2F4     SF2-    UO2+
 B2H4_db   C2              Cs-             H7F7    N2H2     SF3     UO2-
 B2H5      C2+             CsBO2           He      NH2NO2   SF3+    UO2F
 B2H5_db   C2-             CsBr            He+     N2H4     SF3-    UO2F2
 B2H6      C2Cl            CsCl            Hg      N2O      SF4     UO3
 B2O       C2Cl2           CsF             Hg+     N2O+     SF4+    UO3-
 B2O2      C2Cl3           CsH             HgBr2   N2O3     SF4-    V
 B2O3      C2Cl4           CsI             I       N2O4     SF5     V+
 B2_OH_4   C2Cl6           Csli            I+      N2O5     SF5+    V-
 B2S       C2F             CsNO2           I-      N3       SF5-    VCl4
 B2S2      C2FCl           CsNO3           IF5     N3H      SF6     VN
 B2S3      C2FCl3          CsNa            IF7     Na       SF6-    VO
 B3H7_C2v  C2F2            CsO             I2      Na+      SH      VO2
 B3H7_Cs   C2F2Cl2         CsOH            In      Na-      SH-     V4O10
 B3H9      C2F3            CsRb            In+     NaAlF4   SN      W
 B3N3H6    C2F3Cl          Cs2             InBr    NaBO2    SO      W+
 B3O3Cl3   C2F4            Cs2Br2          InBr2   NaBr     SO-     W-
 B3O3FCl2  C2F6            Cs2CO3          InBr3   NaCN     SOF2    WCl6
 B3O3F2Cl  C2H             Cs2Cl2          InCl    NaCl     SO2     WO
 B3O3F3    C2HCl           Cs2F2           InCl2   NaF      SO2-    WOCl4
 B4H4      C2HCl3          Cs2I2           InCl3   NaH      SO2Cl2  WO2
 B4H10     C2HF            Cs2O            InF     NaI      SO2FCl  WO2Cl2
 B4H12     C2HFCl2         Cs2O+           InF2    Nali     SO2F2   WO3
 B5H9      C2HF2Cl         Cs2O2           InF3    NaNO2    SO3     WO3-
 Ba        C2HF3           Cs2O2H2         InH     NaNO3    S2      Xe
 Ba+       C2H2_vinylidene Cs2SO4          InI     NaO      S2-     Xe+
 BaBr      C2H2Cl2         Cu              InI2    NaOH     S2Cl2   Zn
 BaBr2     C2H2FCl         Cu+             InI3    NaOH+    S2F2    Zn+
 BaCl      C2H2F2          Cu-             InO     Na2      S2O     Zr
 BaCl+     CH2CO_ketene    CuCl            InOH    Na2Br2   S3      Zr+
 BaCl2     O_CH_2O         CuF             In2Br2  Na2Cl2   S4      Zr-
 BaF       HO_CO_2OH       CuF2            In2Br4  Na2F2    S5      ZrN
 BaF+      C2H3_vinyl      CuO             In2Br6  Na2I2    S6      ZrO
 BaF2      CH2Br-COOH      Cu2             In2Cl2  Na2O     S7      ZrO+
 BaH       C2H3Cl          Cu3Cl3          In2Cl4  Na2O+    S8      ZrO2
 BaI       CH2Cl-COOH      D               In2Cl6  Na2O2    Sc
 BaI2      C2H3F           D+              In2F2   Na2O2H2  Sc+
 BaO       CH3CN           D-              In2F4   Na2SO4   Sc-
 BaO+      CH3CO_acetyl    DBr             In2F6   Na3Cl3   ScO
 BaOH      C2H4            DCl             In2I2   Na3F3    ScO+
</pre>
</html>"));
        end SingleGasesData;
      annotation (Documentation(info="<html>

</html>"));
      end Common;
    annotation (Documentation(info="<html>
<p>This package contains data for the 1241 ideal gases from</p>
<blockquote>
  <p>McBride B.J., Zehe M.J., and Gordon S. (2002): <b>NASA Glenn Coefficients
  for Calculating Thermodynamic Properties of Individual Species</b>. NASA
  report TP-2002-211556</p>
</blockquote>
<p>Medium models for some of these gases are available in package
<a href=\"modelica://Modelica.Media.IdealGases.SingleGases\">IdealGases.SingleGases</a>
and some examples for mixtures are available in package <a href=\"modelica://Modelica.Media.IdealGases.MixtureGases\">IdealGases.MixtureGases</a>
</p>
<h4>Using and Adapting Medium Models</h4>
<p>
The data records allow computing the ideal gas specific enthalpy, specific entropy and heat capacity of the substances listed below. From them, even the Gibbs energy and equilibrium constants for reactions can be computed. Critical data that is needed for computing the viscosity and thermal conductivity is not included. In order to add mixtures or single substance medium packages that are
subtypes of
<a href=\"modelica://Modelica.Media.Interfaces.PartialMedium\">Interfaces.PartialMedium</a>
(i.e., can be utilized at all places where PartialMedium is defined),
a few additional steps have to be performed:
</p>
<ol>
<li>
All single gas media need to define a constant instance of record
<a href=\"modelica://Modelica.Media.Interfaces.PartialMedium.FluidConstants\">IdealGases.Common.SingleGasNasa.FluidConstants</a>.
For 37 ideal gases such records are provided in package
<a href=\"modelica://Modelica.Media.IdealGases.Common.FluidData\">IdealGases.Common.FluidData</a>.
For the other gases, such a record instance has to be provided by the user, e.g., by getting
the data from a commercial or public data base. A public source of the needed data is for example the <a href=\"http://webbook.nist.gov/chemistry/\"> NIST Chemistry WebBook</a></li>

<li>When the data is available, and a user has an instance of a
<a href=\"modelica://Modelica.Media.Interfaces.PartialMedium.FluidConstants\">FluidConstants</a> record filled with data, a medium package has to be written. Note that only the dipole moment, the accentric factor and critical data are necessary for the viscosity and thermal conductivity functions.</li>
<li><ul>
<li>For single components, a new package following the pattern in
<a href=\"modelica://Modelica.Media.IdealGases.SingleGases\">IdealGases.SingleGases</a> has to be created, pointing both to a data record for cp and to a user-defined fluidConstants record.</li>
<li>For mixtures of several components, a new package following the pattern in
<a href=\"modelica://Modelica.Media.IdealGases.MixtureGases\">IdealGases.MixtureGases</a> has to be created, building an array of data records for cp and an array of (partly) user-defined fluidConstants records.</li>
</ul></li>
</ol>
<p>Note that many properties can computed for the full set of 1241 gases listed below, but due to the missing viscosity and thermal conductivity functions, no fully Modelica.Media-compliant media can be defined.</p>
<p>
Data records for heat capacity, specific enthalpy and specific entropy exist for the following substances and ions:
</p>
<pre>
 Ag        BaOH+           C2H4O_ethylen_o DF      In2I4    Nb      ScO2
 Ag+       Ba_OH_2         CH3CHO_ethanal  DOCl    In2I6    Nb+     Sc2O
 Ag-       BaS             CH3COOH         DO2     In2O     Nb-     Sc2O2
 Air       Ba2             OHCH2COOH       DO2-    K        NbCl5   Si
 Al        Be              C2H5            D2      K+       NbO     Si+
 Al+       Be+             C2H5Br          D2+     K-       NbOCl3  Si-
 Al-       Be++            C2H6            D2-     KAlF4    NbO2    SiBr
 AlBr      BeBr            CH3N2CH3        D2O     KBO2     Ne      SiBr2
 AlBr2     BeBr2           C2H5OH          D2O2    KBr      Ne+     SiBr3
 AlBr3     BeCl            CH3OCH3         D2S     KCN      Ni      SiBr4
 AlC       BeCl2           CH3O2CH3        e-      KCl      Ni+     SiC
 AlC2      BeF             CCN             F       KF       Ni-     SiC2
 AlCl      BeF2            CNC             F+      KH       NiCl    SiCl
 AlCl+     BeH             OCCN            F-      KI       NiCl2   SiCl2
 AlCl2     BeH+            C2N2            FCN     Kli      NiO     SiCl3
 AlCl3     BeH2            C2O             FCO     KNO2     NiS     SiCl4
 AlF       BeI             C3              FO      KNO3     O       SiF
 AlF+      BeI2            C3H3_1_propynl  FO2_FOO KNa      O+      SiFCl
 AlFCl     BeN             C3H3_2_propynl  FO2_OFO KO       O-      SiF2
 AlFCl2    BeO             C3H4_allene     F2      KOH      OD      SiF3
 AlF2      BeOH            C3H4_propyne    F2O     K2       OD-     SiF4
 AlF2-     BeOH+           C3H4_cyclo      F2O2    K2+      OH      SiH
 AlF2Cl    Be_OH_2         C3H5_allyl      FS2F    K2Br2    OH+     SiH+
 AlF3      BeS             C3H6_propylene  Fe      K2CO3    OH-     SiHBr3
 AlF4-     Be2             C3H6_cyclo      Fe+     K2C2N2   O2      SiHCl
 AlH       Be2Cl4          C3H6O_propylox  Fe_CO_5 K2Cl2    O2+     SiHCl3
 AlHCl     Be2F4           C3H6O_acetone   FeCl    K2F2     O2-     SiHF
 AlHCl2    Be2O            C3H6O_propanal  FeCl2   K2I2     O3      SiHF3
 AlHF      Be2OF2          C3H7_n_propyl   FeCl3   K2O      P       SiHI3
 AlHFCl    Be2O2           C3H7_i_propyl   FeO     K2O+     P+      SiH2
 AlHF2     Be3O3           C3H8            Fe_OH_2 K2O2     P-      SiH2Br2
 AlH2      Be4O4           C3H8O_1propanol Fe2Cl4  K2O2H2   PCl     SiH2Cl2
 AlH2Cl    Br              C3H8O_2propanol Fe2Cl6  K2SO4    PCl2    SiH2F2
 AlH2F     Br+             CNCOCN          Ga      Kr       PCl2-   SiH2I2
 AlH3      Br-             C3O2            Ga+     Kr+      PCl3    SiH3
 AlI       BrCl            C4              GaBr    li       PCl5    SiH3Br
 AlI2      BrF             C4H2_butadiyne  GaBr2   li+      PF      SiH3Cl
 AlI3      BrF3            C4H4_1_3-cyclo  GaBr3   li-      PF+     SiH3F
 AlN       BrF5            C4H6_butadiene  GaCl    liAlF4   PF-     SiH3I
 AlO       BrO             C4H6_1butyne    GaCl2   liBO2    PFCl    SiH4
 AlO+      OBrO            C4H6_2butyne    GaCl3   liBr     PFCl-   SiI
 AlO-      BrOO            C4H6_cyclo      GaF     liCl     PFCl2   SiI2
 AlOCl     BrO3            C4H8_1_butene   GaF2    liF      PFCl4   SiN
 AlOCl2    Br2             C4H8_cis2_buten GaF3    liH      PF2     SiO
 AlOF      BrBrO           C4H8_isobutene  GaH     liI      PF2-    SiO2
 AlOF2     BrOBr           C4H8_cyclo      GaI     liN      PF2Cl   SiS
 AlOF2-    C               C4H9_n_butyl    GaI2    liNO2    PF2Cl3  SiS2
 AlOH      C+              C4H9_i_butyl    GaI3    liNO3    PF3     Si2
 AlOHCl    C-              C4H9_s_butyl    GaO     liO      PF3Cl2  Si2C
 AlOHCl2   CBr             C4H9_t_butyl    GaOH    liOF     PF4Cl   Si2F6
 AlOHF     CBr2            C4H10_n_butane  Ga2Br2  liOH     PF5     Si2N
 AlOHF2    CBr3            C4H10_isobutane Ga2Br4  liON     PH      Si3
 AlO2      CBr4            C4N2            Ga2Br6  li2      PH2     Sn
 AlO2-     CCl             C5              Ga2Cl2  li2+     PH2-    Sn+
 Al_OH_2   CCl2            C5H6_1_3cyclo   Ga2Cl4  li2Br2   PH3     Sn-
 Al_OH_2Cl CCl2Br2         C5H8_cyclo      Ga2Cl6  li2F2    PN      SnBr
 Al_OH_2F  CCl3            C5H10_1_pentene Ga2F2   li2I2    PO      SnBr2
 Al_OH_3   CCl3Br          C5H10_cyclo     Ga2F4   li2O     PO-     SnBr3
 AlS       CCl4            C5H11_pentyl    Ga2F6   li2O+    POCl3   SnBr4
 AlS2      CF              C5H11_t_pentyl  Ga2I2   li2O2    POFCl2  SnCl
 Al2       CF+             C5H12_n_pentane Ga2I4   li2O2H2  POF2Cl  SnCl2
 Al2Br6    CFBr3           C5H12_i_pentane Ga2I6   li2SO4   POF3    SnCl3
 Al2C2     CFCl            CH3C_CH3_2CH3   Ga2O    li3+     PO2     SnCl4
 Al2Cl6    CFClBr2         C6D5_phenyl     Ge      li3Br3   PO2-    SnF
 Al2F6     CFCl2           C6D6            Ge+     li3Cl3   PS      SnF2
 Al2I6     CFCl2Br         C6H2            Ge-     li3F3    P2      SnF3
 Al2O      CFCl3           C6H5_phenyl     GeBr    li3I3    P2O3    SnF4
 Al2O+     CF2             C6H5O_phenoxy   GeBr2   Mg       P2O4    SnI
 Al2O2     CF2+            C6H6            GeBr3   Mg+      P2O5    SnI2
 Al2O2+    CF2Br2          C6H5OH_phenol   GeBr4   MgBr     P3      SnI3
 Al2O3     CF2Cl           C6H10_cyclo     GeCl    MgBr2    P3O6    SnI4
 Al2S      CF2ClBr         C6H12_1_hexene  GeCl2   MgCl     P4      SnO
 Al2S2     CF2Cl2          C6H12_cyclo     GeCl3   MgCl+    P4O6    SnO2
 Ar        CF3             C6H13_n_hexyl   GeCl4   MgCl2    P4O7    SnS
 Ar+       CF3+            C6H14_n_hexane  GeF     MgF      P4O8    SnS2
 B         CF3Br           C7H7_benzyl     GeF2    MgF+     P4O9    Sn2
 B+        CF3Cl           C7H8            GeF3    MgF2     P4O10   Sr
 B-        CF4             C7H8O_cresol_mx GeF4    MgF2+    Pb      Sr+
 BBr       CH+             C7H14_1_heptene GeH4    MgH      Pb+     SrBr
 BBr2      CHBr3           C7H15_n_heptyl  GeI     MgI      Pb-     SrBr2
 BBr3      CHCl            C7H16_n_heptane GeO     MgI2     PbBr    SrCl
 BC        CHClBr2         C7H16_2_methylh GeO2    MgN      PbBr2   SrCl+
 BC2       CHCl2           C8H8_styrene    GeS     MgO      PbBr3   SrCl2
 BCl       CHCl2Br         C8H10_ethylbenz GeS2    MgOH     PbBr4   SrF
 BCl+      CHCl3           C8H16_1_octene  Ge2     MgOH+    PbCl    SrF+
 BClOH     CHF             C8H17_n_octyl   H       Mg_OH_2  PbCl2   SrF2
 BCl_OH_2  CHFBr2          C8H18_n_octane  H+      MgS      PbCl3   SrH
 BCl2      CHFCl           C8H18_isooctane H-      Mg2      PbCl4   SrI
 BCl2+     CHFClBr         C9H19_n_nonyl   HAlO    Mg2F4    PbF     SrI2
 BCl2OH    CHFCl2          C10H8_naphthale HAlO2   Mn       PbF2    SrO
 BF        CHF2            C10H21_n_decyl  HBO     Mn+      PbF3    SrOH
 BFCl      CHF2Br          C12H9_o_bipheny HBO+    Mo       PbF4    SrOH+
 BFCl2     CHF2Cl          C12H10_biphenyl HBO2    Mo+      PbI     Sr_OH_2
 BFOH      CHF3            Ca              HBS     Mo-      PbI2    SrS
 BF_OH_2   CHI3            Ca+             HBS+    MoO      PbI3    Sr2
 BF2       CH2             CaBr            HCN     MoO2     PbI4    Ta
 BF2+      CH2Br2          CaBr2           HCO     MoO3     PbO     Ta+
 BF2-      CH2Cl           CaCl            HCO+    MoO3-    PbO2    Ta-
 BF2Cl     CH2ClBr         CaCl+           HCCN    Mo2O6    PbS     TaCl5
 BF2OH     CH2Cl2          CaCl2           HCCO    Mo3O9    PbS2    TaO
 BF3       CH2F            CaF             HCl     Mo4O12   Rb      TaO2
 BF4-      CH2FBr          CaF+            HD      Mo5O15   Rb+     Ti
 BH        CH2FCl          CaF2            HD+     N        Rb-     Ti+
 BHCl      CH2F2           CaH             HDO     N+       RbBO2   Ti-
 BHCl2     CH2I2           CaI             HDO2    N-       RbBr    TiCl
 BHF       CH3             CaI2            HF      NCO      RbCl    TiCl2
 BHFCl     CH3Br           CaO             HI      ND       RbF     TiCl3
 BHF2      CH3Cl           CaO+            HNC     ND2      RbH     TiCl4
 BH2       CH3F            CaOH            HNCO    ND3      RbI     TiO
 BH2Cl     CH3I            CaOH+           HNO     NF       RbK     TiO+
 BH2F      CH2OH           Ca_OH_2         HNO2    NF2      Rbli    TiOCl
 BH3       CH2OH+          CaS             HNO3    NF3      RbNO2   TiOCl2
 BH3NH3    CH3O            Ca2             HOCl    NH       RbNO3   TiO2
 BH4       CH4             Cd              HOF     NH+      RbNa    U
 BI        CH3OH           Cd+             HO2     NHF      RbO     UF
 BI2       CH3OOH          Cl              HO2-    NHF2     RbOH    UF+
 BI3       CI              Cl+             HPO     NH2      Rb2Br2  UF-
 BN        CI2             Cl-             HSO3F   NH2F     Rb2Cl2  UF2
 BO        CI3             ClCN            H2      NH3      Rb2F2   UF2+
 BO-       CI4             ClF             H2+     NH2OH    Rb2I2   UF2-
 BOCl      CN              ClF3            H2-     NH4+     Rb2O    UF3
 BOCl2     CN+             ClF5            HBOH    NO       Rb2O2   UF3+
 BOF       CN-             ClO             HCOOH   NOCl     Rb2O2H2 UF3-
 BOF2      CNN             ClO2            H2F2    NOF      Rb2SO4  UF4
 BOH       CO              Cl2             H2O     NOF3     Rn      UF4+
 BO2       CO+             Cl2O            H2O+    NO2      Rn+     UF4-
 BO2-      COCl            Co              H2O2    NO2-     S       UF5
 B_OH_2    COCl2           Co+             H2S     NO2Cl    S+      UF5+
 BS        COFCl           Co-             H2SO4   NO2F     S-      UF5-
 BS2       COF2            Cr              H2BOH   NO3      SCl     UF6
 B2        COHCl           Cr+             HB_OH_2 NO3-     SCl2    UF6-
 B2C       COHF            Cr-             H3BO3   NO3F     SCl2+   UO
 B2Cl4     COS             CrN             H3B3O3  N2       SD      UO+
 B2F4      CO2             CrO             H3B3O6  N2+      SF      UOF
 B2H       CO2+            CrO2            H3F3    N2-      SF+     UOF2
 B2H2      COOH            CrO3            H3O+    NCN      SF-     UOF3
 B2H3      CP              CrO3-           H4F4    N2D2_cis SF2     UOF4
 B2H3_db   CS              Cs              H5F5    N2F2     SF2+    UO2
 B2H4      CS2             Cs+             H6F6    N2F4     SF2-    UO2+
 B2H4_db   C2              Cs-             H7F7    N2H2     SF3     UO2-
 B2H5      C2+             CsBO2           He      NH2NO2   SF3+    UO2F
 B2H5_db   C2-             CsBr            He+     N2H4     SF3-    UO2F2
 B2H6      C2Cl            CsCl            Hg      N2O      SF4     UO3
 B2O       C2Cl2           CsF             Hg+     N2O+     SF4+    UO3-
 B2O2      C2Cl3           CsH             HgBr2   N2O3     SF4-    V
 B2O3      C2Cl4           CsI             I       N2O4     SF5     V+
 B2_OH_4   C2Cl6           Csli            I+      N2O5     SF5+    V-
 B2S       C2F             CsNO2           I-      N3       SF5-    VCl4
 B2S2      C2FCl           CsNO3           IF5     N3H      SF6     VN
 B2S3      C2FCl3          CsNa            IF7     Na       SF6-    VO
 B3H7_C2v  C2F2            CsO             I2      Na+      SH      VO2
 B3H7_Cs   C2F2Cl2         CsOH            In      Na-      SH-     V4O10
 B3H9      C2F3            CsRb            In+     NaAlF4   SN      W
 B3N3H6    C2F3Cl          Cs2             InBr    NaBO2    SO      W+
 B3O3Cl3   C2F4            Cs2Br2          InBr2   NaBr     SO-     W-
 B3O3FCl2  C2F6            Cs2CO3          InBr3   NaCN     SOF2    WCl6
 B3O3F2Cl  C2H             Cs2Cl2          InCl    NaCl     SO2     WO
 B3O3F3    C2HCl           Cs2F2           InCl2   NaF      SO2-    WOCl4
 B4H4      C2HCl3          Cs2I2           InCl3   NaH      SO2Cl2  WO2
 B4H10     C2HF            Cs2O            InF     NaI      SO2FCl  WO2Cl2
 B4H12     C2HFCl2         Cs2O+           InF2    Nali     SO2F2   WO3
 B5H9      C2HF2Cl         Cs2O2           InF3    NaNO2    SO3     WO3-
 Ba        C2HF3           Cs2O2H2         InH     NaNO3    S2      Xe
 Ba+       C2H2_vinylidene Cs2SO4          InI     NaO      S2-     Xe+
 BaBr      C2H2Cl2         Cu              InI2    NaOH     S2Cl2   Zn
 BaBr2     C2H2FCl         Cu+             InI3    NaOH+    S2F2    Zn+
 BaCl      C2H2F2          Cu-             InO     Na2      S2O     Zr
 BaCl+     CH2CO_ketene    CuCl            InOH    Na2Br2   S3      Zr+
 BaCl2     O_CH_2O         CuF             In2Br2  Na2Cl2   S4      Zr-
 BaF       HO_CO_2OH       CuF2            In2Br4  Na2F2    S5      ZrN
 BaF+      C2H3_vinyl      CuO             In2Br6  Na2I2    S6      ZrO
 BaF2      CH2Br-COOH      Cu2             In2Cl2  Na2O     S7      ZrO+
 BaH       C2H3Cl          Cu3Cl3          In2Cl4  Na2O+    S8      ZrO2
 BaI       CH2Cl-COOH      D               In2Cl6  Na2O2    Sc
 BaI2      C2H3F           D+              In2F2   Na2O2H2  Sc+
 BaO       CH3CN           D-              In2F4   Na2SO4   Sc-
 BaO+      CH3CO_acetyl    DBr             In2F6   Na3Cl3   ScO
 BaOH      C2H4            DCl             In2I2   Na3F3    ScO+
</pre></html>"));
    end IdealGases;

    package Incompressible
    "Medium model for T-dependent properties, defined by tables or polynomials"
      extends Modelica.Icons.VariantsPackage;
      import Modelica.Constants;
      import Modelica.Math;

      package Common "Common data structures"
        extends Modelica.Icons.Package;

        record BaseProps_Tpoly "Fluid state record"
          extends Modelica.Icons.Record;
          SI.Temperature T "Temperature";
          SI.Pressure p "Pressure";
          //    SI.Density d "Density";
        end BaseProps_Tpoly;
      end Common;

      package TableBased "Incompressible medium properties based on tables"
        import Poly = Modelica.Media.Incompressible.TableBased.Polynomials_Temp;
        extends Modelica.Media.Interfaces.PartialMedium(
           ThermoStates = if enthalpyOfT then Modelica.Media.Interfaces.Choices.IndependentVariables.T
                                                                             else Modelica.Media.Interfaces.Choices.IndependentVariables.pT,
           final reducedX=true,
           final fixedX = true,
           mediumName="tableMedium",
           redeclare record ThermodynamicState=Common.BaseProps_Tpoly,
           singleState=true,
           reference_p = 1.013e5,
           Temperature(min = T_min, max = T_max));

        constant Boolean enthalpyOfT=true
          "True if enthalpy is approximated as a function of T only, (p-dependence neglected)";

        constant Boolean densityOfT = size(tableDensity,1) > 1
          "True if density is a function of temperature";

        constant Modelica.SIunits.Temperature T_min
          "Minimum temperature valid for medium model";

        constant Modelica.SIunits.Temperature T_max
          "Maximum temperature valid for medium model";

        constant Temperature T0=273.15 "Reference Temperature";

        constant SpecificEnthalpy h0=0 "Reference enthalpy at T0, reference_p";

        constant SpecificEntropy s0=0 "Reference entropy at T0, reference_p";

        constant MolarMass MM_const=0.1 "Molar mass";

        constant Integer npol=2 "Degree of polynomial used for fitting";

        constant Integer npolDensity=npol
          "Degree of polynomial used for fitting rho(T)";

        constant Integer npolHeatCapacity=npol
          "Degree of polynomial used for fitting Cp(T)";

        constant Integer npolViscosity=npol
          "Degree of polynomial used for fitting eta(T)";

        constant Integer npolConductivity=npol
          "Degree of polynomial used for fitting lambda(T)";

        constant Integer neta=size(tableViscosity,1)
          "Number of data points for viscosity";

        constant Real[:,2] tableDensity "Table for rho(T)";

        constant Real[:,2] tableHeatCapacity "Table for Cp(T)";

        constant Real[:,2] tableViscosity "Table for eta(T)";

        constant Real[:,2] tableConductivity "Table for lambda(T)";

        constant Boolean TinK "True if T[K],Kelvin used for table temperatures";

        constant Boolean hasDensity = not (size(tableDensity,1)==0)
          "True if table tableDensity is present";

        constant Boolean hasHeatCapacity = not (size(tableHeatCapacity,1)==0)
          "True if table tableHeatCapacity is present";

        constant Boolean hasViscosity = not (size(tableViscosity,1)==0)
          "True if table tableViscosity is present";

        final constant Real invTK[neta] = if size(tableViscosity,1) > 0 then
            (if TinK then 1 ./ tableViscosity[:,1] else 1 ./ Cv.from_degC(tableViscosity[:,1])) else fill(0,neta);

        final constant Real poly_rho[:] = if hasDensity then
                                             Poly.fitting(tableDensity[:,1],tableDensity[:,2],npolDensity) else
                                               zeros(npolDensity+1);

        final constant Real poly_Cp[:] = if hasHeatCapacity then
                                             Poly.fitting(tableHeatCapacity[:,1],tableHeatCapacity[:,2],npolHeatCapacity) else
                                               zeros(npolHeatCapacity+1);

        final constant Real poly_eta[:] = if hasViscosity then
                                             Poly.fitting(invTK, Math.log(tableViscosity[:,2]),npolViscosity) else
                                               zeros(npolViscosity+1);

        final constant Real poly_lam[:] = if size(tableConductivity,1)>0 then
                                             Poly.fitting(tableConductivity[:,1],tableConductivity[:,2],npolConductivity) else
                                               zeros(npolConductivity+1);

        redeclare model extends BaseProperties(
          final standardOrderComponents=true,
          p_bar=Cv.to_bar(p),
          T_degC(start = T_start-273.15)=Cv.to_degC(T),
          T(start = T_start,
            stateSelect=if preferredMediumStates then StateSelect.prefer else StateSelect.default))
          "Base properties of T dependent medium"
        //  redeclare parameter SpecificHeatCapacity R=Modelica.Constants.R,

          SI.SpecificHeatCapacity cp "Specific heat capacity";
          parameter SI.Temperature T_start = 298.15 "Initial temperature";
        equation
          assert(hasDensity,"Medium " + mediumName +
                            " can not be used without assigning tableDensity.");
          assert(T >= T_min and T <= T_max, "Temperature T (= " + String(T) +
                 " K) is not in the allowed range (" + String(T_min) +
                 " K <= T <= " + String(T_max) + " K) required from medium model \""
                 + mediumName + "\".");
          R = Modelica.Constants.R;
          cp = Poly.evaluate(poly_Cp,if TinK then T else T_degC);
          h = if enthalpyOfT then h_T(T) else  h_pT(p,T,densityOfT);
          u = h - (if singleState then  reference_p/d else state.p/d);
          d = Poly.evaluate(poly_rho,if TinK then T else T_degC);
          state.T = T;
          state.p = p;
          MM = MM_const;
          annotation(Documentation(info="<html>
<p>
Note that the inner energy neglects the pressure dependence, which is only
true for an incompressible medium with d = constant. The neglected term is
p-reference_p)/rho*(T/rho)*(partial rho /partial T). This is very small for
liquids due to proportionality to 1/d^2, but can be problematic for gases that are
modeled incompressible.
</p>
<p>It should be noted that incompressible media only have 1 state per control volume (usually T),
but have both T and p as inputs for fully correct properties. The error of using only T-dependent
properties is small, therefore a Boolean flag enthalpyOfT exists. If it is true, the
enumeration Choices.independentVariables  is set to  Choices.independentVariables.T otherwise
it is set to Choices.independentVariables.pT.</p>
<p>
Enthalpy is never a function of T only (h = h(T) + (p-reference_p)/d), but the
error is also small and non-linear systems can be avoided. In particular,
non-linear systems are small and local as opposed to large and over all volumes.
</p>

<p>
Entropy is calculated as
</p>
<pre>
  s = s0 + integral(Cp(T)/T,dt)
</pre>
<p>
which is only exactly true for a fluid with constant density d=d0.
</p>
</html>"));
        end BaseProperties;

        redeclare function extends setState_pTX
          "Returns state record, given pressure and temperature"
        algorithm
          state := ThermodynamicState(p=p,T=T);
          annotation(smoothOrder=3);
        end setState_pTX;

        redeclare function extends setState_dTX
          "Returns state record, given pressure and temperature"
        algorithm
          assert(false, "For incompressible media with d(T) only, state can not be set from density and temperature");
        end setState_dTX;

        redeclare function extends setState_phX
          "Returns state record, given pressure and specific enthalpy"
        algorithm
          state :=ThermodynamicState(p=p,T=T_ph(p,h));
          annotation(Inline=true,smoothOrder=3);
        end setState_phX;

        redeclare function extends setState_psX
          "Returns state record, given pressure and specific entropy"
        algorithm
          state :=ThermodynamicState(p=p,T=T_ps(p,s));
          annotation(Inline=true,smoothOrder=3);
        end setState_psX;

            redeclare function extends setSmoothState
        "Return thermodynamic state so that it smoothly approximates: if x > 0 then state_a else state_b"
            algorithm
              state :=ThermodynamicState(p=Media.Common.smoothStep(x, state_a.p, state_b.p, x_small),
                                         T=Media.Common.smoothStep(x, state_a.T, state_b.T, x_small));
              annotation(Inline=true,smoothOrder=3);
            end setSmoothState;

        redeclare function extends specificHeatCapacityCv
          "Specific heat capacity at constant volume (or pressure) of medium"

        algorithm
          assert(hasHeatCapacity,"Specific Heat Capacity, Cv, is not defined for medium "
                                                 + mediumName + ".");
          cv := Poly.evaluate(poly_Cp,if TinK then state.T else state.T - 273.15);
         annotation(smoothOrder=2);
        end specificHeatCapacityCv;

        redeclare function extends specificHeatCapacityCp
          "Specific heat capacity at constant volume (or pressure) of medium"

        algorithm
          assert(hasHeatCapacity,"Specific Heat Capacity, Cv, is not defined for medium "
                                                 + mediumName + ".");
          cp := Poly.evaluate(poly_Cp,if TinK then state.T else state.T - 273.15);
         annotation(smoothOrder=2);
        end specificHeatCapacityCp;

        redeclare function extends dynamicViscosity
          "Return dynamic viscosity as a function of the thermodynamic state record"

        algorithm
          assert(size(tableViscosity,1)>0,"DynamicViscosity, eta, is not defined for medium "
                                                 + mediumName + ".");
          eta := Math.exp(Poly.evaluate(poly_eta, 1/state.T));
         annotation(smoothOrder=2);
        end dynamicViscosity;

        redeclare function extends thermalConductivity
          "Return thermal conductivity as a function of the thermodynamic state record"

        algorithm
          assert(size(tableConductivity,1)>0,"ThermalConductivity, lambda, is not defined for medium "
                                                 + mediumName + ".");
          lambda := Poly.evaluate(poly_lam,if TinK then state.T else Cv.to_degC(state.T));
         annotation(smoothOrder=2);
        end thermalConductivity;

        function s_T "Compute specific entropy"
          extends Modelica.Icons.Function;
          input Temperature T "Temperature";
          output SpecificEntropy s "Specific entropy";
        algorithm
          s := s0 + (if TinK then
            Poly.integralValue(poly_Cp[1:npol],T, T0) else
            Poly.integralValue(poly_Cp[1:npol],Cv.to_degC(T),Cv.to_degC(T0)))
            + Modelica.Math.log(T/T0)*
            Poly.evaluate(poly_Cp,if TinK then 0 else Modelica.Constants.T_zero);
         annotation(Inline=true,smoothOrder=2);
        end s_T;

        redeclare function extends specificEntropy
          "Return specific entropy as a function of the thermodynamic state record"

      protected
          Integer npol=size(poly_Cp,1)-1;
        algorithm
          assert(hasHeatCapacity,"Specific Entropy, s(T), is not defined for medium "
                                                 + mediumName + ".");
          s := s_T(state.T);
         annotation(smoothOrder=2);
        end specificEntropy;

        function h_T "Compute specific enthalpy from temperature"
          import Modelica.SIunits.Conversions.to_degC;
          extends Modelica.Icons.Function;
          input SI.Temperature T "Temperature";
          output SI.SpecificEnthalpy h "Specific enthalpy at p, T";
        algorithm
          h :=h0 + Poly.integralValue(poly_Cp, if TinK then T else Cv.to_degC(T), if TinK then
          T0 else Cv.to_degC(T0));
         annotation(derivative=h_T_der);
        end h_T;

        function h_T_der "Compute specific enthalpy from temperature"
          import Modelica.SIunits.Conversions.to_degC;
          extends Modelica.Icons.Function;
          input SI.Temperature T "Temperature";
          input Real dT "Temperature derivative";
          output Real dh "Derivative of Specific enthalpy at T";
        algorithm
          dh :=Poly.evaluate(poly_Cp, if TinK then T else Cv.to_degC(T))*dT;
         annotation(smoothOrder=1);
        end h_T_der;

        function h_pT "Compute specific enthalpy from pressure and temperature"
          import Modelica.SIunits.Conversions.to_degC;
          extends Modelica.Icons.Function;
          input SI.Pressure p "Pressure";
          input SI.Temperature T "Temperature";
          input Boolean densityOfT = false
            "Include or neglect density derivative dependence of enthalpy" annotation(Evaluate);
          output SI.SpecificEnthalpy h "Specific enthalpy at p, T";
        algorithm
          h :=h0 + Poly.integralValue(poly_Cp, if TinK then T else Cv.to_degC(T), if TinK then
          T0 else Cv.to_degC(T0)) + (p - reference_p)/Poly.evaluate(poly_rho, if TinK then
                  T else Cv.to_degC(T))
            *(if densityOfT then (1 + T/Poly.evaluate(poly_rho, if TinK then T else Cv.to_degC(T))
          *Poly.derivativeValue(poly_rho,if TinK then T else Cv.to_degC(T))) else 1.0);
         annotation(smoothOrder=2);
        end h_pT;

        redeclare function extends temperature
          "Return temperature as a function of the thermodynamic state record"
        algorithm
         T := state.T;
         annotation(Inline=true,smoothOrder=2);
        end temperature;

        redeclare function extends pressure
          "Return pressure as a function of the thermodynamic state record"
        algorithm
         p := state.p;
         annotation(Inline=true,smoothOrder=2);
        end pressure;

        redeclare function extends density
          "Return density as a function of the thermodynamic state record"
        algorithm
          d := Poly.evaluate(poly_rho,if TinK then state.T else Cv.to_degC(state.T));
         annotation(Inline=true,smoothOrder=2);
        end density;

        redeclare function extends specificEnthalpy
          "Return specific enthalpy as a function of the thermodynamic state record"
        algorithm
          h := if enthalpyOfT then h_T(state.T) else h_pT(state.p,state.T);
         annotation(Inline=true,smoothOrder=2);
        end specificEnthalpy;

        redeclare function extends specificInternalEnergy
          "Return specific internal energy as a function of the thermodynamic state record"
        algorithm
          u := (if enthalpyOfT then h_T(state.T) else h_pT(state.p,state.T)) - (if singleState then  reference_p else state.p)/density(state);
         annotation(Inline=true,smoothOrder=2);
        end specificInternalEnergy;

        function T_ph "Compute temperature from pressure and specific enthalpy"
          extends Modelica.Icons.Function;
          input AbsolutePressure p "Pressure";
          input SpecificEnthalpy h "Specific enthalpy";
          output Temperature T "Temperature";
      protected
          package Internal
            "Solve h(T) for T with given h (use only indirectly via temperature_phX)"
            extends Modelica.Media.Common.OneNonLinearEquation;

            redeclare record extends f_nonlinear_Data
              "Superfluous record, fix later when better structure of inverse functions exists"
                constant Real[5] dummy = {1,2,3,4,5};
            end f_nonlinear_Data;

            redeclare function extends f_nonlinear "P is smuggled in via vector"
            algorithm
              y := if singleState then h_T(x) else h_pT(p,x);
            end f_nonlinear;

          end Internal;
        algorithm
         T := Internal.solve(h, T_min, T_max, p, {1}, Internal.f_nonlinear_Data());
          annotation(Inline=false, LateInline=true, inverse(h=h_pT(p,T)));
        end T_ph;

        function T_ps "Compute temperature from pressure and specific enthalpy"
          extends Modelica.Icons.Function;

          input AbsolutePressure p "Pressure";
          input SpecificEntropy s "Specific entropy";
          output Temperature T "Temperature";
      protected
          package Internal
            "Solve h(T) for T with given h (use only indirectly via temperature_phX)"
            extends Modelica.Media.Common.OneNonLinearEquation;

            redeclare record extends f_nonlinear_Data
              "Superfluous record, fix later when better structure of inverse functions exists"
                constant Real[5] dummy = {1,2,3,4,5};
            end f_nonlinear_Data;

            redeclare function extends f_nonlinear "P is smuggled in via vector"
            algorithm
              y := s_T(x);
            end f_nonlinear;

          end Internal;
        algorithm
         T := Internal.solve(s, T_min, T_max, p, {1}, Internal.f_nonlinear_Data());
        end T_ps;

        package Polynomials_Temp
        "Temporary Functions operating on polynomials (including polynomial fitting); only to be used in Modelica.Media.Incompressible.TableBased"
          extends Modelica.Icons.Package;

          function evaluate "Evaluate polynomial at a given abscissa value"
            extends Modelica.Icons.Function;
            input Real p[:]
              "Polynomial coefficients (p[1] is coefficient of highest power)";
            input Real u "Abscissa value";
            output Real y "Value of polynomial at u";
          algorithm
            y := p[1];
            for j in 2:size(p, 1) loop
              y := p[j] + u*y;
            end for;
            annotation(derivative(zeroDerivative=p)=evaluate_der);
          end evaluate;

          function derivativeValue
            "Value of derivative of polynomial at abscissa value u"
            extends Modelica.Icons.Function;
            input Real p[:]
              "Polynomial coefficients (p[1] is coefficient of highest power)";
            input Real u "Abscissa value";
            output Real y "Value of derivative of polynomial at u";
        protected
            Integer n=size(p, 1);
          algorithm
            y := p[1]*(n - 1);
            for j in 2:size(p, 1)-1 loop
              y := p[j]*(n - j) + u*y;
            end for;
            annotation(derivative(zeroDerivative=p)=derivativeValue_der);
          end derivativeValue;

          function secondDerivativeValue
            "Value of 2nd derivative of polynomial at abscissa value u"
            extends Modelica.Icons.Function;
            input Real p[:]
              "Polynomial coefficients (p[1] is coefficient of highest power)";
            input Real u "Abscissa value";
            output Real y "Value of 2nd derivative of polynomial at u";
        protected
            Integer n=size(p, 1);
          algorithm
            y := p[1]*(n - 1)*(n - 2);
            for j in 2:size(p, 1)-2 loop
              y := p[j]*(n - j)*(n - j - 1) + u*y;
            end for;
          end secondDerivativeValue;

          function integralValue "Integral of polynomial p(u) from u_low to u_high"
            extends Modelica.Icons.Function;
            input Real p[:] "Polynomial coefficients";
            input Real u_high "High integrand value";
            input Real u_low=0 "Low integrand value, default 0";
            output Real integral=0.0
              "Integral of polynomial p from u_low to u_high";
        protected
            Integer n=size(p, 1) "Degree of integrated polynomial";
            Real y_low=0 "Value at lower integrand";
          algorithm
            for j in 1:n loop
              integral := u_high*(p[j]/(n - j + 1) + integral);
              y_low := u_low*(p[j]/(n - j + 1) + y_low);
            end for;
            integral := integral - y_low;
            annotation(derivative(zeroDerivative=p)=integralValue_der);
          end integralValue;

          function fitting
            "Computes the coefficients of a polynomial that fits a set of data points in a least-squares sense"
            extends Modelica.Icons.Function;
            input Real u[:] "Abscissa data values";
            input Real y[size(u, 1)] "Ordinate data values";
            input Integer n(min=1)
              "Order of desired polynomial that fits the data points (u,y)";
            output Real p[n + 1]
              "Polynomial coefficients of polynomial that fits the date points";
        protected
            Real V[size(u, 1), n + 1] "Vandermonde matrix";
          algorithm
            // Construct Vandermonde matrix
            V[:, n + 1] := ones(size(u, 1));
            for j in n:-1:1 loop
              V[:, j] := {u[i] * V[i, j + 1] for i in 1:size(u,1)};
            end for;

            // Solve least squares problem
            p :=Modelica.Math.Matrices.leastSquares(V, y);
            annotation (Documentation(info="<html>
<p>
Polynomials.fitting(u,y,n) computes the coefficients of a polynomial
p(u) of degree \"n\" that fits the data \"p(u[i]) - y[i]\"
in a least squares sense. The polynomial is
returned as a vector p[n+1] that has the following definition:
</p>
<pre>
  p(u) = p[1]*u^n + p[2]*u^(n-1) + ... + p[n]*u + p[n+1];
</pre>
</html>"));
          end fitting;

          function evaluate_der
            "Evaluate derivative of polynomial at a given abscissa value"
            extends Modelica.Icons.Function;
            input Real p[:]
              "Polynomial coefficients (p[1] is coefficient of highest power)";
            input Real u "Abscissa value";
            input Real du "Delta of abscissa value";
            output Real dy "Value of derivative of polynomial at u";
        protected
            Integer n=size(p, 1);
          algorithm
            dy := p[1]*(n - 1);
            for j in 2:size(p, 1)-1 loop
              dy := p[j]*(n - j) + u*dy;
            end for;
            dy := dy*du;
          end evaluate_der;

          function integralValue_der
            "Time derivative of integral of polynomial p(u) from u_low to u_high, assuming only u_high as time-dependent (Leibniz rule)"
            extends Modelica.Icons.Function;
            input Real p[:] "Polynomial coefficients";
            input Real u_high "High integrand value";
            input Real u_low=0 "Low integrand value, default 0";
            input Real du_high "High integrand value";
            input Real du_low=0 "Low integrand value, default 0";
            output Real dintegral=0.0
              "Integral of polynomial p from u_low to u_high";
          algorithm
            dintegral := evaluate(p,u_high)*du_high;
          end integralValue_der;

          function derivativeValue_der
            "Time derivative of derivative of polynomial"
            extends Modelica.Icons.Function;
            input Real p[:]
              "Polynomial coefficients (p[1] is coefficient of highest power)";
            input Real u "Abscissa value";
            input Real du "Delta of abscissa value";
            output Real dy
              "Time-derivative of derivative of polynomial w.r.t. input variable at u";
        protected
            Integer n=size(p, 1);
          algorithm
            dy := secondDerivativeValue(p,u)*du;
          end derivativeValue_der;
          annotation (Documentation(info="<html>
<p>
This package contains functions to operate on polynomials,
in particular to determine the derivative and the integral
of a polynomial and to use a polynomial to fit a given set
of data points.
</p>

<p><b>Copyright &copy; 2004-2016, Modelica Association and DLR.</b></p>

<p><i>
This package is <b>free</b> software. It can be redistributed and/or modified
under the terms of the <b>Modelica license</b>, see the license conditions
and the accompanying <b>disclaimer</b> in the documentation of package
Modelica in file \"Modelica/package.mo\".
</i>
</p>

</html>",         revisions="<html>
<ul>
<li><i>Oct. 22, 2004</i> by Martin Otter (DLR):<br>
       Renamed functions to not have abbreviations.<br>
       Based fitting on LAPACK<br>
       New function to return the polynomial of an indefinite integral</li>
<li><i>Sept. 3, 2004</i> by Jonas Eborn (Scynamics):<br>
       polyderval, polyintval added</li>
<li><i>March 1, 2004</i> by Martin Otter (DLR):<br>
       first version implemented</li>
</ul>
</html>"));
        end Polynomials_Temp;
      annotation(Documentation(info="<html>
<p>
This is the base package for medium models of incompressible fluids based on
tables. The minimal data to provide for a useful medium description is tables
of density and heat capacity as functions of temperature.
</p>

<p>It should be noted that incompressible media only have 1 state per control volume (usually T),
but have both T and p as inputs for fully correct properties. The error of using only T-dependent
properties is small, therefore a Boolean flag enthalpyOfT exists. If it is true, the
enumeration Choices.independentVariables  is set to  Choices.independentVariables.T otherwise
it is set to Choices.independentVariables.pT.</p>

<h4>Using the package TableBased</h4>
<p>
To implement a new medium model, create a package that <b>extends</b> TableBased
and provides one or more of the constant tables:
</p>

<pre>
tableDensity        = [T, d];
tableHeatCapacity   = [T, Cp];
tableConductivity   = [T, lam];
tableViscosity      = [T, eta];
tableVaporPressure  = [T, pVap];
</pre>

<p>
The table data is used to fit constant polynomials of order <b>npol</b>, the
temperature data points do not need to be same for different properties. Properties
like enthalpy, inner energy and entropy are calculated consistently from integrals
and derivatives of d(T) and Cp(T). The minimal
data for a useful medium model is thus density and heat capacity. Transport
properties and vapor pressure are optional, if the data tables are empty the corresponding
function calls can not be used.
</p>
</html>"));
      end TableBased;
      annotation (
        Documentation(info="<html>
<h4>Incompressible media package</h4>
<p>
This package provides a structure and examples of how to create simple
medium models of incompressible fluids, meaning fluids with very little
pressure influence on density. The medium properties is typically described
in terms of tables, functions or polynomial coefficients.
</p>
<h4>Definitions</h4>
<p>
The common meaning of <em>incompressible</em> is that properties like density
and enthalpy are independent of pressure. Thus properties are conveniently
described as functions of temperature, e.g., as polynomials density(T) and cp(T).
However, enthalpy can not be independent of pressure since h = u - p/d. For liquids
it is anyway
common to neglect this dependence since for constant density the neglected term
is (p - p0)/d, which in comparison with cp is very small for most liquids. For
water, the equivalent change of temperature to increasing pressure 1 bar is
0.025 Kelvin.
</p>
<p>
Two Boolean flags are used to choose how enthalpy and inner energy is calculated:
</p>
<ul>
<li><b>enthalpyOfT</b>=true, means assuming that enthalpy is only a function
of temperature, neglecting the pressure dependent term.</li>
<li><b>singleState</b>=true, means also neglect the pressure influence on inner
energy, which makes all medium properties pure functions of temperature.</li>
</ul>
<p>
The default setting for both these flags is true, which enables the simulation tool
to choose temperature as the only medium state and avoids non-linear equation
systems, see the section about
<a href=\"modelica://Modelica.Media.UsersGuide.MediumDefinition.StaticStateSelection\">Static
state selection</a> in the Modelica.Media User's Guide.
</p>

<h4>Contents</h4>
<p>
Currently, the package contains the following parts:
</p>
<ol>
<li> <a href=\"modelica://Modelica.Media.Incompressible.TableBased\">
      Table based medium models</a></li>
<li> <a href=\"modelica://Modelica.Media.Incompressible.Examples\">
      Example medium models</a></li>
</ol>

<p>
A few examples are given in the Examples package. The model
<a href=\"modelica://Modelica.Media.Incompressible.Examples.Glycol47\">
Examples.Glycol47</a> shows how the medium models can be used. For more
realistic examples of how to implement volume models with medium properties
look in the <a href=\"modelica://Modelica.Media.UsersGuide.MediumUsage\">Medium
usage section</a> of the User's Guide.
</p>

</html>"));
    end Incompressible;
  annotation (preferredView="info",Documentation(info="<html>
<p>
This library contains <a href=\"modelica://Modelica.Media.Interfaces\">interface</a>
definitions for media and the following <b>property</b> models for
single and multiple substance fluids with one and multiple phases:
</p>
<ul>
<li> <a href=\"modelica://Modelica.Media.IdealGases\">Ideal gases:</a><br>
     1241 high precision gas models based on the
     NASA Glenn coefficients, plus ideal gas mixture models based
     on the same data.</li>
<li> <a href=\"modelica://Modelica.Media.Water\">Water models:</a><br>
     ConstantPropertyLiquidWater, WaterIF97 (high precision
     water model according to the IAPWS/IF97 standard)</li>
<li> <a href=\"modelica://Modelica.Media.Air\">Air models:</a><br>
     SimpleAir, DryAirNasa, ReferenceAir, MoistAir, ReferenceMoistAir.</li>
<li> <a href=\"modelica://Modelica.Media.Incompressible\">
     Incompressible media:</a><br>
     TableBased incompressible fluid models (properties are defined by tables rho(T),
     HeatCapacity_cp(T), etc.)</li>
<li> <a href=\"modelica://Modelica.Media.CompressibleLiquids\">
     Compressible liquids:</a><br>
     Simple liquid models with linear compressibility</li>
<li> <a href=\"modelica://Modelica.Media.R134a\">Refrigerant Tetrafluoroethane (R134a)</a>.</li>
</ul>
<p>
The following parts are useful, when newly starting with this library:
<ul>
<li> <a href=\"modelica://Modelica.Media.UsersGuide\">Modelica.Media.UsersGuide</a>.</li>
<li> <a href=\"modelica://Modelica.Media.UsersGuide.MediumUsage\">Modelica.Media.UsersGuide.MediumUsage</a>
     describes how to use a medium model in a component model.</li>
<li> <a href=\"modelica://Modelica.Media.UsersGuide.MediumDefinition\">
     Modelica.Media.UsersGuide.MediumDefinition</a>
     describes how a new fluid medium model has to be implemented.</li>
<li> <a href=\"modelica://Modelica.Media.UsersGuide.ReleaseNotes\">Modelica.Media.UsersGuide.ReleaseNotes</a>
     summarizes the changes of the library releases.</li>
<li> <a href=\"modelica://Modelica.Media.Examples\">Modelica.Media.Examples</a>
     contains examples that demonstrate the usage of this library.</li>
</ul>
<p>
Copyright &copy; 1998-2016, Modelica Association.
</p>
<p>
<i>This Modelica package is <u>free</u> software and the use is completely at <u>your own risk</u>; it can be redistributed and/or modified under the terms of the Modelica License 2. For license conditions (including the disclaimer of warranty) see <a href=\"modelica://Modelica.UsersGuide.ModelicaLicense2\">Modelica.UsersGuide.ModelicaLicense2</a> or visit <a href=\"https://www.modelica.org/licenses/ModelicaLicense2\"> https://www.modelica.org/licenses/ModelicaLicense2</a>.</i>
</p>
</html>",   revisions="<html>
<ul>
<li><i>May 16, 2013</i> by Stefan Wischhusen (XRG Simulation):<br/>
    Added new media models Air.ReferenceMoistAir, Air.ReferenceAir, R134a.</li>
<li><i>May 25, 2011</i> by Francesco Casella:<br/>Added min/max attributes to Water, TableBased, MixtureGasNasa, SimpleAir and MoistAir local types.</li>
<li><i>May 25, 2011</i> by Stefan Wischhusen:<br/>Added individual settings for polynomial fittings of properties.</li>
</ul>
</html>"),
      Icon(coordinateSystem(preserveAspectRatio=false, extent={{-100,-100},{100,100}}),
          graphics={
          Line(
            points = {{-76,-80},{-62,-30},{-32,40},{4,66},{48,66},{73,45},{62,-8},{48,-50},{38,-80}},
            color={64,64,64},
            smooth=Smooth.Bezier),
          Line(
            points={{-40,20},{68,20}},
            color={175,175,175}),
          Line(
            points={{-40,20},{-44,88},{-44,88}},
            color={175,175,175}),
          Line(
            points={{68,20},{86,-58}},
            color={175,175,175}),
          Line(
            points={{-60,-28},{56,-28}},
            color={175,175,175}),
          Line(
            points={{-60,-28},{-74,84},{-74,84}},
            color={175,175,175}),
          Line(
            points={{56,-28},{70,-80}},
            color={175,175,175}),
          Line(
            points={{-76,-80},{38,-80}},
            color={175,175,175}),
          Line(
            points={{-76,-80},{-94,-16},{-94,-16}},
            color={175,175,175})}));
  end Media;

  package Math
  "Library of mathematical functions (e.g., sin, cos) and of functions operating on vectors and matrices"
  import SI = Modelica.SIunits;
  extends Modelica.Icons.Package;

  package Matrices "Library of functions operating on matrices"
    extends Modelica.Icons.Package;

    function leastSquares
      "Solve linear equation A*x = b (exactly if possible, or otherwise in a least square sense; A may be non-square and may be rank deficient)"
      extends Modelica.Icons.Function;
      input Real A[:, :] "Matrix A";
      input Real b[size(A, 1)] "Vector b";
      input Real rcond=100*Modelica.Constants.eps
        "Reciprocal condition number to estimate the rank of A";
      output Real x[size(A, 2)]
        "Vector x such that min|A*x-b|^2 if size(A,1) >= size(A,2) or min|x|^2 and A*x=b, if size(A,1) < size(A,2)";
      output Integer rank "Rank of A";
  protected
      Integer info;
      Real xx[max(size(A, 1), size(A, 2))];
    algorithm
      if min(size(A)) > 0 then
        (xx,info,rank) := LAPACK.dgelsy_vec(
              A,
              b,
              rcond);
        x := xx[1:size(A, 2)];
        assert(info == 0,
          "Solving an overdetermined or underdetermined linear system\n" +
          "of equations with function \"Matrices.leastSquares\" failed.");
      else
        x := fill(0.0, size(A, 2));
      end if;
      annotation (Documentation(info="<html>
<h4>Syntax</h4>
<blockquote><pre>
x = Matrices.<b>leastSquares</b>(A,b);
</pre></blockquote>
<h4>Description</h4>
<p>
Returns a solution of equation A*x = b in a least
square sense (A may be rank deficient):
</p>
<pre>
  minimize | A*x - b |
</pre>

<p>
Several different cases can be distinguished (note, <b>rank</b> is an
output argument of this function):
</p>

<p>
<b>size(A,1) = size(A,2)</b>
</p>

<p> A solution is returned for a regular, as well as a singular matrix A:
</p>

<ul>
<li> <b>rank</b> = size(A,1):<br>
     A is <b>regular</b> and the returned solution x fulfills the equation
     A*x = b uniquely.</li>

<li> <b>rank</b> &lt; size(A,1):<br>
     A is <b>singular</b> and no unique solution for equation A*x = b exists.
     <ul>
     <li>  If an infinite number of solutions exists, the one is selected that fulfills
           the equation and at the same time has the minimum norm |x| for all solution
           vectors that fulfill the equation.</li>
     <li>  If no solution exists, x is selected such that |A*x - b| is as small as
           possible (but A*x - b is not zero).</li>
     </ul></li>
</ul>

<p>
<b>size(A,1) &gt; size(A,2):</b>
</p>

<p>
The equation A*x = b has no unique solution. The solution x is selected such that
|A*x - b| is as small as possible. If rank = size(A,2), this minimum norm solution is
unique. If rank &lt; size(A,2), there are an infinite number of solutions leading to the
same minimum value of |A*x - b|. From these infinite number of solutions, the one with the
minimum norm |x| is selected. This gives a unique solution that minimizes both
|A*x - b| and |x|.
</p>

<p>
<b>size(A,1) &lt; size(A,2):</b>
</p>

<ul>
<li> <b>rank</b> = size(A,1):<br>
     There are an infinite number of solutions that fulfill the equation A*x = b.
     From this infinite number, the unique solution is selected that minimizes |x|.
     </li>

<li> <b>rank</b> &lt; size(A,1):<br>
     There is either no solution of equation A*x = b, or there are again an infinite
     number of solutions. The unique solution x is returned that minimizes
      both |A*x - b| and |x|.</li>
</ul>

<p>
Note, the solution is computed with the LAPACK function \"dgelsy\",
i.e., QR or LQ factorization of A with column pivoting.
</p>

<h4>Algorithmic details</h4>

<p>
The function first computes a QR factorization with column pivoting:
</p>

<pre>
      A * P = Q * [ R11 R12 ]
                  [  0  R22 ]
</pre>

<p>
with R11 defined as the largest leading submatrix whose estimated
condition number is less than 1/rcond.  The order of R11, <b>rank</b>,
is the effective rank of A.
</p>

<p>
Then, R22 is considered to be negligible, and R12 is annihilated
by orthogonal transformations from the right, arriving at the
complete orthogonal factorization:
</p>

<pre>
     A * P = Q * [ T11 0 ] * Z
                 [  0  0 ]
</pre>

<p>
The minimum-norm solution is then
</p>

<pre>
     x = P * Z' [ inv(T11)*Q1'*b ]
                [        0       ]
</pre>

<p>
where Q1 consists of the first \"rank\" columns of Q.
</p>

<h4>See also</h4>

<p>
<a href=\"modelica://Modelica.Math.Matrices.leastSquares2\">Matrices.leastSquares2</a>
(same as leastSquares, but with a right hand side matrix), <br>
<a href=\"modelica://Modelica.Math.Matrices.solve\">Matrices.solve</a>
(for square, regular matrices A)
</p>

</html>"));
    end leastSquares;

    package LAPACK
    "Interface to LAPACK library (should usually not directly be used but only indirectly via Modelica.Math.Matrices)"
      extends Modelica.Icons.Package;

      function dgelsy_vec
        "Computes the minimum-norm solution to a real linear least squares problem with rank deficient A"

        extends Modelica.Icons.Function;
        input Real A[:, :];
        input Real b[size(A, 1)];
        input Real rcond=0.0 "Reciprocal condition number to estimate rank";
        output Real x[max(size(A, 1), size(A, 2))]=cat(
                  1,
                  b,
                  zeros(max(nrow, ncol) - nrow))
          "solution is in first size(A,2) rows";
        output Integer info;
        output Integer rank "Effective rank of A";
    protected
        Integer nrow=size(A, 1);
        Integer ncol=size(A, 2);
        Integer nx=max(nrow, ncol);
        Integer lwork=max(min(nrow, ncol) + 3*ncol + 1, 2*min(nrow, ncol) + 1);
        Real work[max(min(size(A, 1), size(A, 2)) + 3*size(A, 2) + 1, 2*min(size(A, 1),
          size(A, 2)) + 1)];
        Real Awork[size(A, 1), size(A, 2)]=A;
        Integer jpvt[size(A, 2)]=zeros(ncol);
      external"FORTRAN 77" dgelsy(
                nrow,
                ncol,
                1,
                Awork,
                nrow,
                x,
                nx,
                jpvt,
                rcond,
                rank,
                work,
                lwork,
                info) annotation (Library="lapack");
        annotation (Documentation(info="Lapack documentation
    Purpose
    =======

    DGELSY computes the minimum-norm solution to a real linear least
    squares problem:
        minimize || A * X - B ||
    using a complete orthogonal factorization of A.  A is an M-by-N
    matrix which may be rank-deficient.

    Several right hand side vectors b and solution vectors x can be
    handled in a single call; they are stored as the columns of the
    M-by-NRHS right hand side matrix B and the N-by-NRHS solution
    matrix X.

    The routine first computes a QR factorization with column pivoting:
        A * P = Q * [ R11 R12 ]
                    [  0  R22 ]
    with R11 defined as the largest leading submatrix whose estimated
    condition number is less than 1/RCOND.  The order of R11, RANK,
    is the effective rank of A.

    Then, R22 is considered to be negligible, and R12 is annihilated
    by orthogonal transformations from the right, arriving at the
    complete orthogonal factorization:
       A * P = Q * [ T11 0 ] * Z
                   [  0  0 ]
    The minimum-norm solution is then
       X = P * Z' [ inv(T11)*Q1'*B ]
                  [        0       ]
    where Q1 consists of the first RANK columns of Q.

    This routine is basically identical to the original xGELSX except
    three differences:
      o The call to the subroutine xGEQPF has been substituted by the
        the call to the subroutine xGEQP3. This subroutine is a Blas-3
        version of the QR factorization with column pivoting.
      o Matrix B (the right hand side) is updated with Blas-3.
      o The permutation of matrix B (the right hand side) is faster and
        more simple.

    Arguments
    =========

    M       (input) INTEGER
            The number of rows of the matrix A.  M >= 0.

    N       (input) INTEGER
            The number of columns of the matrix A.  N >= 0.

    NRHS    (input) INTEGER
            The number of right hand sides, i.e., the number of
            columns of matrices B and X. NRHS >= 0.

    A       (input/output) DOUBLE PRECISION array, dimension (LDA,N)
            On entry, the M-by-N matrix A.
            On exit, A has been overwritten by details of its
            complete orthogonal factorization.

    LDA     (input) INTEGER
            The leading dimension of the array A.  LDA >= max(1,M).

    B       (input/output) DOUBLE PRECISION array, dimension (LDB,NRHS)
            On entry, the M-by-NRHS right hand side matrix B.
            On exit, the N-by-NRHS solution matrix X.

    LDB     (input) INTEGER
            The leading dimension of the array B. LDB >= max(1,M,N).

    JPVT    (input/output) INTEGER array, dimension (N)
            On entry, if JPVT(i) .ne. 0, the i-th column of A is permuted
            to the front of AP, otherwise column i is a free column.
            On exit, if JPVT(i) = k, then the i-th column of AP
            was the k-th column of A.

    RCOND   (input) DOUBLE PRECISION
            RCOND is used to determine the effective rank of A, which
            is defined as the order of the largest leading triangular
            submatrix R11 in the QR factorization with pivoting of A,
            whose estimated condition number < 1/RCOND.

    RANK    (output) INTEGER
            The effective rank of A, i.e., the order of the submatrix
            R11.  This is the same as the order of the submatrix T11
            in the complete orthogonal factorization of A.

    WORK    (workspace/output) DOUBLE PRECISION array, dimension (MAX(1,LWORK))
            On exit, if INFO = 0, WORK(1) returns the optimal LWORK.

    LWORK   (input) INTEGER
            The dimension of the array WORK.
            The unblocked strategy requires that:
               LWORK >= MAX( MN+3*N+1, 2*MN+NRHS ),
            where MN = min( M, N ).
            The block algorithm requires that:
               LWORK >= MAX( MN+2*N+NB*(N+1), 2*MN+NB*NRHS ),
            where NB is an upper bound on the blocksize returned
            by ILAENV for the routines DGEQP3, DTZRZF, STZRQF, DORMQR,
            and DORMRZ.

            If LWORK = -1, then a workspace query is assumed; the routine
            only calculates the optimal size of the WORK array, returns
            this value as the first entry of the WORK array, and no error
            message related to LWORK is issued by XERBLA.

    INFO    (output) INTEGER
            = 0: successful exit
            < 0: If INFO = -i, the i-th argument had an illegal value.
"));
      end dgelsy_vec;
      annotation (Documentation(info="<html>
<p>
This package contains external Modelica functions as interface to the
LAPACK library
(<a href=\"http://www.netlib.org/lapack\">http://www.netlib.org/lapack</a>)
that provides FORTRAN subroutines to solve linear algebra
tasks. Usually, these functions are not directly called, but only via
the much more convenient interface of
<a href=\"modelica://Modelica.Math.Matrices\">Modelica.Math.Matrices</a>.
The documentation of the LAPACK functions is a copy of the original
FORTRAN code. The details of LAPACK are described in:
</p>

<dl>
<dt>Anderson E., Bai Z., Bischof C., Blackford S., Demmel J., Dongarra J.,
    Du Croz J., Greenbaum A., Hammarling S., McKenney A., and Sorensen D.:</dt>
<dd> <a href=\"http://www.netlib.org/lapack/lug/lapack_lug.html\">Lapack Users' Guide</a>.
     Third Edition, SIAM, 1999.</dd>
</dl>

<p>
See also <a href=\"http://en.wikipedia.org/wiki/Lapack\">http://en.wikipedia.org/wiki/Lapack</a>.
</p>

<p>
This package contains a direct interface to the LAPACK subroutines
</p>

</html>"));
    end LAPACK;
    annotation (Documentation(info="<html>
<h4>Library content</h4>
<p>
This library provides functions operating on matrices. Below, the
functions are ordered according to categories and a typical
call of the respective function is shown.
Most functions are solely an interface to the external
<a href=\"modelica://Modelica.Math.Matrices.LAPACK\">LAPACK</a> library.
</p>

<p>
Note: A' is a short hand notation of transpose(A):
</p>

<p><b>Basic Information</b></p>
<ul>
<li> <a href=\"modelica://Modelica.Math.Matrices.toString\">toString</a>(A)
     - returns the string representation of matrix A.</li>

<li> <a href=\"modelica://Modelica.Math.Matrices.isEqual\">isEqual</a>(M1, M2)
     - returns true if matrices M1 and M2 have the same size and the same elements.</li>
</ul>

<p><b>Linear Equations</b></p>
<ul>
<li> <a href=\"modelica://Modelica.Math.Matrices.solve\">solve</a>(A,b)
     - returns solution x of the linear equation A*x=b (where b is a vector,
       and A is a square matrix that must be regular).</li>

<li> <a href=\"modelica://Modelica.Math.Matrices.solve2\">solve2</a>(A,B)
     - returns solution X of the linear equation A*X=B (where B is a matrix,
       and A is a square matrix that must be regular)</li>

<li> <a href=\"modelica://Modelica.Math.Matrices.leastSquares\">leastSquares</a>(A,b)
     - returns solution x of the linear equation A*x=b in a least squares sense
       (where b is a vector and A may be non-square and may be rank deficient)</li>

<li> <a href=\"modelica://Modelica.Math.Matrices.leastSquares2\">leastSquares2</a>(A,B)
     - returns solution X of the linear equation A*X=B in a least squares sense
       (where B is a matrix and A may be non-square and may be rank deficient)</li>

<li> <a href=\"modelica://Modelica.Math.Matrices.equalityLeastSquares\">equalityLeastSquares</a>(A,a,B,b)
     - returns solution x of a linear equality constrained least squares problem:
       min|A*x-a|^2 subject to B*x=b</li>

<li> (LU,p,info) = <a href=\"modelica://Modelica.Math.Matrices.LU\">LU</a>(A)
     - returns the LU decomposition with row pivoting of a rectangular matrix A.</li>

<li> <a href=\"modelica://Modelica.Math.Matrices.LU_solve\">LU_solve</a>(LU,p,b)
     - returns solution x of the linear equation L*U*x[p]=b with a b
       vector and an LU decomposition from \"LU(..)\".</li>

<li> <a href=\"modelica://Modelica.Math.Matrices.LU_solve2\">LU_solve2</a>(LU,p,B)
     - returns solution X of the linear equation L*U*X[p,:]=B with a B
       matrix and an LU decomposition from \"LU(..)\".</li>
</ul>

<p><b>Matrix Factorizations</b></p>
<ul>
<li> (eval,evec) = <a href=\"modelica://Modelica.Math.Matrices.eigenValues\">eigenValues</a>(A)
     - returns eigen values \"eval\" and eigen vectors \"evec\" for a real,
       nonsymmetric matrix A in a Real representation.</li>

<li> <a href=\"modelica://Modelica.Math.Matrices.eigenValueMatrix\">eigenValueMatrix</a>(eval)
     - returns real valued block diagonal matrix of the eigenvalues \"eval\" of matrix A.</li>

<li> (sigma,U,VT) = <a href=\"modelica://Modelica.Math.Matrices.singularValues\">singularValues</a>(A)
     - returns singular values \"sigma\" and left and right singular vectors U and VT
       of a rectangular matrix A.</li>

<li> (Q,R,p) = <a href=\"modelica://Modelica.Math.Matrices.QR\">QR</a>(A)
     - returns the QR decomposition with column pivoting of a rectangular matrix A
       such that Q*R = A[:,p].</li>

<li> (H,U) = <a href=\"modelica://Modelica.Math.Matrices.hessenberg\">hessenberg</a>(A)
     - returns the upper Hessenberg form H and the orthogonal transformation matrix U
       of a square matrix A such that H = U'*A*U.</li>

<li> <a href=\"modelica://Modelica.Math.Matrices.realSchur\">realSchur</a>(A)
     - returns the real Schur form of a square matrix A.</li>

<li> <a href=\"modelica://Modelica.Math.Matrices.cholesky\">cholesky</a>(A)
     - returns the cholesky factor H of a real symmetric positive definite matrix A so that A = H'*H.</li>

<li> (D,Aimproved) = <a href=\"modelica://Modelica.Math.Matrices.balance\">balance</a>(A)
     - returns an improved form Aimproved of a square matrix A that has a smaller condition as A,
       with Aimproved = inv(diagonal(D))*A*diagonal(D).</li>
</ul>

<p><b>Matrix Properties</b></p>
<ul>
<li> <a href=\"modelica://Modelica.Math.Matrices.trace\">trace</a>(A)
     - returns the trace of square matrix A, i.e., the sum of the diagonal elements.</li>

<li> <a href=\"modelica://Modelica.Math.Matrices.det\">det</a>(A)
     - returns the determinant of square matrix A (using LU decomposition; try to avoid det(..))</li>

<li> <a href=\"modelica://Modelica.Math.Matrices.inv\">inv</a>(A)
     - returns the inverse of square matrix A (try to avoid, use instead \"solve2(..) with B=identity(..))</li>

<li> <a href=\"modelica://Modelica.Math.Matrices.rank\">rank</a>(A)
     - returns the rank of square matrix A (computed with singular value decomposition)</li>

<li> <a href=\"modelica://Modelica.Math.Matrices.conditionNumber\">conditionNumber</a>(A)
     - returns the condition number norm(A)*norm(inv(A)) of a square matrix A in the range 1..&infin;.</li>

<li> <a href=\"modelica://Modelica.Math.Matrices.rcond\">rcond</a>(A)
     - returns the reciprocal condition number 1/conditionNumber(A) of a square matrix A in the range 0..1.</li>

<li> <a href=\"modelica://Modelica.Math.Matrices.norm\">norm</a>(A)
     - returns the 1-, 2-, or infinity-norm of matrix A.</li>

<li> <a href=\"modelica://Modelica.Math.Matrices.frobeniusNorm\">frobeniusNorm</a>(A)
     - returns the Frobenius norm of matrix A.</li>

<li> <a href=\"modelica://Modelica.Math.Matrices.nullSpace\">nullSpace</a>(A)
     - returns the null space of matrix A.</li>
</ul>

<p><b>Matrix Exponentials</b></p>
<ul>
<li> <a href=\"modelica://Modelica.Math.Matrices.exp\">exp</a>(A)
     - returns the exponential e^A of a matrix A by adaptive Taylor series
       expansion with scaling and balancing</li>

<li> (phi, gamma) = <a href=\"modelica://Modelica.Math.Matrices.integralExp\">integralExp</a>(A,B)
     - returns the exponential phi=e^A and the integral gamma=integral(exp(A*t)*dt)*B as needed
       for a discretized system with zero order hold.</li>

<li> (phi, gamma, gamma1) = <a href=\"modelica://Modelica.Math.Matrices.integralExpT\">integralExpT</a>(A,B)
     - returns the exponential phi=e^A, the integral gamma=integral(exp(A*t)*dt)*B,
       and the time-weighted integral gamma1 = integral((T-t)*exp(A*t)*dt)*B as needed
       for a discretized system with first order hold.</li>
</ul>

<p><b>Matrix Equations</b></p>
<ul>
<li> <a href=\"modelica://Modelica.Math.Matrices.continuousLyapunov\">continuousLyapunov</a>(A,C)
     - returns solution X of the continuous-time Lyapunov equation X*A + A'*X = C</li>

<li> <a href=\"modelica://Modelica.Math.Matrices.continuousSylvester\">continuousSylvester</a>(A,B,C)
     - returns solution X of the continuous-time Sylvester equation A*X + X*B = C</li>

<li> <a href=\"modelica://Modelica.Math.Matrices.continuousRiccati\">continuousRiccati</a>(A,B,R,Q)
     - returns solution X of the continuous-time algebraic Riccati equation
       A'*X + X*A - X*B*inv(R)*B'*X + Q = 0</li>

<li> <a href=\"modelica://Modelica.Math.Matrices.discreteLyapunov\">discreteLyapunov</a>(A,C)
     - returns solution X of the discrete-time Lyapunov equation A'*X*A + sgn*X = C</li>

<li> <a href=\"modelica://Modelica.Math.Matrices.discreteSylvester\">discreteSylvester</a>(A,B,C)
     - returns solution X of the discrete-time Sylvester equation A*X*B + sgn*X = C</li>

<li> <a href=\"modelica://Modelica.Math.Matrices.discreteRiccati\">discreteRiccati</a>(A,B,R,Q)
     - returns solution X of the discrete-time algebraic Riccati equation
       A'*X*A - X - A'*X*B*inv(R + B'*X*B)*B'*X*A + Q = 0</li>
</ul>

<p><b>Matrix Manipulation</b></p>
<ul>
<li> <a href=\"modelica://Modelica.Math.Matrices.sort\">sort</a>(M)
     - returns the sorted rows or columns of matrix M in ascending or descending order.</li>

<li> <a href=\"modelica://Modelica.Math.Matrices.flipLeftRight\">flipLeftRight</a>(M)
     - returns matrix M so that the columns of M are flipped in left/right direction.</li>

<li> <a href=\"modelica://Modelica.Math.Matrices.flipUpDown\">flipUpDown</a>(M)
     - returns matrix M so that the rows of M are flipped in up/down direction.</li>
</ul>

<h4>See also</h4>
<a href=\"modelica://Modelica.Math.Vectors\">Vectors</a>

</html>"),   Icon(graphics={
          Rectangle(
            extent={{-60,66},{-30,18}},
            lineColor={95,95,95},
            fillColor={175,175,175},
            fillPattern=FillPattern.Solid),
          Rectangle(
            extent={{28,66},{58,18}},
            lineColor={95,95,95},
            fillColor={175,175,175},
            fillPattern=FillPattern.Solid),
          Rectangle(
            extent={{-60,-18},{-30,-66}},
            lineColor={95,95,95},
            fillColor={175,175,175},
            fillPattern=FillPattern.Solid),
          Rectangle(
            extent={{28,-18},{58,-66}},
            lineColor={95,95,95},
            fillColor={175,175,175},
            fillPattern=FillPattern.Solid)}));
  end Matrices;

  package Icons "Icons for Math"
    extends Modelica.Icons.IconsPackage;

    partial function AxisLeft
      "Basic icon for mathematical function with y-axis on left side"

      annotation (
        Icon(coordinateSystem(preserveAspectRatio=true, extent={{-100,-100},{100,
                100}}), graphics={
            Rectangle(
              extent={{-100,100},{100,-100}},
              lineColor={0,0,0},
              fillColor={255,255,255},
              fillPattern=FillPattern.Solid),
            Line(points={{-80,-80},{-80,68}}, color={192,192,192}),
            Polygon(
              points={{-80,90},{-88,68},{-72,68},{-80,90}},
              lineColor={192,192,192},
              fillColor={192,192,192},
              fillPattern=FillPattern.Solid),
            Text(
              extent={{-150,150},{150,110}},
              textString="%name",
              lineColor={0,0,255})}),
        Diagram(coordinateSystem(preserveAspectRatio=true, extent={{-100,-100},{
                100,100}}), graphics={Line(points={{-80,80},{-88,80}}, color={95,
              95,95}),Line(points={{-80,-80},{-88,-80}}, color={95,95,95}),Line(
              points={{-80,-90},{-80,84}}, color={95,95,95}),Text(
                  extent={{-75,104},{-55,84}},
                  lineColor={95,95,95},
                  textString="y"),Polygon(
                  points={{-80,98},{-86,82},{-74,82},{-80,98}},
                  lineColor={95,95,95},
                  fillColor={95,95,95},
                  fillPattern=FillPattern.Solid)}),
        Documentation(info="<html>
<p>
Icon for a mathematical function, consisting of an y-axis on the left side.
It is expected, that an x-axis is added and a plot of the function.
</p>
</html>"));
    end AxisLeft;

    partial function AxisCenter
      "Basic icon for mathematical function with y-axis in the center"

      annotation (
        Icon(coordinateSystem(preserveAspectRatio=true, extent={{-100,-100},{100,
                100}}), graphics={
            Rectangle(
              extent={{-100,100},{100,-100}},
              lineColor={0,0,0},
              fillColor={255,255,255},
              fillPattern=FillPattern.Solid),
            Line(points={{0,-80},{0,68}}, color={192,192,192}),
            Polygon(
              points={{0,90},{-8,68},{8,68},{0,90}},
              lineColor={192,192,192},
              fillColor={192,192,192},
              fillPattern=FillPattern.Solid),
            Text(
              extent={{-150,150},{150,110}},
              textString="%name",
              lineColor={0,0,255})}),
        Diagram(graphics={Line(points={{0,80},{-8,80}}, color={95,95,95}),Line(
              points={{0,-80},{-8,-80}}, color={95,95,95}),Line(points={{0,-90},{
              0,84}}, color={95,95,95}),Text(
                  extent={{5,104},{25,84}},
                  lineColor={95,95,95},
                  textString="y"),Polygon(
                  points={{0,98},{-6,82},{6,82},{0,98}},
                  lineColor={95,95,95},
                  fillColor={95,95,95},
                  fillPattern=FillPattern.Solid)}),
        Documentation(info="<html>
<p>
Icon for a mathematical function, consisting of an y-axis in the middle.
It is expected, that an x-axis is added and a plot of the function.
</p>
</html>"));
    end AxisCenter;
  end Icons;

  function exp "Exponential, base e"
    extends Modelica.Math.Icons.AxisCenter;
    input Real u;
    output Real y;

  external "builtin" y = exp(u);
    annotation (
      Icon(coordinateSystem(
          preserveAspectRatio=true,
          extent={{-100,-100},{100,100}}), graphics={
          Line(points={{-90,-80.3976},{68,-80.3976}}, color={192,192,192}),
          Polygon(
            points={{90,-80.3976},{68,-72.3976},{68,-88.3976},{90,-80.3976}},
            lineColor={192,192,192},
            fillColor={192,192,192},
            fillPattern=FillPattern.Solid),
          Line(points={{-80,-80},{-31,-77.9},{-6.03,-74},{10.9,-68.4},{23.7,-61},
                {34.2,-51.6},{43,-40.3},{50.3,-27.8},{56.7,-13.5},{62.3,2.23},{
                67.1,18.6},{72,38.2},{76,57.6},{80,80}}),
          Text(
            extent={{-86,50},{-14,2}},
            lineColor={192,192,192},
            textString="exp")}),
      Diagram(coordinateSystem(
          preserveAspectRatio=true,
          extent={{-100,-100},{100,100}}), graphics={Line(points={{-100,-80.3976},{84,-80.3976}},
            color={95,95,95}),Polygon(
              points={{98,-80.3976},{82,-74.3976},{82,-86.3976},{98,-80.3976}},
              lineColor={95,95,95},
              fillColor={95,95,95},
              fillPattern=FillPattern.Solid),Line(
              points={{-80,-80},{-31,-77.9},{-6.03,-74},{10.9,-68.4},{23.7,-61},{
              34.2,-51.6},{43,-40.3},{50.3,-27.8},{56.7,-13.5},{62.3,2.23},{67.1,
              18.6},{72,38.2},{76,57.6},{80,80}},
              color={0,0,255},
              thickness=0.5),Text(
              extent={{-31,72},{-11,88}},
              textString="20",
              lineColor={0,0,255}),Text(
              extent={{-92,-81},{-72,-101}},
              textString="-3",
              lineColor={0,0,255}),Text(
              extent={{66,-81},{86,-101}},
              textString="3",
              lineColor={0,0,255}),Text(
              extent={{2,-69},{22,-89}},
              textString="1",
              lineColor={0,0,255}),Text(
              extent={{78,-54},{98,-74}},
              lineColor={95,95,95},
              textString="u"),Line(
              points={{0,80},{88,80}},
              color={175,175,175}),Line(
              points={{80,84},{80,-84}},
              color={175,175,175})}),
      Documentation(info="<html>
<p>
This function returns y = exp(u), with -&infin; &lt; u &lt; &infin;:
</p>

<p>
<img src=\"modelica://Modelica/Resources/Images/Math/exp.png\">
</p>
</html>"));
  end exp;

  function log "Natural (base e) logarithm (u shall be > 0)"
    extends Modelica.Math.Icons.AxisLeft;
    input Real u;
    output Real y;

  external "builtin" y = log(u);
    annotation (
      Icon(coordinateSystem(
          preserveAspectRatio=true,
          extent={{-100,-100},{100,100}}), graphics={
          Line(points={{-90,0},{68,0}}, color={192,192,192}),
          Polygon(
            points={{90,0},{68,8},{68,-8},{90,0}},
            lineColor={192,192,192},
            fillColor={192,192,192},
            fillPattern=FillPattern.Solid),
          Line(points={{-80,-80},{-79.2,-50.6},{-78.4,-37},{-77.6,-28},{-76.8,-21.3},
                {-75.2,-11.4},{-72.8,-1.31},{-69.5,8.08},{-64.7,17.9},{-57.5,28},
                {-47,38.1},{-31.8,48.1},{-10.1,58},{22.1,68},{68.7,78.1},{80,80}}),
          Text(
            extent={{-6,-24},{66,-72}},
            lineColor={192,192,192},
            textString="log")}),
      Diagram(coordinateSystem(
          preserveAspectRatio=true,
          extent={{-100,-100},{100,100}}), graphics={Line(points={{-100,0},{84,0}}, color={95,95,95}),
            Polygon(
              points={{100,0},{84,6},{84,-6},{100,0}},
              lineColor={95,95,95},
              fillColor={95,95,95},
              fillPattern=FillPattern.Solid),Line(
              points={{-78,-80},{-77.2,-50.6},{-76.4,-37},{-75.6,-28},{-74.8,-21.3},
              {-73.2,-11.4},{-70.8,-1.31},{-67.5,8.08},{-62.7,17.9},{-55.5,28},{-45,
              38.1},{-29.8,48.1},{-8.1,58},{24.1,68},{70.7,78.1},{82,80}},
              color={0,0,255},
              thickness=0.5),Text(
              extent={{-105,72},{-85,88}},
              textString="3",
              lineColor={0,0,255}),Text(
              extent={{60,-3},{80,-23}},
              textString="20",
              lineColor={0,0,255}),Text(
              extent={{-78,-7},{-58,-27}},
              textString="1",
              lineColor={0,0,255}),Text(
              extent={{84,26},{104,6}},
              lineColor={95,95,95},
              textString="u"),Text(
              extent={{-100,9},{-80,-11}},
              textString="0",
              lineColor={0,0,255}),Line(
              points={{-80,80},{84,80}},
              color={175,175,175}),Line(
              points={{82,82},{82,-6}},
              color={175,175,175})}),
      Documentation(info="<html>
<p>
This function returns y = log(10) (the natural logarithm of u),
with u &gt; 0:
</p>

<p>
<img src=\"modelica://Modelica/Resources/Images/Math/log.png\">
</p>
</html>"));
  end log;
  annotation (Icon(coordinateSystem(preserveAspectRatio=true, extent={{-100,-100},
            {100,100}}), graphics={Line(points={{-80,0},{-68.7,34.2},{-61.5,53.1},
              {-55.1,66.4},{-49.4,74.6},{-43.8,79.1},{-38.2,79.8},{-32.6,76.6},{
              -26.9,69.7},{-21.3,59.4},{-14.9,44.1},{-6.83,21.2},{10.1,-30.8},{17.3,
              -50.2},{23.7,-64.2},{29.3,-73.1},{35,-78.4},{40.6,-80},{46.2,-77.6},
              {51.9,-71.5},{57.5,-61.9},{63.9,-47.2},{72,-24.8},{80,0}}, color={
              0,0,0}, smooth=Smooth.Bezier)}), Documentation(info="<html>
<p>
This package contains <b>basic mathematical functions</b> (such as sin(..)),
as well as functions operating on
<a href=\"modelica://Modelica.Math.Vectors\">vectors</a>,
<a href=\"modelica://Modelica.Math.Matrices\">matrices</a>,
<a href=\"modelica://Modelica.Math.Nonlinear\">nonlinear functions</a>, and
<a href=\"modelica://Modelica.Math.BooleanVectors\">Boolean vectors</a>.
</p>

<dl>
<dt><b>Main Authors:</b></dt>
<dd><a href=\"http://www.robotic.dlr.de/Martin.Otter/\">Martin Otter</a> and
    Marcus Baur<br>
    Deutsches Zentrum f&uuml;r Luft und Raumfahrt e.V. (DLR)<br>
    Institut f&uuml;r Robotik und Mechatronik<br>
    Postfach 1116<br>
    D-82230 Wessling<br>
    Germany<br>
    email: <A HREF=\"mailto:Martin.Otter@dlr.de\">Martin.Otter@dlr.de</A><br></dd>
</dl>

<p>
Copyright &copy; 1998-2016, Modelica Association and DLR.
</p>
<p>
<i>This Modelica package is <u>free</u> software and the use is completely at <u>your own risk</u>; it can be redistributed and/or modified under the terms of the Modelica License 2. For license conditions (including the disclaimer of warranty) see <a href=\"modelica://Modelica.UsersGuide.ModelicaLicense2\">Modelica.UsersGuide.ModelicaLicense2</a> or visit <a href=\"https://www.modelica.org/licenses/ModelicaLicense2\"> https://www.modelica.org/licenses/ModelicaLicense2</a>.</i>
</p>
</html>",   revisions="<html>
<ul>
<li><i>October 21, 2002</i>
       by <a href=\"http://www.robotic.dlr.de/Martin.Otter/\">Martin Otter</a>
       and Christian Schweiger:<br>
       Function tempInterpol2 added.</li>
<li><i>Oct. 24, 1999</i>
       by <a href=\"http://www.robotic.dlr.de/Martin.Otter/\">Martin Otter</a>:<br>
       Icons for icon and diagram level introduced.</li>
<li><i>June 30, 1999</i>
       by <a href=\"http://www.robotic.dlr.de/Martin.Otter/\">Martin Otter</a>:<br>
       Realized.</li>
</ul>

</html>"));
  end Math;

  package Utilities
  "Library of utility functions dedicated to scripting (operating on files, streams, strings, system)"
    extends Modelica.Icons.Package;

    package Streams "Read from files and write to files"
      extends Modelica.Icons.Package;

      function error "Print error message and cancel all actions"
        extends Modelica.Icons.Function;
        input String string "String to be printed to error message window";
        external "C" ModelicaError(string) annotation(Library="ModelicaExternalC");
        annotation (Documentation(info="<html>
<h4>Syntax</h4>
<blockquote><pre>
Streams.<b>error</b>(string);
</pre></blockquote>
<h4>Description</h4>
<p>
Print the string \"string\" as error message and
cancel all actions. Line breaks are characterized
by \"\\n\" in the string.
</p>
<h4>Example</h4>
<blockquote><pre>
  Streams.error(\"x (= \" + String(x) + \")\\nhas to be in the range 0 .. 1\");
</pre></blockquote>
<h4>See also</h4>
<p>
<a href=\"modelica://Modelica.Utilities.Streams\">Streams</a>,
<a href=\"modelica://Modelica.Utilities.Streams.print\">Streams.print</a>,
<a href=\"modelica://ModelicaReference.Operators.'String()'\">ModelicaReference.Operators.'String()'</a>
</p>
</html>"));
      end error;
      annotation (
        Documentation(info="<html>
<h4>Library content</h4>
<p>
Package <b>Streams</b> contains functions to input and output strings
to a message window or on files, as well as reading matrices from file
and writing matrices to file. Note that a string is interpreted
and displayed as html text (e.g., with print(..) or error(..))
if it is enclosed with the Modelica html quotation, e.g.,
</p>
<blockquote><p>
string = \"&lt;html&gt; first line &lt;br&gt; second line &lt;/html&gt;\".
</p></blockquote>
<p>
It is a quality of implementation, whether (a) all tags of html are supported
or only a subset, (b) how html tags are interpreted if the output device
does not allow to display formatted text.
</p>
<p>
In the table below an example call to every function is given:
</p>
<table border=1 cellspacing=0 cellpadding=2>
  <tr><th><b><i>Function/type</i></b></th><th><b><i>Description</i></b></th></tr>
  <tr><td valign=\"top\"><a href=\"modelica://Modelica.Utilities.Streams.print\">print</a>(string)<br>
          <a href=\"modelica://Modelica.Utilities.Streams.print\">print</a>(string,fileName)</td>
      <td valign=\"top\"> Print string \"string\" or vector of strings to message window or on
           file \"fileName\".</td>
  </tr>
  <tr><td valign=\"top\">stringVector =
         <a href=\"modelica://Modelica.Utilities.Streams.readFile\">readFile</a>(fileName)</td>
      <td valign=\"top\"> Read complete text file and return it as a vector of strings.</td>
  </tr>
  <tr><td valign=\"top\">(string, endOfFile) =
         <a href=\"modelica://Modelica.Utilities.Streams.readLine\">readLine</a>(fileName, lineNumber)</td>
      <td valign=\"top\">Returns from the file the content of line lineNumber.</td>
  </tr>
  <tr><td valign=\"top\">lines =
         <a href=\"modelica://Modelica.Utilities.Streams.countLines\">countLines</a>(fileName)</td>
      <td valign=\"top\">Returns the number of lines in a file.</td>
  </tr>
  <tr><td valign=\"top\"><a href=\"modelica://Modelica.Utilities.Streams.error\">error</a>(string)</td>
      <td valign=\"top\"> Print error message \"string\" to message window
           and cancel all actions</td>
  </tr>
  <tr><td valign=\"top\"><a href=\"modelica://Modelica.Utilities.Streams.close\">close</a>(fileName)</td>
      <td valign=\"top\"> Close file if it is still open. Ignore call if
           file is already closed or does not exist. </td>
  </tr>
  <tr><td valign=\"top\"><a href=\"modelica://Modelica.Utilities.Streams.readMatrixSize\">readMatrixSize</a>(fileName, matrixName)</td>
      <td valign=\"top\"> Read dimensions of a Real matrix from a MATLAB MAT file. </td></tr>
  <tr><td valign=\"top\"><a href=\"modelica://Modelica.Utilities.Streams.readRealMatrix\">readRealMatrix</a>(fileName, matrixName, nrow, ncol)</td>
      <td valign=\"top\"> Read a Real matrix from a MATLAB MAT file. </td></tr>
  <tr><td valign=\"top\"><a href=\"modelica://Modelica.Utilities.Streams.writeRealMatrix\">writeRealMatrix</a>(fileName, matrixName, matrix, append, format)</td>
      <td valign=\"top\"> Write Real matrix to a MATLAB MAT file. </td></tr>
</table>
<p>
Use functions <b>scanXXX</b> from package
<a href=\"modelica://Modelica.Utilities.Strings\">Strings</a>
to parse a string.
</p>
<p>
If Real, Integer or Boolean values shall be printed
or used in an error message, they have to be first converted
to strings with the builtin operator
<a href=\"modelica://ModelicaReference.Operators.'String()'\">ModelicaReference.Operators.'String()'</a>(...).
Example:
</p>
<pre>
  <b>if</b> x &lt; 0 <b>or</b> x &gt; 1 <b>then</b>
     Streams.error(\"x (= \" + String(x) + \") has to be in the range 0 .. 1\");
  <b>end if</b>;
</pre>
</html>"));
    end Streams;
      annotation (
  Icon(coordinateSystem(extent={{-100.0,-100.0},{100.0,100.0}}), graphics={
      Polygon(
        origin={1.3835,-4.1418},
        rotation=45.0,
        fillColor={64,64,64},
        pattern=LinePattern.None,
        fillPattern=FillPattern.Solid,
        points={{-15.0,93.333},{-15.0,68.333},{0.0,58.333},{15.0,68.333},{15.0,93.333},{20.0,93.333},{25.0,83.333},{25.0,58.333},{10.0,43.333},{10.0,-41.667},{25.0,-56.667},{25.0,-76.667},{10.0,-91.667},{0.0,-91.667},{0.0,-81.667},{5.0,-81.667},{15.0,-71.667},{15.0,-61.667},{5.0,-51.667},{-5.0,-51.667},{-15.0,-61.667},{-15.0,-71.667},{-5.0,-81.667},{0.0,-81.667},{0.0,-91.667},{-10.0,-91.667},{-25.0,-76.667},{-25.0,-56.667},{-10.0,-41.667},{-10.0,43.333},{-25.0,58.333},{-25.0,83.333},{-20.0,93.333}}),
      Polygon(
        origin={10.1018,5.218},
        rotation=-45.0,
        fillColor={255,255,255},
        fillPattern=FillPattern.Solid,
        points={{-15.0,87.273},{15.0,87.273},{20.0,82.273},{20.0,27.273},{10.0,17.273},{10.0,7.273},{20.0,2.273},{20.0,-2.727},{5.0,-2.727},{5.0,-77.727},{10.0,-87.727},{5.0,-112.727},{-5.0,-112.727},{-10.0,-87.727},{-5.0,-77.727},{-5.0,-2.727},{-20.0,-2.727},{-20.0,2.273},{-10.0,7.273},{-10.0,17.273},{-20.0,27.273},{-20.0,82.273}})}),
  Documentation(info="<html>
<p>
This package contains Modelica <b>functions</b> that are
especially suited for <b>scripting</b>. The functions might
be used to work with strings, read data from file, write data
to file or copy, move and remove files.
</p>
<p>
For an introduction, have especially a look at:
</p>
<ul>
<li> <a href=\"modelica://Modelica.Utilities.UsersGuide\">Modelica.Utilities.User's Guide</a>
     discusses the most important aspects of this library.</li>
<li> <a href=\"modelica://Modelica.Utilities.Examples\">Modelica.Utilities.Examples</a>
     contains examples that demonstrate the usage of this library.</li>
</ul>
<p>
The following main sublibraries are available:
</p>
<ul>
<li> <a href=\"modelica://Modelica.Utilities.Files\">Files</a>
     provides functions to operate on files and directories, e.g.,
     to copy, move, remove files.</li>
<li> <a href=\"modelica://Modelica.Utilities.Streams\">Streams</a>
     provides functions to read from files and write to files.</li>
<li> <a href=\"modelica://Modelica.Utilities.Strings\">Strings</a>
     provides functions to operate on strings. E.g.
     substring, find, replace, sort, scanToken.</li>
<li> <a href=\"modelica://Modelica.Utilities.System\">System</a>
     provides functions to interact with the environment.
     E.g., get or set the working directory or environment
     variables and to send a command to the default shell.</li>
</ul>

<p>
Copyright &copy; 1998-2016, Modelica Association, DLR, and Dassault Syst&egrave;mes AB.
</p>

<p>
<i>This Modelica package is <u>free</u> software and the use is completely at <u>your own risk</u>; it can be redistributed and/or modified under the terms of the Modelica License 2. For license conditions (including the disclaimer of warranty) see <a href=\"modelica://Modelica.UsersGuide.ModelicaLicense2\">Modelica.UsersGuide.ModelicaLicense2</a> or visit <a href=\"https://www.modelica.org/licenses/ModelicaLicense2\"> https://www.modelica.org/licenses/ModelicaLicense2</a>.</i>
</p>

</html>"));
  end Utilities;

  package Constants
  "Library of mathematical constants and constants of nature (e.g., pi, eps, R, sigma)"
    import SI = Modelica.SIunits;
    import NonSI = Modelica.SIunits.Conversions.NonSIunits;
    extends Modelica.Icons.Package;

    final constant Real eps=ModelicaServices.Machine.eps
      "Biggest number such that 1.0 + eps = 1.0";

    final constant Real small=ModelicaServices.Machine.small
      "Smallest number such that small and -small are representable on the machine";

    final constant Real R(final unit="J/(mol.K)") = 8.3144598
      "Molar gas constant (previous value: 8.314472)";

    final constant NonSI.Temperature_degC T_zero=-273.15
      "Absolute zero temperature";
    annotation (
      Documentation(info="<html>
<p>
This package provides often needed constants from mathematics, machine
dependent constants and constants from nature. The latter constants
(name, value, description) are from the following source:
</p>

<dl>
<dt>Peter J. Mohr, David B. Newell, and Barry N. Taylor:</dt>
<dd><b>CODATA Recommended Values of the Fundamental Physical Constants: 2014</b>.
<a href= \"http://dx.doi.org/10.5281/zenodo.22826\">http://dx.doi.org/10.5281/zenodo.22826</a>, 2015. See also <a href=
\"http://physics.nist.gov/cuu/Constants/index.html\">http://physics.nist.gov/cuu/Constants/index.html</a></dd>
</dl>

<p>CODATA is the Committee on Data for Science and Technology.</p>

<dl>
<dt><b>Main Author:</b></dt>
<dd><a href=\"http://www.robotic.dlr.de/Martin.Otter/\">Martin Otter</a><br>
    Deutsches Zentrum f&uuml;r Luft und Raumfahrt e. V. (DLR)<br>
    Oberpfaffenhofen<br>
    Postfach 1116<br>
    D-82230 We&szlig;ling<br>
    email: <a href=\"mailto:Martin.Otter@dlr.de\">Martin.Otter@dlr.de</a></dd>
</dl>

<p>
Copyright &copy; 1998-2016, Modelica Association and DLR.
</p>
<p>
<i>This Modelica package is <u>free</u> software and the use is completely at <u>your own risk</u>; it can be redistributed and/or modified under the terms of the Modelica License 2. For license conditions (including the disclaimer of warranty) see <a href=\"modelica://Modelica.UsersGuide.ModelicaLicense2\">Modelica.UsersGuide.ModelicaLicense2</a> or visit <a href=\"https://www.modelica.org/licenses/ModelicaLicense2\"> https://www.modelica.org/licenses/ModelicaLicense2</a>.</i>
</p>
</html>",   revisions="<html>
<ul>
<li><i>Nov 4, 2015</i>
       by Thomas Beutlich:<br>
       Constants updated according to 2014 CODATA values.</li>
<li><i>Nov 8, 2004</i>
       by Christian Schweiger:<br>
       Constants updated according to 2002 CODATA values.</li>
<li><i>Dec 9, 1999</i>
       by <a href=\"http://www.robotic.dlr.de/Martin.Otter/\">Martin Otter</a>:<br>
       Constants updated according to 1998 CODATA values. Using names, values
       and description text from this source. Included magnetic and
       electric constant.</li>
<li><i>Sep 18, 1999</i>
       by <a href=\"http://www.robotic.dlr.de/Martin.Otter/\">Martin Otter</a>:<br>
       Constants eps, inf, small introduced.</li>
<li><i>Nov 15, 1997</i>
       by <a href=\"http://www.robotic.dlr.de/Martin.Otter/\">Martin Otter</a>:<br>
       Realized.</li>
</ul>
</html>"),
      Icon(coordinateSystem(extent={{-100.0,-100.0},{100.0,100.0}}), graphics={
        Polygon(
          origin={-9.2597,25.6673},
          fillColor={102,102,102},
          pattern=LinePattern.None,
          fillPattern=FillPattern.Solid,
          points={{48.017,11.336},{48.017,11.336},{10.766,11.336},{-25.684,10.95},{-34.944,-15.111},{-34.944,-15.111},{-32.298,-15.244},{-32.298,-15.244},{-22.112,0.168},{11.292,0.234},{48.267,-0.097},{48.267,-0.097}},
          smooth=Smooth.Bezier),
        Polygon(
          origin={-19.9923,-8.3993},
          fillColor={102,102,102},
          pattern=LinePattern.None,
          fillPattern=FillPattern.Solid,
          points={{3.239,37.343},{3.305,37.343},{-0.399,2.683},{-16.936,-20.071},{-7.808,-28.604},{6.811,-22.519},{9.986,37.145},{9.986,37.145}},
          smooth=Smooth.Bezier),
        Polygon(
          origin={23.753,-11.5422},
          fillColor={102,102,102},
          pattern=LinePattern.None,
          fillPattern=FillPattern.Solid,
          points={{-10.873,41.478},{-10.873,41.478},{-14.048,-4.162},{-9.352,-24.8},{7.912,-24.469},{16.247,0.27},{16.247,0.27},{13.336,0.071},{13.336,0.071},{7.515,-9.983},{-3.134,-7.271},{-2.671,41.214},{-2.671,41.214}},
          smooth=Smooth.Bezier)}));
  end Constants;

  package Icons "Library of icons"
    extends Icons.Package;

    partial package ExamplesPackage
      "Icon for packages containing runnable examples"
      extends Modelica.Icons.Package;
      annotation (Icon(coordinateSystem(preserveAspectRatio=false, extent={{-100,
                -100},{100,100}}), graphics={
            Polygon(
              origin={8.0,14.0},
              lineColor={78,138,73},
              fillColor={78,138,73},
              pattern=LinePattern.None,
              fillPattern=FillPattern.Solid,
              points={{-58.0,46.0},{42.0,-14.0},{-58.0,-74.0},{-58.0,46.0}})}), Documentation(info="<html>
<p>This icon indicates a package that contains executable examples.</p>
</html>"));
    end ExamplesPackage;

    partial model Example "Icon for runnable examples"

      annotation (Icon(coordinateSystem(preserveAspectRatio=false, extent={{-100,-100},{100,100}}), graphics={
            Ellipse(lineColor = {75,138,73},
                    fillColor={255,255,255},
                    fillPattern = FillPattern.Solid,
                    extent = {{-100,-100},{100,100}}),
            Polygon(lineColor = {0,0,255},
                    fillColor = {75,138,73},
                    pattern = LinePattern.None,
                    fillPattern = FillPattern.Solid,
                    points = {{-36,60},{64,0},{-36,-60},{-36,60}})}), Documentation(info="<html>
<p>This icon indicates an example. The play button suggests that the example can be executed.</p>
</html>"));
    end Example;

    partial package Package "Icon for standard packages"

      annotation (Icon(coordinateSystem(preserveAspectRatio=false, extent={{-100,-100},{100,100}}), graphics={
            Rectangle(
              lineColor={200,200,200},
              fillColor={248,248,248},
              fillPattern=FillPattern.HorizontalCylinder,
              extent={{-100.0,-100.0},{100.0,100.0}},
              radius=25.0),
            Rectangle(
              lineColor={128,128,128},
              extent={{-100.0,-100.0},{100.0,100.0}},
              radius=25.0)}),   Documentation(info="<html>
<p>Standard package icon.</p>
</html>"));
    end Package;

    partial package BasesPackage "Icon for packages containing base classes"
      extends Modelica.Icons.Package;
      annotation (Icon(coordinateSystem(preserveAspectRatio=false, extent={{-100,
                -100},{100,100}}), graphics={
            Ellipse(
              extent={{-30.0,-30.0},{30.0,30.0}},
              lineColor={128,128,128},
              fillColor={255,255,255},
              fillPattern=FillPattern.Solid)}),
                                Documentation(info="<html>
<p>This icon shall be used for a package/library that contains base models and classes, respectively.</p>
</html>"));
    end BasesPackage;

    partial package VariantsPackage "Icon for package containing variants"
      extends Modelica.Icons.Package;
      annotation (Icon(coordinateSystem(preserveAspectRatio=true,  extent={{-100,-100},
                {100,100}}),       graphics={
            Ellipse(
              origin={10.0,10.0},
              fillColor={76,76,76},
              pattern=LinePattern.None,
              fillPattern=FillPattern.Solid,
              extent={{-80.0,-80.0},{-20.0,-20.0}}),
            Ellipse(
              origin={10.0,10.0},
              pattern=LinePattern.None,
              fillPattern=FillPattern.Solid,
              extent={{0.0,-80.0},{60.0,-20.0}}),
            Ellipse(
              origin={10.0,10.0},
              fillColor={128,128,128},
              pattern=LinePattern.None,
              fillPattern=FillPattern.Solid,
              extent={{0.0,0.0},{60.0,60.0}}),
            Ellipse(
              origin={10.0,10.0},
              lineColor={128,128,128},
              fillColor={255,255,255},
              fillPattern=FillPattern.Solid,
              extent={{-80.0,0.0},{-20.0,60.0}})}),
                                Documentation(info="<html>
<p>This icon shall be used for a package/library that contains several variants of one component.</p>
</html>"));
    end VariantsPackage;

    partial package InterfacesPackage "Icon for packages containing interfaces"
      extends Modelica.Icons.Package;
      annotation (Icon(coordinateSystem(preserveAspectRatio=false, extent={{-100,
                -100},{100,100}}), graphics={
            Polygon(origin={20.0,0.0},
              lineColor={64,64,64},
              fillColor={255,255,255},
              fillPattern=FillPattern.Solid,
              points={{-10.0,70.0},{10.0,70.0},{40.0,20.0},{80.0,20.0},{80.0,-20.0},{40.0,-20.0},{10.0,-70.0},{-10.0,-70.0}}),
            Polygon(fillColor={102,102,102},
              pattern=LinePattern.None,
              fillPattern=FillPattern.Solid,
              points={{-100.0,20.0},{-60.0,20.0},{-30.0,70.0},{-10.0,70.0},{-10.0,-70.0},{-30.0,-70.0},{-60.0,-20.0},{-100.0,-20.0}})}),
                                Documentation(info="<html>
<p>This icon indicates packages containing interfaces.</p>
</html>"));
    end InterfacesPackage;

    partial package SourcesPackage "Icon for packages containing sources"
      extends Modelica.Icons.Package;
      annotation (Icon(coordinateSystem(preserveAspectRatio=false, extent={{-100,
                -100},{100,100}}), graphics={
            Polygon(origin={23.3333,0.0},
              fillColor={128,128,128},
              pattern=LinePattern.None,
              fillPattern=FillPattern.Solid,
              points={{-23.333,30.0},{46.667,0.0},{-23.333,-30.0}}),
            Rectangle(
              fillColor = {128,128,128},
              pattern = LinePattern.None,
              fillPattern = FillPattern.Solid,
              extent = {{-70,-4.5},{0,4.5}})}),
                                Documentation(info="<html>
<p>This icon indicates a package which contains sources.</p>
</html>"));
    end SourcesPackage;

    partial package IconsPackage "Icon for packages containing icons"
      extends Modelica.Icons.Package;
      annotation (Icon(coordinateSystem(preserveAspectRatio=false, extent={{-100,
                -100},{100,100}}), graphics={Polygon(
              origin={-8.167,-17},
              fillColor={128,128,128},
              pattern=LinePattern.None,
              fillPattern=FillPattern.Solid,
              points={{-15.833,20.0},{-15.833,30.0},{14.167,40.0},{24.167,20.0},{
                  4.167,-30.0},{14.167,-30.0},{24.167,-30.0},{24.167,-40.0},{-5.833,
                  -50.0},{-15.833,-30.0},{4.167,20.0},{-5.833,20.0}},
              smooth=Smooth.Bezier,
              lineColor={0,0,0}), Ellipse(
              origin={-0.5,56.5},
              fillColor={128,128,128},
              pattern=LinePattern.None,
              fillPattern=FillPattern.Solid,
              extent={{-12.5,-12.5},{12.5,12.5}},
              lineColor={0,0,0})}));
    end IconsPackage;

    partial package MaterialPropertiesPackage
      "Icon for package containing property classes"
      extends Modelica.Icons.Package;
      annotation (Icon(coordinateSystem(preserveAspectRatio=false, extent={{-100,
                -100},{100,100}}), graphics={
            Ellipse(
              lineColor={102,102,102},
              fillColor={204,204,204},
              pattern=LinePattern.None,
              fillPattern=FillPattern.Sphere,
              extent={{-60.0,-60.0},{60.0,60.0}})}),
                                Documentation(info="<html>
<p>This icon indicates a package that contains properties</p>
</html>"));
    end MaterialPropertiesPackage;

    partial function Function "Icon for functions"

      annotation (Icon(coordinateSystem(preserveAspectRatio=false, extent={{-100,-100},{100,100}}), graphics={
            Text(
              lineColor={0,0,255},
              extent={{-150,105},{150,145}},
              textString="%name"),
            Ellipse(
              lineColor = {108,88,49},
              fillColor = {255,215,136},
              fillPattern = FillPattern.Solid,
              extent = {{-100,-100},{100,100}}),
            Text(
              lineColor={108,88,49},
              extent={{-90.0,-90.0},{90.0,90.0}},
              textString="f")}),
    Documentation(info="<html>
<p>This icon indicates Modelica functions.</p>
</html>"));
    end Function;

    partial record Record "Icon for records"

      annotation (Icon(coordinateSystem(preserveAspectRatio=true, extent={{-100,-100},{100,100}}), graphics={
            Text(
              lineColor={0,0,255},
              extent={{-150,60},{150,100}},
              textString="%name"),
            Rectangle(
              origin={0.0,-25.0},
              lineColor={64,64,64},
              fillColor={255,215,136},
              fillPattern=FillPattern.Solid,
              extent={{-100.0,-75.0},{100.0,75.0}},
              radius=25.0),
            Line(
              points={{-100.0,0.0},{100.0,0.0}},
              color={64,64,64}),
            Line(
              origin={0.0,-50.0},
              points={{-100.0,0.0},{100.0,0.0}},
              color={64,64,64}),
            Line(
              origin={0.0,-25.0},
              points={{0.0,75.0},{0.0,-75.0}},
              color={64,64,64})}),                        Documentation(info="<html>
<p>
This icon is indicates a record.
</p>
</html>"));
    end Record;
    annotation (Icon(coordinateSystem(preserveAspectRatio=false, extent={{-100,
                -100},{100,100}}), graphics={Polygon(
              origin={-8.167,-17},
              fillColor={128,128,128},
              pattern=LinePattern.None,
              fillPattern=FillPattern.Solid,
              points={{-15.833,20.0},{-15.833,30.0},{14.167,40.0},{24.167,20.0},{
                  4.167,-30.0},{14.167,-30.0},{24.167,-30.0},{24.167,-40.0},{-5.833,
                  -50.0},{-15.833,-30.0},{4.167,20.0},{-5.833,20.0}},
              smooth=Smooth.Bezier,
              lineColor={0,0,0}), Ellipse(
              origin={-0.5,56.5},
              fillColor={128,128,128},
              pattern=LinePattern.None,
              fillPattern=FillPattern.Solid,
              extent={{-12.5,-12.5},{12.5,12.5}},
              lineColor={0,0,0})}), Documentation(info="<html>
<p>This package contains definitions for the graphical layout of components which may be used in different libraries. The icons can be utilized by inheriting them in the desired class using &quot;extends&quot; or by directly copying the &quot;icon&quot; layer. </p>

<h4>Main Authors:</h4>

<dl>
<dt><a href=\"http://www.robotic.dlr.de/Martin.Otter/\">Martin Otter</a></dt>
    <dd>Deutsches Zentrum fuer Luft und Raumfahrt e.V. (DLR)</dd>
    <dd>Oberpfaffenhofen</dd>
    <dd>Postfach 1116</dd>
    <dd>D-82230 Wessling</dd>
    <dd>email: <a href=\"mailto:Martin.Otter@dlr.de\">Martin.Otter@dlr.de</a></dd>
<dt>Christian Kral</dt>

    <dd>  <a href=\"http://christiankral.net/\">Electric Machines, Drives and Systems</a><br>
</dd>
    <dd>1060 Vienna, Austria</dd>
    <dd>email: <a href=\"mailto:dr.christian.kral@gmail.com\">dr.christian.kral@gmail.com</a></dd>
<dt>Johan Andreasson</dt>
    <dd><a href=\"http://www.modelon.se/\">Modelon AB</a></dd>
    <dd>Ideon Science Park</dd>
    <dd>22370 Lund, Sweden</dd>
    <dd>email: <a href=\"mailto:johan.andreasson@modelon.se\">johan.andreasson@modelon.se</a></dd>
</dl>

<p>Copyright &copy; 1998-2016, Modelica Association, DLR, AIT, and Modelon AB. </p>
<p><i>This Modelica package is <b>free</b> software; it can be redistributed and/or modified under the terms of the <b>Modelica license</b>, see the license conditions and the accompanying <b>disclaimer</b> in <a href=\"modelica://Modelica.UsersGuide.ModelicaLicense2\">Modelica.UsersGuide.ModelicaLicense2</a>.</i> </p>
</html>"));
  end Icons;

  package SIunits
  "Library of type and unit definitions based on SI units according to ISO 31-1992"
    extends Modelica.Icons.Package;

    package Icons "Icons for SIunits"
      extends Modelica.Icons.IconsPackage;

      partial function Conversion "Base icon for conversion functions"

        annotation (Icon(coordinateSystem(preserveAspectRatio=true, extent={{-100,
                  -100},{100,100}}), graphics={
              Rectangle(
                extent={{-100,100},{100,-100}},
                lineColor={191,0,0},
                fillColor={255,255,255},
                fillPattern=FillPattern.Solid),
              Line(points={{-90,0},{30,0}}, color={191,0,0}),
              Polygon(
                points={{90,0},{30,20},{30,-20},{90,0}},
                lineColor={191,0,0},
                fillColor={191,0,0},
                fillPattern=FillPattern.Solid),
              Text(
                extent={{-115,155},{115,105}},
                textString="%name",
                lineColor={0,0,255})}));
      end Conversion;
    end Icons;

    package Conversions
    "Conversion functions to/from non SI units and type definitions of non SI units"
      extends Modelica.Icons.Package;

      package NonSIunits "Type definitions of non SI units"
        extends Modelica.Icons.Package;

        type Temperature_degC = Real (final quantity="ThermodynamicTemperature",
              final unit="degC")
          "Absolute temperature in degree Celsius (for relative temperature use SIunits.TemperatureDifference)"
                                                                                                              annotation(absoluteValue=true);

        type Pressure_bar = Real (final quantity="Pressure", final unit="bar")
          "Absolute pressure in bar";
        annotation (Documentation(info="<html>
<p>
This package provides predefined types, such as <b>Angle_deg</b> (angle in
degree), <b>AngularVelocity_rpm</b> (angular velocity in revolutions per
minute) or <b>Temperature_degF</b> (temperature in degree Fahrenheit),
which are in common use but are not part of the international standard on
units according to ISO 31-1992 \"General principles concerning quantities,
units and symbols\" and ISO 1000-1992 \"SI units and recommendations for
the use of their multiples and of certain other units\".</p>
<p>If possible, the types in this package should not be used. Use instead
types of package Modelica.SIunits. For more information on units, see also
the book of Francois Cardarelli <b>Scientific Unit Conversion - A
Practical Guide to Metrication</b> (Springer 1997).</p>
<p>Some units, such as <b>Temperature_degC/Temp_C</b> are both defined in
Modelica.SIunits and in Modelica.Conversions.NonSIunits. The reason is that these
definitions have been placed erroneously in Modelica.SIunits although they
are not SIunits. For backward compatibility, these type definitions are
still kept in Modelica.SIunits.</p>
</html>"),   Icon(coordinateSystem(extent={{-100,-100},{100,100}}), graphics={
        Text(
          origin={15.0,51.8518},
          extent={{-105.0,-86.8518},{75.0,-16.8518}},
          lineColor={0,0,0},
          textString="[km/h]")}));
      end NonSIunits;

      function to_degC "Convert from Kelvin to degCelsius"
        extends Modelica.SIunits.Icons.Conversion;
        input Temperature Kelvin "Kelvin value";
        output NonSIunits.Temperature_degC Celsius "Celsius value";
      algorithm
        Celsius := Kelvin + Modelica.Constants.T_zero;
        annotation (Inline=true,Icon(coordinateSystem(preserveAspectRatio=true, extent={{-100,
                  -100},{100,100}}), graphics={Text(
                extent={{-20,100},{-100,20}},
                lineColor={0,0,0},
                textString="K"), Text(
                extent={{100,-20},{20,-100}},
                lineColor={0,0,0},
                textString="degC")}));
      end to_degC;

      function from_degC "Convert from degCelsius to Kelvin"
        extends Modelica.SIunits.Icons.Conversion;
        input NonSIunits.Temperature_degC Celsius "Celsius value";
        output Temperature Kelvin "Kelvin value";
      algorithm
        Kelvin := Celsius - Modelica.Constants.T_zero;
        annotation (Inline=true,Icon(coordinateSystem(preserveAspectRatio=true, extent={{-100,
                  -100},{100,100}}), graphics={Text(
                extent={{-20,100},{-100,20}},
                lineColor={0,0,0},
                textString="degC"),  Text(
                extent={{100,-20},{20,-100}},
                lineColor={0,0,0},
                textString="K")}));
      end from_degC;

      function to_bar "Convert from Pascal to bar"
        extends Modelica.SIunits.Icons.Conversion;
        input Pressure Pa "Pascal value";
        output NonSIunits.Pressure_bar bar "bar value";
      algorithm
        bar := Pa/1e5;
        annotation (Inline=true,Icon(coordinateSystem(preserveAspectRatio=true, extent={{-100,
                  -100},{100,100}}), graphics={Text(
                extent={{-12,100},{-100,56}},
                lineColor={0,0,0},
                textString="Pa"),     Text(
                extent={{98,-52},{-4,-100}},
                lineColor={0,0,0},
                textString="bar")}));
      end to_bar;
      annotation (                              Documentation(info="<html>
<p>This package provides conversion functions from the non SI Units
defined in package Modelica.SIunits.Conversions.NonSIunits to the
corresponding SI Units defined in package Modelica.SIunits and vice
versa. It is recommended to use these functions in the following
way (note, that all functions have one Real input and one Real output
argument):</p>
<pre>
  <b>import</b> SI = Modelica.SIunits;
  <b>import</b> Modelica.SIunits.Conversions.*;
     ...
  <b>parameter</b> SI.Temperature     T   = from_degC(25);   // convert 25 degree Celsius to Kelvin
  <b>parameter</b> SI.Angle           phi = from_deg(180);   // convert 180 degree to radian
  <b>parameter</b> SI.AngularVelocity w   = from_rpm(3600);  // convert 3600 revolutions per minutes
                                                      // to radian per seconds
</pre>

</html>"));
    end Conversions;

    type Time = Real (final quantity="Time", final unit="s");

    type Velocity = Real (final quantity="Velocity", final unit="m/s");

    type Density = Real (
        final quantity="Density",
        final unit="kg/m3",
        displayUnit="g/cm3",
        min=0.0);

    type Pressure = Real (
        final quantity="Pressure",
        final unit="Pa",
        displayUnit="bar");

    type AbsolutePressure = Pressure (min=0.0, nominal = 1e5);

    type DynamicViscosity = Real (
        final quantity="DynamicViscosity",
        final unit="Pa.s",
        min=0);

    type MassFlowRate = Real (quantity="MassFlowRate", final unit="kg/s");

    type ThermodynamicTemperature = Real (
        final quantity="ThermodynamicTemperature",
        final unit="K",
        min = 0.0,
        start = 288.15,
        nominal = 300,
        displayUnit="degC")
      "Absolute temperature (use type TemperatureDifference for relative temperatures)"                   annotation(absoluteValue=true);

    type Temperature = ThermodynamicTemperature;

    type TemperatureDifference = Real (
        final quantity="ThermodynamicTemperature",
        final unit="K") annotation(absoluteValue=false);

    type Compressibility = Real (final quantity="Compressibility", final unit=
            "1/Pa");

    type IsothermalCompressibility = Compressibility;

    type ThermalConductivity = Real (final quantity="ThermalConductivity", final unit=
               "W/(m.K)");

    type SpecificHeatCapacity = Real (final quantity="SpecificHeatCapacity",
          final unit="J/(kg.K)");

    type RatioOfSpecificHeatCapacities = Real (final quantity=
            "RatioOfSpecificHeatCapacities", final unit="1");

    type SpecificEntropy = Real (final quantity="SpecificEntropy",
                                 final unit="J/(kg.K)");

    type SpecificEnergy = Real (final quantity="SpecificEnergy",
                                final unit="J/kg");

    type SpecificEnthalpy = SpecificEnergy;

    type DerDensityByEnthalpy = Real (final unit="kg.s2/m5");

    type DerDensityByPressure = Real (final unit="s2/m2");

    type DerDensityByTemperature = Real (final unit="kg/(m3.K)");

    type MolarMass = Real (final quantity="MolarMass", final unit="kg/mol",min=0);

    type MolarVolume = Real (final quantity="MolarVolume", final unit="m3/mol", min=0);

    type MassFraction = Real (final quantity="MassFraction", final unit="1",
                              min=0, max=1);

    type MoleFraction = Real (final quantity="MoleFraction", final unit="1",
                              min = 0, max = 1);

    type PrandtlNumber = Real (final quantity="PrandtlNumber", final unit="1");
    annotation (Icon(coordinateSystem(preserveAspectRatio=false, extent={{-100,
              -100},{100,100}}), graphics={
          Line(
            points={{-66,78},{-66,-40}},
            color={64,64,64}),
          Ellipse(
            extent={{12,36},{68,-38}},
            lineColor={64,64,64},
            fillColor={175,175,175},
            fillPattern=FillPattern.Solid),
          Rectangle(
            extent={{-74,78},{-66,-40}},
            lineColor={64,64,64},
            fillColor={175,175,175},
            fillPattern=FillPattern.Solid),
          Polygon(
            points={{-66,-4},{-66,6},{-16,56},{-16,46},{-66,-4}},
            lineColor={64,64,64},
            fillColor={175,175,175},
            fillPattern=FillPattern.Solid),
          Polygon(
            points={{-46,16},{-40,22},{-2,-40},{-10,-40},{-46,16}},
            lineColor={64,64,64},
            fillColor={175,175,175},
            fillPattern=FillPattern.Solid),
          Ellipse(
            extent={{22,26},{58,-28}},
            lineColor={64,64,64},
            fillColor={255,255,255},
            fillPattern=FillPattern.Solid),
          Polygon(
            points={{68,2},{68,-46},{64,-60},{58,-68},{48,-72},{18,-72},{18,-64},
                {46,-64},{54,-60},{58,-54},{60,-46},{60,-26},{64,-20},{68,-6},{68,
                2}},
            lineColor={64,64,64},
            smooth=Smooth.Bezier,
            fillColor={175,175,175},
            fillPattern=FillPattern.Solid)}), Documentation(info="<html>
<p>This package provides predefined types, such as <i>Mass</i>,
<i>Angle</i>, <i>Time</i>, based on the international standard
on units, e.g.,
</p>

<pre>   <b>type</b> Angle = Real(<b>final</b> quantity = \"Angle\",
                     <b>final</b> unit     = \"rad\",
                     displayUnit    = \"deg\");
</pre>

<p>
Some of the types are derived SI units that are utilized in package Modelica
(such as ComplexCurrent, which is a complex number where both the real and imaginary
part have the SI unit Ampere).
</p>

<p>
Furthermore, conversion functions from non SI-units to SI-units and vice versa
are provided in subpackage
<a href=\"modelica://Modelica.SIunits.Conversions\">Conversions</a>.
</p>

<p>
For an introduction how units are used in the Modelica standard library
with package SIunits, have a look at:
<a href=\"modelica://Modelica.SIunits.UsersGuide.HowToUseSIunits\">How to use SIunits</a>.
</p>

<p>
Copyright &copy; 1998-2016, Modelica Association and DLR.
</p>
<p>
<i>This Modelica package is <u>free</u> software and the use is completely at <u>your own risk</u>; it can be redistributed and/or modified under the terms of the Modelica License 2. For license conditions (including the disclaimer of warranty) see <a href=\"modelica://Modelica.UsersGuide.ModelicaLicense2\">Modelica.UsersGuide.ModelicaLicense2</a> or visit <a href=\"https://www.modelica.org/licenses/ModelicaLicense2\"> https://www.modelica.org/licenses/ModelicaLicense2</a>.</i>
</p>
</html>",   revisions="<html>
<ul>
<li><i>May 25, 2011</i> by Stefan Wischhusen:<br/>Added molar units for energy and enthalpy.</li>
<li><i>Jan. 27, 2010</i> by Christian Kral:<br/>Added complex units.</li>
<li><i>Dec. 14, 2005</i> by <a href=\"http://www.robotic.dlr.de/Martin.Otter/\">Martin Otter</a>:<br/>Add User&#39;;s Guide and removed &quot;min&quot; values for Resistance and Conductance.</li>
<li><i>October 21, 2002</i> by <a href=\"http://www.robotic.dlr.de/Martin.Otter/\">Martin Otter</a> and Christian Schweiger:<br/>Added new package <b>Conversions</b>. Corrected typo <i>Wavelenght</i>.</li>
<li><i>June 6, 2000</i> by <a href=\"http://www.robotic.dlr.de/Martin.Otter/\">Martin Otter</a>:<br/>Introduced the following new types<br/>type Temperature = ThermodynamicTemperature;<br/>types DerDensityByEnthalpy, DerDensityByPressure, DerDensityByTemperature, DerEnthalpyByPressure, DerEnergyByDensity, DerEnergyByPressure<br/>Attribute &quot;final&quot; removed from min and max values in order that these values can still be changed to narrow the allowed range of values.<br/>Quantity=&quot;Stress&quot; removed from type &quot;Stress&quot;, in order that a type &quot;Stress&quot; can be connected to a type &quot;Pressure&quot;.</li>
<li><i>Oct. 27, 1999</i> by <a href=\"http://www.robotic.dlr.de/Martin.Otter/\">Martin Otter</a>:<br/>New types due to electrical library: Transconductance, InversePotential, Damping.</li>
<li><i>Sept. 18, 1999</i> by <a href=\"http://www.robotic.dlr.de/Martin.Otter/\">Martin Otter</a>:<br/>Renamed from SIunit to SIunits. Subpackages expanded, i.e., the SIunits package, does no longer contain subpackages.</li>
<li><i>Aug 12, 1999</i> by <a href=\"http://www.robotic.dlr.de/Martin.Otter/\">Martin Otter</a>:<br/>Type &quot;Pressure&quot; renamed to &quot;AbsolutePressure&quot; and introduced a new type &quot;Pressure&quot; which does not contain a minimum of zero in order to allow convenient handling of relative pressure. Redefined BulkModulus as an alias to AbsolutePressure instead of Stress, since needed in hydraulics.</li>
<li><i>June 29, 1999</i> by <a href=\"http://www.robotic.dlr.de/Martin.Otter/\">Martin Otter</a>:<br/>Bug-fix: Double definition of &quot;Compressibility&quot; removed and appropriate &quot;extends Heat&quot; clause introduced in package SolidStatePhysics to incorporate ThermodynamicTemperature.</li>
<li><i>April 8, 1998</i> by <a href=\"http://www.robotic.dlr.de/Martin.Otter/\">Martin Otter</a> and Astrid Jaschinski:<br/>Complete ISO 31 chapters realized.</li>
<li><i>Nov. 15, 1997</i> by <a href=\"http://www.robotic.dlr.de/Martin.Otter/\">Martin Otter</a> and Hubertus Tummescheit:<br/>Some chapters realized.</li>
</ul>
</html>"));
  end SIunits;
annotation (
preferredView="info",
version="3.2.2",
versionBuild=3,
versionDate="2016-04-03",
dateModified = "2016-04-03 08:44:41Z",
revisionId="$Id:: package.mo 9263 2016-04-03 18:10:55Z #$",
uses(Complex(version="3.2.2"), ModelicaServices(version="3.2.2")),
conversion(
 noneFromVersion="3.2.1",
 noneFromVersion="3.2",
 noneFromVersion="3.1",
 noneFromVersion="3.0.1",
 noneFromVersion="3.0",
 from(version="2.1", script="modelica://Modelica/Resources/Scripts/Dymola/ConvertModelica_from_2.2.2_to_3.0.mos"),
 from(version="2.2", script="modelica://Modelica/Resources/Scripts/Dymola/ConvertModelica_from_2.2.2_to_3.0.mos"),
 from(version="2.2.1", script="modelica://Modelica/Resources/Scripts/Dymola/ConvertModelica_from_2.2.2_to_3.0.mos"),
 from(version="2.2.2", script="modelica://Modelica/Resources/Scripts/Dymola/ConvertModelica_from_2.2.2_to_3.0.mos")),
Icon(coordinateSystem(extent={{-100.0,-100.0},{100.0,100.0}}), graphics={
  Polygon(
    origin={-6.9888,20.048},
    fillColor={0,0,0},
    pattern=LinePattern.None,
    fillPattern=FillPattern.Solid,
    points={{-93.0112,10.3188},{-93.0112,10.3188},{-73.011,24.6},{-63.011,31.221},{-51.219,36.777},{-39.842,38.629},{-31.376,36.248},{-25.819,29.369},{-24.232,22.49},{-23.703,17.463},{-15.501,25.135},{-6.24,32.015},{3.02,36.777},{15.191,39.423},{27.097,37.306},{32.653,29.633},{35.035,20.108},{43.501,28.046},{54.085,35.19},{65.991,39.952},{77.897,39.688},{87.422,33.338},{91.126,21.696},{90.068,9.525},{86.099,-1.058},{79.749,-10.054},{71.283,-21.431},{62.816,-33.337},{60.964,-32.808},{70.489,-16.14},{77.368,-2.381},{81.072,10.054},{79.749,19.05},{72.605,24.342},{61.758,23.019},{49.587,14.817},{39.003,4.763},{29.214,-6.085},{21.012,-16.669},{13.339,-26.458},{5.401,-36.777},{-1.213,-46.037},{-6.24,-53.446},{-8.092,-52.387},{-0.684,-40.746},{5.401,-30.692},{12.81,-17.198},{19.424,-3.969},{23.658,7.938},{22.335,18.785},{16.514,23.283},{8.047,23.019},{-1.478,19.05},{-11.267,11.113},{-19.734,2.381},{-29.259,-8.202},{-38.519,-19.579},{-48.044,-31.221},{-56.511,-43.392},{-64.449,-55.298},{-72.386,-66.939},{-77.678,-74.612},{-79.53,-74.083},{-71.857,-61.383},{-62.861,-46.037},{-52.278,-28.046},{-44.869,-15.346},{-38.784,-2.117},{-35.344,8.731},{-36.403,19.844},{-42.488,23.813},{-52.013,22.49},{-60.744,16.933},{-68.947,10.054},{-76.884,2.646},{-93.0112,-12.1707},{-93.0112,-12.1707}},
    smooth=Smooth.Bezier),
  Ellipse(
    origin={40.8208,-37.7602},
    fillColor={161,0,4},
    pattern=LinePattern.None,
    fillPattern=FillPattern.Solid,
    extent={{-17.8562,-17.8563},{17.8563,17.8562}})}),
Documentation(info="<html>
<p>
Package <b>Modelica&reg;</b> is a <b>standardized</b> and <b>free</b> package
that is developed together with the Modelica&reg; language from the
Modelica Association, see
<a href=\"https://www.Modelica.org\">https://www.Modelica.org</a>.
It is also called <b>Modelica Standard Library</b>.
It provides model components in many domains that are based on
standardized interface definitions. Some typical examples are shown
in the next figure:
</p>

<p>
<img src=\"modelica://Modelica/Resources/Images/UsersGuide/ModelicaLibraries.png\">
</p>

<p>
For an introduction, have especially a look at:
</p>
<ul>
<li> <a href=\"modelica://Modelica.UsersGuide.Overview\">Overview</a>
  provides an overview of the Modelica Standard Library
  inside the <a href=\"modelica://Modelica.UsersGuide\">User's Guide</a>.</li>
<li><a href=\"modelica://Modelica.UsersGuide.ReleaseNotes\">Release Notes</a>
 summarizes the changes of new versions of this package.</li>
<li> <a href=\"modelica://Modelica.UsersGuide.Contact\">Contact</a>
  lists the contributors of the Modelica Standard Library.</li>
<li> The <b>Examples</b> packages in the various libraries, demonstrate
  how to use the components of the corresponding sublibrary.</li>
</ul>

<p>
This version of the Modelica Standard Library consists of
</p>
<ul>
<li><b>1600</b> models and blocks, and</li>
<li><b>1350</b> functions</li>
</ul>
<p>
that are directly usable (= number of public, non-partial classes). It is fully compliant
to <a href=\"https://www.modelica.org/documents/ModelicaSpec32Revision2.pdf\">Modelica Specification Version 3.2 Revision 2</a>
and it has been tested with Modelica tools from different vendors.
</p>

<p>
<b>Licensed by the Modelica Association under the Modelica License 2</b><br>
Copyright &copy; 1998-2016, ABB, AIT, T.&nbsp;B&ouml;drich, DLR, Dassault Syst&egrave;mes AB, Fraunhofer, A.&nbsp;Haumer, ITI, C.&nbsp;Kral, Modelon,
TU Hamburg-Harburg, Politecnico di Milano, XRG Simulation.
</p>

<p>
<i>This Modelica package is <u>free</u> software and the use is completely at <u>your own risk</u>; it can be redistributed and/or modified under the terms of the Modelica License 2. For license conditions (including the disclaimer of warranty) see <a href=\"modelica://Modelica.UsersGuide.ModelicaLicense2\">Modelica.UsersGuide.ModelicaLicense2</a> or visit <a href=\"https://www.modelica.org/licenses/ModelicaLicense2\"> https://www.modelica.org/licenses/ModelicaLicense2</a>.</i>
</p>

<p>
<b>Modelica&reg;</b> is a registered trademark of the Modelica Association.
</p>
</html>"));
end Modelica;

package Buildings "Library with models for building energy and control systems"
  extends Modelica.Icons.Package;

  package Fluid "Package with models for fluid flow systems"
    extends Modelica.Icons.Package;

    package FMI
    "Package with base classes that facilitate exporting models as an FMU"
      extends Modelica.Icons.Package;

      package Conversion
      "Package with blocks that convert between different connectors"
        extends Modelica.Icons.Package;

        block AirToOutlet
          "Conversion from real signals for a fluid to an Outlet connector"
          extends Modelica.Blocks.Icons.Block;

          replaceable package Medium =
            Modelica.Media.Interfaces.PartialMedium "Medium in the component"
            annotation (choicesAllMatching = true);

          Modelica.Blocks.Interfaces.RealInput m_flow(
            final unit="kg/s") "Mass flow rate"
            annotation (Placement(transformation(extent={{-140,60},{-100,100}})));

          Modelica.Blocks.Interfaces.RealInput h(final unit="J/kg")
            "Specific enthalpy"
            annotation (Placement(transformation(extent={{-140,20},{-100,60}})));

          Modelica.Blocks.Interfaces.RealInput Xi[Medium.nXi](
            each final unit="kg/kg")
            "Water vapor concentration in kg/kg total air"
            annotation (Placement(transformation(extent={{-140,-60},{-100,-20}})));

          Modelica.Blocks.Interfaces.RealInput C[Medium.nC](
            final quantity=Medium.extraPropertiesNames)
            "Prescribed boundary trace substances"
            annotation (Placement(transformation(extent={{-140,-100},{-100,-60}})));

          Interfaces.Outlet outlet(
            redeclare final package Medium = Medium,
            final allowFlowReversal=false,
            final use_p_in=false) "Fluid outlet"
            annotation (Placement(transformation(extent={{100,-10},{120,10}})));

      protected
          Modelica.Blocks.Interfaces.RealInput Xi_internal[Medium.nXi](
            each final unit = "kg/kg")
            "Internal connector for water vapor concentration in kg/kg total air";

          Modelica.Blocks.Interfaces.RealInput X_w_internal(
            final unit = "kg/kg")
            "Internal connector for water vapor concentration in kg/kg total air";

        equation
          // Conditional connectors
          connect(Xi_internal, Xi);
          if Medium.nXi == 0 then
            Xi_internal = zeros(Medium.nXi);
          end if;

          outlet.m_flow = m_flow;
          // If m_flow <= 0, output default properties.
          // This avoids that changes in state variables of the return
          // air are propagated to the room model which may trigger an
          // evaluation of the room ODE, even though Q=max(0, m_flow) c_p (TSup-TZon).
          connect(outlet.forward.X_w,  X_w_internal);
          if m_flow > 0 then
            outlet.forward.T = Medium.temperature_phX(
              p=Medium.p_default,
              h=h,
              X=Xi_internal);
            // Xi internal has 1 or zero components, hence we can use the sum.
            X_w_internal = sum(Xi_internal);
            outlet.forward.C = C;
          else
            outlet.forward.T = Medium.T_default;
            X_w_internal = Medium.X_default[1];
            outlet.forward.C  = zeros(Medium.nC);
          end if;
          annotation (defaultComponentName = "con",
            Documentation(info="<html>
<p>
Block that takes real inputs for properties of an air-based HVAC
system and converts them to an outlet connector of type
<a href=\"modelica://Buildings.Fluid.FMI.Interfaces.Outlet\">
Buildings.Fluid.FMI.Interfaces.Outlet</a>.
</p>
<p>
See <a href=\"modelica://Buildings.Fluid.FMI.ThermalZoneAdaptor\">
Buildings.Fluid.FMI.ThermalZoneAdaptor</a>
for its usage.
</p>
</html>",         revisions="<html>
<ul>
<li>
April 20, 2016, by Michael Wetter:<br/>
First implementation.
</li>
</ul>
</html>"),  Icon(graphics={
                Text(
                  extent={{-98,52},{-66,26}},
                  lineColor={0,0,127},
                  textString="h"),
                Text(
                  extent={{-92,94},{-28,68}},
                  lineColor={0,0,127},
                  textString="m_flow"),
                Text(
                  extent={{-104,-26},{-40,-52}},
                  lineColor={0,0,127},
                  textString="Xi"),
                Text(
                  extent={{-104,-64},{-40,-90}},
                  lineColor={0,0,127},
                  textString="C"),
                Polygon(
                  points={{90,0},{30,20},{30,-20},{90,0}},
                  lineColor={191,0,0},
                  fillColor={191,0,0},
                  fillPattern=FillPattern.Solid),
                Line(points={{-90,0},{30,0}}, color={191,0,0})}));
        end AirToOutlet;

        package Validation
        "Collection of models that illustrate model use and test models"
          extends Modelica.Icons.ExamplesPackage;

          model AirToOutlet2 "Model for testing time and state events"
            extends Modelica.Icons.Example;
            Real x( start = 1.0);
            Modelica.Blocks.Sources.Constant h(k=5E4) "Specific enthalpy"
              annotation (Placement(transformation(extent={{-60,20},{-40,40}})));
            Modelica.Blocks.Sources.Constant Xi[1](k={0.01}) "Water vapor concentration"
              annotation (Placement(transformation(extent={{-60,-20},{-40,0}})));

            Buildings.Fluid.FMI.Conversion.AirToOutlet conAirNoC(
              redeclare package Medium = Buildings.Media.Air)
              "Converter for air without trace substances"
              annotation (Placement(transformation(extent={{0,50},{20,70}})));

            Interfaces.Outlet outlet( redeclare final package Medium =
                  Buildings.Media.Air,
              final allowFlowReversal=false,
              final use_p_in=false)
              annotation (Placement(transformation(extent={{100,50},{120,70}})));
            Modelica.Blocks.Sources.Pulse m_flow(amplitude=0.2, period=2.5)
              "Pulse mass flow rate"
              annotation (Placement(transformation(extent={{-80,60},{-60,80}})));
          equation
           der( x) = 2;
            connect(h.y, conAirNoC.h) annotation (Line(points={{-39,30},{-26.5,30},{-26.5,
                    64},{-2,64}}, color={0,0,127}));
            connect(Xi.y, conAirNoC.Xi) annotation (Line(points={{-39,-10},{-20,-10},{-20,
                    56},{-2,56}}, color={0,0,127}));
            connect(outlet, conAirNoC.outlet)
              annotation (Line(points={{110,60},{21,60},{21,60}}, color={0,0,255}));
            connect(m_flow.y, conAirNoC.m_flow) annotation (Line(points={{-59,70},{-30,70},
                    {-30,68},{-2,68}}, color={0,0,127}));
          annotation (
              Documentation(info="<html>
<p>
This model is used to test time and state events. 
This model has 8 time events (1.25, 3.75, 6.25, 
8.75, 11.25, 13.75, 16.25) 
and 7 state events (0, 2.5, 5, 7.5, 10, 12.5, 15, 17.5), 
and 7.85 when simulated for 20 seconds.</p>
</html>",           revisions="<html>
<ul>
<li>
April 20, 2016 by Michael Wetter:<br/>
First implementation.
</li>
</ul>
</html>"),__Dymola_Commands(file="modelica://Buildings/Resources/Scripts/Dymola/Fluid/FMI/Conversion/Validation/AirToOutlet.mos"
                  "Simulate and plot"));
          end AirToOutlet2;
        annotation (preferredView="info", Documentation(info="<html>
<p>
This package contains examples for the use of models that can be found in
<a href=\"modelica://Buildings.Fluid.FMI.Conversion\">
Buildings.Fluid.FMI.Conversion</a>.
</p>
</html>"));
        end Validation;
        annotation (Icon(graphics={
              Polygon(
                points={{90,0},{30,20},{30,-20},{90,0}},
                lineColor={191,0,0},
                fillColor={191,0,0},
                fillPattern=FillPattern.Solid),
              Line(points={{-90,0},{30,0}}, color={191,0,0})}), Documentation(info="<html>
<p>
This package contains blocks to convert between scalar input-output signals
and the composite input-output connectors
<a href=\"modelica://Buildings.Fluid.FMI.Interfaces.Inlet\">
Buildings.Fluid.FMI.Interfaces.Inlet</a>
and
<a href=\"modelica://Buildings.Fluid.FMI.Interfaces.Outlet\">
Buildings.Fluid.FMI.Interfaces.Outlet</a>.
</p>
</html>"));
      end Conversion;

      package Interfaces
      "Package with interfaces for models that serves as an FMU container"
        extends Modelica.Icons.InterfacesPackage;

        connector FluidProperties "Type definition for fluid properties"
          replaceable package Medium = Modelica.Media.Interfaces.PartialMedium
            "Medium model" annotation (choicesAllMatching=true);


          Medium.Temperature T "Temperature";
          Buildings.Fluid.FMI.Interfaces.MassFractionConnector X_w if
               Medium.nXi > 0 "Water vapor mass fractions per kg total air";
          Medium.ExtraProperty C[Medium.nC] "Properties c_i/m";

          annotation (Documentation(info="<html>
<p>
This is a connector that declares the following fluid properties:
</p>
<ul>
<li>
The temperature <code>T</code>.
</li>
<li>
The mass fraction of water vapor <code>X_w</code> per kg of total air,
unless <code>Medium.nXi=0</code>.
Note that the mass fraction is not per kg of dry air, but rather
per kg of total air as is customary in Modelica.
</li>
<li>
The trace substances
<code>C</code>, 
unless <code>Medium.nC=0</code>.
</li>
</ul>
<p>
These quantities are used in the connectors
<a href=\"modelica://Buildings.Fluid.FMI.Interfaces.Inlet\">
Buildings.Fluid.FMI.Interfaces.Inlet</a>
and
<a href=\"modelica://Buildings.Fluid.FMI.Interfaces.Outlet\">
Buildings.Fluid.FMI.Interfaces.Outlet</a>.
</p>
<p>
Note that none of these quantities is declared to be an
<code>input</code> or <code>output</code>, because the role
is reversed whether the properties are in inlet or
outlet connector.
</p>
</html>",         revisions="<html>
<ul>
<li>
April 15, 2015 by Michael Wetter:<br/>
Changed connector variable to be temperature instead of
specific enthalpy.
</li>
<li>
November 8, 2014 by Michael Wetter:<br/>
First implementation.
</li>
</ul>
</html>"));
        end FluidProperties;

        connector MassFractionConnector =
          Modelica.SIunits.MassFraction
          "Mass fraction of water vapor per kg total mass as a connector"
          annotation (
          defaultComponentName="X_w",
          Icon(graphics,
            coordinateSystem(extent={{-100.0,-100.0},{100.0,100.0}},
              preserveAspectRatio=true,
              initialScale=0.2)),
          Diagram(
            coordinateSystem(preserveAspectRatio=true,
              initialScale=0.2,
              extent={{-100.0,-100.0},{100.0,100.0}}),
              graphics={
            Text(
              lineColor={0,127,127},
              extent={{-10.0,60.0},{-10.0,85.0}},
              textString="%name")}),
          Documentation(info="<html>
<p>
Connector with variable of type <code>Modelica.SIunits.MassFraction</code>.
Note that the mass fraction is in kg water vapor per total mass
of air, rathern than per kg of dry air.
</p>
<p>
This connector has been implemented to conditionally remove
the mass fraction if the medium has only one species.
While this could have been done using a vector of mass fractions
with zero length, as is used in fluid connectors,
this implemantation uses a scalar to avoid vectorized inputs
and outputs of FMUs.
</p>
</html>",         revisions="<html>
<ul>
<li>
April 29,2015, by Michael Wetter:<br/>
First implementation.
</li>
</ul>
</html>"));

        connector Outlet "Connector for fluid outlet"
          replaceable package Medium = Modelica.Media.Interfaces.PartialMedium
            "Medium model" annotation (choicesAllMatching=true);

          parameter Boolean use_p_in = true
            "= true to use pressure connector, false to remove it"
            annotation(Evaluate=true);

          parameter Boolean allowFlowReversal = true
            "= true to allow flow reversal, false restricts to design direction (inlet -> outlet)"
            annotation(Dialog(tab="Assumptions"), Evaluate=true);

          output Medium.MassFlowRate m_flow
            "Mass flow rate from the connection point into the component";
          Buildings.Fluid.FMI.Interfaces.PressureOutput p if
               use_p_in "Thermodynamic pressure in the connection point";

          input Buildings.Fluid.FMI.Interfaces.FluidProperties backward(
            redeclare final package Medium = Medium) if
               allowFlowReversal "Inflowing properties";

          output Buildings.Fluid.FMI.Interfaces.FluidProperties forward(
            redeclare final package Medium = Medium) "Outflowing properties";

          annotation (defaultComponentName="outlet",
          Icon(coordinateSystem(preserveAspectRatio=false, extent={{-100,-100},
                    {100,100}}), graphics={Polygon(
                  points={{-100,100},{-100,-100},{100,0},{-100,100}},
                  lineColor={0,0,255},
                  smooth=Smooth.None,
                  fillPattern=FillPattern.Solid,
                  fillColor={255,255,255}),
                  Text(
                  extent={{-58,134},{48,94}},
                  lineColor={0,0,255},
                  textString="%name")}),
            Documentation(info="<html>
<p>
This is a connector for a fluid outlet.
The connector produces as an output the 
following quantities:
</p>
<ul>
<li>
The mass flow rate <code>m_flow</code>.
</li>
<li>
The pressure <code>p</code>,
unless <code>use_p_in=false</code>.
</li>
<li>
The temperature of the inflowing fluid
<code>forward.T</code>, 
e.g., the temperature of the fluid that streams out of the component if <code>m_flow &gt; 0</code>.
</li>
<li>
The mass fraction of the inflowing fluid
<code>forward.Xi</code>,
unless <code>Medium.nXi=0</code>.
</li>
<li>
The trace substances of the inflowing fluid
<code>forward.C</code>, 
unless <code>Medium.nC=0</code>.
</li>
</ul>
<p>
If <code>allowFlowReversal = true</code>,
the connector requires as input the following quantities.
</p>
<ul>
<li>
The temperature of the outflowing fluid
<code>backward.T</code>,
e.g., if <code>m_flow &le; 0</code>.
</li>
<li>
The mass fraction of the outflowing fluid
<code>backward.Xi</code>,
unless <code>Medium.nXi=0</code>.
</li>
<li>
The trace substances of the outflowing fluid 
<code>backward.C</code>,
unless <code>Medium.nC=0</code>.
</li>
</ul>
<p>
If <code>allowFlowReversal = false</code>, then these inputs are not present
and hence not required to be provided.
</p>
</html>",         revisions="<html>
<ul>
<li>
April 29, 2015, by Michael Wetter:<br/>
Redesigned to conditionally remove the pressure connector
if <code>use_p_in=false</code>.
</li>
<li>
April 15, 2015 by Michael Wetter:<br/>
Changed connector variable to be temperature instead of
specific enthalpy.
</li>
<li>
November 8, 2014 by Michael Wetter:<br/>
First implementation.
</li>
</ul>
</html>"));
        end Outlet;

        connector PressureOutput =
          output Modelica.SIunits.AbsolutePressure(displayUnit="Pa")
          "Output pressure as a connector"
          annotation (
          defaultComponentName="p",
          Icon(
            coordinateSystem(preserveAspectRatio=true,
              extent={{-100.0,-100.0},{100.0,100.0}},
              initialScale=0.1),
              graphics={
            Polygon(
              lineColor={0,127,127},
              fillColor={255,255,255},
              fillPattern=FillPattern.Solid,
              points={{-100.0,100.0},{100.0,0.0},{-100.0,-100.0}})}),
          Diagram(
            coordinateSystem(preserveAspectRatio=true,
              extent={{-100.0,-100.0},{100.0,100.0}},
              initialScale=0.1),
              graphics={
            Polygon(
              lineColor={0,127,127},
              fillColor={255,255,255},
              fillPattern=FillPattern.Solid,
              points={{-100.0,50.0},{0.0,0.0},{-100.0,-50.0}}),
            Text(
              lineColor={0,127,127},
              extent={{30.0,60.0},{30.0,110.0}},
              textString="%name")}),
          Documentation(info="<html>
<p>
Connector with one output signal of type <code>Modelica.SIunits.AbsolutePressure</code>.
This connector has been implemented to conditionally remove
the pressure if no pressure drop calculation is requested.
</p>
</html>",         revisions="<html>
<ul>
<li>
April 29,2015, by Michael Wetter:<br/>
First implementation.
</li>
</ul>
</html>"));
      annotation (preferredView="info", Documentation(info="<html>
<p>
This package contains connectors that are used to export fluid flow models
as Functional Mockup Units.
</p>
<p>
The connectors 
<a href=\"modelica://Buildings.Fluid.FMI.Interfaces.Inlet\">
Buildings.Fluid.FMI.Interfaces.Inlet</a>
and
<a href=\"modelica://Buildings.Fluid.FMI.Interfaces.Outlet\">
Buildings.Fluid.FMI.Interfaces.Outlet</a>
are hierarchical. This was done for the exported FMUs to have hierarchical
names for their input and output signals.
</p>
</html>",       revisions="<html>
</html>"));
      end Interfaces;
      annotation (preferredView="info", Documentation(info="<html>
<p>
This package contains blocks that serve as containers for exporting
models from <code>Buildings.Fluid</code> as a Functional Mockup Unit (FMU).
</p>
<p>
This allows using models from <code>Buildings.Fluid</code>, add them
to a block that only has input and output signals, but no acausal connectors,
and then export the model as a Functional Mockup Unit.
Models can be individual models or systems that are composed of various
models.
For more information, see the
<a href=\"modelica://Buildings.Fluid.FMI.UsersGuide\">User's Guide</a>.
</p>
</html>"),
      Icon(graphics={Bitmap(extent={{-90,-86},{84,88}}, fileName=
                "modelica://Buildings/Resources/Images/Fluid/FMI/FMI_icon.png")}));
    end FMI;
  annotation (
  preferredView="info", Documentation(info="<html>
This package contains components for fluid flow systems such as
pumps, valves and sensors. For other fluid flow models, see
<a href=\"modelica://Modelica.Fluid\">Modelica.Fluid</a>.
</html>"),
  Icon(graphics={
          Polygon(points={{-70,26},{68,-44},{68,26},{2,-10},{-70,-42},{-70,26}},
              lineColor={0,0,0}),
          Line(points={{2,42},{2,-10}}, color={0,0,0}),
          Rectangle(
            extent={{-18,50},{22,42}},
            lineColor={0,0,0},
            fillColor={0,0,0},
            fillPattern=FillPattern.Solid)}));
  end Fluid;

  package Media "Package with medium models"
    extends Modelica.Icons.Package;

    package Air
      "Package with moist air model that decouples pressure and temperature"
      extends Modelica.Media.Interfaces.PartialCondensingGases(
         mediumName="Air",
         final substanceNames={"water", "air"},
         final reducedX=true,
         final singleState = false,
         reference_X={0.01,0.99},
         final fluidConstants = {Modelica.Media.IdealGases.Common.FluidData.H2O,
                                 Modelica.Media.IdealGases.Common.FluidData.N2},
         reference_T=273.15,
         reference_p=101325,
         AbsolutePressure(start=p_default),
         Temperature(start=T_default));
      extends Modelica.Icons.Package;

      constant Integer Water=1
        "Index of water (in substanceNames, massFractions X, etc.)";
      constant Integer Air=2
        "Index of air (in substanceNames, massFractions X, etc.)";

      constant AbsolutePressure pStp = reference_p
        "Pressure for which fluid density is defined";
      constant Density dStp = 1.2 "Fluid density at pressure pStp";

      // Redeclare ThermodynamicState to avoid the warning
      // "Base class ThermodynamicState is replaceable"
      // during model check
      redeclare record extends ThermodynamicState
        "ThermodynamicState record for moist air"
      end ThermodynamicState;

      // There must not be any stateSelect=StateSelect.prefer for
      // the pressure.
      // Otherwise, translateModel("Buildings.Fluid.FMI.ExportContainers.Examples.FMUs.ResistanceVolume")
      // will fail as Dymola does an index reduction and outputs
      //   Differentiated the equation
      //   vol.dynBal.medium.p+res.dp-inlet.p = 0.0;
      //   giving
      //   der(vol.dynBal.medium.p)+der(res.dp) = der(inlet.p);
      //
      //   The model requires derivatives of some inputs as listed below:
      //   1 inlet.m_flow
      //   1 inlet.p
      // Therefore, the statement
      //   p(stateSelect=if preferredMediumStates then StateSelect.prefer else StateSelect.default)
      // has been removed.
      redeclare replaceable model extends BaseProperties(
        Xi(each stateSelect=if preferredMediumStates then StateSelect.prefer else StateSelect.default),
        T(stateSelect=if preferredMediumStates then StateSelect.prefer else StateSelect.default),
        final standardOrderComponents=true) "Base properties"

    protected
        constant Modelica.SIunits.MolarMass[2] MMX = {steam.MM,dryair.MM}
          "Molar masses of components";

        MassFraction X_steam "Mass fraction of steam water";
        MassFraction X_air "Mass fraction of air";
        Modelica.SIunits.TemperatureDifference dT
          "Temperature difference used to compute enthalpy";
      equation
        assert(T >= 200.0 and T <= 423.15, "
Temperature T is not in the allowed range
200.0 K <= (T ="   + String(T) + " K) <= 423.15 K
required from medium model \""         + mediumName + "\".");

        MM = 1/(Xi[Water]/MMX[Water]+(1.0-Xi[Water])/MMX[Air]);

        X_steam  = Xi[Water]; // There is no liquid in this medium model
        X_air    = 1-Xi[Water];

        dT = T - reference_T;
        h = dT*dryair.cp * X_air +
           (dT * steam.cp + h_fg) * X_steam;
        R = dryair.R*X_air + steam.R*X_steam;

        // Equation for ideal gas, from h=u+p*v and R*T=p*v, from which follows that  u = h-R*T.
        // u = h-R*T;
        // However, in this medium, the gas law is d/dStp=p/pStp, from which follows using h=u+pv that
        // u= h-p*v = h-p/d = h-pStp/dStp
        u = h-pStp/dStp;

        // In this medium model, the density depends only
        // on temperature, but not on pressure.
        //  d = p/(R*T);
        d/dStp = p/pStp;

        state.p = p;
        state.T = T;
        state.X = X;
      end BaseProperties;

    redeclare function density "Gas density"
      extends Modelica.Icons.Function;
      input ThermodynamicState state;
      output Density d "Density";
    algorithm
      d :=state.p*dStp/pStp;
      annotation(smoothOrder=5,
      Inline=true,
      Documentation(info="<html>
Density is computed from pressure, temperature and composition in the thermodynamic state record applying the ideal gas law.
</html>"));
    end density;

    redeclare function extends dynamicViscosity
        "Return the dynamic viscosity of dry air"
    algorithm
      eta := 4.89493640395e-08 * state.T + 3.88335940547e-06;
      annotation (
      smoothOrder=99,
      Inline=true,
    Documentation(info="<html>
<p>
This function returns the dynamic viscosity.
</p>
<h4>Implementation</h4>
<p>
The function is based on the 5th order polynomial
of
<a href=\"modelica://Modelica.Media.Air.MoistAir.dynamicViscosity\">
Modelica.Media.Air.MoistAir.dynamicViscosity</a>.
However, for the typical range of temperatures encountered
in building applications, a linear function sufficies.
This implementation is therefore the above 5th order polynomial,
linearized around <i>20</i>&deg;C.
The relative error of this linearization is
<i>0.4</i>% at <i>-20</i>&deg;C,
and less then
<i>0.2</i>% between  <i>-5</i>&deg;C and  <i>+50</i>&deg;C.
</p>
</html>",
    revisions="<html>
<ul>
<li>
December 19, 2013, by Michael Wetter:<br/>
First implementation.
</li>
</ul>
</html>"));
    end dynamicViscosity;

    redeclare function enthalpyOfCondensingGas
        "Enthalpy of steam per unit mass of steam"
      extends Modelica.Icons.Function;

      input Temperature T "temperature";
      output SpecificEnthalpy h "steam enthalpy";
    algorithm
      h := (T-reference_T) * steam.cp + h_fg;
      annotation(smoothOrder=5,
      Inline=true,
      derivative=der_enthalpyOfCondensingGas);
    end enthalpyOfCondensingGas;

    redeclare replaceable function extends enthalpyOfGas
        "Enthalpy of gas mixture per unit mass of gas mixture"
    algorithm
      h := enthalpyOfCondensingGas(T)*X[Water]
           + enthalpyOfDryAir(T)*(1.0-X[Water]);
    annotation (
      Inline=true);
    end enthalpyOfGas;

    redeclare replaceable function extends enthalpyOfLiquid
        "Enthalpy of liquid (per unit mass of liquid) which is linear in the temperature"
    algorithm
      h := (T - reference_T)*cpWatLiq;
      annotation (
        smoothOrder=5,
        Inline=true,
        derivative=der_enthalpyOfLiquid);
    end enthalpyOfLiquid;

    redeclare function enthalpyOfNonCondensingGas
        "Enthalpy of non-condensing gas per unit mass of steam"
      extends Modelica.Icons.Function;

      input Temperature T "temperature";
      output SpecificEnthalpy h "enthalpy";
    algorithm
      h := enthalpyOfDryAir(T);
      annotation (
      smoothOrder=5,
      Inline=true,
      derivative=der_enthalpyOfNonCondensingGas);
    end enthalpyOfNonCondensingGas;

    redeclare function extends enthalpyOfVaporization
        "Enthalpy of vaporization of water"
    algorithm
      r0 := h_fg;
      annotation (
        Inline=true);
    end enthalpyOfVaporization;

    redeclare function extends gasConstant
        "Return ideal gas constant as a function from thermodynamic state, only valid for phi<1"

    algorithm
        R := dryair.R*(1 - state.X[Water]) + steam.R*state.X[Water];
      annotation (
        smoothOrder=2,
        Inline=true,
        Documentation(info="<html>
The ideal gas constant for moist air is computed from <a href=\"modelica://Modelica.Media.Air.MoistAir.ThermodynamicState\">thermodynamic state</a> assuming that all water is in the gas phase.
</html>"));
    end gasConstant;

    redeclare function extends pressure
        "Returns pressure of ideal gas as a function of the thermodynamic state record"

    algorithm
      p := state.p;
      annotation (
      smoothOrder=2,
      Inline=true,
      Documentation(info="<html>
Pressure is returned from the thermodynamic state record input as a simple assignment.
</html>"));
    end pressure;

    redeclare function extends isobaricExpansionCoefficient
        "Isobaric expansion coefficient beta"
    algorithm
      beta := 0;
      annotation (
        smoothOrder=5,
        Inline=true,
    Documentation(info="<html>
<p>
This function returns the isobaric expansion coefficient at constant pressure,
which is zero for this medium.
The isobaric expansion coefficient at constant pressure is
</p>
<p align=\"center\" style=\"font-style:italic;\">
&beta;<sub>p</sub> = - 1 &frasl; v &nbsp; (&part; v &frasl; &part; T)<sub>p</sub> = 0,
</p>
<p>
where
<i>v</i> is the specific volume,
<i>T</i> is the temperature and
<i>p</i> is the pressure.
</p>
</html>",
    revisions="<html>
<ul>
<li>
December 18, 2013, by Michael Wetter:<br/>
First implementation.
</li>
</ul>
</html>"));
    end isobaricExpansionCoefficient;

    redeclare function extends isothermalCompressibility
        "Isothermal compressibility factor"
    algorithm
      kappa := -1/state.p;
      annotation (
        smoothOrder=5,
        Inline=true,
        Documentation(info="<html>
<p>
This function returns the isothermal compressibility coefficient.
The isothermal compressibility is
</p>
<p align=\"center\" style=\"font-style:italic;\">
&kappa;<sub>T</sub> = -1 &frasl; v &nbsp; (&part; v &frasl; &part; p)<sub>T</sub>
  = -1 &frasl; p,
</p>
<p>
where
<i>v</i> is the specific volume,
<i>T</i> is the temperature and
<i>p</i> is the pressure.
</p>
</html>",
    revisions="<html>
<ul>
<li>
December 18, 2013, by Michael Wetter:<br/>
First implementation.
</li>
</ul>
</html>"));
    end isothermalCompressibility;

    redeclare function extends saturationPressure
        "Saturation curve valid for 223.16 <= T <= 373.16 (and slightly outside with less accuracy)"

    algorithm
      psat := Buildings.Utilities.Psychrometrics.Functions.saturationPressure(Tsat);
      annotation (
      smoothOrder=5,
      Inline=true);
    end saturationPressure;

    redeclare function extends specificEntropy
        "Return the specific entropy, only valid for phi<1"

    protected
        Modelica.SIunits.MoleFraction[2] Y "Molar fraction";
    algorithm
        Y := massToMoleFractions(
             state.X, {steam.MM,dryair.MM});
        s := specificHeatCapacityCp(state) * Modelica.Math.log(state.T/reference_T)
             - Modelica.Constants.R *
             sum(state.X[i]/MMX[i]*
                 Modelica.Math.log(max(Y[i], Modelica.Constants.eps)*state.p/reference_p) for i in 1:2);
      annotation (
      Inline=true,
        Documentation(info="<html>
<p>
This function computes the specific entropy.
</p>
<p>
The specific entropy of the mixture is obtained from
</p>
<p align=\"center\" style=\"font-style:italic;\">
s = s<sub>s</sub> + s<sub>m</sub>,
</p>
<p>
where
<i>s<sub>s</sub></i> is the entropy change due to the state change
(relative to the reference temperature) and
<i>s<sub>m</sub></i> is the entropy change due to mixing
of the dry air and water vapor.
</p>
<p>
The entropy change due to change in state is obtained from
</p>
<p align=\"center\" style=\"font-style:italic;\">
s<sub>s</sub> = c<sub>v</sub> ln(T/T<sub>0</sub>) + R ln(v/v<sub>0</sub>) <br/>
= c<sub>v</sub> ln(T/T<sub>0</sub>) + R ln(&rho;<sub>0</sub>/&rho;)
</p>
<p>If we assume <i>&rho; = p<sub>0</sub>/(R T)</i>,
and because <i>c<sub>p</sub> = c<sub>v</sub> + R</i>,
we can write
</p>
<p align=\"center\" style=\"font-style:italic;\">
s<sub>s</sub> = c<sub>v</sub> ln(T/T<sub>0</sub>) + R ln(T/T<sub>0</sub>) <br/>
=c<sub>p</sub> ln(T/T<sub>0</sub>).
</p>
<p>
Next, the entropy of mixing is obtained from a reversible isothermal
expansion process. Hence,
</p>
<p align=\"center\" style=\"font-style:italic;\">
  s<sub>m</sub> = -R &sum;<sub>i</sub>( X<sub>i</sub> &frasl; M<sub>i</sub>
  ln(Y<sub>i</sub> p/p<sub>0</sub>)),
</p>
<p>
where <i>R</i> is the gas constant,
<i>X</i> is the mass fraction,
<i>M</i> is the molar mass, and
<i>Y</i> is the mole fraction.
</p>
<p>
To obtain the state for a given pressure, entropy and mass fraction, use
<a href=\"modelica://Buildings.Media.Air.setState_psX\">
Buildings.Media.Air.setState_psX</a>.
</p>
<h4>Limitations</h4>
<p>
This function is only valid for a relative humidity below 100%.
</p>
</html>",     revisions="<html>
<ul>
<li>
November 27, 2013, by Michael Wetter:<br/>
First implementation.
</li>
</ul>
</html>"));
    end specificEntropy;

    redeclare function extends density_derp_T
        "Return the partial derivative of density with respect to pressure at constant temperature"
    algorithm
      ddpT := dStp/pStp;
      annotation (
      Inline=true,
    Documentation(info="<html>
<p>
This function returns the partial derivative of density
with respect to pressure at constant temperature.
</p>
</html>",
    revisions="<html>
<ul>
<li>
December 18, 2013, by Michael Wetter:<br/>
First implementation.
</li>
</ul>
</html>"));
    end density_derp_T;

    redeclare function extends density_derT_p
        "Return the partial derivative of density with respect to temperature at constant pressure"
    algorithm
      ddTp := 0;

      annotation (
      smoothOrder=99,
      Inline=true,
      Documentation(info=
    "<html>
<p>
This function computes the derivative of density with respect to temperature
at constant pressure.
</p>
</html>",     revisions=
    "<html>
<ul>
<li>
December 18, 2013, by Michael Wetter:<br/>
First implementation.
</li>
</ul>
</html>"));
    end density_derT_p;

    redeclare function extends density_derX
        "Return the partial derivative of density with respect to mass fractions at constant pressure and temperature"
    algorithm
      dddX := fill(0, nX);
    annotation (
      smoothOrder=99,
      Inline=true,
      Documentation(info="<html>
<p>
This function returns the partial derivative of density
with respect to mass fraction.
This value is zero because in this medium, density is proportional
to pressure, but independent of the species concentration.
</p>
</html>",
    revisions="<html>
<ul>
<li>
December 18, 2013, by Michael Wetter:<br/>
First implementation.
</li>
</ul>
</html>"));
    end density_derX;

    redeclare replaceable function extends specificHeatCapacityCp
        "Specific heat capacity of gas mixture at constant pressure"
    algorithm
      cp := dryair.cp*(1-state.X[Water]) +steam.cp*state.X[Water];
        annotation (
      smoothOrder=99,
      Inline=true,
      derivative=der_specificHeatCapacityCp);
    end specificHeatCapacityCp;

    redeclare replaceable function extends specificHeatCapacityCv
        "Specific heat capacity of gas mixture at constant volume"
    algorithm
      cv:= dryair.cv*(1-state.X[Water]) +steam.cv*state.X[Water];
      annotation (
        smoothOrder=99,
        Inline=true,
        derivative=der_specificHeatCapacityCv);
    end specificHeatCapacityCv;

    redeclare function setState_dTX
        "Return thermodynamic state as function of density d, temperature T and composition X"
      extends Modelica.Icons.Function;
      input Density d "Density";
      input Temperature T "Temperature";
      input MassFraction X[:]=reference_X "Mass fractions";
      output ThermodynamicState state "Thermodynamic state";

    algorithm
        // Note that d/dStp = p/pStp, hence p = d*pStp/dStp
        state := if size(X, 1) == nX then
                   ThermodynamicState(p=d*pStp/dStp, T=T, X=X)
                 else
                   ThermodynamicState(p=d*pStp/dStp,
                                      T=T,
                                      X=cat(1, X, {1 - sum(X)}));
        annotation (
        smoothOrder=2,
        Inline=true,
        Documentation(info="<html>
<p>
The <a href=\"modelica://Modelica.Media.Interfaces.PartialMixtureMedium.ThermodynamicState\">thermodynamic state record</a>
    is computed from density <code>d</code>, temperature <code>T</code> and composition <code>X</code>.
</p>
</html>"));
    end setState_dTX;

    redeclare function extends setState_phX
        "Return thermodynamic state as function of pressure p, specific enthalpy h and composition X"
    algorithm
      state := if size(X, 1) == nX then
        ThermodynamicState(p=p, T=temperature_phX(p, h, X), X=X)
     else
        ThermodynamicState(p=p, T=temperature_phX(p, h, X), X=cat(1, X, {1 - sum(X)}));
      annotation (
      smoothOrder=2,
      Inline=true,
      Documentation(info="<html>
The <a href=\"modelica://Modelica.Media.Interfaces.PartialMixtureMedium.ThermodynamicState\">
thermodynamic state record</a> is computed from pressure p, specific enthalpy h and composition X.
</html>"));
    end setState_phX;

    redeclare function extends setState_pTX
        "Return thermodynamic state as function of p, T and composition X or Xi"
    algorithm
        state := if size(X, 1) == nX then
                    ThermodynamicState(p=p, T=T, X=X)
                 else
                    ThermodynamicState(p=p, T=T, X=cat(1, X, {1 - sum(X)}));
        annotation (
      smoothOrder=2,
      Inline=true,
      Documentation(info="<html>
The <a href=\"modelica://Modelica.Media.Interfaces.PartialMixtureMedium.ThermodynamicState\">
thermodynamic state record</a> is computed from pressure p, temperature T and composition X.
</html>"));
    end setState_pTX;

    redeclare function extends setState_psX
        "Return the thermodynamic state as function of p, s and composition X or Xi"
    protected
        Modelica.SIunits.MassFraction[2] X_int=
          if size(X, 1) == nX then X else cat(1, X, {1 - sum(X)}) "Mass fraction";
        Modelica.SIunits.MoleFraction[2] Y "Molar fraction";
        Modelica.SIunits.Temperature T "Temperature";
    algorithm
       Y := massToMoleFractions(
             X_int, {steam.MM,dryair.MM});
        // The next line is obtained from symbolic solving the
        // specificEntropy function for T.
        // In this formulation, we can set T to any value when calling
        // specificHeatCapacityCp as cp does not depend on T.
        T := 273.15 * Modelica.Math.exp((s + Modelica.Constants.R *
               sum(X_int[i]/MMX[i]*
                 Modelica.Math.log(max(Y[i], Modelica.Constants.eps)) for i in 1:2))
                 / specificHeatCapacityCp(setState_pTX(p=p,
                                                       T=273.15,
                                                       X=X_int)));

        state := ThermodynamicState(p=p,
                                    T=T,
                                    X=X_int);

    annotation (
    Inline=true,
    Documentation(info="<html>
<p>
This function returns the thermodynamic state based on pressure,
specific entropy and mass fraction.
</p>
<p>
The state is computed by symbolically solving
<a href=\"modelica://Buildings.Media.Air.specificEntropy\">
Buildings.Media.Air.specificEntropy</a>
for temperature.
</p>
</html>",     revisions="<html>
<ul>
<li>
November 27, 2013, by Michael Wetter:<br/>
First implementation.
</li>
</ul>
</html>"));
    end setState_psX;

    redeclare replaceable function extends specificEnthalpy
        "Compute specific enthalpy from pressure, temperature and mass fraction"
    algorithm
      h := (state.T - reference_T)*dryair.cp * (1 - state.X[Water]) +
           ((state.T-reference_T) * steam.cp + h_fg) * state.X[Water];
      annotation (
       smoothOrder=5,
       Inline=true);
    end specificEnthalpy;

    redeclare replaceable function specificEnthalpy_pTX "Specific enthalpy"
      extends Modelica.Icons.Function;
      input Modelica.SIunits.Pressure p "Pressure";
      input Modelica.SIunits.Temperature T "Temperature";
      input Modelica.SIunits.MassFraction X[:] "Mass fractions of moist air";
      output Modelica.SIunits.SpecificEnthalpy h "Specific enthalpy at p, T, X";

    algorithm
      h := specificEnthalpy(setState_pTX(p, T, X));
      annotation(smoothOrder=5,
                 Inline=true,
                 inverse(T=temperature_phX(p, h, X)),
                 Documentation(info="<html>
Specific enthalpy as a function of temperature and species concentration.
The pressure is input for compatibility with the medium models, but the specific enthalpy
is independent of the pressure.
</html>",
    revisions="<html>
<ul>
<li>
April 30, 2015, by Filip Jorissen and Michael Wetter:<br/>
Added <code>Inline=true</code> for
<a href=\"https://github.com/iea-annex60/modelica-annex60/issues/227\">
issue 227</a>.
</li>
</ul>
</html>"));
    end specificEnthalpy_pTX;

    redeclare replaceable function extends specificGibbsEnergy
        "Specific Gibbs energy"
    algorithm
      g := specificEnthalpy(state) - state.T*specificEntropy(state);
      annotation (
        Inline=true);
    end specificGibbsEnergy;

    redeclare replaceable function extends specificHelmholtzEnergy
        "Specific Helmholtz energy"
    algorithm
      f := specificEnthalpy(state) - gasConstant(state)*state.T - state.T*specificEntropy(state);
      annotation (
        Inline=true);
    end specificHelmholtzEnergy;

    redeclare function extends isentropicEnthalpy "Return the isentropic enthalpy"
    algorithm
      h_is := specificEnthalpy(setState_psX(
                p=p_downstream,
                s=specificEntropy(refState),
                X=refState.X));
    annotation (
      Inline=true,
      Documentation(info="<html>
<p>
This function computes the specific enthalpy for
an isentropic state change from the temperature
that corresponds to the state <code>refState</code>
to <code>reference_T</code>.
</p>
</html>",
    revisions="<html>
<ul>
<li>
December 18, 2013, by Michael Wetter:<br/>
First implementation.
</li>
</ul>
</html>"));
    end isentropicEnthalpy;

    redeclare function extends specificInternalEnergy "Specific internal energy"
      extends Modelica.Icons.Function;
    algorithm
      u := specificEnthalpy(state) - pStp/dStp;
      annotation (
        Inline=true);
    end specificInternalEnergy;

    redeclare function extends temperature
        "Return temperature of ideal gas as a function of the thermodynamic state record"
    algorithm
      T := state.T;
      annotation (
      smoothOrder=2,
      Inline=true,
      Documentation(info="<html>
Temperature is returned from the thermodynamic state record input as a simple assignment.
</html>"));
    end temperature;

    redeclare function extends molarMass "Return the molar mass"
    algorithm
        MM := 1/(state.X[Water]/MMX[Water]+(1.0-state.X[Water])/MMX[Air]);
        annotation (
    Inline=true,
    smoothOrder=99,
    Documentation(info="<html>
<p>
This function returns the molar mass.
</p>
</html>",
    revisions="<html>
<ul>
<li>
December 18, 2013, by Michael Wetter:<br/>
First implementation.
</li>
</ul>
</html>"));
    end molarMass;

    redeclare replaceable function temperature_phX
        "Compute temperature from specific enthalpy and mass fraction"
        extends Modelica.Icons.Function;
      input AbsolutePressure p "Pressure";
      input SpecificEnthalpy h "specific enthalpy";
      input MassFraction[:] X "mass fractions of composition";
      output Temperature T "temperature";
    algorithm
      T := reference_T + (h - h_fg * X[Water])
           /((1 - X[Water])*dryair.cp + X[Water] * steam.cp);
      annotation(smoothOrder=5,
                 Inline=true,
                 inverse(h=specificEnthalpy_pTX(p, T, X)),
                 Documentation(info="<html>
Temperature as a function of specific enthalpy and species concentration.
The pressure is input for compatibility with the medium models, but the temperature
is independent of the pressure.
</html>",
    revisions="<html>
<ul>
<li>
April 30, 2015, by Filip Jorissen and Michael Wetter:<br/>
Added <code>Inline=true</code> for
<a href=\"https://github.com/iea-annex60/modelica-annex60/issues/227\">
issue 227</a>.
</li>
</ul>
</html>"));
    end temperature_phX;

    redeclare function extends thermalConductivity
        "Thermal conductivity of dry air as a polynomial in the temperature"
    algorithm
      lambda := Modelica.Media.Incompressible.TableBased.Polynomials_Temp.evaluate(
          {(-4.8737307422969E-008), 7.67803133753502E-005, 0.0241814385504202},
       Modelica.SIunits.Conversions.to_degC(state.T));
    annotation(LateInline=true);
    end thermalConductivity;

    //////////////////////////////////////////////////////////////////////
    // Protected classes.
    // These classes are only of use within this medium model.
    // Models generally have no need to access them.
    // Therefore, they are made protected. This also allows to redeclare the
    // medium model with another medium model that does not provide an
    // implementation of these classes.
  protected
      record GasProperties
        "Coefficient data record for properties of perfect gases"
        extends Modelica.Icons.Record;

        Modelica.SIunits.MolarMass MM "Molar mass";
        Modelica.SIunits.SpecificHeatCapacity R "Gas constant";
        Modelica.SIunits.SpecificHeatCapacity cp
          "Specific heat capacity at constant pressure";
        Modelica.SIunits.SpecificHeatCapacity cv = cp-R
          "Specific heat capacity at constant volume";
        annotation (
          preferredView="info",
          defaultComponentName="gas",
          Documentation(info="<html>
<p>
This data record contains the coefficients for perfect gases.
</p>
</html>",     revisions="<html>
<ul>
<li>
September 12, 2014, by Michael Wetter:<br/>
Corrected the wrong location of the <code>preferredView</code>
and the <code>revisions</code> annotation.
</li>
<li>
November 21, 2013, by Michael Wetter:<br/>
First implementation.
</li>
</ul>
</html>"));
      end GasProperties;

      // In the assignments below, we compute cv as OpenModelica
      // cannot evaluate cv=cp-R as defined in GasProperties.
      constant GasProperties dryair(
        R =    Modelica.Media.IdealGases.Common.SingleGasesData.Air.R,
        MM =   Modelica.Media.IdealGases.Common.SingleGasesData.Air.MM,
        cp =   Buildings.Utilities.Psychrometrics.Constants.cpAir,
        cv =   Buildings.Utilities.Psychrometrics.Constants.cpAir
                 -Modelica.Media.IdealGases.Common.SingleGasesData.Air.R)
        "Dry air properties";
      constant GasProperties steam(
        R =    Modelica.Media.IdealGases.Common.SingleGasesData.H2O.R,
        MM =   Modelica.Media.IdealGases.Common.SingleGasesData.H2O.MM,
        cp =   Buildings.Utilities.Psychrometrics.Constants.cpSte,
        cv =   Buildings.Utilities.Psychrometrics.Constants.cpSte
                 -Modelica.Media.IdealGases.Common.SingleGasesData.H2O.R)
        "Steam properties";

      constant Real k_mair =  steam.MM/dryair.MM "Ratio of molar weights";

      constant Modelica.SIunits.MolarMass[2] MMX={steam.MM,dryair.MM}
        "Molar masses of components";

      constant Modelica.SIunits.SpecificEnergy h_fg=
        Buildings.Utilities.Psychrometrics.Constants.h_fg
        "Latent heat of evaporation of water";
      constant Modelica.SIunits.SpecificHeatCapacity cpWatLiq=
        Buildings.Utilities.Psychrometrics.Constants.cpWatLiq
        "Specific heat capacity of liquid water";

    replaceable function der_enthalpyOfLiquid
        "Temperature derivative of enthalpy of liquid per unit mass of liquid"
      extends Modelica.Icons.Function;
      input Temperature T "Temperature";
      input Real der_T "Temperature derivative";
      output Real der_h "Derivative of liquid enthalpy";
    algorithm
      der_h := cpWatLiq*der_T;
      annotation (
        Inline=true);
    end der_enthalpyOfLiquid;

    function der_enthalpyOfCondensingGas
        "Derivative of enthalpy of steam per unit mass of steam"
      extends Modelica.Icons.Function;
      input Temperature T "Temperature";
      input Real der_T "Temperature derivative";
      output Real der_h "Derivative of steam enthalpy";
    algorithm
      der_h := steam.cp*der_T;
      annotation (
        Inline=true);
    end der_enthalpyOfCondensingGas;

    replaceable function enthalpyOfDryAir
        "Enthalpy of dry air per unit mass of dry air"
      extends Modelica.Icons.Function;

      input Temperature T "Temperature";
      output SpecificEnthalpy h "Dry air enthalpy";
    algorithm
      h := (T - reference_T)*dryair.cp;
      annotation (
        smoothOrder=5,
        Inline=true,
        derivative=der_enthalpyOfDryAir);
    end enthalpyOfDryAir;

    replaceable function der_enthalpyOfDryAir
        "Derivative of enthalpy of dry air per unit mass of dry air"
      extends Modelica.Icons.Function;
      input Temperature T "Temperature";
      input Real der_T "Temperature derivative";
      output Real der_h "Derivative of dry air enthalpy";
    algorithm
      der_h := dryair.cp*der_T;
      annotation (
        Inline=true);
    end der_enthalpyOfDryAir;

    replaceable function der_enthalpyOfNonCondensingGas
        "Derivative of enthalpy of non-condensing gas per unit mass of steam"
      extends Modelica.Icons.Function;
      input Temperature T "Temperature";
      input Real der_T "Temperature derivative";
      output Real der_h "Derivative of steam enthalpy";
    algorithm
      der_h := der_enthalpyOfDryAir(T, der_T);
      annotation (
        Inline=true);
    end der_enthalpyOfNonCondensingGas;

    replaceable function der_specificHeatCapacityCp
        "Derivative of specific heat capacity of gas mixture at constant pressure"
      extends Modelica.Icons.Function;
        input ThermodynamicState state "Thermodynamic state";
        input ThermodynamicState der_state "Derivative of thermodynamic state";
        output Real der_cp(unit="J/(kg.K.s)")
          "Derivative of specific heat capacity";
    algorithm
      der_cp := (steam.cp-dryair.cp)*der_state.X[Water];
      annotation (
        Inline=true);
    end der_specificHeatCapacityCp;

    replaceable function der_specificHeatCapacityCv
        "Derivative of specific heat capacity of gas mixture at constant volume"
      extends Modelica.Icons.Function;
        input ThermodynamicState state "Thermodynamic state";
        input ThermodynamicState der_state "Derivative of thermodynamic state";
        output Real der_cv(unit="J/(kg.K.s)")
          "Derivative of specific heat capacity";
    algorithm
      der_cv := (steam.cv-dryair.cv)*der_state.X[Water];
      annotation (
        Inline=true);
    end der_specificHeatCapacityCv;

      annotation(preferredView="info", Documentation(info="<html>
<p>
This medium package models moist air using a gas law in which pressure and temperature
are independent, which often leads to significantly faster and more robust computations.
The specific heat capacities at constant pressure and at constant volume are constant.
The air is assumed to be not saturated.
</p>
<p>
This medium uses the gas law
</p>
<p align=\"center\" style=\"font-style:italic;\">
&rho;/&rho;<sub>stp</sub> = p/p<sub>stp</sub>,
</p>
<p>
where
<i>p<sub>std</sub></i> and <i>&rho;<sub>stp</sub></i> are constant reference
temperature and density, rathern than the ideal gas law
</p>
<p align=\"center\" style=\"font-style:italic;\">
&rho; = p &frasl;(R T),
</p>
<p>
where <i>R</i> is the gas constant and <i>T</i> is the temperature.
</p>
<p>
This formulation often leads to smaller systems of nonlinear equations
because equations for pressure and temperature are decoupled.
Therefore, if air inside a control volume such as room air is heated, it
does not increase its specific volume. Consequently, merely heating or cooling
a control volume does not affect the air flow calculations in a duct network
that may be connected to that volume.
Note that multizone air exchange simulation in which buoyancy drives the
air flow is still possible as the models in
<a href=\"modelica://Buildings.Airflow.Multizone\">
Buildings.Airflow.Multizone</a> compute the mass density using the function
<a href=\"modelica://Buildings.Utilities.Psychrometrics.Functions.density_pTX\">
Buildings.Utilities.Psychrometrics.Functions.density_pTX</a> in which density
is a function of temperature.
</p>
<p>
Note that models in this package implement the equation for the internal energy as
</p>
<p align=\"center\" style=\"font-style:italic;\">
  u = h - p<sub>stp</sub> &frasl; &rho;<sub>stp</sub>,
</p>
<p>
where
<i>u</i> is the internal energy per unit mass,
<i>h</i> is the enthalpy per unit mass,
<i>p<sub>stp</sub></i> is the static pressure and
<i>&rho;<sub>stp</sub></i> is the mass density at standard pressure and temperature.
The reason for this implementation is that in general,
</p>
<p align=\"center\" style=\"font-style:italic;\">
  h = u + p v,
</p>
<p>
from which follows that
</p>
<p align=\"center\" style=\"font-style:italic;\">
  u = h - p v = h - p &frasl; &rho; = h - p<sub>stp</sub> &frasl; &rho;<sub>std</sub>,
</p>
<p>
because <i>p &frasl; &rho; = p<sub>stp</sub> &frasl; &rho;<sub>stp</sub></i> in this medium model.
</p>
<p>
The enthalpy is computed using the convention that <i>h=0</i>
if <i>T=0</i> &deg;C and no water vapor is present.
</p>
</html>",     revisions="<html>
<ul>
<li>
June 6, 2015, by Michael Wetter:<br/>
Set <code>AbsolutePressure(start=p_default)</code> to avoid
a translation error if
<a href=\"modelica://Buildings.Fluid.Sources.Examples.TraceSubstancesFlowSource\">
Buildings.Fluid.Sources.Examples.TraceSubstancesFlowSource</a>
is translated in pedantic mode in Dymola 2016.
The reason is that pressures use <code>Medium.p_default</code> as start values,
but
<a href=\"modelica://Modelica.Media.Interfaces.Types\">
Modelica.Media.Interfaces.Types</a>
sets a default value of <i>1E-5</i>.
A similar change has been done for pressure.
This fixes
<a href=\"https://github.com/iea-annex60/modelica-annex60/issues/266\">#266</a>.
</li>
<li>
June 5, 2015, by Michael Wetter:<br/>
Added <code>stateSelect</code> attribute in <code>BaseProperties.T</code>
to allow correct use of <code>preferredMediumState</code> as
described in
<a href=\"modelica://Modelica.Media.Interfaces.PartialMedium\">
Modelica.Media.Interfaces.PartialMedium</a>.
Note that the default is <code>preferredMediumState=false</code>
and hence the same states are used as were used before.
This is for
<a href=\"https://github.com/iea-annex60/modelica-annex60/issues/260\">#260</a>.
</li>
<li>
May 11, 2015, by Michael Wetter:<br/>
Removed
<code>p(stateSelect=if preferredMediumStates then StateSelect.prefer else StateSelect.default)</code>
in declaration of <code>BaseProperties</code>.
Otherwise, when models that contain a fluid volume
are exported as an FMU, their pressure would be
differentiated with respect to time. This would require
the time derivative of the inlet pressure, which is not available,
causing the translation to stop with an error.
</li>
<li>
May 1, 2015, by Michael Wetter:<br/>
Added <code>Inline=true</code> for
<a href=\"https://github.com/iea-annex60/modelica-annex60/issues/227\">
issue 227</a>.
</li>
<li>
March 20, 2015, by Michael Wetter:<br/>
Added missing term <code>state.p/reference_p</code> in function
<code>specificEntropy</code>.
<a href=\"https://github.com/iea-annex60/modelica-annex60/issues/193\">#193</a>.
</li>
<li>
February 3, 2015, by Michael Wetter:<br/>
Removed <code>stateSelect.prefer</code> for temperature.
This is for
<a href=\"https://github.com/iea-annex60/modelica-annex60/issues/160\">#160</a>.
</li>
<li>
July 24, 2014, by Michael Wetter:<br/>
Changed implementation to use
<a href=\"modelica://Buildings.Utilities.Psychrometrics.Constants\">
Buildings.Utilities.Psychrometrics.Constants</a>.
This was done to use consistent values throughout the library.
</li>
<li>
November 16, 2013, by Michael Wetter:<br/>
Revised and simplified the implementation.
</li>
<li>
November 14, 2013, by Michael Wetter:<br/>
Removed function
<code>HeatCapacityOfWater</code>
which is neither needed nor implemented in the
Modelica Standard Library.
</li>
<li>
November 13, 2013, by Michael Wetter:<br/>
Removed non-used computations in <code>specificEnthalpy_pTX</code> and
in <code>temperature_phX</code>.
</li>
<li>
March 29, 2013, by Michael Wetter:<br/>
Added <code>final standardOrderComponents=true</code> in the
<code>BaseProperties</code> declaration. This avoids an error
when models are checked in Dymola 2014 in the pedenatic mode.
</li>
<li>
April 12, 2012, by Michael Wetter:<br/>
Added keyword <code>each</code> to <code>Xi(stateSelect=...</code>.
</li>
<li>
April 4, 2012, by Michael Wetter:<br/>
Added redeclaration of <code>ThermodynamicState</code> to avoid a warning
during model check and translation.
</li>
<li>
August 3, 2011, by Michael Wetter:<br/>
Fixed bug in <code>u=h-R*T</code>, which is only valid for ideal gases.
For this medium, the function is <code>u=h-pStd/dStp</code>.
</li>
<li>
January 27, 2010, by Michael Wetter:<br/>
Fixed bug in <code>else</code> branch of function <code>setState_phX</code>
that lead to a run-time error when the constructor of this function was called.
</li>
<li>
January 22, 2010, by Michael Wetter:<br/>
Added implementation of function
<a href=\"modelica://Buildings.Media.GasesPTDecoupled.MoistAirUnsaturated.enthalpyOfNonCondensingGas\">
enthalpyOfNonCondensingGas</a> and its derivative.
<li>
January 13, 2010, by Michael Wetter:<br/>
Fixed implementation of derivative functions.
</li>
<li>
August 28, 2008, by Michael Wetter:<br/>
First implementation.
</li>
</ul>
</html>"),
        Icon(graphics={
            Ellipse(
              extent={{-78,78},{-34,34}},
              lineColor={0,0,0},
              fillPattern=FillPattern.Sphere,
              fillColor={120,120,120}),
            Ellipse(
              extent={{-18,86},{26,42}},
              lineColor={0,0,0},
              fillPattern=FillPattern.Sphere,
              fillColor={120,120,120}),
            Ellipse(
              extent={{48,58},{92,14}},
              lineColor={0,0,0},
              fillPattern=FillPattern.Sphere,
              fillColor={120,120,120}),
            Ellipse(
              extent={{-22,32},{22,-12}},
              lineColor={0,0,0},
              fillPattern=FillPattern.Sphere,
              fillColor={120,120,120}),
            Ellipse(
              extent={{36,-32},{80,-76}},
              lineColor={0,0,0},
              fillPattern=FillPattern.Sphere,
              fillColor={120,120,120}),
            Ellipse(
              extent={{-36,-30},{8,-74}},
              lineColor={0,0,0},
              fillPattern=FillPattern.Sphere,
              fillColor={120,120,120}),
            Ellipse(
              extent={{-90,-6},{-46,-50}},
              lineColor={0,0,0},
              fillPattern=FillPattern.Sphere,
              fillColor={120,120,120})}));
    end Air;
    annotation (preferredView="info", Documentation(info="<html>
<p>
This package contains media models for water and moist air.
The media models in this package are
compatible with
<a href=\"modelica://Modelica.Media\">
Modelica.Media</a>
but the implementation is in general simpler, which often
leads to more efficient simulation.
Due to the simplifications, the media model of this package
are generally accurate for a smaller temperature range than the
models in <a href=\"modelica://Modelica.Media\">
Modelica.Media</a>, but the smaller temperature range may often be
sufficient for building HVAC applications.
</p>
</html>"),
  Icon(coordinateSystem(preserveAspectRatio=false, extent={{-100,-100},{100,100}}),
          graphics={
          Line(
            points = {{-76,-80},{-62,-30},{-32,40},{4,66},{48,66},{73,45},{62,-8},{48,-50},{38,-80}},
            color={64,64,64},
            smooth=Smooth.Bezier),
          Line(
            points={{-40,20},{68,20}},
            color={175,175,175}),
          Line(
            points={{-40,20},{-44,88},{-44,88}},
            color={175,175,175}),
          Line(
            points={{68,20},{86,-58}},
            color={175,175,175}),
          Line(
            points={{-60,-28},{56,-28}},
            color={175,175,175}),
          Line(
            points={{-60,-28},{-74,84},{-74,84}},
            color={175,175,175}),
          Line(
            points={{56,-28},{70,-80}},
            color={175,175,175}),
          Line(
            points={{-76,-80},{38,-80}},
            color={175,175,175}),
          Line(
            points={{-76,-80},{-94,-16},{-94,-16}},
            color={175,175,175})}));
  end Media;

  package Utilities "Package with utility functions such as for I/O"
    extends Modelica.Icons.Package;

    package Math "Library with functions such as for smoothing"
      extends Modelica.Icons.Package;

      package Functions "Package with mathematical functions"
        extends Modelica.Icons.VariantsPackage;

        function regStep
          "Approximation of a general step, such that the approximation is continuous and differentiable"
          extends Modelica.Icons.Function;
          input Real x "Abscissa value";
          input Real y1 "Ordinate value for x > 0";
          input Real y2 "Ordinate value for x < 0";
          input Real x_small(min=0) = 1e-5
            "Approximation of step for -x_small <= x <= x_small; x_small >= 0 required";
          output Real y "Ordinate value to approximate y = if x > 0 then y1 else y2";
        algorithm
          y := smooth(1, if x >  x_small then y1 else
                         if x < -x_small then y2 else
                         if x_small > 0 then (x/x_small)*((x/x_small)^2 - 3)*(y2-y1)/4 + (y1+y2)/2 else (y1+y2)/2);

          annotation(Inline=true,
          Documentation(revisions="<html>
<ul>
<li><i>February 18, 2016</i>
    by Marcus Fuchs:<br/>
    Add function with <code>Inline = true</code> in annotations to package for better performance,
    as suggested in <a href=\"https://github.com/iea-annex60/modelica-annex60/issues/300\">#300</a> .</li>
<li><i>April 29, 2008</i>
    by <a href=\"mailto:Martin.Otter@DLR.de\">Martin Otter</a>:<br/>
    Designed and implemented.</li>
<li><i>August 12, 2008</i>
    by <a href=\"mailto:Michael.Sielemann@dlr.de\">Michael Sielemann</a>:<br/>
    Minor modification to cover the limit case <code>x_small -> 0</code> without division by zero.</li>
</ul>
</html>",         info="<html>
<p>
This function is used to approximate the equation
</p>
<pre>
    y = <b>if</b> x &gt; 0 <b>then</b> y1 <b>else</b> y2;
</pre>

<p>
by a smooth characteristic, so that the expression is continuous and differentiable:
</p>

<pre>
   y = <b>smooth</b>(1, <b>if</b> x &gt;  x_small <b>then</b> y1 <b>else</b>
                 <b>if</b> x &lt; -x_small <b>then</b> y2 <b>else</b> f(y1, y2));
</pre>

<p>
In the region <code>-x_small &lt; x &lt; x_small</code> a 2nd order polynomial is used
for a smooth transition from <code>y1</code> to <code>y2</code>.
</p>
</html>"));
        end regStep;
      annotation (preferredView="info", Documentation(info="<html>
<p>
This package contains functions for commonly used
mathematical operations. The functions are used in
the blocks
<a href=\"modelica://Buildings.Utilities.Math\">
Buildings.Utilities.Math</a>.
</p>
</html>"));
      end Functions;
    annotation (preferredView="info", Documentation(info="<html>
<p>
This package contains blocks and functions for commonly used
mathematical operations.
The classes in this package augment the classes
<a href=\"modelica://Modelica.Blocks\">
Modelica.Blocks</a>.
</p>
</html>"),
    Icon(coordinateSystem(preserveAspectRatio=true, extent={{-100,-100},
              {100,100}}), graphics={Line(points={{-80,0},{-68.7,34.2},{-61.5,53.1},
                {-55.1,66.4},{-49.4,74.6},{-43.8,79.1},{-38.2,79.8},{-32.6,76.6},{
                -26.9,69.7},{-21.3,59.4},{-14.9,44.1},{-6.83,21.2},{10.1,-30.8},{17.3,
                -50.2},{23.7,-64.2},{29.3,-73.1},{35,-78.4},{40.6,-80},{46.2,-77.6},
                {51.9,-71.5},{57.5,-61.9},{63.9,-47.2},{72,-24.8},{80,0}}, color={
                0,0,0}, smooth=Smooth.Bezier)}));
    end Math;

    package Psychrometrics "Library with psychrometric functions"
      extends Modelica.Icons.VariantsPackage;

      package Constants "Library of constants for psychometric functions"
        extends Modelica.Icons.Package;

        constant Modelica.SIunits.SpecificHeatCapacity cpAir=1006
          "Specific heat capacity of air";

        constant Modelica.SIunits.SpecificHeatCapacity cpSte=1860
          "Specific heat capacity of water vapor";

        constant Modelica.SIunits.SpecificHeatCapacity cpWatLiq = 4184
          "Specific heat capacity of liquid water";

        constant Modelica.SIunits.SpecificEnthalpy h_fg = 2501014.5
          "Enthalpy of evaporator of water";
        annotation (
          Documentation(info="<html>
<p>
This package provides constants for functions used
in the calculation of thermodynamic properties of moist air.
</p>
</html>",       revisions="<html>
<ul>
<li>
July 24, 2014, by Michael Wetter:<br/>
First implementation.
</li>
</ul>
</html>"),Icon(coordinateSystem(extent={{-100.0,-100.0},{100.0,100.0}}), graphics={
            Polygon(
              origin={-9.2597,25.6673},
              fillColor={102,102,102},
              pattern=LinePattern.None,
              fillPattern=FillPattern.Solid,
              points={{48.017,11.336},{48.017,11.336},{10.766,11.336},{-25.684,10.95},{-34.944,-15.111},{-34.944,-15.111},{-32.298,-15.244},{-32.298,-15.244},{-22.112,0.168},{11.292,0.234},{48.267,-0.097},{48.267,-0.097}},
              smooth=Smooth.Bezier),
            Polygon(
              origin={-19.9923,-8.3993},
              fillColor={102,102,102},
              pattern=LinePattern.None,
              fillPattern=FillPattern.Solid,
              points={{3.239,37.343},{3.305,37.343},{-0.399,2.683},{-16.936,-20.071},{-7.808,-28.604},{6.811,-22.519},{9.986,37.145},{9.986,37.145}},
              smooth=Smooth.Bezier),
            Polygon(
              origin={23.753,-11.5422},
              fillColor={102,102,102},
              pattern=LinePattern.None,
              fillPattern=FillPattern.Solid,
              points={{-10.873,41.478},{-10.873,41.478},{-14.048,-4.162},{-9.352,-24.8},{7.912,-24.469},{16.247,0.27},{16.247,0.27},{13.336,0.071},{13.336,0.071},{7.515,-9.983},{-3.134,-7.271},{-2.671,41.214},{-2.671,41.214}},
              smooth=Smooth.Bezier)}));
      end Constants;

      package Functions "Package with psychrometric functions"
        extends Modelica.Icons.Package;

        function saturationPressure
          "Saturation curve valid for 223.16 <= T <= 373.16 (and slightly outside with less accuracy)"
          extends Modelica.Icons.Function;
          input Modelica.SIunits.Temperature TSat(displayUnit="degC",
                                                  nominal=300) "Saturation temperature";
          output Modelica.SIunits.AbsolutePressure pSat(
                                                  displayUnit="Pa",
                                                  nominal=1000) "Saturation pressure";

        algorithm
          pSat := Buildings.Utilities.Math.Functions.regStep(
                     y1=Buildings.Utilities.Psychrometrics.Functions.saturationPressureLiquid(TSat),
                     y2=Buildings.Utilities.Psychrometrics.Functions.sublimationPressureIce(TSat),
                     x=TSat-273.16,
                     x_small=1.0);
          annotation(Inline=true,
            smoothOrder=1,
            Documentation(info="<html>
<p>
Saturation pressure of water, computed from temperature,
according to Wagner <i>et al.</i> (1993).
The range of validity is between
<i>190</i> and <i>373.16</i> Kelvin.
</p>
<h4>References</h4>
<p>
Wagner W., A. Saul, A. Pruss.
 <i>International equations for the pressure along the melting and along the sublimation curve of ordinary water substance</i>,
equation 3.5. 1993.
<a href=\"http://www.nist.gov/data/PDFfiles/jpcrd477.pdf\">
http://www.nist.gov/data/PDFfiles/jpcrd477.pdf</a>.
</p>
</html>",
        revisions="<html>
<ul>
<li>
March 15, 2016, by Michael Wetter:<br/>
Replaced <code>spliceFunction</code> with <code>regStep</code>.
This is for
<a href=\"https://github.com/iea-annex60/modelica-annex60/issues/300\">issue 300</a>.
</li>
<li>
August 19, 2015 by Michael Wetter:<br/>
Changed <code>smoothOrder</code> from <i>5</i> to <i>1</i> as
<a href=\"modelica://Buildings.Utilities.Math.Functions.spliceFunction\">
Buildings.Utilities.Math.Functions.spliceFunction</a> is only once
continuously differentiable.
Inlined the function.
</li>
<li>
November 20, 2013 by Michael Wetter:<br/>
First implementation, moved from <code>Buildings.Media</code>.
</li>
</ul>
</html>"));
        end saturationPressure;

        function saturationPressureLiquid
          "Return saturation pressure of water as a function of temperature T in the range of 273.16 to 373.16 K"
          extends Modelica.Icons.Function;
          input Modelica.SIunits.Temperature TSat(displayUnit="degC",
                                                  nominal=300) "Saturation temperature";
          output Modelica.SIunits.AbsolutePressure pSat(
                                              displayUnit="Pa",
                                              nominal=1000) "Saturation pressure";
        algorithm
          pSat := 611.657*Modelica.Math.exp(17.2799 - 4102.99/(TSat - 35.719));

          annotation (
            smoothOrder=99,
            derivative=Buildings.Utilities.Psychrometrics.Functions.BaseClasses.der_saturationPressureLiquid,
            Inline=true,
            Documentation(info="<html>
<p>
Saturation pressure of water above the triple point temperature computed from temperature
according to Wagner <i>et al.</i> (1993). The range of validity is between
<i>273.16</i> and <i>373.16</i> Kelvin.
</p>
<h4>References</h4>
<p>
Wagner W., A. Saul, A. Pruss.
 <i>International equations for the pressure along the melting and along the sublimation curve of ordinary water substance</i>,
equation 3.5. 1993.
<a href=\"http://www.nist.gov/data/PDFfiles/jpcrd477.pdf\">
http://www.nist.gov/data/PDFfiles/jpcrd477.pdf</a>.
</p>
</html>",
        revisions="<html>
<ul>
<li>
November 20, 2013 by Michael Wetter:<br/>
First implementation, moved from <code>Buildings.Media</code>.
</li>
</ul>
</html>"));
        end saturationPressureLiquid;

        function sublimationPressureIce
          "Return sublimation pressure of water as a function of temperature T between 190 and 273.16 K"
          extends Modelica.Icons.Function;
          input Modelica.SIunits.Temperature TSat(displayUnit="degC",
                                                  nominal=300) "Saturation temperature";
          output Modelica.SIunits.AbsolutePressure pSat(
                                              displayUnit="Pa",
                                              nominal=1000) "Saturation pressure";
      protected
          Modelica.SIunits.Temperature TTriple=273.16 "Triple point temperature";
          Modelica.SIunits.AbsolutePressure pTriple=611.657 "Triple point pressure";
          Real r1=TSat/TTriple "Common subexpression";
          Real a[2]={-13.9281690,34.7078238} "Coefficients a[:]";
          Real n[2]={-1.5,-1.25} "Coefficients n[:]";
        algorithm
          pSat := exp(a[1] - a[1]*r1^n[1] + a[2] - a[2]*r1^n[2])*pTriple;
          annotation (
            Inline=false,
            smoothOrder=5,
            derivative=Buildings.Utilities.Psychrometrics.Functions.BaseClasses.der_sublimationPressureIce,
            Documentation(info="<html>
<p>
Sublimation pressure of water below the triple point temperature, computed from temperature,
according to Wagner <i>et al.</i> (1993).
The range of validity is between
<i>190</i> and <i>273.16</i> Kelvin.
</p>
<h4>References</h4>
<p>
Wagner W., A. Saul, A. Pruss.
 <i>International equations for the pressure along the melting and along the sublimation curve of ordinary water substance</i>,
equation 3.5. 1993.
<a href=\"http://www.nist.gov/data/PDFfiles/jpcrd477.pdf\">
http://www.nist.gov/data/PDFfiles/jpcrd477.pdf</a>.
</p>
</html>",
        revisions="<html>
<ul>
<li>
November 20, 2013 by Michael Wetter:<br/>
First implementation, moved from <code>Buildings.Media</code>.
</li>
</ul>
</html>"));
        end sublimationPressureIce;

        package BaseClasses
        "Package with base classes for Buildings.Utilities.Psychrometrics.Functions"
          extends Modelica.Icons.BasesPackage;

          function der_saturationPressureLiquid
            "Derivative of the function saturationPressureLiquid"
            extends Modelica.Icons.Function;
            input Modelica.SIunits.Temperature Tsat "Saturation temperature";
            input Real dTsat(unit="K/s") "Saturation temperature derivative";
            output Real psat_der(unit="Pa/s") "Differential of saturation pressure";

          algorithm
            psat_der:=611.657*Modelica.Math.exp(17.2799 - 4102.99
                      /(Tsat - 35.719))*4102.99*dTsat/(Tsat - 35.719)^2;

            annotation(Inline=false,
              smoothOrder=5,
              Documentation(info="<html>
<p>
Derivative of function
<a href=\"modelica://Buildings.Utilities.Psychrometrics.Functions.saturationPressureLiquid\">
Buildings.Utilities.Psychrometrics.Functions.saturationPressureLiquid</a>.
</p>
</html>", revisions="<html>
<ul>
<li>
November 20, 2013 by Michael Wetter:<br/>
First implementation, moved from <code>Buildings.Media</code>.
</li>
</ul>
</html>"));
          end der_saturationPressureLiquid;

          function der_sublimationPressureIce
            "Derivative of function sublimationPressureIce"
              extends Modelica.Icons.Function;
              input Modelica.SIunits.Temperature TSat(displayUnit="degC",
                                                      nominal=300)
              "Saturation temperature";
              input Real dTsat(unit="K/s") "Sublimation temperature derivative";
              output Real psat_der(unit="Pa/s") "Sublimation pressure derivative";
        protected
              Modelica.SIunits.Temperature TTriple=273.16 "Triple point temperature";
              Modelica.SIunits.AbsolutePressure pTriple=611.657 "Triple point pressure";
              Real r1=TSat/TTriple "Common subexpression 1";
              Real r1_der=dTsat/TTriple "Derivative of common subexpression 1";
              Real a[2]={-13.9281690,34.7078238} "Coefficients a[:]";
              Real n[2]={-1.5,-1.25} "Coefficients n[:]";
          algorithm
              psat_der := exp(a[1] - a[1]*r1^n[1] + a[2] - a[2]*r1^n[2])*pTriple*(-(a[1]
                *(r1^(n[1] - 1)*n[1]*r1_der)) - (a[2]*(r1^(n[2] - 1)*n[2]*r1_der)));
              annotation (
                Inline=false,
                smoothOrder=5,
                Documentation(info="<html>
<p>
Derivative of function
<a href=\"modelica://Buildings.Utilities.Psychrometrics.Functions.sublimationPressureIce\">
Buildings.Utilities.Psychrometrics.Functions.sublimationPressureIce</a>.
</p>
</html>", revisions="<html>
<ul>
<li>
November 20, 2013 by Michael Wetter:<br/>
First implementation, moved from <code>Buildings.Media</code>.
</li>
</ul>
</html>"));
          end der_sublimationPressureIce;
        annotation (preferredView="info", Documentation(info="<html>
<p>
This package contains base classes that are used to construct the models in
<a href=\"modelica://Buildings.Utilities.Psychrometrics.Functions\">Buildings.Utilities.Psychrometrics.Functions</a>.
</p>
</html>"));
        end BaseClasses;
        annotation (preferredView="info", Documentation(info="<html>
<p>
This package contains functions for psychrometric calculations.
</p>

The nomenclature used in this package is described at
<a href=\"modelica://Buildings.UsersGuide.Conventions\">
Buildings.UsersGuide.Conventions</a>.
</html>"));
      end Functions;
    annotation (preferredView="info", Documentation(info="<html>
<p>
This package contains blocks and functions for psychrometric calculations.
</p>

The nomenclature used in this package is described at
<a href=\"modelica://Buildings.UsersGuide.Conventions\">
Buildings.UsersGuide.Conventions</a>.
</html>"));
    end Psychrometrics;
  annotation (
  preferredView="info", Documentation(info="<html>
<p>
This package contains utility models such as for thermal comfort calculation, input/output, co-simulation, psychrometric calculations and various functions that are used throughout the library.
</p>
</html>"),
  Icon(coordinateSystem(extent={{-100.0,-100.0},{100.0,100.0}}), graphics={
      Polygon(
        origin={1.3835,-4.1418},
        rotation=45.0,
        fillColor={64,64,64},
        pattern=LinePattern.None,
        fillPattern=FillPattern.Solid,
        points={{-15.0,93.333},{-15.0,68.333},{0.0,58.333},{15.0,68.333},{15.0,93.333},{20.0,93.333},{25.0,83.333},{25.0,58.333},{10.0,43.333},{10.0,-41.667},{25.0,-56.667},{25.0,-76.667},{10.0,-91.667},{0.0,-91.667},{0.0,-81.667},{5.0,-81.667},{15.0,-71.667},{15.0,-61.667},{5.0,-51.667},{-5.0,-51.667},{-15.0,-61.667},{-15.0,-71.667},{-5.0,-81.667},{0.0,-81.667},{0.0,-91.667},{-10.0,-91.667},{-25.0,-76.667},{-25.0,-56.667},{-10.0,-41.667},{-10.0,43.333},{-25.0,58.333},{-25.0,83.333},{-20.0,93.333}}),
      Polygon(
        origin={10.1018,5.218},
        rotation=-45.0,
        fillColor={255,255,255},
        fillPattern=FillPattern.Solid,
        points={{-15.0,87.273},{15.0,87.273},{20.0,82.273},{20.0,27.273},{10.0,17.273},{10.0,7.273},{20.0,2.273},{20.0,-2.727},{5.0,-2.727},{5.0,-77.727},{10.0,-87.727},{5.0,-112.727},{-5.0,-112.727},{-10.0,-87.727},{-5.0,-77.727},{-5.0,-2.727},{-20.0,-2.727},{-20.0,2.273},{-10.0,7.273},{-10.0,17.273},{-20.0,27.273},{-20.0,82.273}})}));
  end Utilities;
annotation (
preferredView="info",
version="4.0.0",
versionDate="2016-03-29",
dateModified="2016-03-29",
uses(Modelica(version="3.2.2")),
conversion(
 from(version={"3.0.0", "4.0.0"},
      script="modelica://Buildings/Resources/Scripts/Dymola/ConvertBuildings_from_3.0_to_4.0.mos")),
revisionId="$Id$",
preferredView="info",
Documentation(info="<html>
<p>
The <code>Buildings</code> library is a free library
for modeling building energy and control systems.
Many models are based on models from the package
<code>Modelica.Fluid</code> and use
the same ports to ensure compatibility with the Modelica Standard
Library.
</p>
<p>
The figure below shows a section of the schematic view of the model
<a href=\"modelica://Buildings.Examples.HydronicHeating\">
Buildings.Examples.HydronicHeating</a>.
In the lower part of the figure, there is a dynamic model of a boiler, a pump and a stratified energy storage tank. Based on the temperatures of the storage tank, a finite state machine switches the boiler and its pump on and off.
The heat distribution is done using a hydronic heating system with a three way valve and a pump with variable revolutions. The upper right hand corner shows a room model that is connected to a radiator whose flow is controlled by a thermostatic valve.
</p>
<p align=\"center\">
<img alt=\"image\" src=\"modelica://Buildings/Resources/Images/UsersGuide/HydronicHeating.png\" border=\"1\"/>
</p>
<p>
The web page for this library is
<a href=\"http://simulationresearch.lbl.gov/modelica\">http://simulationresearch.lbl.gov/modelica</a>,
and the development page is
<a href=\"https://github.com/lbl-srg/modelica-buildings\">https://github.com/lbl-srg/modelica-buildings</a>.
Contributions to further advance the library are welcomed.
Contributions may not only be in the form of model development, but also
through model use, model testing,
requirements definition or providing feedback regarding the model applicability
to solve specific problems.
</p>
</html>"));
end Buildings;
model Buildings_Fluid_FMI_Conversion_Validation_AirToOutlet2
 extends Buildings.Fluid.FMI.Conversion.Validation.AirToOutlet2;
  annotation(experiment(
    StopTime=1,
    __Dymola_NumberOfIntervals=500,
    Tolerance=0.0001,
    __Dymola_Algorithm="dassl"),uses(Buildings(version="4.0.0")));
end Buildings_Fluid_FMI_Conversion_Validation_AirToOutlet2;
