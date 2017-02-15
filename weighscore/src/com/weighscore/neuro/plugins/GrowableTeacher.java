package com.weighscore.neuro.plugins;

import com.weighscore.neuro.*;
import com.weighscore.neuro.plugins.SpecialCasesStatistic.SpecialCase;

public class GrowableTeacher extends SimpleTeacher {
    public int specialCaseDetectionMode = 0;
    public int teachEpochsTotal = 3;
    public int teachEpochsCurrent = 0;

    private boolean initted = false;

    public boolean teach(double[] error, Signal signal) {
        if(!initted){
            initted=true;
            Neuron[] n = this.getNeuralNetwork().getOutputNeurons();
            for(int i=0;i<n.length;i++){
                n[i].getMultiStats().addStatistic("EpochErrorGradientStatistic");
            }
        }

        if(this.specialCaseDetectionMode==0)
            super.teach(error, signal);
        else{
            // back propagate the error to find neurons and synapses errors
            double[][] gradient = signal.goBack(error);

            if((this.specialCaseDetectionMode==2)){
                // call recordError for all neurons with SpecialCases stats
                Neuron[] n = this.getNeuralNetwork().getNeurons();
                for (int i = 0; i < n.length; i++) {
                    SpecialCasesStatistic scs;
                    MultiStats ms = n[i].getMultiStats();
                    if (ms.hasStatistic("SpecialCasesStatistic"))
                        scs = (SpecialCasesStatistic) n[i].getMultiStats().getStatistic(
                                "SpecialCasesStatistic");
                    else
                        continue;
                    scs.recordGradient(gradient[i], error);
                }
            }

        }

        return true;
    }

    public boolean nextEpoch(){
        super.nextEpoch();

        if(this.specialCaseDetectionMode==0){

            // detect if teaching process stopped
            NeuralNetworkLocal nn = this.getNeuralNetwork();
            Neuron[] outneurons = nn.getOutputNeurons();
            double outneuronscorrections = 0;
            for (int i = 0; i < outneurons.length; i++) {
                EpochErrorGradientStatistic eegs =
                        (EpochErrorGradientStatistic) outneurons[i].getMultiStats().
                        getStatistic("EpochErrorGradientStatistic");
                double outneuronscorr = eegs.lastEpochAvgCorrection;
                        //outneurons[i].getMultiStats().
                        //getDoubleParameter("EpochErrorGradientStatistic",
                        //                   "lastEpochAvgCorrection");
                outneuronscorrections = outneuronscorrections +
                                        outneuronscorr / outneurons.length;
            }
            // if corrections -> 0 then teaching stopped.
            // start a special cases detecting mode
            if(this.teachEpochsCurrent>=this.teachEpochsTotal &&
               outneuronscorrections<=this.learnRate/10){
                this.specialCaseDetectionMode = 1;
                teachEpochsCurrent = 0;
                // *********************************************
                System.out.println("\n+++ SWITCH TO ACCUMULATING GRADIENT STATS MODE");
            }
            else
                this.teachEpochsCurrent++;
        }
        else if(this.specialCaseDetectionMode==1){
            this.specialCaseDetectionMode = 2;
            // *********************************************
            System.out.println("\n+++ SWITCH TO SPECIAL CASE DETECTION MODE");
        }
        else if(this.specialCaseDetectionMode==2){
            this.specialCaseDetectionMode=0;
            System.out.println("\n+++ STOP SPECCASE DETECTING");
            // we passed one epoch detecting special cases.
            // now we should create a neuron to cover the most errorful
            // special case



            // *********************************************

            NeuralNetworkLocal nn = this.getNeuralNetwork();
            Neuron[] n = nn.getNeurons();
            for(int i=0;i<n.length;i++){
                SpecialCasesStatistic scs;
                MultiStats ms = n[i].getMultiStats();
                if(ms.hasStatistic("SpecialCasesStatistic"))
                    scs = (SpecialCasesStatistic) n[i].getMultiStats().getStatistic("SpecialCasesStatistic");
                else
                    continue;
                SpecialCasesStatistic.SpecialCase sc = scs.specialCaseSet.getMostMeaningfulSpecialCase();
                System.out.println("**********");
                System.out.println("Most Meaningful ("+sc.getQuantity()+"):");
                System.out.println(sc.toString());

                /*sc = scs.specialCaseSet.getMostOftenCase();
                System.out.println("**********");
                System.out.println("Most Often ("+sc.getQuantity()+"):");
                System.out.println(sc.toString());


                System.out.println("**********");
                System.out.println("All ("+ scs.specialCaseSet.getSize() +")");
                System.out.println(scs.specialCaseSet.toString());
                System.out.println("**********");*/

                // clone the neuron
                // ****************
                // create
                Neuron newn = nn.addNeuron();
                // set the threshold - speccaseGrad
                // SIGMOID
                //nn.setWeightOrThreshold(newn, n[i].getWeight() - sc.getAverageGradient()[0]);
                //nn.setWeightOrThreshold(newn, (double)sc.getMeaningSynapsesNumber()-0.7);
                nn.setWeightOrThreshold(newn, Math.abs(sc.getAverageNeuronGradient()) * ((double)sc.getMeaningSynapsesNumber() - 0.5));


                // copy all out synapses
                Synapse[] ns = n[i].getOutSynapses();
                for(int j=0;j<ns.length;j++){
                    // copy synapse
                    Synapse news = nn.addSynapse(newn, ns[j].getOutNeuron());
                    // SIGMOID
                    // set the weight to 1
                    //nn.setWeightOrThreshold(news, 1.0 / sc.getQuantity());
                    nn.setWeightOrThreshold(news, ns[j].getWeight() * (sc.getAverageNeuronGradient()>0?1.0:-1.0));
                                            //sc.getAverageNeuronGradient());

                    // copy statistics
                    MultiStats oldms = news.getMultiStats();
                    String oldsn[] = oldms.getStatisticNames();
                    for (int k = 0; k < oldsn.length; k++) {
                        if (!oldsn[k].equals("SpecialCasesStatistic")) {
                            // copy statistic
                            news.getMultiStats().addStatistic(oldsn[k]);
                        }
                    }
                }

                // create needed input synapses
                ns = n[i].getInSynapses();
                for(int j=0;j<ns.length;j++){
                    // copy synapse
                    Synapse news = nn.addSynapse(ns[j].getInNeuron(), newn);
                    // SIGMOID
                    // set the weight - speccaseGradient
                    //nn.setWeightOrThreshold(news, ns[j].getWeight() - sc.getAverageGradient()[j+1]);
                    //nn.setWeightOrThreshold(news, sc.getSynapseGradientSign(j));
                    if (sc.isMeaningSynapse(j)) {
                        nn.setWeightOrThreshold(news,
                                                Math.abs(sc.
                                getAverageNeuronGradient()));
                    } else {
                        //nn.setWeightOrThreshold(news, 0);
                        nn.setWeightOrThreshold(news,
                                                -1.0 * Math.abs(sc.
                                getAverageNeuronGradient()));
                    }
                    // copy statistics
                    MultiStats oldms = news.getMultiStats();
                    String oldsn[] = oldms.getStatisticNames();
                    for (int k = 0; k < oldsn.length; k++) {
                        if (!oldsn[k].equals("SpecialCasesStatistic")) {
                            // copy statistic
                            news.getMultiStats().addStatistic(oldsn[k]);
                        }
                    }
                }

                // SIGMOID
                // copy activation
                //Activation newa, olda;
                //olda = n[i].getActivation();
                //try {
                //    newa = (Activation) olda.getClass().newInstance();
                //    nn.setActivation(newn, newa);
                //} catch (Exception ex) {
                //    throw new RuntimeException(ex);
                //}
                //// copy activation parameters
                //String[]params = olda.getParameterNames();
                //for(int j=0;j<params.length;j++){
                //    newa.setParameter(params[j], olda.getParameterAsString(params[j]));
                //}
                nn.setActivation(newn, new Sigmoid());
                ((Sigmoid)newn.getActivation()).bipolar=0;
                ((Sigmoid)newn.getActivation()).beta=1;

                // copy statistics
                MultiStats oldms = n[i].getMultiStats();
                String oldsn[] = oldms.getStatisticNames();
                for(int k=0; k<oldsn.length;k++){
                    if(!oldsn[k].equals("SpecialCasesStatistic")){
                        // copy statistic
                        newn.getMultiStats().addStatistic(oldsn[k]);
                    }
                }
                System.out.println("\n+++ CREATED NEURON " + newn.getName());
            }

            //System.exit(0);

        }
        return true;
    }
}
