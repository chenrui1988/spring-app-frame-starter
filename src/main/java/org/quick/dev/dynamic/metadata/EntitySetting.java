package org.quick.dev.dynamic.metadata;

import lombok.Getter;
import lombok.Setter;
import org.jooq.Record;

@Getter
@Setter
public class EntitySetting {

    private Long id;

    private String listFields;

    private String viewFields;

    public EntitySetting(Record record) {
        this.id = (Long) record.get("id");
        this.listFields = record.get("list_fields") == null? null: (String) record.get("list_fields");
        this.viewFields = record.get("view_fields") == null? null: (String) record.get("view_fields");
    }
}
