within ;
model LorenzAttractor
  Real x1(start = 1.0);
  Real x2(start = 1.0);
  Real x3(start = 1.0);
  Modelica.Blocks.Interfaces.RealInput u;
  parameter Real sigma = 10.0;
  parameter Real lambda = 25.0;
  parameter Real b = 2.0;
equation
  der(x1) = sigma*(x2-x1);
  der(x2) = (lambda-x3)*x1-x2;
  der(x3) = x1*x2 - b*x3-u;
  annotation (uses(Modelica(version="3.2.1")));
end LorenzAttractor;
