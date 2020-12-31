package org.quick.dev.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.quick.dev.model.EntityResult;
import org.quick.dev.model.PageQueryParams;
import org.quick.dev.repository.BaseRepository;
import org.quick.dev.repository.entity.BaseEntity;
import org.quick.dev.service.BaseService;
import org.quick.dev.utils.SnowflakeIdWorker;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class BaseServiceImpl<T extends BaseEntity, ID> implements BaseService<T, ID> {

    BaseRepository<T, ID> repository;

    @Override
    public EntityResult selectByCondition(PageQueryParams params) {
        Page<Object> page = PageHelper.startPage(params.getStartPage(), params.getSize());
        List<T> data = repository.selectByCondition(params.getConditions());
        HashMap<String, Object> map = new HashMap<>();
        long totalSize = page.getTotal();
        int totalPages = page.getPages();
        map.put("totalSize",totalSize);
        map.put("totalPages",totalPages);
        map.put("data",data);
        return EntityResult.ok(map);
    }

    @Override
    public EntityResult selectAll() {
        List<T> data = repository.selectAll();
        return EntityResult.ok(data);
    }

    @Override
    public EntityResult selectByPrimaryKey(ID id) {
        T data = repository.selectByPrimaryKey(id);
        return EntityResult.ok(data);
    }

    @Override
    public EntityResult create(T data) {
        data.setId(SnowflakeIdWorker.getNextId());
        data.setCreateDate(new Date());
        data.setDeleted(false);
        repository.insert(data);
        return EntityResult.ok(data);
    }

    @Override
    public EntityResult update(T data) {
        data.setUpdateDate(new Date());
        data.setDeleted(false);
        int i = repository.updateByPrimaryKeySelective(data);
        return EntityResult.ok(i);
    }

    @Override
    public EntityResult delete(ID id) {
        int i = repository.softDeleteByPrimaryKey(id);
        return EntityResult.ok();
    }

    @Override
    public EntityResult batchDelete(List<ID> ids) {
        repository.batchSoftDeleteByPrimaryKey(ids);
        return EntityResult.ok();
    }

    public void setMapper(BaseRepository<T, ID> repository) {
        this.repository = repository;
    }

    public BaseRepository<T, ID> getMapper() {
        return this.repository;
    }
}
