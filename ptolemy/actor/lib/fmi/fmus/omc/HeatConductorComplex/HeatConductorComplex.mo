model HeatConductorComplex
  Modelica.Thermal.HeatTransfer.Sensors.TemperatureSensor temSen "Temperature sensor" annotation(Placement(visible = true, transformation(origin = {140, 0}, extent = {{-10, -10}, {10, 10}}, rotation = 0)));
  Modelica.Thermal.HeatTransfer.Components.ThermalConductor con1(G = 1) "Thermal conductor" annotation(Placement(visible = true, transformation(origin = {40, 0}, extent = {{-10, -10}, {10, 10}}, rotation = 0)));
  Modelica.Thermal.HeatTransfer.Components.Convection con "Convectoin" annotation(Placement(visible = true, transformation(origin = {10, 0}, extent = {{10, -10}, {-10, 10}}, rotation = 0)));
  Modelica.Blocks.Sources.Constant const(k = 5) annotation(Placement(visible = true, transformation(origin = {-20, 30}, extent = {{-10, -10}, {10, 10}}, rotation = 0)));
  Modelica.Thermal.HeatTransfer.Components.ThermalConductor con2(G = 1) "Thermal conductor" annotation(Placement(visible = true, transformation(origin = {80, 0}, extent = {{-10, -10}, {10, 10}}, rotation = 0)));
  Modelica.Thermal.HeatTransfer.Components.HeatCapacitor cap2(C = 1000) "Thermal capacitor" annotation(Placement(visible = true, transformation(origin = {100, 22}, extent = {{-10, -10}, {10, 10}}, rotation = 0)));
  Modelica.Thermal.HeatTransfer.Sources.PrescribedTemperature preTem "Prescribed temperature" annotation(Placement(visible = true, transformation(origin = {-20, 0}, extent = {{-10, -10}, {10, 10}}, rotation = 0)));
  Modelica.Thermal.HeatTransfer.Components.HeatCapacitor cap1(C = 1000) "Thermal capacitor" annotation(Placement(visible = true, transformation(origin = {60, 20}, extent = {{-10, -10}, {10, 10}}, rotation = 0)));
  Modelica.Blocks.Sources.Sine sine1(amplitude = 5, freqHz = 1 / 86400, offset = 273.15 + 20) annotation(Placement(visible = true, transformation(origin = {-60, 0}, extent = {{-10, -10}, {10, 10}}, rotation = 0)));
  Modelica.Blocks.Interfaces.RealInput Q_flow(unit = "W") "Heat flow rate" annotation(Placement(visible = true, transformation(origin = {-110, -60}, extent = {{-10, -10}, {10, 10}}, rotation = 0), iconTransformation(origin = {-106, -40}, extent = {{-10, -10}, {10, 10}}, rotation = 0)));
  Modelica.Blocks.Interfaces.RealOutput T(unit = "K") "Temperature" annotation(Placement(visible = true, transformation(origin = {170, 0}, extent = {{-10, -10}, {10, 10}}, rotation = 0), iconTransformation(origin = {166, 0}, extent = {{-10, -10}, {10, 10}}, rotation = 0)));
  Modelica.Thermal.HeatTransfer.Sources.PrescribedHeatFlow preHeaFlo(T_ref = 293.15, alpha = 0) "Prescribed heat flow rate" annotation(Placement(visible = true, transformation(origin = {50, -60}, extent = {{-10, -10}, {10, 10}}, rotation = 0)));
equation
  connect(preHeaFlo.port, con2.port_b) annotation(Line(points = {{60, -60}, {100, -60}, {100, 0}, {90, 0}}, color = {191, 0, 0}));
  connect(preHeaFlo.Q_flow, Q_flow) annotation(Line(points = {{40, -60}, {-104, -60}}, color = {0, 0, 127}));
  connect(T, temSen.T) annotation(Line(points = {{170, 0}, {152, 0}, {152, 0}, {150, 0}, {150, 0}}, color = {0, 0, 127}));
  connect(preTem.T, sine1.y) annotation(Line(points = {{-32, 0}, {-48, 0}, {-48, 0}, {-48, 0}}, color = {0, 0, 127}));
  connect(cap1.port, con2.port_a) annotation(Line(points = {{60, 10}, {59, 10}, {59, 0}, {70, 0}}, color = {191, 0, 0}));
  connect(preTem.port, con.fluid) annotation(Line(points = {{-10, 0}, {0, 0}, {0, 0}, {0, 0}}, color = {191, 0, 0}));
  connect(con2.port_b, cap2.port) annotation(Line(points = {{90, 0}, {100, 0}, {100, 12}, {100, 12}}, color = {191, 0, 0}));
  connect(con2.port_a, con1.port_b) annotation(Line(points = {{70, 0}, {50, 0}, {50, 0}, {50, 0}}, color = {191, 0, 0}));
  connect(con2.port_b, temSen.port) annotation(Line(points = {{90, 0}, {130, 0}}, color = {191, 0, 0}));
  connect(con.Gc, const.y) annotation(Line(points = {{10, 10}, {10, 30}, {-9, 30}}, color = {0, 0, 127}));
  connect(con1.port_a, con.solid) annotation(Line(points = {{30, 0}, {20, 0}}, color = {191, 0, 0}));
  annotation(uses(Modelica(version = "3.2.1")), Icon(coordinateSystem(extent = {{-100, -100}, {100, 100}}, preserveAspectRatio = true, initialScale = 0.1, grid = {2, 2})), Diagram(coordinateSystem(extent = {{-100, -100}, {160, 100}}, preserveAspectRatio = true, initialScale = 0.1, grid = {2, 2})));
end HeatConductorComplex;