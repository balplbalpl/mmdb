package com.mmdb.rest.download;

import java.io.File;
import java.net.URLDecoder;
import java.util.List;

import net.sf.json.JSONObject;

import org.mortbay.util.UrlEncoded;
import org.restlet.data.Disposition;
import org.restlet.data.Encoding;
import org.restlet.data.MediaType;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.FileRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import org.springframework.context.ApplicationContext;

import com.mmdb.core.log.Log;
import com.mmdb.core.log.LogFactory;
import com.mmdb.rest.BaseRest;
import com.mmdb.util.FileManager;

public class FileDownload extends BaseRest {
	private Log log = LogFactory.getLogger("CiCateRest");

	@Override
	public void ioc(ApplicationContext context) {

	}

	@Override
	public Representation getHandler() throws Exception {
		String param1 = getValue("param1");
		return exprot(param1);
	}

	@Override
	public Representation postHandler(Representation entity) throws Exception {
		return notFindMethod(entity);
	}

	@Override
	public Representation putHandler(Representation entity) throws Exception {
		return notFindMethod(entity);
	}

	@Override
	public Representation delHandler(Representation entity) throws Exception {
		return notFindMethod(entity);
	}

	private Representation exprot(String filename) {
		String path = FileManager.getInstance().getPath();
		File file = new File(path + filename);
		if (file.exists()) {

			MediaType type = MediaType.ALL;
			if (file.getName().endsWith(".xml")) {
				type = MediaType.APPLICATION_ALL_XML;
			} else if (file.getName().endsWith(".json")) {
				type = MediaType.APPLICATION_JSON;
			} else if (file.getName().endsWith(".xls")) {
				type = MediaType.APPLICATION_EXCEL;
			} else if (file.getName().endsWith(".zip")) {
				type = MediaType.APPLICATION_ZIP;
			} else if (file.getName().endsWith(".svg")) {
				type = MediaType.IMAGE_SVG;
			} else if (file.getName().endsWith(".pdf")) {
				type = MediaType.APPLICATION_PDF;
			}

			FileRepresentation fileRe = new FileRepresentation(file, type,
					30000);
			List<Encoding> encodings = fileRe.getEncodings();
			Disposition disposition = fileRe.getDisposition();
			disposition.setType("attachment");
			try {
				disposition.setFilename(new String(filename.getBytes("GBK"),
						"iso8859-1"));
			} catch (Exception e) {
				disposition.setFilename(UrlEncoded.encodeString(filename,
						"UTF-8"));
			}
			fileRe.setAutoDeleting(true);
			return fileRe;
		}
		return new StringRepresentation("文件不存在");
	}

//	private Representation exprotChromeLiunx(){
//		
//	}
//	private Representation exprotFirefoxLiunx(){
//		
//	}
//	private Representation exprotChromeWin_64(){
//		
//	}
//	private Representation exprotFirefoxLiunx(){
//		
//	}
}
