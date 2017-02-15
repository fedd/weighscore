package com.weighscore.neuro;

public interface NeuralNetworkEditable extends NeuralNetworkGeneral {
    public abstract void save();

    // new editing pethods
    public abstract Synapse addSynapse(Neuron from, Neuron to);
    public abstract void setWeightOrThreshold(WeightHolder neuronOrSynapse, double weightOrThreshold);
    public abstract void initWeightOrThreshold(WeightHolder neuronOrSynapse);
    public abstract void teachNeuron(Neuron neuron, double[] correction);
    //public abstract void setStatistic(WeightHolder neuronOrSynapse, Statistic statistic);
    public abstract void setActivation(Neuron neuron, Activation activation);
    public abstract void removeOutSynapse(Neuron from, Synapse synapse);

    // editing pethods
    public abstract void removeNeuron(Neuron neuron);
    public abstract Neuron addNeuron();
    public abstract void setName(String name);
    public abstract void setTeacher(Teacher teacher);
    public abstract void setTranslator(Translator translator);
    public abstract void markAsInput(Neuron neuron);
    public abstract void unMarkAsInput(Neuron neuron);
    public abstract void markAsOutput(Neuron neuron);
    public abstract void unMarkAsOutput(Neuron neuron);
}
