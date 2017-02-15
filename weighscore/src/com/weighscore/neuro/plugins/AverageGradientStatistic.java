package com.weighscore.neuro.plugins;

import com.weighscore.neuro.*;

/**
 * This statistic is intended to be used in the future for conjugate gradient
 * teacher.
 * @author Fyodor Kravchenko
 * @version 1.0
 */
public class AverageGradientStatistic extends Statistic {

    /**
     * Counts the number of epochs (the whole case sets);
     */
    public long epochCasesCnt;
    /**
     * The average gradient member for the current epoch
     */
    public double epochCurAgvGradient;
    /**
     * The average gradient member for the previous epoch
     */
    public double epochLastAgvGradient;


    public synchronized void recordTeaching(double correction) {
        epochCurAgvGradient =
            (epochCurAgvGradient * epochCasesCnt + (-1*correction)) /
            (epochCasesCnt + 1);

        epochCasesCnt++;
    }

    public synchronized void nextEpoch(){
        this.epochLastAgvGradient = this.epochCurAgvGradient;
        this.epochCurAgvGradient = 0.0;
        this.epochCasesCnt = 0;
    }

    public void recordError(double error) {
    }

    public void recordAsking(double question) {
    }

}
