package com.polyu.im_bird_sys.mapper;

import com.polyu.im_bird_sys.vo.FriendsRequestVO;
import com.polyu.im_bird_sys.vo.MyFriendsVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
@Mapper
public interface UserMapperCustomized {
    //接收方收到的好友请求列表
    List<FriendsRequestVO> queryFriendRequestList(String acceptUserID);
    List<MyFriendsVO> queryMyFriends(String userId);
}
