package com.weighscore.neuro.plugins;

import com.weighscore.neuro.*;


/**
 * The class for holding and computing some average statistics that are used in
 * Empiric Teacher
 *
 * @author Fyodor Kravchenko
 * @version 1.0
 */

public class LastErrorStatistic extends Statistic {

    /**
     * The flag indicating that the last error arrays are full
     */
    private boolean lastArraysFull = false;
    /**
     * The last error position in the arrays
     */
    private int currErrCnt = 0;
    /**
     * The size of an array of the last errors of this synapse or neuron
     */
    public int lastErrCnt=100;
    /**
     * Last errors array
     */
    private double[] lastErrs = new double[this.lastErrCnt];
    /**
     * The array of last differences between errors and last average errors
     */
    private double[] lastErrDevs = new double[this.lastErrCnt];


    /**
     * the average value of the last lastErrCnt errors of this neuron or synapse
     */
    public double lastAvgErr=0;
    /**
     * the average value of the last lastErrCnt absolute values of errors of
     * this neuron or synapse
     */
    public double lastAvgErrAbs=0;
    /**
     * the average difference between the last lastErrCnt errors and the last
     * lastErrCnt average errors
     */
    public double lastAvgErrDev=0;
    /**
     * the average absolute difference between the last lastErrCnt errors and
     * the last lastErrCnt average errors
     */
    public double lastAvgErrDevAbs=0;

    /**
     * Initialize the last average errors array
     */
    private synchronized void initLastAvgErr(){
        this.lastErrs = new double[this.lastErrCnt];
        for (int i = 0; i < lastErrs.length; i++) {
            this.lastErrs[i] = lastAvgErr;
        }

        this.lastArraysFull = false;
    }

    /**
     * Initialize the array of last differences between errors and last average
     * errors
     */
    private synchronized void initLastAvgErrDev(){
        this.lastErrDevs = new double[this.lastErrCnt];
        for (int i = 0; i < lastErrs.length; i++) {
            this.lastErrDevs[i] = lastAvgErrDev;
        }

        this.lastArraysFull = false;
    }


    /**
     * Tests whether the last error arrays are full
     *
     * @return whether the last error arrays are full
     */
    public boolean areLastArraysFull(){
        return this.lastArraysFull;
    }

    /**
     * Record the neuron's error, update all averages
     *
     * @param error The error to record
     */
    public synchronized void recordError(double error){
        currErrCnt++;
        if (currErrCnt>=lastErrs.length){ // loop back
            currErrCnt = 0;
            this.lastArraysFull=true;
        }

        double oldesterr=lastErrs[currErrCnt];
        double oldesterrdev = lastErrDevs[currErrCnt];

        lastErrs[currErrCnt] = error;
        // compute the new avearge
        this.lastAvgErr = this.lastAvgErr - (oldesterr/lastErrCnt) + (error/lastErrCnt);
        // compute the new absolute average
        this.lastAvgErrAbs = this.lastAvgErrAbs - (Math.abs(oldesterr)/lastErrCnt) + (Math.abs(error)/lastErrCnt);

        lastErrDevs[currErrCnt] = error - this.lastAvgErr;
        // new average deviation
        this.lastAvgErrDev = this.lastAvgErrDev - (oldesterrdev/lastErrCnt) + (lastErrDevs[currErrCnt]/lastErrCnt);
        this.lastAvgErrDevAbs = this.lastAvgErrDevAbs - (Math.abs(oldesterrdev)/lastErrCnt) + (Math.abs(lastErrDevs[currErrCnt])/lastErrCnt);
    }


    /**
     * Set the named statistic parameter of this statistic object; recalculate
     * averages
     *
     * @param statisticName The name of the statistic
     * @param statistic The new value of the statistic parameter
     */
    public synchronized void setParameter(String statisticName, String statistic){
        super.setParameter(statisticName, statistic);

        if(statisticName.equals("lastAvgErr")) {
            this.initLastAvgErr();
        }
        else if(statisticName.equals("lastAvgErrDev")) {
            this.initLastAvgErrDev();
        }
        else if(statisticName.equals("lastErrCnt")) {
            this.initLastAvgErrDev();
            this.initLastAvgErr();
        }
    }

    public void recordAsking(double question) {
    }

    public void recordTeaching(double correction) {
    }
}
