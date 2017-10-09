/**
Class Used to Find Invalid Moves. Should not lead to Server Failure.
**/
public class InvalidMoveException extends Exception{
  public InvalidMoveException(String message){
    super(message);
  }
}
