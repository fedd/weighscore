package com.weighscore.neuro.gui.graph;

import java.util.*;

import com.weighscore.neuro.*;

import edu.uci.ics.jung.graph.*;
import edu.uci.ics.jung.utils.*;

public class SynapseEdge extends UserDataDelegate implements DirectedEdge{
    private Synapse s=null;

    private NeuronVertex from=null, to=null;

    private HashSet incidentNeuronVertices=null;
    private Pair pair=null;

    protected SynapseEdge(NeuronVertex from, NeuronVertex to){
        // reflect in a neural net
        //s = from.getNeuron().addOutSynapseTo(to.getNeuron());
        Neuron n = from.getNeuron();
        s = ((NeuralNetworkEditable)n.getNeuralNetwork()).addSynapse(n, to.getNeuron());
        init(from, to);
    }

    private void init(NeuronVertex from, NeuronVertex to){
        // remember endpoints
        this.from = from;
        this.from.registerOutEdge(this);
        this.to = to;
        this.to.registerInEdge(this);

        // fill the set
        incidentNeuronVertices = new HashSet(2);
        incidentNeuronVertices.add(from);
        incidentNeuronVertices.add(to);
        // fill the pair
        pair = new Pair(from, to);
    }

    protected SynapseEdge(Synapse s, NeuronVertex from, NeuronVertex to){
        // reflect in a neural net
        this.s = s;

        init(from, to);
    }

    public Synapse getSynapse(){
        return s;
    }

    public Set getIncidentVertices() {
        return incidentNeuronVertices;
    }

    public Vertex getOpposite(Vertex vertex) {
        if(vertex == from)
            return to;
        else if (vertex == to)
            return from;
        else
            throw new IllegalArgumentException("This is not this neuron's synapse");
    }

    public Pair getEndpoints() {
        return pair;
    }

    public Vertex getSource() {
        return from;
    }

    public Vertex getDest() {
        return to;
    }

    public ArchetypeGraph getGraph() {
        return from.getGraph();
    }

    public Set getIncidentElements() {
        return this.incidentNeuronVertices;
    }


    // ***********************************
    // ***********UNIMPLEMENTED***********
    // ***********************************

    /**
     * getEquivalentEdge
     *
     * @param g ArchetypeGraph
     * @return ArchetypeEdge
     * @deprecated
     */
    public ArchetypeEdge getEquivalentEdge(ArchetypeGraph g) {
        throw new UnsupportedOperationException("The equal edges are not distinguished yet");
    }

    public int numVertices() {
        return 2;
    }

    public boolean isIncident(ArchetypeVertex v) {
        return (v == from || v == to);
    }

    public ArchetypeEdge getEqualEdge(ArchetypeGraph g) {
        throw new UnsupportedOperationException("The equal edges are not distinguished yet");
    }

    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("Cloning not supported yet");
    }

    public ArchetypeEdge copy(ArchetypeGraph g) {
        throw new UnsupportedOperationException("The edge copy is not implemented yet");
    }
}
