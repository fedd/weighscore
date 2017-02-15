package com.weighscore.neuro.gui;

import java.awt.BorderLayout;
import java.awt.Frame;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTable;
import java.awt.FlowLayout;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.table.*;
import java.util.*;
import com.weighscore.neuro.*;
import java.awt.Point;
import java.awt.Dimension;

public class PropertiesEditor extends JDialog {
    Properties props = null;
    ParameterHolder ph = null;
    String[][] propsArr;
    JPanel panel1 = new JPanel();
    BorderLayout borderLayout1 = new BorderLayout();
    JTable jTableProps = new JTable();
    JPanel jPanel1 = new JPanel();
    FlowLayout flowLayout1 = new FlowLayout();
    JButton jButtonCancel = new JButton();
    JButton jButtonOK = new JButton();
    JScrollPane jScrollPane1 = new JScrollPane();

    private void loadProps(Properties props){
        this.props=props;
        int i=0;
        Enumeration enu = props.propertyNames();
        while(enu.hasMoreElements()){
            i++;
            enu.nextElement();
        }
        this.propsArr = new String[i][];
        i=0;
        enu = props.propertyNames();
        while(enu.hasMoreElements()){
            this.propsArr[i]=new String[2];
            this.propsArr[i][0] = (String) enu.nextElement();
            this.propsArr[i][1] = props.getProperty(this.propsArr[i][0]);
            i++;
        }
    }
    private void loadParams(ParameterHolder ph){
        this.ph=ph;
        String [] pnames = ph.getParameterNames();
        this.propsArr = new String[pnames.length][];
        for(int i=0; i< pnames.length; i++){
            this.propsArr[i]=new String[2];
            this.propsArr[i][0] = pnames[i];
            this.propsArr[i][1] = ph.getParameterAsString(this.propsArr[i][0]);
        }
    }

    public PropertiesEditor(Properties props, Frame owner, String title, boolean modal) {
        super(owner, title, modal);
        loadProps(props);
        init();
    }
    public PropertiesEditor(ParameterHolder ph, Frame owner, String title, boolean modal) {
        super(owner, title, modal);
        loadParams(ph);
        init();
    }
    public void init() {
        try {
            Point p = getOwner().getLocation();
            Dimension d = getOwner().getSize();
            setLocation(p.x + d.width/2 - 175, p.y + d.height/2 - 90);
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            jbInit();
            //pack();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private PropertiesEditor() {
        throw new RuntimeException("Dont call this constr");
    }

    private void jbInit() throws Exception {
        jTableProps.setModel(new AbstractTableModel(){
            public int getColumnCount() {
                return 2;
            }

            public int getRowCount() {
                return propsArr.length;
            }

            public Object getValueAt(int rowIndex, int columnIndex) {
                return propsArr[rowIndex][columnIndex];
            }

            public String getColumnName(int col) {
                if(col==0)
                    return "Property";
                else
                    return "Value";
            }
            public boolean isCellEditable(int row, int col) {
                 if (col == 0) {
                     return false;
                 } else {
                     return true;
                 }
             }
             public void setValueAt(Object value, int row, int col) {
                 propsArr[row][col] = (String)value;
                 fireTableCellUpdated(row, col);
             }

        });
        panel1.setLayout(borderLayout1);
        jPanel1.setLayout(flowLayout1);
        jButtonCancel.setText("Cancel");
        jButtonCancel.addActionListener(new
                PropertiesEditor_jButtonCancel_actionAdapter(this));
        jButtonOK.setText("OK");
        jButtonOK.addActionListener(new
                                    PropertiesEditor_jButtonOK_actionAdapter(this));
        flowLayout1.setAlignment(FlowLayout.RIGHT);
        flowLayout1.setHgap(20);
        this.setModal(true);
        this.setResizable(false);
        getContentPane().add(panel1);
        panel1.add(jPanel1, java.awt.BorderLayout.SOUTH);
        jPanel1.add(jButtonOK);
        jPanel1.add(jButtonCancel);
        panel1.add(jScrollPane1, java.awt.BorderLayout.CENTER);
        jScrollPane1.getViewport().add(jTableProps);
    }

    public void jButtonOK_actionPerformed(ActionEvent e) {
        //this.jTableProps.
        if(props!=null){
            for (int i = 0; i < propsArr.length; i++) {
                props.setProperty(propsArr[i][0], propsArr[i][1]);
            }
        }
        else if(ph!=null){
            for (int i = 0; i < propsArr.length; i++) {
                ph.setParameter(propsArr[i][0], propsArr[i][1]);
            }
        }
        this.dispose();
    }

    public void jButtonCancel_actionPerformed(ActionEvent e) {
        this.dispose();
    }
}


class PropertiesEditor_jButtonCancel_actionAdapter implements ActionListener {
    private PropertiesEditor adaptee;
    PropertiesEditor_jButtonCancel_actionAdapter(PropertiesEditor adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jButtonCancel_actionPerformed(e);
    }
}


class PropertiesEditor_jButtonOK_actionAdapter implements ActionListener {
    private PropertiesEditor adaptee;
    PropertiesEditor_jButtonOK_actionAdapter(PropertiesEditor adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jButtonOK_actionPerformed(e);
    }
}
