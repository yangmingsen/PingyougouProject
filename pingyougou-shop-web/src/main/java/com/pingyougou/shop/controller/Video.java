package com.pingyougou.shop.controller;

import java.io.File;
import java.io.FileInputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/video")
public class Video {


	/**
	 * 视频流读取
	 * @param id
	 * @param response
	 * @throws Exception
	 */
	@RequestMapping("/play")
	public @ResponseBody void video(HttpServletResponse response)throws Exception{
		File file = new File("F:\\视频\\完整8.0_1.mp4");
		FileInputStream in = new FileInputStream(file);
		ServletOutputStream out = response.getOutputStream();
		byte[] b = null;
		while(in.available() >0) {
			if(in.available()>10240) {
				b = new byte[10240];
			}else {
				b = new byte[in.available()];
			}
			in.read(b, 0, b.length);
			out.write(b, 0, b.length);
		}
		in.close();
		out.flush();
		out.close();
	}

	
}
