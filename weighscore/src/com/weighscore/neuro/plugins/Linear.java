package com.weighscore.neuro.plugins;

import com.weighscore.neuro.*;

/**
 * This class that defines linear actiavation function
 *
 * @author Fyodor Kravchenko
 * @version 1.0
 */
public class Linear extends Activation {
    public double beta = 1;

    public Linear() {
    }

    public double execute(double x) {
        return beta*x;
    }

    public double inverseExecute(double fx) {
        return fx/beta;
    }

    public double differentialExecute(double fx){
        return beta;
    }
}
