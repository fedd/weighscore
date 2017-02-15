package com.weighscore.neuro;

public class NeuralNetworkCreationException extends NeuralException {
    public NeuralNetworkCreationException() {
        super();
    }
    public NeuralNetworkCreationException(String message) {
        super(message);
    }

    public NeuralNetworkCreationException(Throwable cause) {
        super(cause);
    }

    public NeuralNetworkCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
