package com.polyu.im_bird_sys.mapper;

import com.polyu.im_bird_sys.pojo.MyFriends;

public interface MyFriendsMapper {
    int deleteByPrimaryKey(String id);

    int insert(MyFriends record);

    int insertSelective(MyFriends record);

    MyFriends selectByPrimaryKey(String id);

    MyFriends selectOneByExample(MyFriends myFriends);

    int updateByPrimaryKeySelective(MyFriends record);

    int updateByPrimaryKey(MyFriends record);
}