package com.weighscore.neuro.gui.graph;

import com.weighscore.neuro.*;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;

import edu.uci.ics.jung.graph.Edge;
import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.visualization.Layout;
import edu.uci.ics.jung.visualization.PickSupport;
import edu.uci.ics.jung.visualization.PickedState;
import edu.uci.ics.jung.visualization.SettableVertexLocationFunction;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.AbstractPopupGraphMousePlugin;
import javax.swing.*;
import com.weighscore.neuro.gui.*;


/**
 * a plugin that uses popup menus to create vertices, undirected edges,
 * and directed edges.
 *
 * @author Tom Nelson - RABA Technologies
 * @author Fyodor Kravchenko - Vsetech
 */
public class NeuralEPGMPlugin  extends AbstractPopupGraphMousePlugin {

    SettableVertexLocationFunction vertexLocations;
    Editor editor;
    String[] acts;
    //String stats[];


    public NeuralEPGMPlugin(SettableVertexLocationFunction vertexLocations, Editor editor) {
        this.vertexLocations = vertexLocations;
        this.editor=editor;
        acts = editor.getProperties().getProperty("activator").split(" ");
        //stats = editor.getProperties().getProperty("statistic").split(" ");
    }

    protected void handlePopup(MouseEvent e) {
        final VisualizationViewer vv =
            (VisualizationViewer)e.getSource();
        final Layout layout = vv.getGraphLayout();
        final NeuralNetworkGraph graph = (NeuralNetworkGraph) layout.getGraph();
        final Point2D p = e.getPoint();
        final Point2D ivp = vv.inverseViewTransform(e.getPoint());
        PickSupport pickSupport = vv.getPickSupport();
        if(pickSupport != null) {

            final NeuronVertex vertex = (NeuronVertex)pickSupport.getVertex(ivp.getX(), ivp.getY());
            final Edge edge = pickSupport.getEdge(ivp.getX(), ivp.getY());
            final PickedState pickedState = vv.getPickedState();
            JPopupMenu popup = new JPopupMenu();

            if(vertex != null) {
                Set picked = pickedState.getPickedVertices();
                if(picked.size() > 0) {
                    JMenu directedMenu;
                    StringBuffer menuS = new StringBuffer("From neuron");
                    if(picked.size()==1){
                        directedMenu = new JMenu("Create synapse");
                    }
                    else{
                        directedMenu = new JMenu("Create synapses");
                        menuS.append('s');
                    }
                    menuS.append(' ');
                    popup.add(directedMenu);
                    final NeuronVertex[] fromN = new NeuronVertex[picked.size()];
                    Iterator iterator=picked.iterator();
                    int i = 0;
                    while(true) {
                        final NeuronVertex other = (NeuronVertex)iterator.next();
                        menuS.append(other.getNeuron().getName());
                        fromN[i]=other;
                        i++;
                        if(iterator.hasNext()){
                            menuS.append(", ");
                            continue;
                        }
                        break;
                    }
                    menuS.append(" to neuron ");
                    menuS.append(vertex.getNeuron().getName());

                    directedMenu.add(new AbstractAction(menuS.toString()) {
                        public void actionPerformed(ActionEvent e) {
                            for(int i=0; i<fromN.length; i++){
                                Edge newEdge = new SynapseEdge(fromN[i], vertex);
                                graph.addEdge(newEdge);
                            }
                            vv.repaint();
                        }
                        public boolean isEnabled(){
                            return graph.getNeuralNetwork() instanceof NeuralNetworkEditable;
                        }
                    });
                }
                final Neuron n = ((NeuronVertex)vertex).getNeuron();
                final NeuralNetwork nn = n.getNeuralNetwork();
                if(n.isInput()){
                    popup.add(new AbstractAction("Make Neuron Not Input") {
                        public void actionPerformed(ActionEvent e) {
                            ((NeuralNetworkEditable)nn).unMarkAsInput(n);
                            vv.repaint();
                        }
                        public boolean isEnabled(){
                            return nn instanceof NeuralNetworkEditable;
                        }
                    });
                }
                else{
                    popup.add(new AbstractAction("Make Neuron Input") {
                        public void actionPerformed(ActionEvent e) {
                            ((NeuralNetworkEditable)nn).markAsInput(n);
                            vv.repaint();
                        }
                        public boolean isEnabled(){
                            return nn instanceof NeuralNetworkEditable;
                        }
                    });
                }
                if(n.isOutput()){
                    popup.add(new AbstractAction("Make Neuron Not Output") {
                        public void actionPerformed(ActionEvent e) {
                            ((NeuralNetworkEditable)nn).unMarkAsOutput(n);
                            vv.repaint();
                        }
                        public boolean isEnabled(){
                            return nn instanceof NeuralNetworkEditable;
                        }
                    });
                }
                else{
                    popup.add(new AbstractAction("Make Neuron Output") {
                        public void actionPerformed(ActionEvent e) {
                            ((NeuralNetworkEditable)nn).markAsOutput(n);
                            vv.repaint();
                        }
                        public boolean isEnabled(){
                            return nn instanceof NeuralNetworkEditable;
                        }
                    });
                }
                popup.add(new AbstractAction("Edit Neuron") {
                    public void actionPerformed(ActionEvent e) {
                        NeuronEditor ne = new NeuronEditor(n, acts, editor, "Edit Neuron", true);
                        ne.show();
                        vv.repaint();
                    }
                    public boolean isEnabled(){
                        return graph.getNeuralNetwork() instanceof NeuralNetworkEditable;
                    }
                });

                popup.add(new AbstractAction("Delete Neuron") {
                    public boolean isEnabled(){
                        return graph.getNeuralNetwork() instanceof NeuralNetworkEditable;
                    }
                    public void actionPerformed(ActionEvent e) {
                        pickedState.pick(vertex, false);
                        graph.removeVertex(vertex);
                        //vv.restart();
                        vv.repaint();
                    }});
            } else if(edge != null) {
                popup.add(new AbstractAction("Edit Synapse") {
                    public void actionPerformed(ActionEvent e) {
                        NeuronEditor ne =
                                new NeuronEditor(
                                        ((SynapseEdge)edge).getSynapse(),
                                        acts,
                                        editor,
                                        "Edit Neuron",
                                        true);
                        ne.show();
                        vv.repaint();
                    }
                    public boolean isEnabled(){
                        return graph.getNeuralNetwork() instanceof NeuralNetworkEditable;
                    }
                });

                popup.add(new AbstractAction("Delete Synapse") {
                    public boolean isEnabled(){
                        return graph.getNeuralNetwork() instanceof NeuralNetworkEditable;
                    }
                    public void actionPerformed(ActionEvent e) {
                        pickedState.pick(edge, false);
                        graph.removeEdge(edge);
                        vv.repaint();
                    }});
            } else {
                popup.add(new AbstractAction("Create Neuron") {
                    public boolean isEnabled(){
                        return graph.getNeuralNetwork() instanceof NeuralNetworkEditable;
                    }
                    public void actionPerformed(ActionEvent e) {
                        Vertex newVertex = new NeuronVertex(graph);
                        vertexLocations.setLocation(newVertex, vv.inverseTransform(p));
                        Layout layout = vv.getGraphLayout();
                        for(Iterator iterator=graph.getVertices().iterator(); iterator.hasNext(); ) {
                            layout.lockVertex((Vertex)iterator.next());
                        }
                        graph.addVertex(newVertex);
                        vv.getModel().restart();
                        for(Iterator iterator=graph.getVertices().iterator(); iterator.hasNext(); ) {
                            layout.unlockVertex((Vertex)iterator.next());
                        }
                        vv.repaint();
                    }
                });
            }
            if(popup.getComponentCount() > 0) {
                popup.show(vv, e.getX(), e.getY());
            }
        }
    }
}
