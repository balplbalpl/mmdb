package com.mmdb.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import com.mmdb.core.log.Log;
import com.mmdb.core.log.LogFactory;

/**
 * 
 * @author TY
 * @version 2015年5月18日
 */
public class StringXmlUtil {
	private static Log log = LogFactory.getLogger("StringXmlUtil");
	
	/**
	 * 解析保存View（视图）的xml内容 
	 * @param stringXml
	 * @return
	 */
	public static List<Map<String,String>> analysisStringXml(String stringXml){
//		 String stringXml = "<mxGraphModel grid=\"1\" gridSize=\"10\" guides=\"1\" tooltips=\"0\" connect=\"1\" fold=\"1\" page=\"0\" pageScale=\"1\" pageWidth=\"826\" pageHeight=\"1169\"><root><mxCell id=\"0\"/><mxCell id=\"1\" parent=\"0\"/><mxCell id=\"ci_5B2264637631222C22313233225D\" value=\"123\" style=\"image;image=../resource/svg/CI.svg\" vertex=\"1\" parent=\"1\"><mxGeometry x=\"460\" y=\"230\" width=\"80\" height=\"80\" as=\"geometry\"/></mxCell><mxCell id=\"ci_5B2264637632222C22333435225D\" value=\"345\" style=\"image;image=../resource/svg/CI.svg\" vertex=\"1\" parent=\"1\"><mxGeometry x=\"830\" y=\"400\" width=\"80\" height=\"80\" as=\"geometry\"/></mxCell><mxCell id=\"2\" value=\"\" style=\"exitX=1;exitY=0.75\" edge=\"1\" parent=\"1\" source=\"ci_5B2264637631222C22313233225D\" target=\"ci_5B2264637632222C22333435225D\"><mxGeometry relative=\"1\" as=\"geometry\"/></mxCell></root></mxGraphModel>";
		stringXml = stringXml.replace("\\\"", "\"");
		Document document;
         List<Map<String,String>> resL = new ArrayList<Map<String,String>>();
		try {
			document = DocumentHelper.parseText(stringXml);
			Element rootElt = document.getRootElement();
			Element element = rootElt.element("root");
			for(Iterator iterator = element.elementIterator("mxCell"); iterator.hasNext();) {
				Map<String,String> restM = new HashMap<String,String>();
				Element elem = (Element)iterator.next();
				restM.put("id",elem.attributeValue("id"));
				System.out.println(elem.attributeValue("id"));
				restM.put("value",elem.attributeValue("value"));
				restM.put("style",elem.attributeValue("style"));
				restM.put("image",elem.attributeValue("image"));
				resL.add(restM);
			}
		} catch (DocumentException e) {
			log.eLog("字符串格式有误，解析异常!");
		}
		
		return resL;
	}
}
