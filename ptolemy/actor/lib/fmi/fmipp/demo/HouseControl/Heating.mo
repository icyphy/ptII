package Heating
  import Modelica;
  model foo
    annotation(experiment(StartTime = 0.0, StopTime = 10.0, Tolerance = 0.000001));
    Modelica.Blocks.Sources.Constant const(k = 1) annotation(Placement(visible = true, transformation(origin = {-48.5149,24.0924}, extent = {{-12,-12},{12,12}}, rotation = 0)));
    Heating.ControlledHouse controlledhouse1 annotation(Placement(visible = true, transformation(origin = {29.8472,12.691}, extent = {{-12,-12},{12,12}}, rotation = 0)));
  equation
    connect(const.y,controlledhouse1.u) annotation(Line(points = {{-35.3149,24.0924},{19.0364,24.0924},{19.0364,21.7999},{19.5106,21.7999}}));
  end foo;
  model House
    parameter Modelica.SIunits.Temperature Tamb(displayUnit = "degC") = 293.15 "Ambient Temperature" annotation(Placement(visible = true, transformation(origin = {237.455,-74.1818}, extent = {{-12,-12},{12,12}}, rotation = 0)));
    annotation(Diagram(), Icon(graphics = {Rectangle(rotation = 0, lineColor = {0,0,255}, fillColor = {0,0,255}, pattern = LinePattern.Solid, fillPattern = FillPattern.None, lineThickness = 0.25, extent = {{-70.9571,35.3135},{67.6568,-55.4455}}),Polygon(points = {{-85.4785,35.3135},{82.5083,35.6436},{-4.9505,76.8977},{-4.9505,76.8977},{-4.9505,76.8977}}, rotation = 0, lineColor = {0,0,255}, fillColor = {0,0,255}, pattern = LinePattern.Solid, fillPattern = FillPattern.None, lineThickness = 0.25),Line(points = {{-85.1485,34.9835},{-4.62046,77.2277}}, rotation = 0, color = {0,0,255}, pattern = LinePattern.Solid, thickness = 0.25),Rectangle(rotation = 0, lineColor = {0,0,255}, fillColor = {0,0,255}, pattern = LinePattern.Solid, fillPattern = FillPattern.None, lineThickness = 0.25, extent = {{-56.1056,-55.4455},{-29.0429,-5.94059}}),Rectangle(rotation = 0, lineColor = {0,0,255}, fillColor = {0,0,255}, pattern = LinePattern.Solid, fillPattern = FillPattern.None, lineThickness = 0.25, extent = {{1.9802,2.9703},{51.4851,-24.7525}})}));
    parameter Real Ccap = 1 annotation(Placement(visible = true, transformation(origin = {208.364,-69.8182}, extent = {{-12,-12},{12,12}}, rotation = 0)));
    parameter Real Gcond = 0.1 annotation(Placement(visible = true, transformation(origin = {220.727,-94.5455}, extent = {{-12,-12},{12,12}}, rotation = 0)));
    parameter Real Tmax = 25 annotation(Placement(visible = true, transformation(origin = {268,-126.182}, extent = {{-12,-12},{12,12}}, rotation = 0)));
    parameter Real Tmin = 23 annotation(Placement(visible = true, transformation(origin = {253.818,-91.6364}, extent = {{-12,-12},{12,12}}, rotation = 0)));
    Modelica.Blocks.Interfaces.BooleanInput u annotation(Placement(visible = true, transformation(origin = {-84.8221,75.5019}, extent = {{-12,-12},{12,12}}, rotation = 0), iconTransformation(origin = {-84.8221,75.5019}, extent = {{-12,-12},{12,12}}, rotation = 0)));
    Modelica.Blocks.Interfaces.RealOutput y annotation(Placement(visible = true, transformation(origin = {85.0513,75.9424}, extent = {{-12,-12},{12,12}}, rotation = 0), iconTransformation(origin = {85.0513,75.9424}, extent = {{-12,-12},{12,12}}, rotation = 0)));
    Modelica.Thermal.HeatTransfer.Components.ThermalConductor thermalconductor(G = Gcond) annotation(Placement(visible = true, transformation(origin = {43.8713,4.6405}, extent = {{-12,-12},{12,12}}, rotation = 0)));
    Modelica.Electrical.Analog.Basic.Ground ground1 annotation(Placement(visible = true, transformation(origin = {-77.9468,-29.5413}, extent = {{-12,-12},{12,12}}, rotation = 0)));
    Modelica.Electrical.Analog.Ideal.IdealClosingSwitch idealclosingswitch annotation(Placement(visible = true, transformation(origin = {-56.4923,17.0042}, extent = {{-12,-12},{12,12}}, rotation = 0)));
    Modelica.Electrical.Analog.Sources.ConstantVoltage constantvoltage(V = 1) annotation(Placement(visible = true, transformation(origin = {-77.9468,4.6405}, extent = {{-12,12},{12,-12}}, rotation = -90)));
    Modelica.Electrical.Analog.Basic.HeatingResistor heatingresistor(R_ref = 1, T_ref = 300.15, alpha = 1 / 255) annotation(Placement(visible = true, transformation(origin = {-20.4923,4.6405}, extent = {{12,-12},{-12,12}}, rotation = 90)));
    Modelica.Thermal.HeatTransfer.Components.HeatCapacitor heatcapacitor(C = Ccap, T(start = Tamb, fixed = true)) annotation(Placement(visible = true, transformation(origin = {8.59862,-21.5412}, extent = {{12,12},{-12,-12}}, rotation = 180)));
    Modelica.Thermal.HeatTransfer.Sources.FixedTemperature fixedtemperature(T = Tamb) annotation(Placement(visible = true, transformation(origin = {78.4168,5.0042}, extent = {{12,12},{-12,-12}}, rotation = -180)));
    Modelica.Thermal.HeatTransfer.Celsius.TemperatureSensor temperaturesensor annotation(Placement(visible = true, transformation(origin = {8.59865,34.4586}, extent = {{12,-12},{-12,12}}, rotation = 90)));
  equation
    connect(heatcapacitor.port,temperaturesensor.port) annotation(Line(points = {{8.59862,-9.5412},{8.59862,20.6405},{8.59865,20.6405},{8.59865,22.4586}}));
    connect(temperaturesensor.T,y) annotation(Line(points = {{8.59865,46.4586},{8.72727,46.4586},{8.72727,61.4545},{85.0513,61.4545},{85.0513,75.9424}}));
    connect(thermalconductor.port_b,fixedtemperature.port) annotation(Line(points = {{55.8713,4.6405},{66.0532,4.6405},{66.0532,5.0042},{66.4168,5.0042}}));
    connect(heatcapacitor.port,heatingresistor.heatPort) annotation(Line(points = {{8.59862,-9.5412},{8.59862,4.6405},{-8.49228,4.6405},{-8.49228,4.6405}}));
    connect(idealclosingswitch.n,heatingresistor.n) annotation(Line(points = {{-44.4923,17.0042},{-20.1287,17.0042},{-20.1287,16.6405},{-20.4923,16.6405}}));
    connect(heatingresistor.p,constantvoltage.n) annotation(Line(points = {{-20.4923,-7.3595},{-77.2196,-7.3595},{-77.2196,-7.3595},{-77.9468,-7.3595}}));
    connect(heatingresistor.heatPort,thermalconductor.port_a) annotation(Line(points = {{-8.49228,4.6405},{32.235,4.6405},{32.235,4.6405},{31.8713,4.6405}}));
    connect(constantvoltage.n,ground1.p) annotation(Line(points = {{-77.9468,-7.3595},{-82.674,-7.3595},{-82.674,-17.5413},{-77.9468,-17.5413}}));
    connect(constantvoltage.p,idealclosingswitch.p) annotation(Line(points = {{-77.9468,16.6405},{-67.765,16.6405},{-67.765,17.0042},{-68.4923,17.0042}}));
    connect(u,idealclosingswitch.control) annotation(Line(points = {{-84.8221,75.5019},{-56.1287,75.5019},{-56.1287,25.4042},{-56.4923,25.4042}}));
  end House;
  model ControlledHouse
    annotation(Diagram(), Icon(), experiment(StartTime = 0.0, StopTime = 10.0, Tolerance = 0.000001));
    Modelica.Blocks.Logical.And and1 annotation(Placement(visible = true, transformation(origin = {12.5413,1.65017}, extent = {{-12,-12},{12,12}}, rotation = 0)));
    Heating.House house1(Ccap = 0.3, Tmax = 26, Tmin = 24, Gcond = 0.1) annotation(Placement(visible = true, transformation(origin = {51.5116,-6.83828}, extent = {{-12,-12},{12,12}}, rotation = 0)));
    Modelica.Blocks.Logical.OnOffController onoffcontroller(bandwidth = 2) annotation(Placement(visible = true, transformation(origin = {-32.5173,-9.06931}, extent = {{-12,-12},{12,12}}, rotation = 0)));
    Modelica.Blocks.Interfaces.RealInput u annotation(Placement(visible = true, transformation(origin = {-86.1386,75.9076}, extent = {{-12,-12},{12,12}}, rotation = 0), iconTransformation(origin = {-86.1386,75.9076}, extent = {{-12,-12},{12,12}}, rotation = 0)));
    Modelica.Blocks.Interfaces.RealOutput y annotation(Placement(visible = true, transformation(origin = {80.198,74.9175}, extent = {{-12,-12},{12,12}}, rotation = 0), iconTransformation(origin = {80.198,74.9175}, extent = {{-12,-12},{12,12}}, rotation = 0)));
    Modelica.Blocks.Sources.Constant const(k = Tref) annotation(Placement(visible = true, transformation(origin = {-78.2419,18.6832}, extent = {{-12,-12},{12,12}}, rotation = 0)));
    Modelica.Blocks.Math.RealToBoolean realtoboolean(threshold = 0.01) annotation(Placement(visible = true, transformation(origin = {-11.2211,48.8449}, extent = {{-12,12},{12,-12}}, rotation = -90)));
    parameter Real Tref = 25 annotation(Placement(visible = true, transformation(origin = {242.303,-100.353}, extent = {{-12,-12},{12,12}}, rotation = 0)));
  equation
    connect(const.y,onoffcontroller.reference) annotation(Line(points = {{-65.0419,18.6832},{-52.3432,18.6832},{-52.3432,-1.86931},{-46.9173,-1.86931}}));
    connect(house1.y,y) annotation(Line(points = {{61.2425,0.492624},{60.066,0.492624},{60.066,75.2475},{80.198,75.2475},{80.198,74.9175}}));
    connect(realtoboolean.y,and1.u1) annotation(Line(points = {{-11.2211,35.6449},{-11.2211,35.6449},{-11.2211,0.990099},{-1.85875,0.990099},{-1.85875,1.65017}}));
    connect(u,realtoboolean.u) annotation(Line(points = {{-86.1386,75.9076},{-10.8911,75.9076},{-10.8911,63.2449},{-11.2211,63.2449}}));
    connect(house1.y,onoffcontroller.u) annotation(Line(points = {{61.2425,0.492624},{68.3636,0.492624},{68.3636,-48},{-67.7504,-48},{-67.7504,-17.0693},{-46.9173,-17.0693},{-46.9173,-16.2693}}));
    connect(onoffcontroller.y,and1.u2) annotation(Line(points = {{-19.3173,-9.06931},{-2.9703,-9.06931},{-2.9703,-7.94983},{-1.85875,-7.94983}}));
    connect(and1.y,house1.u) annotation(Line(points = {{25.7413,1.65017},{38.9439,1.65017},{38.9439,2.06353},{41.6497,2.06353}}));
  end ControlledHouse;
end Heating;

