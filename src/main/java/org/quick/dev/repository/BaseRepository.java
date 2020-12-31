package org.quick.dev.repository;

import org.apache.ibatis.annotations.Param;
import org.quick.dev.repository.entity.BaseEntity;

import java.util.List;
import java.util.Map;

public interface BaseRepository<T extends BaseEntity, ID> {

    List<T> selectByCondition(Map<String, Object> queryData);

    List<T> selectAll();

    T selectByPrimaryKey(ID id);

    int insert(T record);

    int updateByPrimaryKeySelective(T record);

    int softDeleteByPrimaryKey(@Param("id") ID id);

    int batchSoftDeleteByPrimaryKey(@Param("ids") List<ID> ids);

}
