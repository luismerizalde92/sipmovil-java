/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package logic;

import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import org.apache.log4j.Logger;
//import static logic.Extensions.reloadDialplan;
import sipmovilrtc.connection.SipmovilrtcConnection;
//import static sipmovilrtc.connection.SipmovilrtcConnection.VOICEMAIL_FILE;
import sipmovilrtc.connection.FileOperations; 

/**
 *
 * @author Usuario
 */
public class VoiceMail {
    private static final String VOICEMAIL_INDEX = SipmovilrtcConnection.VOICEMAIL_INDEX;
    private static final Logger LOGGER = SipmovilrtcConnection.logger;
    
    public static Boolean addContext(String contextName, String contextFile){
        System.out.println("FROM VoiceMail.addContext");
        LOGGER.info("From VoiceMail.addContext");
        
        // inclusion del archivo de la empresa en el indice de contextos
        try(FileWriter fw = new FileWriter(VOICEMAIL_INDEX, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {
            out.println("\n");
            out.println("#include "+contextFile);
            
        } catch (IOException e) {
            System.out.println("excepcion try");
            return false;
        }
        
        // creacion contexto de la empresa en el archivo particular
        try(FileWriter fw = new FileWriter(contextFile, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {
            //out.println("\n");
            out.println("["+contextName+"]\n");            
        } catch (IOException e) {
            System.out.println("excepcion try");
            return false;
        }
        return true;
//        try(FileWriter fw = new FileWriter(VOICEMAIL_FILE, true);
//            BufferedWriter bw = new BufferedWriter(fw);
//            PrintWriter out = new PrintWriter(bw))
//        {
//            out.println("\n");
//            out.println("["+contextName+"]");
//            
//        } catch (IOException e) {
//            System.out.println("excepcion try");
//            return false;
//        }
    }
    
    public static Boolean addVoiceMail(String contextName, String user, String pin, String name, String mail, String voicemailFile){
        System.out.println("from VoiceMail.addVoiceMail in file "+voicemailFile);
        LOGGER.info("From VoiceMail.addVoiceMail");
        ArrayList<String> tempArray = new ArrayList<>();
        try (FileReader fr = new FileReader(voicemailFile)) {
            Scanner reader = new Scanner(fr);
            String line;  
            while ( reader.hasNextLine() ) {
                line=reader.nextLine();
                if (line.contains("["+contextName+"]")) {
                    tempArray.add(line);
                    String userLine = user + " => " + pin + ","+ name + "," +mail;  
                    tempArray.add(userLine);
                }else{
                    tempArray.add(line);
                }
            }
            System.out.println("salio while");
            // cierra el archivo despues de leerlo
            fr.close();   
            
            boolean ret = FileOperations.WriteNewFile(tempArray, voicemailFile);
            
        } catch (Exception e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
            LOGGER.fatal(Arrays.toString(e.getStackTrace()));
            System.out.println(e.getMessage());
            return false;
        }
        return true;
    }
    
    public static Boolean changeVoiceMail(String account, String pin, String name, String mail, String voicemailFile){
        System.out.println("from VoiceMail.addVoiceMail in file "+voicemailFile);
        LOGGER.info("From VoiceMail.changeVoiceMail");
        ArrayList<String> tempArray = new ArrayList<>();
        try (FileReader fr = new FileReader(voicemailFile)) {
            Scanner reader = new Scanner(fr);
            String line;  
            while ( reader.hasNextLine() ) {
                line=reader.nextLine();
                if (line.contains(account)) {
                    String userLine = account + " => " + pin + ","+ name + "," +mail;  
                    tempArray.add(userLine);
                }else{
                    tempArray.add(line);
                }
            }
            System.out.println("salio while");
            // cierra el archivo despues de leerlo
            fr.close();   
            
            boolean ret = FileOperations.WriteNewFile(tempArray, voicemailFile);
            
        } catch (Exception e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
            LOGGER.fatal(Arrays.toString(e.getStackTrace()));
            System.out.println(e.getMessage());
            return false;
        }
        return true;
    }
    
    public static boolean deleteVoicemail(String account, String voicemailFile){
        System.out.println("From VoiceMail.deleteVoicemail");
        LOGGER.info("From VoiceMail.deleteVoicemail");
        ArrayList<String> tempArray = new ArrayList<>();
        try (FileReader fr = new FileReader(voicemailFile)) {
            Scanner reader = new Scanner(fr);
            String line;  
            while ( reader.hasNextLine() ) {
                line=reader.nextLine();
                if (!line.contains(account)) {
                    tempArray.add(line);
                }
            }
            System.out.println("salio while");
            // cierra el archivo despues de leerlo
            fr.close();   
            
            boolean ret = FileOperations.WriteNewFile(tempArray, voicemailFile);
            
        } catch (Exception e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
            LOGGER.fatal(Arrays.toString(e.getStackTrace()));
            System.out.println(e.getMessage());
            return false;
        }
        return true;
    }
}
