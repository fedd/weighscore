package com.weighscore.neuro.gui;

import java.awt.BorderLayout;
import java.awt.Frame;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JTextArea;
import java.awt.FlowLayout;
import com.weighscore.neuro.*;
import java.awt.*;
import java.io.*;
import javax.swing.JScrollPane;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TranEd extends JDialog {
    JPanel panel1 = new JPanel();
    BorderLayout borderLayout1 = new BorderLayout();
    JPanel jPanel1 = new JPanel();
    JButton jButton1 = new JButton();
    JButton jButton2 = new JButton();
    JTextArea jTextArea1 = new JTextArea();
    FlowLayout flowLayout1 = new FlowLayout();
    JScrollPane jScrollPane1 = new JScrollPane();

    public TranEd(Frame owner, String title, boolean modal) {
        super(owner, title, modal);
        try {
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            jbInit();
            pack();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public TranEd() {
        this(new Frame(), "TranEd", false);
    }

    private void jbInit() throws Exception {
        panel1.setLayout(borderLayout1);
        jButton1.setText("Cancel");
        jButton1.addActionListener(new TranEd_jButton1_actionAdapter(this));
        jButton2.setText("OK");
        jButton2.addActionListener(new TranEd_jButton2_actionAdapter(this));
        jPanel1.setLayout(flowLayout1);
        flowLayout1.setAlignment(FlowLayout.RIGHT);
        jScrollPane1.setPreferredSize(new Dimension(200, 300));
        getContentPane().add(panel1);
        panel1.add(jPanel1, java.awt.BorderLayout.SOUTH);
        jPanel1.add(jButton2);
        jPanel1.add(jButton1);
        panel1.add(jScrollPane1, java.awt.BorderLayout.CENTER);
        jScrollPane1.getViewport().add(jTextArea1);
        Window owner = this.getOwner();
        if(owner instanceof Editor){
            NeuralNetwork nn = ((Editor) owner).nn;
            if(nn.getTranslator()!=null){
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                nn.outputTranslatorDefinition(baos);
                jTextArea1.setText(baos.toString());
            }
            else{
                jTextArea1.setText("");
            }
        }
    }

    public void jButton1_actionPerformed(ActionEvent e) {
        this.dispose();
    }

    public void jButton2_actionPerformed(ActionEvent e) {
        Translator tr = new Translator();
        InputStream is = new ByteArrayInputStream(jTextArea1.getText().getBytes());
        com.weighscore.neuro.plugins.XmlFileOrigin.initTransByInput(tr, is);
        ((NeuralNetworkEditable)((Editor) this.getOwner()).nn).setTranslator(tr);
        this.dispose();
    }
}


class TranEd_jButton2_actionAdapter implements ActionListener {
    private TranEd adaptee;
    TranEd_jButton2_actionAdapter(TranEd adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jButton2_actionPerformed(e);
    }
}


class TranEd_jButton1_actionAdapter implements ActionListener {
    private TranEd adaptee;
    TranEd_jButton1_actionAdapter(TranEd adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jButton1_actionPerformed(e);
    }
}
