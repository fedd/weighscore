package com.weighscore.neuro;

import java.io.OutputStream;

public interface NeuralNetworkGeneral {
    public final static String COPYRIGHT = "(c) Vsetech 2005-2006";
    public final static String WELCOME = "Comments appreciated at http://www.weighscore.com/forum, support@weighscore.com\n";
    public final static String PLUGINPACKAGE = "com.weighscore.neuro.plugins.";

    public String[] ask(String[] question);

    public String getAnswerFieldName(int index);

    public int getAnswerSize();

    public String getAskFieldName(int index);

    public int getAskSize();

    public NeuralNetworkLocal getCopy(String newName, boolean overwrite);

    public String[] getFieldNames();

    public Neuron[] getInputNeurons();

    public String getName();

    public WeightHolder getNeuronOrSynapse(String name);

    public Neuron[] getNeurons();

    public WeightHolder[] getNeuronsAndSynapses();
    public int getNeuronSynapseIndex(WeightHolder neuronOrSynapse);
    public Neuron[] getOutputNeurons();

    public Teacher getTeacher();

    public Translator getTranslator();

    public void nextEpoch();

    public void outputDefinition(OutputStream out);

    public void outputTranslatorDefinition(OutputStream out);

    public double[] teach(String[] questionAnswer);

    public double[] test(String[] questionAnswer);

    public int getInputIndex(Neuron neuron);
    public int getOutputIndex(Neuron neuron);
}
