package com.weighscore.neuro.plugins;


import com.weighscore.neuro.*;

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
public class EmpiricTeacher extends Teacher {
    public double momentumCoefficient = 0.2;
    public double learnRate = 0.4;

    private int allJoggedCount=0;

    /**
     * teach
     *
     * @param error double[]
     * @param signal Signal
     * @return boolean
     */
    public boolean teach(double[] error, Signal signal) {
        double[][] correction = signal.goBack(error);

        NeuralNetworkLocal na = super.getNeuralNetwork();
        Neuron[] n = na.getNeurons();
        Neuron[] outs = na.getOutputNeurons();
        //signal.getGradient();

        // подгонка скорости обучения
        double avgLastAvgErrDev = 0;
        if(((LastErrorStatistic)outs[0].getMultiStats().getStatistic("LastErrorStatistic")).areLastArraysFull()){
            for(int i=0; i<outs.length; i++){
                // средний avgErrDev
                avgLastAvgErrDev = avgLastAvgErrDev +
                                   ((LastErrorStatistic)outs[0].getMultiStats().getStatistic("LastErrorStatistic")).lastAvgErrDev/
                                   outs.length;
            }
            double mul = Math.exp(Math.abs(avgLastAvgErrDev)*-1000);
            //System.out.print("mul:"+mul+"\t");
            //if(mul<0.001)
                this.learnRate = this.learnRate - this.learnRate*0.001 * mul;
                this.momentumCoefficient = this.learnRate/4;
            //System.out.print("LR:"+this.learnRate+"\t");
        }


        // изменение - отрицательный градиент умножить на скорость обучения
        double mom = 0.0;
        for(int i=0; i<correction.length; i++){
            Synapse[] insyns = n[i].getInSynapses();
            for(int j = 0; j<correction[i].length; j++){
                //mom = ((MomentumStatistic) n[i].getMultiStats().getStatistic("MomentumStatistic")).lastCorrection *
                //          this.momentumCoefficient;
                if(j==0)
                    mom = ((MomentumStatistic) n[i].getMultiStats().
                           getStatistic("MomentumStatistic"))
                          .lastCorrection * this.momentumCoefficient;
                else{
                    mom = ((MomentumStatistic) insyns[j-1].getMultiStats().
                           getStatistic("MomentumStatistic"))
                          .lastCorrection * this.momentumCoefficient;
                }
                correction[i][j] = correction[i][j] * -1.0 * this.learnRate + mom;
            }
        }

        // обучение
        for (int i = 0; i < na.getNeurons().length; i++) {
            //n[i].teach(correction[i]);
            na.teachNeuron(n[i], correction[i]);
        }

        //  ***********************************************************
        // заливка дырок
        if (this.learnRate < 0.001) {
            int stopped=0, envolved=0;
            com.weighscore.neuro.WeightHolder[] wh = na.getNeuronsAndSynapses();
            boolean[] fill = new boolean[wh.length];
            double maxLae = 0.0;
            for (int i = 0; i < wh.length; i++) {
                LastErrorStatistic les;
                try{
                    les = (LastErrorStatistic) wh[i].getMultiStats().getStatistic("LastErrorStatistic");
                } catch (ClassCastException e) {
                    throw new NeuralException(
                         "Statistic error: EmpiricTeacher needs LastErrorStatistic or it's descent");
                }
                double lae = les.lastAvgErrAbs;
                if(wh[i].getMultiStats().getLongParameter("teachCnt")>0){// .teachCnt>0){
                    envolved++;
                    if(lae<=this.learnRate){
                        stopped++;
                        fill[i] = false;
                    }
                    else{
                        fill[i]=true;
                    }
                    if(lae>maxLae){
                        maxLae=lae;
                    }
                }
            }
            envolved--;
            //System.out.print("st:"+stopped+",en:"+envolved+"\t");
            if(maxLae>0.0)
                this.learnRate = maxLae/4;
            else
                this.learnRate = 0.5;
            this.momentumCoefficient = this.learnRate/4;
            if(stopped==envolved){
                for (int i = 0; i < wh.length; i++) {
                    //wh[i].initWeight();
                    na.initWeightOrThreshold(wh[i]);
                }
                na.processEvent(new NeuralEvent(this, NeuralEvent.GENERIC, "All weights reinitted, learn rate set to " + this.learnRate));
                //System.out.println("\nAll weights reinitted, learn rate set to " + this.learnRate);
            }
            else{
                //double floodfill = Math.random() * 2 * this.learnRate - this.learnRate;
                double floodfill = Math.random() * this.learnRate / 2;
                for (int i = 0; i < wh.length; i++) {
                    if(fill[i])
                        //wh[i].setWeight(wh[i].getWeight() + floodfill);
                        na.setWeightOrThreshold(wh[i], wh[i].getWeight() + floodfill);
                }
                na.processEvent(new NeuralEvent(this, NeuralEvent.GENERIC, "Some weights jogged by " + floodfill + ", learn rate set to " + this.learnRate));
                //System.out.println("\nSome weights jogged by " + floodfill + ", learn rate set to " + this.learnRate);
            }
        }

        return true;
    }

    public String[] getNeededStatisticNames(){
        String[] s = new String[2];
        s[0] = "LastErrorStatistic";
        s[1] = "MomentumStatistic";
        return s;
    }
}
