class Main :
  "Read the input and send the square to the output"
  def fire(self) :
    token = self.input.get(0)
    self.output.broadcast(token.multiply(token))
    return
