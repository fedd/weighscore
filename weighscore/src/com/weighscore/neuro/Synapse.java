package com.weighscore.neuro;

//import org.apache.log4j.Logger;
import java.lang.ref.WeakReference;

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

public class Synapse extends WeightHolder {

    private int indexAsIn;
    protected int indexAsOut;
    protected void setIndexAsIn(int index){
        this.indexAsIn=index;
        this.myName="";
    }
    public int getIndexAsIn(){
        return this.indexAsIn;
    }
    public int getIndexAsOut(){
        return this.indexAsOut;
    }

    //private static Logger log = Logger.getLogger(Synapse.class);

    // статистика сигналов
    //protected Statistic statistic;


    protected Synapse(Neuron inNeuron, Neuron outNeuron) {
        //log.trace("start create synapse");
        this.inNeuronWR= new WeakReference(inNeuron);
        this.outNeuronWR=new WeakReference(outNeuron);
        this.initWeight();
        //this.statistic = new FullStatistic(this, inNeuron.getNeuralNetwork().lastErrCnt);
        /*try {
            this.setStatistic((Statistic) inNeuron.getStatistic().getClass().
                              newInstance());
        }
        catch(Exception ex){
            throw new NeuralException("Couldn't set statistic for synapse");
        }*/
        //log.debug("created synapse");

        String[] sts = this.getInNeuron().getNeuralNetwork().getTeacher().getNeededStatisticNames();
        for (int i = 0; i < sts.length; i++) {
            this.getMultiStats().addStatistic(sts[i]);
        }
    }

    //private Neuron inNeuron;
    private WeakReference inNeuronWR;
    public Neuron getInNeuron(){
        return (Neuron) inNeuronWR.get();
    }
    //private Neuron outNeuron;
    private WeakReference outNeuronWR;
    public Neuron getOutNeuron(){
        return (Neuron) this.outNeuronWR.get();
    }

    protected double oldweight;
    protected double weight;
    protected synchronized void setWeight(double weight){
        this.weight = weight;
        this.oldweight = weight;
        this.getNeuralNetwork().processEvent(new NeuralEvent(this, NeuralEvent.WEIGHTMODIFIED));
        //log.trace("weight set");
    }
    protected void initWeight(){
        this.weight=Math.random() * 2.0 - 1.0;
        this.oldweight = this.weight;
        this.getNeuralNetwork().processEvent(new NeuralEvent(this, NeuralEvent.WEIGHTMODIFIED));
        //log.trace("weight initted");
    }
    public double getWeight(){
        return this.weight;
    }

    protected double weigh(double question){
        //log.trace("weigh method called with question " + question);
        double tryanswer;

        // регистрируем статистику
        this.multistats.recordAsking(question);

        tryanswer = question * weight;

        //log.trace("weigh method will return " + tryanswer + " (weight - " +weight+ ")" );

        return tryanswer;
    }

    /*public Statistic getStatistic() {
        return this.statistic;
    }

    protected void setStatistic(Statistic statistic) {
        if(statistic!=null){
            this.statistic = statistic;

            this.statistic.setWhose(this);
        }
    }*/
    String myName="";
    public String getName(){
        if(myName.length()==0){
            myName="S-" + (this.getOutNeuron().getIndex()+1) + "-" + (this.getIndexAsIn()+1);
        }
        return this.myName;
    }

    public NeuralNetwork getNeuralNetwork() {
        return this.getInNeuron().getNeuralNetwork();
    };
}
