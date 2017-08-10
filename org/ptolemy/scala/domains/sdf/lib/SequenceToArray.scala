import ptolemy.kernel.CompositeEntity  


import org.ptolemy.scala.implicits._
import org.ptolemy.scala.actor.TypedAtomicActor 
 

/**
 * @author Moez Ben Hajhmida
 *
 */
case class SequenceToArray(name: String)(implicit container: CompositeEntity) extends TypedAtomicActor{
  val sequenceToArray = new ptolemy.domains.sdf.lib.SequenceToArray (container, name) 
  var arrayLength = sequenceToArray.arrayLength
  var input = sequenceToArray.input
  var input_tokenConsumptionRate = sequenceToArray.input_tokenConsumptionRate
  var output = sequenceToArray.output
  var output_tokenProductionRate = sequenceToArray.output_tokenProductionRate
  var output_tokenInitProduction = sequenceToArray.output_tokenInitProduction

 
  def getActor():ptolemy.domains.sdf.lib.SequenceToArray = sequenceToArray


  def set(parameterName: String, expressionString: String):SequenceToArray = {   
    setExpression (sequenceToArray, parameterName, expressionString)
    this 
  }
}
