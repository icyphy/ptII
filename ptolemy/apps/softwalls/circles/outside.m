function boolean = outside(position)
%B = OUTSIDE(POSITION)
%
%   Returns true if a circle with center (x,y) = (position(1),position(2)) and
%   radius 0.25 is outside a square with corners at (2,2) and (4,4)

a1 = 2;
a2 = 4;
b1 = 2;
b2 = 4;
r = 0.25;

x = position(1);
y = position(2);

if (x + r < a1 | ...
    x - r > a2 | ...
    y + r < b1 | ...
    y - r > b2 | ...
    outsidecorner(x, y, a1, a2, b1, b2, r))
        boolean = 1;
else
    boolean = 0;
end

% end oustide

%
%=============================================================================
% outsidecorner
% Returns true if the circle is outside all 4 corners.
%=============================================================================
%

function b = outsidecorner(x, y, a1, a2, b1, b2, r)
% Returns true if the circle at center (x,y) is outside all of the 4 corners.
%

if ((x < a1 | x > a2) & ...
    (y < b1 | y > b2) & ...
    (x - a1)^2 + (y - b1)^2 > r^2 & ...
    (x - a1)^2 + (y - b2)^2 > r^2 & ...
    (x - a2)^2 + (y - b1)^2 > r^2 & ...
    (x - a2)^2 + (y - b2)^2 > r^2)
		b = 1;
else
    b = 0;
end

%end outsidecorner