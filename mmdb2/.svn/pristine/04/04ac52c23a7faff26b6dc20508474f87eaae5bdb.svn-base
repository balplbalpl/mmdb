/**
 *
 */
package com.mmdb.demo;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

import org.apache.commons.io.IOUtils;

import com.mmdb.core.utils.UUID;

/**
 * @author aol_aog@163.com(James Gao)
 */
public class DataSimulator {

    private Properties prop1, prop2, prop3, prop4;

    /**
     *
     */
    public DataSimulator() {
        prop1 = new Properties();
        prop2 = new Properties();
        prop3 = new Properties();
        prop4 = new Properties();
    }

    /**
     * 生成2小时内的数据。
     *
     * @return
     */
    public List<Object[]> genPerfData1(int interval) {
        List<Object[]> data = new LinkedList<Object[]>();
        try {

            this.prop1.clear();
            this.prop1 = loadProperties("/config/demo/perf_data.properties");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("读取配置文件/config/demo/perf_data.properties时出错，请修改：" + e.getMessage());
            return data;
        }
        Set<Entry<Object, Object>> entries = this.prop1.entrySet();
        Random r = new Random();

        long t = System.currentTimeMillis() - 150L * 60 * 1000L;

        for (int i = 0; i <= 150; i++) {
            for (Entry<Object, Object> entry : entries) {
                String obj[] = new String[10];
                if (entry.getValue() == null || entry.getValue().toString().equals(""))
                    continue;
                // System.out.println(i+"=="+entry.getValue());
                String[] tmp = entry.getValue().toString().split("\\|");
                obj[0] = tmp[0]; // ci
                obj[1] = tmp[1]; // instance
                obj[2] = tmp[2]; // kpi
                obj[3] = new Timestamp(t + i * 60L * 1000L).toString(); // 时间

                if (tmp[3] == null || tmp[3].trim().equals("")) {
                    obj[3] = "" + r.nextInt(100);
                }
                if (tmp[3].trim().indexOf("[") != -1) {
                    // 根据区间生成值.
                    int index = tmp[3].trim().indexOf(']');
                    String srange = tmp[3].trim().substring(1, index);
                    index = srange.indexOf(',');
                    int min = Integer.parseInt(srange.substring(0, index));
                    int max = Integer.parseInt(srange.substring(index + 1));

                    obj[4] = "" + (r.nextInt(max - min + 1) + min);

                } else {
                    obj[4] = tmp[3];// 值
                }

                obj[5] = (int) (Math.random() * 20) + "";
                obj[6] = "19938";
                obj[7] = "" + r.nextInt(2);
                obj[8] = "";
                obj[9] = "";
                data.add(obj);
            }
        }
        return data;
    }

    public List<Object[]> genPerfData() {
        List<Object[]> data = new LinkedList<Object[]>();
        try {

            this.prop1.clear();
            this.prop1 = loadProperties("/config/demo/perf_data.properties");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("读取配置文件/config/demo/perf_data.properties时出错，请修改：" + e.getMessage());
            return data;
        }
        Set<Entry<Object, Object>> entries = this.prop1.entrySet();
        Random r = new Random();

        for (Entry<Object, Object> entry : entries) {
            String obj[] = new String[5];
            if (entry.getValue() == null || entry.getValue().toString().equals(""))
                continue;
            String[] tmp = entry.getValue().toString().split("\\|");
            obj[0] = tmp[0]; // ci
            obj[1] = tmp[1]; // instance
            obj[2] = tmp[2]; // kpi
            obj[3] = new Timestamp(System.currentTimeMillis()).toString(); // 时间

            if (tmp[3] == null || tmp[3].trim().equals("")) {
                obj[3] = "" + r.nextInt(100);
            }
            if (tmp[3].trim().indexOf("[") != -1) {
                // 根据区间生成值.
                int index = tmp[3].trim().indexOf(']');
                String srange = tmp[3].trim().substring(1, index);
                index = srange.indexOf(',');
                int min = Integer.parseInt(srange.substring(0, index));
                int max = Integer.parseInt(srange.substring(index + 1));

                obj[4] = "" + (r.nextInt(max - min + 1) + min);

            } else {
                obj[4] = tmp[3];// 值
            }

            data.add(obj);
        }

        return data;
    }

    public List<Object[]> genTicketData() {
        List<Object[]> data = new LinkedList<Object[]>();
        try {

            this.prop2.clear();
            this.prop2 = loadProperties("/config/demo/ticket_data.properties");
        } catch (Exception e) {
            e.printStackTrace();
            System.out
                    .println("读取配置文件/config/demo/ticket_data.properties时出错，请修改：" + e.getMessage());
            return data;
        }
        Set<Entry<Object, Object>> entries = this.prop2.entrySet();
        for (Entry<Object, Object> entry : entries) {
            String obj[] = new String[10];
            obj[0] = entry.getKey().toString(); // 编号

            String[] tmp = entry.getValue().toString().split("\\|");
            obj[1] = tmp[0]; // 应用别名
            obj[2] = tmp[1]; // 应用id
            obj[3] = tmp[2]; // ci
            obj[4] = tmp[3];// 时间
            obj[5] = "数据库连接不上，请及时解决";
            obj[5] = "23";
            obj[6] = "19938";
            obj[7] = "" + new Random().nextInt(2);
            obj[8] = "";
            obj[9] = "";
            data.add(obj);
        }
        return data;
    }

    public List<Object[]> genAlarmData() {
        List<Object[]> data = new LinkedList<Object[]>();
        try {

            this.prop4.clear();
            this.prop4 = loadProperties("/config/demo/alarm_data.properties");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("读取配置文件/config/demo/alarm_data.properties时出错，请修改：" + e.getMessage());
            return data;
        }
        Set<Entry<Object, Object>> entries = this.prop4.entrySet();

        int year, month, day, hour;
        Calendar cal = Calendar.getInstance();
        year = cal.get(Calendar.YEAR);
        month = cal.get(Calendar.MONTH) + 1;
        day = cal.get(Calendar.DAY_OF_MONTH);
        hour = cal.get(Calendar.HOUR_OF_DAY);
        for (Entry<Object, Object> entry : entries) {
            String obj[] = new String[6];
            obj[0] = UUID.getID(); // GUID

            String[] tmp = entry.getValue().toString().split("\\|");
            obj[1] = tmp[0]; // ci

            Random r = new Random();
            int i = r.nextInt(4);
            switch (i) {
                case 0:
                    obj[2] = "CRITICAL";
                    break;// SEVERITY
                case 1:
                    obj[2] = "MAJOR";
                    break;
                case 2:
                    obj[2] = "MINOR";
                    break;
                case 3:
                    obj[2] = "INFO";
                    break;
                default:
                    obj[2] = "MINOR";
                    break;
            }

            obj[3] = "OPEN"; // STATUS
            int maxh = hour;
            int minh = hour - 3;
            int h = (r.nextInt(maxh - minh + 1) + minh);
            int ms = r.nextInt(60);
            obj[4] = year + "-" + (month < 10 ? "0" + month : month) + "-"
                    + (day < 10 ? "0" + day : day) + " " + (h < 10 ? "0" + (h > 0 ? h : "") : h)
                    + ":" + (ms < 10 ? "0" + ms : ms) + ":" + (ms < 10 ? "0" + ms : ms);// MC_ARRIVAL_TIME
            obj[5] = tmp[2]; // MSG
            data.add(obj);
        }
        return data;
    }

    public List<Object[]> genKPIThreshold() {
        List<Object[]> data = new LinkedList<Object[]>();
        try {

            this.prop3.clear();
            this.prop3 = loadProperties("/config/demo/kpi_data.properties");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("读取配置文件/config/demo/kpi_data.properties时出错，请修改：" + e.getMessage());
            return data;
        }
        Set<Entry<Object, Object>> entries = this.prop3.entrySet();
        for (Entry<Object, Object> entry : entries) {
            String obj[] = new String[9];
            // System.out.println("KPI序号: "+entry.getKey()); // 序号

            String[] tmp = entry.getValue().toString().split("\\|");
            // 顺序：ci,kpi_class,instance,kpi,threshold4,threshold3,threshold2,threshold1
            obj[0] = tmp[0]; // ci
            obj[1] = tmp[2]; // kpi_class
            obj[2] = tmp[1]; // instance
            obj[3] = tmp[3]; // kpi
            if (tmp.length <= 4) {
                obj[4] = "";// threshold4
                obj[5] = "";// threshold3
                obj[6] = "";// threshold2
                obj[7] = "";// threshold1
            } else {
                if (tmp.length == 5) {
                    obj[4] = tmp[4];// threshold4
                    obj[5] = "";// threshold3
                    obj[6] = "";// threshold2
                    obj[7] = "";// threshold1
                }

                if (tmp.length == 6) {
                    obj[4] = tmp[4];// threshold4
                    obj[5] = tmp[5];// threshold3
                    obj[6] = "";// threshold2
                    obj[7] = "";// threshold1
                }
                if (tmp.length == 7) {
                    obj[4] = tmp[4];// threshold4
                    obj[5] = tmp[5];// threshold3
                    obj[6] = tmp[6];// threshold2
                    obj[7] = "";// threshold1
                }
                if (tmp.length >= 8) {
                    obj[4] = tmp[4];// threshold4
                    obj[5] = tmp[5];// threshold3
                    obj[6] = tmp[6];// threshold2
                    obj[7] = tmp[7];// threshold1
                }

            }
            obj[8] = "Y"; // enabled status
            data.add(obj);
        }
        return data;
    }

    private Properties loadProperties(String path) throws Exception {
        InputStreamReader isr = null;
        Properties prop = new Properties();
        try {
            String file = this.getClass().getResource(path).getFile();
            isr = new InputStreamReader(new FileInputStream(new File(file)), "UTF-8");
            List<String> lines = IOUtils.readLines(isr);
            for (String line : lines) {
                if (line != null && !line.trim().startsWith("#")) {
                    String[] tmp = line.split("=");
                    if (tmp != null && tmp.length == 2) {
                        prop.put(tmp[0], tmp[1]);
                    }
                }
            }
        } finally {
            IOUtils.closeQuietly(isr);
        }
        return prop;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        String[] tmp = "by18qyxljap1003|实例1|CPU|cpu使用率|(35,40]|(40,65]|(65,80]|(80,100]||||"
                .split("\\|");
        System.out.println(Arrays.asList(tmp));
    }

}
