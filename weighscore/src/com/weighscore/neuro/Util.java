package com.weighscore.neuro;

import java.lang.reflect.*;
import java.text.*;
//import java.io.*;

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
public class Util {
    public static String prtArr(double[] arr, boolean numbers){
        StringBuffer s = new StringBuffer();
        for(int i=0; i<arr.length; i++){
            if(numbers){
                s.append(i);
                s.append(':');
                s.append(' ');
            }
            s.append(arr[i]);
            s.append('\t');
        }
        return s.toString();
    }
    public static String prtArr(String[] arr, boolean numbers){
        StringBuffer s = new StringBuffer();
        for(int i=0; i<arr.length; i++){
            if(numbers){
                s.append(i);
                s.append(':');
                s.append(' ');
            }
            s.append(arr[i]);
            s.append('\t');
        }
        return s.toString();
    }

    public static double getMaxMod(double[] arr){
        double curmax = 0;
        for(int i=0;i<arr.length;i++){
            double curabs = Math.abs(arr[i]);
            if(curmax<curabs){
                curmax=curabs;
            }
        }
        return curmax;
    }

    protected static double getRandom(double from, double to){
        return Math.random()*(to-from) + from;
    }

    protected static double[] getArrCopy(double[] arrsrc){
        double[] arr = new double[arrsrc.length];
        System.arraycopy(arrsrc, 0, arr, 0, arr.length);
        return arr;
    }
    protected static double[][] getArrCopy(double[][] arrsrc){
        double[][] arr = new double[arrsrc.length][];
        for(int i=0; i<arr.length; i++){
            arr[i] = Util.getArrCopy(arrsrc[i]);
        }
        return arr;
    }

    public static double[] convertArr(String[] arr, int offset){
        double[] dest = new double[arr.length-offset];
        for(int i=0; i<dest.length; i++){
            try{
                dest[i] = Double.parseDouble(arr[offset]);
            }
            catch(NumberFormatException e){
                throw new NeuralException("Wrong parameter type; need double");
            }
            offset++;
        }
        return dest;
    }

    public static String[] convertArr(double[] arr){
        String[] dest = new String[arr.length];
        for(int i=0; i<dest.length; i++){
            dest[i] = String.valueOf(arr[i]);
        }
        return dest;
    }
    public static String[] cropArr(String[] arr, int start){
        String[] dest = new String[arr.length-start];
        System.arraycopy(arr, start, dest, 0, dest.length);
        return dest;
    }

    private static String double2str(Double d, int fractionDigits){
        NumberFormat nfr = NumberFormat.getInstance();
        nfr.setMaximumFractionDigits(fractionDigits);
        nfr.setMinimumFractionDigits(fractionDigits);
        return nfr.format(d.doubleValue());
    }
    private static double doubleObject2double(Object o, String name){
        try{
            return ((Double) o).doubleValue();
        }
        catch(ClassCastException e){
            throw new NeuralException("The parameter " + name + "(\"" + o.toString() + "\") is not Double");
        }
    }

    public static Class getPluginClassForName(String className) throws
            ClassNotFoundException {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException ex) {
            try {
                return Class.forName(NeuralNetwork.PLUGINPACKAGE + className);
            } catch (ClassNotFoundException ex1) {
                throw new ClassNotFoundException("Classes not found: " + className +", "+NeuralNetwork.PLUGINPACKAGE + className);
            }
        }
    }
    public static String getPluginNameForClass(Class pluginClass){
        String s = pluginClass.getName();
        s = s.replaceFirst(NeuralNetwork.PLUGINPACKAGE, "");
        return s;
    }

    /*public static class NullStream extends OutputStream
    {
        public void write(int b) throws IOException
        {
        }
    }

    public static class NullPrintStream extends PrintStream
    {
        public NullPrintStream()
        {
            super(new NullStream());
        }
    }*/


    public static class AttributeGetter {
        private static Method getClassNameMethod = null;
        private static Method getWeightMethod = null;
        private static Method getNameMethod = null;
        private static Method getOtherParameterMethod = null;
        private static Method getWeightHolderForthSignalMethod = null;
        private static Method getWeightHolderBackSignalMethod = null;
        private static Method getWeightHolderForthAndBackSignalMethod = null;
        private static Method getFieldMethod = null;
        private static Method getFieldItemMethod = null;

        static {
            Class[] params = new Class[5];
            params[0] = String.class;
            params[1] = NeuralNetwork.class;
            params[2] = WeightHolder.class;
            params[3] = Signal.class;
            params[4] = Integer.class;
            try {
                getClassNameMethod = AttributeGetter.class.getDeclaredMethod("getClassName", params);
                getWeightMethod = AttributeGetter.class.getDeclaredMethod("getWeight", params);
                getNameMethod = AttributeGetter.class.getDeclaredMethod("getName", params);
                getOtherParameterMethod = AttributeGetter.class.getDeclaredMethod("getOtherParameter",params);
                getWeightHolderForthSignalMethod = AttributeGetter.class.getDeclaredMethod("getWeightHolderForthSignal", params);
                getWeightHolderBackSignalMethod = AttributeGetter.class.getDeclaredMethod("getWeightHolderBackSignal",params);
                getWeightHolderForthAndBackSignalMethod = AttributeGetter.class.getDeclaredMethod("getWeightHolderForthAndBackSignal",params);
                getFieldMethod = AttributeGetter.class.getDeclaredMethod("getField",params);
                getFieldItemMethod = AttributeGetter.class.getDeclaredMethod("getFieldItem",params);
            } catch (Exception ex) {
                throw new RuntimeException("Couldn't initialize methods", ex);
            }
        }





        public static String getStringAttribute(String[] longNames, NeuralNetwork nn, Signal sig, int maximumFractionDigits, String separator){
            StringBuffer out = new StringBuffer();
            boolean skipped = false;
            for(int i=0; i<longNames.length; i++){
                String s;
                try{
                    s = getStringAttribute(longNames[i], nn, sig,
                                           maximumFractionDigits);
                }
                catch(NeuralException e){
                    s="";
                }
                if (s.length() > 0){
                    if (skipped) {
                        out.append(separator);
                    } else {
                        skipped = true;
                    }
                }
                out.append(s);
            }
            String ret = out.toString();
            return ret;
        }

        public static String getStringAttribute(String[] shortNames, NeuralNetwork nn, WeightHolder wh, Signal sig, int maximumFractionDigits, String separator){
            StringBuffer out = new StringBuffer();
            boolean skipped = false;
            for(int i=0; i<shortNames.length; i++){
                String s;
                try{
                    s = getStringAttribute(shortNames[i], nn, wh, sig,
                                           maximumFractionDigits);
                }
                catch(NeuralException e){
                    s="";
                }
                if (s.length() > 0){
                    if (skipped) {
                        out.append(separator);
                    } else {
                        skipped = true;
                    }
                }
                out.append(s);
            }
            String ret = out.toString();
            return ret;
        }

        public static String getStringAttribute(String longName, NeuralNetwork nn, Signal sig, int maximumFractionDigits){
            Object o = getObjectAttribute(longName, nn, sig, maximumFractionDigits);
            try{
                Double d = (Double)o;
                return double2str(d, maximumFractionDigits);
            }
            catch(ClassCastException e){
                return o.toString();
            }
        }
        public static String getStringAttribute(String shortName, NeuralNetwork nn, WeightHolder wh, Signal sig, int maximumFractionDigits){
            Object o = getObjectAttribute(shortName, nn, wh, sig, maximumFractionDigits);
            try{
                Double d = (Double)o;
                return double2str(d, maximumFractionDigits);
            }
            catch(ClassCastException e){
                return o.toString();
            }
        }
        public static double getDoubleAttribute(String longName, NeuralNetwork nn, Signal sig){
            return doubleObject2double(getObjectAttribute(longName, nn, sig, 0), longName);
        }
        public static double getDoubleAttribute(String shortName, NeuralNetwork nn, WeightHolder wh, Signal sig){
            return doubleObject2double(getObjectAttribute(shortName, nn, wh, sig, 0), shortName);
        }
        private static Object getObjectAttribute(String longName, NeuralNetwork nn, Signal sig, int maximumFractionDigits){
            int dot = longName.indexOf('.');
            String start = longName.substring(0, dot);
            String shortName = longName.substring(dot+1);
            if(start.equalsIgnoreCase("network")){
                return getObjectAttribute(shortName, nn, null, sig, maximumFractionDigits);
            }
            else{
                WeightHolder wh = nn.getNeuronOrSynapse(start);
                return getObjectAttribute(shortName, nn, wh, sig, maximumFractionDigits);
            }
        }
        private static Object getObjectAttribute(String shortName, NeuralNetwork nn, WeightHolder wh, Signal sig, int maximumFractionDigits){
            Method m = getMethod(shortName);
            Object[] params = new Object[5];
            params[0] = shortName;
            params[1] = nn;
            params[2] = wh;
            params[3] = sig;
            params[4] = new Integer(maximumFractionDigits);
            Object o = null;
            try {
                o = m.invoke(null, params);
            } catch (Exception ex) {
                if(ex instanceof InvocationTargetException){
                    InvocationTargetException iex = (InvocationTargetException)ex;
                    Throwable te = iex.getTargetException();
                    if(te instanceof NeuralException){
                        throw (NeuralException)te;
                    }
                }
                throw new RuntimeException("Couldn't execute command " + shortName , ex);
            }
            /*} catch (NeuralException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new RuntimeException("Couldn't execute command " + shortName , ex);
            }*/
            return o;
        }

        private static Method getMethod(String shortName){
            if(shortName.equalsIgnoreCase("weight"))
                return getWeightMethod;
            if(shortName.equalsIgnoreCase("name"))
                return getNameMethod;
            if(shortName.equalsIgnoreCase("statistic"))
                return getClassNameMethod;
            if(shortName.equalsIgnoreCase("activation"))
                return getClassNameMethod;
            if(shortName.equalsIgnoreCase("teacher"))
                return getClassNameMethod;
            if(shortName.equalsIgnoreCase("field"))
                return getFieldMethod;
            if(shortName.equalsIgnoreCase("item"))
                return getFieldItemMethod;

            int dot = shortName.indexOf('.');
            if(dot==-1)
                return getOtherParameterMethod;
                //throw new NeuralException("No such attribute - " + shortName);

            String start = shortName.substring(0, dot);
            if(start.equalsIgnoreCase("signal")){
                String end = shortName.substring(dot + 1);
                if (end.equalsIgnoreCase("forth")) {
                    return getWeightHolderForthSignalMethod;
                } else if (end.equalsIgnoreCase("back")) {
                    return getWeightHolderBackSignalMethod;
                } else if (end.equalsIgnoreCase("both")) {
                    return getWeightHolderForthAndBackSignalMethod;
                } else
                    throw new NeuralException("No such signal attribute - " + shortName);
            }else{
                throw new NeuralException("No such attribute - " + shortName);
            }
        }





        //     methods called by name
        // all have (String name, NeuralNetworkAbstract nn, WeightHolder wh, Signal sig)
        private static double getWeight(String name, NeuralNetwork nn, WeightHolder wh, Signal sig, Integer maximumFractionDigits){
            return wh.getWeight();
        }
        private static String getName(String name, NeuralNetwork nn, WeightHolder wh, Signal sig, Integer maximumFractionDigits){
            if(wh==null)
                return nn.getName();
            else
                return wh.getName();
        }
        private static String getClassName(String name, NeuralNetwork nn, WeightHolder wh, Signal sig, Integer maximumFractionDigits){
            String clnm;
            if(wh!=null){
                if(name.equals("statistic")){
                    StringBuffer sb = new StringBuffer();
                    String[] sts = wh.getMultiStats().getStatisticNames();
                    for(int i=0; i<sts.length;i++){
                        if(i!=0)
                            sb.append(' ');
                        sb.append(sts[i]);
                    }
                    clnm = sb.toString();
                }
                else{
                    try{
                        Neuron n = ((Neuron) wh);
                        clnm = n.getActivation().getClass().getName();
                    }
                    catch(ClassCastException e){
                        clnm = "";
                    }
                }
            }
            else
                clnm = nn.getTeacher().getClass().getName();
            clnm = clnm.replaceFirst(NeuralNetwork.PLUGINPACKAGE, "");
            return clnm;
        }
        private static Object getOtherParameter(String name, NeuralNetwork nn, WeightHolder wh, Signal sig, Integer maximumFractionDigits){
            ParameterHolder ph;
            if(wh!=null){
                ph = wh.getMultiStats();
                try{
                    return getParameter(name, ph);
                }catch(NeuralException e){
                    if(wh instanceof Neuron){
                        ph = ((Neuron) wh).getActivation();
                        return getParameter(name, ph);
                    }else{
                        throw new NeuralException("No such synapse parameter - " + name);
                    }
                }
            }
            else{
                ph = nn.getTeacher();
                return getParameter(name, ph);
            }
        }
        private static double getWeightHolderForthSignal(String name, NeuralNetwork nn, WeightHolder wh, Signal sig, Integer maximumFractionDigits){
            return getWeightHolderBackOrForthSignal(wh, sig, true);
        }
        private static double getWeightHolderBackSignal(String name, NeuralNetwork nn, WeightHolder wh, Signal sig, Integer maximumFractionDigits){
            return getWeightHolderBackOrForthSignal(wh, sig, false);
        }
        private static String getWeightHolderForthAndBackSignal(String name, NeuralNetwork nn, WeightHolder wh, Signal sig, Integer maximumFractionDigits){
            String forth = double2str(new Double(getWeightHolderBackOrForthSignal(wh, sig, false)), maximumFractionDigits.intValue());
            String back = double2str(new Double(getWeightHolderBackOrForthSignal(wh, sig, true)), maximumFractionDigits.intValue());
            return forth + " - " + back;
        }


        // helper methods
        private static Object getParameter(String name, ParameterHolder ph){
            String type = ph.getParameterType(name);
            if(type.equals("double"))
                return new Double(ph.getDoubleParameter(name));
            if(type.equals("long"))
                return new Long(ph.getLongParameter(name));
            if(type.equals("int"))
                return new Integer(ph.getIntegerParameter(name));
            return ph.getParameter(name);
        }
        private static double getWeightHolderBackOrForthSignal(WeightHolder wh, Signal sig, boolean forth){
            if(sig==null)
                //return 0;
                throw new NeuralException("No signal");
            if(!forth){
                try{
                    return sig.getGradient(wh);
                }
                catch (NullPointerException e){
                    return 0;
                }
            }
            else{
                Neuron n;
                if (wh instanceof Neuron) {
                    n = ((Neuron) wh);
                } else {
                    Synapse s = (Synapse) wh;
                    n = s.getInNeuron();
                }
                return sig.getAnswer(n);
            }
        }
        private static String getField(String name, NeuralNetwork nn, WeightHolder wh, Signal sig, Integer maximumFractionDigits){
            if(wh instanceof Neuron){
                Neuron n = (Neuron) wh;
                Translator tr = nn.getTranslator();
                if(tr==null){
                    if (n.isInput()) {
                        return Integer.toString(n.getInputIndex());
                    } else if (n.isOutput()) {
                        return Integer.toString(n.getOutputIndex());
                    } else
                        throw new NeuralException("Can't get field nuber from hidden layer neuron");
                }else
                    return tr.getFieldNameByNeuron(n);
            }else
                throw new NeuralException("Can't get field entry from synapse");
        }
        private static String getFieldItem(String name, NeuralNetwork nn, WeightHolder wh, Signal sig, Integer maximumFractionDigits){
            if(wh instanceof Neuron){
                Neuron n = (Neuron) wh;
                Translator tr = nn.getTranslator();
                if(tr==null){
                    throw new NeuralException("No translator - can't get field's dictionary items");
                }else
                    return tr.getFieldItem(n);
            }else
                throw new NeuralException("Can't get field's dictionary item from synapse");
        }
    }
}
