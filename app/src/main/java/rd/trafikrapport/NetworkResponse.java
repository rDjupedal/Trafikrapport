package rd.trafikrapport;

public interface NetworkResponse {
    public void success(String data);
    public void error(Exception e);
}
