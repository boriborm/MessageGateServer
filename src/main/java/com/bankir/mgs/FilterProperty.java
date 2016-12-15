package com.bankir.mgs;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FilterProperty {
    private String property;
    private Object value;
    private int type;
    private String dateFormat;
    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    public static final int TYPE_UNDEF = 0;
    public static final int TYPE_LONG = 1;
    public static final int TYPE_STRING = 2;
    public static final int TYPE_DOUBLE = 3;
    public static final int TYPE_DATE = 4;
    public static final int TYPE_BOOLEAN = 5;

    /* Инициализация через конструктор из-за того, что GSON убивает
       значения по умолчанию в объявлении переменных */
    public  FilterProperty(){
        this.dateFormat = DEFAULT_DATE_FORMAT;
        this.type = TYPE_UNDEF;
    }
    public FilterProperty(String property, Object value) {
        this();

        this.property = property;
        this.value = value;

        if (value instanceof String) this.type = TYPE_STRING;
        if (value instanceof Date) this.type = TYPE_DATE;
        if (value instanceof Long) this.type = TYPE_LONG;
        if (value instanceof Double) this.type = TYPE_DOUBLE;
        if (value instanceof Boolean) this.type = TYPE_BOOLEAN;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public Object getValue() {

        //String
        if (this.type==TYPE_STRING&&!(this.value instanceof String)) {
            return getStringValue();
        }

        //Long
        if (this.type==TYPE_LONG&&!(this.value instanceof Long)) {
            return getLongValue();
        }

        //Double
        if (this.type==TYPE_DOUBLE&&!(this.value instanceof Double)) {
            return getDoubleValue();
        }

        //Boolean
        if (this.type== TYPE_BOOLEAN &&!(this.value instanceof Boolean)) {
            return getBooleanValue();
        }

        //Date
        if (this.type== TYPE_DATE&&!(this.value instanceof Date)) {
            return getDateValue();
        }
        return value;

    }

    public void setDateFormat(String dateFormat) {
        if (dateFormat!=null) this.dateFormat = dateFormat;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getStringValue() {return String.valueOf(value);}

    public Long getLongValue() {
        if (value==null) return null;

        Long val = null;

        if (value instanceof Long) val = (Long) value;

        if (value instanceof Double){
                val = ((Double) value).longValue();
        }

        if (value instanceof String){
            try {
                val = (Double.valueOf((String) value)).longValue();
            } catch (NumberFormatException e) {e.printStackTrace();}
        }
        return val;
    }

    public Double getDoubleValue() {
        if (value==null) return null;

        Double val = null;

        if (value instanceof Double) val = (Double) value;

        if (value instanceof Long) {
            try {
                val = Double.parseDouble(String.valueOf(value));
            }catch (NumberFormatException e) {}
        }

        if (value instanceof String){
            try {
                val = Double.parseDouble((String) value);
            } catch (NumberFormatException e) {}
        }
        return val;
    }


    public Date getDateValue() {
        if (value==null) return null;

        Date val = null;

        if (value instanceof Date) val = (Date) value;

        if (value instanceof Long){
            val.setTime((Long) value);
        }

        if (value instanceof String){
            try {
                DateFormat formatter = new SimpleDateFormat(this.dateFormat);
                val = formatter.parse((String) value);
            } catch (ParseException e) {e.printStackTrace();}
        }
        return val;
    }

    public Boolean getBooleanValue(){
        if (value==null) return null;

        Boolean val = null;

        if (value instanceof Boolean) val = (Boolean) value;


        if (value instanceof String ){
            if (((String) value).equalsIgnoreCase("true")) val = true;
            if (((String) value).equalsIgnoreCase("false")) val = false;
        }

        if (value instanceof Double){
            if (((Double) value)==0) val = false;
            else val = true;
        }

        if (value instanceof Long){
            if (((Long) value)==0) val = false;
            else val = true;
        }

        return val;
    }
    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String toString(){
        return property+": "+ getValue();
    }
}
