package com.weighscore.neuro.gui;

import java.awt.BorderLayout;
import java.awt.Frame;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JTable;
import com.weighscore.neuro.*;
import javax.swing.table.AbstractTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JScrollPane;
import javax.swing.*;
import java.awt.Dimension;
import java.awt.FlowLayout;

public class ActionForm extends JDialog {
    JPanel panel1 = new JPanel();
    BorderLayout borderLayout1 = new BorderLayout();
    JPanel jPanel1 = new JPanel();
    JButton Cancel = new JButton();
    JButton AskButton = new JButton();
    JButton TestButton = new JButton();
    JButton Teach = new JButton();
    JButton TeachButton = new JButton();
    JPanel jPanel2 = new JPanel();
    JTable jTable1 = new JTable();
    BorderLayout borderLayout2 = new BorderLayout();

    private static ActionForm.ActionThread t = null;
    NeuralNetwork nn;
    Object toRepaint;
    //Signal[] curSig;
    String[][] data = new String[0][0];
    JScrollPane jScrollPane1 = new JScrollPane();
    JTextField VisualDelayField = new JTextField();
    JLabel jLabel1 = new JLabel();
    FlowLayout flowLayout1 = new FlowLayout();

    public ActionForm(NeuralNetwork nn, JPanel toRepaint, Frame owner, String title, boolean modal) {
        super(owner, title, modal);
        this.nn = nn;
        this.toRepaint = toRepaint;
        //this.curSig=currentSignal;

        try {
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            jbInit();
            pack();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public ActionForm() {
        this(null, null, new Frame(), "ActionForm", false);
    }


    private void loadData(){
        int ins, outs;
        ins = nn.getAskSize();
        outs = nn.getAnswerSize();
        data = new String[ins+outs][2];

        for(int i=0; i<ins; i++){
            data[i][0] = nn.getAskFieldName(i);
            data[i][1] = "";
        }
        for(int i=0; i<outs; i++){
            data[i+ins][0] = nn.getAnswerFieldName(i);
            data[i+ins][1] = "";
        }
    }


    private void jbInit() throws Exception {
        loadData();
        jTable1.setModel(new AskTableModel(data));
        panel1.setLayout(borderLayout1);
        Cancel.setText("Close");
        Cancel.addActionListener(new ActionForm_Cancel_actionAdapter(this));
        AskButton.setText("Ask");
        AskButton.addActionListener(new ActionForm_AskButton_actionAdapter(this));
        TestButton.setText("Test");
        TestButton.addActionListener(new ActionForm_TestButton_actionAdapter(this));
        Teach.setText("jButton4");
        TeachButton.setText("Teach");
        TeachButton.addActionListener(new ActionForm_TeachButton_actionAdapter(this));
        jPanel2.setLayout(borderLayout2);
        jLabel1.setText("Visualize Propagation Delay, ms");
        VisualDelayField.setPreferredSize(new Dimension(60, 21));
        VisualDelayField.setText(((Editor)this.getOwner()).viewproperties.getProperty("propagationVisualiseDelay", "0"));
        VisualDelayField.setHorizontalAlignment(SwingConstants.RIGHT);
        jPanel1.setLayout(flowLayout1);
        flowLayout1.setAlignment(FlowLayout.RIGHT);
        getContentPane().add(panel1);
        panel1.add(jPanel1, java.awt.BorderLayout.SOUTH);
        jPanel1.add(jLabel1);
        jPanel1.add(VisualDelayField);
        jPanel1.add(TeachButton);
        jPanel1.add(TestButton);
        jPanel1.add(AskButton);
        jPanel1.add(Cancel);
        panel1.add(jPanel2, java.awt.BorderLayout.CENTER);
        jPanel2.add(jScrollPane1, java.awt.BorderLayout.CENTER);
        jScrollPane1.getViewport().add(jTable1);
    }

    public void AskButton_actionPerformed(ActionEvent e) {
        //this.ask();
        if(t==null){
            t = new ActionThread(this, true, false);
            t.start();
        }else{
            JOptionPane.showMessageDialog(null, "Action process is active, please wait", "Error", JOptionPane.ERROR_MESSAGE);
        }

    }

    String[] ans;
    void ask(){
        ((Editor)this.getOwner()).viewproperties.setProperty("propagationVisualiseDelay", this.VisualDelayField.getText());

        String[] q = new String[nn.getAskSize()];
        for(int i=0; i<q.length; i++){
            q[i] = data[i][1];
        }
        ((AskTableModel)jTable1.getModel()).setError(false);
        try{
            ans = nn.ask(q);
            /*if(t==null){
                t = new ActionThread(this, q, true, false);
                t.start();
            }else{
                JOptionPane.showMessageDialog(null,
                        "Action process is active, please wait", "Error",
                                              JOptionPane.ERROR_MESSAGE);
                return;
            }*/

            for (int i = 0; i < ans.length; i++) {
                jTable1.getModel().setValueAt(ans[i], i + q.length, 1);
            }
        }
        catch(NeuralException ee){
            JOptionPane.showMessageDialog(this, ee.toString() + ee.getMessage(), "Neural Error", JOptionPane.ERROR_MESSAGE);
        }
        ((JPanel)this.toRepaint).repaint();
    }

    public void Cancel_actionPerformed(ActionEvent e) {
        this.dispose();
    }

    double[] err;
    void testteach(boolean teach){
        ((Editor)this.getOwner()).viewproperties.setProperty("propagationVisualiseDelay", this.VisualDelayField.getText());

        String[] qa = new String[data.length];
        for(int i=0; i<qa.length; i++){
            qa[i] = data[i][1];
        }

        try{
            if (teach)
                err = nn.teach(qa);
            else
                err = nn.test(qa);
            /*if(t==null){
                t = new ActionThread(this, qa, false, teach);
                t.start();
            }else{
                JOptionPane.showMessageDialog(null,
                        "Action process is active, please wait", "Error",
                                              JOptionPane.ERROR_MESSAGE);
                return;
            }*/


            ((AskTableModel) jTable1.getModel()).setError(true);

            int a = nn.getAskSize();
            for (int i = 0; i < err.length; i++) {
                jTable1.getModel().setValueAt(String.valueOf(qa[a]), a, 2);
                jTable1.getModel().setValueAt(String.valueOf(err[i]), a, 3);
                a++;
            }
        }
        catch(NeuralException e){
            JOptionPane.showMessageDialog(this, e.getMessage(), "Neural Error", JOptionPane.ERROR_MESSAGE);
        }
        ((JPanel)this.toRepaint).repaint();
    }

    public void TestButton_actionPerformed(ActionEvent e) {
        //this.testteach(false);
        if(t==null){
            t = new ActionThread(this, false, false);
            t.start();
        }else{
            JOptionPane.showMessageDialog(null, "Action process is active, please wait", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void TeachButton_actionPerformed(ActionEvent e) {
        //this.testteach(true);
        if(t==null){
            t = new ActionThread(this, false, true);
            t.start();
        }else{
            JOptionPane.showMessageDialog(null,
                    "Action process is active, please wait", "Error",
                                          JOptionPane.ERROR_MESSAGE);
        }
    }
    protected static class ActionThread extends Thread{
        private boolean ask;
        private boolean teach;
        private ActionForm f;
        //private String[] queAns;

        ActionThread(ActionForm f, /*String[] quAn, */boolean ask, boolean teach){
            this.f=f;
            this.ask = ask;
            this.teach = teach;
            //this.queAns=quAn;
        }
        public void run(){
            try{
                if(ask)
                    f.ask();
                    //f.ans = f.nn.ask(queAns);
                else
                    f.testteach(teach);
                    /*if(teach)
                        f.err = f.nn.teach(queAns);
                    else
                        f.err = f.nn.test(queAns);*/
            }
            catch(Exception e){
                JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
            t = null;
        }
        protected void finalize(){
            t = null;
        }
     }
}


class ActionForm_TeachButton_actionAdapter implements ActionListener {
    private ActionForm adaptee;
    ActionForm_TeachButton_actionAdapter(ActionForm adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.TeachButton_actionPerformed(e);
    }
}


class ActionForm_TestButton_actionAdapter implements ActionListener {
    private ActionForm adaptee;
    ActionForm_TestButton_actionAdapter(ActionForm adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.TestButton_actionPerformed(e);
    }
}


class ActionForm_Cancel_actionAdapter implements ActionListener {
    private ActionForm adaptee;
    ActionForm_Cancel_actionAdapter(ActionForm adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.Cancel_actionPerformed(e);
    }
}


class ActionForm_AskButton_actionAdapter implements ActionListener {
    private ActionForm adaptee;
    ActionForm_AskButton_actionAdapter(ActionForm adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.AskButton_actionPerformed(e);
    }
}

class AskTableModel extends AbstractTableModel{
    String[][]data;
    AskTableModel(String data[][]) {
        this.data=data;
    }

    //private boolean isError = false;
    private int cCnt = 2;

    public void setError(boolean isError){
        //this.isError = isError;
        if(isError){
            this.cCnt = 4;
        }
        else{
            this.cCnt = 2;
        }
        for(int i=0; i<data.length; i++){
            String fn = data[i][0];
            String qa = data[i][1];
            data[i] = new String[cCnt];
            data[i][0] = fn;
            data[i][1] = qa;
        }
        fireTableStructureChanged();
    }

    public int getColumnCount() {
        return cCnt;
    }

    public int getRowCount() {
        return data.length;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        return data[rowIndex][columnIndex];
    }

    public String getColumnName(int col) {
        if (col == 0)
            return "Field";
        else if (col == 1)
            return "Question/Answer";
        else if (col == 2)
            return "Given Answer";
        else
            return "Error";
    }

    public boolean isCellEditable(int row, int col) {
        if (col == 0 || col == 2 || col == 3) {
            return false;
        } else {
            return true;
        }
    }

    public void setValueAt(Object value, int row, int col) {
        data[row][col] = (String) value;
        fireTableCellUpdated(row, col);

    }


}
