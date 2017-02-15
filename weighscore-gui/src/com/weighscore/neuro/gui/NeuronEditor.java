package com.weighscore.neuro.gui;

import java.awt.BorderLayout;
import java.awt.Frame;

import java.awt.Rectangle;
import javax.swing.*;
import java.awt.FlowLayout;
import com.weighscore.neuro.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Dimension;

public class NeuronEditor extends JDialog {
    WeightHolder n = null;
    String[] acts = null;
    String[] stats = null;
    Frame owner = null;
    Activation a = null;
    Statistic s = null;


    JPanel panel1 = new JPanel();
    JLabel jLabel1 = new JLabel();
    JTextField ThresholdField = new JTextField();
    JLabel jLabel2 = new JLabel();
    JComboBox ActsCombo = new JComboBox();
    JButton ButtonStParams = new JButton();
    JLabel jLabel3 = new JLabel();
    JButton ButtonActParams = new JButton();
    JPanel jPanel1 = new JPanel();
    FlowLayout flowLayout1 = new FlowLayout();
    JButton ButtonOK = new JButton();
    JButton ButtonCancel = new JButton();
    JTextField StatField = new JTextField();
    public NeuronEditor(WeightHolder neuron, String[] activators, Frame owner, String title, boolean modal) {
        super(owner, title, modal);
        try {
            this.n = neuron;
            this.owner=owner;
            acts = activators;

            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            jbInit();

            if(this.n instanceof Neuron){
                ActsCombo.setEnabled(true);
                this.ButtonActParams.setEnabled(true);
                Neuron nnn = (Neuron) this.n;
                for (int i = 0; i < acts.length; i++) {
                    ActsCombo.addItem(acts[i]);
                }
                ActsCombo.setSelectedItem(Util.getPluginNameForClass(nnn.getActivation().
                        getClass()));
            }
            else{
                ActsCombo.setEnabled(false);
                this.ButtonActParams.setEnabled(false);
            }
            StringBuffer sb = new StringBuffer();
            String[] ss = n.getMultiStats().getStatisticNames();
            for(int i=0; i<ss.length;i++){
                if(i!=0)
                    sb.append(' ');
                sb.append(ss[i]);
            }

            this.StatField.setText(sb.toString());
            this.ThresholdField.setText(String.valueOf( n.getWeight()));
            pack();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private NeuronEditor() {
        this(null, null,  new Frame(), "NeuronEditor", false);
    }

    private void jbInit() throws Exception {
        panel1.setLayout(null);
        jLabel1.setHorizontalAlignment(SwingConstants.RIGHT);
        jLabel1.setText("Threshold");
        jLabel1.setBounds(new Rectangle(6, 13, 64, 15));
        ThresholdField.setText("0.0");
        ThresholdField.setHorizontalAlignment(SwingConstants.TRAILING);
        ThresholdField.setBounds(new Rectangle(81, 8, 130, 21));
        jLabel2.setHorizontalAlignment(SwingConstants.RIGHT);
        jLabel2.setText("Activator");
        jLabel2.setBounds(new Rectangle(10, 35, 61, 15));
        ActsCombo.setBounds(new Rectangle(81, 34, 308, 21));
        ButtonStParams.setBounds(new Rectangle(392, 57, 100, 21));
        ButtonStParams.setText("Parameters");
        ButtonStParams.addActionListener(new
                NeuronEditor_ButtonStParams_actionAdapter(this));
        jLabel3.setHorizontalAlignment(SwingConstants.RIGHT);
        jLabel3.setText("Statistic");
        jLabel3.setBounds(new Rectangle(2, 61, 70, 15));
        ButtonActParams.setBounds(new Rectangle(392, 33, 100, 21));
        ButtonActParams.setText("Parameters");
        ButtonActParams.addActionListener(new
                NeuronEditor_ButtonStatParams_actionAdapter(this));
        jPanel1.setLayout(flowLayout1);
        flowLayout1.setAlignment(FlowLayout.RIGHT);
        flowLayout1.setHgap(20);
        ButtonOK.setText("OK");
        ButtonOK.addActionListener(new NeuronEditor_ButtonOK_actionAdapter(this));
        ButtonCancel.setText("Cancel");
        ButtonCancel.addActionListener(new
                                       NeuronEditor_ButtonCancel_actionAdapter(this));
        panel1.setMinimumSize(new Dimension(515, 84));
        panel1.setPreferredSize(new Dimension(515, 84));
        StatField.setBounds(new Rectangle(81, 58, 305, 21));
        panel1.add(ThresholdField);
        panel1.add(ActsCombo);
        panel1.add(jLabel2);
        panel1.add(jLabel1);
        panel1.add(jLabel3);
        panel1.add(ButtonActParams);
        panel1.add(ButtonStParams);
        panel1.add(StatField);
        this.getContentPane().add(jPanel1, java.awt.BorderLayout.SOUTH);
        jPanel1.add(ButtonCancel);
        jPanel1.add(ButtonOK);
        this.getContentPane().add(panel1, java.awt.BorderLayout.NORTH);
    }

    public void ButtonCancel_actionPerformed(ActionEvent e) {
        this.dispose();
    }

    public void ButtonOK_actionPerformed(ActionEvent e) {
        try{
            ((NeuralNetworkEditable) n.getNeuralNetwork()).setWeightOrThreshold(
                    n, Double.parseDouble(this.ThresholdField.getText()));
            String name;
            if(n instanceof Neuron){
                Neuron nnn = (Neuron) n;
                name = (String)this.ActsCombo.getSelectedItem();
                ((NeuralNetworkEditable) n.getNeuralNetwork()).setActivation(nnn,
                        this.getActivation(name));
            }
            name = this.StatField.getText();

            String[]newsts = name.split("\\s+");
            MultiStats ms = n.getMultiStats();
            String[]oldsts = ms.getStatisticNames();
            // removing
olds:       for(int old=0; old<oldsts.length;old++){
                // go through current stats
                for(int neu=0; neu<newsts.length;neu++){
                    if(oldsts[old].equals(newsts[neu])){
                        // if met old among new, continue
                        continue olds;
                    }
                }
                // if old was not among new, remove
                ms.removeStatistic(oldsts[old]);
            }
            for(int neu=0; neu<newsts.length;neu++){
                if(newsts[neu].length()!=0)
                    ms.addStatistic(newsts[neu]);
            }

        }
        catch(ClassCastException ee){
        }
        this.dispose();
    }

    private Activation getActivation(String name){
        if(n instanceof Neuron){
            Activation act = ((Neuron)n).getActivation();
            if (Util.getPluginNameForClass(act.getClass()).equals(name)) {
                return act;
            } else {
                if (a != null &&
                    Util.getPluginNameForClass(a.getClass()).equals(name)) {
                    return a;
                } else {
                    try {
                        act = (Activation) Util.getPluginClassForName(name).
                              newInstance();
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                    a = act;
                    return act;
                }
            }
        }else return null;
    }
    public void ButtonStParams_actionPerformed(ActionEvent e) {
        JDialog proped = new PropertiesEditor(n.getMultiStats(), this.owner, "Edit Statistic", true);
        proped.setSize(450, 200);
        proped.show();
    }

    public void ButtonActParams_actionPerformed(ActionEvent e) {
        JDialog proped = new PropertiesEditor((ParameterHolder)this.getActivation((String)this.ActsCombo.getSelectedItem()), this.owner, "Edit Statistic", true);
        proped.setSize(450, 200);
        proped.show();
    }
}


class NeuronEditor_ButtonStParams_actionAdapter implements ActionListener {
    private NeuronEditor adaptee;
    NeuronEditor_ButtonStParams_actionAdapter(NeuronEditor adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.ButtonStParams_actionPerformed(e);
    }
}


class NeuronEditor_ButtonStatParams_actionAdapter implements ActionListener {
    private NeuronEditor adaptee;
    NeuronEditor_ButtonStatParams_actionAdapter(NeuronEditor adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {

        adaptee.ButtonActParams_actionPerformed(e);
    }
}


class NeuronEditor_ButtonOK_actionAdapter implements ActionListener {
    private NeuronEditor adaptee;
    NeuronEditor_ButtonOK_actionAdapter(NeuronEditor adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.ButtonOK_actionPerformed(e);
    }
}


class NeuronEditor_ButtonCancel_actionAdapter implements ActionListener {
    private NeuronEditor adaptee;
    NeuronEditor_ButtonCancel_actionAdapter(NeuronEditor adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.ButtonCancel_actionPerformed(e);
    }
}
