package com.weighscore.neuro;

public class InvalidNeuralDefinitionException extends NeuralException {
    public InvalidNeuralDefinitionException() {
        super();
    }

    public InvalidNeuralDefinitionException(String message) {
        super(message);
    }

    public InvalidNeuralDefinitionException(Throwable cause) {
        super(cause);
    }

    public InvalidNeuralDefinitionException(String message, Throwable cause) {
        super(message, cause);
    }
}
