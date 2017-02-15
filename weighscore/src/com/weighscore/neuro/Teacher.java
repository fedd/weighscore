package com.weighscore.neuro;

import java.lang.ref.WeakReference;

/**
 * This class can should be extended to implement the specific teaching action.
 * The descent class may define teaching parameters as protected class fields,
 * and redefine record methods to work with parameters.
 * Parameters become automatically available to the neural system by
 * ParameterHolder methods.
 *
 * @author Fyodor Kravchenko
 * @version 2.0
 */
public abstract class Teacher extends com.weighscore.neuro.ParameterHolder {

    private WeakReference naWR;
    protected void setNeuralNetwork(NeuralNetworkLocal neuralNetwork){
        this.naWR = new WeakReference(neuralNetwork);
    }
    protected NeuralNetworkLocal getNeuralNetwork(){
        return (NeuralNetworkLocal) this.naWR.get();
    }

    /**
     * This method is called when the signal was forth and the error was back
     * propagated and the gradient members are known and kept in Signal object.
     * This method can compute the correction of neurons' weights. It can call
     * neurons' teach() method to change the weights
     *
     * @param error double[]
     * @param signal object that holds the values passed through the neurons
     *   and the values of back-propagated errors
     */
    public abstract boolean teach(double[] error, Signal signal);

    /**
     * This method is called from the teaching program (nnt) when the teaching
     * case set (epoch) reached its end. It should be overriden by teachers like
     * conjugate gradient teachers, that parform any action when the epoch
     * changes.
     */
    public boolean nextEpoch(){
        WeightHolder[] wh = this.getNeuralNetwork().getNeuronsAndSynapses();
        for (int i = 0; i < wh.length; i++) {
            //try {
                wh[i].getMultiStats().nextEpoch();
            //} catch (ClassCastException e) {
            //    throw new NeuralException(
            //            "Last average gradient unaware statistic used at " + i +
            //            ". Use AverageGradientStatistic or its children with ConjugateGradientTeacher");
            //}
        }
        return true;
    }

    /**
     * Normally the statistic objects appear automatically after the first
     * attempt of using it. If the teacher needs some statistics to be processed
     * before the first use (like some averages that need to start to be stat
     * computed beforehand), the teacher may override this method to return an
     * array of the needed statistic class names.
     *
     * @return An array of the statistic class names needed to be set to every
     *   neuron and synapse of the neural network
     */
    public String[] getNeededStatisticNames(){
        return new String[0];
    }
}
