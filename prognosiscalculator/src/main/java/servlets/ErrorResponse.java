package servlets;

import org.json.JSONObject;

public class ErrorResponse extends JSONObject {
    public ErrorResponse(){
        this("");
    }

    public ErrorResponse(String description) {
        super();
        put("Error",true);
        put("Error Description",description);
    }
}
