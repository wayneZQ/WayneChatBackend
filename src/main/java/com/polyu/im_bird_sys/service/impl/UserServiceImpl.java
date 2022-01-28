package com.polyu.im_bird_sys.service.impl;

import com.polyu.im_bird_sys.enums.SearchFriendsStatusEnum;
import com.polyu.im_bird_sys.idworker.Sid;
import com.polyu.im_bird_sys.mapper.FriendsRequestMapper;
import com.polyu.im_bird_sys.mapper.MyFriendsMapper;
import com.polyu.im_bird_sys.mapper.UserMapper;
import com.polyu.im_bird_sys.mapper.UserMapperCustomized;
import com.polyu.im_bird_sys.pojo.FriendsRequest;
import com.polyu.im_bird_sys.pojo.MyFriends;
import com.polyu.im_bird_sys.pojo.User;
import com.polyu.im_bird_sys.service.UserService;
import com.polyu.im_bird_sys.utils.FastDFSClient;
import com.polyu.im_bird_sys.utils.FileUtils;
import com.polyu.im_bird_sys.utils.QRcodeUtils;
import com.polyu.im_bird_sys.vo.FriendsRequestVO;
import com.polyu.im_bird_sys.vo.MyFriendsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    //注入mapper
    @Autowired
    UserMapper userMapper;

    @Autowired
    MyFriendsMapper myFriendsMapper;

    @Autowired
    FriendsRequestMapper friendsRequestMapper;

    @Autowired
    UserMapperCustomized userMapperCustomized;

    @Autowired
    Sid sid;

    @Autowired
    QRcodeUtils qRcodeUtils;

    @Autowired
    FastDFSClient fastDFSClient;

    @Override
    public User getUserById(String id) {
        return userMapper.selectByPrimaryKey(id);
    }

    /**
     * 根据用户的账户查找指定用户
     * @param username
     * @return
     */
    @Override
    public User queryUserNameExist(String username) {
        User user = userMapper.queryUserNameExist(username);
        return user;
    }

    @Override
    public User insert(User user) {
        //调用sid中的主键生成策略
        String userId = sid.nextShort();
        user.setId(userId);

        //为每个注册用户生成唯一的二维码
        String qrCodePath = "D:\\waynechat\\qrcode\\" + userId + "qrcode.png";
        qRcodeUtils.createQRcode(qrCodePath,"wechat_qrcode:"+user.getUsername());
        //二维码文件生成
        MultipartFile qrcodeFile = FileUtils.fileToMultipart(qrCodePath);
        String qrCodeURL = "";
        try{
            qrCodeURL = fastDFSClient.uploadQRCode(qrcodeFile);
        }catch (Exception e){
            e.printStackTrace();
        }
        user.setQrcode(qrCodeURL);

        userMapper.insert(user);
        return user;
    }

    /**
     *
     * @param user
     * @return
     */
    @Override
    public User updateUserInfo(User user) {
        userMapper.updateByPrimaryKeySelective(user);
        User updateResult = userMapper.selectByPrimaryKey(user.getId());
        return updateResult;
    }

    /**
     * 搜索用户的前置条件
     * @param myUserId
     * @param friend_username
     * @return 返回的是错误的状态值
     */
    @Override
    public Integer preconditionSearchFriends(String myUserId, String friend_username) {
        //根据好友账号查找返回的用户
        User user = queryUserNameExist(friend_username);
        //无此用户
        if (user == null){
            return SearchFriendsStatusEnum.USER_NOT_EXIST.status;
        }
        //不可添加自己
        if(myUserId.equals(user.getId())){
            return SearchFriendsStatusEnum.NOT_YOURSELF.status;
        }
        //已经是好友
        MyFriends myFriends = new MyFriends();
        myFriends.setMyUserId(myUserId);
        myFriends.setMyFriendUserId(user.getId());
        MyFriends friend_result = myFriendsMapper.selectOneByExample(myFriends);
        if (friend_result != null){
            return SearchFriendsStatusEnum.ALREADY_FRIENDS.status;
        }
        return SearchFriendsStatusEnum.SUCCESS.status;
    }

    /**
     *
     * @param myUserId
     * @param friendUserName
     */
    @Override
    public void sendFriendRequest(String myUserId, String friendUserName) {
        //根据好友账号查找返回的用户
        User user = queryUserNameExist(friendUserName);

        MyFriends myFriends = new MyFriends();
        myFriends.setMyUserId(myUserId);
        myFriends.setMyFriendUserId(user.getId());
        MyFriends friend_result = myFriendsMapper.selectOneByExample(myFriends);
        //使用 FriendsRequest表 完成添加好友
        if (friend_result == null){
            FriendsRequest friendsRequest = new FriendsRequest();
            String requestID = sid.nextShort();
            friendsRequest.setId(requestID);
            friendsRequest.setSendUserId(myUserId);
            friendsRequest.setAcceptUserId(user.getId());
            friendsRequest.setRequestDateTime(new Date());

            friendsRequestMapper.insert(friendsRequest);
        }
    }

    /**
     *
     * @param acceptUserID
     * @return
     */
    @Override
    public List<FriendsRequestVO> queryFriendRequestList(String acceptUserID) {
        return userMapperCustomized.queryFriendRequestList(acceptUserID);
    }

    /**
     *
     * @param friendsRequest
     */
    @Override
    public void deleteFriendRequest(FriendsRequest friendsRequest) {
        friendsRequestMapper.deleteByFriendRequest(friendsRequest);
    }

    /**
     *
     * @param sendUserId
     * @param acceptUserId
     */
    @Override
    public void passFriendRequest(String sendUserId, String acceptUserId) {
        //保存好友记录
        saveFriends(sendUserId,acceptUserId);
        saveFriends(acceptUserId,sendUserId);

        //删除好友申请表中的记录
        FriendsRequest friendsRequest = new FriendsRequest();
        friendsRequest.setSendUserId(sendUserId);
        friendsRequest.setAcceptUserId(acceptUserId);
        friendsRequestMapper.deleteByFriendRequest(friendsRequest);
    }

    /**
     * 保存好友记录
     * @param sendUserId
     * @param acceptUserId
     */
    private void saveFriends(String sendUserId, String acceptUserId){
        String recordID = sid.nextShort();

        MyFriends myFriends = new MyFriends();
        myFriends.setMyUserId(sendUserId);
        myFriends.setMyFriendUserId(acceptUserId);
        myFriends.setId(recordID);
        myFriendsMapper.insert(myFriends);
    }

    /**
     *
     * @param userId
     * @return
     */
    @Override
    public List<MyFriendsVO> queryMyFriends(String userId) {
        return userMapperCustomized.queryMyFriends(userId);
    }
}
