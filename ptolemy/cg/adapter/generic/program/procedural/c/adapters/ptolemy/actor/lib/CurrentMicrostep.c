/***fireBlock***/
struct Director* director = (*(actor->getDirector))(actor);
$put(output, (*(((struct DEDirector*)director)->getMicrostep))((struct DEDirector*)director));
/**/
