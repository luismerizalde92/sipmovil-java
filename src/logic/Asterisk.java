/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package logic;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Scanner;
import org.apache.log4j.Logger;
import sipmovilrtc.connection.FileOperations;
import sipmovilrtc.connection.SipmovilrtcConnection;

/**
 *
 * @author Usuario
 */
public class Asterisk {
    private static final String MANAGER_FILE = "/etc/asterisk/manager.conf";
    private static final Logger LOGGER = SipmovilrtcConnection.logger;
    
    
    public static boolean addManagerUser(String user, String ip, String password, 
            String prev_user) throws IOException {
        
        System.out.println("FROM Asterisk.addManagerUser");
        LOGGER.info("From Asterisk.addManagerUser");
       
        ArrayList<String> managerArray = new ArrayList<>();
        
        if (user.equals(prev_user)){
            managerArray.add("\n");
        }        
        managerArray.add("["+user+"]");
        managerArray.add("secret = "+password);
        managerArray.add("deny=0.0.0.0/0.0.0.0");
        managerArray.add("permit = 127.0.0.1/255.255.255.0");
        managerArray.add("permit = "+ip+"/255.255.255.0");
        managerArray.add("read = all");
        managerArray.add("write = all");
        managerArray.add("writetimeout = 1000");

        boolean ret = createUpdateDeleteManager(managerArray, prev_user, 
            MANAGER_FILE, false);
        
        
        reloadManager();
        
        return ret;
    }
    
    public static boolean deleteManagerUser(String user) throws IOException {
        
        System.out.println("FROM Asterisk.deleteManagerUser");
        LOGGER.info("From Asterisk.deleteManagerUser");
        
        ArrayList<String> managerArray = new ArrayList<>();
        
        boolean ret = createUpdateDeleteManager(managerArray, user, 
            MANAGER_FILE, true);   
        
        reloadManager();
        
        return ret;
    }
    
    private static void reloadManager() throws IOException{
        String command = "sudo asterisk -rx manager reload";  
        System.out.println("el comando a ejecutar: "+command);
        Process p = Runtime.getRuntime().exec(command);
        BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String s;
        System.out.println("antes de while respuesta comando");
        while ((s = stdInput.readLine()) != null) {
                System.out.println(s);
        }
    }
    
    public static boolean createUpdateDeleteManager(ArrayList<String> managerArray, 
            String user, String managerFile, boolean delete) throws IOException{
        System.out.println("From Asterisk.createOrUpdateManager");
        LOGGER.info("From Asterisk.createOrUpdateManager");
        
        ArrayList<String> tempArray = new ArrayList<>();
        try (FileReader fr = new FileReader(managerFile)) {
            Scanner reader = new Scanner(fr);
            String line;  
            boolean extensionExists = false;
            while ( reader.hasNextLine() ) {
                line=reader.nextLine();
                if (line.contains("["+user+"]")) {
                    extensionExists = true;
                    if (delete == false) {
                        System.out.println("ENTRO primera adicion");
                        tempArray.addAll(managerArray);
                    }                    
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
            if (extensionExists == false && delete == false){
                System.out.println("ENTRO segunda adicion");
                tempArray.addAll(managerArray);
            }
            // cierra el archivo despues de leerlo
            fr.close();   
            
            boolean ret = FileOperations.WriteNewFile(tempArray, managerFile);
            return ret;
        }
    }
}
