package com.weighscore.neuro.plugins;

import com.weighscore.neuro.*;

/**
 * The class for holding and computing momentum statistic
 *
 * @author Fyodor Kravchenko
 * @version 1.0
 */
public class MomentumStatistic extends Statistic {

    /**
     * The last change value of the weight
     */
    public double lastCorrection;
    /**
     * The teaching signals counter
     */
    public long teachCnt;

    public double volatility = 1;

    public void recordAsking(double question) {
    }

    public void recordError(double error) {
    }

    /**
     * Updates the last correction and teach signal counter statistics
     *
     * @param correction the change value of the weight
     */
    public synchronized void recordTeaching(double correction) {
        this.teachCnt++;
        this.lastCorrection=correction;
    }
}
