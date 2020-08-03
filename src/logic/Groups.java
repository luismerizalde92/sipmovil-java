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
import sipmovilrtc.connection.FileOperations;
import sipmovilrtc.connection.SipmovilrtcConnection;

/**
 *
 * @author Usuario
 */
public class Groups {
    
    private static final String QUEUES_INDEX = SipmovilrtcConnection.QUEUES_INDEX;
    private static final Logger LOGGER = SipmovilrtcConnection.logger;
    
    public static Boolean addContext(String contextFile){
        System.out.println("FROM Groups.addContext");  
        LOGGER.info("From Groups.addContext");
        // inclusion del archivo de la empresa en el indice de contextos
        try(FileWriter fw = new FileWriter(QUEUES_INDEX, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {
            out.println("\n");
            out.println("#include "+contextFile);
            
        } catch (IOException e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
            LOGGER.fatal(Arrays.toString(e.getStackTrace()));
            System.out.println("excepcion try");
            return false;
        }
        
        return true;
    }
    
    public static Boolean addGroup(String slugName, String retryTime, String timeout, String strategy, String queueFile){
        System.out.println("FROM Groups.addGroup");  
        LOGGER.info("From Groups.addGroup");
        // inclusion del archivo de la empresa en el indice de contextos
        try(FileWriter fw = new FileWriter(queueFile, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {
            out.println("["+slugName+"]");
            out.println("timeout = "+timeout);
            out.println("retry = "+retryTime);
            out.println("strategy = "+strategy+"\n");

            
        } catch (IOException e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
            LOGGER.fatal(Arrays.toString(e.getStackTrace()));
            System.out.println("excepcion try");
            return false;
        }
        
        return true;
    }
    
    public static boolean editGroup(String slugName, String retryTime, String timeout, String strategy, String queueFile){
        System.out.println("From Groups.editGroup");
        LOGGER.info("From Groups.editGroup");
        ArrayList<String> tempArray = new ArrayList<>();
        try (FileReader fr = new FileReader(queueFile)) {
            Scanner reader = new Scanner(fr);
            String line;  
            while ( reader.hasNextLine() ) {
                line=reader.nextLine();
                if (line.contains(slugName)) {
                    tempArray.add("["+slugName+"]");
                    tempArray.add("timeout = "+timeout);
                    tempArray.add("retry = "+retryTime);
                    tempArray.add("strategy = "+strategy+"\n");
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
            // cierra el archivo despues de leerlo
            fr.close();   
            
            boolean ret = FileOperations.WriteNewFile(tempArray, queueFile);
            
        } catch (Exception e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
            LOGGER.fatal(Arrays.toString(e.getStackTrace()));
            System.out.println(e.getMessage());
            return false;
        }
        return true;
    }  
    
    public static boolean deleteGroup(String slugName, String queueFile){
        System.out.println("From Groups.deleteGroup");
        LOGGER.info("From Groups.deleteGroup");
        ArrayList<String> tempArray = new ArrayList<>();
        try (FileReader fr = new FileReader(queueFile)) {
            Scanner reader = new Scanner(fr);
            String line;  
            while ( reader.hasNextLine() ) {
                line=reader.nextLine();
                if (line.contains(slugName)) {
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
            
            boolean ret = FileOperations.WriteNewFile(tempArray, queueFile);
            
        } catch (Exception e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
            LOGGER.fatal(Arrays.toString(e.getStackTrace()));
            System.out.println(e.getMessage());
            return false;
        }
        return true;
    }  
    
}
