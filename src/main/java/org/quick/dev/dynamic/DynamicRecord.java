package org.quick.dev.dynamic;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.jooq.Record;
import org.quick.dev.dynamic.metadata.DynamicEntityDefinition;
import org.quick.dev.dynamic.metadata.DynamicField;
import org.quick.dev.service.EntityDefinitionService;
import org.quick.dev.utils.SpringContextUtil;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

public class DynamicRecord extends HashMap<String, Object> implements Serializable {

    private String entityName;

    private DynamicEntityDefinition metadata;

    private EntityDefinitionService definitionService;

    public DynamicRecord() {
        this.definitionService = SpringContextUtil.getBean(EntityDefinitionService.class);
    }

    public DynamicRecord(Record record) {
        this();
        this.entityName = (String) record.get("entity_name");
        List<DynamicField> fields = this.getMetadata().getFields();
        for(DynamicField field : fields) {
            switch (field.getType()) {
                case KEY:
                    this.put(field.getName(), (Long)record.get(field.getFieldName()));
                    break;
                case STRING:
                case TEXTAREA:
                case RICH_TEXT:
                    this.put(field.getName(), (String) record.get(field.getFieldName()));
                    break;
                default:
                    this.put(field.getName(), record.get(field.getFieldName()));
            }
        }
    }


    @JsonIgnoreProperties
    public DynamicEntityDefinition getMetadata() {
        return this.metadata == null ? this.definitionService.getEntityDefinition(entityName) : this.metadata;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }
}
