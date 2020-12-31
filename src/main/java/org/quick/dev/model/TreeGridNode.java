package org.quick.dev.model;

import lombok.Getter;
import lombok.Setter;
import org.quick.dev.repository.entity.BaseEntity;

import java.util.List;

@Getter
@Setter
public class TreeGridNode<T> extends BaseEntity {

    private List<TreeGridNode> children;
    private boolean isLeaf;

}
