package com.polyu.im_bird_sys.controller;

import com.polyu.im_bird_sys.bo.UserBo;
import com.polyu.im_bird_sys.enums.OperatorFriendRequestTypeEnum;
import com.polyu.im_bird_sys.enums.SearchFriendsStatusEnum;
import com.polyu.im_bird_sys.pojo.FriendsRequest;
import com.polyu.im_bird_sys.pojo.User;
import com.polyu.im_bird_sys.service.UserService;
import com.polyu.im_bird_sys.utils.FastDFSClient;
import com.polyu.im_bird_sys.utils.FileUtils;
import com.polyu.im_bird_sys.utils.JSONResult;
import com.polyu.im_bird_sys.utils.MD5Utils;
import com.polyu.im_bird_sys.vo.FriendsRequestVO;
import com.polyu.im_bird_sys.vo.MyFriendsVO;
import com.polyu.im_bird_sys.vo.UserVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

@Controller
@RequestMapping("user")
public class UserController {

    //注入service层
    @Autowired
    UserService userService;

    @Autowired
    FastDFSClient fastDFSClient;

    @RequestMapping("getUser")
    public String getUserById(String id, Model model){
        User user=userService.getUserById(id);
        model.addAttribute("user",user);
        return "user_list";
    }

    /**
     * 用户与注册一体化方法
     */
    @RequestMapping("registerOrLogin")
    @ResponseBody
    public JSONResult registerOrlogin(User user){
        User userResult = userService.queryUserNameExist(user.getUsername());
        if(userResult != null){
            //此用户存在，可以登录
            if (!userResult.getPassword().equals(MD5Utils.getPwd(user.getPassword()))){
                //密码错误
                return JSONResult.errorMsg("用户名或密码错误");
            }
        }else{
            //用户不存在，需要进行注册
            user.setPassword(MD5Utils.getPwd(user.getPassword()));
            user.setUsername(user.getUsername());

            userResult= userService.insert(user);
        }
        UserVO userVO=new UserVO();
        BeanUtils.copyProperties(userResult,userVO);
        return JSONResult.ok(userVO);
    }

    /**
     * 上传用户头像
     */
    @RequestMapping("uploadFaceBase64")
    @ResponseBody
    public JSONResult uploadProfile(@RequestBody UserBo userBo) throws Exception{
        //获取前端传来的base64字符串,然后转为文件对象进行上传
        String base64Data = userBo.getProfileData();
        System.out.println(base64Data);

        String userProfilePath = "D:\\waynechat\\"+userBo.getUserId()+"userBase64.png";

        //调用fileUtils中的方法将base64字符串转为文件对象
        //FileUtils.base64ToFile(userProfilePath,base64Data);
        MultipartFile multipartFile = FileUtils.fileToMultipart(userProfilePath);

       //获取fastdfs上传图片路径
        String url = fastDFSClient.uploadBase64(multipartFile);
        System.out.println(url);

        //头像的名称处理
        String thump = "_150*150.";
        String[] arr = url.split("\\.");
        String thumpUrl = arr[0] + thump + arr[1];

        //更新用户头像
        User user = new User();
        user.setId(userBo.getUserId());
        user.setFaceImage(thumpUrl);
        user.setFaceImageBig(url);
        User updateUserResult = userService.updateUserInfo(user);
        return JSONResult.ok(updateUserResult);
    }

    /**
     * 修改用户昵称
     * @param user
     * @return
     */
    @RequestMapping("setNickname")
    @ResponseBody
    public JSONResult setNickname(User user){
        User userResult = userService.updateUserInfo(user);
        return JSONResult.ok(userResult);
    }

    /**
     * 搜索好友功能
     * @param myUserId
     * @param friend_username
     * @return
     */
    @RequestMapping("searchFriend")
    @ResponseBody
    public JSONResult searchFriend(String myUserId,String friend_username){
        /**
         * 前置条件：
         * 1.搜索的用户如果不存在，则返回【无此用户】
         * 2.搜索的账号如果是你自己，则返回【不能添加自己】
         * 3.搜索的朋友已经是你好友，返回【该用户已经是你的好友】
         * 解决的方法：使用枚举类
         */
        Integer status = userService.preconditionSearchFriends(myUserId,friend_username);
        //可以正常添加好友
        if (status == SearchFriendsStatusEnum.SUCCESS.status){
            User user = userService.queryUserNameExist(friend_username);
            //将查询到的用户信息返回给前端
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(user,userVO);
            return JSONResult.ok(userVO);
        }else{
            String msg = SearchFriendsStatusEnum.getMsgByKey(status);
            return JSONResult.errorMsg(msg);
        }
    }

    /**
     * 发送添加好友请求
     * @param myUserId
     * @param friendUserName
     * @return
     */
    @RequestMapping("addFriendRequest")
    @ResponseBody
    public JSONResult addFriendRequest(String myUserId,String friendUserName){
        if (StringUtils.isBlank(myUserId) || StringUtils.isBlank(friendUserName)){
            return JSONResult.errorMsg("好友似乎不存在...");
        }
        //检查是否已经发送过好友请求


        userService.sendFriendRequest(myUserId,friendUserName);
        return JSONResult.ok();
    }

    /**
     * 查询好友请求
     * @param userId
     * @return
     */
    @RequestMapping("queryFriendRequest")
    @ResponseBody
    public JSONResult queryFriendRequest(String userId){
        List<FriendsRequestVO> friendsRequestList = userService.queryFriendRequestList(userId);
        return JSONResult.ok(friendsRequestList);
    }

    /**
     *
     * 处理好友请求
     * 与前端传入的参数同名
     * @param acceptUserId
     * @param sendUserId
     * @param operatorType
     * @return
     */
    @RequestMapping("operateFriendRequest")
    @ResponseBody
    public JSONResult operateFriendRequest(String acceptUserId,String sendUserId,Integer operatorType){
        FriendsRequest friendsRequest = new FriendsRequest();
        friendsRequest.setAcceptUserId(acceptUserId);
        friendsRequest.setSendUserId(sendUserId);
        if(operatorType == OperatorFriendRequestTypeEnum.REJECT.type){
            //拒绝 直接删除记录
            userService.deleteFriendRequest(friendsRequest);
        }else if(operatorType == OperatorFriendRequestTypeEnum.PASS.type){
            //同意 像好友表中添加记录，并删除记录
            userService.passFriendRequest(sendUserId,acceptUserId);
        }
        //查询好友表中的列表数据
        List<MyFriendsVO> myFriends = userService.queryMyFriends(acceptUserId);
        return JSONResult.ok(myFriends);
    }
}
