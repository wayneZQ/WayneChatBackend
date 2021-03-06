package com.polyu.im_bird_sys.enums;

/**
 * 搜索好友的前置状态
 */
public enum  SearchFriendsStatusEnum {
    SUCCESS(0, "OK"),
    USER_NOT_EXIST(1, "无此用户"),
    NOT_YOURSELF(2, "不能添加你自己为好友"),
    ALREADY_FRIENDS(3, "该用户已经是你的好友");

    public final Integer status;
    public final String msg;

    SearchFriendsStatusEnum(Integer status, String msg) {
        this.status = status;
        this.msg = msg;
    }
    public Integer getStatus(){
        return this.status;
    }

    /**
     * 根据状态查找消息
     * @param status
     * @return
     */
    public static String getMsgByKey(Integer status){
        for(SearchFriendsStatusEnum type: SearchFriendsStatusEnum.values()){
            if(status == type.status){
                return type.msg;
            }
        }
        return null;
    }
}
