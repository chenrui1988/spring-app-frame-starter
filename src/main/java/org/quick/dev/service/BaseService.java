package org.quick.dev.service;


import org.quick.dev.model.EntityResult;
import org.quick.dev.model.PageQueryParams;
import org.quick.dev.repository.entity.BaseEntity;

import java.util.List;

public interface BaseService<T extends BaseEntity, ID> {

    EntityResult selectByCondition(PageQueryParams params);

    EntityResult selectAll();

    EntityResult selectByPrimaryKey(ID id);

    EntityResult create(T data);

    EntityResult update(T data);

    EntityResult delete(ID id);

    EntityResult batchDelete(List<ID> ids);

}
