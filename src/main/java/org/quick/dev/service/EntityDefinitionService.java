package org.quick.dev.service;

import org.jooq.*;
import org.quick.dev.dynamic.metadata.DynamicEntityDefinition;
import org.quick.dev.dynamic.metadata.DynamicField;
import org.quick.dev.dynamic.metadata.DynamicFieldType;
import org.quick.dev.dynamic.metadata.EntitySetting;
import org.quick.dev.utils.SnowflakeIdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.jooq.impl.DSL.*;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.SQLDataType.*;

@Service
@Transactional(rollbackFor = Exception.class)
public class EntityDefinitionService {

    private static final String ENTITY_TABLE = "base_entity";
    private static final String ENTITY_SETTING_TABLE = "base_entity_setting";
    private static final String FIELD_TABLE = "base_field";

    private static final String ID_FIELD = "id";
    private static final String SYSTEM_FIELD = "is_system";
    private static final String DELETED_FIELD = "deleted";
    private static final String CREATE_DATE_FIELD = "create_date";
    private static final String CREATE_USER_FIELD = "create_user_id";
    private static final String UPDATE_DATE_FIELD = "update_date";
    private static final String UPDATE_USER_FIELD = "update_user_id";
    private static final String ENTITY_NAME_FIELD = "entity_name";

    private static final String ID_PROPERTY = "id";
    private static final String SYSTEM_PROPERTY = "isSystem";
    private static final String DELETED_PROPERTY = "deleted";
    private static final String CREATE_DATE_PROPERTY = "createDate";
    private static final String CREATE_USER_PROPERTY = "createUser";
    private static final String UPDATE_DATE_PROPERTY = "updateDate";
    private static final String UPDATE_USER_PROPERTY = "updateUser";

    private Map<String, DynamicEntityDefinition> metadataCache = new ConcurrentHashMap<>();

    @Autowired
    DSLContext dsl;

    public List<DynamicEntityDefinition> getAllEntityDefinitions() {
        Table table = table(ENTITY_TABLE);
        Result<Record> result = dsl.fetch(table);
        List<DynamicEntityDefinition> definitions = new ArrayList<>();
        result.forEach(record -> definitions.add(new DynamicEntityDefinition(record)));
        return definitions;
    }

    public DynamicEntityDefinition getEntityDefinition(String entityName) {
        if(metadataCache.containsKey(entityName)) return metadataCache.get(entityName);
        Record record = dsl.select().from(table(ENTITY_TABLE)).
                where(field("name").eq(entityName)).and(field(DELETED_FIELD).eq(0))
                .fetchOne();
        DynamicEntityDefinition metadata = new DynamicEntityDefinition(record);
        metadata.setFields(getAllFields(metadata.getName()));
        metadata.setEntitySetting(this.getEntitySetting(metadata.getName()));

        metadataCache.put(entityName, metadata);
        return metadata;
    }

    public List<DynamicField> getAllFields(String entity) {
        List<Record> records = dsl.select().from(table(FIELD_TABLE))
                .where(field("entity").eq(entity)).and(field(DELETED_FIELD).eq(0))
                .fetch();
        List<DynamicField> fields = new ArrayList<>();
        records.forEach(record -> fields.add(new DynamicField(record)));
        return fields;
    }

    public EntitySetting getEntitySetting(String entity) {
        Record record = dsl.select().from(table(ENTITY_SETTING_TABLE)).
                where(field("entity").eq(entity)).and(field(DELETED_FIELD).eq(0))
                .fetchOne();
        if(record != null) {
            return new EntitySetting(record);
        }
        return null;
    }

    public void createEntityDefinition(DynamicEntityDefinition definition) {
        String tableName = "ge_"+definition.getName().toLowerCase();
        this.createTable(tableName);
        dsl.insertInto(table(ENTITY_TABLE), field(ID_FIELD), field("name"), field("label"), field("table_name"), field(SYSTEM_FIELD), field(CREATE_DATE_FIELD), field(ENTITY_NAME_FIELD), field(DELETED_FIELD))
                .values(SnowflakeIdWorker.getNextId(), definition.getName(), definition.getLabel(), tableName, 0, new Date(), "Entity", 0)
                .execute();
        this.createSystemField(definition.getName());
    }

    private void createSystemField(String entity) {
        this.createKeyFieldDefinition(ID_PROPERTY, "ID", ID_FIELD, entity);
        this.createFieldDefinition(SYSTEM_PROPERTY, "系统字段标识", SYSTEM_FIELD, entity, DynamicFieldType.BOOL, "1");
        this.createFieldDefinition(DELETED_PROPERTY, "删除标识", DELETED_FIELD, entity, DynamicFieldType.BOOL, "1");
        this.createFieldDefinition(CREATE_DATE_PROPERTY, "创建时间", CREATE_DATE_FIELD, entity, DynamicFieldType.DATETIME, "32");
        this.createRelFieldDefinition(CREATE_USER_PROPERTY, "创建者", CREATE_USER_FIELD, entity,  "User");
        this.createFieldDefinition(UPDATE_DATE_PROPERTY, "最后更新时间", UPDATE_DATE_FIELD, entity, DynamicFieldType.DATETIME, "32");
        this.createRelFieldDefinition(UPDATE_USER_PROPERTY, "最后更新者", UPDATE_USER_FIELD, entity, "User");

    }

    private void createKeyFieldDefinition(String name, String label, String fieldName, String entity) {
        this.createFieldDefinition(name, label, fieldName, entity, DynamicFieldType.KEY, "32");
    }

    private void createFieldDefinition(String name, String label, String fieldName, String entity, DynamicFieldType type, String length) {
        dsl.insertInto(table(FIELD_TABLE), field(ID_FIELD), field("name"), field("label"), field("entity"), field("type"),
                field("length"), field("field_name"), field(SYSTEM_FIELD), field(CREATE_DATE_FIELD), field(ENTITY_NAME_FIELD), field(DELETED_FIELD))
                .values(SnowflakeIdWorker.getNextId(), name, label, entity, type.toString(), length, fieldName, 0, new Date(), "Field", 0)
                .execute();
    }

    private void createRelFieldDefinition(String name, String label, String fieldName, String entity, String relEntity) {
        dsl.insertInto(table(FIELD_TABLE), field(ID_FIELD), field("name"), field("label"), field("entity"), field("type"),
                field("rel_entity"), field("length"), field("field_name"), field(SYSTEM_FIELD), field(CREATE_DATE_FIELD), field(ENTITY_NAME_FIELD), field(DELETED_FIELD))
                .values(SnowflakeIdWorker.getNextId(), name, label, entity, DynamicFieldType.FOREIGN_KEY.toString(),
                        relEntity, "32", fieldName, 0, new Date(), "Field", 0)
                .execute();
    }

    private void createFieldDefinition(DynamicField dynamicField) {
        if(dynamicField.getType().equals(DynamicFieldType.FOREIGN_KEY)) {
            this.createRelFieldDefinition(dynamicField.getName(), dynamicField.getLabel(), dynamicField.getFieldName(), dynamicField.getEntity(), dynamicField.getRelEntity());
        } else {
            this.createFieldDefinition(dynamicField.getName(), dynamicField.getLabel(), dynamicField.getFieldName(), dynamicField.getEntity(), dynamicField.getType(), dynamicField.getLength());
        }
    }

    private void createField(DynamicField dynamicField) {
//        dsl.alterTable("").addColumn()
    }

    private void createTable(String tableName) {
        dsl.createTableIfNotExists(tableName)
                .column(ID_FIELD, BIGINT.length(64).nullable(false))
                .column(SYSTEM_FIELD, TINYINT)
                .column(DELETED_FIELD, TINYINT)
                .column(CREATE_USER_FIELD, BIGINT.length(64))
                .column(CREATE_DATE_FIELD, TIMESTAMP)
                .column(UPDATE_USER_FIELD, BIGINT.length(64))
                .column(UPDATE_DATE_FIELD, TIMESTAMP)
                .constraints(primaryKey(ID_FIELD))
                .execute();
    }

    public void deleteEntityDefinition(String entity) {
        dsl.update(table(ENTITY_TABLE)).set(field(DELETED_FIELD), 1).where(field("name").eq(entity)).execute();
        dsl.update(table(FIELD_TABLE)).set(field(DELETED_FIELD), 1).where(field("entity").eq(entity)).execute();
        dsl.alterTable("ge_"+entity.toLowerCase()).renameTo("de_"+entity.toLowerCase() +"_" + new Date().getTime()).execute();
    }
}
