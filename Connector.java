import java.io.*;
import java.net.*;
import java.util.Hashtable;
import java.util.Set;
/**
Thread that interacts directly with the Client, handling messages from the socket.
**/
public class Connector extends Thread{
  Socket client;
  String username;
  Integer CurrentGameKey;
  Room CurrentGame;
  Integer uID;
  Boolean spinDown=false;
  public Connector(Socket socket, Integer userNumber){
    super("Connection"+socket);
    username = "newUser"+userNumber;
    client = socket;
    uID = userNumber;
  }
  /**
  Main Loop of Server Thread
  **/
  public void run(){
    try(
    ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
    ObjectInputStream in = new ObjectInputStream(client.getInputStream());
    ){
      ChatPacket inputPacket, outputPacket;
      while ((inputPacket = ((ChatPacket)in.readObject())) != null) {//blocks on input unless connection is severed
        outputPacket=process(inputPacket);
        out.writeObject(outputPacket);
        if(spinDown){//User is exiting, proceed with voluntary disconnect
          spinDown();
          break;
        }
      }
    }catch(IOException e){
      spinDown=true;
      spinDown();
      System.err.println("the user"+uID+"at Socket"+client+ "Experienced Error "+ e);
    }catch(ClassNotFoundException e){
      System.err.println(e);
    }
  }
/**
Choice Tree for Input from client to server.
**/
  public ChatPacket process(ChatPacket input){
    ChatPacket c;
    if(input.packetType.equals("Start")){//Handle Handshake
      c = new ChatPacket("Start","2,0,1");//2 games avaialble, codes 0 and 1
      return c;
    }
    else if(input.packetType.equals("KeepAlive")){//Handle Standard KeepAlive
    return new ChatPacket("KeepAlive");
  }
    else if(input.packetType.equals("WaitingForRoom")){//Handle Room Waiting KeepAlive
      if (CurrentGame.state==Room.PLAYING){
        c = new ChatPacket("RoomBegin", uID.toString());
        return c;
      }else{
        return new ChatPacket("KeepAlive");
      }
    }

    else if(input.packetType.equals("BeginPlay")){//Handle Ready For Game Message
      String output = CurrentGame.welcomeMessage();
      String type;
      if(CurrentGame.turn == uID){//Do I get to start?
        type = "YourTurn";
      }
      else{
        type = "OtherTurn";
      }
      c = new ChatPacket(type, output, input.gameID);
      return c;
    }
    else if(input.packetType.equals("WaitingForTurn")){//Handle KeepAlive while waiting on a turn
      if(CurrentGame.state==Room.DONE){
        String finishing = CurrentGame.finish();
        CurrentGame = null;
        return new ChatPacket("FinishGame",finishing);
      }
      return CurrentGame.getNextMessage();
    }
    else if(input.packetType.equals("Playing")){//Handle Message for Game Moves.
      try{
        return new ChatPacket("OtherTurn", CurrentGame.SendCommand(uID, input.packetMessage), input.gameID);
      }catch (InvalidMoveException e){
        return new ChatPacket("YourTurn", e.toString(), input.gameID);
      }
    }


    else if(input.packetType.equals("Menu")){//Handle Non-game related Commands
      String command = input.packetMessage;
      if(command.matches("username (.*)")){//Reset Username
        username=command.split(" ")[1];
        c = new ChatPacket("Message", "Username is now "+username);
        return c;
      }else if(command.equals("hello")){//Echo UserName
        c = new ChatPacket("Message", "hello "+username);
        return c;
      }else if(command.matches("room(.*)")){//run PrivateRoom
        String[] cPieces= command.split(" ");
        if(cPieces.length!=3){
          return new ChatPacket("Message", "Usage: room [password] [game] 0 for Convo 1 for TicTacToe)");
        }
        String pass = cPieces[1];
        int gamePick = Integer.parseInt(cPieces[2]);
        synchronized(ServerContainer.privateRooms){
          if(ServerContainer.privateRooms.containsKey(pass)){
            CurrentGame = ServerContainer.privateRooms.get(pass);
            CurrentGame.AddPlayer(username, uID, this);
            CurrentGame.state=Room.PLAYING;
            c = new ChatPacket("RoomBegin", pass);
            return c;
          }else{
            ServerContainer.privateRooms.put(pass,new Room(gamePick));
            CurrentGame = ServerContainer.privateRooms.get(pass);
            CurrentGame.AddPlayer(username,uID, this);
            c = new ChatPacket("NoRooms",pass);
            return c;
          }
        }
      }else if(command.matches("quickplay(.*)")){//run Public MatchMaking
        int gamePick;
        try{
          gamePick = Integer.parseInt(command.split(" ")[1]);
        }catch(ArrayIndexOutOfBoundsException e){
          return new ChatPacket("Message","Usage: quickplay [game], 0 for Convo 1 for TicTacToe\n");
        }
        if(gamePick!=0&&gamePick!=1){
          return new ChatPacket("Message","please select a game you want to play, 0 for Convo 1 for TicTacToe\n");
        }
        synchronized (ServerContainer.roomList){
          Integer gameKey = findGame(gamePick);
          if(gameKey==null){
            ServerContainer.roomList.put(uID,new Room(gamePick));
            Room newGame= ServerContainer.roomList.get(uID);
            newGame.AddPlayer(username, uID, this);
            CurrentGame = newGame;
            c = new ChatPacket("NoRooms",uID.toString());
            return c;
          }
          Room game = ServerContainer.roomList.get(gameKey);
          game.AddPlayer(username, uID, this);
          CurrentGame = game;
          c = new ChatPacket("RoomBegin", uID.toString());
          return c;
        }
      }else if(command.matches("status")){//get Server Status.
        synchronized(ServerContainer.servState){
          c = new ChatPacket("Message", "there are currently "+ServerContainer.servState.currentUsers+" users");
        }
        return c;
      }else if(command.matches("exit")){
        spinDown=true;
        c = new ChatPacket("SafeToExit","Have a wonderful day");
        return c;
      }else{
        c = new ChatPacket("Message", "Did Not understand Menu Selection:"+command);
        return c;
      }
    }else{
    c = new ChatPacket("Message", "sorry didnt get that " + input );//bad Command
    return c;
  }
  }
  /**
  Find a game on the server with the given Game ID. use whatever comes up first.
  **/
  public Integer findGame(int gameID){
    for(Integer k : ServerContainer.roomList.keySet()){
      if(ServerContainer.roomList.get(k).gameID==gameID){
        if(ServerContainer.roomList.get(k).state==Room.WAITING){
          return k;
        }
      }
    }
    return null;
  }
  /**
  Do all Operations necessary for a Client disconnect here.
  **/
  private void spinDown(){
    System.out.println("safely Ending Thread for "+uID);
    synchronized(ServerContainer.servState){
    ServerContainer.servState.currentUsers--;
    }
  }

}
