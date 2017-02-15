package com.weighscore.neuro;

import java.util.Properties;

/**
 * <p>Title: MetNeuroScore</p>
 *
 * <p>Description: Neural scoring subsystem</p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: Vsetech</p>
 *
 * @author Fedd Kraft
 * @version 0.1
 */
public abstract class Origin {
    Properties properties;
    protected void setProperties(Properties properties){
        this.properties = properties;
    }
    protected Properties getProperties(){
        return this.properties;
    }

    public abstract void initNeuralNetwork(NeuralNetworkLocal neuralNetwork, String sourceName);
    public abstract void saveNeuralNetwork(NeuralNetworkLocal neuralNetwork);
    public abstract String[] getNeuralNetworkNames();
    public abstract boolean doesExist(String sourceName);
}
