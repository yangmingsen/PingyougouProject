package com.pingyougou.manager.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pingyougou.pojo.TbBrand;
import com.pingyougou.sellergoods.service.BrandService;

import entity.PageResult;
import entity.Result;

@RestController
@RequestMapping("/brand")
public class BrandController {
	
	@Reference
	private BrandService brandService;
	
	/**
	 * 通过get查找方式查找所有品牌
	 * @return
	 */
	@RequestMapping("/findAll")
	public List<TbBrand> findAll() {
		return brandService.findAll();
	}
	
	/**
	 * 分页查找
	 * @param page
	 * @param size
	 * @return
	 */
	@RequestMapping("/findPage")
	public PageResult findPage( int page, int size ) {
		return brandService.findPage(page, size);
	}
	
	@RequestMapping("/add")
	public Result add(@RequestBody TbBrand brand) {
		
		try {
			brandService.add(brand);
			return new Result(true, "success");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new Result(false, "添加失败");
		}
	}
	
	/**
	 * 根据id查找品牌
	 * @param id
	 * @return
	 */
	@RequestMapping("/findOne")
	public TbBrand findOne(Long id) {
		return brandService.findOne(id);
	}
	
	/**
	 * 更新品牌
	 * @param brand
	 * @return
	 */
	@RequestMapping("/update")
	public Result update(@RequestBody TbBrand brand) {
		try {
			brandService.update(brand);
			return new Result(true, "修改成功");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new Result(false, "修改失败");
		}
		
	}
	
	/**
	 * 删除根据ids
	 * @param ids
	 * @return
	 */
	@RequestMapping("/delete")
	public Result delete(Long [] ids) {
		try {
			brandService.delete(ids);
			return new Result(true, "删除成功");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new Result(false, "删除失败");
		}
	}
	
	/**
	 * 查找
	 * @param brand
	 * @param page
	 * @param size
	 * @return
	 */
	@RequestMapping("/search")
	public PageResult search(@RequestBody TbBrand brand, int page, int size  ){
		return brandService.findPage(brand, page, size);		
	}
	
	
	@RequestMapping("/selectOptionList")
	public List<Map> selectOptionList(){
		return brandService.selectOptionList();
	}
	
	
}
