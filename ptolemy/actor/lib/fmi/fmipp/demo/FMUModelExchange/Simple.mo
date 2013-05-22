within ;
model Simple "just a simple model - Compilation etc."

   parameter Real p = 0.1;
   Real x(start = 0.5);
   Real z;

equation
  der(x) = p * x;
  z = x *  x;

  annotation (uses(Modelica(version="3.2")));
end Simple;
