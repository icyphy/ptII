model GeneratorTest
  Modelica.Blocks.Continuous.PID PID(k = 0.3, Ti = 15) annotation(Placement(visible = true, transformation(origin = {-40.636,10.9541}, extent = {{-10,-10},{10,10}}, rotation = 0)));
  Modelica.Blocks.Math.Feedback feedback1 annotation(Placement(visible = true, transformation(origin = {32.5088,44.1696}, extent = {{-10,-10},{10,10}}, rotation = 0)));
  Modelica.Blocks.Sources.Constant const(k = 120) annotation(Placement(visible = true, transformation(origin = {-5.39631,43.9958}, extent = {{-10,-10},{10,10}}, rotation = 0)));
  GeneratorContactorLoad generatorcontactorload1 annotation(Placement(visible = true, transformation(origin = {4.68384,5.15222}, extent = {{-10,-10},{10,10}}, rotation = 0)));
  Modelica.Blocks.Sources.Step step1(offset = 0, startTime = 150) annotation(Placement(visible = true, transformation(origin = {-38.4075,-33.7237}, extent = {{-10,-10},{10,10}}, rotation = 0)));
equation
  connect(step1.y,generatorcontactorload1.contactor) annotation(Line(points = {{-27.4075,-33.7237},{-14.5199,-33.7237},{-14.5199,0.468384},{-4.53877,0.468384},{-4.53877,0.841275}}));
  connect(generatorcontactorload1.voltage,feedback1.u2) annotation(Line(points = {{13.8173,5.52693},{31.3817,5.52693},{31.3817,34.6604},{32.3185,34.6604},{32.3185,34.6604}}));
  connect(PID.y,generatorcontactorload1.drive) annotation(Line(points = {{-29.636,10.9541},{-3.74707,10.9541},{-3.74707,10.7728},{-3.74707,10.7728}}));
  connect(const.y,feedback1.u1) annotation(Line(points = {{5.60369,43.9958},{24.0283,43.9958},{24.0283,44.1696},{24.5088,44.1696}}));
  connect(feedback1.y,PID.u) annotation(Line(points = {{41.5088,44.1696},{54.417,44.1696},{54.417,75.265},{-72.7915,75.265},{-72.7915,10.2473},{-53.0035,10.2473},{-53.0035,10.2473}}));
  annotation(Icon(coordinateSystem(extent = {{-100,-100},{100,100}}, preserveAspectRatio = true, initialScale = 0.1, grid = {2,2})), Diagram(coordinateSystem(extent = {{-100,-100},{100,100}}, preserveAspectRatio = true, initialScale = 0.1, grid = {2,2})));
end GeneratorTest;

