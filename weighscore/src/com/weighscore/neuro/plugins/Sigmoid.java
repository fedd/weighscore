package com.weighscore.neuro.plugins;

import com.weighscore.neuro.*;

/**
 * This class that defines sigmoid actiavation function
 *
 * @author Fyodor Kravchenko
 * @version 1.0
 */
public class Sigmoid extends Activation {
    public double beta = 1;
    public int bipolar = 0;

    public Sigmoid() {
    }

    public double execute(double x) {
        double y;
        double e_x = Math.exp(beta * x * -1.0);
        if(bipolar==1){
            double ex = Math.exp(beta*x);
            y = (ex - e_x) / (ex + e_x);
        }
        else{
            y = 1.0 / (1.0 + e_x);
        }
        return y;
    }

    public double inverseExecute(double fx) {
        if(bipolar!=1)
            throw new RuntimeException("Unipolar Sigmoid inverse function is not yet implemented");

        double x;

        if(fx>=1)
            x=Double.MAX_VALUE;
        else if(fx<=-1)
            x=Double.MAX_VALUE*-1;
        else{
            double div;
            div = (1 + fx) / (1 - fx);
            x = 0.5 * Math.log(div);
            x = x / beta;
        }
        return x;
    }

    public double differentialExecute(double fx) {
        if(bipolar==1)
            return beta*(1-fx*fx);
        else
            return beta * fx * (1 - beta * fx);
    }
}
