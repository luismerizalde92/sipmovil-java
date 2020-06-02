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
    
    
    //funcion para desactivar una cuenta
    public static boolean editExtension(String old_extension, String new_extension, String context, String account, String time_ring, String extensionFile){
        System.out.println("From Extensions.editExtension");
        LOGGER.info("From Extensions.editExtension");
        ArrayList<String> tempArray = new ArrayList<>();
        try (FileReader fr = new FileReader(extensionFile)) {
            Scanner reader = new Scanner(fr);
            String line;  
            while ( reader.hasNextLine() ) {
                line=reader.nextLine();
                if (line.contains("exten => "+old_extension)) {
                    System.out.println("Entro old_extension");
                    tempArray.add("exten => "+new_extension+",1,NoOp(Llamada entrante ${CDR(src)} a extension ${EXTEN} por canal ${CHANNEL})");
                    tempArray.add("same => n,Set(OVERFLOW="+time_ring+")");
                    tempArray.add("same => n,Gosub(sipmovil-extension,s,1(${EXTEN},${OVERFLOW},${CDR(dcontext)},${CONTEXT}))\n");
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
            out.println("exten => *111,1,VoiceMailMain(${CDR(src)}@"+contextName+")");
            out.println("exten => _XXX.,1,Progress()");
            out.println("same => n,Gosub(outgoing-call,s,1(${EXTEN},${CONTEXT},${CDR(src)},"+sipmovil_trunk+"))");
//            out.println("exten => _57X.,1,Progress()");
//            out.println("exten => _57X.,2,DIAL(PJSIP/${EXTEN}@"+sipmovil_trunk+")");

            
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
    
    public static Boolean createExtension(String contextName, String extension, String timeRing, String account, String exten_file){
        System.out.println("from Extensions.createExtension in file "+exten_file);
        LOGGER.info("From Extensions.createExtension");
        try(FileWriter fw = new FileWriter(exten_file, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {
            out.println("\n");
            out.println("exten => "+extension+",1,NoOp(Llamada entrante ${CDR(src)} a extension ${EXTEN} por canal ${CHANNEL})");
            out.println("same => n,Set(OVERFLOW="+timeRing+")");
            out.println("same => n,Gosub(sipmovil-extension,s,1(${EXTEN},${OVERFLOW},${CDR(dcontext)},${CONTEXT}))");
        } catch (IOException e) {
            System.out.println("excepcion try");
            return false;
        }
        
        return true;
    }
    
    public static boolean createGroup(String extension, String groupName, String slugName, 
            String overflowTime, String retryTime, String ringTime, String accounts, 
            String strategy, String exten_file){
        System.out.println("from Extensions.createGroup in file "+exten_file);
        LOGGER.info("From Extensions.createGroup");
        try(FileWriter fw = new FileWriter(exten_file, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {
            out.println("exten => "+extension+",1,NoOp(Llamada a grupo: "+groupName+")");
            out.println("same => n,Set(SLUG="+slugName+")");
            out.println("same => n,Set(OVERFLOW="+overflowTime+")");
            out.println("same => n,Set(STRATEGY="+strategy+")");
            out.println("same => n,Set(WAIT="+retryTime+")");
            out.println("same => n,Set(RINGTIME="+ringTime+")");
            out.println("same => n,Set(ACCOUNTS="+"\""+accounts+"\""+")");
            out.println("same => n,Gosub(sipmovil-ringgroup,s,1(${SLUG},${OVERFLOW},"+
                            "${STRATEGY},${WAIT},${RINGTIME},${ACCOUNTS}))\n");
        } catch (IOException e) {
            System.out.println("excepcion try");
            return false;
        }
        return true;
    }
    
    public static boolean editGroupExtension(String old_extension, 
            String new_extension, String slugName, String groupName, 
            String overflowTime, String extensionFile, String retryTime,
            String timeout, String strategy, String accounts){
        System.out.println("From Extensions.editGroupExtension");
        LOGGER.info("From Extensions.editGroupExtension");
        ArrayList<String> tempArray = new ArrayList<>();
        try (FileReader fr = new FileReader(extensionFile)) {
            Scanner reader = new Scanner(fr);
            String line;  
            while ( reader.hasNextLine() ) {
                line=reader.nextLine();
                if (line.contains("exten => "+old_extension)) {
                    tempArray.add("exten => "+new_extension+",1,NoOp(Llamada a grupo: "+groupName+")");
                    tempArray.add("same => n,Set(SLUG="+slugName+")");
                    tempArray.add("same => n,Set(OVERFLOW="+overflowTime+")");
                    tempArray.add("same => n,Set(STRATEGY="+strategy+")");
                    tempArray.add("same => n,Set(WAIT="+retryTime+")");
                    tempArray.add("same => n,Set(RINGTIME="+timeout+")");
                    tempArray.add("same => n,Set(ACCOUNTS="+"\""+accounts+"\""+")");
                    tempArray.add("same => n,Gosub(sipmovil-ringgroup,s,1(${SLUG},${OVERFLOW},"+
                            "${STRATEGY},${WAIT},${RINGTIME},${ACCOUNTS}))\n");
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
    
    public static boolean createIVR(String extension, String ivrSlug, String exten_file){
        System.out.println("from Extensions.createIVR in file "+exten_file);
        LOGGER.info("From Extensions.createIVR");
        try(FileWriter fw = new FileWriter(exten_file, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {
            out.println("exten => "+extension+",1,Goto("+ivrSlug+",s,1)");
        } catch (IOException e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
            LOGGER.fatal(Arrays.toString(e.getStackTrace()));
            System.out.println("excepcion try");
            return false;
        }
        return true;
    }
    
    public static boolean editIVR(String extension, String oldExtension, String ivrSlug, String exten_file){
        System.out.println("from Extensions.editIVR in file "+exten_file);
        LOGGER.info("From Extensions.editIVR");
        ArrayList<String> tempArray = new ArrayList<>();
        try (FileReader fr = new FileReader(exten_file)) {
            Scanner reader = new Scanner(fr);
            String line;  
            while ( reader.hasNextLine() ) {
                line=reader.nextLine();
                if (line.contains("exten => "+oldExtension)) {
                    tempArray.add("exten => "+extension+",1,Goto("+ivrSlug+",s,1)");
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
            
            boolean ret = FileOperations.WriteNewFile(tempArray, exten_file);
            
        } catch (Exception e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
            LOGGER.fatal(Arrays.toString(e.getStackTrace()));
            System.out.println(e.getMessage());
            return false;
        }
        return true;
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
