function [bias, mode, d] = getbias(position, theta, current_mode)
%[BIAS, MODE] = GETBIAS(POSITION, THETA, CURRENT_MODE)
%
%   Gets the current SoftWalls bias.  It first calculates the bias mode.  Then
%   it 
%
%   Mode 0: No Circles Touching--No Bias
%   Mode 1: Left Circle Touching--Right Circle Outside (No Bias) or 
%           Apply Rightward Bias
%   Mode 2: Right Circle Touching--Left Circle Outside (No Bias) or
%           Apply Leftward Bias
%
%   Possible Mode Transitions:
%   0 -> 0, 1, 2
%   1 -> 0, 1, 2*
%   2 -> 0, 1*, 2
%
%   * These transitions can only happen when zero bias is being applied. 

speed = 1;
minturningradius = 0.15;

maxturningrate = speed/minturningradius;

[left, right] = getcenters(position, theta);
outsideleft = outside(left);
outsideright = outside(right);

d = distance(position);

switch current_mode
case 0
    if (outsideleft & outsideright)
        mode = 0;
        bias = 0;
    elseif (~outsideleft & outsideright)
        mode = 1;
        bias = 0;
    elseif (outsideleft & ~outsideright)
        mode = 2;
        bias = 0;
    else %if (~outsideleft & ~outsideright)
        %Now both have entered the no-fly zone.
        %Bias leftward to avoid nondeterminism.
        mode = 2;
        bias = 2 * maxturningrate * biasfactor(left);
    end
case 1
    if (outsideleft & outsideright)
        mode = 0;
        bias = 0;
    else
        bias = - 2 * maxturningrate * biasfactor(right);
        if bias ~= 0    % nonzero bias, keep turning
            mode = 1;
        elseif (~outsideright & outsideleft)
            % Swith to mode 2, which is safe since there is zero bias.
            mode = 2;
        else % (if ~outsideleft) stay in this mode
            mode = 1;
        end
    end
case 2
    if (outsideleft & outsideright)
        mode = 0;
        bias = 0;
    else
        bias = 2 * maxturningrate * biasfactor(left);
        if bias ~= 0    % nonzero bias, keep turning
            mode = 2;
        elseif (outsideright & ~outsideleft)
            % Swith to mode 1, which is safe since there is zero bias.
            mode = 1;
        else % (if ~outsideright) stay in this mode
            mode = 2;
        end
    end
otherwise
    disp(current_mode);
    error('Error--invalid mode input given to getbias.m');
end

% end getbias

%
%=============================================================================
% getcenters
% Gets the centers of the circle positions.
%=============================================================================
%

function [leftposition, rightposition] = getcenters(position,theta)
%[L,R] = GETCENTERS(POSITION,THETA)
%
%   Given the (x,y) (=) position and heading theta of the aircraft, it 
%   calculates the centers of the 2 circles used in flight calculation.
%
%   L corresponds to the circle on the aircraft's left and R to the right.

offset = [0.15;
    0];

leftposition = rotmatrix(theta + pi/2) * offset + position';

rightposition = rotmatrix(theta - pi/2) * offset + position';

% end getcenters

%
%=============================================================================
% rotmatrix
% Returns a 2D rotation matrix.
%=============================================================================
%


function R = rotmatrix(theta)
%R = ROTMATRIX(THETA)
%
%   Returns a 2D rotation matrix (in SO(2)) corresponding to right-handed
%   rotation by theta.

R = [cos(theta) -sin(theta);
    sin(theta) cos(theta)];

% end rotmatrix

%
%=============================================================================
% distance
% Returns the distance of a point from the no-fly zone
%=============================================================================
%

function d = distance(position)
% Measures the distance from the given position to the SoftWall, assuming
% the point is outside the softwall.
% The SoftWall is a square with corners at (2,2) and (4,4).

a1 = 2;
a2 = 4;
b1 = -1;
b2 = 1;
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

% end distance

%
%=============================================================================
% outside
% Returns true if the circle is outside the no-fly zone
%=============================================================================
%

function boolean = outside(position)
%B = OUTSIDE(POSITION)
%
%   Returns true if a circle with center (x,y) = (position(1),position(2)) and
%   radius 0.25 is outside a square with corners at (2,2) and (4,4)

a1 = 2;
a2 = 4;
b1 = -1;
b2 = 1;
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

%
%=============================================================================
% biasfactor
% Returns a bias weighting between 0 and 1.
%=============================================================================
%

function b = biasfactor(position)
%B = BIASFACTOR(POSITION)
%
% Calculates the bias applied, based on the position's distance from the 
% SoftWall.  The position is the center of the circle for which bias is being
% applied.  B is a number between 0 and 1, regulating no of full bias.
% We assume the inner circle has radiua 0.15 and the outer circle 0.25.

router = 0.25;
rinner = 0.15;

d = distance(position);

b = max((router - d) / (router - rinner), 0);

% disp([d b]);

% The 0 is added to calculate a possilbe zero bias.

% end biasfactor