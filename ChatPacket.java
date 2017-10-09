
import java.io.*;

public class ChatPacket implements Serializable{
  String packetType;
  String packetMessage;
  Integer gameID;
  public ChatPacket(String type, String message,Integer gameID){
    packetType=type;
    packetMessage=message;
    this.gameID = gameID;
  }
  public ChatPacket(String type, String message){
    packetType=type;
    packetMessage=message;
    gameID=0;
  }
  public ChatPacket(String type){
    packetType=type;
    packetMessage=null;
    gameID=0;
  }
  public String toString(){
    return packetType+": "+packetMessage;
  }
}
