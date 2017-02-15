package com.weighscore.neuro.server;

import com.weighscore.neuro.*;

public class NeuralService {

    private static NeuralNetworkLocal getNN(String name){
        try{
            return (NeuralNetworkLocal) NeuralNetwork.getNeuralNetwork(name);
        }
        catch(Exception e){
            throw new NeuralServerException("Coudn't get network " + name, e);
        }
    }

    public static String[] ask(String name, String[] args){
        return getNN(name).ask(args);
    }

    public static double[] teach(String name, String[] args){
        return getNN(name).teach(args);
    }

    public static double[] test(String name, String[] args){
        return getNN(name).test(args);
    }

    /*public static void getDefinition(String name, OutputStream out){
        try{
            NeuralNetworkFactory.getNeuralNetworkFactory().getTranslator(name).
                    outputDefinition(out);
        }
        catch(NullPointerException e){
            if(giveNNdef)
                getNN(name).outputDefinition(out);
            else
                throw new NeuralServerException("Unable to get neural network definition");
        }
    }*/

    public static String[] getFieldNames(String name){
        return getNN(name).getFieldNames();
    }

    /*public static void getTranslatorDefinition(String name, OutputStream out){
        try{
            NeuralNetworkFactory.getNeuralNetworkFactory().getTranslator(name).
                    outputDefinition(out);
        }
        catch(NullPointerException e){
            getNN(name).outputTranslatorDefinition(out);
        }
    }*/

    public static String getAnswerFieldName(String name, int index) {
        return getNN(name).getAnswerFieldName(index);
    }

    public static String getAskFieldName(String name, int index) {
        return getNN(name).getAskFieldName(index);
    }

    public static int getAnswerSize(String name) {
        return getNN(name).getAnswerSize();
    }

    public static int getAskSize(String name) {
        return getNN(name).getAskSize();
    }

    public static void nextEpoch(String name) {
        getNN(name).nextEpoch();
    }

}
