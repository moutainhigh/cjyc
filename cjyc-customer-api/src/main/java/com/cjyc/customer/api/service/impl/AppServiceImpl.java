package com.cjyc.customer.api.service.impl;
import com.cjyc.common.until.CommonUtil;
import com.cjyc.common.until.Constants;
import com.cjyc.common.until.MiaoxinSmsUtil;
import com.cjyc.common.until.PinyinUtil;
import com.cjyc.customer.api.dao.CustomerMapper;
import com.cjyc.customer.api.entity.Customer;
import com.cjyc.customer.api.service.IAppService;
import com.cjyc.customer.api.service.ITokenService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by leo on 2019/7/25.
 */
@Service
public class AppServiceImpl implements IAppService {

    @Autowired
    private ITokenService tokenService;
    @Autowired
    private CustomerMapper customerMapper;

    @Value("${messages.expires}")
    private int msgExpires;

    @Value("${messages.daylimit}")
    private int msgDaylimit;

    private static Map<String,Customer> loginUserMap = new HashMap<>();
    private static Map<String,String> msgMap = new HashMap<>(); //登录验证码存储map
    private static Map<String,Integer> msgLimitMap = new HashMap<>();

    /**
     * 发送登录短信验证码
     * */
    @Override
    public boolean sendMessage(String phone) {

        int msgToday = msgLimitMap.isEmpty()? 0 : msgLimitMap.get(phone);

        //手机号合法且 未超出单日上限
        if(CommonUtil.valiPhoneNumber(phone) && msgDaylimit > msgToday){
            //生成6位短信码
            String code = CommonUtil.randomNum(6);

            try {
                //发送短信
                MiaoxinSmsUtil.send(phone, String.format(Constants.MSG_SEND_TEMP, code, msgExpires));

                //msgExpires时间后删除短信码
                final Timer timer=new Timer();

                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        msgMap.remove(phone);
                        timer.cancel();
                    }
                },msgExpires*60*1000);

                //单日累计发送次数设值
                msgLimitMap.put(phone, msgToday+1);

                //保存验证码
                msgMap.put(phone,code);
            }catch (Exception ex){
                ex.printStackTrace();
            }

            return true;

        } else {
            return false;
        }

    }

    /**
     * 校验验证码
     * */
    @Override
    public boolean checkMsgCode(String phone, String msgCode) {

        String valiCode = msgMap.get(phone);

        return StringUtils.isNotBlank(valiCode) && valiCode.equals(msgCode);

    }

    /**
     * 登录
     * */
    @Override
    public Map login(String phone) throws Exception{
        Map<String,Object> map = new HashMap<>();

        //登录之后验证码失效
        msgMap.remove(phone);

        //查询注册用户
        Customer customer = customerMapper.selectByPhone(phone);

        //登录
        if(customer != null){
            //登录状态 刷新token时间
            tokenService.refreshTokenTime(customer.getToken());
            //todo 数据处理
        //注册
        }else{
            String newCustomerCode = UUID.randomUUID().toString();
            customer = Customer.getInstance();
            customer.setCustomerCode(newCustomerCode);
            String token = tokenService.createToken(customer.getCustomerCode());
            customer.setToken(token);
            customer.setPhone(phone);
            customer.setName("客户"+ CommonUtil.randomNum(10));//默认名称
            customer.setFirstLetter(PinyinUtil.getPinYinAcronym("客户"));
            customer.setPwd(phone);//默认密码是其手机号
            customer.setToken(token);
            int id = customerMapper.insert(customer);

            if(id > 0){
                //todo 极光注册别名
                customer.setAlias("alias"+String.valueOf(id));
                customerMapper.updateById(customer);
            }
        }

        loginUserMap.put(customer.getToken(), customer);

        return map;
    }

    /**
     * 登出
     * */
    @Override
    public void logout(String customerCode, String token) {
        Customer customer = loginUserMap.get(token);
        msgMap.remove(customer.getPhone());
        loginUserMap.remove(token);
        tokenService.delToken(token);
    }

}
