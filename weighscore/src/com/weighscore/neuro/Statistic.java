package com.weighscore.neuro;

import java.lang.ref.WeakReference;

/**
 * The base class for holding and computing statistics.
 * The descent class may define statistic parameters as public class fields,
 * and redefine record methods to work with parameters.
 * Parameters become automatically available to the neural system by
 * ParameterHolder methods.
 *
 *
 * @author Fyodor Kravchenko
 * @version 1.0
 */
public abstract class Statistic extends ParameterHolder {
    private WeakReference whoseWR;
    protected WeightHolder getWhose(){
        return (WeightHolder) this.whoseWR.get();
    }
    protected void setWhose(WeightHolder neuronOrSynapse){
        this.whoseWR = new WeakReference(neuronOrSynapse);
    }

    /**
     * This method is called in Neuron when back propagating error.
     * Neuron passes it's error to it's statistic and Synapses' errors to their
     * statistics
     *
     * @param error error value
     */
    public abstract void recordError(double error);
    /**
     * This method is called in Neuron when back propagating error.
     * Neuron passes gradient to it's statistic and Synapses' gradients to their
     * statistics
     *
     * @param gradient gradient value
     */
    public void recordGradient(double gradient){};

    /**
     * This method is called when performing asking to neuron action
     *
     * @param question question value
     */
    public abstract void recordAsking(double question);

    /**
     * This method is called when performing teaching neuron action
     *
     * @param correction the value of weight change
     */
    public abstract void recordTeaching(double correction);

    /**
     * This method is called by the teacher when teacher's nextEpoch() method is called
     */
    public void nextEpoch(){}
}
