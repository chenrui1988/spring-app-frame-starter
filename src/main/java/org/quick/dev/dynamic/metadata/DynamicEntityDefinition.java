package org.quick.dev.dynamic.metadata;

import lombok.Getter;
import lombok.Setter;
import org.jooq.Record;

import java.util.List;

@Getter
@Setter
public class DynamicEntityDefinition {

    private Long id;

    private String name;

    private String label;

    private String tableName;

    private Boolean isSystem;

    private EntitySetting entitySetting;

    private List<DynamicField> fields;

    public DynamicEntityDefinition() {}

    public DynamicEntityDefinition(Record record) {
        this.id = (Long) record.get("id");
        this.name = (String) record.get("name");
        this.label = (String) record.get("label");
        this.tableName = (String) record.get("table_name");
        this.isSystem = (Boolean)record.get("is_system");
    }

}
