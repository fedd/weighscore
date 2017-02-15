package com.weighscore.neuro.plugins;

/**
 * The class for holding and computing no statistics
 *
 * @author Fyodor Kravchenko
 * @version 1.0
 */
public class NoStatistic extends com.weighscore.neuro.Statistic {
    public void recordAsking(double question) {
    }
    public void recordError(double error) {
    }
    public void recordTeaching(double correction) {
    }
}
