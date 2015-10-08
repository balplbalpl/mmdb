package com.mmdb.util;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.mmdb.core.utils.JsonUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Dom4j 生成XML文档与解析XML文档
 *
 * @author XIE
 */
public class XmlUtil {

    /**
     * 获取document文件
     *
     * @param inputStream 输入流
     * @return
     * @throws DocumentException
     * @throws IOException
     */
    public static Document getDocumnet(InputStream inputStream) throws DocumentException, IOException {
        return new SAXReader().read(inputStream);
    }

    /**
     * 创建分类的XML文件
     *
     * @param list
     * @return
     * @throws IOException
     */
    public static InputStream createCiCateXml(List<Map<String, Object>> list) throws IOException {
        Document document = DocumentHelper.createDocument();
        Element employees = document.addElement("categorys");
        for (Map<String, Object> map : list) {
            Element employee = employees.addElement("category");
            Element id = employee.addElement("id");
            id.setText(map.get("id").toString());
            Element name = employee.addElement("name");
            name.setText(map.get("name").toString());
            Element image = employee.addElement("image");
            image.setText(map.get("image") != null ? map.get("image").toString() : "");
            Element clientId = employee.addElement("clientId");
            clientId.setText(map.get("clientId") != null ? map.get("clientId").toString() : "");
            Element parent = employee.addElement("parent");
            parent.setText(map.get("parent") != null ? map.get("parent").toString() : "");
            Element children = employee.addElement("children");
            children.setText(map.get("children") != null ? JsonUtil.encodeByJackSon(map.get("children")) : "");
            Element owner = employee.addElement("owner");
            owner.setText(map.get("owner") != null ? map.get("owner").toString() : "");
            Element major = employee.addElement("major");
            if (map.get("major") != null) {
                Map<String, Object> omajor = (Map<String, Object>) map.get("major");
                major.setText(omajor.get("name").toString());
                Element major2 = employee.addElement("ownMajor");
                if (map.get("ownMajor") != null) {
                    Map<String, Object> ownmajor = (Map<String, Object>) map.get("ownMajor");
                    major2.setText(ownmajor.get("name").toString());
                } else {
                    major2.setText("");
                }
            } else {
                major.setText("");
            }
            Element extendAttributes = employee.addElement("extendAttributes");
            if (map.get("extendAttributes") != null) {
                List<Map<String, Object>> extendAtts = (List<Map<String, Object>>) map.get("extendAttributes");
                for (Map<String, Object> m : extendAtts) {
                    Element extendAttribute = extendAttributes.addElement("attribute");
                    Element attrName = extendAttribute.addElement("name");
                    attrName.setText(m.get("name").toString());
                    Element required = extendAttribute.addElement("required");
                    required.setText(m.get("required").toString());
                    Element level = extendAttribute.addElement("level");
                    String le = m.get("level") != null ? m.get("level").toString() : "";
                    level.setText(le);
                }
            }
            Element selfAttributes = employee.addElement("selfAttributes");
            if (map.get("selfAttributes") != null) {
                List<Map<String, Object>> selfAtts = (List<Map<String, Object>>) map.get("selfAttributes");
                for (Map<String, Object> m : selfAtts) {
                    Element extendAttribute = selfAttributes.addElement("attribute");
                    Element attrName = extendAttribute.addElement("name");
                    attrName.setText(m.get("name").toString());
                    Element required = extendAttribute.addElement("required");
                    required.setText(m.get("required").toString());
                    Element level = extendAttribute.addElement("level");
                    String le = m.get("level") != null ? m.get("level").toString() : "";
                    level.setText(le);
                }
            }
        }
        // 从字符串获取字节写入流
        return new ByteArrayInputStream(document.asXML().getBytes("utf-8"));
    }
    
    /**
     * 创建分类的XML文件
     *
     * @param list
     * @return
     * @throws IOException
     */
    public static InputStream createKpiCateXml(List<Map<String, Object>> list) throws IOException {
        Document document = DocumentHelper.createDocument();
        Element employees = document.addElement("categorys");
        for (Map<String, Object> map : list) {
            Element employee = employees.addElement("category");
            Element id = employee.addElement("id");
            id.setText(map.get("id").toString());
            Element name = employee.addElement("name");
            name.setText(map.get("name").toString());
            Element parent = employee.addElement("parent");
            parent.setText(map.get("parent").toString());
            Element image = employee.addElement("image");
            image.setText(map.get("image") != null ? map.get("image").toString() : "");
            Element owner = employee.addElement("owner");
            owner.setText(map.get("owner") != null ? map.get("owner").toString() : "");
        }
        // 从字符串获取字节写入流
        return new ByteArrayInputStream(document.asXML().getBytes("utf-8"));
    }

    /**
     * 解析配置项分类的XML文件
     *
     * @param inputStream 文件输入流
     * @return
     * @throws Exception
     */
    public static Map<String, Map<String, Object>> parserCiCateXml(InputStream inputStream) throws Exception {
        Map<String, Map<String, Object>> retMap = new LinkedHashMap<String, Map<String, Object>>();
        Document document = getDocumnet(inputStream);
        Element categorys = document.getRootElement();
        for (Iterator<Element> i = categorys.elementIterator(); i.hasNext(); ) {
            Map<String, Object> map = new LinkedHashMap<String, Object>();
            Element category = i.next();
            String id = category.elementTextTrim("id");
            String name = category.elementTextTrim("name");
            String image = category.elementTextTrim("image");
            String clientId = category.elementTextTrim("clientId");
            String parent = category.elementTextTrim("parent");
            String children = category.elementTextTrim("children");
            String major = category.elementTextTrim("major");
            String ownMajor = category.elementTextTrim("ownMajor");
            Element selfAttributes = category.element("selfAttributes");
            Map<String, Object> selfAttrs = new LinkedHashMap<String, Object>();
            for (Iterator<Element> it = selfAttributes.elementIterator(); it.hasNext(); ) {
                Map<String, String> attrMap = new LinkedHashMap<String, String>();
                Element sattr = it.next();
                String attrname = sattr.elementTextTrim("name");
                String required = sattr.elementTextTrim("required");
                // String defaultVal = sattr.elementTextTrim("defaultVal");
                String level = sattr.elementTextTrim("level");
                // String type = sattr.elementTextTrim("type");
                // String sources = sattr.elementTextTrim("sources");
                attrMap.put("name", attrname);
                attrMap.put("required", required);
                attrMap.put("defaultVal", "");
                attrMap.put("hide", "false");
                attrMap.put("level", level);
                attrMap.put("type", "String");
                attrMap.put("sources", "[\"CMDB\",\"EXCEL\",\"PAGE\"]");
                selfAttrs.put(attrname, attrMap);
            }
            map.put("selfAttrs", selfAttrs);
            map.put("id", id);
            map.put("name", name);
            map.put("image", image);
            map.put("clientId", clientId);
            map.put("children", JsonUtil.decodeByJackSon(children, List.class));
            map.put("parent", parent);
            map.put("major", major);
            map.put("ownMajor", ownMajor);
            retMap.put(id, map);
        }
        return retMap;
    }
    
    /**
     * 解析配置项分类的XML文件
     *
     * @param inputStream 文件输入流
     * @return
     * @throws Exception
     */
    public static Map<String, Map<String, Object>> parserKpiCateXml(InputStream inputStream) throws Exception {
        Map<String, Map<String, Object>> retMap = new LinkedHashMap<String, Map<String, Object>>();
        Document document = getDocumnet(inputStream);
        Element categorys = document.getRootElement();
        for (Iterator<Element> i = categorys.elementIterator(); i.hasNext(); ) {
            Map<String, Object> map = new LinkedHashMap<String, Object>();
            Element category = i.next();
            String id = category.elementTextTrim("id");
            String name = category.elementTextTrim("name");
            String parent = category.elementText("parent");
            String image = category.elementText("image");
            String owner = category.elementText("owner");
            map.put("id", id);
            map.put("name", name);
            map.put("parent",parent);
            map.put("image", image);
            map.put("owner", owner);
            retMap.put(id, map);
        }
        return retMap;
    }
    
    /**
	 * 创建关系分类的XML文件
	 * 
	 * @param list
	 * @return
	 * @throws IOException
	 */
	public static InputStream createRelXml(List<Map<String, Object>> list) throws IOException {
		Document document = DocumentHelper.createDocument();
		Element employees = document.addElement("categorys");
		for (Map<String, Object> map : list) {
			Element employee = employees.addElement("category");
			Element id = employee.addElement("id");
			id.setText(map.get("id").toString());
			Element name = employee.addElement("name");
			name.setText(map.get("name").toString());
			Element image = employee.addElement("image");
			image.setText(map.get("image") != null ? map.get("image").toString() : "");
			Element parent = employee.addElement("parent");
			parent.setText(map.get("parent") != null ? map.get("parent").toString() : "");
			Element owner = employee.addElement("owner");
			owner.setText(map.get("owner") != null ? map.get("owner").toString() : "");
			Element children = employee.addElement("children");
			children.setText(map.get("children") != null ? JsonUtil.encodeByJackSon(map.get("children")) : "");
			Element extendAttributes = employee.addElement("extendAttributes");
			if (map.get("extendAttributes") != null) {
				List<Map<String, Object>> extendAtts = (List<Map<String, Object>>) map.get("extendAttributes");
				for (Map<String, Object> m : extendAtts) {
					Element extendAttribute = extendAttributes.addElement("attribute");
					Element attrName = extendAttribute.addElement("name");
					attrName.setText(m.get("name").toString());
					Element required = extendAttribute.addElement("required");
					required.setText(m.get("required").toString());
					Element hide = extendAttribute.addElement("hide");
					hide.setText(m.get("hide").toString());
				}
			}
			Element selfAttributes = employee.addElement("selfAttributes");
			if (map.get("selfAttributes") != null) {
				List<Map<String, Object>> selfAtts = (List<Map<String, Object>>) map.get("selfAttributes");
				for (Map<String, Object> m : selfAtts) {
					Element extendAttribute = selfAttributes.addElement("attribute");
					Element attrName = extendAttribute.addElement("name");
					attrName.setText(m.get("name").toString());
					Element required = extendAttribute.addElement("required");
					required.setText(m.get("required").toString());
					Element hide = extendAttribute.addElement("hide");
					hide.setText(m.get("hide").toString());
				}
			}
		}
		// 从字符串获取字节写入流
		return new ByteArrayInputStream(document.asXML().getBytes("utf-8"));
	}
    
	/**
	 * 解析关系分类的XML文件
	 * 
	 * @param inputStream
	 *            文件输入流
	 * @return
	 * @throws Exception
	 */
	public static Map<String, Map<String, Object>> parserRelCateXml(InputStream inputStream) throws Exception {
		Map<String, Map<String, Object>> retMap = new HashMap<String, Map<String, Object>>();
		Document document = getDocumnet(inputStream);
		Element categorys = document.getRootElement();
		for (Iterator<Element> i = categorys.elementIterator(); i.hasNext();) {
			Map<String, Object> map = new HashMap<String, Object>();
			Element category = i.next();
			String id = category.elementTextTrim("id");
			String name = category.elementTextTrim("name");
			String parent = category.elementTextTrim("parent");
			String children = category.elementTextTrim("children");
			String owner = category.elementTextTrim("owner");
			Element selfAttributes = category.element("selfAttributes");
			Map<String, Object> selfAttrs = new HashMap<String, Object>();
			for (Iterator<Element> it = selfAttributes.elementIterator(); it.hasNext();) {
				Map<String, String> attrMap = new HashMap<String, String>();
				Element sattr = it.next();
				String attrname = sattr.elementTextTrim("name");
				String required = sattr.elementTextTrim("required");
				// String defaultVal = sattr.elementTextTrim("defaultVal");
				String hide = sattr.elementTextTrim("hide");
				// String type = sattr.elementTextTrim("type");
				// String sources = sattr.elementTextTrim("sources");
				attrMap.put("name", attrname);
				attrMap.put("required", required);
				attrMap.put("defaultVal", "");
				attrMap.put("hide", hide);
				attrMap.put("type", "String");
				attrMap.put("sources", "[\"CMDB\",\"EXCEL\",\"PAGE\"]");
				selfAttrs.put(attrname, attrMap);
			}
			map.put("selfAttrs", selfAttrs);
			map.put("id", id);
			map.put("name", name);
			map.put("children", JsonUtil.decodeByJackSon(children, List.class));
			map.put("parent", parent);
			map.put("owner", owner);
			retMap.put(id, map);
		}
		return retMap;
	}
}