package com.weighscore.neuro;

import java.lang.ref.WeakReference;
/**
 * This is an abstract class that defines the behavior of activation functions
 * of neurons.
 *
 * @author Fyodor Kravchenko
 * @version 1.0
 */
public abstract class Activation extends ParameterHolder {
    /**
     * Method of function execution. Should be implemented by subclass,
     * defining the particular function
     *
     * @param x function's input value
     * @return function's output value
     */
    public abstract double execute(double x);

    /**
     * Method for inverse function execution. Should be implemented by subclass,
     * defining the particular inverse function
     *
     * @param fx inverse function input value
     * @return inverse function output value
     */
    public abstract double inverseExecute(double fx);

    /**
     * The differential function of this actiavation function. Takes the result
     * of this activation function as a parameter
     *
     * @param fx the function output value
     * @return differential
     */
    public abstract double differentialExecute(double fx);

    /**
     * This method is called from the network processing routines. By default,
     * it calls execute method. This method can be overriden if the activation
     * function needs to be aware of the current status of activation of other
     * network's neurons. This can be used to implement winner-takes-all neurons
     *
     * @param x function's input value
     * @param signal the status of activation of other neurons
     * @return function's output value
     * @throws NoResultYetException if the neuron should not be acivated at
     *   this iteration because of the status of activation of other neurons
     */
    public double execute (double x, Signal signal) throws com.weighscore.neuro.NoResultYetException{
        return this.execute(x);
    }

    /**
     * This method is called from the network processing routines. By default,
     * it calls differentialExecute method. This method can be overriden if the
     * activation function needs to be aware of the current status of activation
     * of other network's neurons.
     *
     * @param fx the function output value
     * @param signal the signal object to get the status of activation of other
     * neurons
     * @return differential
     * @throws NoResultYetException if the neuron should wait at this iteration
     *   because of the status of computing differentials of other neurons
     */
    public double differentialExecute(double fx, Signal signal) throws com.weighscore.neuro.NoResultYetException{
        return this.differentialExecute(fx);
    }

    public void setParameter(String parameterName, String parameter){
        super.setParameter(parameterName, parameter);
        Neuron wh = this.getNeuron();
        if(wh!=null)
            wh.getNeuralNetwork().processEvent(new NeuralEvent(wh, NeuralEvent.WIGHTHOLDERPARAMETERMODIFIED));
    }


    private WeakReference nWR=null;
    protected void setNeuron(Neuron neuron){
        this.nWR=new WeakReference(neuron);
    }

    /**
     * The method returns the neuron which this activation belongs to
     *
     * @return this activation's neuron
     */
    public Neuron getNeuron(){
        if(nWR==null)
            return null;
        return (Neuron) this.nWR.get();
    }
}
