package com.weighscore.neuro.gui.graph;

import java.util.*;

import com.weighscore.neuro.*;

import edu.uci.ics.jung.graph.*;
import edu.uci.ics.jung.utils.*;

public class NeuronVertex extends UserDataDelegate implements Vertex{
    private Neuron n = null;

    private NeuralNetworkGraph graph = null;

    private HashSet outs = new HashSet(4);
    private HashSet ins = new HashSet(4);
    private HashSet succs = null;
    private HashSet preds = null;
    private Hashtable snt = null;

    protected synchronized void registerOutEdge(SynapseEdge e){
        this.outs.add(e);
        this.succs=null;
        this.snt=null;
    }

    protected synchronized void registerInEdge(SynapseEdge e){
        this.ins.add(e);
        this.preds=null;
        this.snt=null;
    }


    protected NeuronVertex(NeuralNetworkGraph graph) {
        this.graph=graph;
        this.n = ((NeuralNetworkEditable)this.graph.getNeuralNetwork()).addNeuron();
    }

    protected NeuronVertex(NeuralNetworkGraph graph, Neuron neuron){
        this.graph = graph;
        this.n = neuron;
    }

    public Neuron getNeuron(){
        return n;
    }

    public ArchetypeGraph getGraph() {
        return graph;
    }

    public Set getIncidentElements() {
        HashSet s = new HashSet();
        s.addAll(ins);
        s.addAll(outs);

        return s;
    }

    private Hashtable getSynapseNeuronTable(){
        if(snt==null){
            snt = new Hashtable(ins.size()+outs.size());

            boolean out = true;
            HashSet hs = outs;
            while (true) {
                for (Iterator iter = hs.iterator(); iter.hasNext(); ) {
                    SynapseEdge syn = (SynapseEdge) iter.next();
                    NeuronVertex neib;
                    if(out)
                        neib = (NeuronVertex) syn.getDest();
                    else
                        neib = (NeuronVertex) syn.getSource();

                    Object obj = snt.get(neib);
                    if (obj == null)
                        snt.put(neib, syn);
                    else if (obj instanceof Set)
                        ((Set) obj).add(syn);
                    else {
                        HashSet s = new HashSet(2);
                        s.add(syn);
                        s.add(obj);
                        snt.put(neib, s);
                    }
                }
                if(!out)
                    break;
                out=false;
                hs=ins;
            }
        }
        return snt;
    }

    public Set getNeighbors() {
        return getSynapseNeuronTable().keySet();
    }

    public Set getIncidentEdges() {
        return this.getIncidentElements();
    }

    public int degree() {
        return ins.size()+outs.size();
    }

    public int numNeighbors() {
        return degree();
    }


    public boolean isNeighborOf(ArchetypeVertex v) {
        return this.getSynapseNeuronTable().containsKey(v);
    }

    public boolean isIncident(ArchetypeEdge e) {
        return ins.contains(e) || outs.contains(e);
    }

    public ArchetypeEdge findEdge(ArchetypeVertex v) {
        Object o = getSynapseNeuronTable().get(v);
        if (o == null)
            return null;
        else if (o instanceof SynapseEdge)
            return (SynapseEdge) o;
        else
            return (SynapseEdge) ((HashSet) o).iterator().next();
    }

    public Set findEdgeSet(ArchetypeVertex v) {
        Object o = getSynapseNeuronTable().get(v);
        if (o == null)
            return new HashSet(0);
        if(o instanceof Set)
            return (Set) o;
        HashSet s = new HashSet(1);
        s.add(o);
        return s;
    }

    public Set getPredecessors() {
        if(preds == null){
            preds = new HashSet(ins.size());
            for (Iterator iter = ins.iterator(); iter.hasNext(); ) {
                preds.add(((SynapseEdge) iter.next()).getSource());
            }
        }
        return preds;
    }

    public Set getSuccessors() {
        if(succs == null){
            succs = new HashSet(outs.size());
            for (Iterator iter = ins.iterator(); iter.hasNext(); ) {
                succs.add(((SynapseEdge) iter.next()).getDest());
            }
        }
        return succs;
    }

    public Set getInEdges() {
        return ins;
    }

    public Set getOutEdges() {
        return outs;
    }

    public int inDegree() {
        return ins.size();
    }

    public int outDegree() {
        return outs.size();
    }

    public int numPredecessors() {
        return inDegree();
    }

    public int numSuccessors() {
        return outDegree();
    }

    public boolean isSuccessorOf(Vertex v) {
        return getPredecessors().contains(v);
    }

    public boolean isPredecessorOf(Vertex v) {
        return getSuccessors().contains(v);
    }

    public boolean isSource(Edge e) {
        return outs.contains(e);
    }

    public boolean isDest(Edge e) {
        return ins.contains(e);
    }

    public Edge findEdge(Vertex v) {
        return (Edge)findEdge((ArchetypeVertex)v);
    }

    public Set findEdgeSet(Vertex v) {
        return this.findEdgeSet((ArchetypeVertex)v);
    }


    // ***********************************
    // ***********UNIMPLEMENTED***********
    // ***********************************
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("Cloning not supported yet");
    }

    public ArchetypeVertex getEqualVertex(ArchetypeGraph g) {
        throw new UnsupportedOperationException("The equal vertices are not distinguished yet");
    }

    /**
     * getEquivalentVertex
     *
     * @param g ArchetypeGraph
     * @return ArchetypeVertex
     * @deprecated
     */
    public ArchetypeVertex getEquivalentVertex(ArchetypeGraph g) {
            throw new UnsupportedOperationException("The equal vertices are not distinguished yet");
    }

    public ArchetypeVertex copy(ArchetypeGraph g) {
        throw new UnsupportedOperationException("The vertex copy is not implemented yet");
    }
}
