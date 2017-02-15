package com.weighscore.neuro;

import java.net.*;
import java.io.OutputStream;
import java.io.*;
import com.weighscore.neuro.plugins.XmlFileOrigin;

public class NeuralNetworkRemote extends NeuralNetwork {
    private static final long RELOADAFTERMILLIS = 10*1000*60;


    private String host, name, remotename;
    private int port = 1133;
    private Socket socket = null;
    private String separator = "\t";
    private PrintWriter sockout;
    private BufferedReader sockin;

    private NeuralNetworkLocal localNN = null;
    private long timeloaded = 0;

    protected NeuralNetworkRemote() {
    }

    public void setName(String name){
        URI nuri;
        try {
            nuri = new URI(name);
        } catch (URISyntaxException ex) {
            throw new NeuralException("Bad neural network name", ex);
        }
        setName(nuri);
    }

    private synchronized void setName(URI nuri){
        if(socket!=null && socket.isConnected()){
            throw new NeuralException("Can not set name for the connected remote neural interface");
        }
        if(nuri.getScheme().equals("neuro")){
            this.host=nuri.getHost();
            this.port=nuri.getPort();
            this.name=nuri.toString();
            int slashpos = this.name.lastIndexOf('/');
            this.remotename = this.name.substring(slashpos+1);
        }
        else{
            throw new NeuralException("Not a remote neural name");
        }
    }

    public synchronized void connect(){
        if(socket==null){
            try {
                this.socket = new Socket(host, port);
                sockout = new PrintWriter(socket.getOutputStream(), true);
                sockin = new BufferedReader(new InputStreamReader(socket.
                        getInputStream()));
            } catch (Exception ex) {
                throw new NeuralException("Coudn't open a connection to " +
                                          host + ':' + port, ex);
            }

            String[] arg = {this.remotename};
            command("persistent", arg);
        }
    }

    public String[] ask(String[] question){
        String[] ans = command("ask", question);
        this.processEvent(new NeuralEvent(this, NeuralEvent.ASKED));
        return ans;
    }

    public double[] teach(String[] questionAnswer){
        double[] ans = Util.convertArr(command("teach", questionAnswer), 0);
        this.processEvent(new NeuralEvent(this, NeuralEvent.TAUGHT));
        return ans;
    }

    public double[] test(String[] questionAnswer){
        double [] ans = Util.convertArr(command("test", questionAnswer), 0);
        this.processEvent(new NeuralEvent(this, NeuralEvent.TESTED));
        return ans;
    }

    public String[] getFieldNames(){
        String[] arg = new String[0];
        return command("getFieldNames", arg);
    }

    public String getName(){
        return this.name;
    }

    public String getAnswerFieldName(int index) {
        String[] arg = { String.valueOf(index) };
        return command("getAnswerFieldName", arg)[0];
    }

    public int getAnswerSize() {
        String[] arg = new String[0];
        return Integer.parseInt(command("getAnswerSize", arg)[0]);
    }

    public String getAskFieldName(int index) {
        String[] arg = { String.valueOf(index) };
        return command("getAskFieldName", arg)[0];
    }

    public int getAskSize() {
        String[] arg = new String[0];
        return Integer.parseInt(command("getAskSize", arg)[0]);
    }

    public synchronized void outputDefinition(OutputStream out){
        sockout.println("outputDefinition");
        String input;
        boolean first = true;
        try {
            while ((input = sockin.readLine()) != null) {
                if(first){
                    first=false;
                    if(input.substring(0, 3).equals("ER\t")){
                        throw new NeuralException("Server error: " + input.substring(3));
                    }
                }
                out.write(input.getBytes());
            }
            out.flush();
        } catch (IOException ex) {
            throw new NeuralException("Unable to get definition - i/o error", ex);
        }
    }

    public synchronized void outputTranslatorDefinition(OutputStream out){
        sockout.println("outputTranslatorDefinition");
        String input;
        boolean first = true;
        try {
            while ((input = sockin.readLine()) != null) {
                if(first){
                    first=false;
                    if(input.substring(0, 3).equals("ER\t")){
                        throw new NeuralException("Server error: " + input.substring(3));
                    }
                }
                out.write(input.getBytes());
            }
            out.flush();
        } catch (IOException ex) {
            throw new NeuralException("Unable to get translator definition - i/o error", ex);
        }
    }

    public synchronized void disconnect(){
        try {
            command("quit", new String[0]);
        } catch (Exception ex) {
        }

        try {
            this.sockin.close();
            this.sockout.close();
            this.socket.close();
            socket=null;
        } catch (IOException ex) {
            throw new NeuralException("Coudn't close a connection to " + host + ':' + port, ex);
        }
    }

    private synchronized String[] command(String com, String[] arg){
        sockout.print(com);
        sockout.println(tabArr(arg));
        String ret = null;
        try {
            ret = sockin.readLine();
        } catch (IOException ex) {
            throw new NeuralException("Socket read error:", ex);
        }
        if(ret==null){
            return null;
        }
        String[] r = ret.split(this.separator,2);
        if(r[0].equals("OK"))
            try{
                return r[1].split(this.separator);
            }
        catch(ArrayIndexOutOfBoundsException e){
            return new String[0];
        }
        else
            throw new NeuralException("Server error: " + r[1]);
    }

    private String tabArr(String[] s){
        StringBuffer st = new StringBuffer();
        for(int i=0;i<s.length;i++){
            st.append(this.separator);
            st.append(s[i]);
        }
        return st.toString();
    }

    public void nextEpoch() {
        command("nextEpoch", new String[0]);
        this.processEvent(new NeuralEvent(this, NeuralEvent.EPOCHCHANGED));
    }

    protected void finalize(){
        this.disconnect();
    }


    // ******************************
    // wotking with local copy of the remote network
    // ******************************
    private synchronized NeuralNetworkLocal getNN(){
        if(this.timeloaded<System.currentTimeMillis() || localNN==null){
            this.timeloaded = System.currentTimeMillis() + this.RELOADAFTERMILLIS;
            localNN = new NeuralNetworkLocal();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            this.outputDefinition(out);
            InputStream in = new ByteArrayInputStream(out.toByteArray());
            XmlFileOrigin.initNeuralNetworkByInput(localNN, in);
        }
        return localNN;
    }

    public Neuron[] getNeurons() {
        return this.getNN().getNeurons();
    }

    public WeightHolder[] getNeuronsAndSynapses() {
        return this.getNN().getNeuronsAndSynapses();
    }

    public WeightHolder getNeuronOrSynapse(String name) {
        return this.getNN().getNeuronOrSynapse(name);
    }

    public Teacher getTeacher() {
        return this.getNN().getTeacher();
    }

    public Neuron[] getInputNeurons() {
        return this.getNN().getInputNeurons();
    }

    public Translator getTranslator() {
        return this.getNN().getTranslator();
    }

    public Neuron[] getOutputNeurons() {
        return this.getNN().getOutputNeurons();
    }

    public int getInputIndex(Neuron neuron) {
        return this.getNN().getInputIndex(neuron);
    }

    public int getOutputIndex(Neuron neuron) {
        return this.getNN().getOutputIndex(neuron);
    }

    public int getNeuronSynapseIndex(WeightHolder neuronOrSynapse) {
        return this.getNN().getNeuronSynapseIndex(neuronOrSynapse);
    }

}
