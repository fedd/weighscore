package com.weighscore.neuro;

import java.util.*;
import sun.misc.*;
import java.io.*;
import com.weighscore.neuro.plugins.*;

public class NeuralNetworkFactory {
    private static String NEURALCONFIG = "neural.config";

    private static NeuralNetworkFactory naf=null;

    private sun.misc.SoftCache nas = new SoftCache();// named Neural networks
    //private sun.misc.SoftCache translators = new SoftCache();// translators
    private Properties properties;// neural.config
    private Origin origin=null;

    public Properties getProperties(){
        return this.properties;
    }
    public void saveProperties(){
        try{
            FileOutputStream fos = new FileOutputStream(NEURALCONFIG, false);
            this.properties.store(fos, null);
        }
        catch(Exception e){
            throw new NeuralException("Couldn't store properties in " + NEURALCONFIG, e);
        }
    }

    private NeuralNetworkFactory(){
        Properties defaults = new Properties();
        defaults.setProperty("neuralNetwork.origin", Util.getPluginNameForClass(XmlFileOrigin.class));// "com.weighscore.neuro.plugins.XmlFileOrigin");
        defaults.setProperty("neuralNetwork.origin.xml.path", "./");
        defaults.setProperty("neuralNetwork.origin.xml.extension", ".nn");

        this.properties = new Properties(defaults);
        try{
            FileInputStream fis = new FileInputStream(NEURALCONFIG);
            this.properties.load(fis);
        }
        catch(Exception e){
            System.err.println(NEURALCONFIG + "file not found, apply defaults");
        }

        String or = this.properties.getProperty("neuralNetwork.origin");
        try {
            this.origin = (Origin) Util.getPluginClassForName(or).newInstance();
            this.origin.setProperties(this.properties);
        } catch (Exception ex) {
            throw new NeuralException("Couldn't set up a neural network origin " + or, ex);
        }
    }

    public static NeuralNetworkFactory getNeuralNetworkFactory(){
        if(naf==null){
            naf = new NeuralNetworkFactory();
        }
        return naf;
    }

    protected synchronized NeuralNetwork getNeuralNetwork(String name){
        if(name==null)
            throw new NeuralException("No neural network name specified");
        NeuralNetwork na = (NeuralNetwork)nas.get(name);
        if(na==null){
            // decide wheter take the local network or remote
            if(name.startsWith("neuro:")){
                na = new NeuralNetworkRemote();
                NeuralNetworkRemote nnr = (NeuralNetworkRemote)na;
                nnr.setName(name);
                nnr.connect();
                nas.put(nnr.getName(), nnr);
            }
            else{
                na = this.createNeuralNetwork(name, false);
                try{
                    this.origin.initNeuralNetwork((NeuralNetworkLocal)na, name);
                }catch(NeuralException e){
                    ((NeuralNetworkLocal)na).setName(null);
                    nas.remove(name);
                    throw new NeuralException("Could not get local neural network " + name + ": " + e.getMessage(), e);
                }
            }
        }
        return na;
    }

    protected synchronized NeuralNetwork createNeuralNetwork(String name, boolean overwrite){
        if(!overwrite && nas.containsKey(name)){
            throw new NeuralNetworkCreationException("The NeuralNetwork with the name \"" + name +
                                       "\" already exists in this factory");
        }
        if(name.startsWith("neuro:")){
            throw new NeuralNetworkCreationException("Can not create neural network on the server");
        }
        NeuralNetworkLocal na = new NeuralNetworkLocal();
        na.setName(name);
        nas.put(name, na);
        return na;
    }

    /*protected synchronized NeuralNetwork getNeuralNetwork(String name, boolean create){
        try{
            return (NeuralNetwork) getNeuralNetwork(name);
        }
        catch(NeuralException e){
            if(create)
                return createNeuralNetwork(name);
            else
                throw e;
        }
    }*/

    protected synchronized void saveNeuralNetwork(NeuralNetworkLocal nn){
        if(nn.getName()!=null && nn.getName().length()!=0)
            this.origin.saveNeuralNetwork(nn);
    }

    protected synchronized NeuralNetworkLocal getNeuralNetworkCopy(NeuralNetwork nn, String newName, boolean overwrite){
        if (!overwrite && this.origin.doesExist(newName)){
            throw new NeuralNetworkCreationException("The network "+ newName + "already exists");
        }
        try{
            NeuralNetworkLocal newn = (NeuralNetworkLocal)this.createNeuralNetwork(newName, overwrite);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            XmlFileOrigin.outputNeuralNetwork(nn, out);
            InputStream in = null;
            in = new ByteArrayInputStream(out.toByteArray());
            XmlFileOrigin.initNeuralNetworkByInput(newn, in);
            return newn;
        }
        catch(Exception e){
            this.nas.remove(newName);
            throw new NeuralNetworkCreationException("Couldn't copy the network", e);
        }
    }

    /*protected synchronized NeuralNetworkAbstract saveNeuralNetworkAs(NeuralNetworkAbstract na, String newName){
        if(newName.startsWith("neuro:")){
            throw new NeuralException("Can not save neural network on the server");
        }
        if(newName!=null && newName.length()!=0){
            this.origin.saveNeuralNetworkAs((NeuralNetwork)na, newName);
            NeuralNetworkAbstract newn = this.createNeuralNetwork(newName);
            return newn;
        }
        else
            throw new NeuralException("No new name specified");
    }*/

    public synchronized void saveAllNetworks(){
        Collection s = nas.values();
        Iterator i = s.iterator();
        while(i.hasNext()){
            Object o=null;
            try{
                o = i.next();
                NeuralNetworkLocal nn = (NeuralNetworkLocal) o;
                nn.save();
            }
            catch(NoClassDefFoundError e){
            }
            catch(ClassCastException e){
            }
        }
    }

    public String[] getNeuralNetworkNames(){
        return this.origin.getNeuralNetworkNames();
    }
}
