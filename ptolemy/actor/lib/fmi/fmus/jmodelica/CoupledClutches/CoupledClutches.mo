// From http://www.maplesoft.com/documentation_center/online_manuals/modelica/Modelica_Mechanics_Rotational_Examples.html#Modelica.Mechanics.Rotational.Examples.CoupledClutches

model CoupledClutches 
  "Drive train with 3 dynamically coupled clutches"

  import SI = Modelica.SIunits;

  extends Modelica.Icons.Example;

  parameter SI.Frequency freqHz=0.2 
    "frequency of sine function to invoke clutch1";
  parameter SI.Time T2=0.4 "time when clutch2 is invoked";
  parameter SI.Time T3=0.9 "time when clutch3 is invoked";


  Modelica.Mechanics.Rotational.Components.Inertia J1(
    J=1,
    phi(fixed=true, start=0),
    w(start=10, fixed=true));
  Modelica.Mechanics.Rotational.Sources.Torque torque(useSupport=true);
  Modelica.Mechanics.Rotational.Components.Clutch clutch1(        peak=1.1, fn_max=20);
  Modelica.Blocks.Sources.Sine sin1(amplitude=10, freqHz=5);
  Modelica.Blocks.Sources.Step step1(startTime=T2);
  Modelica.Mechanics.Rotational.Components.Inertia J2(        J=1,
    phi(fixed=true, start=0),
    w(fixed=true));
  Modelica.Mechanics.Rotational.Components.Clutch clutch2(        peak=1.1, fn_max=20);
  Modelica.Mechanics.Rotational.Components.Inertia J3(        J=1,
    phi(fixed=true, start=0),
    w(fixed=true));
  Modelica.Mechanics.Rotational.Components.Clutch clutch3(        peak=1.1, fn_max=20);
  Modelica.Mechanics.Rotational.Components.Inertia J4(        J=1,
    phi(fixed=true, start=0),
    w(fixed=true));
  Modelica.Blocks.Sources.Sine sin2(
    amplitude=1,
    freqHz=freqHz,
    phase=1.57);
  Modelica.Blocks.Sources.Step step2(startTime=T3);
  Modelica.Mechanics.Rotational.Components.Fixed fixed;
equation 
  connect(torque.flange, J1.flange_a);
  connect(J1.flange_b, clutch1.flange_a);
  connect(clutch1.flange_b, J2.flange_a);
  connect(J2.flange_b, clutch2.flange_a);
  connect(clutch2.flange_b, J3.flange_a);
  connect(J3.flange_b, clutch3.flange_a);
  connect(clutch3.flange_b, J4.flange_a);
  connect(sin1.y, torque.tau);
  connect(sin2.y, clutch1.f_normalized);
  connect(step1.y, clutch2.f_normalized);
  connect(step2.y, clutch3.f_normalized);
  connect(fixed.flange, torque.support);
end CoupledClutches;
