package com.example.ferpiguardsystem.Model;

import java.util.List;
  
public class KeyHistoryActivityParentModel {

    private String ParentItemTitle;
    private List<KeyHistoryActivityChildModel> ChildItemList;
    public KeyHistoryActivityParentModel(){}

    public KeyHistoryActivityParentModel(String parentItemTitle, List<KeyHistoryActivityChildModel> childItemList) {
        ParentItemTitle = parentItemTitle;
        ChildItemList = childItemList;
    }

    public String getParentItemTitle() {
        return ParentItemTitle;
    }

    public void setParentItemTitle(String parentItemTitle) {
        ParentItemTitle = parentItemTitle;
    }

    public List<KeyHistoryActivityChildModel> getChildItemList() {
        return ChildItemList;
    }

    public void setChildItemList(List<KeyHistoryActivityChildModel> childItemList) {
        ChildItemList = childItemList;
    }
}
