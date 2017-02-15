package com.weighscore.neuro.plugins;


import com.weighscore.neuro.*;

public class ConjugateGradientTeacher extends Teacher {

    private double[] previousDirection=null;
    private int epochsTought=0;
    private double averagePerformance=0.0;

    private double getBeta(double[] g, double[]gprev){

        // chislitel
        double chislitel = 0;
        for(int i=0; i<g.length;i++){
            chislitel = chislitel + g[i] * (g[i] - gprev[i]);
        }
        double znamenatel = 0;
        for(int i=0; i<g.length;i++){
            znamenatel = znamenatel + gprev[i] * gprev[i];
        }
        double beta = chislitel/znamenatel;
        return beta;
    }



    public boolean nextEpoch(){


        // teach really
        double beta=0.0;
        double[] g;
        double[] gprev;
        double[] p;

        WeightHolder[] wh = this.getNeuralNetwork().getNeuronsAndSynapses();
        if(this.previousDirection==null || this.epochsTought>wh.length){
            this.previousDirection=new double[wh.length];
            this.epochsTought=0;

            g = new double[wh.length];
            p = new double[wh.length];
            for(int i=0;i<p.length;i++){
                p[i]= -1 * g[i];
                this.previousDirection[i]=p[i];
            }
        }
        else{

            g = new double[wh.length];
            gprev = new double[wh.length];
            for (int i = 0; i < gprev.length; i++) {
                try {
                    g[i] = ((AverageGradientStatistic) wh[i].getMultiStats().getStatistic("AverageGradientStatistic")).epochCurAgvGradient;//.getDoubleParameter("epochCurAgvGradient");
                           //epochCurAgvGradient;
                    gprev[i] = ((AverageGradientStatistic) wh[i].getMultiStats().getStatistic("AverageGradientStatistic")).epochLastAgvGradient;//.getDoubleParameter("epochLastAgvGradient");
                               //epochLastAgvGradient;
                } catch (ClassCastException e) {
                    throw new NeuralException(
                            "Last average gradient unaware statistic used at " +
                            i +
                            ". Use AverageGradientStatistic or its children with ConjugateGradientTeacher");
                }
            }

            // find direction
            beta = getBeta(g, gprev);
            p = new double[wh.length];

            for (int i = 0; i < p.length; i++) {
                p[i] = beta * this.previousDirection[i] - g[i];
                this.previousDirection[i] = p[i];
            }
        }
        // find mimimum in the direction
        double min = this.getMinimumBrent(0,1,0.001, 30,p,g);

        // move aling the direction
        for(int i=0;i<wh.length;i++){
            this.getNeuralNetwork().setWeightOrThreshold(wh[i], wh[i].getWeight() + p[i]*min);
            //wh[i].setWeight(wh[i].getWeight() + p[i]*min);
        }

        // proceed statistic
        for (int i = 0; i < wh.length; i++) {
            try {
                wh[i].getMultiStats().nextEpoch();
            } catch (ClassCastException e) {
                throw new NeuralException(
                        "Last average gradient unaware statistic used at " + i +
                        ". Use AverageGradientStatistic or its children with ConjugateGradientTeacher");
            }
        }
        this.averagePerformance=0.0;

        return true;
    }






    public boolean teach(double[] error, Signal signal) {
        // !!!!!???????!!!!!!!!?????????!!!!!!!!!??!?????????!!!!!!!!!?????
        double[][] gradient = signal.goBack(error);

        NeuralNetworkLocal na = super.getNeuralNetwork();
        Neuron[] n = na.getNeurons();
        //signal.getGradient();

        for(int i=0; i<gradient.length; i++){

            n[i].getMultiStats().recordTeaching(gradient[i][0] * -1);

            Synapse[] s = n[i].getInSynapses();

            for(int j = 1; j<gradient[i].length; j++){
                s[j-1].getMultiStats().recordTeaching(gradient[i][j] * -1);
            }
        }
        // compute average epoch's performance (=error)
//        double curperf = this.getPerformance(signal.getNetworkError());
//        long cnt = ((AverageGradientStatistic)na.getOutputNeurons()[0].getMultiStats().getStatistic("AverageGradientStatistic")).getLongParameter("epochCasesCnt");//  .epochCasesCnt;
//        this.averagePerformance = (averagePerformance * (cnt - 1) + curperf) /
//                                  cnt;
        return true;
    }





    /* ***********************************************************************
    Оптимизация функции методом Брента

    Источник: http://alglib.sources.ru/extremums/brent.php

    Входные параметры:
        a           -   левая граница отрезка, на котором ищется минимум
        b           -   правая граница отрезка, на котором ищется минимум
        Epsilon     -   абсолютная точность, с которой находится расположение
                        минимума

    Выходные параметры:
        XMin        -   точка найденного минимума

    Результат:
        значение функции в найденном минимуме
    *************************************************************************/
    private double getMinimumBrent(double a, double b, double epsilon, int maxIterations, double[] direction, double[] gradient)
    {
        //double result;
        double ia;
        double ib;
        double bx;
        double d=0.0;
        double e;
        double etemp;
        double fu;
        double fv;
        double fw;
        double fx;
        int iter;
        double p;
        double q;
        double r;
        double u;
        double v;
        double w;
        double x;
        double xm;
        double cgold;

        cgold = 0.3819660;
        bx = 0.5*(a+b);
        if( a<b )
        {
            ia = a;
        }
        else
        {
            ia = b;
        }
        if( a>b )
        {
            ib = a;
        }
        else
        {
            ib = b;
        }
        v = bx;
        w = v;
        x = v;
        e = 0.0;
        fx = f(x, direction, gradient);
        fv = fx;
        fw = fx;
        for(iter = 1; iter <= maxIterations; iter++)
        {
            xm = 0.5*(ia+ib);
            if( Math.abs(x-xm)<=epsilon*2-0.5*(ib-ia) )
            {
                break;
            }
            if( Math.abs(e)>epsilon )
            {
                r = (x-w)*(fx-fv);
                q = (x-v)*(fx-fw);
                p = (x-v)*q-(x-w)*r;
                q = 2*(q-r);
                if( q>0 )
                {
                    p = -p;
                }
                q = Math.abs(q);
                etemp = e;
                e = d;
                if( !(Math.abs(p)>=Math.abs(0.5*q*etemp)||p<=q*(ia-x)||p>=q*(ib-x)) )
                {
                    d = p/q;
                    u = x+d;
                    if( u-ia<epsilon*2||ib-u<epsilon*2 )
                    {
                        d = mysign(epsilon, xm-x);
                    }
                }
                else
                {
                    if( x>=xm )
                    {
                        e = ia-x;
                    }
                    else
                    {
                        e = ib-x;
                    }
                    d = cgold*e;
                }
            }
            else
            {
                if( x>=xm )
                {
                    e = ia-x;
                }
                else
                {
                    e = ib-x;
                }
                d = cgold*e;
            }
            if( Math.abs(d)>=epsilon )
            {
                u = x+d;
            }
            else
            {
                u = x+mysign(epsilon, d);
            }
            fu = f(u, direction, gradient);
            if( fu<=fx )
            {
                if( u>=x )
                {
                    ia = x;
                }
                else
                {
                    ib = x;
                }
                v = w;
                fv = fw;
                w = x;
                fw = fx;
                x = u;
                fx = fu;
            }
            else
            {
                if( u<x )
                {
                    ia = u;
                }
                else
                {
                    ib = u;
                }
                if( fu<=fw||w==x )
                {
                    v = w;
                    fv = fw;
                    w = u;
                    fw = fu;
                }
                else
                {
                    if( fu<=fv||v==x||v==2 )
                    {
                        v = u;
                        fv = fu;
                    }
                }
            }
        }
        //xmin = x;
        //double result = fx;
        return x; //result;
    }


    private double mysign(double a, double b)
    {
        double result;

        if( b>0 )
        {
            result = Math.abs(a);
        }
        else
        {
            result = -Math.abs(a);
        }
        return result;
    }

    private double f(double x, double[] direction, double [] gradient){
        // this function finds E(w+p) = E(w) + grad(w)T * direction * x


        // get E(w)
        double ew = this.averagePerformance;

        double gtdx = 0.0;
        for(int i=0; i< gradient.length;i++){
            gtdx = gtdx + gradient[i] * direction[i] * x;
        }

        double res = ew + gtdx;

        return res;
    }


    private double getPerformance(double[] err){
        // this finds current E(w)
        double Se2=0.0;
        for(int i=0; i<err.length;i++){
            Se2 = Se2 + Math.pow(err[i], 2);
        }
        Se2 = Se2/2;
        return Se2;
    }



}
