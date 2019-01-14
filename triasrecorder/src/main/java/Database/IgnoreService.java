package Database;

public class IgnoreService {
    private String service_id;
    private int exception_type;

    public IgnoreService(){
    }
    public IgnoreService(String service_id, int exception_type) {
        this.service_id = service_id;
        this.exception_type = exception_type;
    }

    public String getService_id() {
        return service_id;
    }

    public void setService_id(String service_id) {
        this.service_id = service_id;
    }

    public int getException_type() {
        return exception_type;
    }

    public void setException_type(int exception_type) {
        this.exception_type = exception_type;
    }
}
