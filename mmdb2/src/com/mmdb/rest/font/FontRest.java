package com.mmdb.rest.font;

import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;
import org.springframework.context.ApplicationContext;

import com.mmdb.core.log.Log;
import com.mmdb.core.log.LogFactory;
import com.mmdb.core.utils.FileUtil;
import com.mmdb.core.utils.JsonUtil;
import com.mmdb.core.utils.ProjectInfo;
import com.mmdb.core.utils.Return;
import com.mmdb.core.utils.SysProperties;
import com.mmdb.rest.BaseRest;

public class FontRest extends BaseRest {
	// private Log log = LogFactory.getLogger("FontRest");

	// 中文字体缓存
	private Map<String, Object> FONTCACHE_ZH = new HashMap<String, Object>();
	private Map<String, Object> FONTCACHE_EN = new HashMap<String, Object>();

	@Override
	public void ioc(ApplicationContext context) {

	}

	@Override
	public Representation getHandler() throws Exception {
		return notFindMethod(null);
	}

	@Override
	public Representation postHandler(Representation entity) throws Exception {
		JSONObject params = parseEntity(entity);
		JSONArray fonts = params.getJSONArray("font");
		return getFontStyles(fonts);
	}

	@Override
	public Representation putHandler(Representation entity) throws Exception {
		return notFindMethod(entity);
	}

	@Override
	public Representation delHandler(Representation entity) throws Exception {
		return notFindMethod(entity);
	}

	private Representation getFontStyles(List<String> fonts) throws Exception {
		Return ret = new Return();
		log.dLog("getFontStyles");
		Map<String, Object> map_zh = new HashMap<String, Object>();
		Map<String, Object> map_en = new HashMap<String, Object>();
		if (fonts == null || fonts.size() == 0) {
			throw new Exception("参数不能为空");
		}
		if (FONTCACHE_ZH != null && FONTCACHE_ZH.size() > 0) {
			map_zh.putAll(FONTCACHE_ZH);
		} else {
			String url_zh = SysProperties.get("font.style.zh");
			if (url_zh.startsWith("resource/css")) {
				url_zh = ProjectInfo.getProjectRealPathConvert() + url_zh;
			}
			String json_zh = null;
			try {
				json_zh = FileUtil.read2string(url_zh, "UTF-8", false);
			} catch (Exception e) {
				url_zh = "/" + url_zh;
				json_zh = FileUtil.read2string(url_zh, "UTF-8", false);
			}
			int startNum = json_zh.indexOf("({") + 1;
			int endNum = json_zh.indexOf("});") + 1;
			json_zh = json_zh.substring(startNum, endNum);
			map_zh = JsonUtil.decodeByJackSon(json_zh, Map.class);
			FONTCACHE_ZH.putAll(map_zh);
		}
		if (FONTCACHE_EN != null && FONTCACHE_EN.size() > 0) {
			map_en.putAll(FONTCACHE_EN);
		} else {
			String url_en = SysProperties.get("font.style.en");
			if (url_en.startsWith("resource/css")) {
				url_en = ProjectInfo.getProjectRealPathConvert() + url_en;
			}
			String json_en = null;
			try {
				json_en = FileUtil.read2string(url_en, "UTF-8", false);
			} catch (Exception e) {
				url_en = "/" + url_en;
				json_en = FileUtil.read2string(url_en, "UTF-8", false);
			}
			int startNum = json_en.indexOf("({") + 1;
			int endNum = json_en.indexOf("});") + 1;
			json_en = json_en.substring(startNum, endNum);
			map_en = JsonUtil.decodeByJackSon(json_en, Map.class);
			FONTCACHE_EN.putAll(map_en);
		}
		Map<String, Object> retMap = new HashMap<String, Object>();
		if (map_zh != null && map_zh.containsKey("glyphs") && map_en != null
				&& map_en.containsKey("glyphs")) {
			Map<String, Object> gMap_zh = (Map<String, Object>) map_zh
					.get("glyphs");
			Map<String, Object> gMap_en = (Map<String, Object>) map_en
					.get("glyphs");
			for (String font : fonts) {
				if (isChinese(font.charAt(0))) {
					if (gMap_zh.containsKey(font)) {
						Object obj = gMap_zh.get(font);
						retMap.put(font, obj);
					}
				} else {
					if (gMap_en.containsKey(font)) {
						Object obj = gMap_en.get(font);
						retMap.put(font, obj);
					}
				}
			}
		}
		ret.setData(retMap).setMessage("获取字体样式完成");
		log.dLog("getFontStyles success");
		return new JsonRepresentation(ret.toString());
	}

	/**
	 * 根据Unicode编码完美的判断中文汉字和符号
	 * 
	 * @param c
	 * @return
	 */
	private boolean isChinese(char c) {
		Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
		if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
				|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
				|| ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
				|| ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
				|| ub == Character.UnicodeBlock.GENERAL_PUNCTUATION) {
			return true;
		}
		return false;
	}
}
