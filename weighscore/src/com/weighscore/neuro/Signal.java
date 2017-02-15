package com.weighscore.neuro;

/**
 * The class which performs the network asking process and error back
 * propagation. It is instantiated for every ask, teach or test action.
 * The system calls go method when asking the network.
 * The teacher may call goBack method to back propagat the error and to get
 * gradient
 *
 * @author Fyodor Kravchenko
 * @version 2.0
 */
public class Signal /*extends NeuralEventGenerator*/ {
    private NeuralNetworkLocal na;
    private Neuron[] n;

    // status holders
    private double[][] inputs;
    private double[][] errInputs;
    private double[][] gradient;
    private double[] answers, errors;
    private int[] waits, waitsB;
    private int[] readies, readiesB;
    private boolean done=false;
    private boolean doneB=false;

    //private double[] networkError=null, networkQuestion = null, networkAnswer = null;

    /**
     * Tests if the signal was sent forth and reached its end
     *
     * @return true if the signal was sent forth and reached it's destination
     */
    public boolean getGoneForth(){
        return this.done;
    }

    /**
     * Tests if the signal was sent back and reached its end
     *
     * @return true if the signal was sent back and reached it's destination
     */
    public boolean getGoneBack(){
        return this.doneB;
    }

    /**
     * Gets the output of the specified neuron
     *
     * @param n Neuron
     * @return output of the neuron
     */
    public double getAnswer(Neuron n){
        //if(done)
            return answers[n.getIndex()];
        //else
          //  throw new NeuralException("The signal didn't propagated");
    }

    /**
     * Gets the back propagated error for the specified neuron
     *
     * @param n Neuron
     * @return double
     */
    public double getError(Neuron n){
        return errors[n.getIndex()];
    }

    /**
     * Gets the fitness function gradient member for the specified neuron
     *
     * @param wh Neuron or synapse
     * @return gradient member
     */
    public double getGradient(WeightHolder wh){
        if(wh instanceof Neuron){
            Neuron n = (Neuron)wh;
            return gradient[n.getIndex()][0];
        }
        else{
            Synapse s = (Synapse)wh;
            Neuron n = s.getOutNeuron();
            return gradient[n.getIndex()][s.getIndexAsIn()+1];
        }
    }

    /**
     * Returns the gradient values that are computed while error back
     * propagation process
     *
     * @return The gradient members as an array of arrays of doubles. The array
     *   size equals to the quantity of neurons in the network. Every array
     *   entry is an array, which size equals to the quantity of the neuron's
     *   input sysnapses plus one; the first array entry corresponds to the
     *   neuron's theshold
     */
    public double[][] getGradient(){
        return Util.getArrCopy(this.gradient);
    }

    // настройки
    private byte delay=3;

    protected static Signal getSignal(NeuralNetworkLocal neuralNetwork){
        Signal sig;
        while(true){
            try {
                sig = new Signal(neuralNetwork);
                return sig;
            } catch (OutOfMemoryError er) {
                System.out.print("\ngc - ");
                System.gc();
                System.out.println("done");
            }
        }
    }

    public boolean isExcited(Neuron neuron){
        try{
            return this.waits[neuron.getIndex()] == 0;
        }catch(NullPointerException e){
            return false;
        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }
    public boolean isExcitedBack(Neuron neuron){
        try{
            return this.waitsB[neuron.getIndex()]==0;
        }catch(NullPointerException e){
            return false;
        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }

    public float getExcitedness(Neuron neuron){
        float ex;
        try{
            ex = this.readies[neuron.getIndex()];
        }catch(NullPointerException e){
            return 0;
        } catch (ArrayIndexOutOfBoundsException e) {
            return 0;
        }
        if(ex>0 && neuron.getInSynapses().length !=0){
            ex = ex / neuron.getInSynapses().length;
        }
        else{
            ex=0;
        }
        return ex;
    }
    public float getBackExcitedness(Neuron neuron){
        float ex;
        try{
            ex = this.readiesB[neuron.getIndex()];
        }catch(NullPointerException e){
            return 0;
        } catch (ArrayIndexOutOfBoundsException e) {
            return 0;
        }
        if(ex>0 && neuron.getOutSynapses().length!=0){
            ex = ex / neuron.getOutSynapses().length;
        }
        else{
            ex=0;
        }
        return ex;
    }

    private Signal(NeuralNetworkLocal na) {
        //this.setNeuralEventListener(na.getNeuralEventListener());
        this.na=na;
        this.n=na.getNeurons();

        this.inputs=new double[this.n.length][];
        this.answers = new double[n.length];
        this.waits=new int[n.length];
        this.readies=new int[n.length];
        for(int i=0;i<n.length;i++){
            inputs[i] = new double[n[i].getInSynapses().length];
            waits[i] = -1;
        }
    }

    /**
     * Propagate the question by the neural network
     *
     * @param question the question (input values)
     * @return the answer (the output values)
     */
    public double[] go(double[] question){
        if(done){
            throw new NeuralException("The signal has already reached its end");
        }
        else{
            //this.networkQuestion = question;
            this.prepare(question);
            while(this.step()>0);
            double[] answer = new double[this.na.getOutputNeurons().length];
            for(int i=0; i< answer.length; i++){
                answer[i] = this.answers[this.na.getOutputNeurons()[i].getIndex()];
            }
            done=true;
            //this.networkAnswer = answer;
            return answer;
        }
    }

    /**
     * Propagate the error back computing the gradient
     *
     * @param error error (the difference between the given answer and the
     *   correct answer)
     * @return the gradient
     */
    public double[][] goBack(double[] error/*, boolean teach*/){
        //this.networkError = error;

        if(doneB){
            throw new NeuralException("The backpropagation signal has already reached its beginning");
        }
        else{
            this.prepareB(error);
            while(this.stepBack(/*teach*/)>0);
            doneB=true;
        }

        return this.getGradient();
    }



    private void prepare(double[] question){

        Neuron[] in=this.na.getInputNeurons();

        if(question.length!=in.length){
            throw new NeuralException("The question size and the area input size don't match");
        }

        // проходим по входным нейронам, далее по их выходным синапсам и ставим входные значения далее
        for(int i=0; i<question.length; i++){
            answers[in[i].getIndex()] = question[i];

            Synapse[] outs = in[i].getOutSynapses();

            this.setOutsAsIns(outs, question[i]);
        }
    }

    private void prepareB(double[] error){
        while(true){
            try{
                this.errInputs = new double[this.n.length][];
                this.gradient = new double[n.length][];
                this.errors = new double[n.length];
                this.waitsB = new int[n.length];
                this.readiesB = new int[n.length];
                for (int i = 0; i < n.length; i++) {
                    errInputs[i] = new double[n[i].getOutSynapses().length];
                    waitsB[i] = -1;
                }

                Neuron[] out = this.na.getOutputNeurons();

                if (error.length != out.length) {
                    throw new NeuralException(
                            "The answer size and the network output size don't match");
                }

                // проходим по выходным нейронам, далее по их входным синапсам и ставим входные значения далее
                for (int i = 0; i < error.length; i++) {
                    // ставим, что дождался сигнала
                    waitsB[out[i].getIndex()] = 0;
                    // устанавливаем сигнал
                    this.errInputs[out[i].getIndex()] = new double[1];
                    this.errInputs[out[i].getIndex()][0] = error[i];
                }
                break;
            }
            catch(OutOfMemoryError er){
            }
        }
    }

    private void setInsAsOuts(Synapse[] ins, double signal){


        for (int j=0; j<ins.length; j++){
            // нейрон, входной для j-го синапса
            Neuron on = ins[j].getInNeuron();

            errInputs[on.getIndex()] // это выходное значение нейрона, входного для j-го синапса
                    [ins[j].indexAsOut]  // это выходное значение номер ins[j].indexAsOut
                    //=signal[j+1]; // was - =signal MINUS cure
                    =signal;

            readiesB[on.getIndex()]++;  // считаем, сколько этот нейрон уже получил выходных значений

            // проверяем, все ли выходные сигналы он уже получил
            if (readiesB[on.getIndex()] < on.getOutSynapses().length){
                waitsB[on.getIndex()] = this.delay;
            }
            else{
                waitsB[on.getIndex()] = 0;
            }
        }
    }

    private void setOutsAsIns(Synapse[] outs, double signal){
        for (int j=0; j<outs.length; j++){
            // нейрон, выходной для j-го синапса
            Neuron on = outs[j].getOutNeuron();

            inputs[on.getIndex()] // это входное значение нейрона, выходного для j-го синапса
                    [outs[j].getIndexAsIn()]  // это входное значение номер out[j].indexAsIn
                    =signal;
            readies[on.getIndex()]++;  // считаем, сколько этот нейрон уже получил входных значений

            // проверяем, все ли входные сигналы он уже получил
            if (readies[on.getIndex()] < on.getInSynapses().length){
                waits[on.getIndex()] = this.delay;
            }
            else{
                waits[on.getIndex()] = 0;
            }
        }
    }

    private int step(){
        int neuswaiting = 0;

        // находим всех, кто дождался всех сигналов
        for(int i=0; i<this.waits.length; i++){
            if (waits[i]==0){
                // i-й нейрон получил все сигналы, можно считать его выходной сигнал и
                // распространять дальше
                try {
                    answers[i] = this.n[i].ask(inputs[i], this);
                    this.setOutsAsIns(this.n[i].getOutSynapses(), answers[i]);
                    neuswaiting = this.n[i].getOutSynapses().length;

                    na.processEvent(new NeuralEvent(this, NeuralEvent.SIGNALPROCEEDEDFORTH));

                    waits[i]=-1;
                } catch (NoResultYetException ex) {
                    waits[i]=1;
                }
            }
        }

        // устанавливаем waits только после степа потому что в степе его смотрим и устанавливаем
        for(int i=0; i<this.waits.length; i++){
            if(waits[i]>0){
                waits[i]--;
                neuswaiting++;
            }
        }

        return neuswaiting;
    }

    private int stepBack(/*boolean teach*/){
        int neuswaiting = 0;

        // находим всех, кто дождался всех сигналов
        for(int i=0; i<this.waitsB.length; i++){
            if (waitsB[i]==0){

                errors[i]=0;
                if(this.n[i].isOutput){
                    errors[i] = errInputs[i][0];
                }
                else{
                    // error of i-th neuron
                    for (int ii = 0; ii < errInputs[i].length; ii++) {
                        errors[i] = errors[i] +
                                    errInputs[i][ii] *
                                    this.n[i].getOutSynapses()[ii].getWeight();
                    }
                }

                //gradient[i] = this.n[i].test(inputs[i], errInputs[i], answers[i]);
                try {
                    gradient[i] = this.n[i].test(inputs[i], errors[i],
                                                 answers[i], this);

                    // MINUS cure
                    //this.setInsAsOuts(this.n[i].getInSynapses(), gradient[i]/*[0]*/);
                    this.setInsAsOuts(this.n[i].getInSynapses(), gradient[i][0] * -1.0);

                    neuswaiting = this.n[i].getInSynapses().length;

                    na.processEvent(new NeuralEvent(this, NeuralEvent.SIGNALPROCEEDEDBACK));

                    waitsB[i]=-1;
                } catch (NoResultYetException ex) {
                    waitsB[i]=1;
                }

            }
        }

        // устанавливаем waits только после степа потому что в степе его смотрим и устанавливаем
        for(int i=0; i<this.waitsB.length; i++){
            if(waitsB[i]>0){
                waitsB[i]--;
                neuswaiting++;
            }
        }

        return neuswaiting;
    }

    /*private double sum(double[] x){
        double ret = 0;
        for (int i = 0; i < x.length; i++) {
            ret = ret + x[i];
        }
        return ret;
    }*/

    /*public double[] getNetworkError(){
        if(this.networkError==null){
            throw new NeuralException("Can't get network error; the signal wasn't sent back");
        }
        return this.networkError;
    }
    public double[] getNetworkQuestion(){
        if(this.networkQuestion==null){
            throw new NeuralException("Can't get network question; the signal wasn't sent");
        }
        return this.networkQuestion;
    }
    public double[] getNetworkAnswer(){
        if(this.networkAnswer==null){
            throw new NeuralException("Can't get network answer; the signal wasn't sent");
        }
        return this.networkAnswer;
    }*/
}
