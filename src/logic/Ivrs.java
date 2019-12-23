/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package logic;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import org.apache.log4j.Logger;
import sipmovilrtc.connection.SipmovilrtcConnection;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import sipmovilrtc.connection.FileOperations;

/**
 *
 * @author Usuario
 */
public class Ivrs {
    
    private static final Logger LOGGER = SipmovilrtcConnection.logger;
    
    public static Boolean createIvr(String slugName, String audioPath, String ivrName, 
            String waitTime, String options, String timezone, String ivrFile, 
            Boolean data_input, String context, String action, Boolean call_extension){
        System.out.println("FROM Ivrs.createIvr");  
        LOGGER.info("From Ivrs.createIvr");
        System.out.println(options);
        Gson gson = new Gson();   
        
        JsonArray jsonArray = gson.fromJson(options, JsonArray.class);
        JsonArray timezoneArray = gson.fromJson(timezone, JsonArray.class);
        // inclusion del archivo de la empresa en el indice de contextos
        try(FileWriter fw = new FileWriter(ivrFile, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {
            System.out.println("paso 1");
            out.println("["+slugName+"]");
            out.println("exten => s,1,NoOp(Reproduciendo IVR: "+ivrName+")");
            Integer arraySize = timezoneArray.size();
            if (arraySize > 0) {
                for (int i = 0; i < arraySize-1; i++) {
                    String element = timezoneArray.get(i).getAsString();
                    out.println("same => n,GotoifTime("+element+"?begin)");
                }
                out.println("same => n(end-timezone),"+timezoneArray.get(arraySize-1).getAsString());
            }
            System.out.println("paso 2");
            out.println("same => n(begin),Answer");
            if (data_input == true){
                out.println("same => n(loop),Read(EXTENSION,"+audioPath+",3,,,"+waitTime+")");
                out.println("same => n,Gotoif($[“${EXTENSION}”==””]?invalid)");
                out.println("same => n,Goto(${EXTENSION},1)");

                if (call_extension == true) {
                    out.println("exten => _XXX,1,Noop(Comunicando a extension ${EXTEN})");
                    out.println("same => n,Goto("+context+",${EXTEN},1)");
                }
                
                for (JsonElement pa : jsonArray) {
                    JsonObject element = pa.getAsJsonObject();
                    String key = element.get("key").getAsString();
                    String dial = element.get("dial").getAsString();
                    String overflow_type = element.get("overflow_type").getAsString();
                    if (key.contains("i")) {
                        out.println("exten => "+key+"(invalid),1,NoOp(Pressed "+key+")");
                    }else{
                        out.println("exten => "+key+",1,NoOp(Pressed "+key+")");
                    }
//                    out.println("exten => "+key+",1,NoOp(Pressed "+key+")");
                    out.println("same => n,"+dial);
                    if (overflow_type.equals("PLAY_AUDIO")){
                        out.println("same => n,Hangup");
                    }
                }
            }else{
                out.println("same => n(loop),Playback("+audioPath+")");
                out.println("same => n,"+action);
//                out.println("same => n,WaitExten("+waitTime+")");
            }
            System.out.println("paso 3");            
            
        } catch (IOException e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
            LOGGER.fatal(Arrays.toString(e.getStackTrace()));
            System.out.println("excepcion try");
            return false;
        }
        
        return true;
    }   
    
    
    public static boolean editIVR(String slugName, String audioPath, String ivrName, 
            String waitTime, String options, String timezone, String ivrFile,
            Boolean data_input, String context, String action, Boolean call_extension){
        System.out.println("from Ivrs.editIVR in file ");
        LOGGER.info("From Ivrs.editIVR");
        Gson gson = new Gson();
        JsonArray jsonArray = gson.fromJson(options, JsonArray.class);
        JsonArray timezoneArray = gson.fromJson(timezone, JsonArray.class);
        ArrayList<String> tempArray = new ArrayList<>();
        try (FileReader fr = new FileReader(ivrFile)) {
            Scanner reader = new Scanner(fr);
            String line;  
            while ( reader.hasNextLine() ) {
                line=reader.nextLine();
                if (line.contains("["+slugName+"]")) {
                    tempArray.add("["+slugName+"]");
                    tempArray.add("exten => s,1,NoOp(Reproduciendo IVR: "+ivrName+")");
                    Integer arraySize = timezoneArray.size();
                    if (arraySize > 0) {
                        for (int i = 0; i < arraySize-1; i++) {
                            String element = timezoneArray.get(i).getAsString();
                            tempArray.add("same => n,GotoifTime("+element+"?begin)");
                        }
                        tempArray.add("same => n(end-timezone),"+timezoneArray.get(arraySize-1).getAsString());
                    }
                    tempArray.add("same => n(begin),Answer");
                    if (data_input == true){
                        tempArray.add("same => n(loop),Read(EXTENSION,"+audioPath+",3,,,"+waitTime+")");
                        tempArray.add("same => n,Gotoif($[“${EXTENSION}”==””]?invalid)");
                        tempArray.add("same => n,Goto(${EXTENSION},1)");
                        tempArray.add("same => n(continue),Hangup");
                        if (call_extension == true) {
                            tempArray.add("exten => _XXX,1,Noop(Comunicando a extension ${EXTEN})");
                            tempArray.add("same => n,Goto("+context+",${EXTEN},1)");
                        }                        
                        for (JsonElement pa : jsonArray) {
                            JsonObject element = pa.getAsJsonObject();
                            String key = element.get("key").getAsString();
                            String dial = element.get("dial").getAsString();
                            String overflow_type = element.get("overflow_type").getAsString();
                            if (key.contains("i")) {
                                tempArray.add("exten => "+key+"(invalid),1,NoOp(Pressed "+key+")");
                            }else{
                                tempArray.add("exten => "+key+",1,NoOp(Pressed "+key+")");
                            }
//                            tempArray.add("exten => "+key+",1,NoOp(Pressed "+key+")");
                            tempArray.add("same => n,"+dial);
                            if (overflow_type.equals("PLAY_AUDIO")){
                                tempArray.add("same => n,Hangup");
                            }
                        }
                    }else{
                        tempArray.add("same => n(loop),Playback("+audioPath+")");
                        tempArray.add("same => n,"+action);
                    }
                    
                    boolean flg = false;
                    while ( reader.hasNextLine() && flg == false ) {
                        line=reader.nextLine();
                        if (line.length() == 0 || line.equals("") || line.startsWith("[")){
                            flg = true;
                        }
                    }
                }else{
                    tempArray.add(line);
                }
            }
            System.out.println("salio while");
            // cierra el archivo despues de leerlo
            fr.close();   
            
            boolean ret = FileOperations.WriteNewFile(tempArray, ivrFile);
            
        } catch (Exception e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
            LOGGER.fatal(Arrays.toString(e.getStackTrace()));
            System.out.println(e.getMessage());
            return false;
        }
        return true;
    }
    
    
    public static Boolean deleteIvr(String slugName, String ivrFile){
        System.out.println("FROM Ivrs.deleteIvr: "+slugName+" in "+ivrFile);  
        LOGGER.info("From Ivrs.deleteIvr: "+slugName+" in "+ivrFile);
         
        ArrayList<String> tempArray = new ArrayList<>();
        try (FileReader fr = new FileReader(ivrFile)) {
            Scanner reader = new Scanner(fr);
            String line;  
            while ( reader.hasNextLine() ) {
                line=reader.nextLine();
                if (line.contains("["+slugName+"]")) {
                    boolean flg = false;
                    while ( reader.hasNextLine() && flg == false ) {
                        line=reader.nextLine();
                        if (line.length() == 0 || line.equals("") || line.startsWith("[")){
                            flg = true;
                        }
                    }
                }else{
                    tempArray.add(line);
                }
            }
            System.out.println("salio while");
            // cierra el archivo despues de leerlo
            fr.close();   
            
            boolean ret = FileOperations.WriteNewFile(tempArray, ivrFile);
            
        } catch (Exception e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
            LOGGER.fatal(Arrays.toString(e.getStackTrace()));
            System.out.println(e.getMessage());
            return false;
        }
        return true;
    }
    
    
    public static boolean updateIvr(String ivrSlug, String timezone, String ivrFile){
        System.out.println("from Ivrs.updateIvr in ivr "+ivrSlug);
        LOGGER.info("From Ivrs.updateIvr in ivr "+ivrSlug);
        Gson gson = new Gson();
        JsonArray timezoneArray = gson.fromJson(timezone, JsonArray.class);
        ArrayList<String> tempArray = new ArrayList<>();
        try (FileReader fr = new FileReader(ivrFile)) {
            Scanner reader = new Scanner(fr);
            String line;  
            while ( reader.hasNextLine() ) {
                line=reader.nextLine();
                if (line.contains("["+ivrSlug+"]")) {
                    tempArray.add(line);
                    boolean flg = false;
                    while ( reader.hasNextLine() && flg == false ) {
                        line=reader.nextLine();
                        if(line.contains("GotoifTime")){
                            
                        }else{
                            if(line.contains("(end-timezone)")){
                                flg = true;
                            }else{
                                tempArray.add(line);
                            }                            
                        }
                        
                    }
                    for (JsonElement pa : timezoneArray) {
                        tempArray.add("same => n,GotoifTime("+pa.getAsString()+"?begin)");
                    }
                    tempArray.add("same => n,Hangup");
                }else{
                    tempArray.add(line);
                }
            }
            System.out.println("salio while");
            // cierra el archivo despues de leerlo
            fr.close();   
            
            boolean ret = FileOperations.WriteNewFile(tempArray, ivrFile);
            
        } catch (Exception e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
            LOGGER.fatal(Arrays.toString(e.getStackTrace()));
            System.out.println(e.getMessage());
            return false;
        }
        return true;
    }
}


