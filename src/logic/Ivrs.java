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
    
    public static boolean createOrUpdateIVR(String slugName, String audioPath, String ivrName, 
            String waitTime, String options, String timezone, String ivrFile,
            Boolean data_input, String context, String action, Boolean call_extension,
            String validOptions) throws IOException{
        
        System.out.println("from Ivrs.createOrUpdateIVR in file ");
        LOGGER.info("From Ivrs.createOrUpdateIVR");
        Gson gson = new Gson();
        JsonArray jsonArray = gson.fromJson(options, JsonArray.class);
        JsonArray timezoneArray = gson.fromJson(timezone, JsonArray.class);
        ArrayList<String> extensionArray = new ArrayList<>();

        extensionArray.add("["+slugName+"]");
        extensionArray.add("exten => s,1,NoOp(Reproduciendo IVR: "+ivrName+")");
        Integer arraySize = timezoneArray.size();
        if (arraySize > 0) {
            for (int i = 0; i < arraySize-1; i++) {
                String element = timezoneArray.get(i).getAsString();
                extensionArray.add("same => n,GotoifTime("+element+"?begin)");
            }
            extensionArray.add(timezoneArray.get(arraySize-1).getAsString());
        }
        extensionArray.add("same => n(begin),Answer");
        if (data_input == true){
            extensionArray.add("same => n(loop),Read(EXTENSION,"+audioPath+",3,,,"+waitTime+")");
            extensionArray.add("same => n,Gotoif($[\"${EXTENSION}\"==\"\"]?timeout,1:check-extension)");
            extensionArray.add("same => n,Verbose(the extension length ${LEN(${EXTENSION})})");
            extensionArray.add("same => n(check-extension),Gotoif($[${LEN(${EXTENSION})}==3]?internal-extension:check-menu)");
            extensionArray.add("same => n(check-menu),Gotoif($[${LEN(${EXTENSION})}==1]?check-option:h,1)");
            extensionArray.add("same => n(check-option),SET(IS_VALID=${REGEX(\""+validOptions+"\" ${EXTENSION})})");
            extensionArray.add("same => n,Gotoif($[${IS_VALID}==1]?internal-extension:invalid,1)");
            extensionArray.add("same => n(internal-extension),Goto(${EXTENSION},1)");
            extensionArray.add("same => n,Goto(${EXTENSION},1)");
            extensionArray.add("same => n(continue),Hangup");
            if (call_extension == true) {
                extensionArray.add("exten => _XXX,1,Noop(Comunicando a extension ${EXTEN})");
                extensionArray.add("same => n,Goto("+context+",${EXTEN},1)");
            }                        
            for (JsonElement pa : jsonArray) {
                JsonObject element = pa.getAsJsonObject();
                String key = element.get("key").getAsString();
                String dial = element.get("dial").getAsString();
                String overflow_type = element.get("overflow_type").getAsString();
                if (key.equals("i")) {
                    extensionArray.add("exten => invalid,1,NoOp(An invalid optionwas choosen)");
                }else if(key.equals("t")){
                    extensionArray.add("exten => timeout,1,NoOp(The time to input option finished)");     
                }else{
                    extensionArray.add("exten => "+key+",1,NoOp(Pressed "+key+")");
                }
//                            tempArray.add("exten => "+key+",1,NoOp(Pressed "+key+")");
                extensionArray.add(dial);
                if (overflow_type.equals("PLAY_AUDIO")){
                    extensionArray.add("same => n,Hangup");
                }
            }
        }else{
            extensionArray.add("same => n(loop),Playback("+audioPath+")");
            extensionArray.add(action);
        }
        
        extensionArray.add("exten => h,1,NoOp(Pressed hangup)");
        extensionArray.add("same => n,Stasis(sipmovil-bridge,end_ivr_call,${SIPMOVIL_CALL_ID},${SIPMOVIL_CALL_CONTEXT})");

        boolean ret = createOrUpdateExtension(extensionArray, slugName,
                ivrFile);
        return ret;

    }
    
    
    
    public static boolean createOrUpdateExtension(ArrayList<String> extensionArray, 
            String slugName, String ivrsFile) throws IOException{
        System.out.println("From Extensions.createOrUpdateExtension");
        LOGGER.info("From Extensions.createOrUpdateExtension");
        
        ArrayList<String> tempArray = new ArrayList<>();
        try (FileReader fr = new FileReader(ivrsFile)) {
            Scanner reader = new Scanner(fr);
            String line;  
            boolean ivrExists = false;
            while ( reader.hasNextLine() ) {
                line=reader.nextLine();
                if (line.startsWith("["+slugName+"]")) {
                    ivrExists = true;
                    tempArray.addAll(extensionArray);
                    boolean flg = false;
                    while ( reader.hasNextLine() && flg == false ) {
                        line=reader.nextLine();
                        if (line.length() == 0 || line.equals("") || line.startsWith("[")){
                            tempArray.add(line);
                            flg = true;
                        }
                    }
                }else{
                    tempArray.add(line);
                }
            }
            System.out.println("salio while");
            if (ivrExists == false){
                tempArray.add("\n");
                tempArray.addAll(extensionArray);
            }
            // cierra el archivo despues de leerlo
            fr.close();   
            
            boolean ret = FileOperations.WriteNewFile(tempArray, ivrsFile);
            return ret;
        }
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


