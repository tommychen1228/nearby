package com.cdm.nearby.modal;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: cdm
 * Date: 2/13/13
 * Time: 2:42 PM
 */
public class Category {
    private String name;
    private String code;
    private List<Category> children = new ArrayList<Category>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public List<Category> getChildren() {
        return children;
    }

    public void setChildren(List<Category> children) {
        this.children = children;
    }

    public boolean hasChildren(){
        return !children.isEmpty();
    }
}
