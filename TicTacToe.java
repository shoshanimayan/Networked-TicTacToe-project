/**
A slightly more complicated Example Class. Extends Game to Show how a the methods are enough to run these simple games.
**/
public class TicTacToe extends Game{
  private int[][] board;
  public TicTacToe(Room myRoom){
    super(myRoom);
    board =new int[][] {{0,0,0},{0,0,0},{0,0,0}};
  }
  public String welcomeMessage(){
    String s = "Welcome to Classic TicTacToe "+myRoom.players[0]+" and "+myRoom.players[1]+". Let me print you out a board\n"+
    printBoard()+
    "\n on your turn pick the number you want to choose."+myRoom.players[0]+" is X \n" ;
    return s;
  }
  public String move(String message) throws InvalidMoveException{
    int mark=1;
    if(message.equals("concede")){
        myRoom.state=Room.DONE;
        myRoom.changeTurnSwitch();
        return "You Conceded";
    }
    if(myRoom.turnSwitch==1){
      mark = -1;
    }
    if(getBoard(message)==0){
      SetBoard(message, mark);
    }else{
      throw new InvalidMoveException("mark already placed their, please put something on an empty location");
    }
    if(isGameFinished()){
      myRoom.state=Room.DONE;
    }
    return printBoard();
  }
  public String finish(){
    return "The final board is \n"+printBoard()+"\n"+myRoom.players[myRoom.turnSwitch]+" is the winner";
  }

  private int getBoard(String message) throws InvalidMoveException{
    if(message.equals("1")){
      return board[0][0];
    }
    else if(message.equals("2")){
      return board[0][1];
    }
    else if(message.equals("3")){
      return board[0][2];
    }
    else if(message.equals("4")){
      return board[1][0];
    }
    else if(message.equals("5")){
      return board[1][1];
    }
    else if(message.equals("6")){
      return board[1][2];
    }
    else if(message.equals("7")){
      return board[2][0];
    }
    else if(message.equals("8")){
      return board[2][1];
    }
    else if(message.equals("9")){
      return board[2][2];
    }
    else{
      throw new InvalidMoveException("not a valid location, send 1-9");
    }
  }
  private void SetBoard(String message, int mark) throws InvalidMoveException{
    if(message.equals("1")){
        board[0][0]=mark;
    }
    else if(message.equals("2")){
      board[0][1]=mark;
    }
    else if(message.equals("3")){
      board[0][2]=mark;
    }
    else if(message.equals("4")){
      board[1][0]=mark;
    }
    else if(message.equals("5")){
      board[1][1]=mark;
    }
    else if(message.equals("6")){
      board[1][2]=mark;
    }
    else if(message.equals("7")){
      board[2][0]=mark;
    }
    else if(message.equals("8")){
      board[2][1]=mark;
    }
    else if(message.equals("9")){
      board[2][2]=mark;
    }
    else{
      throw new InvalidMoveException("not a valid location, send 1-9");
    }
  }
  private boolean isGameFinished(){
    int[] sums = new int[]{
      board[0][0]+board[0][1]+board[0][2],//Rows
      board[1][0]+board[1][1]+board[1][2],
      board[2][0]+board[2][1]+board[2][2],

      board[0][0]+board[1][0]+board[2][0],//Col
      board[0][1]+board[1][1]+board[2][1],
      board[0][2]+board[1][2]+board[2][2],

      board[0][0]+board[1][1]+board[2][2],//Dags
      board[0][2]+board[1][1]+board[2][0],
    };
    for (int i : sums){
      if(i == 3 || i == -3){
        return true;
      }
    }
    return false;


  }
  private String printBoard(){
    String out ="";
    Integer count = 0;
    for(int i = 0 ; i<3; i++){
      for(int j =0 ; j<3; j++){
        count++;
        if(board[i][j]==0){
          out+=" "+count.toString();
        }else if (board[i][j]==1) {
          out+=" X";
        }else{
          out+=" O";
        }
      }
      out+="\n";
    }
    return out;
  }
}
