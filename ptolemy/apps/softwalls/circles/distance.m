function d = distance(position)
%Measures the distance from the given position to the SoftWall, assuming
%the point is outside the softwall.
%The SoftWall is a square with corners at (2,2) and (4,4).

a1 = 2;
a2 = 4;
b1 = 2;
b2 = 4;
r = 0.25;

x = position(1);
y = position(2);

if (y > b1 & y < b2)
    % The point is directly to the left or right of the SoftWall.
    dsquared = min([(x - a1)^2 (x - a2)^2]);
elseif (x > a1 & x < a2)
    % The point is directly on top or bottom of teh SoftWall.
    dsquared = min([(y - b1)^2 (y - b2)^2]);
else
    % The point will be closest to one of the corners.
    dsquared = min([((x - a1)^2 + (y - b1)^2) ...
            ((x - a1)^2 + (y - b2)^2) ...
            ((x - a2)^2 + (y - b1)^2) ...
            ((x - a2)^2 + (y - b2)^2)]);
end

d = sqrt(dsquared);