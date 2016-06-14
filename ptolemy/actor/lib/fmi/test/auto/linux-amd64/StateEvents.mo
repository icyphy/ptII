within ;
package StateEvents "This package contains a collection of models
  which are used to test time and state events.
  These models need to be exported as FMUs 2.0
  for model exchange and imported in the 
  Ptolemy II system models examples."
  model StateEvent1
    // This model has one state event at t=1.75s
    // when simulated from 0 to 10s.
    Real x1(start=1);
    Real x2(start=-2.5);
    Real x3(start=4);
    Real y;
  equation
    der(x1) = -1;
    der(x2) = 1;
    der(x3) = -2;
    if (x1 > x2) then
      y = 1;
    else
      y = -1;
    end if;
    annotation (Documentation(info="<html>
<p>
This model has one state event at t=1.75s when simulated from 0 to 10s.
</p>
</html>"));
  end StateEvent1;

  model StateEvent4
    // This model has one time event at t=1s
    // and one state event at t = 1.75s
    // when simulated from 0 to 10s.
    Real x1(start=1);
    Real x2(start=-2.5);
    Real x3(start=4);
    Real y;
    Real y1;
  equation
    der(x1) = -1;
    der(x2) = 1;
    der(x3) = -2;
    if ((x1 > x2)) then
      y = 1;
    else
      y = -1;
    end if;

    if (time >= 1) then
      y1 = 10;
    else
      y1 = 3;
    end if;

    annotation (uses(Modelica(version="3.2.1")), Documentation(info="<html>
<p>
This model has one time event at t=1s 
and one state event at t = 1.75s 
when simulated from 0 to 10s.
</p>
</html>"));
  end StateEvent4;

  model StateEvent5
    // This model has two state events
    //  at t=1s  and at t = 1.75s
    // when simulated from 0 to 10s.
    Real x1(start=1);
    Real x2(start=-2.5);
    Real x3(start=4);
    Real y;
    Real y1;
  equation
    der(x1) = -1;
    der(x2) = 1;
    der(x3) = -2;
    if ((x1 > x2)) then
      y = 1;
    else
      y = -1;
    end if;

    if (time > 1) then
      y1 = 10;
    else
      y1 = 3;
    end if;

    annotation (uses(Modelica(version="3.2.1")), Documentation(info="<html>
<p>
This model has two state events
at t=1s  and at t = 1.75s 
when simulated from 0 to 10s.
</p>
</html>"));
  end StateEvent5;
  annotation (Documentation(revisions="<html>
<ul>
<li>
June 1 2015, by Thierry S. Nouidui:<br/>
First implementation.
</li>
</ul>
</html>"));
end StateEvents;
