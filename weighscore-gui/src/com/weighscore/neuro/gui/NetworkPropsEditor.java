package com.weighscore.neuro.gui;

import java.awt.BorderLayout;
import java.awt.Frame;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JComboBox;
import java.awt.FlowLayout;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JButton;
import com.weighscore.neuro.*;
import javax.swing.SwingConstants;
import java.awt.Rectangle;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class NetworkPropsEditor extends JDialog {
    NeuralNetworkEditable nn = null;
    String[] teachers = null;
    Teacher t = null;
    Frame owner = null;


    JPanel panel1 = new JPanel();
    JLabel jLabel2 = new JLabel();
    JComboBox TeacherCombo = new JComboBox();
    JButton ButtonTchParams = new JButton();
    JPanel jPanel1 = new JPanel();
    FlowLayout flowLayout1 = new FlowLayout();
    JButton ButtonOK = new JButton();
    JButton ButtonCancel = new JButton();


    public NetworkPropsEditor(NeuralNetworkEditable nn, String[] teachers, Frame owner, String title, boolean modal) {
        super(owner, title, modal);
        try {
            this.nn = nn;
            this.teachers = teachers;
            this.owner=owner;


            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            jbInit();

            for(int i=0; i < teachers.length;i++){
                TeacherCombo.addItem(teachers[i]);
            }
            TeacherCombo.setSelectedItem(Util.getPluginNameForClass(nn.getTeacher().getClass()));

            /*NeuralNetworkFactory nf = NeuralNetworkFactory.getNeuralNetworkFactory();
            this.TranslatorCombo.addItem("<No translator>");
            String[] transes = nf.getTranslatorNames();
            for(int i=0; i < transes.length;i++){
                this.TranslatorCombo.addItem(transes[i]);
            }
            String trname = nn.getTranslatorName();
            if(trname==null)
                TranslatorCombo.setSelectedItem("<No translator>");
            else
                TranslatorCombo.setSelectedItem(nn.getTranslatorName());*/

            pack();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private NetworkPropsEditor() {
        this(null, null, new Frame(), "NetworkPropsEditor", false);
    }

    private void jbInit() throws Exception {
        panel1.setLayout(null);
        jLabel2.setHorizontalAlignment(SwingConstants.RIGHT);
        jLabel2.setText("Teacher");
        jLabel2.setBounds(new Rectangle(10, 35, 61, 15));
        TeacherCombo.setBounds(new Rectangle(81, 34, 308, 21));
        ButtonTchParams.setBounds(new Rectangle(392, 35, 100, 21));
        ButtonTchParams.setText("Parameters");
        ButtonTchParams.addActionListener(new
                NetworkPropsEditor_ButtonTchParams_actionAdapter(this));
        jPanel1.setLayout(flowLayout1);
        flowLayout1.setAlignment(FlowLayout.RIGHT);
        flowLayout1.setHgap(20);
        ButtonOK.setText("OK");
        ButtonOK.addActionListener(new NetworkPropsEditor_ButtonOK_actionAdapter(this));
        ButtonCancel.setText("Cancel");
        ButtonCancel.addActionListener(new
                                       NetworkPropsEditor_ButtonCancel_actionAdapter(this));
        panel1.setMinimumSize(new Dimension(515, 84));
        panel1.setPreferredSize(new Dimension(515, 84));
        panel1.add(TeacherCombo);
        panel1.add(jLabel2);
        panel1.add(ButtonTchParams);
        this.getContentPane().add(jPanel1, java.awt.BorderLayout.SOUTH);
        jPanel1.add(ButtonCancel);
        jPanel1.add(ButtonOK);
        this.getContentPane().add(panel1, java.awt.BorderLayout.NORTH);
    }

    public void ButtonCancel_actionPerformed(ActionEvent e) {
        this.dispose();
    }

    public void ButtonOK_actionPerformed(ActionEvent e) {
        String name = (String)this.TeacherCombo.getSelectedItem();
        nn.setTeacher(this.getTeacher(name));
        /*name = (String)this.TranslatorCombo.getSelectedItem();
        if(name.equals("<No translator>"))
            nn.setTranslatorByName(null);
        else
            nn.setTranslatorByName(name);*/
        this.dispose();
    }
    private Teacher getTeacher(String name){
        Teacher tch = nn.getTeacher();
        if(Util.getPluginNameForClass(tch.getClass()).equals(name)){
            return tch;
        }else{
            if(t!=null && Util.getPluginNameForClass(t.getClass()).equals(name)){
                return t;
            }
            else{
                try {
                    tch = (Teacher) Util.getPluginClassForName(name).newInstance();
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
                t = tch;
                return tch;
            }
        }
    }

    public void ButtonTchParams_actionPerformed(ActionEvent e) {
        JDialog proped = new PropertiesEditor((ParameterHolder)this.getTeacher((String)this.TeacherCombo.getSelectedItem()), this.owner, "Edit Statistic", true);
        proped.setSize(450, 200);
        proped.show();
    }

}


class NetworkPropsEditor_ButtonTchParams_actionAdapter implements ActionListener {
    private NetworkPropsEditor adaptee;
    NetworkPropsEditor_ButtonTchParams_actionAdapter(NetworkPropsEditor adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.ButtonTchParams_actionPerformed(e);
    }
}


class NetworkPropsEditor_ButtonOK_actionAdapter implements ActionListener {
    private NetworkPropsEditor adaptee;
    NetworkPropsEditor_ButtonOK_actionAdapter(NetworkPropsEditor adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.ButtonOK_actionPerformed(e);
    }
}


class NetworkPropsEditor_ButtonCancel_actionAdapter implements ActionListener {
    private NetworkPropsEditor adaptee;
    NetworkPropsEditor_ButtonCancel_actionAdapter(NetworkPropsEditor adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.ButtonCancel_actionPerformed(e);
    }
}
