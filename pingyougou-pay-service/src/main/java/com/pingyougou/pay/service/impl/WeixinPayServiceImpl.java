package com.pingyougou.pay.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import com.pingyougou.pay.service.WeixinPayService;

import utils.HttpClient;

@Service
public class WeixinPayServiceImpl implements WeixinPayService {
	
	@Value("${appid}")//微信公众账号或开放平台APP的唯一标识
	private String appid;
	
	@Value("${partner}")//财付通平台的商户账号
	private String partner;
	
	@Value("${partnerkey}")//财付通平台的商户密钥
	private String partnerkey;

	@Override
	public Map createNative(String out_trade_no, String total_fee) {
		// TODO Auto-generated method stub
		
		System.out.println(appid+" "+partner+" "+partnerkey);
		
		Map<String,String> param=new HashMap();//创建参数
		param.put("appid", appid);//公众号
		param.put("mch_id", partner);//商户号
		param.put("nonce_str", WXPayUtil.generateNonceStr());//随机字符串
		param.put("body", "pingyougouzhifu");//商品描述
		param.put("out_trade_no", out_trade_no);//商户订单号
		param.put("total_fee",total_fee);//总金额（分）
		param.put("spbill_create_ip", "127.0.0.1");//IP
		param.put("notify_url", "http://www.itcast.cn");//回调地址(随便写)
		param.put("trade_type", "NATIVE");//交易类型
		
		try {
			//2.生成要发送的xml 
			String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);
			System.out.println("XML = "+xmlParam);	
			HttpClient client=new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
			client.setHttps(true);
			client.setXmlParam(xmlParam);
			client.post();		
			//3.获得结果 
			String result = client.getContent();
			System.out.println("Result = "+result);
			Map<String, String> resultMap = WXPayUtil.xmlToMap(result);	
			System.out.println("微信返回结果 "+resultMap);
			Map<String, String> map=new HashMap<>();
			map.put("code_url", resultMap.get("code_url"));//支付地址
			map.put("total_fee", total_fee);//总金额
			map.put("out_trade_no",out_trade_no);//订单号
			return map;
		} catch (Exception e) {
			e.printStackTrace();
			return new HashMap<>();
		}

	}
	
	
	@Override
	public Map queryPayStatus(String out_trade_no) {
		Map param=new HashMap();
		param.put("appid", appid);//公众账号ID
		param.put("mch_id", partner);//商户号
		param.put("out_trade_no", out_trade_no);//订单号
		param.put("nonce_str", WXPayUtil.generateNonceStr());//随机字符串
		String url="https://api.mch.weixin.qq.com/pay/orderquery";		
		try {
			String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);	
			HttpClient client=new HttpClient(url);
			client.setHttps(true);
			client.setXmlParam(xmlParam);
			client.post();
			String result = client.getContent();			
			Map<String, String> map = WXPayUtil.xmlToMap(result);
			System.out.println(map);
			return map;			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}	
	}
	
	
	@Override
	public Map closePay(String out_trade_no) {
		Map param=new HashMap();
		param.put("appid", appid);//公众账号ID
		param.put("mch_id", partner);//商户号
		param.put("out_trade_no", out_trade_no);//订单号
		param.put("nonce_str", WXPayUtil.generateNonceStr());//随机字符串
		String url="https://api.mch.weixin.qq.com/pay/closeorder";
		try {
			String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);
			HttpClient client=new HttpClient(url);
			client.setHttps(true);
			client.setXmlParam(xmlParam);
			client.post();
			String result = client.getContent();
			Map<String, String> map = WXPayUtil.xmlToMap(result);
			System.out.println(map);
			return map;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}		
	}
	
	

}
