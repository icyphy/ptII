within ;
model FourInFourOuts
  Modelica.Blocks.Interfaces.RealInput u
    annotation (Placement(transformation(extent={{-138,-72},{-98,-32}})));
  Modelica.Blocks.Interfaces.RealInput u2
    annotation (Placement(transformation(extent={{-140,22},{-100,62}})));
  Modelica.Blocks.Interfaces.RealInput u3
    annotation (Placement(transformation(extent={{-140,-8},{-100,32}})));
  Modelica.Blocks.Interfaces.RealInput u4
    annotation (Placement(transformation(extent={{-138,-40},{-98,0}})));
  Modelica.Blocks.Interfaces.RealOutput y
    annotation (Placement(transformation(extent={{100,46},{120,66}})));
  Modelica.Blocks.Interfaces.RealOutput y1
    annotation (Placement(transformation(extent={{100,22},{120,42}})));
  Modelica.Blocks.Interfaces.RealOutput y2
    annotation (Placement(transformation(extent={{100,-6},{120,14}})));
  Modelica.Blocks.Interfaces.RealOutput y3
    annotation (Placement(transformation(extent={{100,-34},{120,-14}})));
equation
  connect(u2, y) annotation (Line(
      points={{-120,42},{-10,42},{-10,56},{110,56}},
      color={0,0,127},
      smooth=Smooth.None));
  connect(u3, y1) annotation (Line(
      points={{-120,12},{-10,12},{-10,32},{110,32}},
      color={0,0,127},
      smooth=Smooth.None));
  connect(u4, y2) annotation (Line(
      points={{-118,-20},{-10,-20},{-10,4},{110,4}},
      color={0,0,127},
      smooth=Smooth.None));
  connect(u, y3) annotation (Line(
      points={{-118,-52},{-10,-52},{-10,-24},{110,-24}},
      color={0,0,127},
      smooth=Smooth.None));
  annotation (uses(Modelica(version="3.2.1")), Diagram(coordinateSystem(
          preserveAspectRatio=false, extent={{-100,-100},{100,100}}), graphics));
end FourInFourOuts;
