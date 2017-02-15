package com.weighscore.neuro;

import java.lang.reflect.*;
import java.util.*;

/**
 * The class for holding and working with parameters, which are defined as
 * the descent class' public fields.
 * It's methods are used to serialize neural network objects.
 *
 * @author Fyodor Kravchenko
 * @version 2.0
 */public class ParameterHolder {

    private String[] parameterNames=null;

    private Vector getParameterNamesVect(){
        Vector v = new Vector();
        Class c = this.getClass();
        while(c!=null){
            Field[] flds = c.getDeclaredFields();
            for (int i = 0; i < flds.length; i++) {
                int mod = flds[i].getModifiers();
                //if (Modifier.isProtected(mod)) {
                if (Modifier.isPublic(mod)) {
                    v.add(flds[i].getName());
                }
            }
            c = c.getSuperclass();
        }

        return v;
    }

    /**
     * Returns an array of parameter names
     *
     * @return Array of strings containing the names of the parameters
     */
    public String[] getParameterNames(){
        if (this.parameterNames==null){
            Vector v = this.getParameterNamesVect();
            String[] nms = new String[v.size()];
            v.toArray(nms);
            this.parameterNames=nms;
        }

        return this.parameterNames;
    }

    private Field getField(String name){
        try {
            Field f=null;
            Class c = this.getClass();
            while (c!=null){
                try {
                    f = c.getDeclaredField(name);
                    int mod = f.getModifiers();
                    //if (Modifier.isProtected(mod)) {
                    if (Modifier.isPublic(mod)) {
                        break;
                    }
                } catch (NoSuchFieldException ex1) {
                    //ex1.printStackTrace();
                }
                f=null;
                c = c.getSuperclass();
            }
            if (f!=null)
                return f;
            else
                throw new NeuralException("No such parameter - " + name);
        } catch (Exception ex) {
            throw new NeuralException("Couldn't get parameter " + name, ex);
        }
    }

    /**
     * Returns the value of the double parameter by name
     *
     * @param parameterName The name of the parameter
     * @return The value of the parameter
     */
    public double getDoubleParameter(String parameterName){
        Field f = this.getField(parameterName);
        try {
            return f.getDouble(this);
        } catch (Exception ex) {
            throw new NeuralException("Couldn't get double parameter " + parameterName);
        }
    }

    /**
     * Returns the value of the long parameter by name
     *
     * @param parameterName The name of the parameter
     * @return The value of the parameter
     */
    public long getLongParameter(String parameterName){
        Field f = this.getField(parameterName);
        try {
            return f.getLong(this);
        } catch (Exception ex) {
            throw new NeuralException("Couldn't get long parameter " + parameterName);
        }
    }

    /**
     * Returns the value of the int parameter by name
     *
     * @param parameterName The name of the parameter
     * @return The value of the parameter
     */
    public int getIntegerParameter(String parameterName){
        Field f = this.getField(parameterName);
        try {
            return f.getInt(this);
        } catch (Exception ex) {
            throw new NeuralException("Couldn't get int parameter " + parameterName);
        }
    }

    /**
     * Returns the value of the parameter by name, as object
     *
     * @param parameterName The name of the parameter
     * @return The value of the parameter
     */
    public Object getParameter(String parameterName){
        Field f = this.getField(parameterName);
        try {
            return f.get(this);
        } catch (Exception ex) {
            throw new NeuralException("Couldn't get object parameter " + parameterName);
        }
    }

    /**
     * Returns the value of the parameter by name, as string
     *
     * @param parameterName The name of the parameter
     * @return The value of the parameter
     */
    public String getParameterAsString(String parameterName){
        Field f = this.getField(parameterName);
        try {
            String p = f.get(this).toString();
            return p;
        } catch (Exception ex) {
            throw new NeuralException("Couldn't get parameter " + parameterName + " as string");
        }
    }

    /**
     * Returns the java type of the named parameter, as string
     *
     * @param parameterName The name of the parameter
     * @return The type of the parameter as string
     */
    public String getParameterType(String parameterName){
        Field f = this.getField(parameterName);
        Class c = f.getType();
        return c.getName();
    }

    /**
     * Sets the value of the named parameter
     *
     * @param parameterName The name of the parameter
     * @param parameter The value of the parameter as string
     */
    public void setParameter(String parameterName, String parameter){
        try {
            Field f = this.getField(parameterName);
            if (f == null) {
                throw new NeuralException("No such parameter - " + parameterName);
            }
            Class c = f.getType();
            if (c.getName().equals("double")) {
                f.setDouble(this, Double.parseDouble(parameter));
            } else if (c.getName().equals("long")) {
                f.setLong(this, Long.parseLong(parameter));
            } else if (c.getName().equals("int")) {
                f.setInt(this, Integer.parseInt(parameter));
            } else
                throw new NeuralException("Work with the type " + c.getName() +
                                          " is not implemented");

        } catch (Exception ex) {
            throw new NeuralException("Couldn't set parameter " + parameterName + ": " + ex.getMessage(), ex);
        }
    }
}
