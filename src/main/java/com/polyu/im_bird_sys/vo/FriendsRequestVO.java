package com.polyu.im_bird_sys.vo;

import lombok.Data;


@Data
public class FriendsRequestVO {
    private String sendUserID;
    private String sendUsername;
    private String sendUserNickname;
    private String sendUserFaceImage;
}
