within ;
model RLC "RLC circuit"

  Modelica.Electrical.Analog.Basic.Resistor R1(R=1) "Resistor"
    annotation (Placement(transformation(extent={{-20,30},{0,50}})));
  Modelica.Electrical.Analog.Basic.Capacitor C(C=1) "Capacitor" annotation (
      Placement(transformation(
        extent={{-10,-10},{10,10}},
        rotation=270,
        origin={20,0})));
  Modelica.Electrical.Analog.Basic.Inductor L(L=1) "Inductor"
    annotation (Placement(transformation(extent={{-20,10},{0,30}})));
  Modelica.Electrical.Analog.Sources.StepVoltage V(V=1, startTime=0.1)
    "Voltage" annotation (Placement(transformation(
        extent={{-10,-10},{10,10}},
        rotation=270,
        origin={-40,0})));
  Modelica.Electrical.Analog.Basic.Resistor R2(R=1) "Resistor" annotation (
      Placement(transformation(
        extent={{-10,-10},{10,10}},
        rotation=270,
        origin={20,-30})));
  Modelica.Electrical.Analog.Basic.Ground ground
    annotation (Placement(transformation(extent={{10,-80},{30,-60}})));
equation
  connect(V.p, R1.p) annotation (Line(
      points={{-40,10},{-40,40},{-20,40}},
      color={0,0,255},
      smooth=Smooth.None));
  connect(L.p, V.p) annotation (Line(
      points={{-20,20},{-40,20},{-40,10}},
      color={0,0,255},
      smooth=Smooth.None));
  connect(L.n, C.p) annotation (Line(
      points={{0,20},{20,20},{20,10}},
      color={0,0,255},
      smooth=Smooth.None));
  connect(R1.n, C.p) annotation (Line(
      points={{0,40},{20,40},{20,10}},
      color={0,0,255},
      smooth=Smooth.None));
  connect(C.n, R2.p) annotation (Line(
      points={{20,-10},{20,-20}},
      color={0,0,255},
      smooth=Smooth.None));
  connect(R2.n, V.n) annotation (Line(
      points={{20,-40},{20,-50},{-40,-50},{-40,-10}},
      color={0,0,255},
      smooth=Smooth.None));
  connect(ground.p, V.n) annotation (Line(
      points={{20,-60},{20,-50},{-40,-50},{-40,-10}},
      color={0,0,255},
      smooth=Smooth.None));
  annotation (uses(Modelica(version="3.2.1")), Diagram(coordinateSystem(
          preserveAspectRatio=false, extent={{-100,-100},{100,100}}), graphics),
    experiment(StopTime=20, Tolerance=1e-05),
    __Dymola_experimentSetupOutput);
end RLC;
