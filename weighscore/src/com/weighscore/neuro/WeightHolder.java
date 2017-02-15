package com.weighscore.neuro;

/**
 * <p>Title: MetNeuroScore</p>
 *
 * <p>Description: Neural scoring subsystem</p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: Vsetech</p>
 *
 * @author Fedd Kraft
 * @version 0.1
 */
public abstract class WeightHolder {
    public abstract double getWeight();
    protected abstract void setWeight(double weight);
    protected abstract void initWeight();
    public abstract String getName();
    public abstract NeuralNetwork getNeuralNetwork();

    // статистика сигналов
    protected MultiStats multistats = new MultiStats(this);
    public MultiStats getMultiStats(){
        return this.multistats;
    }
    protected int whIndex = -1;
}
