package com.weighscore.neuro.plugins;

import com.weighscore.neuro.*;

public class Threshold extends Activation {
    public double beta = 1;

    public Threshold() {
    }

    public double differentialExecute(double fx) {
        throw new RuntimeException("Never use this");
    }

    public double execute(double x) {
        return (x>=0?1:0);
    }

    public double inverseExecute(double fx) {
        throw new RuntimeException("Not implemented");
    }

    public double differentialExecute(double fx, Signal signal) throws com.weighscore.neuro.NoResultYetException{
        // вы€снить, какой бы была производна€, если бы функци€ была сигмоидальной

        // находим вход
        Neuron n = this.getNeuron();
        Synapse[] s = n.getInSynapses();
        double x = 0;
        for(int i=0;i<s.length;i++){
            Neuron in = s[i].getInNeuron();
            double answer = signal.getAnswer(in);
            x = x + answer * s[i].getWeight();
        }

        // находим дифференциал
        //     b * e ^ -bx
        //  ----------------
        //  (1 + e ^ -bx) ^2
        double eminusbetax = Math.exp(-1.0 * beta * x);
        double chis = beta * eminusbetax;
        double znam = Math.pow(1 + eminusbetax, 2);
        double diff = chis/znam;

        if(Double.isNaN(diff) || Double.isInfinite(diff)){
            throw new NeuralException("NaN or Infinite happened");
        }

        return diff;
    }
}
