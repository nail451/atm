package Interface;

public interface Command {
    String key = null;
    String value = null;


    public String getKey();

    public void setKey(String key);

    public String getValue();

    public void setValue(String value);

    @Override
    public String toString();
}
