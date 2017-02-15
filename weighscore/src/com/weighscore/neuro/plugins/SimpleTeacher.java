package com.weighscore.neuro.plugins;


import com.weighscore.neuro.*;

/**
 * The simple teacher with momentum and constant learn rate
 *
 * @author Fyodor Kravchenko
 * @version 1.0
 */
public class SimpleTeacher extends Teacher {
    /**
     * Momentum coefficient - a multiplier to the last weight changes
     */
    public double momentumCoefficient = 0.2;
    /**
     * Learn rate - a multiplier to the computed teaching values
     */
    public double learnRate = 0.4;

    /**
     * Compute gradient members, compute the weights corrections using the learn
     * rate, the correction of previous iteration and the momentum
     *
     * @param error double[]
     * @param signal The Signal object holding the querying status and the
     *   gradient members
     * @return Always true
     */
    public boolean teach(double[] error, Signal signal) {
        // get gradient which is an array of arrays
        double[][] correction = signal.goBack(error);

        NeuralNetworkLocal na = super.getNeuralNetwork();
        // get all network's neurons
        Neuron[] n = na.getNeurons();

        double mom = 0.0;
        double vol = 1.0;
        MomentumStatistic momstat;
        // iterate neurons
        for(int i=0; i<correction.length; i++){
            // iterate neuron's weights (threshold and input synapses)
            Synapse[] insyns = n[i].getInSynapses();
            for(int j = 0; j<correction[i].length; j++){
                // the previous weights change is kept in the neuron's or synapse's statistic
                // object of the MomentumStatistic class
                if(j==0){
                    momstat = ((MomentumStatistic) n[i].getMultiStats().
                           getStatistic("MomentumStatistic"));
                    mom = momstat.lastCorrection * this.momentumCoefficient;
                    vol = momstat.volatility;
                }
                else{
                    momstat = ((MomentumStatistic) insyns[j-1].getMultiStats().
                           getStatistic("MomentumStatistic"));
                    mom = momstat.lastCorrection * this.momentumCoefficient;
                    vol = momstat.volatility;
                }
                // correction - the negative gradient multiplied on learn rate
                // volatitity - a coefficient to mark mature neurons and synapses
                correction[i][j] = (correction[i][j] * -1.0 * this.learnRate * vol + mom);// * vol;

                //System.out.print(correction[i][j] + " ");
            }

            // ************************************** test
            /*if(i==54){
                System.out.print("\t" + Util.prtArr(correction[i], true));
            }*/
        }

        // record the correction - teaching
        for (int i = 0; i < na.getNeurons().length; i++) {
            // calling neuron's teach method to update weights
            // ensures that the statistic will be updated
            na.teachNeuron(n[i], correction[i]);
            //n[i].teach(correction[i]);
        }

        return true;
    }
}
