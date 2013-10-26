model GeneratorContactorLoad
  Modelica.Blocks.Interfaces.RealInput drive annotation(Placement(visible = true, transformation(origin = {-89.4614,56.2061}, extent = {{-10,-10},{10,10}}, rotation = 0), iconTransformation(origin = {-89.4614,56.2061}, extent = {{-10,-10},{10,10}}, rotation = 0)));
  Modelica.Blocks.Interfaces.RealOutput voltage annotation(Placement(visible = true, transformation(origin = {91.3349,3.74707}, extent = {{-10,-10},{10,10}}, rotation = 0), iconTransformation(origin = {91.3349,3.74707}, extent = {{-10,-10},{10,10}}, rotation = 0)));
  Modelica.Blocks.Interfaces.RealInput contactor annotation(Placement(visible = true, transformation(origin = {-92.2261,-43.1095}, extent = {{-10,-10},{10,10}}, rotation = 0), iconTransformation(origin = {-92.2261,-43.1095}, extent = {{-10,-10},{10,10}}, rotation = 0)));
  Modelica.Mechanics.Rotational.Sources.Torque torque1 annotation(Placement(visible = true, transformation(origin = {-42.3846,38.8544}, extent = {{-10,-10},{10,10}}, rotation = 0)));
  Modelica.Electrical.Machines.BasicMachines.DCMachines.DC_PermanentMagnet dc_permanentmagnet1(TaOperational = 273.15, VaNominal = 100, IaNominal = 100, wNominal = 1425, TaNominal = 273.15, Ra = 0.05, TaRef = 293.15, alpha20a = 0, La = 293.15, Jr = 0.15, Js = 0.29) annotation(Placement(visible = true, transformation(origin = {14.6997,-48.1979}, extent = {{10,-10},{-10,10}}, rotation = 0)));
  Modelica.Electrical.Analog.Ideal.IdealClosingSwitch idealclosingswitch1 annotation(Placement(visible = true, transformation(origin = {-23.6749,-15.1943}, extent = {{10,-10},{-10,10}}, rotation = 90)));
  Modelica.Electrical.Analog.Sensors.VoltageSensor voltagesensor1 annotation(Placement(visible = true, transformation(origin = {14.4876,8.12721}, extent = {{10,-10},{-10,10}}, rotation = 180)));
  Modelica.Electrical.Machines.BasicMachines.DCMachines.DC_PermanentMagnet dcpm(TaOperational = 273.15, VaNominal = 100, IaNominal = 100, wNominal = 1425, TaNominal = 273.15, Ra = 0.05, TaRef = 293.15, alpha20a = 0, La = 293.15, Jr = 0.15, Js = 0.29) annotation(Placement(visible = true, transformation(origin = {15.1943,38.5159}, extent = {{-10,-10},{10,10}}, rotation = 180)));
  Modelica.Electrical.Analog.Basic.Ground ground1 annotation(Placement(visible = true, transformation(origin = {55.1071,-48.1965}, extent = {{-10,-10},{10,10}}, rotation = 0)));
  Modelica.Mechanics.Rotational.Components.Inertia inertia1(J = 3) annotation(Placement(visible = true, transformation(origin = {-41.9767,-48.1384}, extent = {{-10,-10},{10,10}}, rotation = 0)));
  Modelica.Blocks.Math.RealToBoolean realtoboolean1 annotation(Placement(visible = true, transformation(origin = {-56.2061,-14.5199}, extent = {{-10,-10},{10,10}}, rotation = 0)));
equation
  connect(realtoboolean1.y,idealclosingswitch1.control) annotation(Line(points = {{-45.2061,-14.5199},{-30.9133,-14.5199},{-30.9133,-15.4567},{-30.9133,-15.4567}}));
  connect(contactor,realtoboolean1.u) annotation(Line(points = {{-92.2261,-43.1095},{-80.0937,-43.1095},{-80.0937,-14.9883},{-68.3841,-14.9883},{-68.3841,-14.9883}}));
  connect(inertia1.flange_b,dc_permanentmagnet1.flange) annotation(Line(points = {{-31.9767,-48.1384},{5.15222,-48.1384},{5.15222,-48.7119},{5.15222,-48.7119}}));
  connect(voltagesensor1.n,ground1.p) annotation(Line(points = {{24.4876,8.12721},{56.2061,8.12721},{56.2061,-38.4075},{55.7377,-38.4075},{55.7377,-38.4075}}));
  connect(voltagesensor1.v,voltage) annotation(Line(points = {{14.4876,18.1272},{71.6628,18.1272},{71.6628,3.27869},{91.3349,3.27869},{91.3349,3.27869}}));
  connect(dcpm.pin_an,ground1.p) annotation(Line(points = {{21.1943,28.5159},{56.2061,28.5159},{56.2061,-38.8759},{55.7377,-38.8759},{55.7377,-38.8759}}));
  connect(voltagesensor1.p,idealclosingswitch1.p) annotation(Line(points = {{4.48763,8.12721},{-23.8876,8.12721},{-23.8876,-5.62061},{-24.356,-5.62061},{-24.356,-5.62061}}));
  connect(dc_permanentmagnet1.pin_an,ground1.p) annotation(Line(points = {{20.6997,-38.1979},{55.7377,-38.1979},{55.7377,-38.8759},{55.7377,-38.8759}}));
  connect(idealclosingswitch1.n,dc_permanentmagnet1.pin_ap) annotation(Line(points = {{-23.6749,-25.1943},{-23.6749,-37.9391},{8.43091,-37.9391},{8.43091,-37.9391}}));
  connect(idealclosingswitch1.p,dcpm.pin_ap) annotation(Line(points = {{-23.6749,-5.19435},{-23.6749,28.103},{9.36768,28.103},{9.36768,28.103}}));
  connect(torque1.flange,dcpm.flange) annotation(Line(points = {{-32.3846,38.8544},{4.59364,38.8544},{5.19431,37.1025},{5.19431,38.5159}}));
  connect(drive,torque1.tau) annotation(Line(points = {{-89.4614,56.2061},{-67.8445,56.2061},{-67.8445,38.5159},{-55.1237,38.5159},{-55.1237,38.5159}}));
  annotation(Diagram(coordinateSystem(extent = {{-100,-100},{100,100}}, preserveAspectRatio = true, initialScale = 0.1, grid = {2,2})), Icon(coordinateSystem(extent = {{-100,-100},{100,100}}, preserveAspectRatio = true, initialScale = 0.1, grid = {2,2}), graphics = {Rectangle(origin = {0.468384,4.44965}, extent = {{-80.5621,82.6698},{80.5621,-82.6698}})}));
end GeneratorContactorLoad;

