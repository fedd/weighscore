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
public class Translator {

    public String[] field;
    public int askfields=0;
    public boolean[] ispass;

    public double[] dividor;

    public String[][] inputval; //[поле][номер активатора] - входящее значения справочников

    public int[][] activator;// [поле][номер активатора] - номер нейрона куда писать
    public int[][] backActivator; //[вход-выход][номер нейрона] - поле

    public double[][] outputrange_val; // [поле][номер активатора] - значение, которое писать
    public double[][] outputrange_min; // [поле][номер активатора] - значение для обратной расшифровки
    public double[][] outputrange_max; // [поле][номер активатора] - значение для обратной расшифровки

    public int asknrns; // количество входящих нейронов (вопрос)
    public int ansnrns; // количество исходящих нейронов (ответ)

    private void init(String[] fields,
                     int askFieldsCount,
                     boolean[] isPasses,
                     double[] dividors,
                     String[][] inputValues,
                     int[][] activators,
                     //int[][] backActivators,
                     double[][] outputRangeValue,
                     double[][] outputRangeMin,
                     double[][] outputRangeMax,
                     int askNeuronsCount,
                     int answerNeuronsCount){
        int fldCnt = fields.length;




        /*if(backActivators.length!=2)
            throw new NeuralException("Wrong translator init params (backActivators must have 2 entries)");
        else {
            for(int i=0; i<1; i++){
                int[] ba = backActivators[i];
                if(i==0){
                    if (ba.length != askNeuronsCount)
                        throw new NeuralException(
                                "Wrong translator init params (input backActivator neurons count doesn't fit)");
                }
                else if (ba.length != answerNeuronsCount){
                    throw new NeuralException(
                            "Wrong translator init params (output backActivator neurons count doesn't fit)");
                }
                for(int j=0; j<ba.length; j++){
                    if(i==0){
                        if (ba[j] < 0 || ba[j] > askFieldsCount)
                            throw new NeuralException(
                                    "Wrong translator init params (input backActivator #"+j+" is negative or more than input fields count)");
                    }
                    else {
                        if (ba[j] < 0 || ba[j] > fldCnt - askFieldsCount)
                            throw new NeuralException(
                                    "Wrong translator init params (output backActivator #"+j+" is negative or more than output fields count)");
                    }
                }
            }
        }*/

        if(fldCnt <= askFieldsCount
           || fldCnt != isPasses.length
           || fldCnt != dividors.length
           || fldCnt != inputValues.length
           || fldCnt != activators.length
           || fldCnt !=  outputRangeValue.length
           || fldCnt !=  outputRangeMin.length
           || fldCnt !=  outputRangeMax.length){
            throw new NeuralException("Wrong translator init params (field counts don't fit)");
        }

        for(int i=0; i<activator.length; i++){
            int actCnt = activator[i].length;
            if(actCnt != outputRangeValue[i].length ||
                    actCnt != outputRangeMin[i].length ||
                         actCnt != outputRangeMax[i].length){
                throw new NeuralException("Wrong translator init params (activator counts don't fit, activator #"+i+")");
            }
        }




        field = fields;
        askfields=askFieldsCount;
        ispass = isPasses;

        dividor = dividors;

        inputval = inputValues;

        activator = activators;

        outputrange_val = outputRangeValue;
        outputrange_min = outputRangeMin;
        outputrange_max = outputRangeMax;

        asknrns = askNeuronsCount;
        ansnrns = answerNeuronsCount;


        backActivator = new int[2][];
        backActivator[0] = new int[asknrns];
        backActivator[1] = new int[ansnrns];
        for(int i=0; i<activator.length; i++){
            int inout = (askfields<=i?0:1);
            for(int j = 0; j<activator[i].length; j++){
                backActivator[inout][activator[i][j]] = i;
            }
        }
    }

    public int getAskNeuronsCount(){
        return this.asknrns;
    }
    public int getAnswerNeuronsCount(){
        return this.ansnrns;
    }
    public String getFieldName(int fieldIndex){
        return this.field[fieldIndex];
    }
    public boolean isAskField(int fieldIndex){
        return fieldIndex >= this.askfields;
    }
    public boolean isPass(int fieldIndex){
        return this.ispass[fieldIndex];
    }
    public double getDivisor(int fieldIndex){
        return this.dividor[fieldIndex];
    }
    public int getItemsCount(int fieldIndex){
        return this.inputval[fieldIndex].length;
    }
    public String getItem(int fieldIndex, int activatorIndex){
        return this.inputval[fieldIndex][activatorIndex];
    }
    public int getActivatedNeuronInOutIndex(int fieldIndex, int activatorIndex){
        return this.activator[fieldIndex][activatorIndex];
    }
    public int getActivatedNeuronInOutIndicesCount(int fieldIndex){
        return this.activator[fieldIndex].length;
    }
    public int getFieldIndexByNeuron(Neuron neuron){
        boolean input = neuron.isInput();
        if(!input && !neuron.isOutput())
            throw new NeuralException("Can't get field index from hidden layer neuron");
        int inoutindex = (input?neuron.getInputIndex() : neuron.getOutputIndex());
        //if(!input)
        //    inoutindex = inoutindex + this.askfields;
        return this.backActivator[(input?0:1)][inoutindex-1];
    }
    public String getFieldNameByNeuron(Neuron neuron){
        return field[this.getFieldIndexByNeuron(neuron)];
    }
    public String getFieldItem(Neuron neuron){
        int fldind = this.getFieldIndexByNeuron(neuron);
        if(!ispass[fldind]){
            StringBuffer sb = new StringBuffer();
            int neuind = (neuron.isInput()?neuron.getInputIndex():neuron.getOutputIndex()) - 1;
            boolean washere = false;
            for (int i=0; i<this.activator[fldind].length; i++){
                if(activator[fldind][i] == neuind){
                    if(washere)
                        sb.append('|');
                    sb.append(inputval[fldind][i]);
                    washere = true;
                }
            }
            if(washere)
                return sb.toString();
            else
                throw new NeuralException("No item found for neuron " + neuron.getIndex());
        }
        throw new NeuralException("No items in pass fields");
    }



    public double getActivatingValue(int fieldIndex, int activatorIndex){
        return this.outputrange_val[fieldIndex][activatorIndex];
    }
    public int getActivatingValuesCount(int fieldIndex){
        return this.outputrange_val[fieldIndex].length;
    }
    public double getActivatingMaxValue(int fieldIndex, int activatorIndex){
        return this.outputrange_max[fieldIndex][activatorIndex];
    }
    public double getActivatingMinValue(int fieldIndex, int activatorIndex){
        return this.outputrange_min[fieldIndex][activatorIndex];
    }

    public String[] getFieldNames(){
        return field;
    }

    public int getAskFieldsCount(){
        return askfields;
    }

    public int getAnswerFieldsCount(){
        return field.length - askfields;
    }

    public String getAskFieldName(int index){
        if (index>askfields)
            throw new NeuralException("No input field with this index " + index);
        return this.field[index];
    }

    public String getAnswerFieldName(int index){
        return this.field[askfields+index];
    }

    public Translator() {
    }

    protected double[][] translate(String[] input){
        if(input.length>ispass.length){
            throw new NeuralException("Translation: too many parameters");
        }else if (input.length < this.askfields){
            throw new NeuralException("Translation: too few parameters");
        }
        double[][] trinput = new double[2][];
        trinput[0] = new double[asknrns];
        trinput[1] = new double[ansnrns];

        //System.out.println("in: " + NeuralNetworkFactory.prtArr(input));

        for(int i=0; i< input.length; i++){// i - счетчик филдов
            int ans;
            if(i<askfields){
                ans=0;
            }else{
                ans=1;
            }
            if(ispass[i]){
                // просто передаем значение
                try{
                    double toput;
                    if(input[i]==null)
                        toput = 0;
                    else
                        toput = Double.parseDouble(input[i]) / dividor[i];
                    trinput[ans][activator[i][0]] = toput;
                }
                catch(NumberFormatException e){
                    throw new NeuralException("Input value #" + (i+1) + " (field \""+this.field[i]+"\") must be double");
                }
            }else{
                // проходим по всем активаторам
                boolean found = false;
                for(int ii=0; ii<activator[i].length; ii++){
                    if( inputval[i][ii].equals(input[i]) ){// значение справочника совпало
                        trinput[ans][activator[i][ii]] = outputrange_val[i][ii];
                        found = true;
                        break;
                    }
                }
                if(!found && input[i]!= null)
                    throw new NeuralException("Translator didn't found any activator for value \""+input[i]+"\" in field #" + (i+1) + " \""+this.field[i]+"\"");
            }
        }
        //System.out.println("out: " + NeuralNetworkFactory.prtArr(trinput[0]) + '='
          //      + NeuralNetworkFactory.prtArr(trinput[1]));

        return trinput;
    }

    protected String[] backTranslate(double[][] output){
        String[] troutput = new String[field.length];

        //System.out.println("bin: " + NeuralNetworkFactory.prtArr(output[0]) + '='
          //      + NeuralNetworkFactory.prtArr(output[1]));

        for(int i=0; i< output.length; i++){// i - счетчик 0-1 (вопрос-ответ)
            for (int j = 0; j < output[i].length; j++) { // счетчик нейронов внутри вопроса-ответа

                // теперь нужно найти соответствие нейрона полю
                int fnum = backActivator[i][j];
                if (ispass[fnum]) {
                    troutput[fnum] = Double.toString(output[i][j] * dividor[fnum]);
                } else {
                    // нужнонайти значение справочника по min-max
                    for(int k=0; k<outputrange_val[fnum].length; k++){
                        if(output[i][j]>=outputrange_min[fnum][k] && output[i][j]<=outputrange_max[fnum][k]
                                && j== activator[fnum][k]){
                            troutput[fnum] = inputval[fnum][k];
                            break;
                        }
                    }
                }
            }
        }
        //System.out.println("bout: " + NeuralNetworkFactory.prtArr(troutput));
        return troutput;
    }

    /*public static Translator getTranslator(String name){
        Translator tran = null;
        if(name!=null && name.length()!=0){
            try {
                try {
                    tran = NeuralNetworkFactory.getNeuralNetworkFactory().
                           getTranslator(name);
                } catch (NoClassDefFoundError e) {
                    XmlFileOrigin xfo = new XmlFileOrigin();
                    tran = new Translator();
                    xfo.initTranslator(tran, "Translator.xml");
                }
            } catch (NeuralException e) {
                throw new NeuralException("Couldn't get the translator " + name, e);
            }
        }
        return tran;
    }

    protected static Translator createTranslator(String name, InputStream translatorDefinition){
        Translator tran = null;
        try{
            tran = NeuralNetworkFactory.getNeuralNetworkFactory().
                   createTranslator(name, translatorDefinition);
        } catch (NoClassDefFoundError e) {
            tran = new Translator();
            XmlFileOrigin.initTransByInput(tran, translatorDefinition);
        }
        return tran;
    }

    public void outputDefinition(OutputStream out){
        XmlFileOrigin.outputTranslator(this, out);
    }*/
}
