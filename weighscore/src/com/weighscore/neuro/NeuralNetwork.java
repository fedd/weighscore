package com.weighscore.neuro;

import com.weighscore.neuro.plugins.*;

public abstract class NeuralNetwork /*extends NeuralEventGenerator*/ implements
        NeuralNetworkGeneral {
    /*NeuralEvent askEvent = new NeuralEvent(this, NeuralEvent.ASKED);
    NeuralEvent testEvent = new NeuralEvent(this, NeuralEvent.TESTED);
    NeuralEvent teachEvent = new NeuralEvent(this, NeuralEvent.TAUGHT);
    NeuralEvent nextEpochEvent = new NeuralEvent(this, NeuralEvent.EPOCHCHANGED);
    NeuralEvent savedEvent = new NeuralEvent(this, NeuralEvent.SAVED);*/

    public static final String DEFAULTNETWORKNAME = "NeuralNetwork.nn";

    public static NeuralNetwork getNeuralNetwork(String name){
        try{
            return NeuralNetworkFactory.getNeuralNetworkFactory().getNeuralNetwork(name);
        }catch(NoClassDefFoundError e){
            XmlFileOrigin xfo = new XmlFileOrigin();
            NeuralNetworkLocal nn = new NeuralNetworkLocal();
            nn.setName(DEFAULTNETWORKNAME);
            xfo.initNeuralNetwork(nn, DEFAULTNETWORKNAME);
            return nn;
        }
    }
    public static NeuralNetwork createNeuralNetwork(String name){
        try{
            return NeuralNetworkFactory.getNeuralNetworkFactory().createNeuralNetwork(name, false);
        }catch(NoClassDefFoundError e){
            NeuralNetworkLocal nn = new NeuralNetworkLocal();
            nn.setName(DEFAULTNETWORKNAME);
            return nn;
        }
    }
    public NeuralNetworkLocal getCopy(String newName, boolean overwrite){
        try{
            return NeuralNetworkFactory.getNeuralNetworkFactory().getNeuralNetworkCopy(this, newName, overwrite);
        }catch(NoClassDefFoundError e){
            throw new NeuralException("Can not save the neural network with a new name");
        }
    }


    private NeuralEventListener nel = null;
    public void setNeuralEventListener(NeuralEventListener neuralEventListener){
        this.nel = neuralEventListener;
    }
    public void processEvent(NeuralEvent neuralEvent){
        if(this.nel!=null)
            nel.neuralEventHappened(neuralEvent);
    }
    public NeuralEventListener getNeuralEventListener(){
        return nel;
    }

}
