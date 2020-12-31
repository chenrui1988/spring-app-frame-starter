package org.quick.dev.dynamic.metadata;

import lombok.Getter;
import org.jooq.Record;

@Getter
public class DynamicField {

    private Long id;

    private String name;

    private String label;

    private String fieldName;

    private DynamicFieldType type;

    private String relEntity;

    private String length;

    private String entity;

    private Boolean isSystem;

    public DynamicField(Record record) {
        System.out.println(record);
        this.id = (Long) record.get("id");
        this.name = (String) record.get("name");
        this.label = (String) record.get("label");
        this.type = DynamicFieldType.valueOf((String) record.get("type"));
        this.relEntity = (String) record.get("rel_entity");
        this.length = (String) record.get("length");
        this.fieldName = (String) record.get("field_name");
        this.isSystem = (Boolean)record.get("is_system");;
    }

}
