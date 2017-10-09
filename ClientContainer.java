
import java.io.*;
import java.net.*;
import java.util.Arrays;
/**
Handles connection to server and sending it messages. Block on waiting to Receive.
**/
public class ClientContainer{
  static String state = "Menu";//Begin in menu state whenever opened
  static Integer gameID = 0;
  static Boolean showKeepAlive = false;
  static Integer[] listOfGame;
  final static String usage=
  "The Following Commands are avaialable\n"+
  "quickplay [gameID], 0 for conversation, 1 for TicTacToe\n"+
  "room [password] [gameID], 0 for conversation, 1 for TicTacToe\n"+
  "username [newName]\n"+
  "hello\n"+
  "status\n"+
  "exit\n";
  public static void main(String[] args) {
    if(args.length != 2){
      System.err.println(
      "Usage: needs three arguments, java ClientContainer <hostname> <showKeepAlive>"
      );
      System.exit(1);
    }
    String hostName = args[0];
    int portNumber = 19;//Defined in RFC
    showKeepAlive=Boolean.parseBoolean(args[1]);
    //Java 7's new Try with recources. Automatically closes all opened recources when try ends or error is caught.
    try(
    Socket echoSocket = new Socket(hostName,portNumber);
    ObjectOutputStream out = new ObjectOutputStream(echoSocket.getOutputStream());
    ObjectInputStream in = new ObjectInputStream(echoSocket.getInputStream());

    BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in))
    ){
      echoSocket.setSoTimeout(2000);//set timeout on server to 20 seconds. If no keepalive messages, throw error and kill connection
      String userInput;
      out.writeObject(new ChatPacket("Start"));//Handshake and get usage
      out.flush();
      ChatPacket welcome = (ChatPacket)in.readObject();
      ClientProtocol.processProcedure(welcome);
	System.out.println(usage);
      while(!state.equals("Exit")){//Main Control loop. If Exit state is reached, end loop.
        if(state.equals("Menu")){//Take input from user to send to Server for menu ops
          while(!stdin.ready()){//if no keyboard inputs, send keepalives
            out.writeObject(new ChatPacket("KeepAlive"));
            ChatPacket KA=(ChatPacket)in.readObject();
            if(KA==null){
              System.out.println("connection Lost, exiting Client");
              state="Exit";
            }
            ClientProtocol.processProcedure(KA);
          }
          if((userInput = stdin.readLine()) != null){
            if(validateMenu(userInput)){
              out.writeObject(new ChatPacket(state, userInput));
              ChatPacket message= (ChatPacket)in.readObject();//blocks on call :(
              ClientProtocol.processProcedure(message);
            }else{
              System.out.println(usage);
            }
          }
        }
        if(state.equals("WaitingForRoom")){//continue waiting for room
          out.writeObject(new ChatPacket(state));//send test to keepalive and also if room is full
          out.flush();
          ChatPacket message= (ChatPacket)in.readObject();
          ClientProtocol.processProcedure(message);
        }
        if(state.equals("BeginPlay")){
          out.writeObject(new ChatPacket(state, "ready",gameID));//send that client is ready to begin game once room is full
          out.flush();
          ChatPacket message= (ChatPacket)in.readObject();
          ClientProtocol.processProcedure(message);
        }
        if(state.equals("Playing")){//send command to server for paly
          while(!stdin.ready()){//while waiting for input, send keepAlives, so as to not block on user input.
            out.writeObject(new ChatPacket("KeepAlive","", gameID));
            out.flush();
            ChatPacket KA=(ChatPacket)in.readObject();
            if(KA==null){
              System.out.println("connection Lost, exiting Client");
              state="Exit";
            }
            ClientProtocol.processProcedure(KA);
          }
          if((userInput = stdin.readLine()) != null){
            out.writeObject(new ChatPacket(state, userInput, gameID));
            ChatPacket message= (ChatPacket)in.readObject();
            ClientProtocol.processProcedure(message);
          }
        }
        while(state.equals("WaitingForTurn")){//Do while Waiting for your turn
          if(stdin.ready()){
            String trash = stdin.readLine();//Throw away in input so that you have a fresh buffer when your turn arrives
            System.out.println("wait your turn, threw away "+ trash);
          }
          out.writeObject(new ChatPacket(state));//send KeepAlive and check if my turn.
          out.flush();
          ChatPacket message= (ChatPacket)in.readObject();
          ClientProtocol.processProcedure(message);
        }
      }
    }catch (UnknownHostException e) {
      System.err.println("Don't know about host " + hostName);
    }
    catch (IOException e) {
      System.err.println("Something Happened to the server! It may not be taking connections at this port or it just died" +
      hostName);
    }
    catch (ClassNotFoundException e){
      System.err.println("Unexpected type of Object" + e);
    }finally{
      System.out.println("Exiting Client");
    }
  }
  /**
  Validate Menu Options. We only do menu options because of the two places where we have user input, only Menu is not game specific.
  **/
  public static boolean validateMenu(String input){
    boolean valid = false;
    if(state == "Menu"){
      if(input.matches("username (.*)")){
        if(input.split(" ").length==2){
          valid = true;
        }
      }else if(input.matches("quickplay(.*)")){
        if(input.split(" ").length==2){
          if(Arrays.asList(listOfGame).contains(Integer.parseInt(input.split(" ")[1]))){
            valid = true;
          }else{
          System.out.println("not a valid game");}
        }else{
        System.out.println("wrong num of arguments");}
      }else if(input.matches("room(.*)")){
        if(input.split(" ").length==3){
          if(Arrays.asList(listOfGame).contains(Integer.parseInt(input.split(" ")[2]))){
            valid = true;
          }else{
          System.out.println("not a valid game");}
        }else{
        System.out.println("wrong num of arguments");}
      }
      else if((input.matches("hello"))||(input.matches("status"))||(input.matches("exit"))){
        valid = true;
      }
    }
    return valid;
  }
}
