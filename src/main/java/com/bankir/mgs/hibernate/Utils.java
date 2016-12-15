package com.bankir.mgs.hibernate;


import com.bankir.mgs.Config;
import com.bankir.mgs.FilterProperty;
import com.bankir.mgs.SorterProperty;
import com.google.gson.Gson;
import org.hibernate.StatelessSession;
import org.hibernate.query.Query;

import java.util.ArrayList;
import java.util.List;

public class Utils {



    public static FilterProperty getFilterProperty(List<FilterProperty> properties, String propertyName){
        FilterProperty property = null;
        for (FilterProperty prop : properties)
        {
            if (prop.getProperty()!=null&&prop.getProperty().equals(propertyName)){
                property = prop;
                System.out.println("finded: "+property.toString());
                break;
            }
        }

        return property;
    }

    public static SorterProperty getSorterProperty(List<SorterProperty> properties, String propertyName){
        SorterProperty property = null;
        for (SorterProperty prop : properties)
        {
            if (prop.getProperty()!=null&&prop.getProperty().equals(propertyName)){
                property = prop;
                break;
            }
        }
        return property;
    }

    public static List<FilterProperty> parseFilterProperties(String filter) {
        return parseFilterProperties(filter, new GeneralFilterPropertyPrepare());
    }

    public static List<FilterProperty> parseFilterProperties(String filter, FilterPropertyPrepareInterface listener) {
        List<FilterProperty> ret = new ArrayList<>();
        String propertyName;
        if (filter!=null&&filter.length()>0) {
            Gson gson = Config.getGsonBuilder().create();
            if (filter.startsWith("{")) filter = "[" + filter + "]";

            FilterProperty[] properties;
            properties = gson.fromJson(filter, FilterProperty[].class);

            for(FilterProperty property:properties){
                propertyName = property.getProperty();
                if (fieldNameIsValid(propertyName)){
                    if (listener!=null) {
                        listener.prepare(property);
                    }
                    ret.add(property);
                }
            }
        }
        return ret;
    }

    public static List<SorterProperty> parseSortProperties(String sort) {
        List<SorterProperty> ret = new ArrayList<>();

        if (sort!=null&&sort.length()>0) {
            Gson gson = Config.getGsonBuilder().create();
            if (sort.startsWith("{")) sort = "[" + sort + "]";

            SorterProperty[] properties;

            properties = gson.fromJson(sort, SorterProperty[].class);

            for(SorterProperty property:properties){
                if (fieldNameIsValid(property.getProperty())) ret.add(property);
            }
        }



        return ret;
    }

    private static boolean fieldNameIsValid(String in){
        boolean isValid = true;
        if (in.contains(" ")) isValid=false;
        if (in.contains("\"")) isValid=false;
        if (in.contains("'")) isValid=false;
        if (in.contains("(")) isValid=false;
        if (in.contains(")")) isValid=false;
        return isValid;
    }

    public static Query createQuery(StatelessSession session, String hql, Integer start, Integer limit, List<FilterProperty> filters, List<SorterProperty> sorters ){


        if (filters!=null&&filters.size()>0){
            String whereHql="";
            for (FilterProperty filter : filters) {
                if (filter.getProperty()!=null&&fieldNameIsValid(filter.getProperty()))
                    if (!hql.contains(":" + filter.getProperty()))
                        whereHql += " and " + filter.getProperty() + " = :" + filter.getProperty();
            }
            hql+=whereHql;
        }

        if (sorters!=null&&sorters.size()>0) {
            String sorthql = " order by ";
            for (int i=0;i<sorters.size();i++){
                SorterProperty property = sorters.get(i);
                if (property.getProperty()!=null&& fieldNameIsValid(property.getProperty())){
                    sorthql = sorthql + property.getProperty()+" "+property.getDirection();
                }
            }
            hql+=sorthql;
        }

        Query query = session.createQuery(hql);

        if (filters!=null&&filters.size()>0){
            for (FilterProperty filter : filters){
                query.setParameter(filter.getProperty(), filter.getValue());
            }
        }


        if (start!=null) query = query.setFirstResult(start);

        if (limit!=null) query = query.setMaxResults(limit);

        return query;
    }

}
