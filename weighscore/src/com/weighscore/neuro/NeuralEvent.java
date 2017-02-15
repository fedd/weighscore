package com.weighscore.neuro;

import java.util.EventObject;

public class NeuralEvent extends EventObject {
    public static final int GENERIC = 0;

    public static final int SIGNALPROCEEDEDFORTH = -1;
    public static final int SIGNALPROCEEDEDBACK = -2;

    public static final int ASKED = 1;
    public static final int TESTED = 2;
    public static final int TAUGHT = 3;

    public static final int EPOCHCHANGED = 100;
    public static final int SAVED = 200;

    public static final int TOPOLOGYMODIFIED = -100;
    public static final int WEIGHTMODIFIED = -101;
    public static final int WIGHTHOLDERPARAMETERMODIFIED = -102;


    private int eventType = GENERIC;
    private Signal sig;
    private String message;

    public NeuralEvent(NeuralNetwork neuralNetwork, int eventType){
        super(neuralNetwork);
        this.eventType = eventType;
        this.sig=null;
        this.message=null;
    }
    public NeuralEvent(Signal signal, int eventType){
        super(signal);
        this.eventType = eventType;
        this.sig=null;
        this.message=null;
    }
    public NeuralEvent(WeightHolder neuronOrSynapse, int eventType){
        super(neuronOrSynapse);
        this.eventType = eventType;
        this.sig=null;
        this.message=null;
    }
    public NeuralEvent(NeuralNetwork neuralNetwork, Signal signal, int eventType){
        super(neuralNetwork);
        this.eventType = eventType;
        this.sig=signal;
        this.message=null;
    }
    public NeuralEvent(WeightHolder neuronOrSynapse, Signal signal, int eventType){
        super(neuronOrSynapse);
        this.eventType = eventType;
        this.sig=signal;
        this.message=null;
    }
    public NeuralEvent(NeuralNetwork neuralNetwork, int eventType, String message){
        super(neuralNetwork);
        this.eventType = eventType;
        this.sig=null;
        this.message=message;
    }
    public NeuralEvent(Teacher teacher, int eventType, String message){
        super(teacher);
        this.eventType = eventType;
        this.sig=null;
        this.message=message;
    }
    public NeuralEvent(WeightHolder neuronOrSynapse, int eventType, String message){
        super(neuronOrSynapse);
        this.eventType = eventType;
        this.sig=null;
        this.message=message;
    }
    public NeuralEvent(NeuralNetwork neuralNetwork, Signal signal, int eventType, String message){
        super(neuralNetwork);
        this.eventType = eventType;
        this.sig=signal;
        this.message=message;
    }
    public NeuralEvent(WeightHolder neuronOrSynapse, Signal signal, int eventType, String message){
        super(neuronOrSynapse);
        this.eventType = eventType;
        this.sig=signal;
        this.message=message;
    }


    public int getEventType(){
        return this.eventType;
    }
    public Signal getSignal(){
        if(this.sig!=null)
            return this.sig;
        try{
            return (Signal)this.getSource();
        }
        catch(ClassCastException e){
            return null;
        }
    }
    public String getMeggage(){
        return this.message;
    }
}
