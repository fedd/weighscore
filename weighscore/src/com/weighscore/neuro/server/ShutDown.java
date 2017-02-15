package com.weighscore.neuro.server;

public class ShutDown extends Thread {
    public ShutDown(String name){
        super(name);
    }
    public void run(){
        nns.mayAcceptClient=false;
        while(!nns.stoppedAllClients){
            try {
                this.sleep(5);
            } catch (InterruptedException ex) {
            }
        }
    }
}
