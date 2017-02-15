package com.weighscore.neuro.gui;

import java.util.Properties;
import com.weighscore.neuro.*;
import java.io.*;
import javax.swing.*;

import java.awt.Toolkit;
import java.awt.Dimension;

public class JdbcAction {
    private static JdbcActionThread t = null;

    protected static ChartView chartview = null;

    protected static void jdbcprocess(NeuralNetwork nn, Properties ppp, String chartvalues, boolean ask, boolean test, PrintStream output){
        if(JdbcAction.t==null){

            BufferedReader prin = null;
            PrintStream prout = null;
            String chv[] = null;

            if(chartvalues!=null && chartvalues.trim().length()!=0){
                try {
                    PipedInputStream in = new PipedInputStream();
                    prin = new BufferedReader(new InputStreamReader(in));
                    prout = new PrintStream(new PipedOutputStream(
                            in));
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                chv = ("Count " + chartvalues).split("\\s+");
            }


            JdbcAction.t = new JdbcActionThread(nn, ppp, ask, test, output, prout, chv);
            JdbcAction.t.start();


            if(prin!=null){
                String title = nn.getName() + "'s Teaching Progress Chart";
                if(chartview!=null)
                    chartview.reinit(title, prin, chv, 0);
                else{
                    chartview = new ChartView(title, prin, chv, 0);

                    Dimension screenSize = Toolkit.getDefaultToolkit().
                                           getScreenSize();
                    Dimension frameSize = chartview.getSize();
                    if (frameSize.height > screenSize.height) {
                        frameSize.height = screenSize.height;
                    }
                    if (frameSize.width > screenSize.width) {
                        frameSize.width = screenSize.width;
                    }
                    chartview.setLocation((screenSize.width - frameSize.width -
                                           60),
                                          (screenSize.height - frameSize.height -
                                           40));
                    chartview.setIconImage((new ImageIcon(Editor.class.getResource("arrange.png"))).getImage());
                }
                chartview.setVisible(true);
            }

        }else{
            JOptionPane.showMessageDialog(null, "The JDBC process is active", "Error", JOptionPane.ERROR_MESSAGE);
        }
   }
   /*protected static void updateChart(){
       if(chartview!=null){
           chartview.addParams();
       }
   }*/

   protected static class JdbcActionThread extends Thread{
       NeuralNetwork nn;
       Properties ppp;
       boolean ask;
       boolean test;
       PrintStream output;
       PrintStream progressout;
       String[] progressparams;

       JdbcActionThread(NeuralNetwork nn, Properties ppp, boolean ask, boolean test, PrintStream output, PrintStream progressout, String[] progressparams){
           super("JdbcProcessThread");
           this.nn = nn;
           this.ppp = ppp;
           this.ask = ask;
           this.test = test;
           this.output = output;
           this.progressout = progressout;
           this.progressparams = progressparams;
       }
       public void run(){
           try{
               nnt.jdbcprocess(nn, ppp, ask, test, output, progressparams, progressout);
               /*if(chartview!=null)
                   chartview.panelizeChart();*/
           }
           catch(Exception e){
               JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
               //e.printStackTrace();
               if(chartview!=null){
                   chartview.dispose();
                   chartview=null;
               }
           }
           JdbcAction.t = null;
       }
   }
}
