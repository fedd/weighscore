package com.weighscore.neuro.gui;

import com.weighscore.neuro.*;
import com.weighscore.neuro.gui.graph.*;

import edu.uci.ics.jung.graph.*;
import edu.uci.ics.jung.graph.decorators.*;
import edu.uci.ics.jung.visualization.*;
import edu.uci.ics.jung.visualization.control.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JToolBar;
import javax.swing.JButton;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JToggleButton;
import javax.swing.ButtonGroup;
import javax.swing.*;
import java.io.*;
import java.util.*;
import com.weighscore.neuro.plugins.*;

//import javax.swing.text.*;

public class Editor extends JFrame {

    private final static String APPNAME = "Neural Network Graphical Tool";

    private final static String CONFFILE = "nng.config";

    private final static String DBCONFFILEENDING = ".nngdb.config";
    private final static String VIEWCONFFILEENDING = ".nngview.config";

    private boolean needToRename = false;

    //public long propagationVisualiseDelay = 0;

    //private static String visColor = "weight";
    //private static String visLabel = "name";

    boolean noJdbc=false;
    boolean noSaveas=false;


    NeuralNetwork nn;
    //NumberFormat nfr = NumberFormat.getInstance();

    Properties properties;
    Properties viewproperties, defaultvp;
    Properties dbproperties, defaultdbp;
    Graph gr;
    Layout lo;
    PluggableRenderer pr;
    VisualizationViewer vv;
    Dimension dim;
    SettableVertexLocationFunction nl;
    EditingModalGraphMouse gm;
    GraphMousePlugin mp;
    GraphZoomScrollPane sp;

    JPanel contentPane;

    Signal currentSignal = null;
    BorderLayout borderLayout1 = new BorderLayout();
    JMenuBar jMenuBar1 = new JMenuBar();
    JMenu jMenuNetwork = new JMenu();
    JMenuItem jMenuFileExit = new JMenuItem();
    JMenu jMenuHelp = new JMenu();
    JMenuItem jMenuHelpAbout = new JMenuItem();
    JToolBar jToolBar = new JToolBar();
    JButton openButton = new JButton();
    ImageIcon image1 = new ImageIcon(com.weighscore.neuro.gui.Editor.class.
                                     getResource("open.png"));
    //ImageIcon image2 = new ImageIcon(com.weighscore.neuro.gui.Editor.class.
    //                                 getResource("closeFile.png"));
    //ImageIcon image3 = new ImageIcon(com.weighscore.neuro.gui.Editor.class.
    //                                 getResource("help.png"));
    JLabel statusBar = new JLabel();
    JButton saveButton = new JButton();
    JButton arrangeButton = new JButton();
    JToggleButton drawB = new JToggleButton();
    JToggleButton pickB = new JToggleButton();
    JToggleButton moveB = new JToggleButton();
    ButtonGroup buttonGroup1 = new ButtonGroup();
    JMenuItem jMenuNew = new JMenuItem();
    JMenuItem jMenuOpen = new JMenuItem();
    JMenuItem jMenuSave = new JMenuItem();
    JMenuItem jMenuSaveAs = new JMenuItem();
    JMenu jMenuEdit = new JMenu();
    JMenuItem jMenuNNProps = new JMenuItem();
    JMenu jMenuAction = new JMenu();
    JMenuItem jMenuDBTeach = new JMenuItem();
    JMenuItem jMenuDBTest = new JMenuItem();
    JMenuItem jMenuDBAsk = new JMenuItem();
    JMenuItem jMenuAsk = new JMenuItem();
    JMenuItem jMenuNeuralConfig = new JMenuItem();
    JMenuItem jMenuEditTran = new JMenuItem();
    JMenuItem jMenuDBConfig = new JMenuItem();
    JMenuItem jMenuViewConfig = new JMenuItem();

    StringBursterPrintStream statusBarOut = new StringBursterPrintStream(statusBar);
    JMenuItem jMenuStopDBAction = new JMenuItem();
    JButton newNNButton = new JButton();

    private Properties loadProps(Properties defaults, String filename){
        Properties props = new Properties(defaults);
        try{
            FileInputStream fis = new FileInputStream(filename);
            props.load(fis);
        }
        catch(Exception e){
            if(this.needToRename)
                System.err.println(filename + " not found, apply defaults");
        }
        return props;
    }

    public Editor() {
            setDefaultCloseOperation(EXIT_ON_CLOSE);
            try {
                jbInit();
            } catch (Exception ex) {
            }
        }


    public Editor(boolean flag) {
        try {
            setDefaultCloseOperation(EXIT_ON_CLOSE);

            // default view props
            this.defaultvp = new Properties();
            this.defaultvp.setProperty("color", "weight");
            this.defaultvp.setProperty("label", "name");
            this.defaultvp.setProperty("tooltip", "name activation");
            this.defaultvp.setProperty("betweenLayers", "100");
            this.defaultvp.setProperty("betweenNeuronsInLayer", "50");
            this.defaultvp.setProperty("sideMargin", "50");
            this.defaultvp.setProperty("topMargin", "50");
            this.defaultvp.setProperty("propagationVisualiseDelay", "0");
            this.defaultvp.setProperty("chartValues", "");

            // default database props
            this.defaultdbp = new Properties();
            this.defaultdbp.setProperty("target", "");
            this.defaultdbp.setProperty("runs", "");
            this.defaultdbp.setProperty("dbdriver", "");
            this.defaultdbp.setProperty("dburl", "");
            this.defaultdbp.setProperty("dbuser", "");
            this.defaultdbp.setProperty("dbpassword", "");
            this.defaultdbp.setProperty("dbtable", "");
            this.defaultdbp.setProperty("dbquery", "");
            this.defaultdbp.setProperty("dbidfields", "");

            // load properties
            Properties defaults = new Properties();
            defaults.setProperty("network", "new");
            String acts;
            try{
                Class a = Threshold.class;
                acts = "Sigmoid Linear Threshold";
            }
            catch(NoClassDefFoundError e){
                acts = "Sigmoid Linear";
            }
            defaults.setProperty("activator", acts);
            //defaults.setProperty("statistic", "com.weighscore.neuro.plugins.MomentumStatistic com.weighscore.neuro.plugins.LastErrorStatistic com.weighscore.neuro.plugins.FullStatistic");
            defaults.setProperty("teacher", "SimpleTeacher EmpiricTeacher");
            this.properties = this.loadProps(defaults, CONFFILE);

            String toOpen = properties.getProperty("network");

            try{
                Class a = JdbcAction.class;
            }
            catch(NoClassDefFoundError e){
                this.noJdbc=true;
            }
            try{
                Class a = NeuralNetworkFactory.class;
            }
            catch(NoClassDefFoundError e){
                this.noSaveas=true;
                toOpen=NeuralNetwork.DEFAULTNETWORKNAME;
            }

            if(!toOpen.equals("new")){
                try{
                    nn = NeuralNetwork.getNeuralNetwork(toOpen);
                    setTitle(APPNAME + " - " + nn.getName());
                    this.needToRename=false;
                }
                catch(NeuralException e){
                    //e.printStackTrace();
                    toOpen="new";
                }
            }
            if(toOpen.equals("new"))
                nn = this.createNN();


            loadNeuralNetwork();

            jbInit();
        } catch (Exception exception) {
            exception.printStackTrace();
            System.exit(0);
        }
    }

    /**
     * Component initialization.
     *
     * @throws java.lang.Exception
     */
    private void jbInit() throws Exception {
        contentPane = (JPanel) getContentPane();
        contentPane.setLayout(borderLayout1);
        setSize(new Dimension(400, 300));
        //setTitle(APPNAME + " - <New>");
        statusBar.setText(" ");
        jMenuNetwork.setText("Network");
        jMenuFileExit.setText("Exit");
        jMenuFileExit.addActionListener(new Editor_jMenuFileExit_ActionAdapter(this));
        jMenuHelp.setText("Help");
        jMenuHelpAbout.setText("About");
        jMenuHelpAbout.addActionListener(new
                                         Editor_jMenuHelpAbout_ActionAdapter(this));
        saveButton.setToolTipText("Save Neural Network");
        saveButton.setIcon(new ImageIcon(Editor.class.getResource("save.png")));
        saveButton.addActionListener(new Editor_jsaveButton_actionAdapter(this));
        arrangeButton.addActionListener(new Editor_arrangeButton_actionAdapter(this));
        drawB.addActionListener(new Editor_jToggleButton1_actionAdapter(this));
        pickB.setToolTipText("Pick Mode");
        pickB.setIcon(new ImageIcon(Editor.class.getResource("pick.png")));
        pickB.addActionListener(new Editor_pickB_actionAdapter(this));
        moveB.addActionListener(new Editor_moveB_actionAdapter(this));
        jMenuNew.setAction(null);
        jMenuNew.setText("New");
        jMenuNew.addActionListener(new Editor_jMenuNew_actionAdapter(this));
        jMenuOpen.setText("Open");
        jMenuOpen.addActionListener(new Editor_jMenuOpen_actionAdapter(this));
        jMenuSave.setText("Save");
        jMenuSave.addActionListener(new Editor_jMenuSave_actionAdapter(this));
        jMenuSaveAs.setText("SaveAs");
        jMenuSaveAs.addActionListener(new Editor_jMenuSaveAs_actionAdapter(this));
        jMenuEdit.setText("Edit");
        jMenuNNProps.setText("Network Properties");
        jMenuNNProps.addActionListener(new Editor_jMenuNNProps_actionAdapter(this));
        jMenuNNProps.setEnabled(this.nn instanceof NeuralNetworkEditable);
        jMenuAction.setText("Action");
        jMenuDBTeach.setText("Database Teach");
        jMenuDBTeach.addActionListener(new Editor_jMenuDBTeach_actionAdapter(this));
        jMenuDBTest.setText("Database Test");
        jMenuDBTest.addActionListener(new Editor_jMenuDBTest_actionAdapter(this));
        jMenuDBAsk.setText("Database Record");
        jMenuDBAsk.addActionListener(new Editor_jMenuDBAsk_actionAdapter(this));
        jMenuAsk.setText("Console Action");
        jMenuAsk.addActionListener(new Editor_jMenuAsk_actionAdapter(this));
        jMenuNeuralConfig.setActionCommand("Neural Config");
        jMenuNeuralConfig.setText("System Properties");
        jMenuNeuralConfig.addActionListener(new
                Editor_jMenuNeuralConfig_actionAdapter(this));
        jMenuEditTran.setText("Edit Translator");
        jMenuEditTran.addActionListener(new Editor_jMenuEditTran_actionAdapter (this));
        jMenuDBConfig.setText("Network Database Options");
        jMenuDBConfig.addActionListener(new Editor_jMenuDBConfig_actionAdapter(this));
        jMenuViewConfig.setText("Network View Options");
        jMenuViewConfig.addActionListener(new
                                          Editor_jMenuViewConfig_actionAdapter(this));
        jMenuStopDBAction.setText("Stop Database Action");
        jMenuStopDBAction.addActionListener(new
                Editor_jMenuStopDBAction_actionAdapter(this));
        arrangeButton.setToolTipText("Arrange Neurons");
        arrangeButton.setIcon(new ImageIcon(Editor.class.getResource(
                "arrange.png")));
        drawB.setToolTipText("Draw Mode");
        drawB.setIcon(new ImageIcon(Editor.class.getResource("draw.png")));
        moveB.setToolTipText("Move Mode");
        moveB.setIcon(new ImageIcon(Editor.class.getResource("move.png")));
        newNNButton.setToolTipText("Create Neural Network");
        newNNButton.setIcon(new ImageIcon(Editor.class.getResource("new.png")));
        newNNButton.addActionListener(new Editor_newNNButton_actionAdapter(this));
        openButton.addActionListener(new Editor_openButton_actionAdapter(this));
        jMenuBar1.add(jMenuNetwork);
        jMenuBar1.add(jMenuEdit);
        jMenuBar1.add(jMenuAction);
        jMenuNetwork.add(jMenuNew);
        jMenuNetwork.add(jMenuOpen);
        jMenuNetwork.add(jMenuSave);
        jMenuNetwork.add(jMenuSaveAs);
        jMenuNetwork.addSeparator();
        jMenuNetwork.add(jMenuNeuralConfig);
        jMenuNetwork.addSeparator();
        jMenuNetwork.add(jMenuFileExit);
        jMenuBar1.add(jMenuHelp);
        jMenuHelp.add(jMenuHelpAbout);
        setJMenuBar(jMenuBar1);
        openButton.setIcon(new ImageIcon(Editor.class.getResource("open.png")));
        openButton.setToolTipText("Open Neural Network");
        jToolBar.add(newNNButton);
        jToolBar.add(openButton);
        jToolBar.add(saveButton);
        jToolBar.add(arrangeButton);
        jToolBar.add(drawB);
        jToolBar.add(pickB);
        jToolBar.add(moveB);
        contentPane.add(jToolBar, BorderLayout.NORTH);
        contentPane.add(statusBar, BorderLayout.SOUTH);
        contentPane.add(sp, java.awt.BorderLayout.CENTER);
        buttonGroup1.add(drawB);
        buttonGroup1.add(pickB);
        buttonGroup1.add(moveB);
        jMenuEdit.add(jMenuNNProps);
        jMenuEdit.add(jMenuEditTran);
        jMenuEdit.addSeparator();
        jMenuEdit.add(jMenuViewConfig);
        jMenuEdit.add(jMenuDBConfig);
        jMenuAction.add(jMenuDBTeach);
        jMenuAction.add(jMenuDBTest);
        jMenuAction.add(jMenuDBAsk);
        jMenuAction.add(jMenuStopDBAction);
        jMenuAction.addSeparator();
        jMenuAction.add(jMenuAsk);
        if(this.noSaveas){
            this.jMenuNeuralConfig.setEnabled(false);
            this.jMenuOpen.setEnabled(false);
            this.jMenuSaveAs.setEnabled(false);
            this.jMenuNew.setEnabled(false);

            this.newNNButton.setEnabled(false);
            this.openButton.setEnabled(false);
        }
        if(this.noJdbc){
            this.jMenuStopDBAction.setEnabled(false);
            this.jMenuDBAsk.setEnabled(false);
            this.jMenuDBTeach.setEnabled(false);
            this.jMenuDBTest.setEnabled(false);
            this.jMenuDBConfig.setEnabled(false);
        }
    }

    /**
     * File | Exit action performed.
     *
     * @param actionEvent ActionEvent
     */
    void jMenuFileExit_actionPerformed(ActionEvent actionEvent) {
        storeProps(this.properties, CONFFILE);

        System.exit(0);
    }

    private void storeProps(Properties props, String filename){
        try{
            FileOutputStream fos = new FileOutputStream(filename, false);
            props.store(fos, null);
        }
        catch(Exception e){
            throw new NeuralException("Couldn't store properties in " + filename, e);
        }
    }


    /**
     * Help | About action performed.
     *
     * @param actionEvent ActionEvent
     */
    void jMenuHelpAbout_actionPerformed(ActionEvent actionEvent) {
        Editor_AboutBox dlg = new Editor_AboutBox(this);
        Dimension dlgSize = dlg.getPreferredSize();
        Dimension frmSize = getSize();
        Point loc = getLocation();
        dlg.setLocation((frmSize.width - dlgSize.width) / 2 + loc.x,
                        (frmSize.height - dlgSize.height) / 2 + loc.y);
        dlg.setModal(true);
        dlg.pack();
        dlg.setVisible(true);
    }

    public void jsaveButton_actionPerformed(ActionEvent e) {
        this.jMenuSave_actionPerformed(e);
    }

    public void arrangeButton_actionPerformed(ActionEvent e) {
        ((NeuronLocationFunction)this.nl).reinit(
            Integer.parseInt(viewproperties.getProperty("betweenLayers")),
            Integer.parseInt(viewproperties.getProperty("betweenNeuronsInLayer")),
            Integer.parseInt(viewproperties.getProperty("sideMargin")),
            Integer.parseInt(viewproperties.getProperty("topMargin")));
        this.vv.restart();

    }

    public void jToggleButton1_actionPerformed(ActionEvent e) {
        gm.setMode(ModalGraphMouse.Mode.EDITING);
    }

    public void pickB_actionPerformed(ActionEvent e) {
        gm.setMode(ModalGraphMouse.Mode.PICKING);
    }

    public void moveB_actionPerformed(ActionEvent e) {
        gm.setMode(ModalGraphMouse.Mode.TRANSFORMING);
    }

    private NeuralNetwork createNN(){
        NeuralNetwork nnet;
        String nname = "New";
        while(true){
            try{
                nnet = NeuralNetwork.createNeuralNetwork(nname);
                //nnet = NeuralNetworkFactory.getNeuralNetworkFactory().
                //       createNeuralNetwork(nname);
                this.needToRename=true;
                break;
            }
            catch(NeuralNetworkCreationException e){}
            nname = nname + "~";
        }
        this.needToRename=true;
        setTitle(APPNAME + " - <New>");
        return nnet;
    }

    private void loadNeuralNetwork(){
        NeuralEventAdapter nea = new NeuralEventAdapter(this);
        nn.setNeuralEventListener(nea);
        this.viewproperties = this.loadProps(this.defaultvp, nn.getName()+this.VIEWCONFFILEENDING);
        this.dbproperties = this.loadProps(this.defaultdbp, nn.getName()+this.DBCONFFILEENDING);
        gr = new NeuralNetworkGraph(nn);
        nl = new NeuronLocationFunction(
            (NeuralNetworkGraph)gr,
            Integer.parseInt(viewproperties.getProperty("betweenLayers")),
            Integer.parseInt(viewproperties.getProperty("betweenNeuronsInLayer")),
            Integer.parseInt(viewproperties.getProperty("sideMargin")),
            Integer.parseInt(viewproperties.getProperty("topMargin")));
        pr = new PluggableRenderer();
        lo = new StaticLayout(gr);
        dim = new Dimension(((NeuronLocationFunction)nl).getWidth(), ((NeuronLocationFunction)nl).getHeight());
        ((StaticLayout)lo).initialize(dim, nl);
        vv = new VisualizationViewer(lo, pr, dim);
        gm = new EditingModalGraphMouse(){
            protected void loadPlugins() {
                pickingPlugin = new PickingGraphMousePlugin();
                animatedPickingPlugin = new AnimatedPickingGraphMousePlugin();
                translatingPlugin = new TranslatingGraphMousePlugin(InputEvent.BUTTON1_MASK);
                scalingPlugin = new CrossoverScalingGraphMousePlugin(in, out);
                rotatingPlugin = null;// new RotatingGraphMousePlugin();
                shearingPlugin = null; //new ShearingGraphMousePlugin();
                editingPlugin = new NeuralEGMPlugin();

                add(scalingPlugin);
                setMode(Mode.EDITING);
            }
            public void setVertexLocations(SettableVertexLocationFunction vertexLocations) {
                ((NeuralEGMPlugin)editingPlugin).setVertexLocations(vertexLocations);
            }
        };
        drawB.setSelected(true);
        mp = new NeuralEPGMPlugin(nl, this);
        sp = new GraphZoomScrollPane(vv);
        vv.setPickSupport(new ShapePickSupport());
        vv.setBackground(new Color(245, 245, 255));
        pr.setEdgeShapeFunction(new EdgeShape.QuadCurve());
        GraphLabelRenderer glr = new DefaultGraphLabelRenderer(Color.GRAY, Color.GRAY, false);
        pr.setGraphLabelRenderer(glr);
        pr.setVertexStringer(new VertexStringer() {
            public String getLabel(ArchetypeVertex v) {
                Neuron n = ((NeuronVertex) v).getNeuron();
                String s;
                s = getWHlabel(n, false);
                //String atr = viewproperties.getProperty("label").trim();
                //if(atr.length()>0)
                //    s = Util.AttributeGetter.getStringAttribute(atr.split("\\s+"), nn, n, currentSignal, 3, " - ");
                //else
                //    s = null;

                //if(n.isInput()){
                //    if(s != null && s.length()>0)
                //        s = nn.getInputIndex(n) + " - " + s;
                //    else
                //        s = String.valueOf(nn.getInputIndex(n));
                //}
                //if(n.isOutput()){
                //    if(s != null && s.length()>0)
                //        s = nn.getOutputIndex(n) + " - " + s;
                //    else
                //        s = String.valueOf(nn.getOutputIndex(n));
                //}
                return s;
            }
        });
        pr.setEdgeStringer(new EdgeStringer() {
            public String getLabel(ArchetypeEdge e) {
                Synapse syn = ((SynapseEdge) e).getSynapse();
                String s;
                s = getWHlabel(syn, false);
                //String atr = viewproperties.getProperty("label").trim();
                //if(atr.length()>0)
                //    s = Util.AttributeGetter.getStringAttribute(atr.split("\\s+"), nn, syn, currentSignal, 3, " - ");
                //else
                //    s = null;
                return s;
            }
        });
        pr.setEdgePaintFunction(new EdgePaintFunction() {
            public Paint getDrawPaint(Edge e) {
                Synapse w = ((SynapseEdge) e).getSynapse();
                return getWHpaint(w);
                //double d = Util.AttributeGetter.getDoubleAttribute(viewproperties.getProperty("color"), nn, w, currentSignal);
                //Paint p = getPaint( d, 0f, 0.7f);
                //return p;
            }

            public Paint getFillPaint(Edge e) {
                Color c = new Color(0, true);
                return c;
            }
        });
        /*pr.setEdgeShapeFunction(new EdgeShapeFunction(){
            public Shape getShape(Edge e) {
                Shape s = new
                return getSShape(n.isInput(), n.isOutput());;
            }
        });*/
        pr.setVertexShapeFunction(new VertexShapeFunction(){
            public Shape getShape(Vertex v) {
                Neuron n = ((NeuronVertex)v).getNeuron();
                Shape sh = getNShape(n.isInput(), n.isOutput());
                /*int[] xs, ys;
                if(n.isInput()){
                    if(n.isOutput()){
                        // romb
                        xs = new int[4];
                        ys = new int[4];
                        xs[0]=0;
                        ys[0]=-13;
                        xs[1]=13;
                        ys[1]=0;
                        xs[2]=0;
                        ys[2]=13;
                        xs[3]=-13;
                        ys[3]=0;
                    }else{
                        // right arrow triangle
                        xs = new int[3];
                        ys = new int[3];
                        xs[0]=-10;
                        ys[0]=-12;
                        xs[1]=10;
                        ys[1]=0;
                        xs[2]=-10;
                        ys[2]=12;
                    }
                }else{
                    if(n.isOutput()){
                        // left arrow triangle
                        xs = new int[3];
                        ys = new int[3];
                        xs[0]=10;
                        ys[0]=-12;
                        xs[1]=10;
                        ys[1]=12;
                        xs[2]=-10;
                        ys[2]=0;
                    }else{
                        // quadrat
                        xs = new int[4];
                        ys = new int[4];
                        xs[0]=-10;
                        ys[0]=-10;
                        xs[1]=10;
                        ys[1]=-10;
                        xs[2]=10;
                        ys[2]=10;
                        xs[3]=-10;
                        ys[3]=10;
                    }
                }
                Shape sh = new Polygon(xs, ys, xs.length);*/
                return sh;
            }
        });
        pr.setVertexStrokeFunction(new VertexStrokeFunction(){
            public Stroke getStroke(Vertex v) {
                Stroke s;
                if(Integer.parseInt(viewproperties.getProperty("propagationVisualiseDelay"))>0 && currentSignal!=null){
                    Neuron neu = ((NeuronVertex) v).getNeuron();
                    if(currentSignal.isExcited(neu) || currentSignal.isExcitedBack(neu))
                        return new BasicStroke(3);
                }
                return new BasicStroke(1);
            }
        });
        pr.setVertexPaintFunction(new VertexPaintFunction() {
            public Paint getFillPaint(Vertex v) {
                Neuron w = ((NeuronVertex)v).getNeuron();
                return getWHpaint(w);
                //double d = Util.AttributeGetter.getDoubleAttribute(viewproperties.getProperty("color"), nn, w, currentSignal);
                //Paint p = getPaint(d * -1, 0f, 1f);
                //return p;
            }
            public Paint getDrawPaint(Vertex v) {
                if(Integer.parseInt(viewproperties.getProperty("propagationVisualiseDelay"))>0 && currentSignal!=null){
                    Neuron neu = ((NeuronVertex) v).getNeuron();
                    float b = currentSignal.getBackExcitedness(neu);
                    float r = (b>0?0:currentSignal.getExcitedness(neu));
                    b = (r>0?0:b);
                    //System.out.println("" + neu.getIndex() + "\t" + r + "\t" + b);
                    return new Color(r, 0, b);

                    /*if(currentSignal.isExcited(neu))
                        return Color.RED;
                    else if(currentSignal.isExcitedBack(neu))
                        return Color.BLUE;*/
                }
                return Color.BLACK;
            }
        });
        pr.setEdgeStrokeFunction(new EdgeStrokeFunction(){
            public Stroke getStroke(Edge e) {
                Stroke s = new BasicStroke(1);
                return s;
            }
        });
        vv.setToolTipFunction(new ToolTipFunction(){
            public String getToolTipText(MouseEvent event) {
                return null;
            }
            public String getToolTipText(Vertex v) {
                Neuron n = ((NeuronVertex) v).getNeuron();
                String s;
                s = getWHlabel(n, true);
                //String atr = viewproperties.getProperty("tooltip").trim();
                //if(atr.length()>0)
                //    s = Util.AttributeGetter.getStringAttribute(atr.split("\\s+"), nn, n, currentSignal, 3, " - ");
                //else
                //    s = null;
                return s;
            }
            public String getToolTipText(Edge e) {
                Synapse syn = ((SynapseEdge) e).getSynapse();
                String s;
                s = getWHlabel(syn, true);
                //String atr = viewproperties.getProperty("tooltip").trim();
                //if(atr.length()>0)
                //    s = Util.AttributeGetter.getStringAttribute(atr.split("\\s+"), nn, syn, currentSignal, 3, " - ");
                //else
                //    s = null;
                return s;
            }
        });
        gm.setVertexLocations(nl);
        vv.setGraphMouse(gm);
        gm.add(mp);
        gm.setMode(ModalGraphMouse.Mode.EDITING);
    }

    // shapes
    Shape innerShape=null;
    Shape outputShape=null;
    Shape inputShape=null;
    Shape inoutShape=null;
    Shape getNShape(boolean input, boolean output) {
        Shape toret = null;
        int[] xs=null, ys=null;
        if(input){
            if(output){
                if(inoutShape==null){
                    // romb
                    xs = new int[4];
                    ys = new int[4];
                    xs[0] = 0;
                    ys[0] = -13;
                    xs[1] = 13;
                    ys[1] = 0;
                    xs[2] = 0;
                    ys[2] = 13;
                    xs[3] = -13;
                    ys[3] = 0;
                }
                else
                    toret=inoutShape;
            }else{
                if(inputShape==null){
                    // right arrow triangle
                    xs = new int[3];
                    ys = new int[3];
                    xs[0] = -10;
                    ys[0] = -12;
                    xs[1] = 10;
                    ys[1] = 0;
                    xs[2] = -10;
                    ys[2] = 12;
                }
                else
                    toret = inputShape;
            }
        }else{
            if(output){
                if(outputShape==null){
                    // left arrow triangle
                    xs = new int[3];
                    ys = new int[3];
                    xs[0] = 10;
                    ys[0] = -12;
                    xs[1] = 10;
                    ys[1] = 12;
                    xs[2] = -10;
                    ys[2] = 0;
                }
                else
                    toret = outputShape;
            }else{
                if(innerShape==null){
                    // quadrat
                    xs = new int[4];
                    ys = new int[4];
                    xs[0] = -10;
                    ys[0] = -10;
                    xs[1] = 10;
                    ys[1] = -10;
                    xs[2] = 10;
                    ys[2] = 10;
                    xs[3] = -10;
                    ys[3] = 10;
                }
                else
                    toret = innerShape;
            }
        }
        if(toret==null)
            toret = new Polygon(xs, ys, xs.length);
        return toret;
    }


    // labels cache and creator
    String[] WHlabels=null;
    String[] WHtooltips=null;
    synchronized String getWHlabel(WeightHolder wh, boolean tooltip){
        String[] WHstrs = (tooltip?WHtooltips:WHlabels);
        if(WHstrs==null){
            WeightHolder[] whs = nn.getNeuronsAndSynapses();
            WHstrs = new String[whs.length];
            for(int i=0; i<whs.length;i++){
                String p = getLabel(whs[i], tooltip);
                WHstrs[i]=p;
            }
            if(tooltip)
                WHtooltips = WHstrs;
            else
                WHlabels = WHstrs;
        }
        try {
            return WHstrs[nn.getNeuronSynapseIndex(wh)];
        }
        catch(ArrayIndexOutOfBoundsException e){
            return getLabel(wh, tooltip);
        }
    }
    private String getLabel(WeightHolder wh, boolean tooltip){
        String s;
        String prop = (tooltip?"tooltip":"label");
        String atr = viewproperties.getProperty(prop).trim();
        if(atr.length()>0)
            s = Util.AttributeGetter.getStringAttribute(atr.split("\\s+"), nn, wh, currentSignal, 3, " - ");
        else
            s = null;
        return s;
    }
    synchronized void setWHlabel(WeightHolder wh, boolean tooltip){
        String[] WHstrs = (tooltip?WHtooltips:WHlabels);
        if(WHstrs!=null){
            String p = getLabel(wh, tooltip);
            try {
                WHstrs[nn.getNeuronSynapseIndex(wh)] = p;
                if(tooltip)
                    WHtooltips = WHstrs;
                else
                    WHlabels = WHstrs;
            }
            catch(ArrayIndexOutOfBoundsException e){
                // nosink tu du
                // we get here before the weightholder becomes part of a network
            }
        }
        else
            getWHpaint(wh);
    }
    // labels cache and creator end

    // paints cache and creator
    private Paint getPaint(double value, float blue, float alpha){
        float red, green;
        float weight = (float)value;
        green = 1f + weight;
        if(green>1f)green=1f;
        if(green<0f)green=0f;
        red = 1f - weight;
        if(red>1f)red=1f;
        if(red<0f)red=0f;
        Paint c;
        c = new Color(red, green, blue, alpha);
        //c = new GradientPaint(from, new Color(red, green, blue, alpha), to, Color.YELLOW /*new Color(red+(1-red)/2, green+(1-green)/2, blue+(1-blue)/2, alpha)*/);
        return c;
    }

    Paint[] WHpaints = null;
    synchronized Paint getWHpaint(WeightHolder wh){
        if(WHpaints==null){
            WeightHolder[] whs = nn.getNeuronsAndSynapses();
            WHpaints = new Paint[whs.length];
            for(int i=0; i<whs.length;i++){
                //setWHpaint(whs[i]);
                Paint p = getPaint(whs[i]);
                WHpaints[i]=p;
            }
        }
        try {
            return WHpaints[nn.getNeuronSynapseIndex(wh)];
        }
        catch(ArrayIndexOutOfBoundsException e){
            return getPaint(wh);
        }
    }
    private Paint getPaint(WeightHolder wh){
        Paint p;
        double d = Util.AttributeGetter.getDoubleAttribute(
                viewproperties.getProperty("color"), nn, wh,
                currentSignal);
        if (wh instanceof Neuron)
            p = getPaint(d * -1, 0f, 1f);
        else
            p = getPaint(d, 0f, 0.7f);
        return p;
    }
    synchronized void setWHpaint(WeightHolder wh){
        if(WHpaints!=null){
            Paint p = getPaint(wh);
            try {
                WHpaints[nn.getNeuronSynapseIndex(wh)] = p;
            }
            catch(ArrayIndexOutOfBoundsException e){
                // nosink tu du
                // we get here before the weightholder becomes part of a network
            }
        }
        else
            getWHpaint(wh);
    }
    // paints cache and creator end

    private boolean askSaveOrCancel(){
        int ans = JOptionPane.showConfirmDialog(this, "Save current network?", "Unsaved Network", JOptionPane.YES_NO_CANCEL_OPTION);
        if(ans == JOptionPane.YES_OPTION){
            jMenuSave_actionPerformed(null);
        }
        else if(ans== JOptionPane.CANCEL_OPTION){
            return false;
        }
        return true;
    }

    public void jMenuNew_actionPerformed(ActionEvent e) {
        if(!askSaveOrCancel())
            return;

        contentPane.remove(sp);
        nn = this.createNN();
        this.loadNeuralNetwork();
        contentPane.add(sp, java.awt.BorderLayout.CENTER);
        contentPane.validate();
    }

    public void jMenuSaveAs_actionPerformed(ActionEvent e) {
        while(true){
            String newName = JOptionPane.showInputDialog(this,
                    "New neural network name", "Save As",
                    JOptionPane.PLAIN_MESSAGE);
            try{
                if(newName==null)
                    break;
                else if (newName.length()==0){
                    continue;
                }
                nn = nn.getCopy(newName, false);
                ((NeuralNetworkEditable)nn).save();
                this.storeProps(this.viewproperties, nn.getName()+this.VIEWCONFFILEENDING);
                this.storeProps(this.dbproperties, nn.getName()+this.DBCONFFILEENDING);
                this.needToRename=false;
                setTitle(APPNAME + " - " + nn.getName());
                this.properties.setProperty("network", nn.getName());
                break;
            }
            catch(NeuralNetworkCreationException ee){
                int ans = JOptionPane.showConfirmDialog(this, ee.getMessage()+".\nOverwrite?", "Already Exists", JOptionPane.YES_NO_CANCEL_OPTION);
                if(ans==JOptionPane.YES_OPTION){
                    nn = nn.getCopy(newName, true);
                    ((NeuralNetworkEditable)nn).save();
                    this.storeProps(this.viewproperties, nn.getName()+this.VIEWCONFFILEENDING);
                    this.storeProps(this.dbproperties, nn.getName()+this.DBCONFFILEENDING);
                    this.needToRename=false;
                    setTitle(APPNAME + " - " + nn.getName());
                    this.properties.setProperty("network", nn.getName());
                    break;
                }
                if(ans==JOptionPane.CANCEL_OPTION){
                    break;
                }
            }
        }
    }

    public void jMenuSave_actionPerformed(ActionEvent e) {
        if(this.needToRename && !this.noSaveas)
            jMenuSaveAs_actionPerformed(e);
        else{
            try {
                ((NeuralNetworkEditable) nn).save();
                this.storeProps(this.viewproperties, nn.getName()+this.VIEWCONFFILEENDING);
                this.storeProps(this.dbproperties, nn.getName()+this.DBCONFFILEENDING);
            } catch (ClassCastException ee) {
                ee.printStackTrace();
            }
        }
    }

    public void jMenuOpen_actionPerformed(ActionEvent e) {
        if(!askSaveOrCancel())
            return;

        String[] filenames = NeuralNetworkFactory.getNeuralNetworkFactory().getNeuralNetworkNames();
        while(true){
            String toOpen = (String)JOptionPane.showInputDialog(this,
                    "Select network to open", "Open Network",
                    JOptionPane.PLAIN_MESSAGE, null, filenames, null);
            if(toOpen != null && toOpen.trim().length()!=0){
                try {
                    contentPane.remove(sp);
                    nn = NeuralNetwork.getNeuralNetwork(toOpen);
                    //nn = NeuralNetworkFactory.getNeuralNetworkFactory().getNeuralNetwork(toOpen, false);
                    setTitle(APPNAME + " - " + nn.getName());
                    this.properties.setProperty("network", nn.getName());
                    this.needToRename = false;
                    this.loadNeuralNetwork();
                    contentPane.add(sp, java.awt.BorderLayout.CENTER);
                    contentPane.validate();
                    break;
                } catch (NeuralException ee) {
                    ee.printStackTrace();
                    int ansi = JOptionPane.showConfirmDialog(this,
                            "Not a neural network", "Not a Neural Network",
                            JOptionPane.OK_CANCEL_OPTION);
                    if (ansi == JOptionPane.CANCEL_OPTION) {
                        break;
                    }
                }
            }
            else
                break;
        }
    }

    public void jMenuNeuralConfig_actionPerformed(ActionEvent e) {
        Properties config = NeuralNetworkFactory.getNeuralNetworkFactory().getProperties();
        JDialog proped = new PropertiesEditor(config, this, "Edit Neural System Configuration", true);
        proped.setSize(450, 200);
        proped.show();
        NeuralNetworkFactory.getNeuralNetworkFactory().saveProperties();
    }

    public void jMenuNNProps_actionPerformed(ActionEvent e) {
        String[] teachers = this.properties.getProperty("teacher").split(" ");
        try{
            JDialog proped = new NetworkPropsEditor((NeuralNetworkEditable) nn,
                    teachers, this, "Neural Network properties", true);
            proped.show();
        }catch(ClassCastException ee){
        }
    }

    public Properties getProperties(){
        return this.properties;
    }

    public void jMenuEditTran_actionPerformed(ActionEvent e) {
        //JDialog traned = new TranslatorEditor(nn.getTranslator(), this, nn.getName() + "'s Translator", true);
        JDialog traned = new TranEd(this, "Edit " + nn.getName() + "'s translator", true);
        traned.show();
    }

    public void jMenuViewConfig_actionPerformed(ActionEvent e) {
        JDialog proped = new PropertiesEditor(this.viewproperties, this, nn.getName() + "'s View Options", true);
        proped.setSize(450, 200);
        proped.show();
    }

    public void jMenuAsk_actionPerformed(ActionEvent e) {
        JDialog act = new ActionForm(nn, vv, this, "Action", true);
        act.setSize(450, 200);
        act.show();

        this.currentSignal=null;
    }

    public void jMenuDBTeach_actionPerformed(ActionEvent e) {
        JdbcAction.jdbcprocess(nn, dbproperties, viewproperties.getProperty("chartValues"), false, false, this.statusBarOut);
    }

    public void jMenuDBConfig_actionPerformed(ActionEvent e) {
        JDialog proped = new PropertiesEditor(this.dbproperties, this, nn.getName() + "'s JDBC Data Set Options", true);
        proped.setSize(450, 200);
        proped.show();
    }

    public void jMenuStopDBAction_actionPerformed(ActionEvent e) {
        nnt.stopJdbcProcess();
    }

    public void jMenuDBTest_actionPerformed(ActionEvent e) {
        JdbcAction.jdbcprocess(nn, dbproperties, viewproperties.getProperty("chartValues"), false, true, this.statusBarOut);
    }

    public void jMenuDBAsk_actionPerformed(ActionEvent e) {
        JdbcAction.jdbcprocess(nn, dbproperties, viewproperties.getProperty("chartValues"), true, false, this.statusBarOut);
    }

    public void newNNButton_actionPerformed(ActionEvent e) {
        jMenuNew_actionPerformed(e);
    }

    public void openButton_actionPerformed(ActionEvent e) {
        jMenuOpen_actionPerformed(e);
    }
}


class Editor_newNNButton_actionAdapter implements ActionListener {
    private Editor adaptee;
    Editor_newNNButton_actionAdapter(Editor adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.newNNButton_actionPerformed(e);
    }
}


class Editor_jMenuStopDBAction_actionAdapter implements ActionListener {
    private Editor adaptee;
    Editor_jMenuStopDBAction_actionAdapter(Editor adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jMenuStopDBAction_actionPerformed(e);
    }
}


class Editor_jMenuAsk_actionAdapter implements ActionListener {
    private Editor adaptee;
    Editor_jMenuAsk_actionAdapter(Editor adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jMenuAsk_actionPerformed(e);
    }
}


class Editor_jMenuViewConfig_actionAdapter implements ActionListener {
    private Editor adaptee;
    Editor_jMenuViewConfig_actionAdapter(Editor adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jMenuViewConfig_actionPerformed(e);
    }
}


class Editor_jMenuEditTran_actionAdapter implements ActionListener {
    private Editor adaptee;
    Editor_jMenuEditTran_actionAdapter (Editor adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {

        adaptee.jMenuEditTran_actionPerformed(e);
    }
}


class Editor_jMenuNNProps_actionAdapter implements ActionListener {
    private Editor adaptee;
    Editor_jMenuNNProps_actionAdapter(Editor adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jMenuNNProps_actionPerformed(e);
    }
}


class Editor_jMenuNeuralConfig_actionAdapter implements ActionListener {
    private Editor adaptee;
    Editor_jMenuNeuralConfig_actionAdapter(Editor adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jMenuNeuralConfig_actionPerformed(e);
    }
}


class Editor_jMenuSave_actionAdapter implements ActionListener {
    private Editor adaptee;
    Editor_jMenuSave_actionAdapter(Editor adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jMenuSave_actionPerformed(e);
    }
}


class Editor_jMenuOpen_actionAdapter implements ActionListener {
    private Editor adaptee;
    Editor_jMenuOpen_actionAdapter(Editor adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jMenuOpen_actionPerformed(e);
    }
}


class Editor_openButton_actionAdapter implements ActionListener {
    private Editor adaptee;
    Editor_openButton_actionAdapter(Editor adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.openButton_actionPerformed(e);
    }
}


class Editor_jMenuSaveAs_actionAdapter implements ActionListener {
    private Editor adaptee;
    Editor_jMenuSaveAs_actionAdapter(Editor adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jMenuSaveAs_actionPerformed(e);
    }
}


class Editor_jMenuNew_actionAdapter implements ActionListener {
    private Editor adaptee;
    Editor_jMenuNew_actionAdapter(Editor adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jMenuNew_actionPerformed(e);
    }
}


class Editor_moveB_actionAdapter implements ActionListener {
    private Editor adaptee;
    Editor_moveB_actionAdapter(Editor adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.moveB_actionPerformed(e);
    }
}


class Editor_pickB_actionAdapter implements ActionListener {
    private Editor adaptee;
    Editor_pickB_actionAdapter(Editor adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.pickB_actionPerformed(e);
    }
}


class Editor_jToggleButton1_actionAdapter implements ActionListener {
    private Editor adaptee;
    Editor_jToggleButton1_actionAdapter(Editor adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jToggleButton1_actionPerformed(e);
    }
}


class Editor_arrangeButton_actionAdapter implements ActionListener {
    private Editor adaptee;
    Editor_arrangeButton_actionAdapter(Editor adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.arrangeButton_actionPerformed(e);
    }
}


class Editor_jsaveButton_actionAdapter implements ActionListener {
    private Editor adaptee;
    Editor_jsaveButton_actionAdapter(Editor adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jsaveButton_actionPerformed(e);
    }
}


class Editor_jMenuFileExit_ActionAdapter implements ActionListener {
    Editor adaptee;

    Editor_jMenuFileExit_ActionAdapter(Editor adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent actionEvent) {
        adaptee.jMenuFileExit_actionPerformed(actionEvent);
    }
}


class Editor_jMenuHelpAbout_ActionAdapter implements ActionListener {
    Editor adaptee;

    Editor_jMenuHelpAbout_ActionAdapter(Editor adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent actionEvent) {
        adaptee.jMenuHelpAbout_actionPerformed(actionEvent);
    }
}


class Editor_jMenuDBAsk_actionAdapter implements ActionListener {
    private Editor adaptee;
    Editor_jMenuDBAsk_actionAdapter(Editor adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jMenuDBAsk_actionPerformed(e);
    }
}


class Editor_jMenuDBConfig_actionAdapter implements ActionListener {
    private Editor adaptee;
    Editor_jMenuDBConfig_actionAdapter(Editor adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jMenuDBConfig_actionPerformed(e);
    }
}


class Editor_jMenuDBTeach_actionAdapter implements ActionListener {
    private Editor adaptee;
    Editor_jMenuDBTeach_actionAdapter(Editor adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jMenuDBTeach_actionPerformed(e);
    }
}


class Editor_jMenuDBTest_actionAdapter implements ActionListener {
    private Editor adaptee;
    Editor_jMenuDBTest_actionAdapter(Editor adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jMenuDBTest_actionPerformed(e);
    }
}

class NeuralEventAdapter implements NeuralEventListener{
    Editor adaptee;
    NeuralEventAdapter (Editor adaptee){
        this.adaptee = adaptee;
    }

    public void neuralEventHappened(NeuralEvent e){
        Signal sig = e.getSignal();
        int ety = e.getEventType();
        if(sig!=null){
            adaptee.currentSignal = sig;

            if(Integer.parseInt(adaptee.viewproperties.getProperty("propagationVisualiseDelay"))>0){
                if(e.getEventType() == e.SIGNALPROCEEDEDFORTH || e.getEventType() == e.SIGNALPROCEEDEDBACK){
                    adaptee.vv.repaint();
                    try {
                        Thread.currentThread().sleep(Integer.parseInt(adaptee.viewproperties.getProperty("propagationVisualiseDelay")));
                        //adaptee.currentSignal.wait(adaptee.propagationVisualiseDelay);
                    } catch (InterruptedException ex) {
                    }
                }
            }

        }
        if (e.getEventType() == e.GENERIC) {
            adaptee.statusBarOut.print(Util.getPluginNameForClass(e.getSource().getClass()));
            adaptee.statusBarOut.print(':');
            adaptee.statusBarOut.print(' ');
            adaptee.statusBarOut.println(e.getMeggage());
        }
        else if(ety==e.TOPOLOGYMODIFIED){
            adaptee.WHpaints = null;
            adaptee.WHlabels = null;
            adaptee.WHtooltips = null;
        }
        else if(ety==e.WEIGHTMODIFIED || ety==e.WIGHTHOLDERPARAMETERMODIFIED){
            try{
                WeightHolder wh = (WeightHolder) e.getSource();
                adaptee.setWHpaint(wh);
                adaptee.setWHlabel(wh, true);
                adaptee.setWHlabel(wh, false);
            }
            catch (ClassCastException ex){}
        }

        /*if(ety==e.TAUGHT){
            if (!adaptee.noJdbc) {
                JdbcAction.updateChart();
            }
        }*/
    }
}
class StringBursterStream extends OutputStream{
    ByteArrayOutputStream buf = new ByteArrayOutputStream();
    StringBurster acq;
    int prev;

    StringBursterStream (JLabel label){
        this.acq = new StringBurster(label);
    }
    public void write(int b) throws IOException
    {
        if(b=='\n' || b=='\r'){
            if(prev!='\n' && prev!='\r'){
                String s = buf.toString();
                acq.burst(s);
                buf.reset();
            }
        }
        else{
            buf.write(b);
        }
        prev = b;
    }
}
class StringBurster{
    JLabel label;
    StringBurster(JLabel label){
        this.label = label;
    }
    public synchronized void burst(String s){
        label.setText(s);
        label.repaint();
    }
}
class StringBursterPrintStream extends PrintStream{
    public StringBursterPrintStream (JLabel label)
    {
        super(new StringBursterStream (label));
    }
}

