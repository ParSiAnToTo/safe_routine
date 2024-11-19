package com.example.saferoutine.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CheckListTemplateDTO {

    private String id;
    private int teamId;
    private String checkListName;
    private String resetTime;
    private String resetCycle;
    private List<CheckListRow> checkListRow;
    private List<CheckListCol> checkListCol;
    private String checkListType;
    private boolean checkListActive;
    private boolean checkListUse;
    private String checkListBackUpDate;

}
