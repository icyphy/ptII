model HeatConductor "Test model for QSS"
extends Modelica.Icons.Example;

Modelica.Blocks.Interfaces.RealInput TOut "Outside temperature"
annotation (Placement(transformation(extent={{-140,-20},{-100,20}})));

Modelica.Blocks.Interfaces.RealOutput TSur "Surface temperature"
annotation (Placement(transformation(extent={{100,70},{120,90}})));

Real x "State";
initial equation
x=273.15;
equation
der(x) = TOut-x;
TSur = x;

annotation (uses(Modelica(version="3.2.1")), Diagram(coordinateSystem(
preserveAspectRatio=false, extent={{-100,-100},{100,100}}), graphics));

end HeatConductor;