package com.weighscore.neuro;

import com.weighscore.neuro.plugins.*;

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

//import java.util.*;
//import org.w3c.dom.*;

public class Neuron extends WeightHolder {
    private static final Class DEFAULTACTIVATION = com.weighscore.neuro.plugins.Sigmoid.class;
    //private static final Class DEFAULTSTATISTIC = com.weighscore.neuro.plugins.LastErrorStatistic.class;

    //private static Logger log = Logger.getLogger(Neuron.class);


    protected boolean isInput=false, isOutput=false;
    protected int inputIndex = -1, outputIndex = -1;

    public int getInputIndex(){
        return this.neuralNetwork.getInputIndex(this);
    }
    public int getOutputIndex(){
        return this.neuralNetwork.getOutputIndex(this);
    }

    public boolean isInput(){
        return this.isInput;
    }
    public boolean isOutput(){
        return this.isOutput;
    }

/*    // статистика сигналов
    protected Statistic statistic;
    public Statistic getStatistic(){
        return this.statistic;
    }
    protected void setStatistic(Statistic statistic){
        if(statistic!=null){
            this.statistic = statistic;
            this.statistic.setWhose(this);
            for (int i = 0; i < this.getOutSynapses().length; i++) {
                this.getOutSynapses()[i].setStatistic(statistic);
            }
        }
    }*/

    // активизационная функция
    private Activation activation;
    protected void setActivation(Activation activationFunction){
        if(activationFunction!=null){
            //log.trace("set activation function");
            this.activation = activationFunction;
            this.activation.setNeuron(this);
        }
        this.neuralNetwork.processEvent(new NeuralEvent(this, NeuralEvent.WIGHTHOLDERPARAMETERMODIFIED));
    }
    public Activation getActivation(){
        //log.trace("get activation function");
        return this.activation;
    }




    protected Neuron(NeuralNetwork neuralNetwork) {
        //this.neuralNetworkWR = new WeakReference(neuralNetwork);
        this.neuralNetwork = neuralNetwork;

        /*try{
            this.setStatistic((Statistic) DEFAULTSTATISTIC.newInstance());
        }
        catch(Exception ex){
            throw new NeuralException("Couldn't set default statistic for neuron", ex);
        }*/

        try{
            this.setActivation((Activation)DEFAULTACTIVATION.newInstance());
        }
        catch(Exception ex){
            throw new NeuralException("Couldn't set default activation", ex);
        }

        String[] sts = this.neuralNetwork.getTeacher().getNeededStatisticNames();
        for(int i=0; i<sts.length;i++){
            this.getMultiStats().addStatistic(sts[i]);
        }
        this.initThreshold();
    }

    //private WeakReference neuralNetworkWR;
    private NeuralNetwork neuralNetwork = null;
    public NeuralNetwork getNeuralNetwork(){
        //return (NeuralNetwork) this.neuralNetworkWR.get();
        return this.neuralNetwork;
    }

    private int neuronIndex=-1;
    protected synchronized void setIndex(int index){
        this.neuronIndex=index;
        myName="";
    }
    public int getIndex(){
        return this.neuronIndex;
    }

    private Synapse[] inSynapses = new Synapse[0];
    private Synapse[] outSynapses = new Synapse[0];
    public Synapse[] getInSynapses(){
        return this.inSynapses;
    }
    public Synapse[] getOutSynapses(){
        return this.outSynapses;
    }

    private synchronized void addInSynapse(Synapse inSynapse){
        Synapse[] newIns = new Synapse[this.inSynapses.length+1];
        System.arraycopy(this.inSynapses,0,newIns,0,this.inSynapses.length);
        inSynapse.setIndexAsIn(newIns.length-1);
        newIns[this.inSynapses.length] = inSynapse;
        this.inSynapses = newIns;
    }
    protected synchronized Synapse addOutSynapseTo(Neuron outNeuron){
        // можно открыть в паблик
        Synapse[] newOuts = new Synapse[this.outSynapses.length+1];
        System.arraycopy(this.outSynapses,0,newOuts,0,this.outSynapses.length);
        Synapse newOut = new Synapse(this, outNeuron);
        newOut.indexAsOut=this.outSynapses.length;
        newOuts[this.outSynapses.length] = newOut;
        this.outSynapses = newOuts;
        outNeuron.addInSynapse(newOut);

        //*********************************************
        //this.getNeuralNetwork().weightHolders = null;
        //*********************************************

        return newOut;
    }

    // ************** REMOVING
    /*protected Synapse getOutSynapseTo(Neuron outNeuron){
        // перебором
        for(int i=0; i<this.outSynapses.length; i++){
            if(this.outSynapses[i].getOutNeuron() == outNeuron)
                return this.outSynapses[i];
        }
        return null;
    }*/
    private synchronized void removeInSynapse(Synapse inSynapse, boolean proceedForIn){
        int removed = inSynapse.getIndexAsIn();
        if(removed!=-1){
            inSynapse.setIndexAsIn(-1);
            Synapse[] newIns = new Synapse[this.inSynapses.length - 1];
            System.arraycopy(this.inSynapses, 0, newIns, 0, removed);
            System.arraycopy(this.inSynapses, removed + 1, newIns, removed,
                             this.inSynapses.length - (removed + 1));
            for (int i = 0; i < newIns.length; i++) {
                newIns[i].setIndexAsIn(i);
            }
            this.inSynapses = newIns;

            if (proceedForIn)
                inSynapse.getInNeuron().removeOutSynapse(inSynapse, false);
        }
        else
            throw new RuntimeException("Tried to delete the deleted");
    }
    private synchronized void removeOutSynapse(Synapse outSynapse, boolean proceedForOut){
        int removed = outSynapse.indexAsOut;
        if(removed!=-1){
            outSynapse.indexAsOut=-1;
            Synapse[] newOuts = new Synapse[this.outSynapses.length - 1];
            System.arraycopy(this.outSynapses, 0, newOuts, 0, removed);
            System.arraycopy(this.outSynapses, removed + 1, newOuts, removed,
                             this.outSynapses.length - (removed + 1));
            for (int i = 0; i < newOuts.length; i++) {
                newOuts[i].indexAsOut = i;
            }
            this.outSynapses = newOuts;

            if (proceedForOut)
                outSynapse.getOutNeuron().removeInSynapse(outSynapse, false);
        }
    }
    protected void removeOutSynapse(Synapse outSynapse){
        this.removeOutSynapse(outSynapse, true);
    }
    /*public synchronized void removeOutSynapseTo(Neuron outNeuron){
        Synapse outSynapse = this.getOutSynapseTo(outNeuron);
        if (outSynapse == null)
            throw new NeuralException(
                    "The synapse to remove does not belong to this neuron");
        this.removeOutSynapse(outSynapse, true);
    }*/

    protected synchronized void removeAllSynapses(){
        Synapse[] ss = new Synapse[this.inSynapses.length];
        System.arraycopy(this.inSynapses,0,ss,0,ss.length);
        for(int i=0;i<ss.length;i++){
            this.removeInSynapse(ss[i], true);
        }
        ss = new Synapse[this.outSynapses.length];
        System.arraycopy(this.outSynapses,0,ss,0,ss.length);
        for(int i=0;i<ss.length;i++){
            this.removeOutSynapse(ss[i], true);
        }
    }
    // ************** end REMOVING

    private double threshold;
    protected void setThreshold(double threshold){
        //log.trace("set threshold");
        this.threshold=threshold;
        this.neuralNetwork.processEvent(new NeuralEvent(this, NeuralEvent.WEIGHTMODIFIED));
    }
    protected void initThreshold(){
        //log.trace("init threshold");
        this.threshold=Math.random() * 2.0 - 1.0;
        this.neuralNetwork.processEvent(new NeuralEvent(this, NeuralEvent.WEIGHTMODIFIED));
    }
    protected double getThreshold(){
        //log.trace("get threshold");
        return this.threshold;
    }


    protected double ask(double[] question, Signal sig) throws NoResultYetException {
        //log.trace("start ask question - " + Util.prtArr(question));
        if (question.length!=this.inSynapses.length){
            throw new NeuralException("The neuron " + this.neuronIndex + " received a malformed question");
        }

        // MINUS
        //double signal=this.threshold;
        double signal=this.threshold * -1.0;

        for(int i=0; i<question.length; i++){
            signal=signal + this.inSynapses[i].weigh(question[i]);
        }
        //log.trace("computed signal before act func - " + signal);

        // регистрируем статистику
        //log.trace("register asking statistic");
        this.multistats.recordAsking(signal);

        double retsignal = this.activation.execute(signal, sig);
        //log.trace("computed signal after act func - " + retsignal);

        //log.debug("got answer - " + retsignal);

        // ************************************** test
        /*if(this.neuronIndex>=42 && this.neuronIndex<=54){
            System.out.print("\t"+this.neuronIndex+":\t" + retsignal);
        }*/
        /*if(this.neuronIndex>=42 && this.neuronIndex<=54){
            //System.out.print("\t"+this.neuronIndex+":r\t" + retsignal + "\t" + this.neuronIndex+"q:\t" + Util.prtArr(question, false) +"\t"+this.neuronIndex+":w\t" + this.getInSynapses()[0].getWeight() + "\t" +this.neuronIndex+":th\t" + this.getThreshold() + "\t" );
            System.out.print("\t"+this.neuronIndex+":\tmax:\t" + this.getInSynapses()[0].getWeight() / this.getThreshold() + "\tq:\t" + question[0] +"\tr\t"+ retsignal + "\t" );

        }*/
        if(this.neuronIndex==54){
            StringBuffer prt = new StringBuffer();
            for(int i=0;i<this.inSynapses.length;i++){
                prt.append('\t');
                prt.append(this.inSynapses[i].getName());
                prt.append('\t');
                prt.append(sig.getAnswer(this.inSynapses[i].getInNeuron()));
                prt.append('\t');
                prt.append(this.inSynapses[i].weight);
            }
            System.out.print(prt.toString() +  "\t"+this.neuronIndex+"sig:\t" + retsignal);
        }



        return retsignal;
    }

    protected double[] test (double question[], double/*[]*/ error, double answer, Signal sig) throws NoResultYetException {
        //log.trace("start testing");
        //log.trace("compute error from " + error);

        double de =  this.activation.differentialExecute(answer, sig);
        //log.trace("computed differential from answer - " + de);

        double ret = de * error;
        //double ret = err;
        //log.trace("computed error * differential - " + ret);

        // регистрируем статистику ошибки
        //log.trace("register error statistic - " + error);
        this.multistats.recordError(error);

        Synapse[] insyns = this.getInSynapses();
        // градиент со стр 59
        //log.trace("compute gradient components");
        double grad[] = new double[insyns.length+1];
        // MINUS
        grad[0] = -1.0 * ret;

        this.multistats.recordGradient(grad[0]);

        //grad[0] = ret;
        //log.trace("gradient component of threshold = " + grad[0]);
        for (int i = 1; i < grad.length; i++) {
            int ii = i-1;
            grad[i] = question[ii] * ret;
            insyns[ii].multistats.recordGradient(grad[i]);

            insyns[ii].multistats.recordError(question[ii]*error);
        }

        //log.debug("tested - " + ret);
        return grad;
    }

    protected void teach(double[] correction){
        Synapse[] insyns = this.getInSynapses();
        // обучение синапсов
        int ii=0;
        for (int i = 0; i < insyns.length; i++) {
            ii++;
            insyns[i].oldweight = insyns[i].weight;
            insyns[i].multistats.recordTeaching(correction[ii]);

            insyns[i].weight = insyns[i].weight + correction[ii];
        }
        multistats.recordTeaching(correction[0]);

        // MINUS (moved to test)
        threshold = threshold + correction[0];
        //threshold = threshold - correction[0];
    }

    public double getWeight() {
        return this.getThreshold();
    }

    protected void setWeight(double weight) {
        this.setThreshold(weight);
    }

    protected void initWeight() {
        this.initThreshold();
    }

    private String myName="";
    public String getName(){
        if(myName.length()==0){
            myName="N-" + (this.getIndex()+1);
        }
        return this.myName;
    };
}
