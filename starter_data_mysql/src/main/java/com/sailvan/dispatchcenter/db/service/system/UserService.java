package com.sailvan.dispatchcenter.db.service.system;

import com.sailvan.dispatchcenter.common.constant.ResponseCode;
import com.sailvan.dispatchcenter.common.domain.ApiResponseDomain;
import com.sailvan.dispatchcenter.common.domain.UserSearch;
import com.sailvan.dispatchcenter.common.domain.system.Permission;
import com.sailvan.dispatchcenter.common.domain.system.UserDomain;
import com.sailvan.dispatchcenter.db.dao.automated.system.PermissionDao;
import com.sailvan.dispatchcenter.db.dao.automated.system.UserDao;
import com.sailvan.dispatchcenter.common.response.ApiResponse;
import com.sailvan.dispatchcenter.common.response.PageDataResult;
import com.sailvan.dispatchcenter.common.util.DateUtils;
import com.sailvan.dispatchcenter.common.util.DigestUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * @Title: AdminUserServiceImpl
 * @Description:
 * @date 2021-04
 * @author menghui
 */
@Service
public class UserService implements com.sailvan.dispatchcenter.common.pipe.system.UserService {

    private static Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserDao userDao;

    @Autowired
    private PermissionDao permissionDao;

    @Override
    public PageDataResult getUserList(UserSearch userSearch, Integer pageNum, Integer pageSize) {

        PageHelper.startPage(pageNum, pageSize);
        List<UserDomain> baseUserDomains = userDao.getUserList(userSearch);

        List<UserDomain> userDomainList = new ArrayList<>();
        for(UserDomain user:baseUserDomains){
            UserDomain userDomain =  new UserDomain();

            String permissionIds = user.getPermissionIds();
            BeanUtils.copyProperties(user,userDomain);
            userDomain.setPermissionIds(permissionIds);
            if(!StringUtils.isEmpty(permissionIds)){
                String[] ids = permissionIds.split(",");
                Map<String,ArrayList> map = new HashMap(16);
                Set<String> p = new HashSet<>();
                for(String id: ids){
                    ArrayList<String> op = new ArrayList<>();
                    if (id.contains("/")) {
                        String operating = id.split("/")[1];
                        if(!StringUtils.isEmpty(operating)){
                            String operating1 = getOperating(Integer.parseInt(operating)-Integer.parseInt(id.split("/")[0]));
                            op.add(operating1);
                        }
                        id = id.split("/")[0];
                    }
                    Permission permission = permissionDao.selectByPrimaryKey(Integer.parseInt(id));
                    String name = permission.getName();
                    if (!map.containsKey(name)) {
                        map.put(name,op);
                    }else {
                        ArrayList arrayList = map.get(name);
                        arrayList.addAll(op);
                        map.put(name,arrayList);
                    }
                }
                userDomain.setPermissions(map.toString());
            }
            userDomainList.add(userDomain);
        }

        PageDataResult pageDataResult = new PageDataResult();
        if(baseUserDomains.size() != 0){
            PageInfo<UserDomain> pageInfo = new PageInfo<>(userDomainList);
            pageDataResult.setList(userDomainList);
            pageDataResult.setTotals((int) pageInfo.getTotal());
            pageDataResult.setPageNum(pageNum);
        }

        return pageDataResult;
    }

    private String getOperating(int diff){
        switch (diff){
            case 1:
                return "add";
            case 2:
                return "delete";
            case 3:
                return "update";
            default:
                break;
        }
        return "all";
    }


    @Override
    public ApiResponseDomain addUser(UserDomain user) {
        ApiResponse apiResponse = new ApiResponse();
        try {
            UserDomain old = userDao.getUserByUserName(user.getSysUserName(),null);
            if(old != null){
                logger.error("用户[新增]，结果=用户名已存在！");
                return apiResponse.error(ResponseCode.ERROR_CODE,"用户[新增]，结果=用户名已存在！","");
            }
            String phone = user.getUserPhone();
            int length = 11;
            if(phone.length() != length){
                logger.error("置用户[新增或更新]，结果=手机号位数不对！");
                return apiResponse.error(ResponseCode.ERROR_CODE,"手机号位数不对！","");
            }
            String username = user.getSysUserName();
            if(user.getSysUserPwd() == null){
                String password = DigestUtils.Md5(username,"123456");
                user.setSysUserPwd(password);
            }else{
                String password = DigestUtils.Md5(username,user.getSysUserPwd());
                user.setSysUserPwd(password);
            }
            user.setRegTime(DateUtils.getCurrentDate());
            user.setUserStatus(1);
            int result = userDao.insert(user);
            if(result == 0){
                logger.error("用户[新增]，结果=新增失败！");
                return apiResponse.error(ResponseCode.ERROR_CODE,"新增失败！","");
            }
            logger.info("用户[新增]，结果=新增成功！");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("用户[新增]异常！", e);
            return apiResponse.error(ResponseCode.ERROR_CODE,"用户[新增]异常！","");
        }
        return apiResponse.success("新增成功！","");
    }


    @Override
    public ApiResponseDomain updateUser(UserDomain user) {
        ApiResponse apiResponse = new ApiResponse();
        Integer id = user.getId();
        UserDomain old = userDao.getUserByUserName(user.getSysUserName(),id);
        if(old != null){
            logger.error("用户[更新]，结果=用户名已存在！");
            return apiResponse.error(ResponseCode.ERROR_CODE,"用户名已存在！","");
        }
        String username = user.getSysUserName();
        if(!StringUtils.isEmpty(user.getSysUserPwd())){
            String password = DigestUtils.Md5(username,user.getSysUserPwd());
            user.setSysUserPwd(password);
        }

        int result = userDao.updateUser(user);
        if(result == 0){
            logger.error("用户[更新]，结果=更新失败！");
            return apiResponse.error(ResponseCode.ERROR_CODE,"更新失败！","");
        }
        logger.info("用户[更新]，结果=更新成功！");
        return apiResponse.success("更新成功！","");
    }

    @Override
    public UserDomain getUserById(Integer id) {
        return userDao.selectByPrimaryKey(id);
    }


    @Override
    public ApiResponseDomain delUser(Integer id,Integer status) {
        ApiResponse apiResponse = new ApiResponse();
        try {
            // 删除用户
            int result = userDao.updateUserStatus(id,status);
            if(result == 0){
                logger.error("删除用户失败");
                return apiResponse.error(ResponseCode.ERROR_CODE, "删除用户失败", "");
            }
            logger.info("删除用户成功");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("删除用户异常！", e);
            return apiResponse.error(ResponseCode.ERROR_CODE, "删除用户异常", "");
        }
        return apiResponse.success("删除用户成功", "");
    }

    @Override
    public ApiResponseDomain recoverUser(Integer id, Integer status) {
        ApiResponse apiResponse = new ApiResponse();
        try {
            int result = userDao.updateUserStatus(id,status);
            if(result == 0){
                return apiResponse.error(ResponseCode.ERROR_CODE, "恢复用户失败", "");
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("恢复用户异常！", e);
            return apiResponse.error(ResponseCode.ERROR_CODE, "恢复用户失败", "");
        }
        return apiResponse.success("恢复用户成功", "");
    }

    @Override
    public UserDomain findByUserName(String userName) {
        return userDao.findByUserName(userName);
    }


    @Override
    public int updatePwd(String userName, String password) {
        password = DigestUtils.Md5(userName,password);
        return userDao.updatePwd(userName,password);
    }
}
