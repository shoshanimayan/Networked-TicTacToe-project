import java.net.*;
import java.io.*;
import java.util.Hashtable;
/**
Main Class for Server, keeps all the shared data of the server and spins up all threads.
**/
public class ServerContainer{
  public static ServerState servState = new ServerState();
  public static Hashtable<Integer,Room> roomList = new Hashtable<Integer,Room>();
  public static Hashtable<String, Room> privateRooms = new Hashtable<String,Room>();


  public static void main(String[] args) throws IOException{
    int portNumber = 19;//defined in RFC
    startServer(portNumber);
  }
/**
Method to begin server at the given portNumber.
Keeps Track of How Many users are currently on.
**/
  public static void startServer(int portNumber){
      int userNumber=0;
      boolean listening = true;
    try(
      ServerSocket serverSocket = new ServerSocket(portNumber);
        ){
          while(listening){
            Socket newClient=serverSocket.accept();
            userNumber++;
            servState.currentUsers++;
            new Connector(newClient,userNumber).start();

          }
        }catch (IOException e) {
            System.out.println("Exception caught when trying to listen on port "
                + portNumber + " or listening for a connection");
            System.out.println(e.getMessage());
        }
      }
  }
