package com.weighscore.neuro;

public class NeuralException extends RuntimeException {
    public NeuralException() {
        super();
    }

    public NeuralException(String message) {
        super(message);
    }

    public NeuralException(Throwable cause) {
        super(cause);
    }

    public NeuralException(String message, Throwable cause) {
        super(message, cause);
    }
}
