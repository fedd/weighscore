package com.weighscore.neuro.server;

public class neural {
    public String[] ask(String name, String[] args){
        return NeuralService.ask(name, args);
    }

    public double[] teach(String name, String[] args){
        return NeuralService.teach(name, args);
    }

    public double[] test(String name, String[] args){
        return NeuralService.test(name, args);
    }

    public String[] getFieldNames(String name){
        return NeuralService.getFieldNames(name);
    }

    public String getAnswerFieldName(String name, int index) {
        return NeuralService.getAnswerFieldName(name, index);
    }

    public String getAskFieldName(String name, int index) {
        return NeuralService.getAskFieldName(name, index);
    }

    public int getAnswerSize(String name) {
        return NeuralService.getAnswerSize(name);
    }

    public int getAskSize(String name) {
        return NeuralService.getAskSize(name);
    }

    public void nextEpoch(String name) {
        NeuralService.nextEpoch(name);
    }
}
