package org.quick.dev.model;

import lombok.Getter;

import java.util.List;


@Getter
public class PageData<T> {

    private Integer totalSize;

    private List<T> data;

    public PageData(Integer totalSize, List<T> data) {
        this.totalSize = totalSize;
        this.data =data;
    }

}
