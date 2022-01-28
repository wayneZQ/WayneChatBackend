package com.polyu.im_bird_sys.service;

import com.polyu.im_bird_sys.pojo.FriendsRequest;
import com.polyu.im_bird_sys.pojo.User;
import com.polyu.im_bird_sys.vo.FriendsRequestVO;
import com.polyu.im_bird_sys.vo.MyFriendsVO;

import java.util.List;

public interface UserService {
    //根据id查找指定用户对象
    User getUserById(String id);
    //根据用户名查找指定用户
    User queryUserNameExist(String username);
    //加入新用户
    User insert(User user);
    //更新用户信息
    User updateUserInfo(User user);
    //搜索用户的前置条件
    Integer preconditionSearchFriends(String myUserId, String friend_username);
    //发送好友请求
    void sendFriendRequest(String myUserId,String friendUserName);
    //查询用户收到的好友请求
    List<FriendsRequestVO> queryFriendRequestList(String acceptUserID);
    //处理好友请求，忽略好友请求
    void deleteFriendRequest(FriendsRequest friendsRequest);
    //处理好友请求，通过好友请求
    void passFriendRequest(String sendUserId,String acceptUserId);

    //好友列表查询
    List<MyFriendsVO> queryMyFriends(String userId);
}
