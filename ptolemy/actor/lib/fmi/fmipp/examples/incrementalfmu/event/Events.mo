within ;
model Events "different type of events" 
  
  Real x(start = 1);
  Real z;
  parameter Real k = 10;
  // discrete Real zpts;
  // discrete Boolean sign(start=true);
  
equation 
  der(x) = if z > 0.1 then -1 else 1;
  z = sin(k * time * Modelica.Constants.pi);
  // when (z<0 and sign) or (z>0 and not sign) then
  //  zpts = time;
  //  sign = not pre(sign);
  // end when;
  annotation (uses(Modelica(version="2.2.2")));
end Events;
