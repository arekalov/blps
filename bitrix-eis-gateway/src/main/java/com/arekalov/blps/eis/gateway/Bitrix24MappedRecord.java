package com.arekalov.blps.eis.gateway;

import jakarta.resource.cci.MappedRecord;
import java.io.Serializable;
import java.util.AbstractMap;
import java.util.Set;

public class Bitrix24MappedRecord extends AbstractMap<String, Object>
        implements MappedRecord<String, Object>, Serializable {

    private final java.util.Map<String, Object> m = new java.util.LinkedHashMap<>();
    private String recordName = "bitrixRecord";
    private String recordShortDescription;

    public Bitrix24MappedRecord() {
    }

    public Bitrix24MappedRecord(String name) {
        this.recordName = name != null ? name : "bitrixRecord";
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return m.entrySet();
    }

    @Override
    public Object put(String k, Object v) {
        return m.put(k, v);
    }

    @Override
    public void clear() {
        m.clear();
    }

    @Override
    public String getRecordName() {
        return recordName;
    }

    @Override
    public void setRecordName(String name) {
        this.recordName = name != null ? name : "bitrixRecord";
    }

    @Override
    public String getRecordShortDescription() {
        return recordShortDescription;
    }

    @Override
    public void setRecordShortDescription(String description) {
        this.recordShortDescription = description;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        Bitrix24MappedRecord c = new Bitrix24MappedRecord();
        c.setRecordName(getRecordName());
        c.setRecordShortDescription(getRecordShortDescription());
        c.m.putAll(this.m);
        return c;
    }
}
