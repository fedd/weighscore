package com.weighscore.neuro.gui.graph;

import java.util.*;

import com.weighscore.neuro.*;

import edu.uci.ics.jung.graph.*;
import edu.uci.ics.jung.utils.*;
import edu.uci.ics.jung.graph.event.*;

public class NeuralNetworkGraph extends UserDataDelegate implements DirectedGraph{
    private NeuralNetwork nn = null;
    //private NeuralNetwork nnn = null;
    private HashSet edges = null, verts=null;

    public NeuralNetworkGraph(NeuralNetwork neuralNetwork) {
        nn = neuralNetwork;

        // recall edges. first recall all vertices, make hashtable
        Set v = this.getVertices();
        Hashtable hnv = new Hashtable();
        for(Iterator i=v.iterator(); i.hasNext();){
            NeuronVertex nv = (NeuronVertex) i.next();
            hnv.put(nv.getNeuron(), nv);
        }
        // using that hashtable, recall edges
        Set keys = hnv.keySet();
        this.edges=new HashSet();
        for(Iterator i = keys.iterator(); i.hasNext();){
            Neuron nfrom = (Neuron) i.next();
            NeuronVertex from = (NeuronVertex) hnv.get(nfrom);
            Synapse[] outs = nfrom.getOutSynapses();
            for(int j = 0; j<outs.length; j++){
                Neuron nto = outs[j].getOutNeuron();
                NeuronVertex to = (NeuronVertex) hnv.get(nto);

                SynapseEdge se = new SynapseEdge(outs[j], from, to);
                this.edges.add(se);
            }
        }
    }

    public NeuralNetwork getNeuralNetwork(){
        return nn;
    }

    /**
     * isDirected
     *
     * @return boolean
     * @deprecated
     */
    public boolean isDirected() {
        return true;
    }

    public Vertex addVertex(Vertex v) {
        if (v.getGraph() != this)
            throw new IllegalArgumentException("The vertex is not of this graph");
        this.getVertices().add(v);
        return v;
    }

    public Edge addEdge(Edge e) {
        if (((SynapseEdge)e).getSource().getGraph() != this)
            throw new IllegalArgumentException("The edge is not of this graph");
        this.getEdges().add(e);
        return e;
    }

    public void removeVertex(Vertex v) {
        if(nn instanceof NeuralNetworkEditable){
            NeuronVertex nv = (NeuronVertex) v;
            Neuron n = nv.getNeuron();
            Set ses = nv.getIncidentEdges();
            for (Iterator i = ses.iterator(); i.hasNext(); ) {
                SynapseEdge se = (SynapseEdge) i.next();
                this.removeEdge(se);
            }
            ((NeuralNetworkEditable)nn).removeNeuron(n);
            this.getVertices().remove(v);
        }
        //edges = null;
    }

    public void removeEdge(Edge e) {
        if(nn instanceof NeuralNetworkEditable){

            SynapseEdge se = (SynapseEdge) e;
            NeuronVertex snv = (NeuronVertex) se.getSource();
            Neuron sn = snv.getNeuron();

            //sn.removeOutSynapse(se.getSynapse());
            ((NeuralNetworkEditable)nn).removeOutSynapse(sn, se.getSynapse());
            this.getEdges().remove(e);
        }
    }

    public Set getVertices() {
        if(verts==null){
            Neuron[] ns = nn.getNeurons();
            NeuronVertex nv = null;
            verts = new HashSet(ns.length);
            for(int i=0; i< ns.length;i++){
                nv = new NeuronVertex(this, ns[i]);
                verts.add(nv);
            }
        }
        return verts;
    }

    public Set getEdges() {
        if (edges==null){
            HashSet vs = (HashSet) this.getVertices();
            WeightHolder[] whs = nn.getNeuronsAndSynapses();
            edges = new HashSet(whs.length-vs.size());
            for(Iterator i1 = vs.iterator(); i1.hasNext(); ){
                NeuronVertex nv = (NeuronVertex) i1.next();
                Set es =  nv.getOutEdges();
                for(Iterator i2 = es.iterator(); i2.hasNext(); ){
                    SynapseEdge se = (SynapseEdge) i2.next();
                    edges.add(se);
                }
            }
        }
        return edges;
    }

    public int numVertices() {
        return this.getVertices().size();
    }

    public int numEdges() {
        return this.getEdges().size();
    }

    /**
     * removeVertices
     *
     * @param vertices Set
     * @deprecated
     */
    public void removeVertices(Set vertices) {
        throw new UnsupportedOperationException("Deprecated method is not implemented");
    }

    /**
     * removeEdges
     *
     * @param edges Set
     * @deprecated
     */
    public void removeEdges(Set edges) {
        throw new UnsupportedOperationException("Deprecated method is not implemented");
    }

    public void removeAllEdges() {
        Set s = this.getEdges();
        for(Iterator i1 = s.iterator(); i1.hasNext(); ){
            this.removeEdge((SynapseEdge)i1.next());
        }
    }

    public void removeAllVertices() {
        Set s = this.getVertices();
        for(Iterator i1 = s.iterator(); i1.hasNext(); ){
            this.removeVertex((NeuronVertex)i1.next());
        }
    }

    // ***********************************
    // ***********UNIMPLEMENTED***********
    // ***********************************
    public void addListener(GraphEventListener gel, GraphEventType get) {
        //mGraphListenerHandler.addListener(gel, get);
    }

    public void removeListener(GraphEventListener gel, GraphEventType get) {
        //mGraphListenerHandler.removeListener(gel, get);
    }

    public Collection getVertexConstraints() {
        return null;
    }

    public Collection getEdgeConstraints() {
        return null;
    }



    public ArchetypeGraph newInstance() {
        throw new UnsupportedOperationException("New instance of graph not supported yet");
    }

    public ArchetypeGraph copy() {
        throw new UnsupportedOperationException("The graph copy is not implemented yet");
    }

    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("Cloning not supported yet");
    }







}
