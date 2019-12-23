package sipmovilrtc.connection;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dsk2.json.JSONException;
import dsk2.json.JSONObject;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import logic.Accounts;
import logic.Extensions;
import logic.Groups;
import logic.VoiceMail;
import logic.Ivrs;
import org.apache.log4j.Logger;
import static sipmovilrtc.connection.SipmovilrtcConnection.logger;

public class FileOperations {
    
    private static final String path = "C:\\Users\\Luis Merizalde\\Documents\\asterisk\\sip.conf";
    
    private static final String EXTENSION_FOLDER = SipmovilrtcConnection.EXTENSIONS_FOLDER;
    
    private static final String COMPANY_DIRECTORY = SipmovilrtcConnection.COMPANY_DIRECTORY;
    private static final String FTP_USER = SipmovilrtcConnection.FTP_USER;
    private static final Logger logger = SipmovilrtcConnection.logger;
    private static final String PJSIP_FILE = "pjsip.conf";
    private static final String EXTENSIONS_FILE = "extensions.conf";
    private static final String IVRS_FILE = "ivrs.conf";
    private static final String VOICEMAIL_FILE = "voicemail.conf";
    private static final String QUEUES_FILE = "queues.conf";
    private static final String AUDIO_FOLDER = "sounds/";
         
    public static boolean WriteNewFile(ArrayList<String> array, String path){
        try (PrintWriter pr = new PrintWriter(path)){
            for (String str: array) {
                pr.println(str);
            }
            pr.close();
            return true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }
    
    public static JSONObject execute(String tx_id, JSONObject params, String operation) {
        System.out.println(tx_id);
        System.out.println(params);
        System.out.println(operation);
        
        logger.debug("[" + tx_id + "] Call file sent.");
        
        JSONObject json = new JSONObject();        
        String[] lineArr;
        
        switch(operation) {
            case "GET_CURRENT_ACCOUNTS":
                System.out.println("FileOperation: GET_CURRENT_ACCOUNTS");
                logger.info("FileOperation: GET_CURRENT_ACCOUNTS");
                Accounts.getCurrentAccounts(path);
                break;
                
            case "ADD_ACCOUNT":      
                System.out.println("FileOperation: ADD_ACCOUNT");
                logger.info("FileOperation: ADD_ACCOUNT");
                try {
                    String account = params.getString("account");
                    String password = params.getString("password");
                    String context = params.getString("context");
                    String accountFile = COMPANY_DIRECTORY+context+"/"+PJSIP_FILE;
                    Accounts.addAccount(account, password, context, accountFile);
                } catch (Exception e) {
                }              
                break;
                
            case "DISABLE_ACCOUNT":
                System.out.println("FileOperation: DISABLE_ACCOUNT");
                logger.info("FileOperation: DISABLE_ACCOUNT");
                try {
                    System.out.println("CASE DISABLE_ACCOUNT");
                    String context_name = params.getString("context_name");
                    String account = params.getString("account");
                    String context = params.getString("context");
                    String accountFile = COMPANY_DIRECTORY+context_name+"/"+PJSIP_FILE;
                    Accounts.disableAccount(account, context, accountFile);
                } catch (Exception e) {
                }              
                break;
                
//            case "KILL_ACCOUNT": 
//                System.out.println("FileOperation: KILL_ACCOUNT");
//                logger.info("FileOperation: KILL_ACCOUNT");
//                try {
//                    String account = params.getString("account");
//                    Accounts.killAccount(path, account);                    
//                    // guarda el archivo con las extensiones que no fueron eliminadas
//                    boolean ret = WriteNewFile(tempArray, path);                   
//                } catch (Exception e) {
//                }                
                
            case "GET_ACCOUNT":  
                System.out.println("FileOperation: GET_ACCOUNT");
                logger.info("FileOperation: GET_ACCOUNT");
                try {
                    String account = params.getString("account");
                    Accounts.getAccount(path, account);
                } catch (Exception e) {
                }
//                try {
//                    json.put("response", false); //return false;
//                } catch (Exception ex) {
//                }
                
            case "CHANGE_CODECS":  
                System.out.println("FileOperation: CHANGE_CODECS");
                logger.info("FileOperation: CHANGE_CODECS");
                try {
                    String account = params.getString("account");
                    String codecs = params.getString("codecs");                    
                    ArrayList<String> tempArray = Accounts.changeCodecs(path, account, codecs);                    
                    // guarda el archivo con las extensiones que no fueron eliminadas
                    boolean ret = WriteNewFile(tempArray, path);                   
                } catch (Exception e) {
                }
                
//            case "CREATE_CONTEXT":     
//                System.out.println("FileOperation: CREATE_CONTEXT");
//                try {
//                    String context_name = params.getString("context_name"); 
//                    String context_file = EXTENSION_FOLDER + context_name +".conf";
//                    Runtime.getRuntime().exec("touch " + context_file);
//                    System.out.println("Creo archivo :"+context_file);
//                    boolean ret = Extensions.addContext(context_name, context_file); 
//                    json.put("response", ret);
//                    break;
//                } catch (Exception e) {
//                }
                
            case "VOICEMAIL_CONTEXT": 
                System.out.println("FileOperation: VOICEMAIL_CONTEXT");
                logger.info("FileOperation: VOICEMAIL_CONTEXT");
                try {
                    String context_name = params.getString("context_name");                    
//                    boolean ret = VoiceMail.addContext(context_name); 
//                    return json.put("response", ret);
                } catch (Exception e) {
                }
                
            case "ADD_VOICEMAIL": 
                System.out.println("FileOperation: ADD_VOICEMAIL");
                logger.info("FileOperation: ADD_VOICEMAIL");
                try {
                    String context_name = params.getString("context_name"); 
                    String user = params.getString("user"); 
                    String pin = params.getString("pin");
                    String name = params.getString("name"); 
                    String mail = params.getString("mail");
                    String voicemailFile = COMPANY_DIRECTORY+context_name+"/"+VOICEMAIL_FILE;
                    boolean ret = VoiceMail.addVoiceMail(context_name, user, pin, name, mail, voicemailFile); 
                    
                    json.put("response", ret);
                    break;
                } catch (Exception e) {
                }
                
            case "CHANGE_VOICEMAIL": 
                System.out.println("FileOperation: CHANGE_VOICEMAIL");
                logger.info("FileOperation: CHANGE_VOICEMAIL");
                try {
                    String context_name = params.getString("context_name"); 
                    String account = params.getString("account"); 
                    String pin = params.getString("pin");
                    String name = params.getString("name"); 
                    String mail = params.getString("mail");
                    String voicemailFile = COMPANY_DIRECTORY+context_name+"/"+VOICEMAIL_FILE;
                    boolean ret = VoiceMail.changeVoiceMail(account, pin, name, mail, voicemailFile); 
                    
                    json.put("response", ret);
                    break;
                } catch (Exception e) {
                }
                
            case "CREATE_EXTENSION": 
                System.out.println("FileOperation: CREATE_EXTENSION");
                logger.info("FileOperation: CREATE_EXTENSION");
                try {
                    String context_name = params.getString("context_name"); 
                    String extension = params.getString("extension");
                    String time_ring = params.getString("time_ring");
                    String account = params.getString("account");
                    String current_file = EXTENSION_FOLDER + context_name + ".conf";
                    boolean ret = Extensions.createExtension(context_name, extension, time_ring, account, current_file); 
                    
                    json.put("response", ret);
                    break;
                } catch (Exception e) {
                }
                
            case "EDIT_EXTENSION": 
                System.out.println("FileOperation: EDIT_EXTENSION");
                logger.info("FileOperation: EDIT_EXTENSION");
                try {
                    String old_extension = params.getString("old_extension"); 
                    String new_extension = params.getString("new_extension");
                    String context_name = params.getString("context_name");
                    String time_ring = params.getString("time_ring");
                    String account = params.getString("account");
                    String company_directory = COMPANY_DIRECTORY+context_name+"/";
                    String extensions_file = company_directory + EXTENSIONS_FILE;
                    boolean ret = Extensions.editExtension(old_extension, new_extension, context_name, account, time_ring, extensions_file); 
                    
                    json.put("response", ret);
                    break;
                } catch (Exception e) {
                }
                
            case "CREATE_COMPANY": 
                System.out.println("FileOperation: CREATE_COMPANY");
                logger.info("FileOperation: CREATE_COMPANY");
                try {  
                    String context_name = params.getString("context_name");
                    String sipmovil_trunk = params.getString("sipmovil_trunk");
                    // Creación del directorio para una nueva empresa
                    String company_directory = COMPANY_DIRECTORY+context_name+"/";
                    Runtime.getRuntime().exec("mkdir " + company_directory);
                    // creacion del directorio para almacenar los srchivos de audio
                    String audio_folder = company_directory + AUDIO_FOLDER;
                    Runtime.getRuntime().exec("mkdir " + audio_folder);
                    Runtime.getRuntime().exec("sudo chown "+FTP_USER+":"+FTP_USER+" "+ audio_folder);
                    Runtime.getRuntime().exec("sudo chmod a+rwx "+audio_folder);
                    // creacion del archivo donde se almacenaran los clientes PJSIP
                    String pjsip_file = company_directory + PJSIP_FILE;
                    Runtime.getRuntime().exec("touch " + pjsip_file);
                    Accounts.addContext(pjsip_file);
                    // creacion del archivo donde se almacenaran las extensiones
                    String extensions_file = company_directory + EXTENSIONS_FILE;
                    String ivrs_file = company_directory + IVRS_FILE;
                    Runtime.getRuntime().exec("touch " + extensions_file);
                    Runtime.getRuntime().exec("touch " + ivrs_file);
                    Extensions.addContext(context_name, extensions_file, sipmovil_trunk, ivrs_file);                    
                    // creacion del archivo donde se almacenaran los correos de las extenciones
                    String voicemail_file = company_directory + VOICEMAIL_FILE;
                    Runtime.getRuntime().exec("touch " + voicemail_file);
                    VoiceMail.addContext(context_name, voicemail_file);
                    // creacion del archivo donde se almacenaran los grupos
                    String queues_file = company_directory + QUEUES_FILE;
                    Runtime.getRuntime().exec("touch " + queues_file);
                    Groups.addContext(queues_file);          

                    json.put("response", true);
                    break;
                } catch (Exception e) {
                }
                
            case "UPDATE_TRUNK":
                System.out.println("FileOperation: UPDATE_TRUNK");
                logger.info("FileOperation: UPDATE_TRUNK");
                try {  
                    String context = params.getString("context");
                    String sipmovil_trunk = params.getString("sipmovil_trunk");
                    String company_directory = COMPANY_DIRECTORY+context+"/";
                    // creacion de la extension para cuenta webrtc
                    String extensions_file = company_directory + EXTENSIONS_FILE;
                    Extensions.updateTrunk(sipmovil_trunk, extensions_file);                    
                    json.put("response", true);
                    break;
                } catch (Exception e) {
                }
                
            case "CREATE_USER": 
                System.out.println("FileOperation: CREATE_USER");
                logger.info("FileOperation: CREATE_USER");
                try {  
                    String account = params.getString("account");
                    String password = params.getString("password");
                    String context = params.getString("context");
                    String extension = params.getString("extension");
                    String time_ring = params.getString("time_ring");
                    String pin = params.getString("pin");
                    String name = params.getString("name"); 
                    String mail = params.getString("mail");
                    String company_directory = COMPANY_DIRECTORY+context+"/";
                    // Creación de la nueva cuenta webrtc
                    String accountFile = company_directory+PJSIP_FILE;
                    Accounts.addAccount(account, password, context, accountFile);
                    // creacion de la extension para cuenta webrtc
                    String extensions_file = company_directory + EXTENSIONS_FILE;
                    Extensions.createExtension(context, extension, time_ring, account, extensions_file);                    
                    // creacion del archivo donde se almacenaran los correos de las extenciones
                    String voicemail_file = company_directory + VOICEMAIL_FILE;
                    VoiceMail.addVoiceMail(context, account, pin, name, mail, voicemail_file);
       

                    json.put("response", true);
                    break;
                } catch (Exception e) {
                }
                
            case "CREATE_GROUP": 
                System.out.println("FileOperation: CREATE_GROUP");
                logger.info("FileOperation: CREATE_GROUP");
                try {  
                    String context = params.getString("context");
                    String retryTime = params.getString("retryTime");
                    String timeout = params.getString("timeout");
                    String strategy = params.getString("strategy");
                    String extension = params.getString("extension");
                    String groupName = params.getString("groupName");
                    String slugName = params.getString("slugName");
                    String overflowTime = params.getString("overflowTime");
                    String company_directory = COMPANY_DIRECTORY+context+"/";
                    // Creación del grupo en el respectivo archivo queue.conf
                    String queueFile = company_directory+QUEUES_FILE;
                    Groups.addGroup(slugName, retryTime, timeout, strategy, queueFile);
                    // creacion de la extension para para el grupo de timbrado
                    String extensions_file = company_directory + EXTENSIONS_FILE;
                    Extensions.createGroup(extension, groupName, slugName, overflowTime, extensions_file);                    
                    
                    json.put("response", true);
                    break;
                } catch (Exception e) {
                }
                
            case "EDIT_GROUP": 
                System.out.println("FileOperation: EDIT_GROUP");
                logger.info("FileOperation: EDIT_GROUP");
                try {  
                    String context = params.getString("context");
                    String slugName = params.getString("slugName");
                    String retryTime = params.getString("retryTime");
                    String timeout = params.getString("timeout");
                    String strategy = params.getString("strategy");
                    String company_directory = COMPANY_DIRECTORY+context+"/";
                    // Actualizacion de la extension para para el grupo de timbrado
                    String queueFile = company_directory+QUEUES_FILE;
                    Groups.editGroup(slugName, retryTime, timeout, strategy, queueFile);                    
                    
                    json.put("response", true);
                    break;
                } catch (Exception e) {
                }
                
            case "EDIT_GROUP_EXTENSION": 
                System.out.println("FileOperation: EDIT_GROUP_EXTENSION");
                logger.info("FileOperation: EDIT_GROUP_EXTENSION");
                try {  
                    String context = params.getString("context");
                    String old_extension = params.getString("old_extension");
                    String new_extension = params.getString("new_extension");
                    String slugName = params.getString("slugName");
                    String overflowTime = params.getString("overflowTime");
                    String groupName = params.getString("groupName");
                    String company_directory = COMPANY_DIRECTORY+context+"/";
                    // Actualizacion de la extension para para el grupo de timbrado
                    String extensions_file = company_directory+EXTENSIONS_FILE;
                    Extensions.editGroupExtension(old_extension, new_extension,
                            slugName, groupName, overflowTime, extensions_file);                    
                    
                    json.put("response", true);
                    break;
                } catch (Exception e) {
                }
                
            case "DELETE_GROUP": 
                System.out.println("FileOperation: DELETE_GROUP");
                logger.info("FileOperation: DELETE_GROUP");
                try {  
                    String context = params.getString("context");
                    String slugName = params.getString("slugName");
                    String group_extension = params.getString("group_extension");
                    String company_directory = COMPANY_DIRECTORY+context+"/";
                    // Eliminar extension del grupo de timbrado
                    String extensions_file = company_directory + EXTENSIONS_FILE;
                    Extensions.deleteGroupExtension(group_extension, extensions_file);                    
                    // Eliminar el grupo del archivo queues.conf
                    String queues_file = company_directory + QUEUES_FILE;
                    Groups.deleteGroup(slugName, queues_file);
                    json.put("response", true);
                    break;
                } catch (Exception e) {
                }
              
            case "CREATE_IVR":
                System.out.println("FileOperation: CREATE_IVR");
                logger.info("FileOperation: CREATE_IVR");
                try {  
                    String context = params.getString("context");
                    String extension = params.getString("extension");
                    String audio_name = params.getString("audio_name");
                    String ivr_name = params.getString("ivr_name");
                    String wait_time = params.getString("wait_time");
                    String ivrSlug = params.getString("ivrSlug");
                    String options = params.getString("options");
                    String timezone = params.getString("timezone");
                    Boolean data_input = params.getBoolean("data_input");
                    String action = params.getString("action");
                    Boolean call_extension = params.getBoolean("call_extension");
                    String company_directory = COMPANY_DIRECTORY+context+"/";
                    // creacion de la extension para el ivr
                    String extensions_file = company_directory + EXTENSIONS_FILE;
                    Extensions.createIVR(extension, ivrSlug, extensions_file);
                    // creacion de la logica del ivr
                    String ivr_file = company_directory + IVRS_FILE;
                    String audio_path = company_directory + AUDIO_FOLDER + audio_name;
                    Ivrs.createIvr(ivrSlug, audio_path, ivr_name, wait_time, 
                        options, timezone, ivr_file, data_input, context, 
                        action, call_extension);
                    json.put("response", true);
                    break;
                } catch (JSONException e) {
                    System.out.println("Entro excepcion CREATE_IVR");
                    e.getStackTrace();
                    logger.info(e.getStackTrace());
                    System.out.println(Arrays.toString(e.getStackTrace()));
                }
                
            case "EDIT_IVR":
                System.out.println("FileOperation: EDIT_IVR");
                logger.info("FileOperation: EDIT_IVR");
                try {  
                    String context = params.getString("context");
                    String extension = params.getString("extension");
                    String audio_name = params.getString("audio_name");
                    String ivr_name = params.getString("ivr_name");
                    String wait_time = params.getString("wait_time");
                    String ivrSlug = params.getString("ivrSlug");
                    String options = params.getString("options");
                    String timezone = params.getString("timezone");
                    String before_extension = params.getString("before_extension");
                    Boolean data_input = params.getBoolean("data_input");
                    String action = params.getString("action");
                    Boolean call_extension = params.getBoolean("call_extension");
                    String company_directory = COMPANY_DIRECTORY+context+"/";
                    // edicion de la extension para el ivr
                    if (extension.equals(before_extension) == false) {
                        String extensions_file = company_directory + EXTENSIONS_FILE;
                        Extensions.editIVR(extension, before_extension, ivrSlug, 
                                extensions_file);
                    }
                    
                    // creacion de la logica del ivr
                    String ivr_file = company_directory + IVRS_FILE;
                    String audio_path = company_directory + AUDIO_FOLDER + audio_name;
                    Ivrs.editIVR(ivrSlug, audio_path, ivr_name, wait_time, 
                            options, timezone, ivr_file, data_input, context, 
                            action, call_extension);
                    json.put("response", true);
                    break;
                } catch (JSONException e) {
                    System.out.println("Entro excepcion CREATE_IVR");
                    e.getStackTrace();
                    logger.info(e.getStackTrace());
                    System.out.println(Arrays.toString(e.getStackTrace()));
                }
                
            case "DELETE_IVR": 
                System.out.println("FileOperation: DELETE_IVR");
                logger.info("FileOperation: DELETE_IVR");
                try {  
                    String context = params.getString("context");
                    String extension = params.getString("extension");
                    String ivrSlug = params.getString("ivrSlug");
                    String audio_name = params.getString("audio_name");
                    String company_directory = COMPANY_DIRECTORY+context+"/";
                    String audio_path = COMPANY_DIRECTORY+context+"/"+AUDIO_FOLDER+audio_name;
                    // Eliminar extension del grupo de timbrado
                    String extensions_file = company_directory + EXTENSIONS_FILE;
                    Extensions.deleteGroupExtension(extension, extensions_file);                    
                    // Eliminar el ivr del archivo ivr.conf
                    String ivrs_file = company_directory + IVRS_FILE;
                    Ivrs.deleteIvr(ivrSlug, ivrs_file);
                    // Eliminar archivo de audio del la carpeta de sonidos
                    Runtime.getRuntime().exec("rm " + audio_path);
                    json.put("response", true);
                    break;
                } catch (Exception e) {
                }
                
            case "UPDATE_TIMEZONE": 
                System.out.println("FileOperation: UPDATE_TIMEZONE");
                logger.info("FileOperation: UPDATE_TIMEZONE");
                try {  
                    String ivrs = params.getString("ivrs");
                    String timezone = params.getString("timezone");
                    Gson gson = new Gson();
                    JsonArray ivrArray = gson.fromJson(ivrs, JsonArray.class);
                    for (JsonElement pa : ivrArray) {
                        JsonObject element = pa.getAsJsonObject();
                        String context = element.get("company__slug_name").getAsString();
                        String ivrSlug = element.get("slug_name").getAsString();
                        String company_directory = COMPANY_DIRECTORY+context+"/";
                        String ivr_file = company_directory + IVRS_FILE;
                        Ivrs.updateIvr(ivrSlug, timezone, ivr_file);
                    }
                    json.put("response", true);
                    break;
                } catch (Exception e) {
                }
                
            case "UPDATE_DIDS": 
                System.out.println("FileOperation: UPDATE_DIDS");
                logger.info("FileOperation: UPDATE_DIDS");
                try {  
                    String context = params.getString("context");
                    String old_dids = params.getString("old_dids");
                    String new_dids = params.getString("new_dids");
                    String extension = params.getString("extension");
                    String company_directory = COMPANY_DIRECTORY+context+"/";
                    // Eliminar los did existentes
                    String extension_file = company_directory + EXTENSIONS_FILE;
                    Extensions.deleteDIDs(old_dids, extension_file);                    
                    // escribir los nuevos DIDs con la extension pertinente
                    Extensions.addDIDs(new_dids, extension, context, 
                            extension_file);

                    json.put("response", true);
                    break;
                } catch (Exception e) {
                }
                
            case "NEW_DIDS": 
                System.out.println("FileOperation: NEW_DIDS");
                logger.info("FileOperation: NEW_DIDS");
                try {  
                    String data = params.getString("data");
                    Gson gson = new Gson();
                    JsonArray didsArray = gson.fromJson(data, JsonArray.class); 
                    for (JsonElement pa : didsArray) {
                        JsonObject element = pa.getAsJsonObject();
                        String context = element.get("context").getAsString();
                        JsonArray didArray = element.get("dids").getAsJsonArray();
                        String company_directory = COMPANY_DIRECTORY+context+"/";
                        // escribir los nuevos did
                        String extension_file = company_directory + EXTENSIONS_FILE;
                        Extensions.addDIDs(didArray, context, extension_file);
                    }
                    json.put("response", true);
                    break;
                } catch (Exception e) {
                }
                
            case "DELETE_USER": 
                System.out.println("FileOperation: NEW_DIDS");
                logger.info("FileOperation: NEW_DIDS");
                try {  
                    String context = params.getString("context");
                    String extension = params.getString("extension");
                    String account = params.getString("account");
                    String company_directory = COMPANY_DIRECTORY+context+"/";
                    //eliminar extension
                    String extension_file = company_directory + EXTENSIONS_FILE;
                    Extensions.deleteGroupExtension(extension, extension_file);
                    //eliminar cuenta pjsip
                    String pjsip_file = company_directory + PJSIP_FILE;
                    Accounts.killAccount(account, pjsip_file);
                    //eliminar cuenta voicemail
                    String voicemail_file = company_directory + VOICEMAIL_FILE;
                    VoiceMail.deleteVoicemail(account, voicemail_file);
                    
                    json.put("response", true);
                    break;
                } catch (Exception e) {
                }
                
            default:
                try {
                    json.put("response", false); //return false;
                } catch (JSONException ex) {
                }
        }
        
        
        try {
            json.put("response", true); //return false;
            
        } catch (Exception ex) {
        }
        return json;
    }
    
}