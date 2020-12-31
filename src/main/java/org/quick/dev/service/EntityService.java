package org.quick.dev.service;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.Table;
import org.quick.dev.dynamic.metadata.DynamicEntityDefinition;
import org.quick.dev.dynamic.DynamicRecord;
import org.quick.dev.model.EntityResult;
import org.quick.dev.model.PageData;
import org.quick.dev.model.PageQueryParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

@Service
public class EntityService {

    @Autowired
    DSLContext dsl;

    @Autowired
    EntityDefinitionService definitionService;

    public EntityResult selectByCondition(String entityName, PageQueryParams params) {
        DynamicEntityDefinition metadata = definitionService.getEntityDefinition(entityName);
        Table table = table(metadata.getTableName());
        int count = dsl.fetchCount(table);
        Result<Record> result = dsl.select().from(table).where(field("deleted").eq(0)).limit(params.getStartPage()*params.getSize(), params.getSize()).fetch();

        List<DynamicRecord> entityDefinitionList = new ArrayList<>();
        result.forEach(record -> entityDefinitionList.add(new DynamicRecord(record)));
        PageData pageData = new PageData(count, entityDefinitionList);
        return EntityResult.ok(pageData);
    }

    public EntityResult getDefinition(String entityName) {
        DynamicEntityDefinition metadata = definitionService.getEntityDefinition(entityName);
        return EntityResult.ok(metadata);
    }

    public EntityResult createEntity(DynamicEntityDefinition definition) {
        definitionService.createEntityDefinition(definition);
        return EntityResult.ok();
    }

    public EntityResult deleteDefinition(String entity) {
        definitionService.deleteEntityDefinition(entity);
        return EntityResult.ok();
    }
}
