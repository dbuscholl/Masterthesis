package servlets;

import org.json.JSONObject;

/**
 * This class is a simple JSON Object giving information about an error which occured during anything. This can simply
 * be embedded into the response
 */
public class ErrorResponse extends JSONObject {
    /**
     * constructor inisitalizing empty
     */
    public ErrorResponse(){
        this("");
    }

    /**
     * oconstructor initalizing with an error
     * @param description describe the things that went wrong here
     */
    public ErrorResponse(String description) {
        super();
        put("Error",true);
        put("Error Description",description);
    }
}
