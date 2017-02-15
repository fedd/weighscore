package com.weighscore.neuro.server;

import java.lang.reflect.*;
import com.weighscore.neuro.*;
import java.net.*;
import java.io.*;
import java.util.*;
//import java.text.da

public class nns extends Thread {

    private static final int PORT=1133;
    private final static String NAME = "Weighscore Neural Network Server";

    private Class[] simpleTypes, outputTypes;
    private boolean giveNNdef;
    private String separator = "\t";
    private boolean okerr;

    protected static boolean mayAcceptClient = true;
    protected static boolean stoppedAllClients = false;
    //private boolean ivefinished = false;

    private Socket client;

    public nns(boolean okerr, String separator, boolean giveNeuralNetworkDefinition, ThreadGroup tg){
        super(tg, "ClientThread");

        this.okerr = okerr;
        if(separator!=null)
            this.separator = separator;

        simpleTypes = new Class[2];
        String[] q = new String[1];
        simpleTypes[0] = NeuralNetworkLocal.class;
        simpleTypes[1] = q.getClass();

        outputTypes = new Class[3];
        outputTypes[0] = NeuralNetworkLocal.class;
        outputTypes[1] = q.getClass();
        outputTypes[2] = OutputStream.class;

        this.giveNNdef=giveNeuralNetworkDefinition;
        client=null;
    }

    private static void printHelp(){
        System.out.print(NAME);
        System.out.print('.');
        System.out.print(' ');
        System.out.println(NeuralNetworkLocal.COPYRIGHT);
        System.out.println(NeuralNetworkLocal.WELCOME);

        System.out.println("Usage: nns [-p PORT] [-g] [-o] [-l LOGFILE] [-h]");
        System.out.println("Options: ");
        System.out.println("  -p PORT     specify the PORT to listen on; default is " + PORT);
        System.out.println("  -g          allow retreive the neural network XML definitions");
        System.out.println("  -o          add OK  (except for XML retrievals) or ER as the first output field");
        System.out.println("  -l LOGFILE  set the file to get output messages instead of console");
        System.out.println("  -S          shutdown the server");
        System.out.println("  -h          output this information");
    }

    public static void main(String[] args) throws IOException {
        boolean givexml = false;
        boolean okerr = true;
        int port = PORT;

        boolean doShutdown=false;

        //Vector threads = new Vector();

        // разбор аргументов
        for(int i=0; i<args.length; i++){
            if(args[i].equals("-h")){
                printHelp();
                System.exit(0);
                continue;
            }
            if(args[i].equals("-g")){// показывать ксмль файлы с тренслятором и нейросетью
                givexml=true;
                continue;
            }
            if(args[i].equals("-S")){// запустить процесс шатдауна
                doShutdown=true;
                continue;
            }
            if(args[i].equals("-o")){// писать OK или ERR
                okerr=true;
                continue;
            }
            if(args[i].equals("-p")){// номер порта
                i++;
                try{
                    port = Integer.parseInt(args[i]);
                }
                catch(Exception e){
                    System.err.println("Please specify port number after -p switch");
                    System.exit(-1);
                }
                continue;
            }
            if(args[i].equals("-l")){// файл лога ошибок
                i++;
                try{
                    String filename = args[i];
                    FileOutputStream errstream = new FileOutputStream(filename, true);
                    PrintStream errps = new PrintStream(errstream, true);
                    //System.setErr(errps);
                    System.setOut(errps);
                }
                catch(Exception e){
                    System.err.println("Coudln't set log file");
                    System.exit(-1);
                }
                continue;
            }
        }

        if(doShutdown){
            sendShutDownSignal(port);
            System.exit(0);
        }


        // запуск тредов сервера
        ServerSocket serverSocket = null;

        System.out.print(NAME);
        System.out.print('.');
        System.out.print(' ');
        System.out.println(NeuralNetworkLocal.COPYRIGHT);
        //System.out.println(NeuralNetwork.WELCOME);

        try {
            serverSocket = new ServerSocket(port);
            System.out.print(new Date());
            System.out.print("\t");
            System.out.println("NEURAL SERVER START");
            System.out.println("Listening port " + port);
            System.out.println("-------------------------");
        } catch (IOException e) {
            /*System.err.println();
            System.err.print(new Date());*/
            System.err.println("Could not listen on port: " + port);
            System.exit(-1);
        }

        serverSocket.setSoTimeout(100);

        ShutDown shutdown = new ShutDown("Shutdown");
        Runtime.getRuntime().addShutdownHook(shutdown);

        ThreadGroup tg = new ThreadGroup("Clients");
        nns servthread = new nns(okerr, null, givexml, tg);

        while (mayAcceptClient) {
            try{
                servthread.client = serverSocket.accept();
            }
            catch(SocketTimeoutException e){
                continue;
            }
            System.out.print(new Date());
            System.out.print("\t");
            System.out.print(servthread.client.getInetAddress().toString());
            System.out.print("\t");
            System.out.print(servthread.client.hashCode());
            System.out.println("\tCONNECT");

            servthread.client.setSoTimeout(30000);
            servthread.start();

            servthread = new nns(okerr, null, givexml, tg);
        }

        System.out.println("Shutting down listening port " + port);


        // достаем активные треды
        Thread[] tarr = new Thread[tg.activeCount()]; // new Thread[Thread.activeCount()];
        tg.enumerate(tarr);//        Thread.enumerate(tarr);
        // посылаем в сокеты всех активных тредов конец стрима
        for(int i=0; i<tarr.length;i++){
            //try
            {
                nns thr = (nns) tarr[i];
                try{
                    thr.client.shutdownInput();
                }catch(Exception e){}
                try{
                    thr.client.shutdownOutput();
                }catch(Exception e){}
            }
            //catch(ClassCastException e){}
        }
        // дожидаемся, когда все треды завершат работу
        for(int i=0; i<tarr.length;i++){
            //try
            {
                nns thr = (nns) tarr[i];
                try{
                    while (thr.isAlive()) {
                        try {
                            Thread.sleep(5);
                        } catch (InterruptedException ex) {
                        }
                    }
                }
                catch(NullPointerException e){}
            }
            //catch(ClassCastException e){}
        }

        NeuralNetworkFactory.getNeuralNetworkFactory().saveAllNetworks();

        serverSocket.close();
        System.out.println("-------------------------");
        System.out.print(new Date());
        System.out.println("\tNEURAL SERVER STOP");
        System.out.println("*************************");
        System.out.println();

        stoppedAllClients = true;
    }

    public void run() {

        NeuralNetworkLocal persistent = null;

        try {
            OutputStream out = client.getOutputStream();
            BufferedReader in = new BufferedReader(
                                    new InputStreamReader(
                                    client.getInputStream()));

            String inputLine;

            String[] command = new String[2];
            do{
                try{
                    if ((inputLine = in.readLine()) != null) {

                        StringWriter logsw = new StringWriter();
                        PrintWriter logpw = new PrintWriter(logsw);

                        //String[] command = inputLine.split(separator, 2);
                        int sep = inputLine.indexOf(separator);
                        if (sep > 0) {
                            command[0] = inputLine.substring(0, sep);
                            command[1] = inputLine.substring(sep +
                                    separator.length());
                        } else {
                            command[0] = inputLine;
                            command[1] = null;
                        }

                        //synchronized(this.getClass())
                        {
                            //System.out.print(new Date());
                            //System.out.print("\t(");
                            //System.out.print(this.client.getInetAddress().toString());
                            //System.out.print(")\tEXEC \"" + inputLine + "\": ");
                            logpw.print("EXEC \"" + inputLine + "\": ");

                            // persistent command
                            if (command[0].equals("persistent")) {
                                try {
                                    if (command[1] != null) {
                                        persistent = (NeuralNetworkLocal)
                                                NeuralNetworkLocal.
                                                getNeuralNetwork(command[1]);

                                        //if (okerr)
                                        {
                                            out.write("OK\r\n".getBytes());
                                        }
                                        out.flush();
                                        //System.out.println("OK");
                                        logpw.print("OK");
                                        clientLog(logsw.toString());
                                        continue;
                                    } else {
                                        if (okerr) {
                                            out.write("ER\t".getBytes());
                                        }
                                        out.write(
                                                "Neural network name not specified".
                                                getBytes());
                                        out.write('\r');
                                        out.write('\n');
                                        out.flush();
                                        //System.out.println("ERROR: Neural network name not specified");
                                        logpw.print(
                                                "ERROR: Neural network name not specified");
                                        clientLog(logsw.toString());
                                        continue;
                                    }
                                } catch (NeuralException e) {
                                    if (okerr) {
                                        out.write("ER\t".getBytes());
                                    }
                                    out.write("No such neural network - ".
                                              getBytes());
                                    out.write(command[1].getBytes());
                                    out.write('\r');
                                    out.write('\n');
                                    out.flush();
                                    //System.out.println("ERROR: no such network - " + command[1]);
                                    logpw.print("ERROR: no such network - " +
                                                command[1]);
                                    clientLog(logsw.toString());
                                    continue;
                                } catch (ClassCastException e) {
                                    if (okerr) {
                                        out.write("ER\t".getBytes());
                                    }
                                    out.write("No such neural network - ".
                                              getBytes());
                                    out.write(command[1].getBytes());
                                    out.write('\r');
                                    out.write('\n');
                                    out.flush();
                                    //System.out.println("ERROR: no such network - " + command[1]);
                                    logpw.print("ERROR: no such network - " +
                                                command[1]);
                                    clientLog(logsw.toString());
                                } catch (Exception e) {
                                    if (okerr) {
                                        out.write("ER\t".getBytes());
                                    }
                                    out.write("INTERNAL ERROR".getBytes());
                                    out.write(command[1].getBytes());
                                    out.write('\r');
                                    out.write('\n');
                                    out.flush();

                                    //System.out.println("ERROR (Unprocessed)");
                                    //e.printStackTrace(System.out);
                                    logpw.println("ERROR (Unprocessed)");
                                    e.printStackTrace(logpw);
                                    clientLog(logsw.toString());
                                }

                            } else if (command[0].equals("quit")) {
                                //System.out.println("OK");
                                logpw.print("OK");
                                clientLog(logsw.toString());
                                break;
                            } else if (command[0].equals("shutdown")) {

                                if (this.client.getInetAddress().
                                    isLoopbackAddress()) {
                                    out.write("OK\r\n".getBytes());
                                    out.flush();
                                    logpw.print("OK");
                                    clientLog(logsw.toString());
                                    mayAcceptClient = false;
                                    break;
                                } else {
                                    out.write("ER\t".getBytes());
                                    out.write("ERROR - No Such Command: ".
                                              getBytes());
                                    out.write(command[0].getBytes());
                                    out.write('\r');
                                    out.write('\n');
                                    out.flush();

                                    logpw.print(
                                            "ERROR (Shutdown command should be passed from a loopback address)");
                                    clientLog(logsw.toString());
                                    break;
                                }
                            }

                            NeuralNetworkLocal neuralNetwork;
                            if (persistent != null) {
                                neuralNetwork = persistent;
                            } else {
                                // выцыганить имя сети, если это не сеть, оставить в аргументах
                                String[] comm;
                                try {
                                    comm = command[1].split(separator, 2);
                                } catch (NullPointerException e) {
                                    //System.out.println("ERROR (No Arguments)");
                                    logpw.print("ERROR (No Arguments)");
                                    clientLog(logsw.toString());
                                    if (okerr) {
                                        out.write("ER\t".getBytes());
                                    }
                                    out.write(
                                            "Protocol error: no arguments specified\r\n".
                                            getBytes());
                                    break;
                                }
                                String nnname = comm[0];
                                try {
                                    neuralNetwork = (NeuralNetworkLocal)
                                            NeuralNetworkLocal.
                                            getNeuralNetwork(nnname);
                                    if (comm.length <= 1)
                                        command[1] = null; // выцыганили
                                    else
                                        command[1] = comm[1]; // выцыганили
                                } catch (NeuralException e) {
                                    neuralNetwork = null;
                                }
                            }

                            try {

                                // **********************************************
                                exec(neuralNetwork, command[0], command[1], out);
                                // **********************************************

                                //System.out.println("OK");
                                logpw.print("OK");
                                clientLog(logsw.toString());
                            } catch (ArrayIndexOutOfBoundsException e) {
                                //System.out.println("ERROR (Not enough parameters)");
                                logpw.print("ERROR (Not enough parameters)");
                                clientLog(logsw.toString());

                                if (okerr) {
                                    out.write("ER\t".getBytes());
                                }
                                out.write("Not enough parameters".getBytes());
                                out.write("\r\n".getBytes());
                            } catch (NeuralServerException e) {
                                //System.out.println("ERROR (Neural: " + e.getMessage() + ")");
                                logpw.print("ERROR (Neural: " + e.getMessage() +
                                            ")");
                                clientLog(logsw.toString());

                                if (okerr) {
                                    out.write("ER\t".getBytes());
                                }
                                out.write("Neural server error: ".getBytes());
                                out.write(e.getMessage().getBytes());
                                out.write("\r\n".getBytes());
                                //e.printStackTrace(new PrintStream(out));
                            } catch (Exception e) {
                                //System.out.println("ERROR (Unprocessed)");
                                //e.printStackTrace(System.out);
                                logpw.println("ERROR (Unprocessed)");
                                e.printStackTrace(logpw);
                                clientLog(logsw.toString());

                                if (okerr) {
                                    out.write("ER\t".getBytes());
                                }
                                out.write("INTERNAL ERROR".getBytes());
                                out.write("\r\n".getBytes());
                            }
                        }
                    }
                }
                catch(java.net.SocketTimeoutException to){
                    clientLog("TIMEOUT EXPIRED");
                    break;
                }
                out.flush();
            }
            while(mayAcceptClient && persistent!=null);

            //System.out.print(new Date());
            //System.out.print("\t(");
            //System.out.print(client.getInetAddress().toString());
            //System.out.println(")\tDISCONNECT");
            clientLog("DISCONNECT");

            out.flush();
            out.close();
            in.close();
            client.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected String getContentType(String method){
        Method m;
        try {
            m = this.getClass().getMethod(method, simpleTypes);
            return "text/plain";
        } catch (NoSuchMethodException ex) {
            try {
                m = this.getClass().getMethod(method, outputTypes);
                return "text/xml";
            } catch (NoSuchMethodException ex1) {
                throw new NeuralServerException("No such command - " + method);
            }
        }
    }


    protected void exec(NeuralNetworkLocal neuralNetwork, String method, String arguments, OutputStream out){

        String args[];
        if(arguments!=null)
            args = arguments.split(separator);
        else
            args=null;
        Method m;

        Object[] arg = new Object[2];
        arg[0] = neuralNetwork;
        arg[1] = args;
        try {
            try {
                m = this.getClass().getMethod(method, simpleTypes);
                String[] strret = (String[]) m.invoke(this, arg);
                if(okerr){
                    out.write("OK".getBytes());
                    out.write(this.separator.getBytes());
                }
                for (int i = 0; i < strret.length; i++) {
                    if (i > 0) {
                        out.write(this.separator.getBytes());
                    }
                    out.write(strret[i].getBytes());
                }
                out.write('\r');
                out.write('\n');
            } catch (NoSuchMethodException nometh) {
                m = this.getClass().getMethod(method, outputTypes);
                if (m.getParameterTypes().length == 3) {
                    Object[] outarg = new Object[3];
                    outarg[0] = neuralNetwork;
                    outarg[1] = args;
                    outarg[2] = out;
                    m.invoke(this, outarg);
                }
            }
        } catch (InvocationTargetException ex) {
            Throwable exex = ex.getTargetException();
            if (exex.getClass().equals(NeuralException.class)) {
                throw new NeuralServerException(
                        "Neural system failed to execute command \"" + method +
                        (neuralNetwork == null ? "" :
                         "\t" + neuralNetwork.getName()) +
                        (arguments == null ? "" : "\t" + arguments) + "\": " +
                        exex.getMessage());
            }
            else if (exex.getClass().equals(NullPointerException.class)) {
                throw new NeuralServerException(
                        "Neural server failed to execute command \"" + method +
                        (neuralNetwork == null ? "" :
                         "\t" + neuralNetwork.getName()) + "\t" +
                        (arguments == null ? "" : "\t" + arguments) +
                        "\": No Such Neural Network or No Arguments");
            }
            else {
                throw new NeuralServerException(
                        "Neural server failed to execute command \"" + method +
                        (neuralNetwork == null ? "" :
                         "\t" + neuralNetwork.getName()) + "\t" +
                        (arguments == null ? "" : "\t" + arguments) +
                        "\" due to internal error: " +
                        exex.toString() /*.getMessage()*/);
            }
        } catch (NoSuchMethodException e){
            throw new NeuralServerException(
                    "Neural server failed to execute command \"" + method +
                    (neuralNetwork==null?"":"\t" + neuralNetwork.getName()) + "\t" + (arguments==null?"":"\t" + arguments) + "\": No Such Command");
        }
        catch (Exception ex) {
            throw new RuntimeException("Server was unable execute command \"" + method + (neuralNetwork==null?"":"\t" + neuralNetwork.getName()) + "\t" + arguments +"\". Unhandled exception. Please send the log to developers", ex);
        }
    }



    // ********
    // commands
    // ********
    public String[] ask(NeuralNetworkLocal neuralNetwork, String[] args){
        return neuralNetwork.ask(args);
    }

    public String[] teach(NeuralNetworkLocal neuralNetwork, String[] args){
        return Util.convertArr(neuralNetwork.teach(args));
    }

    public String[] test(NeuralNetworkLocal neuralNetwork, String[] args){
        return Util.convertArr(neuralNetwork.test(args));
    }

    public void getDefinition(NeuralNetworkLocal neuralNetwork, String args[], OutputStream out){
        if (giveNNdef)
            neuralNetwork.outputDefinition(out);
        else
            throw new NeuralServerException(
                    "Unable to get neural network definition");
    }

    public String[] getFieldNames(NeuralNetworkLocal neuralNetwork, String[] args){
        return neuralNetwork.getFieldNames();
    }

    public void getTranslatorDefinition(NeuralNetworkLocal neuralNetwork, String[] args, OutputStream out){
        neuralNetwork.outputTranslatorDefinition(out);
    }

    public String[] getAnswerFieldName(NeuralNetworkLocal neuralNetwork, String[] args) {
        String[] ret = new String[1];
        int index;

        if(args==null){
            throw new NeuralServerException("Too few parameters for command getAnswerFieldName");
        }

        try {
            index = Integer.parseInt(args[0]);
        } catch (Exception e) {
            throw new NeuralServerException(
                    "Field index for network answer field not specified");
        }

        ret[0] = neuralNetwork.getAnswerFieldName(index);
        return ret;
    }

    public String[] getAskFieldName(NeuralNetworkLocal neuralNetwork, String[] args) {
        String[] ret = new String[1];
        int index;
        if(args==null){
            throw new NeuralServerException("Too few parameters for command getAskFieldName");
        }
        try {
            index = Integer.parseInt(args[0]);
        } catch (Exception e) {
            throw new NeuralServerException(
                    "Field index for network ask field not specified");
        }
        ret[0] = neuralNetwork.getAskFieldName(index);
        return ret;
    }

    public String[] getAnswerSize(NeuralNetworkLocal neuralNetwork, String[] args) {
        String[] ret = new String[1];
        ret[0] = Integer.toString(neuralNetwork.getAnswerSize());
        return ret;
    }

    public String[] getAskSize(NeuralNetworkLocal neuralNetwork, String[] args) {
        String[] ret = new String[1];
        ret[0] = Integer.toString(neuralNetwork.getAskSize());
        return ret;
    }

    public String[] nextEpoch(NeuralNetworkLocal neuralNetwork, String[] args) {
        neuralNetwork.nextEpoch();
        String[] ret = { "OK" };
        return ret;
    }





    public void clientLog(String s){
        synchronized(this.getClass()){
            System.out.print(new Date());
            System.out.print('\t');
            System.out.print(this.client.getInetAddress().toString());
            System.out.print('\t');
            System.out.print(this.client.hashCode());
            System.out.print('\t');
            System.out.println(s);
        }
    }

    private static void sendShutDownSignal(int port){
        Socket s = null;
        try {
            s = new Socket(InetAddress.getByName("localhost"), port);
            s.setSoTimeout(10000);

            PrintWriter out = new PrintWriter(s.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(s.
                        getInputStream()));

            out.println("shutdown");
            out.flush();

            int o=0, k=0;

            try {
                o = in.read();
                k = in.read();
            } catch (SocketTimeoutException ex2) {
                System.err.println("Server does not respond on port " + port);
                System.exit( -1);
            }

            if(o!='O' && k!='K'){
                System.err.println("Server on port " + port + " rejected to shutdown.");
                System.exit( -1);
            }
            out.close();
            in.close();
        } catch (IOException ex2) {
            System.err.println("Coudln't send shut down signal: " + ex2.getMessage());
            System.exit(-1);
        } finally {
            try {
                s.close();
            } catch (IOException ex) {
            }
        }
    }

    private static String[] commandExample(String host,
                                           int port,
                                           String networkName,
                                           String command,
                                           String arguments){
        Socket s = null;
        String[] ret;
        try {
            s = new Socket(host, port);
            s.setSoTimeout(10000);

            PrintWriter out = new PrintWriter(s.getOutputStream(), true);
            BufferedReader in =
                new BufferedReader(new InputStreamReader(s.getInputStream()));

            out.println(command + '\t' + networkName + '\t' + arguments);
            out.flush();

            //command.replace('t','f');

            try {
                String reply = in.readLine();
                String[] replyArr = reply.split("\t" ,2);
                if(replyArr[0].equals("OK")){
                    try {
                        ret = replyArr[1].split("\t");
                    } catch (ArrayIndexOutOfBoundsException e) {
                        ret = new String[0];
                    }
                }
                else{
                    throw new RuntimeException("Server error: " + replyArr[1]);
                }
            } catch (SocketTimeoutException ex2) {
                throw new RuntimeException("Server does not respond on port " +
                                           port, ex2);
            }
            out.close();
            in.close();
            s.close();
        } catch (IOException ex2) {
            throw new RuntimeException(ex2);
        }
        return ret;
    }
}
