package com.polyu.im_bird_sys.enums;

public enum OperatorFriendRequestTypeEnum {

    REJECT(0,"拒绝"),
    PASS(1,"同意");

    public final Integer type;
    public final String msg;

    OperatorFriendRequestTypeEnum(Integer type,String msg){
        this.type = type;
        this.msg = msg;
    }
     public Integer getType(){
        return type;
     }

     public static String getMsgByType(Integer type){
        for (OperatorFriendRequestTypeEnum operatorType: OperatorFriendRequestTypeEnum.values()){
            if (type == operatorType.getType()) {
                return operatorType.msg;
            }
        }
         return null;
     }
}
