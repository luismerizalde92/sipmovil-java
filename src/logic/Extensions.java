/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package logic;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import sipmovilrtc.connection.FileOperations;
import sipmovilrtc.connection.SipmovilrtcConnection;
import org.apache.log4j.Logger;
/**
 *
 * @author Usuario
 */



public class Extensions {
    
    private static final String EXTENSIONS_INDEX = SipmovilrtcConnection.EXTENSIONS_INDEX;
    private static final Logger LOGGER = SipmovilrtcConnection.logger;
    
    
     
    
    public static boolean deleteGroupExtension(String group_extension, String extensionFile){
        System.out.println("From Extensions.deleteGroupExtension: "+group_extension);
        LOGGER.info("From Extensions.deleteGroupExtension: "+group_extension);
        ArrayList<String> tempArray = new ArrayList<>();
        try (FileReader fr = new FileReader(extensionFile)) {
            Scanner reader = new Scanner(fr);
            String line;  
            while ( reader.hasNextLine() ) {
                line=reader.nextLine();
                if (line.contains("exten => "+group_extension)) {
                    boolean flg = false;
                    while ( reader.hasNextLine() && flg == false ) {
                        line=reader.nextLine();
                        if (line.length() == 0 || line.equals("") || line.startsWith("[") || line.startsWith("exten")){
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
            
            boolean ret = FileOperations.WriteNewFile(tempArray, extensionFile);
            
        } catch (Exception e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
            LOGGER.fatal(Arrays.toString(e.getStackTrace()));
            System.out.println(e.getMessage());
            return false;
        }
        return true;
    }
    
    public static boolean updateTrunk(String sipmovil_trunk, String extensionFile){
        System.out.println("From Extensions.updateTrunk");
        LOGGER.info("From Extensions.updateTrunk");
        ArrayList<String> tempArray = new ArrayList<>();
        try (FileReader fr = new FileReader(extensionFile)) {
            Scanner reader = new Scanner(fr);
            String line;  
            while ( reader.hasNextLine() ) {
                line=reader.nextLine();
                if (line.contains("Gosub(outgoing-call")) {
                    tempArray.add("same => n,Gosub(outgoing-call,s,1(${EXTEN},${CONTEXT},${CDR(src)},"+sipmovil_trunk+"))");
//                    tempArray.add("exten => _57X.,2,DIAL(PJSIP/${EXTEN}@"+sipmovil_trunk+")");
                    boolean flg = false;
                    while ( reader.hasNextLine() && flg == false ) {
                        line=reader.nextLine();
                        if (line.length() == 0 || line.equals("") || line.startsWith("[") || line.startsWith("exten")){
                            tempArray.add(line);
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
            
            boolean ret = FileOperations.WriteNewFile(tempArray, extensionFile);
            
        } catch (Exception e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
            LOGGER.fatal(Arrays.toString(e.getStackTrace()));
            System.out.println(e.getMessage());
            return false;
        }
        return true;
    }
    
    public static Boolean addContext(String contextName, String contextFile, String sipmovil_trunk, String ivrFile){
        System.out.println("FROM Extensions.addContext");
        LOGGER.info("From Extensions.addContext");
        System.out.println(contextName);        
        // inclusion del archivo de la empresa en el indice de contextos
        try(FileWriter fw = new FileWriter(EXTENSIONS_INDEX, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {
            out.println("\n");
            out.println("#include "+contextFile);
            out.println("#include "+ivrFile);
            
        } catch (IOException e) {
            System.out.println("excepcion try");
            return false;
        }
        
        // creacion contexto de la empresa en el archivo particular
        try(FileWriter fw = new FileWriter(contextFile, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {
            out.println("["+contextName+"]"); 
            out.println("\n");
            out.println("exten => 6002,1,Verbose()");
            out.println("same => n,Stasis(change-app,${CONTEXT},${EXTEN})");
            out.println("same => n,Hangup()");
            out.println("\n");
            out.println("exten => _XXX,1,Verbose(Enter to sipmovil bridge stasis inbound)");
            out.println("same => n,Verbose(call from ${CDR(src)} to extension ${EXTEN})");
            out.println("same => n,Stasis(sipmovil-router,${CONTEXT},${CDR(src)},${EXTEN})");
            out.println("same => n(ivr),Verbose(Enter to extension type IVR, the value of SIPMOVIL_DST: ${SIPMOVIL_DST})");
            out.println("same => n,Goto(${SIPMOVIL_DST},s,1)");
            out.println("same => n,Hangup()");
            out.println("\n");
            out.println("exten => _XXX.,1,Verbose(Enter to sipmovil bridge stasis outgoing)");
            out.println("same => n,Verbose(call from ${CDR(src)} to extension ${EXTEN})");
            out.println("same => n,Stasis(sipmovil-bridge,outgoing,${CONTEXT},${EXTEN},${CDR(src)},no_call_id,no)");
            out.println("same => n,Hangup()");

            
        } catch (IOException e) {
            System.out.println("excepcion try");
            return false;
        }
        return true;
        
//        boolean res = reloadDialplan();
//        if (res == true){
//            System.out.println("se ejecito correctamente el dialplan");
//        }else{
//            System.out.println("Error al ejecutar el dialplan");
//        }
        
    }     
    
    
    public static boolean createOrUpdateExtension(String old_extension, 
            String new_extension, String time_ring, String extensionFile) throws IOException{
        
        System.out.println("From Extensions.createOrUpdateRinggroup");
        LOGGER.info("From Extensions.createOrUpdateRinggroup");
        
        ArrayList<String> extensionArray = new ArrayList<>();
        extensionArray.add("exten => "+new_extension+",1,NoOp(Llamada entrante ${CDR(src)} a extension ${EXTEN} por canal ${CHANNEL})");
        extensionArray.add("same => n,Set(OVERFLOW="+time_ring+")");
        extensionArray.add("same => n,Gosub(sipmovil-extension,s,1(${EXTEN},${OVERFLOW},${CDR(dcontext)},${CONTEXT}))\n");
        
        boolean ret = createOrUpdateExtension(extensionArray, old_extension,
                extensionFile);
        return ret;
    }
    
  
    public static boolean createOrUpdateRinggroup(String old_extension, 
            String new_extension, String slugName, String groupName, 
            String overflowTime, String extensionFile, String retryTime,
            String timeout, String strategy, String accounts) throws IOException{
        System.out.println("From Extensions.createOrUpdateRinggroup");
        LOGGER.info("From Extensions.createOrUpdateRinggroup");
        
        ArrayList<String> extensionArray = new ArrayList<>();
        extensionArray.add("exten => "+new_extension+",1,NoOp(Llamada a grupo: "+groupName+")");
        extensionArray.add("same => n,Set(SLUG="+slugName+")");
        extensionArray.add("same => n,Set(OVERFLOW="+overflowTime+")");
        extensionArray.add("same => n,Set(STRATEGY="+strategy+")");
        extensionArray.add("same => n,Set(WAIT="+retryTime+")");
        extensionArray.add("same => n,Set(RINGTIME="+timeout+")");
        extensionArray.add("same => n,Set(ACCOUNTS="+"\""+accounts+"\""+")");
        extensionArray.add("same => n,Gosub(sipmovil-ringgroup,s,1(${SLUG},${OVERFLOW},"+
                            "${STRATEGY},${WAIT},${RINGTIME},${ACCOUNTS}))\n");
        
        boolean ret = createOrUpdateExtension(extensionArray, old_extension,
                extensionFile);
        return ret;

    } 
    
    public static boolean createOrUpdateIVR(String extension, String oldExtension, 
            String ivrSlug, String extensionFile) throws IOException{
        System.out.println("from Extensions.createOrUpdateIVR in file "+extensionFile);
        LOGGER.info("From Extensions.createOrUpdateIVR");
        
        ArrayList<String> extensionArray = new ArrayList<>();
        extensionArray.add("exten => "+extension+",1,Goto("+ivrSlug+",s,1)\n");
        
        boolean ret = createOrUpdateExtension(extensionArray, oldExtension,
                extensionFile);
        return ret;
    }
    
    
    public static boolean createOrUpdateExtension(ArrayList<String> extensionArray, 
            String old_extension, String extensionFile) throws IOException{
        System.out.println("From Extensions.createOrUpdateExtension");
        LOGGER.info("From Extensions.createOrUpdateExtension");
        
        ArrayList<String> tempArray = new ArrayList<>();
        try (FileReader fr = new FileReader(extensionFile)) {
            Scanner reader = new Scanner(fr);
            String line;  
            boolean extensionExists = false;
            while ( reader.hasNextLine() ) {
                line=reader.nextLine();
                if (line.startsWith("exten => "+old_extension)) {
                    extensionExists = true;
                    tempArray.addAll(extensionArray);
                    boolean flg = false;
                    while ( reader.hasNextLine() && flg == false ) {
                        line=reader.nextLine();
                        if (line.length() == 0 || line.equals("") || line.startsWith("[") || line.startsWith("exten")){
                            tempArray.add(line);
                            flg = true;
                        }
                    }
                }else{
                    tempArray.add(line);
                }
            }
            System.out.println("salio while");
            if (extensionExists == false){
//                tempArray.add("\n");
                tempArray.addAll(extensionArray);
            }
            // cierra el archivo despues de leerlo
            fr.close();   
            
            boolean ret = FileOperations.WriteNewFile(tempArray, extensionFile);
            return ret;
        }
    }
    
    public static Boolean reloadDialplan(){
        System.out.println("Entro funcion reloadDialplan");
        LOGGER.info("From Extensions.reloadDialplan");
        try {
            Runtime.getRuntime().exec("asterisk -rx 'dialplan reload'");
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    
    
    
    public static boolean deleteDIDs(String dids, String extensionFile){
        System.out.println("From Extensions.deleteDIDs");
        LOGGER.info("From Extensions.deleteDIDs");
        ArrayList<String> tempArray = new ArrayList<>();
        Gson gson = new Gson();
        JsonArray didsArray = gson.fromJson(dids, JsonArray.class);
        try (FileReader fr = new FileReader(extensionFile)) {
            Scanner reader = new Scanner(fr);
            String line;  
            while ( reader.hasNextLine() ) {
                line=reader.nextLine();
                boolean resp = testArrayElement(line, didsArray);
                if(resp == false){
                    tempArray.add(line);
                }                 
            }
            System.out.println("salio while");
            // cierra el archivo despues de leerlo
            fr.close();   
            
            boolean ret = FileOperations.WriteNewFile(tempArray, extensionFile);
            
        } catch (Exception e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
            LOGGER.fatal(Arrays.toString(e.getStackTrace()));
            System.out.println(e.getMessage());
            return false;
        }
        return true;
    }
    
    public static Boolean addDIDs(String dids, String extension, String context,
            String extensionsFile){
        System.out.println("FROM Extensions.addDIDs");  
        LOGGER.info("From Extensions.addDIDs");
        Gson gson = new Gson();
        JsonArray didsArray = gson.fromJson(dids, JsonArray.class);
        // inclusion del archivo de la empresa en el indice de contextos
        try(FileWriter fw = new FileWriter(extensionsFile, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {
            for (JsonElement pa : didsArray) {
                String did = pa.getAsString();
                out.println("exten => "+did+",1,Goto("+context+","+extension+",1)");
            }
            
        } catch (IOException e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
            LOGGER.fatal(Arrays.toString(e.getStackTrace()));
            System.out.println("excepcion try");
            return false;
        }
        
        return true;
    }
    
    public static boolean didOperation(String context, String did, String extension,
                        String operation, String extensionFile){
        System.out.println("From Extensions.didOperation");
        LOGGER.info("From Extensions.didOperation");
        ArrayList<String> tempArray = new ArrayList<>();
        try (FileReader fr = new FileReader(extensionFile)) {
            Scanner reader = new Scanner(fr);
            String line;  
            while ( reader.hasNextLine() ) {
                line=reader.nextLine();
                if (line.contains(did)) {
                    if (operation.equals("update")){
                        tempArray.add("exten => "+did+",1,Goto("+context+","+extension+",1)");
                    }
                }else{
                    tempArray.add(line);
                }
            }
            if (operation.equals("add")){
                tempArray.add("exten => "+did+",1,Goto("+context+","+extension+",1)");
            }
            System.out.println("salio while");
            // cierra el archivo despues de leerlo
            fr.close();   
            
            boolean ret = FileOperations.WriteNewFile(tempArray, extensionFile);
            
        } catch (Exception e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
            LOGGER.fatal(Arrays.toString(e.getStackTrace()));
            System.out.println(e.getMessage());
            return false;
        }
        return true;
    }  
    
    public static Boolean newDIDs(JsonArray didsArray, String extensionsFile){
        System.out.println("FROM Groups.addDIDs with jsonArray");  
        LOGGER.info("From Groups.addDIDs jsonArray");

        // inclusion del archivo de la empresa en el indice de contextos
        try(FileWriter fw = new FileWriter(extensionsFile, false);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {
            out.println("[from-sipmovil]");
            for (JsonElement pa : didsArray) {
                JsonObject element = pa.getAsJsonObject();
                String did = element.get("did").getAsString();
                String context = element.get("context").getAsString();
                String extension = element.get("extension").getAsString();
                out.println("exten => "+did+",1,Goto("+context+","+extension+",1)");
            }
            
        } catch (IOException e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
            LOGGER.fatal(Arrays.toString(e.getStackTrace()));
            System.out.println("excepcion try");
            return false;
        }
        
        return true;
    }
    
    public static boolean updateVoicemail(String context, String extension,
            String extensionFile){
        System.out.println("From Extensions.updateVoicemail");
        LOGGER.info("From Extensions.updateVoicemail");
        ArrayList<String> tempArray = new ArrayList<>();
        try (FileReader fr = new FileReader(extensionFile)) {
            Scanner reader = new Scanner(fr);
            String line;  
            while ( reader.hasNextLine() ) {
                line=reader.nextLine();
                if (line.contains("1,VoiceMailMain")) {
                    tempArray.add("exten => "+extension+",1,VoiceMailMain(${CDR(src)}@"+context+")");
                }else{
                    tempArray.add(line);
                }
            }
            System.out.println("salio while");
            // cierra el archivo despues de leerlo
            fr.close();   
            
            boolean ret = FileOperations.WriteNewFile(tempArray, extensionFile);
            
        } catch (Exception e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
            LOGGER.fatal(Arrays.toString(e.getStackTrace()));
            System.out.println(e.getMessage());
            return false;
        }
        return true;
    }
    
    private static Boolean testArrayElement(String line, JsonArray didsArray){
        for (JsonElement pa : didsArray) {
            String did = pa.getAsString();
            if (line.contains(did)) {
                return true;
            }
        }        
        return false;
    }
}
