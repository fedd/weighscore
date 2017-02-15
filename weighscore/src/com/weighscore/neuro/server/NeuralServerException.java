package com.weighscore.neuro.server;

public class NeuralServerException extends RuntimeException {
    public NeuralServerException() {
        super();
    }

    public NeuralServerException(String message) {
        super(message);
    }

    public NeuralServerException(Throwable cause) {
        super(cause);
    }

    public NeuralServerException(String message, Throwable cause) {
        super(message, cause);
    }
}
