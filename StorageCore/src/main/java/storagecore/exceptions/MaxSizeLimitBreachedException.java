package storagecore.exceptions;

public class MaxSizeLimitBreachedException extends RuntimeException {
    public MaxSizeLimitBreachedException() {
        super("The file you're trying to move is larger than the maximum size limit");
    }
}
