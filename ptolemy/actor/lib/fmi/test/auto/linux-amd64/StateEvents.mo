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

    model StateEvent2
      // This model has 8 state event at t=1.35s,
      // t = 2.39s, t = 3.85s, t = 4.9s, t = 6.35s,
      // t = 7.4s, t = 8.85s, t = 9.9s
      // when simulated from 0 to 10s.
    Real x1(start=1.1);
    Real x2(start=-2.5);
    Real x3(start=4);
    Real y;
    equation
      der(x1) = cos(2 * 3.14 * time/2.5);
    der(x2) = 1;
    der(x3) = -2;
    if (x1 > 1) then
      y = 1;
    else
      y = -1;
    end if;
    annotation (Documentation(info="<html>
<p>
This model has 8 state event at 1.35, 2.39, 
3.85, 4.9, 6.35, 7.4, 8.85, 9.9s when simulated from 0 to 10s.
</p>
</html>"));
    end StateEvent2;

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

    model StateEvent6
      // This model has 8 state event at t = 0.0s,
      //  t=1.25s, t = 2.5s, t = 3.75s, t = 5.0s,
      // t = 6.25s, t = 7.5s, t = 8.75s
      // when simulated from 0 to 10s.
    Real x1(start=1.0);
    Real x2(start=-2.5);
    Real x3(start=4);
    Real y;
    equation
      der(x1) = cos(2 * 3.14 * time/2.5);
    der(x2) = 1;
    der(x3) = -2;
    if (x1 > 1) then
      y = 1;
    else
      y = -1;
    end if;
    annotation (Documentation(info="<html>
<p>
This model has 8 state event at 0.0, 1.25, 2.5, 
3.75, 5.0, 6.25, 7.5, 8.75 when simulated from 0 to 10s.
</p>
</html>"));
    end StateEvent6;
  annotation (Documentation(revisions="<html>
<ul>
<li>
June 1 2016, by Thierry S. Nouidui:<br/>
First implementation.
</li>
</ul>
</html>"));
end StateEvents;
