package com.weighscore.neuro.gui;

import java.awt.BorderLayout;
import java.awt.Frame;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JButton;
import java.awt.FlowLayout;
import javax.swing.JTable;
import com.weighscore.neuro.*;
import java.util.*;

public class TranslatorEditor extends JDialog {
    Translator tr = null;
    Vector trtb = new Vector();
    Vector trtbnms = new Vector();

    JPanel panel1 = new JPanel();
    BorderLayout borderLayout1 = new BorderLayout();
    JPanel jPanel1 = new JPanel();
    JButton SaveButton = new JButton();
    JButton CancelButton = new JButton();
    FlowLayout flowLayout1 = new FlowLayout();
    JTable jTable1 = new JTable();
    public TranslatorEditor(Translator translator, Frame owner, String title, boolean modal) {
        super(owner, title, modal);
        try {
            tr = translator;
            int fcnt = tr.getAnswerFieldsCount()+tr.getAskFieldsCount();
            trtbnms.add("Field");
            trtbnms.add("I/O");
            trtbnms.add("Type");
            trtbnms.add("Divisor");
            trtbnms.add("Item");
            trtbnms.add("Activator");
            trtbnms.add("Value");
            trtbnms.add("Min");
            trtbnms.add("Max");

            for(int i = 0; i<fcnt; i++){
                Vector entry;

                String fname = tr.getFieldName(i);
                Boolean inout = new Boolean(tr.isAskField(i));
                Boolean ispass = new Boolean(tr.isPass(i));
                Double divisor = null;
                String item = null;
                Double val = null, min = null, max = null;

                Integer act;

                if(ispass.booleanValue()){
                    divisor = new Double(tr.getDivisor(i));
                    act = new Integer(tr.getActivatedNeuronInOutIndex(i, 0) + 1);
                    entry = new Vector();

                    entry.add(fname);
                    entry.add(inout);
                    entry.add(ispass);
                    entry.add(divisor);
                    entry.add(item);
                    entry.add(act);
                    entry.add(val);
                    entry.add(min);
                    entry.add(max);

                    trtb.add(entry);
                }
                else{
                    int acts = tr.getActivatedNeuronInOutIndicesCount(i);
                    for(int j = 0; j<acts; j++){
                        entry = new Vector();

                        item = tr.getItem(i, j);
                        act = new Integer(tr.getActivatedNeuronInOutIndex(i, j) + 1);
                        val = new Double(tr.getActivatingValue(i, j));
                        min = new Double(tr.getActivatingMinValue(i, j));
                        max = new Double(tr.getActivatingMaxValue(i, j));

                        entry.add(fname);
                        entry.add(inout);
                        entry.add(ispass);
                        entry.add(divisor);

                        entry.add(item);
                        entry.add(act);
                        entry.add(val);
                        entry.add(min);
                        entry.add(max);

                        trtb.add(entry);
                    }
                }
            }
            jTable1 = new JTable(this.trtb, trtbnms);




            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            jbInit();
            pack();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private TranslatorEditor() {
        this(null, new Frame(), "TranslatorEditor", false);
    }

    private void jbInit() throws Exception {
        panel1.setLayout(borderLayout1);
        SaveButton.setText("Save");
        CancelButton.setText("Cancel");
        jPanel1.setLayout(flowLayout1);
        flowLayout1.setAlignment(FlowLayout.RIGHT);
        flowLayout1.setHgap(20);
        borderLayout1.setHgap(10);
        borderLayout1.setVgap(10);
        getContentPane().add(panel1);
        panel1.add(jPanel1, java.awt.BorderLayout.SOUTH);
        jPanel1.add(CancelButton);
        jPanel1.add(SaveButton);
        panel1.add(jTable1, java.awt.BorderLayout.CENTER);
    }
}
