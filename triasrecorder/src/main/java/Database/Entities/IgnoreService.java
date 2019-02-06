package Database.Entities;

/**
 * This class is used to determine wheter a trip is scheduled for a specific date or not. This class is usually provided
 * by the database.
 */
public class IgnoreService {
    private String service_id;
    private int exception_type;

    /**
     * This class is used to determine wheter a trip is scheduled for a specific date or not. This class is usually provided
     * by the database.
     */
    public IgnoreService(){
    }

    /**
     * This class is used to determine wheter a trip is scheduled for a specific date or not. This class is usually provided
     * by the database.
     * @param service_id the service id which is targeted
     * @param exception_type 1 if the service is scheduled, 2 if the service is removed for the service
     */
    public IgnoreService(String service_id, int exception_type) {
        this.service_id = service_id;
        this.exception_type = exception_type;
    }

    /**
     *
     * @return serviceId
     */
    public String getService_id() {
        return service_id;
    }

    /**
     * sets the serviceID
     * @param service_id
     */
    public void setService_id(String service_id) {
        this.service_id = service_id;
    }

    /**
     * A value of 1 indicates that service has been added. A value of 2 indicates that service has been removed.
     * @return the exception type value
     */
    public int getException_type() {
        return exception_type;
    }

    /**
     * A value of 1 indicates that service has been added. A value of 2 indicates that service has been removed.
     * @param exception_type exception type value
     */
    public void setException_type(int exception_type) {
        this.exception_type = exception_type;
    }
}
