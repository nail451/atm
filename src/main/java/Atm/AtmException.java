package Atm;

public class AtmException extends Exception {

    private String exceptionDescription;
    private Boolean end;

    public String getExceptionDescription() { return exceptionDescription; }

    public void setExceptionDescription(String exceptionDescription) { this.exceptionDescription = exceptionDescription; }

    public Boolean getEnd() { return end; }

    public void setEnd(Boolean end) { this.end = end; }

    public AtmException(String arg, Boolean end) {
        setExceptionDescription(arg);
        setEnd(end);
    }
}
