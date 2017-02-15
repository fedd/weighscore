package com.weighscore.neuro.plugins;

import com.weighscore.neuro.*;

public class EpochErrorGradientStatistic extends Statistic {

    public int lastEpochSize = 0;
    public double lastEpochAbsAvgError = Double.MAX_VALUE;
    public double lastEpochAbsAvgGradient = Double.MAX_VALUE;
    public double lastEpochAvgCorrection = Double.MAX_VALUE;
    public double lastEpochAbsAvgCorrection = Double.MAX_VALUE;

    private int curEpochSize = 0;
    private double curEpochCumulAbsError = 0;
    private double curEpochCumulAbsGradient = 0;
    private double curEpochCumulCorrection = 0;
    private double curEpochCumulAbsCorrection = 0;


    public void recordAsking(double question) {}

    public void recordError(double error) {
        this.curEpochSize++;
        this.curEpochCumulAbsError = this.curEpochCumulAbsError + Math.abs(error);
    }

    public void recordGradient(double gradient) {
        this.curEpochCumulAbsGradient = this.curEpochCumulAbsGradient + Math.abs(gradient);
    }

    public void recordTeaching(double correction) {
        this.curEpochCumulCorrection = this.curEpochCumulCorrection + correction;
        this.curEpochCumulAbsCorrection = this.curEpochCumulAbsCorrection + Math.abs(correction);
    }

    public void nextEpoch(){
        this.lastEpochSize = this.curEpochSize;
        this.lastEpochAvgCorrection = this.curEpochCumulCorrection / this.curEpochSize;
        this.lastEpochAbsAvgCorrection = this.curEpochCumulAbsCorrection / this.curEpochSize;
        this.lastEpochAbsAvgError = this.curEpochCumulAbsError / this.curEpochSize;
        this.lastEpochAbsAvgGradient = this.curEpochCumulAbsGradient / this.curEpochSize;

        this.curEpochSize = 0;
        this.curEpochCumulAbsCorrection = 0;
        this.curEpochCumulCorrection = 0;
        this.curEpochCumulAbsError = 0;
        this.curEpochCumulAbsGradient = 0;
    }
}
