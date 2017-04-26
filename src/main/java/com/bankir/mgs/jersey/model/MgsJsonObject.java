package com.bankir.mgs.jersey.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class MgsJsonObject {
    private boolean success;
    private String message;
    private Object data;
    private Long total;
    public MgsJsonObject(boolean success){
        this.success = success;
    }

    public MgsJsonObject(String errorMessage){
        this.success=false;
        this.message=errorMessage;
    }
    public MgsJsonObject(Object data){
        this.success=true;
        this.data=data;
    }

    public String getError() {
        return message;
    }

    public MgsJsonObject() {
        this.success=true;
    }

    public static Response getErrorResponse(Response.Status status, String errorMessage){
        MgsJsonObject jObj = new MgsJsonObject(errorMessage);
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

    public static MgsJsonObject Success(){
        return new MgsJsonObject();
    }
}
