package com.weighscore.neuro.gui;

import java.awt.*;
import org.jfree.data.xy.*;
import org.jfree.chart.*;
import org.jfree.chart.plot.*;
//import com.weighscore.neuro.*;
import java.awt.image.*;
import javax.swing.*;
import java.io.*;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: Vsetech Company</p>
 *
 * @author Fyodor Kravchenko
 * @version 1.0
 */
public class ChartView extends JFrame {
    private long updateFreq = 10*1000;// 10 секунд
    private ChartView me;
    private XYSeriesCollection  dataset;
    private XYSeries[] series;
    //private NeuralNetwork net;
    private BufferedReader chartdata;
    private String[] params;
    //private int count = 0;
    JFreeChart chart = null;

    JPanel imagepanel;
    JLabel imagelabel;

    long lastupdated = 0;

    public static void main(String[] args) {
        try{
            // arguments: progressdatafile [refresh seconds]

            File f = new File(args[0]);
            FileReader fis = null;

            try {
                fis = new FileReader(f);
            } catch (FileNotFoundException ex) {
                System.err.println("File not found - " + args[0]);
                System.exit( -1);
            }

            BufferedReader br = new BufferedReader(fis) {
                private long breakWhenIdleMillis = 10 * 1000; // break if 10 seconds idle
                public String readLine() throws IOException {
                    String s;
                    long lastNullMillis = System.currentTimeMillis();
                    while ((s = super.readLine()) == null) {
                        if ((lastNullMillis + breakWhenIdleMillis) <
                            System.currentTimeMillis()) {
                            break;
                        }
                    }
                    return s;
                }
            };

            // find out the title - first line of file
            String title = null;
            try {
                title = br.readLine();
            } catch (IOException ex1) {
                System.err.println("File couldn't be read - " + args[0]);
                System.exit( -1);
            }

            // find out the parameter names - 4-th line
            String params = null;
            try {
                br.readLine();
                br.readLine();
                params = br.readLine();

                //System.out.println(params);
            } catch (IOException ex2) {
            }
            String[] parr = params.split("\\s+");

            long secs = 0;
            try {
                secs = Long.parseLong(args[1]);
                secs = secs * 1000;
            } catch (ArrayIndexOutOfBoundsException e) {
            }
            ChartView chartview = new ChartView(title, br, parr, secs);
            Dimension screenSize = Toolkit.getDefaultToolkit().
                                   getScreenSize();
            Dimension frameSize = chartview.getSize();
            if (frameSize.height > screenSize.height) {
                frameSize.height = screenSize.height;
            }
            if (frameSize.width > screenSize.width) {
                frameSize.width = screenSize.width;
            }
            chartview.setLocation((screenSize.width - frameSize.width)/2,
                                  (screenSize.height - frameSize.height)/2);
            chartview.setIconImage((new ImageIcon(ChartView.class.getResource("arrange.png"))).getImage());
            chartview.setVisible(true);


        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("Usage: ChartView progressdatafile [refreshseconds]");
            System.exit( -1);
        }
    }


    public void reinit(String title, BufferedReader chartdata, String[] params, long refreshFrequency){
        setTitle(title);
        if(refreshFrequency!=0)
            this.updateFreq = refreshFrequency;

        /*if(this.net!=null){
            if(this.net.getName().compareTo(net.getName())!=0){
                this.count=0;
            }
        }

        this.net = net;*/
        this.chartdata=chartdata;

        lastupdated = 0;
        this.params=params;
        this.dataset = new XYSeriesCollection();
        this.series = new XYSeries[params.length-1];
        for (int i = 1; i < params.length; i++) {
            this.series[i-1] = new XYSeries(params[i]);
            //dataset.addSeries(series[i]);
        }

        imagepanel = new JPanel(new BorderLayout());
        imagelabel = new JLabel();
        imagepanel.add(imagelabel, BorderLayout.CENTER);
        imagepanel.setSize(this.getWidth(), this.getHeight());
        setContentPane(imagepanel);
        imagelabel.setSize(imagepanel.getWidth(), imagepanel.getHeight());

        dataset.removeAllSeries();
        chart = null;

        updateImageLabel();

        drawChart();
    }

    public ChartView(String title, BufferedReader chartdata, String[] params, long refreshFrequency) {

        me=this;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
        int h, w;
        w = (int)((double)screensize.width * 0.75);
        h = (int)((double)screensize.height * 0.75);
        this.setSize(w, h);

        reinit(title, chartdata, params, refreshFrequency);
    }

    private void regeneratechart(){
        for (int i = 1; i < params.length; i++) {
            dataset.addSeries(series[i-1]);
        }
        chart = ChartFactory.createXYLineChart(
            this.getTitle(),
            "Teach Actions",
            "Value",
            dataset,
            PlotOrientation.VERTICAL,
            true,
            true,
            false
                );
        chart.setBackgroundPaint(new Color(245, 245, 255));
    }

    private void updateImageLabel(){
        regeneratechart();

        BufferedImage image = chart.createBufferedImage(imagepanel.getWidth(), imagepanel.getHeight());
        imagelabel.setIcon(new ImageIcon(image));

        dataset.removeAllSeries();
        chart = null;
    }


    public void drawChart(){

        new Thread("DrawChartThread"){
            public void run(){
                String nextline;
                try {
                    boolean skipBroken = false;
                    while ((nextline = chartdata.readLine()) != null) {
                        if(skipBroken){
                            skipBroken=false;
                            continue;
                        }

                        String[] data = nextline.split("\\s+");
                        if(data.length != params.length){
                            skipBroken=true;
                            continue;
                        }
                        try{
                            long count = Long.parseLong(data[0]);
                            for (int i = 1; i < params.length; i++) {
                                XYSeries xys = series[i - 1];
                                double val = Double.parseDouble(data[i]);
                                xys.add(count, val);
                            }
                        }
                        catch(NumberFormatException e){
                            skipBroken=true;
                            continue;
                        }

                        long cur = System.currentTimeMillis();
                        if (cur - lastupdated > updateFreq) {
                            updateImageLabel();
                            lastupdated = cur;
                        }
                    }

                    chartdata.close();
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(me, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }

                panelizeChart();
            }
        }.start();

    }

    public void panelizeChart(){
        regeneratechart();

        ChartPanel chartpanel = new ChartPanel(chart);
        setContentPane(chartpanel);
        imagelabel=null;
        imagepanel=null;
        this.validate();
    }

    public void dispose(){
        try{
            JdbcAction.chartview = null;
        }
        catch(NoClassDefFoundError e){}

        dataset.removeAllSeries();
        chart = null;
        super.dispose();
    }
}
