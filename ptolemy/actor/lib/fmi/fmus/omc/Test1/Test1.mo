model Test1
  Real x (start = 1.0);
  parameter Real a = -1.0;
equation
  der(x) = a*x;
end Test1;
