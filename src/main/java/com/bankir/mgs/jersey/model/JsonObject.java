package com.bankir.mgs.jersey.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class JsonObject {
    private boolean success;
    private String message;
    private Object data;
    private Long total;
    public JsonObject(boolean success){
        this.success = success;
    }

    public JsonObject(String errorMessage){
        this.success=false;
        this.message=errorMessage;
    }
    public JsonObject(Object data){
        this.success=true;
        this.data=data;
    }

    public String getError() {
        return message;
    }

    public JsonObject() {
        this.success=true;
    }

    public static Response getErrorResponse(Response.Status status, String errorMessage){
        JsonObject jObj = new JsonObject(errorMessage);
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        return Response.status(status)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(gson.toJson(jObj)).build();
    }

    public String toString(){
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        return gson.toJson(this);
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }

    public static JsonObject Success(){
        return new JsonObject();
    }
}
