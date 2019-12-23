/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package logic;

import dsk2.json.JSONObject;
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
 * @author Luis Merizalde
 */
public class Accounts {
    
    private static final String PJSIP_INDEX = SipmovilrtcConnection.PJSIP_INDEX;
    private static final Logger LOGGER = SipmovilrtcConnection.logger;
    
    public static Boolean addContext(String contextFile){
        System.out.println("FROM Accounts.addContext");
        LOGGER.info("From Accounts.addContext");   
        // inclusion del archivo de la empresa en el indice de contextos
        try(FileWriter fw = new FileWriter(PJSIP_INDEX, true);
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
    
    // funcion para traer la lista de cuentas
    public static JSONObject getCurrentAccounts(String path){
        System.out.println("FROM Accounts.getCurrentAccounts");
        LOGGER.info("From Accounts.getCurrentAccounts");
        JSONObject json = new JSONObject();
        ArrayList<JSONObject> accountArray = new ArrayList<>();
        try {                        
            // array temporal para mantener los datos del archivo leido
            try {
                //File file = new File("D:\\Usuario Luis Alfredo\\Desktop\\SSC\\test_sipmovil\\users.txt");
                try (FileReader fr = new FileReader(path)) {
                    Scanner reader = new Scanner(fr);
                    String line;
                    while ( reader.hasNextLine() ) {                                
                        line=reader.nextLine();
                        Integer pos_open = line.indexOf("["); 
                        Integer pos_close = line.indexOf("]");
                        if ( pos_open == 0 && pos_close != -1 ) {
                            boolean flag = false;
                            JSONObject account_params = new JSONObject();
                            account_params.put("account",line.substring(pos_open+1, pos_close));
                            while (flag == false && reader.hasNextLine()) {
                                line=reader.nextLine();
                                if (line.length() == 0 || !line.equals("")){
                                    flag = true;
                                }else{  
                                    String[] lineArr = line.split("=");
                                    account_params.put(lineArr[0], lineArr[1]);
                                }
                            }
                            accountArray.add(account_params);
                        }
                    }
                    fr.close();
                    json.put("array", accountArray);
                    json.put("response", true);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        } catch (Exception e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
            LOGGER.fatal(Arrays.toString(e.getStackTrace()));
            System.out.println("entro Exception ConexionServer");
            try {
                json.put("response", false); //return false;
            } catch (Exception ex) {
            }
        }
        return json;
    }
    
    //funcion para desactivar una cuenta
    public static boolean disableAccount(String account, String context, String accountFile){
        System.out.println("From Accounts.disableAccount");
        LOGGER.info("From Accounts.disableAccount");
        ArrayList<String> tempArray = new ArrayList<>();
        try (FileReader fr = new FileReader(accountFile)) {
            Scanner reader = new Scanner(fr);
            String line;  
            while ( reader.hasNextLine() ) {
                line=reader.nextLine();
                if (line.contains("["+account+"]")) {
                    System.out.println("Entro account");
                    tempArray.add(line);
                    boolean flg = false;
                    while ( reader.hasNextLine() && flg == false ) {
                        line=reader.nextLine();
                        if (line.contains("context=")) {
                            System.out.println("context=");
                            tempArray.add("context="+context);
                            flg = true;
                        }else{
                            tempArray.add(line);
                        }
                    }
                }else{
                    tempArray.add(line);
                }
            }
            System.out.println("salio while");
            // cierra el archivo despues de leerlo
            fr.close();   
            
            boolean ret = FileOperations.WriteNewFile(tempArray, accountFile);
            
        } catch (Exception e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
            LOGGER.fatal(Arrays.toString(e.getStackTrace()));
            System.out.println(e.getMessage());
            return false;
        }
        return true;
    }    
    
    // funcion para agregar una nueva cuenta
    public static JSONObject addAccount(String account, String password, String context, String accountFile){
        System.out.println("from Accounts.addAccount in file "+accountFile);
        LOGGER.info("From Accounts.addAccount");
        JSONObject json = new JSONObject();
        try(FileWriter fw = new FileWriter(accountFile, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {
            out.println("["+account+"]");
            out.println("type=aor");
            out.println("max_contacts=8");
            out.println("remove_existing=yes");
            out.println("["+account+"]");
            out.println("type=auth");
            out.println("auth_type=userpass");
            out.println("username="+account);
            out.println("password="+password);
            out.println("["+account+"]");
            out.println("type=endpoint");
            out.println("context="+context);
            out.println("dtls_auto_generate_cert=yes");
            out.println("disallow=all");
            out.println("allow=ulaw,vp8,h264,g722");
            out.println("aors="+account);
            out.println("auth="+account);
            out.println("max_audio_streams=10");
            out.println("max_video_streams=10");
            out.println("webrtc=yes");
            out.println("dtls_ca_file=/etc/asterisk/keys/portal_publisuerte_com.crt");
            out.println("direct_media=no\n");
        } catch (IOException e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
            LOGGER.fatal(Arrays.toString(e.getStackTrace()));
            System.out.println("excepcion try");
        }
        return json;
    }
    
    
    
    // funcion para agregar una nueva cuenta
//    public static ArrayList<String> killAccount(String account, String path){
//        System.out.println("From Accounts.deleteGroupExtension");
//        LOGGER.info("From Accounts.deleteGroupExtension");
//        JSONObject json = new JSONObject();
//        ArrayList<String> tempArray = new ArrayList<>();
//        try (FileReader fr = new FileReader(path)) {
//            Scanner reader = new Scanner(fr);
//            String line;  
//            while ( reader.hasNextLine() ) {
//                line=reader.nextLine();
//                if (line.contains("["+account+"]")) {
//                    boolean flag = false;
//                    while (flag == false && reader.hasNextLine()) {                                    
//                        line=reader.nextLine();
//                        System.out.println(line);
//                        if (line.length() == 0 || line == "" || line.startsWith("[")){
//                            flag = true;
//                        }
//                    }
//                    json.put("response", true);                                
//                }else{
//                    tempArray.add(line);
//                }
//            }
//            System.out.println("salio while");
//            // cierra el archivo despues de leerlo
//            fr.close();            
//            
//        } catch (Exception e) {
//            System.out.println(Arrays.toString(e.getStackTrace()));
//            LOGGER.fatal(Arrays.toString(e.getStackTrace()));
//            System.out.println(e.getMessage());
//        }
//        return tempArray;
//    }
    
    public static boolean killAccount(String account, String pjsipFile){
        System.out.println("From Extensions.deleteGroupExtension");
        LOGGER.info("From Extensions.deleteGroupExtension");
        ArrayList<String> tempArray = new ArrayList<>();
        try (FileReader fr = new FileReader(pjsipFile)) {
            Scanner reader = new Scanner(fr);
            String line;  
            while ( reader.hasNextLine() ) {
                line=reader.nextLine();
                if (line.contains("["+account+"]")) {
                    boolean flg = false;
                    while ( reader.hasNextLine() && flg == false ) {
                        line=reader.nextLine();
                        if (line.length() == 0 || line.equals("")){
                            flg = true;
                        }else if (line.startsWith("[")){
                            String nextAccount = line.trim().replace("[", "").replace("]", "");
                            if(!nextAccount.equals(account)){
                                flg = true;
                            }
                        }
                    }
                }else{
                    tempArray.add(line);
                }
            }
            System.out.println("salio while");
            // cierra el archivo despues de leerlo
            fr.close();   
            
            boolean ret = FileOperations.WriteNewFile(tempArray, pjsipFile);
            
        } catch (Exception e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
            LOGGER.fatal(Arrays.toString(e.getStackTrace()));
            System.out.println(e.getMessage());
            return false;
        }
        return true;
    }
    
    
    
    // funcion para ver la informacion de una cuenta
    public static JSONObject getAccount(String path, String account){
        System.out.println("from Accounts.getAccount");
        LOGGER.info("From Accounts.getAccount");
        JSONObject json = new JSONObject();
        try (FileReader fr = new FileReader(path)) {
            Scanner reader = new Scanner(fr);
            String line;                      
            
            while ( reader.hasNextLine() ) {
                // split para verificar si la cuenta existe
                line=reader.nextLine();
                if (line.contains("["+account+"]")) {
                    boolean flag = false;
                    JSONObject account_params = new JSONObject();
                    while (flag == false && reader.hasNextLine()) {                                    
                        line=reader.nextLine();
                        if (!line.equals("")){
                            flag = true;
                        }else{                                        
                            String[] lineArr = line.split("=");
                            account_params.put(lineArr[0], lineArr[1]);
                        }
                    }
                    json.put("params", account_params);
                    json.put("response", true);

                }
            }
            // cierra el archivo despues de leerlo
            fr.close();
        } catch (Exception e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
            LOGGER.fatal(Arrays.toString(e.getStackTrace()));
            System.out.println(e.getMessage());
        }
        return json;
    }
    
    
    // funcion para ver la informacion de una cuenta
    public static ArrayList<String> changeCodecs(String path, String account, String codecs){
        System.out.println("from Accounts.changeCodecs");
        LOGGER.info("From Accounts.changeCodecs");
        ArrayList<String> tempArray = new ArrayList<>();
        try (FileReader fr = new FileReader(path)) {
            Scanner reader = new Scanner(fr);
            String line;  
            while ( reader.hasNextLine() ) {
                // split para verificar si la cuenta existe
                line=reader.nextLine();
                if (line.contains("["+account+"]")) {
                    tempArray.add(line);
                    System.out.println("entro if");
                    boolean flag = false;
                    boolean has_codecs = false;
                    while (flag == false && reader.hasNextLine()) {                                    
                        line=reader.nextLine();
                        System.out.println(line);
                        if (line.length() == 0 || line == ""){
                            flag = true;
                        }else{
                            if (line.contains("allow")) {
                                tempArray.add("allow="+codecs);
                                has_codecs = true;                                                                                        
                            }else{
                                tempArray.add(line);                                         
                            }
                        }                                    
                    }
                    if (has_codecs == false) {
                        tempArray.add("allow="+codecs);
                        tempArray.add("\n");
                    }

                }else{
                    tempArray.add(line);
                }
            }
            System.out.println("salio while");
            // cierra el archivo despues de leerlo
            fr.close();
        } catch (Exception e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
            LOGGER.fatal(Arrays.toString(e.getStackTrace()));
            System.out.println(e.getMessage());
        }
        return tempArray;        
    }
    
}
