package com.weighscore.neuro.plugins;

import com.weighscore.neuro.*;
import java.util.*;
import com.weighscore.neuro.plugins.SpecialCasesStatistic.SpecialCase;

public class SpecialCasesStatistic extends Statistic {
    public SpecialCaseSet specialCaseSet = new SpecialCaseSet();

    private boolean initialized = false;
    private EpochErrorGradientStatistic myAvgGrad;
    //private EpochErrorGradientStatistic[] mySynAvgGrads;

    public void recordAsking(double question) {}
    public void recordError(double error) {
        if(this.initialized)
            return;

        // here we provide EpochErrorStatistic to this neuron
        // and to all its input synapses
        Neuron n;
        try{
            n = (Neuron)this.getWhose();
        }
        catch(ClassCastException e){
            throw new NeuralException("SpecialCasesStatistic cannot be a statistic of a synapse. See " + this.getWhose().getName());
        }
        if(n.isOutput())
            throw new NeuralException("SpecialCasesStatistic cannot be a statistic of an output neuron. See " + this.getWhose().getName());

        myAvgGrad = (EpochErrorGradientStatistic) n.getMultiStats().addStatistic("EpochErrorGradientStatistic");
        Synapse[] s = n.getInSynapses();
        //mySynAvgGrads = new EpochErrorGradientStatistic[s.length];
        for(int i=0;i<s.length;i++){
            //mySynAvgGrads[i] =
                    //(EpochErrorGradientStatistic)
                    s[i].getMultiStats().
                    addStatistic("EpochErrorGradientStatistic");
        }
        this.initialized=true;
    }

    public void recordTeaching(double correction) {}

    protected void recordGradient(double[] gradient, double[] error) {
        // called only from Growable teacher
        GrowableTeacher gt;
        try{
            gt = (GrowableTeacher)this.getWhose().getNeuralNetwork().getTeacher();
        }
        catch(ClassCastException e){
            return;
        }
        if(gt.specialCaseDetectionMode!=2)
            return;


        // record a special case
        // if this is a special case - if a gradient is bigger then average gradient
        //**************************** testtttt!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
         //if(Math.abs(gradient[0]) > this.myAvgGrad.lastEpochAbsAvgGradient)
        {
            this.specialCaseSet.registerSpecialCase(gradient, error);
        }
    }

    public void setParameter(String parameterName, String parameter){
        if(parameterName.equals("specialCaseSet")){
            try{
                Neuron n = (Neuron)this.getWhose();
                this.specialCaseSet = new SpecialCaseSet(n, parameter);
            }
            catch(ClassCastException e){}
        }
        else
            super.setParameter(parameterName, parameter);
    }


    protected class SpecialCase{
        private char[] caseSynapses;
        private long quantity = 0;
        private double cumulGrad = 0;
        private double[] cumulSynapseGrads;
        private Neuron neuron;

        private double[] cachedAvgGrad=null;
        private int meaningSynapsesNumber=0;;

        protected SpecialCase(char[] caseSynapses){
            this.neuron=(Neuron) getWhose();
            if(caseSynapses.length != neuron.getInSynapses().length)
                throw new RuntimeException("SpecialCase synapses quantity doesnt match to neuron input synapses");
            this.caseSynapses=new char[caseSynapses.length];
            System.arraycopy(caseSynapses, 0, this.caseSynapses, 0, caseSynapses.length);

            this.cumulSynapseGrads = new double[caseSynapses.length];
        }

        protected SpecialCase(Neuron neuron, String caseIdentification){
            this.neuron=neuron;
            String[] dd = caseIdentification.trim().split("\\s+");
            //this.quantity=Long.parseLong(dd[1]);
            this.quantity=Long.parseLong(dd[0]);
            //cumulGrad = Double.parseDouble(dd[0]);
            cumulGrad = Double.parseDouble(dd[1]) * this.quantity;

            cumulSynapseGrads = new double[dd.length-2];
            caseSynapses = new char[cumulSynapseGrads.length];
            for(int i=2;i<dd.length;i++){
                int ii=i-2;
                cumulSynapseGrads[ii] = Double.parseDouble(dd[i]) * this.quantity;
                if(cumulSynapseGrads[ii]==0.0)
                    caseSynapses[ii] = '0';
                /*else if(cumulSynapseGrads[ii]>0)
                    caseSynapses[ii] = '+';
                else
                    caseSynapses[ii] = '-';*/
                else
                    caseSynapses[ii] = '1';
            }

        }

        protected synchronized void registerAbsGradient(double[] gradient){
            if(gradient.length-1!=cumulSynapseGrads.length)
                throw new RuntimeException("SpecialCase synapses quantity doesnt match to synapses errors quantity provided");

            for(int i=1; i<gradient.length; i++){
                int ii = i-1;
                if(caseSynapses[ii]!='0')
                    cumulSynapseGrads[ii] = cumulSynapseGrads[ii] + gradient[i];// ********* the sign must be the same - we checked it in CaseSet - this not true when '1' used to indicate speccase synapse
            }
            this.cumulGrad=this.cumulGrad + gradient[0];

            this.quantity++;

            cachedAvgGrad = null;
            meaningSynapsesNumber = 0;
        }

        protected boolean isMeaningSynapse(int inSynapseNumber){
            return caseSynapses[inSynapseNumber]!='0';
        }

        /*protected byte getSynapseGradientSign(int inSynapseNumber){
            if(caseSynapses[inSynapseNumber]=='+')
                return 1;
            else if(caseSynapses[inSynapseNumber]=='-')
                return -1;
            else return 0;
        }*/

        protected long getMeaningSynapsesNumber(){
            if(meaningSynapsesNumber==0){
                for(int i=0;i<caseSynapses.length;i++){
                    if(isMeaningSynapse(i))
                        meaningSynapsesNumber++;
                }
            }
            return meaningSynapsesNumber;
        }



        protected Neuron getNeuron(){
            return this.neuron;
        }

        protected char[] getCaseId(){
            return this.caseSynapses;
        }

        protected long getQuantity(){
            return this.quantity;
        }

        protected double getAverageNeuronGradient(){
            return cumulGrad / (double) quantity;
        }
        protected double getCumulativeNeuronGradient(){
            return cumulGrad;
        }
        /*protected double[] getAverageSynapsesGradients(){
            double[] ret = new double[this.caseSynapses.length];
            for(int i = 0; i<ret.length; i++){
                ret[i] = cumulSynapseGrads[i] / (double) quantity;
            }
            return ret;
        }*/


        protected synchronized double[] getAverageGradient(){
            if(cachedAvgGrad==null){
                double[] ret = new double[this.caseSynapses.length + 1];
                ret[0] = cumulGrad / (double) quantity;
                for (int i = 1; i < ret.length; i++) {
                    ret[i] = cumulSynapseGrads[i - 1] / (double) quantity;
                }
                cachedAvgGrad = ret;
            }
            return cachedAvgGrad;
        }

        /*protected double getAverageSynapsesGradient(){
            double ret = 0;
            double[] e = this.getAverageSynapsesGradients();
            for(int i = 0; i<e.length; i++){
                ret = ret + e[i] / (double) e.length;
            }
            return ret;
        }*/

        public String toString(){
            //StringBuffer sb = new StringBuffer(Double.toString(this.getAverageNeuronGradient()));
            StringBuffer sb = new StringBuffer();
            //sb.append(' ');
            sb.append(quantity);
            //double[] bb = this.getAverageSynapsesGradients();
            double[] bb = this.getAverageGradient();
            for(int i=0;i<bb.length;i++){
                sb.append(' ');
                sb.append(bb[i]);
            }
            String ret = sb.toString();
            return ret;
        }

    }
    protected class SpecialCaseSet{
        //private Neuron neuron;
        //private Synapse[] synapses;

        private Hashtable set = new Hashtable(); // string +00+--00 - SpecialCase

        public SpecialCaseSet(Neuron neuron, String caseIdentifications){
            String[] ss = caseIdentifications.trim().split(";");
            for(int i=0;i<ss.length;i++){
                SpecialCase sc = new SpecialCase(neuron, ss[i]);
                set.put(sc.getCaseId(), sc);
            }
        }

        protected SpecialCaseSet(){
        }

        protected void clear(){
            set.clear();
        }
        protected int getSize(){
            return set.size();
        }

        protected SpecialCase getMostMeaningfulSpecialCase(){
            SpecialCase maxerrsc = null;

            Enumeration en = set.elements();
            while(en.hasMoreElements()){
                SpecialCase sc = (SpecialCase) en.nextElement();
                if(maxerrsc==null ||
                   //sc.getAverageNeuronGradient() > maxerrsc.getAverageNeuronGradient()
                   Math.abs(sc.getCumulativeNeuronGradient()) > Math.abs(maxerrsc.getCumulativeNeuronGradient())
                   //sc.getQuantity() > maxerrsc.getQuantity()
                   )

                    maxerrsc = sc;
            }
            return maxerrsc;
        }

        protected SpecialCase getMostOftenCase(){
            SpecialCase maxerrsc = null;

            Enumeration en = set.elements();
            while(en.hasMoreElements()){
                SpecialCase sc = (SpecialCase) en.nextElement();
                if(maxerrsc==null ||
                   //sc.getAverageNeuronAbsGradient() > maxerrsc.getAverageNeuronAbsGradient()
                   sc.getQuantity() > maxerrsc.getQuantity()
                   )

                    maxerrsc = sc;
            }
            return maxerrsc;
        }

        public String toString(){
            StringBuffer sb = new StringBuffer();

            Enumeration en = set.elements();
            boolean first = true;
            while(en.hasMoreElements()){
                if(first){
                    first = false;
                }
                else{
                        sb.append(';');
                        //sb.append('\n');
                }
                SpecialCase sc = (SpecialCase) en.nextElement();
                sb.append(sc.toString());
            }

            return sb.toString();
        }

        protected void registerSpecialCase(double[] gradient, double[] error){
            SpecialCase sc;

            Synapse[]synapses = ((Neuron)getWhose()).getInSynapses();

            // find input synapses where average gradient is less then this
            char[] caseid = new char[synapses.length];
            for(int i=0;i<caseid.length;i++){
                double grad = gradient[i+1];
                EpochErrorGradientStatistic st = (EpochErrorGradientStatistic)
                                                 synapses[i].getMultiStats().
                                                 getStatistic("EpochErrorGradientStatistic");
                double absavggrad = st.lastEpochAbsAvgGradient;

                if(Math.abs(grad) > absavggrad){
                    /*if(grad>0)
                        caseid[i]='+';
                    else
                        caseid[i]='-';*/
                    caseid[i]='1';
                }
                else
                    caseid[i]='0';
            }

            //find if we already registered this case
            String casestr = new String(caseid);
            sc = (SpecialCase) set.get(casestr);
            if(sc == null){
                // create
                sc = new SpecialCase(caseid);
                set.put(casestr, sc);
            }

            // register the case
            sc.registerAbsGradient(gradient);


            if (casestr.equals("1000101100110000000000101000100000010011111110000")) {
               System.out.print("\tSc:\t" + gradient[0]);
               System.out.print("\ter:\t" + error[0]);
            }
            else{
                System.out.print("\t\t");
                System.out.print("\t\t");
            }
            /*if(casestr.equals("+000+0++00++0000000000+0+000+000000+00++0000+0+0+")){
                System.out.println("\nthat found:\t" + Util.prtArr(gradient, true));
                System.out.println("\nerror:\t" + Util.prtArr(error, true));
            }*/
            /*if (casestr.equals("+000+0++00++0000000000+0+000+000000+00++0000+0+0+")) {
               System.out.print("\tSc:\t" + gradient[0]);
               System.out.print("\ter:\t" + error[0]);
            }*/
        }
    }
}

















