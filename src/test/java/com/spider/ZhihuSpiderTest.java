package com.spider;

import us.codecraft.webmagic.Spider;

public class ZhihuSpiderTest {
	public static void main(String[] args) throws Exception {
		String answerUrl = "https://www.zhihu.com/question/48804993";
		Spider.create(new ZhihuSpider()).addUrl(answerUrl).thread(1).run();
	}
}
