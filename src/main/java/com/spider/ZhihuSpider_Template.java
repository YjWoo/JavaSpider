package com.spider;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.JsonPathSelector;

/**
 * 知乎回答内的图片抓取
 * 
 * @author YjWoo
 *
 */
public class ZhihuSpider_Template implements PageProcessor {
	// 设置site参数
	private Site site = Site.me().setRetryTimes(10).setSleepTime(2500).setTimeOut(5000)
			// 设置自己的Cookie
			.addCookie("...", "...")
			// 设置User-Agent
			.setUserAgent("...");

	private static final String URL_question = "^https://www\\.zhihu\\.com/question/\\d+$";

	private static final String URL_answer = "https://www\\.zhihu\\.com/question/\\d+/answer/\\d+";

	private String questionId;
	private int answerNum;

	public void process(Page page) {

		if (page.getUrl().regex(URL_answer).match()) {
			String[] ps = page.getUrl().toString().split("/");
			String answerName = ps[ps.length - 1];

			List<String> urlList = page.getHtml().xpath("//div[@class=RichContent-inner]//img/@data-original").all();
			String questionTitle = page.getHtml().xpath("//h1[@class=QuestionHeader-title]/text()").toString();
			System.out.println("title：" + questionTitle);
			System.out.println(urlList);
			System.out.println(urlList.size());
			List<String> url = new ArrayList<String>();

			for (int i = 0; i < urlList.size(); i = i + 2) {
				if (urlList.get(i).startsWith("https://")) {
					url.add(urlList.get(i));
				}
			}

			String filePath = "E:\\ZhihuPic\\";
			try {
				downLoadPics(url, questionTitle, filePath, answerName);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (page.getUrl().regex(URL_question).match()) {
			String[] ps = page.getUrl().toString().split("/");
			questionId = ps[ps.length - 1];
			System.out.println(questionId);

			Pattern pattern = Pattern.compile("\\d+");
			Matcher matcher = pattern
					.matcher(page.getHtml().xpath("//h4[@class=List-headerText]/span/text()").toString());
			if (matcher.find()) {
				answerNum = Integer.parseInt(matcher.group());
				System.out.println(matcher.group());
				System.out.println("AnswerNum: " + answerNum);
			}

			int limit = 20;
			int i = 0;
			while (i <= answerNum / limit) {
				int offset = i * limit > answerNum ? answerNum : i * limit;
				String url = "https://www.zhihu.com/api/v4/questions/" + questionId
						+ "/answers?include=data%5B%2A%5D.is_normal%2Cadmin_closed_comment%2Creward_info%2Cis_collapsed%2Cannotation_action%2Cannotation_detail%2Ccollapse_reason%2Cis_sticky%2Ccollapsed_by%2Csuggest_edit%2Ccomment_count%2Ccan_comment%2Ccontent%2Ceditable_content%2Cvoteup_count%2Creshipment_settings%2Ccomment_permission%2Ccreated_time%2Cupdated_time%2Creview_info%2Cquestion%2Cexcerpt%2Crelationship.is_authorized%2Cis_author%2Cvoting%2Cis_thanked%2Cis_nothelp%2Cupvoted_followees%3Bdata%5B%2A%5D.mark_infos%5B%2A%5D.url%3Bdata%5B%2A%5D.author.follower_count%2Cbadge%5B%3F%28type%3Dbest_answerer%29%5D.topics&offset="
						+ offset + "&limit=" + limit + "&sort_by=default";
				page.addTargetRequest(url);
				i++;
			}
		} else {
			List<String> id = new JsonPathSelector("$.data[*].id").selectList(page.getRawText());
			if (answerNum == 0) {
				// 默认情况下，打印该问题下回答数量
				System.out.println("answerNum:" + Integer
						.valueOf((new JsonPathSelector("$.paging.totals").selectList(page.getRawText())).get(0)));
			}
			for (int i = 0; i < id.size(); i++) {
				String answerUrl = "https://www.zhihu.com/question/" + questionId + "/answer/" + id.get(i);
				page.addTargetRequest(answerUrl);
			}
		}

	}

	public Site getSite() {
		return site;
	}

	public static boolean downLoadPics(List<String> imgUrls, String title, String filePath, String answerName)
			throws Exception {
		boolean isSuccess = true;

		// 文件路径+标题
		String dir = filePath + title;
		// 创建
		File fileDir = new File(dir);
		fileDir.mkdirs();

		int i = 1;
		// 循环下载图片
		for (String imgUrl : imgUrls) {
			URL url = new URL(imgUrl);
			// 打开网络输入流
			DataInputStream dis = new DataInputStream(url.openStream());
			String newImageName = dir + "/" + answerName + "pic" + i + ".jpg";
			// 建立一个新的文件
			FileOutputStream fos = new FileOutputStream(new File(newImageName));
			byte[] buffer = new byte[1024];
			int length;
			System.out.println("Download....picture:no." + i + " ......wait a moment");
			// 开始填充数据
			while ((length = dis.read(buffer)) > 0) {
				fos.write(buffer, 0, length);
			}
			dis.close();
			fos.close();
			System.out.println("no." + i + " pic download successful......");
			i++;
		}
		return isSuccess;
	}

}