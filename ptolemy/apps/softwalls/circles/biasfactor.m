function b = biasfacotr(position)
%B = BIASFACTOR(POSITION)
%
% Calculates the bias applied, based on the position's distance from the 
% SoftWall.  The position is the center of the circle for which bias is being
% applied.  B is a number between 0 and 1, regulating no of full bias.
% We assume the inner circle has radiua 0.15 and the outer circle 0.25.

router = 0.25;
rinner = 0.15;

d = distance(position);

b = (router - d) / (router - rinner);