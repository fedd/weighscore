package com.weighscore.neuro.gui.graph;

import java.awt.geom.*;
import java.util.Iterator;

import edu.uci.ics.jung.graph.ArchetypeVertex;
import edu.uci.ics.jung.visualization.VertexLocationFunction;
import com.weighscore.neuro.*;
import java.util.*;
import java.awt.*;
import edu.uci.ics.jung.visualization.SettableVertexLocationFunction;

public class NeuronLocationFunction implements VertexLocationFunction, SettableVertexLocationFunction {

    Hashtable locs;
    int betweenLayers, betweenNeuronsInLayer, leftMargin, upperMargin;
    int width, height;
    NeuralNetworkGraph nng;

    public void reinit(int betweenLayers, int betweenNeuronsInLayer, int sideMargin, int topBottomMargin){
        this.betweenLayers=betweenLayers;
        this.betweenNeuronsInLayer=betweenNeuronsInLayer;
        this.leftMargin = sideMargin;
        this.upperMargin = topBottomMargin;

        Set vertices = nng.getVertices();
        locs = new Hashtable(vertices.size());

        if(vertices.size()!= 0){

            for (Iterator i = vertices.iterator(); i.hasNext(); ) {
                NeuronVertex nv = (NeuronVertex) i.next();
                locs.put(nv, nv.getNeuron());
            }

            Neuron[] ns = nng.getNeuralNetwork().getNeurons();
            Neuron[] ins = nng.getNeuralNetwork().getInputNeurons();

            HashSet processed = new HashSet(ns.length);

            Vector layers = new Vector();

            layers.add(new Vector(ins.length));

            for (int i = 0; i < ins.length; i++) {
                this.process(ins[i], processed, layers, 0);
            }

            // process all unprocessed neurons (than have no input synapses, but are not input themselves)
            for (int i=0; i < ns.length; i++){
                if(!processed.contains(ns[i])){
                    this.process(ns[i], processed, layers, 0);
                }
            }

            // find the biggest layer pixed height
            int biggest = 0;
            for (int i = 0; i < layers.size(); i++) {
                int curr = ((Vector) layers.get(i)).size();
                if (curr > biggest)
                    biggest = curr;
            }
            biggest = biggest * this.betweenNeuronsInLayer;

            // generate points
            Hashtable points = new Hashtable(vertices.size());
            for (int i = 0; i < layers.size(); i++) {
                int x = i * this.betweenLayers + this.leftMargin;
                Vector layer = (Vector) layers.get(i);
                int height = layer.size() * this.betweenNeuronsInLayer;
                for (int j = 0; j < layer.size(); j++) {
                    int y = j * this.betweenNeuronsInLayer + (biggest - height) / 2 +
                            this.upperMargin;
                    points.put(layer.get(j), new Point(x, y));
                }
            }

            // change neurons to points in locs
            for (Iterator i = locs.keySet().iterator(); i.hasNext(); ) {
                ArchetypeVertex v = (ArchetypeVertex) i.next();
                Neuron n = (Neuron) locs.get(v);
                locs.put(v, points.get(n));
            }

            this.height = biggest + this.upperMargin * 2;
            this.width = layers.size() * this.betweenLayers + this.leftMargin * 2;
        }
        else{
            this.height = 640;
            this.width = 480;
        }
    }

    public NeuronLocationFunction (NeuralNetworkGraph nng, int betweenLayers, int betweenNeuronsInLayer, int sideMargin, int topBottomMargin){
        this.nng=nng;

        this.reinit(betweenLayers, betweenNeuronsInLayer, sideMargin, topBottomMargin);

    }

    private void process(Neuron start, HashSet processed, Vector layers, int depth){
        if(!processed.contains(start)){
            processed.add(start);

            Synapse[] ss = start.getOutSynapses();

            ((Vector)layers.get(depth)).add(start);

            if(layers.size()<=depth+1 && ss.length>0){
                layers.add(new Vector(ss.length));
            }

            for(int i=0; i<ss.length; i++){
                this.process(ss[i].getOutNeuron(), processed, layers, depth+1);
            }
        }
    }

    public int getWidth(){
        return this.width;
    }

    public int getHeight(){
        return this.height;
    }

    /**
     * getLocation
     *
     * @param v ArchetypeVertex
     * @return Point2D
     * @todo Implement this
     *   edu.uci.ics.jung.visualization.VertexLocationFunction method
     */
    public Point2D getLocation(ArchetypeVertex v) {
        return (Point2D)locs.get(v);
    }

    /**
     * getVertexIterator
     *
     * @return Iterator
     * @todo Implement this
     *   edu.uci.ics.jung.visualization.VertexLocationFunction method
     */
    public Iterator getVertexIterator() {
        return locs.keySet().iterator();
    }

    public void setLocation(ArchetypeVertex v, Point2D location) {
        locs.put(v, location);
    }
}
