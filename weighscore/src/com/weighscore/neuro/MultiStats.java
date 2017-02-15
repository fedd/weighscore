package com.weighscore.neuro;


import java.util.*;

public class MultiStats extends Statistic {

    private Hashtable stats = new Hashtable(1,1);  // st classname - st object
    private Hashtable params = null;  // parametername - st object
    private String[] parameterNames=null;
    private String[] statisticNames = null;

    protected MultiStats(WeightHolder whose){
        this.setWhose(whose);
    }

    public synchronized void addStatistic(Statistic statistic){
        String clname = Util.getPluginNameForClass(statistic.getClass());
        if(!this.stats.containsKey(clname)){
            WeightHolder wh = this.getWhose();
            this.stats.put(clname, statistic);
            statistic.setWhose(wh);
            parameterNames = null;
            statisticNames = null;
            this.params=null;
            wh.getNeuralNetwork().processEvent(new NeuralEvent(wh, NeuralEvent.WIGHTHOLDERPARAMETERMODIFIED));
        }
    }

    public Statistic addStatistic(String statisticName){
        Class sc;
        Statistic s;
        try {
            sc = Util.getPluginClassForName(statisticName);
        } catch (ClassNotFoundException ex) {
            throw new NeuralException("No class found - " + statisticName, ex);
        }
        try {
            s = (Statistic) sc.newInstance();
        } catch (IllegalAccessException ex1) {
            throw new NeuralException(ex1);
        } catch (InstantiationException ex1) {
            throw new NeuralException(ex1);
        } catch (ClassCastException ex1) {
            throw new NeuralException("Not a statistic - " + statisticName, ex1);
        }
        this.addStatistic(s);
        return s;
    }

    public synchronized void removeStatistic(String statisticName){
        this.stats.remove(statisticName);
        parameterNames=null;
        statisticNames = null;
        this.params=null;
        WeightHolder wh = this.getWhose();
        wh.getNeuralNetwork().processEvent(new NeuralEvent(wh, NeuralEvent.WIGHTHOLDERPARAMETERMODIFIED));
    }

    public Statistic getStatistic(String statisticName){
        Statistic s = (Statistic) this.stats.get(statisticName);
        if(s!=null)
            return s;
        else
            return this.addStatistic(statisticName);
    }

    public boolean hasStatistic(String statisticName){
        return this.stats.containsKey(statisticName);
    }


    private HashSet getParameterNamesHS(){
        HashSet v = new HashSet();
        for(Enumeration enu = stats.elements();enu.hasMoreElements();){
            Statistic s = (Statistic)enu.nextElement();
            String[] ps = s.getParameterNames();
            for(int i = 0; i<ps.length; i++){
                v.add(ps[i]);
            }
        }
        return v;
    }

    private synchronized Statistic getStatisticByParameter(String paramName){
        if(this.params == null){
            this.params = new Hashtable(1,1);

            for(Enumeration enu = stats.elements();enu.hasMoreElements();){
                Statistic s = (Statistic)enu.nextElement();
                String[] ps = s.getParameterNames();
                for(int i = 0; i<ps.length; i++){
                    params.put(ps[i], s);
                }
            }
        }
        Statistic s = (Statistic) this.params.get(paramName);
        if(s==null)
            throw new NeuralException("No such statistic - " + paramName);
        return s;
    }

    public String[] getStatisticNames(){
        if (this.statisticNames==null){
            Set s = this.stats.keySet();
            String[] nms = new String[s.size()];
            s.toArray(nms);
            this.statisticNames=nms;
        }
        return this.statisticNames;
    }

    /**
     * Returns an array of parameter names
     *
     * @return Array of strings containing the names of the parameters
     */
    public String[] getParameterNames(){
        if (this.parameterNames==null){
            HashSet  v = this.getParameterNamesHS();
            String[] nms = new String[v.size()];
            v.toArray(nms);
            this.parameterNames=nms;
        }

        return this.parameterNames;
    }

    /**
     * Returns the value of the double parameter by name
     *
     * @param parameterName The name of the parameter
     * @return The value of the parameter
     */
    public double getDoubleParameter(String parameterName){
        Statistic s = this.getStatisticByParameter(parameterName);
        return s.getDoubleParameter(parameterName);
    }
    public double getDoubleParameter(String statisticName, String parameterName){
        Statistic s = this.getStatistic(statisticName);
        return s.getDoubleParameter(parameterName);
    }

    /**
     * Returns the value of the long parameter by name
     *
     * @param parameterName The name of the parameter
     * @return The value of the parameter
     */
    public long getLongParameter(String parameterName){
        Statistic s = this.getStatisticByParameter(parameterName);
        return s.getLongParameter(parameterName);
    }
    public long getLongParameter(String statisticName, String parameterName){
        Statistic s = this.getStatistic(statisticName);
        return s.getLongParameter(parameterName);
    }

    public int getIntegerParameter(String parameterName){
        Statistic s = this.getStatisticByParameter(parameterName);
        return s.getIntegerParameter(parameterName);
    }
    public int getIntegerParameter(String statisticName, String parameterName){
        Statistic s = this.getStatistic(statisticName);
        return s.getIntegerParameter(parameterName);
    }

    /**
     * Returns the value of the parameter by name, as object
     *
     * @param parameterName The name of the parameter
     * @return The value of the parameter
     */
    public Object getParameter(String parameterName){
        Statistic s = this.getStatisticByParameter(parameterName);
        return s.getParameter(parameterName);
    }
    public Object getParameter(String statisticName, String parameterName){
        Statistic s = this.getStatistic(statisticName);
        return s.getParameter(parameterName);
    }

    /**
     * Returns the value of the parameter by name, as string
     *
     * @param parameterName The name of the parameter
     * @return The value of the parameter
     */
    public String getParameterAsString(String parameterName){
        Statistic s = this.getStatisticByParameter(parameterName);
        return s.getParameterAsString(parameterName);
    }
    public String getParameterAsString(String statisticName, String parameterName){
        Statistic s = this.getStatistic(statisticName);
        return s.getParameterAsString(parameterName);
    }

    /**
     * Returns the java type of the named parameter, as string
     *
     * @param parameterName The name of the parameter
     * @return The type of the parameter as string
     */
    public String getParameterType(String parameterName){
        Statistic s = this.getStatisticByParameter(parameterName);
        return s.getParameterType(parameterName);
    }
    public String getParameterType(String statisticName, String parameterName){
        Statistic s = this.getStatistic(statisticName);
        return s.getParameterType(parameterName);
    }

    /**
     * Sets the value of the named parameter
     *
     * @param parameterName The name of the parameter
     * @param parameter The value of the parameter as string
     */
    public void setParameter(String parameterName, String parameter){
        Statistic s = this.getStatisticByParameter(parameterName);
        s.setParameter(parameterName, parameter);
        WeightHolder wh = this.getWhose();
        wh.getNeuralNetwork().processEvent(new NeuralEvent(wh, NeuralEvent.WIGHTHOLDERPARAMETERMODIFIED));
    }
    public void setParameter(String statisticName, String parameterName, String parameter){
        Statistic s = this.getStatistic(statisticName);
        s.setParameter(parameterName, parameter);
        WeightHolder wh = this.getWhose();
        wh.getNeuralNetwork().processEvent(new NeuralEvent(wh, NeuralEvent.WIGHTHOLDERPARAMETERMODIFIED));
    }



    public void recordAsking(double question) {
        for(Enumeration enu = stats.elements();enu.hasMoreElements();){
            Statistic s = (Statistic) enu.nextElement();
            s.recordAsking(question);
        }
        WeightHolder wh = this.getWhose();
        wh.getNeuralNetwork().processEvent(new NeuralEvent(wh, NeuralEvent.WIGHTHOLDERPARAMETERMODIFIED));
    }

    public void recordError(double error) {
        for(Enumeration enu = stats.elements();enu.hasMoreElements();){
            Statistic s = (Statistic) enu.nextElement();
            s.recordError(error);
        }
        WeightHolder wh = this.getWhose();
        wh.getNeuralNetwork().processEvent(new NeuralEvent(wh, NeuralEvent.WIGHTHOLDERPARAMETERMODIFIED));
    }

    public void recordGradient(double gradient) {
        for(Enumeration enu = stats.elements();enu.hasMoreElements();){
            Statistic s = (Statistic) enu.nextElement();
            s.recordGradient(gradient);
        }
        WeightHolder wh = this.getWhose();
        wh.getNeuralNetwork().processEvent(new NeuralEvent(wh, NeuralEvent.WIGHTHOLDERPARAMETERMODIFIED));
    }

    public void recordTeaching(double correction) {
        for(Enumeration enu = stats.elements();enu.hasMoreElements();){
            Statistic s = (Statistic) enu.nextElement();
            s.recordTeaching(correction);
        }
        WeightHolder wh = this.getWhose();
        wh.getNeuralNetwork().processEvent(new NeuralEvent(wh, NeuralEvent.WIGHTHOLDERPARAMETERMODIFIED));
    }
    public void nextEpoch(){
        for(Enumeration enu = stats.elements();enu.hasMoreElements();){
            Statistic s = (Statistic) enu.nextElement();
            s.nextEpoch();
        }
        WeightHolder wh = this.getWhose();
        wh.getNeuralNetwork().processEvent(new NeuralEvent(wh, NeuralEvent.WIGHTHOLDERPARAMETERMODIFIED));
    }
}
