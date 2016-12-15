package com.bankir.mgs.hibernate;

import com.bankir.mgs.FilterProperty;

public class GeneralFilterPropertyPrepare implements FilterPropertyPrepareInterface {

    @Override
    public void prepare(FilterProperty property) {
        String name = property.getProperty();
        if (name!=null){
            switch (name){
                case "bulkId":
                    property.setType(FilterProperty.TYPE_LONG);
                    break;
                case "beginDate":
                case "endDate":
                    property.setType(FilterProperty.TYPE_DATE);
                    break;
                case "typeId":
                case "statusGroupName":
                case "propertyName":
                    property.setType(FilterProperty.TYPE_STRING);
                    break;
                default:
                    property.setType(FilterProperty.TYPE_UNDEF);
            }
            // Приводим value к указанному формату в
            //property.setValue(property.getValue());
        }
    }
}
