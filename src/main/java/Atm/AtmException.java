package Atm;

public class AtmException extends Exception {

    public String exceptionDescription;
    public Boolean end;

    public AtmException(String arg, Boolean end) {
        exceptionDescription = arg;
        this.end = end;
    }
}
