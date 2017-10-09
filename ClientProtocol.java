import java.io.*;
import java.net.*;
import java.util.Arrays;
/**
Choice Tree for input from server to Client
**/
public class ClientProtocol{
  public static void processProcedure(ChatPacket input){
    if(input.packetType.equals("Start")){
      String string = "Welcome to GameServer!\n"+ClientContainer.usage;
      String[] startingMessage = input.packetMessage.split(",");
      int n = Integer.parseInt(startingMessage[0]);
      Integer[] listOfGame = new Integer[n];
      for (int i = 0;i<listOfGame.length ; i++ ) {//get all avaialable game keys
        listOfGame[i]=Integer.parseInt(startingMessage[i+1]);
      }
      System.out.println(Arrays.asList(listOfGame));
      ClientContainer.listOfGame =listOfGame;
      System.out.println(ClientContainer.usage);
      return;
    }
    if(input.packetType.equals("Message")){//handle generic message
      System.out.println(input);
      return;
    }
    if(input.packetType.equals("NoRooms")){//handle lack of Rooms found
      System.out.println("NoRoom : waiting for room with name of"+input.packetMessage);
      ClientContainer.state="WaitingForRoom";
      return;
    }
    if(input.packetType.equals("RoomBegin")){//let server know that client is ready to play
      System.out.println("got Into game");
      ClientContainer.state="BeginPlay";
      return;
    }
    if(input.packetType.equals("YourTurn")){//handle notice that it is the clients turn
      System.out.println(input.packetMessage);
      ClientContainer.state="Playing";
      return;
    }
    if(input.packetType.equals("OtherTurn")){//handle notice that it is someone elses turn
      System.out.print(input.packetMessage);
      try{
        Thread.sleep(3000);//check every 3 seconds
      }catch(InterruptedException e){
        System.err.println("Sleep interrupted" + e);
        System.exit(1);
      }
      ClientContainer.state="WaitingForTurn";
      return;
    }
    if(input.packetType.equals("FinishGame")){//handle endo of game
      ClientContainer.state="Menu";
      ClientContainer.gameID=0;
      System.out.println(input.packetMessage);
      System.out.println("Game was finished. Returning to Menu");
      return;
    }
    if(input.packetType.equals("Error")){//handle error from server. Not currently in use, but nice to have if need be.
      ClientContainer.state="Exit";
      return;
    }
    if(input.packetType.equals("KeepAlive")){//handle notice that Server Connection is fine.
      try{
        if(ClientContainer.showKeepAlive){//only print message if this is On for debug
        System.out.println(input);
        }
        Thread.sleep(500);
      }catch(InterruptedException e){
        System.err.println("Sleep interrupted" + e);
        System.exit(1);
      }
      return;
    }
    if(input.packetType.equals("SafeToExit")){//Handle Exit Procedure
      System.out.println("Ready to Exit");
      ClientContainer.state="Exit";
      return;
    }
    System.out.println("cound not process packet "+ input);//Error in packet.
    return;
  }
}
