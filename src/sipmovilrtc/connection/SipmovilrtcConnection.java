package sipmovilrtc.connection;

import dsk2.json.JSONObject;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import org.apache.log4j.*;

public class SipmovilrtcConnection {

    public static Logger logger = Logger.getLogger("arlanda21");
    protected static String CALLBACK_DIRECTORY;
    protected static String DEFAULT_OUTGOING_ROUTE, DEFAULT_OUTGOING_PREFIX;
    protected static int DEFAULT_WAIT_TIME, DEFAULT_PRIORITY;
    protected static int DEFAULT_RETRY_TIME, DEFAULT_MAX_RETRIES;
    protected static String DEFAULT_CALLERID;
    protected static String DEFAULT_CONTEXT, DEFAULT_EXTENSION;
    protected static int BIND_PORT, INPUT_SOCKET_TIMEOUT;
    protected static ServerSocket server;
    
    public static String EXTENSIONS_FILE, PJSIP_FILE, VOICEMAIL_FILE, EXTENSIONS_FOLDER;
    public static String GROUPS_FILE, GROUPS_FOLDER;
    
    public static String COMPANY_DIRECTORY, PJSIP_INDEX, EXTENSIONS_INDEX, VOICEMAIL_INDEX, QUEUES_INDEX, FTP_USER;
    
    
    public static boolean loadExecutionParameters() {
        try {
            FileInputStream archivo = new FileInputStream("siprtc.properties");
            Properties prop = new Properties();
            prop.load(archivo);
            archivo.close();
            BIND_PORT = Integer.parseInt(
                    prop.getProperty("BIND_PORT"));
            INPUT_SOCKET_TIMEOUT = Integer.parseInt(
                    prop.getProperty("INPUT_SOCKET_TIMEOUT"));
            DEFAULT_WAIT_TIME = Integer.parseInt(
                    prop.getProperty("DEFAULT_WAIT_TIME"));
            DEFAULT_MAX_RETRIES = Integer.parseInt(
                    prop.getProperty("DEFAULT_MAX_RETRIES"));
            DEFAULT_RETRY_TIME = Integer.parseInt(
                    prop.getProperty("DEFAULT_RETRY_TIME"));
            DEFAULT_CALLERID = prop.getProperty("DEFAULT_CALLERID");
            DEFAULT_CONTEXT = prop.getProperty("DEFAULT_CONTEXT");
            DEFAULT_EXTENSION = prop.getProperty("DEFAULT_EXTENSION");
            DEFAULT_PRIORITY = Integer.parseInt(
                    prop.getProperty("DEFAULT_PRIORITY"));
            DEFAULT_OUTGOING_ROUTE = prop.getProperty("DEFAULT_OUTGOING_ROUTE");
            DEFAULT_OUTGOING_PREFIX = prop.getProperty(
                    "DEFAULT_OUTGOING_PREFIX");
            CALLBACK_DIRECTORY = prop.getProperty("CALLBACK_DIRECTORY");
            
            EXTENSIONS_FILE = prop.getProperty("EXTENSION_DIRECTORY");
            PJSIP_FILE = prop.getProperty("PJSIP_DIRECTORY");
            VOICEMAIL_FILE = prop.getProperty("VOICEMAIL_DIRECTORY");
            EXTENSIONS_FOLDER = prop.getProperty("EXTENSIONS_FOLDER");
            GROUPS_FILE = prop.getProperty("GROUPS_FILE");
            GROUPS_FOLDER = prop.getProperty("GROUPS_FOLDER");
            
            COMPANY_DIRECTORY = prop.getProperty("COMPANY_DIRECTORY");
            PJSIP_INDEX = prop.getProperty("PJSIP_INDEX");
            EXTENSIONS_INDEX = prop.getProperty("EXTENSIONS_INDEX");
            VOICEMAIL_INDEX = prop.getProperty("VOICEMAIL_INDEX");
            QUEUES_INDEX = prop.getProperty("QUEUES_INDEX");
            
            FTP_USER = prop.getProperty("FTP_USER");
            
            
            //INPUT_REQUEST_FORMAT = Integer.parseInt(
            //        prop.getProperty("INPUT_REQUEST_FORMAT"));
        } catch (Exception e) {
            logger.error("", e);
            return false;
        }
        return true;
    }
    
    public static void main(String[] args) throws Exception {
        try {
            PropertyConfigurator.configure("logger.properties");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        logger.info("Init to loadExecutionParameters()");
        loadExecutionParameters();
        logger.info("Finish to loadExecutionParameters()");
        try {
            server = new ServerSocket(55722);
            logger.info("create new ServerSocket(55722)");
            while (true) {
                Socket socket = server.accept();
                logger.info("sentence: server.accept()");
                logger.info("-- Incoming request from "
                        + socket.getInetAddress().getHostAddress());
                socket.setSoTimeout(INPUT_SOCKET_TIMEOUT);
                new Thread(new RunRequest(socket)).start();                
            }
            
        } catch (Exception e) {
            logger.info("Entro excepcion al crear el socket");
            logger.info(e.getStackTrace());
            e.printStackTrace();
        }
    }   
}


class RunRequest implements Runnable {

    private Logger logger = Logger.getLogger("runrequest");
    private Socket socket;

    public RunRequest(Socket s) {
        socket = s;
    }

    @Override
    public void run() {
        try {
            DataOutputStream output = new DataOutputStream(socket.getOutputStream());
            BufferedReader input = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
            String data = input.readLine();
            JSONObject json = new JSONObject(data);
            String tx_id = json.getString("tx_id");
            String operation = json.getString("operation");
            System.out.println("Operation: "+operation);
            logger.debug("[" + tx_id + "] Received request: " + json.toString());
            JSONObject params = json.getJSONObject("params");
            System.out.println(params);
            JSONObject ret = FileOperations.execute(tx_id, params, operation);
            JSONObject response = new JSONObject();
            response.put("tx_id", tx_id);
            response.put("result", ret);
            logger.debug("[" + tx_id + "] Result: " + response.toString());
            output.write(response.toString().getBytes());
        } catch (Exception e) {
            System.out.println("entro excepcion primer try");
        } finally {
            try {
                socket.close();
            } catch (Exception e) {
                System.out.println("entro Exception socket.close();");
            }
        }
    }
}


