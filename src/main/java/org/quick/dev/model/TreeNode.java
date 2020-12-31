package org.quick.dev.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TreeNode {

    private String title;
    private boolean expand;
    private List<TreeNode> children;

}
