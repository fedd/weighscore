package com.weighscore.neuro;

import java.util.*;
import java.io.*;
import com.weighscore.neuro.plugins.*;

public class NeuralNetworkLocal extends NeuralNetwork implements NeuralNetworkEditable {

    private String name=null;

    private static final Class DEFAULTTEACHER = com.weighscore.neuro.plugins.SimpleTeacher.class;

    private Teacher teacher=null;
    private Translator translator=null;

    private WeightHolder[] weightHolders=null;
    private Neuron[] neurons = new Neuron[0];
    private Neuron[] inputNeurons = new Neuron[0];
    private Neuron[] outputNeurons = new Neuron[0];



    public Teacher getTeacher(){
        return this.teacher;
    }
    public void setTeacher(Teacher teacher){
        if(teacher!=null){
            this.teacher = teacher;
            this.teacher.setNeuralNetwork(this);

            // load all teacher's statistics
            String[] sts = this.teacher.getNeededStatisticNames();
            this.weightHolders=null;
            WeightHolder[] whs = this.getNeuronsAndSynapses();
            for(int j=0; j<sts.length;j++){
                for (int i = 0; i < whs.length; i++) {
                    whs[i].getMultiStats().addStatistic(sts[j]);
                }
            }
        }
    }


    protected NeuralNetworkLocal() {
        try {
            setTeacher((Teacher) DEFAULTTEACHER.newInstance());
        } catch (Exception ex) {
            throw new NeuralException("Couldn't initialize default teacher for neural area", ex);
        }
    }

    public String getName(){
        return name;
    }
    public void setName(String name){
        this.name = name;
    }

    public Neuron[] getNeurons(){
        return this.neurons;
    }
    public Neuron[] getInputNeurons(){
        return this.inputNeurons;
    }
    public Neuron[] getOutputNeurons(){
        return this.outputNeurons;
    }

    public Neuron addNeuron(){
        synchronized(this.teacher){
            Neuron[] ns = new Neuron[this.neurons.length + 1];
            System.arraycopy(this.neurons, 0, ns, 0, this.neurons.length);
            Neuron newn = new Neuron(this);
            newn.setIndex(this.neurons.length);
            ns[this.neurons.length] = newn;
            this.neurons = ns;
            this.weightHolders = null;//this.getNeuronsAndSynapses();//this.weightHolders = null;
            this.processEvent(new NeuralEvent(this,
                                              NeuralEvent.TOPOLOGYMODIFIED));
            return newn;
        }
    }

    public void removeNeuron(Neuron neuron){
        synchronized(this.teacher){
            if (neuron.getNeuralNetwork() != this)
                throw new NeuralException(
                        "The input neuron provided is of another Neural Network");
            try {
                this.unMarkAsInput(neuron);
            } catch (NeuralException e) {}
            try {
                this.unMarkAsOutput(neuron);
            } catch (NeuralException e) {}

            neuron.removeAllSynapses();

            Neuron[] ns = new Neuron[this.neurons.length - 1];

            int removed = neuron.getIndex();
            if (removed != -1) {
                neuron.setIndex( -1);
                System.arraycopy(this.neurons, 0, ns, 0, removed);
                System.arraycopy(this.neurons, removed + 1, ns, removed,
                                 this.neurons.length - (removed + 1));
                for (int i = 0; i < ns.length; i++) {
                    ns[i].setIndex(i);
                }

                this.neurons = ns;
                this.weightHolders = null;//this.getNeuronsAndSynapses(); // this.weightHolders = null;
                this.processEvent(new NeuralEvent(this,
                                                  NeuralEvent.TOPOLOGYMODIFIED));
            } else
                throw new RuntimeException("Deleting the deleted neuron");
        }
    }

    public int getInputIndex(Neuron neuron){
        if(neuron.isInput()){
            synchronized(neuron){
                if (neuron.inputIndex == -1) {
                    for (int i = 0; i < this.inputNeurons.length; i++) {
                        if (this.inputNeurons[i] == neuron) {
                            neuron.inputIndex = i + 1;
                            break;
                        }
                    }
                }
                return neuron.inputIndex;
            }
        }
        else
            return -1;
    }
    public int getOutputIndex(Neuron neuron){
        if(neuron.isOutput()){
            synchronized(neuron){
                if (neuron.outputIndex == -1) {
                    for (int i = 0; i < this.outputNeurons.length; i++) {
                        if (this.outputNeurons[i] == neuron) {
                            neuron.outputIndex = i + 1;
                            break;
                        }
                    }
                }
            }
            return neuron.outputIndex;
        }
        else
            return -1;
    }

    public void markAsInput(Neuron neuron){
        synchronized(this.teacher){
            if (neuron.getNeuralNetwork() != this) {
                throw new NeuralException(
                        "The input neuron provided is of another Neural Network");
            }

            Neuron[] ns = new Neuron[this.inputNeurons.length + 1];
            System.arraycopy(this.inputNeurons, 0, ns, 0,
                             this.inputNeurons.length);
            Neuron newn = neuron;
            ns[this.inputNeurons.length] = newn;
            this.inputNeurons = ns;
            neuron.isInput = true;
            this.processEvent(new NeuralEvent(this,
                                              NeuralEvent.TOPOLOGYMODIFIED));
        }
    }
    public void unMarkAsInput(Neuron neuron){
        synchronized(this.teacher){
            if (neuron.getNeuralNetwork() != this)
                throw new NeuralException(
                        "The input neuron provided is of another Neural Network");
            if (!neuron.isInput)
                throw new NeuralException(
                        "The neuron provided is not an input neuron");

            Neuron[] ns = new Neuron[this.inputNeurons.length - 1];
            int ii = 0;
            for (int i = 0; i < this.inputNeurons.length; i++) {
                if (this.inputNeurons[i] == neuron) {
                    continue;
                }
                try {
                    ns[ii] = this.inputNeurons[i];
                } catch (ArrayIndexOutOfBoundsException e) {
                    throw new NeuralException(
                            "The neuron provided was not in the list of input neurons");
                }
                ii++;
            }
            this.inputNeurons = ns;
            neuron.isInput = false;
            this.processEvent(new NeuralEvent(this,
                                              NeuralEvent.TOPOLOGYMODIFIED));
        }
    }

    public void markAsOutput(Neuron neuron){
        synchronized(this.teacher){

            if (neuron.getNeuralNetwork() != this) {
                throw new NeuralException(
                        "The output neuron provided is of another Neural Network");
            }

            Neuron[] ns = new Neuron[this.outputNeurons.length + 1];
            System.arraycopy(this.outputNeurons, 0, ns, 0,
                             this.outputNeurons.length);
            Neuron newn = neuron;
            ns[this.outputNeurons.length] = newn;
            this.outputNeurons = ns;
            neuron.isOutput = true;
            this.processEvent(new NeuralEvent(this,
                                              NeuralEvent.TOPOLOGYMODIFIED));
        }
    }
    public void unMarkAsOutput(Neuron neuron){
        synchronized(this.teacher){

            if (neuron.getNeuralNetwork() != this)
                throw new NeuralException(
                        "The output neuron provided is of another Neural Network");
            if (!neuron.isOutput)
                throw new NeuralException(
                        "The neuron provided is not an output neuron");

            Neuron[] ns = new Neuron[this.outputNeurons.length - 1];
            int ii = 0;
            for (int i = 0; i < this.outputNeurons.length; i++) {
                if (this.outputNeurons[i] == neuron) {
                    continue;
                }
                try {
                    ns[ii] = this.outputNeurons[i];
                } catch (ArrayIndexOutOfBoundsException e) {
                    throw new NeuralException(
                            "The neuron provided was not in the list of ouput neurons");
                }
                ii++;
            }
            this.outputNeurons = ns;
            neuron.isOutput = false;
            this.processEvent(new NeuralEvent(this,
                                              NeuralEvent.TOPOLOGYMODIFIED));
        }
    }

    protected double[] ask(double[] question){
        Signal sig = Signal.getSignal(this);
        double[] ans = sig.go(question);
        this.processEvent(new NeuralEvent(this, sig, NeuralEvent.ASKED));
        return ans;
    }

    public String[] ask(String[] question){
        Translator tr = this.getTranslator();
        if(tr!=null){
            double[][] trinput = tr.translate(question);

            double[] answer = this.ask(trinput[0]);

            double[][] output = new double[2][];
            output[0] = trinput[0];
            output[1] = answer;
            String[] questionAnswer = tr.backTranslate(output);
            String[] answers = new String[questionAnswer.length -
                               getTranslator().askfields];
            try {
                System.arraycopy(questionAnswer, question.length, answers, 0,
                                 answers.length);
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new NeuralException(
                        "Too many parameters for translated ask");
            }
            return answers;
        }
        else{
            double[] q;
            try{
                q = Util.convertArr(question, 0);
            }
            catch(NeuralException e){
                throw new NeuralException("String ask parameters found; have no appropriate translator");
            }
            double[] a = this.ask(q);
            String[] s = Util.convertArr(a);
            return s;
        }
    }


    public double[] teach(String[] questionAnswer){
        return this.testTeach(true, questionAnswer);
    }
    public double[] test(String[] questionAnswer){
        return this.testTeach(false, questionAnswer);
    }
    protected double[] teach(double[] questionAnswer){
        return this.testTeach(true, questionAnswer);
    }
    protected double[] test(double[] questionAnswer){
        return this.testTeach(false, questionAnswer);
    }

    private double[] testTeach (boolean teach, String[] questionAnswer){
        Translator tr = this.getTranslator();
        if(tr!=null){

            //System.out.println("QA: " + Util.prtArr(questionAnswer, false));//**************

            double[][] trinput = tr.translate(questionAnswer);

            //  ************************
            //for(int i=0;i<trinput.length;i++)
            //    System.out.println("trinp" + i +": " + Util.prtArr(trinput[i], false));//**************

            double errcoeff[] = this.testTeach(teach, trinput[0], trinput[1]);

            //System.out.println("er: "+Util.prtArr(errcoeff, false));//**************

            String[] back = tr.backTranslate(trinput);

            //System.out.println("backtr: "+Util.prtArr(back, false));//**************

            for(int i=0; i<back.length; i++){
                try{
                    questionAnswer[i] = back[i];
                }
                catch (ArrayIndexOutOfBoundsException e){
                    throw new NeuralException("Answer not given for test/teach");
                }
            }
            return errcoeff;
        }
        else{
            double[] qa;
            try{
                qa = Util.convertArr(questionAnswer, 0);
            }
            catch(NeuralException e){
                throw new NeuralException("String test/teach parameters found; have no appropriate translator");
            }
            double[] errcoeff = this.testTeach(teach, qa);
            String[] s = Util.convertArr(qa);
            for(int i=0; i<s.length; i++){
                questionAnswer[i]=s[i];
            }
            return errcoeff;
        }
    }
    private double[] testTeach (boolean teach, double[] questionAnswer){
        double[]q = new double[this.inputNeurons.length];
        double[]a = new double[this.outputNeurons.length];
        if(q.length+a.length!=questionAnswer.length){
            if(q.length+a.length<questionAnswer.length){
                throw new NeuralException("Too many parameters for test/teach");
            }else{
                throw new NeuralException("Too few parameters for test/teach");
            }
        }
        System.arraycopy(questionAnswer, 0, q, 0, q.length);
        System.arraycopy(questionAnswer, q.length, a, 0, a.length);

        return this.testTeach(teach, q, a);
    }

    private double[] testTeach(boolean doTeach,
                                            double[] question,
                                            double[] answer){
        synchronized(this.teacher){

            Signal sig = Signal.getSignal(this);
            double[] tryanswer = sig.go(question);
            double errs[] = new double[answer.length];

            for (int i = 0; i < errs.length; i++) {
                errs[i] = tryanswer[i] - answer[i];
            }

            for (int i = 0; i < answer.length; i++) {
                answer[i] = tryanswer[i];
                if (Double.isNaN(tryanswer[i]) || Double.isInfinite(tryanswer[i])) {
                    throw new NeuralException(
                            "Overflow occurred while back propagating signal at neuron " +
                            i);
                }
            }

            if (doTeach) {
                //sig.goBack(errs); - перенес в тичера
                // собственно обучение
                this.teacher.teach(errs, sig);
                this.processEvent(new NeuralEvent(this, sig, NeuralEvent.TAUGHT));
            } else
                this.processEvent(new NeuralEvent(this, sig, NeuralEvent.TESTED));

            return errs;
        }
    }



    public synchronized WeightHolder[] getNeuronsAndSynapses(){
        if(this.weightHolders!=null){
            return this.weightHolders;
        }
        else{
            Vector sts = new Vector();
            // проход по нейронам
            for(int n = 0; n< this.neurons.length; n++){
                sts.add(this.neurons[n]);
                this.neurons[n].whIndex = sts.size()-1;
                // проход по синапсам
                Synapse[] synapses = neurons[n].getOutSynapses();
                for(int s=0; s<synapses.length; s++){
                    sts.add(synapses[s]);
                    synapses[s].whIndex = sts.size()-1;
                }
            }
            this.weightHolders = new WeightHolder[sts.size()];
            sts.toArray(this.weightHolders);
            return this.weightHolders;
        }
    }

    public synchronized int getNeuronSynapseIndex(WeightHolder neuronOrSynapse){
        /*if(neuronOrSynapse.whIndex==-1){
            this.weightHolders = null;
            this.getNeuronsAndSynapses();
        }*/
        return neuronOrSynapse.whIndex;
    }

    protected double getWeight(int index){
        return this.getNeuronsAndSynapses()[index].getWeight();
    }

    protected void setWeight(int index, double weight){
        this.getNeuronsAndSynapses()[index].setWeight(weight);
    }

    protected double[] getLastAbsoluteAverageErrors(){
        double[] laae = new double[this.outputNeurons.length];
        try{
            for (int i = 0; i < laae.length; i++) {
                laae[i] = (
                        (LastErrorStatistic)this.outputNeurons[i].getMultiStats().getStatistic("LastErrorStatistic")
                          ).lastAvgErrAbs;
            }
        }
        catch(ClassCastException e){
            throw new NeuralException("Can not get last average errors; please use LastErrorStatistic or it's descendant for all output neurons");
        }
        return laae;
    }

    public void setTranslator(Translator translator){
        this.translator=translator;
    }
    public Translator getTranslator(){
        return this.translator;
    }
    public String[] getFieldNames(){
        try{
            return this.getTranslator().getFieldNames();
        }catch(NullPointerException e){
            String[] s = new String[this.getInputNeurons().length+this.getOutputNeurons().length];
            for(int i=0; i<s.length; i++){
                s[i]="";
            }
            return s;
        }
    }
    public String getAnswerFieldName(int index){
        try{
            return this.getTranslator().getAnswerFieldName(index);
        }catch (NullPointerException e){
            return Integer.toString(index+1);
        }
    }
    public String getAskFieldName(int index){
        try{
            return this.getTranslator().getAskFieldName(index);
        }catch (NullPointerException e){
            return Integer.toString(index+1);
        }
    }
    public int getAskSize(){
        try{
            return this.getTranslator().getAskFieldsCount();
        }catch(NullPointerException e){
            return this.getInputNeurons().length;
        }
    }
    public int getAnswerSize(){
        try{
            return this.getTranslator().getAnswerFieldsCount();
        }catch(NullPointerException e){
            return this.getOutputNeurons().length;
        }
    }
    public void outputDefinition(OutputStream out){
        XmlFileOrigin.outputNeuralNetwork(this, out);
    }

    public void outputTranslatorDefinition(OutputStream out){
        if(this.translator!=null)
            XmlFileOrigin.outputTranslator(this.getTranslator(), out);
        else{
            try {
                out.flush();
                out.close();
            } catch (IOException ex) {
                throw new NeuralException("Empty translator output error", ex);
            }
        }
    }


    public void nextEpoch(){
        this.getTeacher().nextEpoch();
        this.processEvent(new NeuralEvent(this, NeuralEvent.EPOCHCHANGED));
    }

    /*protected void finalize(){
        if(this.name!=null){
            System.out.println("Autosave the network " + this.getName());
            save();
        }
    }*/

    public WeightHolder getNeuronOrSynapse(String name){
        try{
            if (name.startsWith("N-")) {
                int ind = Integer.parseInt(name.substring(2));
                return getNeurons()[ind-1];
            } else if (name.startsWith("S-")) {
                int defis = name.indexOf('-', 2);
                int ind = Integer.parseInt(name.substring(2, defis));
                int synind = Integer.parseInt(name.substring(defis + 1));
                Synapse s = getNeurons()[ind-1].getInSynapses()[synind-1];
                return s;
            } else
                throw new NeuralException("Wrong neuron or synapse name - " + name);
        }
        catch(NumberFormatException e){
            throw new NeuralException("Wrong neuron or synapse index in name - " + name, e);
        }
        catch(ArrayIndexOutOfBoundsException e){
            throw new NeuralException("No neuron with this name - " + name, e);
        }
    }

    public Synapse addSynapse(Neuron from, Neuron to) {
        synchronized(this.teacher){

            Synapse syn = from.addOutSynapseTo(to);
            this.weightHolders = null;//this.getNeuronsAndSynapses();  // this.weightHolders = null;
            this.processEvent(new NeuralEvent(this,
                                              NeuralEvent.TOPOLOGYMODIFIED));
            return syn;
        }
    }

    public void setWeightOrThreshold(WeightHolder neuronOrSynapse,
                                     double weightOrThreshold) {
        neuronOrSynapse.setWeight(weightOrThreshold);
    }

    /*public void setStatistic(WeightHolder neuronOrSynapse, Statistic statistic) {
        neuronOrSynapse.setStatistic(statistic);
    }*/

    public void setActivation(Neuron neuron, Activation activation) {
        neuron.setActivation(activation);
    }

    public void removeOutSynapse(Neuron from, Synapse synapse) {
        synchronized(this.teacher){

            from.removeOutSynapse(synapse);
            this.weightHolders = null;//this.getNeuronsAndSynapses();  // this.weightHolders = null;
            this.processEvent(new NeuralEvent(this,
                                              NeuralEvent.TOPOLOGYMODIFIED));
        }
    }

    public void initWeightOrThreshold(WeightHolder neuronOrSynapse) {
        neuronOrSynapse.initWeight();
    }
    public void save(){
        try{
            NeuralNetworkFactory.getNeuralNetworkFactory().saveNeuralNetwork(this);
            this.processEvent(new NeuralEvent(this, NeuralEvent.SAVED));
        }catch(NoClassDefFoundError e){
            XmlFileOrigin xfo = new XmlFileOrigin();
            try{
                xfo.saveNeuralNetwork((NeuralNetworkLocal)this);
                this.processEvent(new NeuralEvent(this, NeuralEvent.SAVED));
            }
            catch (ClassCastException ee){
                throw new NeuralException("Can not save remote network with this configuration");
            }
        }
    }

    public void teachNeuron(Neuron neuron, double[] correction) {
        neuron.teach(correction);
    }
}
