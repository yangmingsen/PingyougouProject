package com.pingyougou.sellergoods.service;

import java.util.List;
import java.util.Map;

import com.pingyougou.pojo.TbBrand;

import entity.PageResult;


/**
 * 品牌接口
 * @author yangmingsen
 *
 */
public interface BrandService {
	
	public List<TbBrand> findAll();
	
	/**
	 * 品牌分类
	 * @param pageNum 当前页
	 * @param pageSize 每页记录数
	 * @return
	 */
	public PageResult findPage(int pageNum,int pageSize);
	
	/**
	 * 增加
	 * @param brand
	 */
	public void add(TbBrand brand);
	
	/**
	 * 根据ID查找品牌
	 * @param id
	 * @return
	 */
	public TbBrand findOne(Long id);
	
	/**
	 * 修改
	 * @param brand
	 */
	public void  update(TbBrand brand);
		
	/**
	 * 删除
	 */
	public void delete(Long [] ids);
	
	/**
	 * 
	 * @param brand
	 * @param pageNum
	 * @param pageSize
	 * @return
	 */
	public PageResult findPage(TbBrand brand, int pageNum,int pageSize);
	
	/**
	 * 品牌下拉框数据
	 */
	List<Map> selectOptionList();

}
