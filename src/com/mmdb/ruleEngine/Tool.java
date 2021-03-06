package com.mmdb.ruleEngine;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.mmdb.core.utils.SysProperties;
import com.mmdb.model.categroy.storage.RelCategoryStorage;
import com.mmdb.model.info.CiInformation;
import com.mmdb.model.info.storage.CiInfoStorage;

public class Tool {
	private static CiInfoStorage ciInfoStorage = new CiInfoStorage();
	private static RelCategoryStorage rcStorage = new RelCategoryStorage();
	public static final int getBuff = 1000;
	
	public static List<CiInformation> getCis(String categoryId, String ciAttr, String val, String op, String direction){
		try {
			val = val.trim();
			if(op.equals("equals")){
				List<CiInformation> result = new ArrayList<CiInformation>();
				List<CiInformation> infos = ciInfoStorage.getByProperty(ciAttr, val);
				if(infos!=null){
					for(CiInformation ci:infos){
						if(ci.getCategoryId().equals(categoryId)){
							result.add(ci);
						}
					}
				}
				if(result.size()<=0){
					List<CiInformation> infos_l = ciInfoStorage.getByProperty(ciAttr, val.toLowerCase());
					if(infos_l!=null){
						for(CiInformation ci:infos_l){
							if(ci.getCategoryId().equals(categoryId)){
								result.add(ci);
							}
						}
					}
				}
				if(result.size()<=0){
					List<CiInformation> infos_u = ciInfoStorage.getByProperty(ciAttr, val.toUpperCase());
					if(infos_u!=null){
						for(CiInformation ci:infos_u){
							if(ci.getCategoryId().equals(categoryId)){
								result.add(ci);
							}
						}
					}
				}
				return result;
			}else{
				List<CiInformation> infos = ciInfoStorage.getByCategory(categoryId);
				List<CiInformation> result = new ArrayList<CiInformation>();
				if(infos!=null){
					for(CiInformation ci:infos){
						if(ci.getData()!=null){
							if(ci.getData().get(ciAttr)!=null){
								String a = ci.getData().get(ciAttr).toString().trim();
								if(direction.equals("positive")){
									if(compare(a, op, val)){
										result.add(ci);
									}
								}else{
									if(compare(val, op, a)){
										result.add(ci);
									}
								}
							}
						}
					}
				}
				return result;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new ArrayList<CiInformation>();
		}
	}
	
//	public static Set<String> getImpactedCis(JSONArray cis, JSONObject eventJs){
//		try {
//			List<JSONObject> rules = CDao.blackList;
//			Set<String> result = new HashSet<String>();
//			for(int i=0;i<cis.size();i++){
//				JSONArray ciJs = cis.getJSONArray(i);
//				boolean ignore = false;
//				for(JSONObject rule:rules){
//					Set<String> ruleKeys = rule.keySet();
//					if(rule.containsKey("cateid")){
//						if(rule.containsKey("ciid")){
//							if(ciJs.getString(0).equals(rule.getString("cateid"))&&ciJs.getString(1).equals(rule.getString("ciid"))){
//								boolean b = false;
//								for(String key:ruleKeys){
//									if(!"cateid".equals(key)&&!"ciid".equals(key)){
//										if(!matchLike(rule.getString(key), eventJs.getString(key))){
//											b = true;
//											break;
//										}
//									}
//								}
//								if(!b){
//									ignore = true;
//									break;
//								}
//							}
//						}else{
//							if(ciJs.getString(0).equals(rule.getString("cateid"))){
//								boolean b = false;
//								for(String key:ruleKeys){
//									if(!"cateid".equals(key)){
//										if(!matchLike(rule.getString(key), eventJs.getString(key))){
//											b = true;
//											break;
//										}
//									}
//								}
//								if(!b){
//									ignore = true;
//									break;
//								}
//							}
//						}
//					}else{
//						boolean b = false;
//						for(String key:ruleKeys){
//							if(!matchLike(rule.getString(key), eventJs.getString(key))){
//								b = true;
//								break;
//							}
//						}
//						if(!b){
//							ignore = true;
//							break;
//						}
//					}
//				}
//				
//				if(!ignore){
//					List<CiInformation> infos = ciInfoStorage.getByProperty("id", ciJs.getString(1));
//					if(infos!=null){
//						for(CiInformation ci:infos){
//							if(ci.getCategoryId().equals(ciJs.getString(0))){
//								final Node node = ciInfoStorage.getOne(ci);
//						        Traverser tr = null;
//						        TraversalDescription trDescription = Traversal.description();
//						        List<String> names = new ArrayList<String>();
//								List<RelCategory> rcs = rcStorage.getAll();
//								for (RelCategory rc : rcs) {
//									String name = rc.getId();
//									if (!name.equals(RelationshipTypes.MMDB2DCV) && !name.equals("所属") && !name.equals("发起") && !names.contains(name)) {
//										names.add(name);
//									}
//								}
//						        if(names.size()>0){
//						        	for (String name : names) {
//							            RelationshipType rs = StorageUtil.str2RelationType(name);
//							            trDescription = trDescription.depthFirst().relationships(rs, Direction.INCOMING);
//							        }
//							        tr = trDescription.traverse(node);
//							        for (Path position : tr) {
//							        	if(position!=null&&position.nodes()!=null&&position.nodes().iterator()!=null){
//							        		Iterator<Node> nodes = position.nodes().iterator();
//								            while (nodes.hasNext()) {
//								                Node node2 = nodes.next();
//								                
//								                JSONArray impactedCiJs = new JSONArray();
//								                System.out.println(node2.getId());
//								                System.out.println(ciInfoStorage.getOne(node2.getId()).getId());
//								                impactedCiJs.add(node2.getProperty("categoryId"));
//								                impactedCiJs.add(node2.getProperty("id"));
//								                result.add(impactedCiJs.toString());
//								            }
//							        	}
//							        }
//							        result.remove(ciJs.toString());
//						        }
//							}
//						}
//					}
//				}
//			}
//			return result;
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return new HashSet<String>();
//	}
	
	private static boolean matchLike(String exp, String val){
		exp = exp.trim().toLowerCase();
		val = val.trim().toLowerCase();
		if(exp.startsWith("*")){
			if(exp.endsWith("*")){
				if(val.contains(exp.subSequence(1, exp.length()-1))){
					return true;
				}
			}else{
				if(val.endsWith(exp.substring(1))){
					return true;
				}
			}
		}else{
			if(exp.endsWith("*")){
				if(val.startsWith(exp.substring(0, exp.length()-1))){
					return true;
				}
			}else{
				if(val.equals(exp)){
					return true;
				}
			}
		}
		return false;
	}
	
	public static boolean compare(String a, String op, String b){
		if(op.equals("equals")){
			return a.toLowerCase().equals(b.toLowerCase());
		}else if(op.equals("not equals")){
			return !a.toLowerCase().equals(b.toLowerCase());
		}else if(op.equals("contains")){
			return a.toLowerCase().contains(b.toLowerCase());
		}else if(op.equals("not contains")){
			return !a.toLowerCase().contains(b.toLowerCase());
		}else if(op.equals("startsWith")){
			return a.toLowerCase().startsWith(b.toLowerCase());
		}else if(op.equals("not startsWith")){
			return !a.toLowerCase().startsWith(b.toLowerCase());
		}else if(op.equals("endsWith")){
			return a.toLowerCase().endsWith(b.toLowerCase());
		}else if(op.equals("not endsWith")){
			return !a.toLowerCase().endsWith(b.toLowerCase());
		}else{
			return false;
		}
	}
	
	
	/**
	 * 将前台的long型时间戳转成string("yyyy-MM-dd HH:mm:ss")类型
	 * 
	 * 
	 * **/
	public static String longTostring(Long time){ 
	    SimpleDateFormat formatter; 
	    formatter = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss"); 
		Date date = new Date();
		try {
			date.setTime(time);
			String ctime = formatter.format(date);
			return ctime; 
		} catch (Exception e) {
			e.printStackTrace();
			return null; 
		} 
	} 
	
	/**
	 * 找路径
	 * 
	 * 
	 * **/
	public static String findPath(String dirName, String resourceName){ 
		String serverPath = getRealPath();
		File root = new File(serverPath);
		if(!root.exists()){
			serverPath = "/"+serverPath;
			root = new File(serverPath);
		}
		List<File> fileList = new ArrayList<File>();
		fileList.add(root);
		int level = 0;
		int dirLevel = -1;
		int resourceLevel = -1;
		File dirFile = null;
		File resourceFile = null;
		while(true){
			List<File> fileListNew = new ArrayList<File>();
			for(File dir:fileList){
				dir.exists();
				File[] children = dir.listFiles();
				for(File child:children){
					if(child.isDirectory()){
						if(child.getName().equals(dirName)){
							dirLevel = level;
							dirFile = child;
						}
						if(child.getName().equals(resourceName)){
							resourceLevel = level;
							resourceFile = child;
						}
						fileListNew.add(child);
					}
				}
			}
			if(dirFile!=null&&resourceFile!=null){
				break;
			}
			if(fileListNew.size()==0){
				break;
			}
			fileList = fileListNew;
			level++;
		}
		if(dirFile!=null&&resourceFile!=null){
			String ret = "";
			for(int i=-1;i<dirLevel;i++){
				ret = ret + "../";
			}
			ret = ret + resourceFile.getAbsolutePath().substring(serverPath.length());
			return ret;
		}else{
			return null;
		}
	}
	
	public static String getRealPath(){
		/*String path = Tool.class.getResource("/").toString();
		System.out.println("----------------  "+path);
		path = path.replaceAll("file:", "").replaceAll("WebRoot/WEB-INF/classes/|WEB-INF/classes/", "");
		System.out.println("----------------  "+path);*/
		return SysProperties.get("webappBase");
	}
	
	public static Double executePerfVal(String val, String exp){
		try{
			Double v = Double.parseDouble(val);
			if(exp==null||exp.length()==0){
				return v;
			}
			
			exp = "("+exp.replace("$v", v+"").replace(" ", "")+")";
			while(exp.contains("(")){
				int left = exp.lastIndexOf("(");
				int right = left+exp.substring(left).indexOf(")")+1;
				String bracket = exp.substring(left, right);
				bracket = bracket.substring(1, bracket.length()-1);
				String front_bracket = exp.substring(0, left);
				String last_bracket = exp.substring(right);
				while(bracket.contains("^")){
					String a_tri = bracket.substring(0, bracket.indexOf("^"));
					String b_tri = bracket.substring(bracket.indexOf("^")+1);
					Double a_tri_d = null;
					Double b_tri_d = null;
					String front_tri = "";
					String last_tri = "";
					boolean flag = false;
					for(int i=a_tri.length();i>0;i--){
						String a_tri_byte = a_tri.substring(i-1, i);
						if(a_tri_byte.equals("^")||a_tri_byte.equals("+")||a_tri_byte.equals("*")||a_tri_byte.equals("/")){
							a_tri_d = Double.parseDouble(a_tri.substring(i));
							front_tri = a_tri.substring(0, i);
							flag = true;
							break;
						}else if(a_tri_byte.equals("-")){
							boolean f = true;
							if(i>1){
								String ff = a_tri.substring(i-2, i-1);
								if(ff.equals("^")||ff.equals("+")||ff.equals("-")||ff.equals("*")||ff.equals("/")){
									f = false;
								}
							}else{
								f=false;
							}
							if(f){
								a_tri_d = Double.parseDouble(a_tri.substring(i));
								front_tri = a_tri.substring(0, i);
								flag = true;
								break;
							}
						}
					}
					if(!flag){
						a_tri_d = Double.parseDouble(a_tri);
					}
					flag = false;
					for(int i=0;i<b_tri.length();i++){
						String b_tri_byte = b_tri.substring(i, i+1);
						if(b_tri_byte.equals("^")||b_tri_byte.equals("+")||b_tri_byte.equals("*")||b_tri_byte.equals("/")){
							b_tri_d = Double.parseDouble(b_tri.substring(0, i));
							last_tri = b_tri.substring(i);
							flag = true;
							break;
						}else if(b_tri_byte.equals("-")){
							if(i>0){
								b_tri_d = Double.parseDouble(b_tri.substring(0, i));
								last_tri = b_tri.substring(i);
								flag = true;
								break;
							}
						}
					}
					if(!flag){
						b_tri_d = Double.parseDouble(b_tri);
					}
					Double ret_tri_d = Math.pow(a_tri_d, b_tri_d);
					bracket = front_tri + ret_tri_d + last_tri;
				}
				while(bracket.contains("*")||bracket.contains("/")){
					int con_index_multi = bracket.indexOf("*");
					if(con_index_multi<0){
						con_index_multi = bracket.indexOf("/");
					}else{
						if(bracket.indexOf("/")>=0){
							if(con_index_multi>bracket.indexOf("/")){
								con_index_multi = bracket.indexOf("/");
							}
						}
					}
					String con_multi = bracket.substring(con_index_multi, con_index_multi+1);
					String a_multi = bracket.substring(0, bracket.indexOf(con_multi));
					String b_multi = bracket.substring(bracket.indexOf(con_multi)+1);
					Double a_multi_d = null;
					Double b_multi_d = null;
					String front_multi = "";
					String last_multi = "";
					boolean flag = false;
					for(int i=a_multi.length();i>0;i--){
						String a_multi_byte = a_multi.substring(i-1, i);
						if(a_multi_byte.equals("+")||a_multi_byte.equals("*")||a_multi_byte.equals("/")){
							a_multi_d = Double.parseDouble(a_multi.substring(i));
							front_multi = a_multi.substring(0, i);
							flag = true;
							break;
						}else if(a_multi_byte.equals("-")){
							boolean f = true;
							if(i>1){
								String ff = a_multi.substring(i-2, i-1);
								if(ff.equals("+")||ff.equals("-")||ff.equals("*")||ff.equals("/")){
									f = false;
								}
							}else{
								f=false;
							}
							if(f){
								a_multi_d = Double.parseDouble(a_multi.substring(i));
								front_multi = a_multi.substring(0, i);
								flag = true;
								break;
							}
						}
					}
					if(!flag){
						a_multi_d = Double.parseDouble(a_multi);
					}
					flag = false;
					for(int i=0;i<b_multi.length();i++){
						String b_multi_byte = b_multi.substring(i, i+1);
						if(b_multi_byte.equals("+")||b_multi_byte.equals("*")||b_multi_byte.equals("/")){
							b_multi_d = Double.parseDouble(b_multi.substring(0, i));
							last_multi = b_multi.substring(i);
							flag = true;
							break;
						}else if(b_multi_byte.equals("-")){
							if(i>0){
								b_multi_d = Double.parseDouble(b_multi.substring(0, i));
								last_multi = b_multi.substring(i);
								flag = true;
								break;
							}
						}
					}
					if(!flag){
						b_multi_d = Double.parseDouble(b_multi);
					}
					Double ret_multi_d = con_multi.equals("*") ? (a_multi_d*b_multi_d):(a_multi_d/b_multi_d);
					bracket = front_multi + ret_multi_d + last_multi;
				}
				while(bracket.contains("+")||bracket.contains("-")){
					if(bracket.indexOf("-")==0&&bracket.lastIndexOf("-")==0&&bracket.indexOf("+")<0){
						break;
					}
					int con_index_plus = bracket.indexOf("+");
					if(con_index_plus<0){
						con_index_plus = bracket.indexOf("-");
						if(con_index_plus==0){
							con_index_plus = bracket.substring(1).indexOf("-")+1;
						}
					}else{
						int minus = bracket.indexOf("-");
						if(minus==0){
							minus = bracket.substring(1).indexOf("-")+1;
						}
						if(minus>0){
							if(con_index_plus>minus){
								con_index_plus = minus;
							}
						}
					}
					String con_plus = bracket.substring(con_index_plus, con_index_plus+1);
					String a_plus = bracket.substring(0, bracket.indexOf(con_plus));
					String b_plus = bracket.substring(bracket.indexOf(con_plus)+1);
					Double a_plus_d = Double.parseDouble(a_plus);
					Double b_plus_d = null;
					String front_plus = "";
					String last_plus = "";
					boolean flag = false;
					for(int i=0;i<b_plus.length();i++){
						String b_plus_byte = b_plus.substring(i, i+1);
						if(b_plus_byte.equals("+")){
							b_plus_d = Double.parseDouble(b_plus.substring(0, i));
							last_plus = b_plus.substring(i);
							flag = true;
							break;
						}else if(b_plus_byte.equals("-")){
							if(i>0){
								b_plus_d = Double.parseDouble(b_plus.substring(0, i));
								last_plus = b_plus.substring(i);
								flag = true;
								break;
							}
						}
					}
					if(!flag){
						b_plus_d = Double.parseDouble(b_plus);
					}
					Double ret_plus_d = con_plus.equals("+") ? (a_plus_d+b_plus_d):(a_plus_d-b_plus_d);
					bracket = front_plus + ret_plus_d + last_plus;
				}
				exp = front_bracket + Double.parseDouble(bracket) + last_bracket;
			}
			return Double.parseDouble(exp);
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	public static boolean checkPerfValExp(String exp){
		try{
			if(!exp.contains("$v")){
				return false;
			}
			exp = "("+exp.replace("$v", "0").replace(" ", "")+")";
			while(exp.contains("(")){
				int left = exp.lastIndexOf("(");
				int right = left+exp.substring(left).indexOf(")")+1;
				String bracket = exp.substring(left, right);
				bracket = bracket.substring(1, bracket.length()-1);
				String front_bracket = exp.substring(0, left);
				String last_bracket = exp.substring(right);
				while(bracket.contains("^")){
					String a_tri = bracket.substring(0, bracket.indexOf("^"));
					String b_tri = bracket.substring(bracket.indexOf("^")+1);
					Double a_tri_d = null;
					Double b_tri_d = null;
					String front_tri = "";
					String last_tri = "";
					boolean flag = false;
					for(int i=a_tri.length();i>0;i--){
						String a_tri_byte = a_tri.substring(i-1, i);
						if(a_tri_byte.equals("^")||a_tri_byte.equals("+")||a_tri_byte.equals("*")||a_tri_byte.equals("/")){
							a_tri_d = Double.parseDouble(a_tri.substring(i));
							front_tri = a_tri.substring(0, i);
							flag = true;
							break;
						}else if(a_tri_byte.equals("-")){
							boolean f = true;
							if(i>1){
								String ff = a_tri.substring(i-2, i-1);
								if(ff.equals("^")||ff.equals("+")||ff.equals("-")||ff.equals("*")||ff.equals("/")){
									f = false;
								}
							}else{
								f=false;
							}
							if(f){
								a_tri_d = Double.parseDouble(a_tri.substring(i));
								front_tri = a_tri.substring(0, i);
								flag = true;
								break;
							}
						}
					}
					if(!flag){
						a_tri_d = Double.parseDouble(a_tri);
					}
					flag = false;
					for(int i=0;i<b_tri.length();i++){
						String b_tri_byte = b_tri.substring(i, i+1);
						if(b_tri_byte.equals("^")||b_tri_byte.equals("+")||b_tri_byte.equals("*")||b_tri_byte.equals("/")){
							b_tri_d = Double.parseDouble(b_tri.substring(0, i));
							last_tri = b_tri.substring(i);
							flag = true;
							break;
						}else if(b_tri_byte.equals("-")){
							if(i>0){
								b_tri_d = Double.parseDouble(b_tri.substring(0, i));
								last_tri = b_tri.substring(i);
								flag = true;
								break;
							}
						}
					}
					if(!flag){
						b_tri_d = Double.parseDouble(b_tri);
					}
					bracket = front_tri + "0" + last_tri;
				}
				while(bracket.contains("*")||bracket.contains("/")){
					int con_index_multi = bracket.indexOf("*");
					if(con_index_multi<0){
						con_index_multi = bracket.indexOf("/");
					}else{
						if(bracket.indexOf("/")>=0){
							if(con_index_multi>bracket.indexOf("/")){
								con_index_multi = bracket.indexOf("/");
							}
						}
					}
					String con_multi = bracket.substring(con_index_multi, con_index_multi+1);
					String a_multi = bracket.substring(0, bracket.indexOf(con_multi));
					String b_multi = bracket.substring(bracket.indexOf(con_multi)+1);
					Double a_multi_d = null;
					Double b_multi_d = null;
					String front_multi = "";
					String last_multi = "";
					boolean flag = false;
					for(int i=a_multi.length();i>0;i--){
						String a_multi_byte = a_multi.substring(i-1, i);
						if(a_multi_byte.equals("+")||a_multi_byte.equals("*")||a_multi_byte.equals("/")){
							a_multi_d = Double.parseDouble(a_multi.substring(i));
							front_multi = a_multi.substring(0, i);
							flag = true;
							break;
						}else if(a_multi_byte.equals("-")){
							boolean f = true;
							if(i>1){
								String ff = a_multi.substring(i-2, i-1);
								if(ff.equals("+")||ff.equals("-")||ff.equals("*")||ff.equals("/")){
									f = false;
								}
							}else{
								f=false;
							}
							if(f){
								a_multi_d = Double.parseDouble(a_multi.substring(i));
								front_multi = a_multi.substring(0, i);
								flag = true;
								break;
							}
						}
					}
					if(!flag){
						a_multi_d = Double.parseDouble(a_multi);
					}
					flag = false;
					for(int i=0;i<b_multi.length();i++){
						String b_multi_byte = b_multi.substring(i, i+1);
						if(b_multi_byte.equals("+")||b_multi_byte.equals("*")||b_multi_byte.equals("/")){
							b_multi_d = Double.parseDouble(b_multi.substring(0, i));
							last_multi = b_multi.substring(i);
							flag = true;
							break;
						}else if(b_multi_byte.equals("-")){
							if(i>0){
								b_multi_d = Double.parseDouble(b_multi.substring(0, i));
								last_multi = b_multi.substring(i);
								flag = true;
								break;
							}
						}
					}
					if(!flag){
						b_multi_d = Double.parseDouble(b_multi);
					}
					bracket = front_multi + "0" + last_multi;
				}
				while(bracket.contains("+")||bracket.contains("-")){
					if(bracket.indexOf("-")==0&&bracket.lastIndexOf("-")==0&&bracket.indexOf("+")<0){
						break;
					}
					int con_index_plus = bracket.indexOf("+");
					if(con_index_plus<0){
						con_index_plus = bracket.indexOf("-");
						if(con_index_plus==0){
							con_index_plus = bracket.substring(1).indexOf("-")+1;
						}
					}else{
						int minus = bracket.indexOf("-");
						if(minus==0){
							minus = bracket.substring(1).indexOf("-")+1;
						}
						if(minus>0){
							if(con_index_plus>minus){
								con_index_plus = minus;
							}
						}
					}
					String con_plus = bracket.substring(con_index_plus, con_index_plus+1);
					String a_plus = bracket.substring(0, bracket.indexOf(con_plus));
					String b_plus = bracket.substring(bracket.indexOf(con_plus)+1);
					Double a_plus_d = Double.parseDouble(a_plus);
					Double b_plus_d = null;
					String front_plus = "";
					String last_plus = "";
					boolean flag = false;
					for(int i=0;i<b_plus.length();i++){
						String b_plus_byte = b_plus.substring(i, i+1);
						if(b_plus_byte.equals("+")){
							b_plus_d = Double.parseDouble(b_plus.substring(0, i));
							last_plus = b_plus.substring(i);
							flag = true;
							break;
						}else if(b_plus_byte.equals("-")){
							if(i>0){
								b_plus_d = Double.parseDouble(b_plus.substring(0, i));
								last_plus = b_plus.substring(i);
								flag = true;
								break;
							}
						}
					}
					if(!flag){
						b_plus_d = Double.parseDouble(b_plus);
					}
					bracket = front_plus + "0" + last_plus;
				}
				exp = front_bracket + Double.parseDouble(bracket) + last_bracket;
			}
			Double.parseDouble(exp);
			return true;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}
	
	public static Long transPerfValDate(Object date){
		Long ret = -1L;
		try{
			ret = Long.parseLong(date+"");
		}catch(Exception e){
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try{
			ret = sdf.parse(date+"").getTime();
		}catch(Exception e){
		}
		return ret;
	}
	
	public static boolean matchPerfMapping1(String exp){
		if("".equals(exp)){
			return true;	//如果条件为空，不用判断直接返回true
		}
		try{
			exp = "(" + exp.replace(" ", "") + ")";
			while(exp.contains("o.get(\"")){
				int start = exp.indexOf("o.get(\"");
				int end = start + exp.substring(start).indexOf("\")") + 2;
				String var = exp.substring(start, end);
				exp = exp.replace(var, "");
			}
			while(exp.contains(".equals(")){
				int start = exp.indexOf(".equals(");
				int end = start + exp.substring(start).indexOf(")") + 1;
				String var = exp.substring(start, end);
				exp = exp.replace(var, "=\"\"");
			}
			while(exp.contains(".contains(")){
				int start = exp.indexOf(".contains(");
				int end = start + exp.substring(start).indexOf(")") + 1;
				String var = exp.substring(start, end);
				exp = exp.replace(var, "=\"\"");
			}
			while(exp.contains("\"")){
				int start = exp.indexOf("\"");
				int end = start + exp.substring(start+1).indexOf("\"") + 2;
				String var = exp.substring(start, end);
				exp = exp.replace(var, "$var");
			}
			exp = exp.replace("$var=$var", "$exp");
			while(exp.contains("(")){
				int start = exp.lastIndexOf("(");
				int end = start + exp.substring(start).indexOf(")") + 1;
				String subExp = exp.substring(start+1, end-1);
				String frontExp = exp.substring(0, start);
				String lastExp = exp.substring(end);
				while(subExp.contains("&&")){
					String frontAnd = subExp.substring(0, subExp.indexOf("&&"));
					String lastAnd = subExp.substring(subExp.indexOf("&&")+2);
					if("$exp".equals(frontAnd.substring(frontAnd.length()-4, frontAnd.length()))){
						frontAnd = frontAnd.substring(0, frontAnd.length()-4);
						if(frontAnd.length()>0&&"!".equals(frontAnd.substring(frontAnd.length()-1, frontAnd.length()))){
							frontAnd = frontAnd.substring(0, frontAnd.length()-1);
						}
					}
					if("$exp".equals(lastAnd.substring(0, 4))){
						lastAnd = lastAnd.substring(4);
					}else{
						if("!$exp".equals(lastAnd.substring(0, 5))){
							lastAnd = lastAnd.substring(5);
						}
					}
					subExp = frontAnd + "$exp" + lastAnd;
				}
				while(subExp.contains("||")){
					String frontAnd = subExp.substring(0, subExp.indexOf("||"));
					String lastAnd = subExp.substring(subExp.indexOf("||")+2);
					if("$exp".equals(frontAnd.substring(frontAnd.length()-4, frontAnd.length()))){
						frontAnd = frontAnd.substring(0, frontAnd.length()-4);
						if(frontAnd.length()>0&&"!".equals(frontAnd.substring(frontAnd.length()-1, frontAnd.length()))){
							frontAnd = frontAnd.substring(0, frontAnd.length()-1);
						}
					}
					if("$exp".equals(lastAnd.substring(0, 4))){
						lastAnd = lastAnd.substring(4);
					}else{
						if("!$exp".equals(lastAnd.substring(0, 5))){
							lastAnd = lastAnd.substring(5);
						}
					}
					subExp = frontAnd + "$exp" + lastAnd;
				}
				if("$exp".equals(subExp)){
					exp = frontExp + "$exp" + lastExp;
				}else{
					if("!$exp".equals(subExp)){
						exp = frontExp + "$exp" + lastExp;
					}else{
						return false;
					}
				}
			}
			if("$exp".equals(exp)){
				return true;
			}else{
				if("!$exp".equals(exp)){
					return true;
				}else{
					return false;
				}
			}
		}catch(Exception e){
			return false;
		}
	}
	
	public static String matchPerfMapping2(String exp){
		try{
			while(exp.contains("o.get(\"")){
				int start = exp.indexOf("o.get(\"");
				int end = start + exp.substring(start).indexOf("\")") + 2;
				String var = exp.substring(start, end);
				exp = exp.replace(var, "");
			}
			return exp;
		}catch(Exception e){
			return null;
		}
	}
	
	public static void main(String[] args) {
		String str = getRealPath();
		str = str.replaceAll("file:/", "").replaceAll("WebRoot/WEB-INF/classes/", "");
		System.out.println(str);
	}
}
