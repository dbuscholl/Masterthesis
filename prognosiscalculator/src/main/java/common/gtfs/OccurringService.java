package common.gtfs;

/**
 * This class is used to determine wheter a trip is scheduled for a specific date or not. This class is usually provided
 * by the database. If this service id is given by a trip then it occurs!
 */
public class OccurringService {
    private String service_id;

    /**
     * This class is used to determine wheter a trip is scheduled for a specific date or not. This class is usually provided
     * by the database. If this service id is given by a trip then it occurs!
     */
    public OccurringService(){
    }

    /**
     * This class is used to determine wheter a trip is scheduled for a specific date or not. This class is usually provided
     * by the database. If this service id is given by a trip then it occurs!
     * @param service_id the service id which is targeted
     */
    public OccurringService(String service_id) {
        this.service_id = service_id;
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

}
