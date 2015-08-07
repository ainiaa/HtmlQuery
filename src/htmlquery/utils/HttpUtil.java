/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package htmlquery.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author kira
 */
public class HttpUtil {

    public static String httpPost(String urlAddress, Map<String, String> paramMap) {
        if (paramMap == null) {
            paramMap = new HashMap();
        }
        String[] params = new String[paramMap.size()];
        int i = 0;
        for (String paramKey : paramMap.keySet()) {
            String param = paramKey + "=" + paramMap.get(paramKey);
            params[i] = param;
            i++;
        }
        return httpPost(urlAddress, params);
    }

    public static String httpPost(String urlAddress, List<String> paramList) {
        if (paramList == null) {
            paramList = new ArrayList();
        }
        return httpPost(urlAddress, paramList.toArray(new String[0]));
    }

    public static String httpPost(String urlAddress, String[] params) {
        URL url;
        HttpURLConnection con = null;
        BufferedReader in = null;
        StringBuilder result = new StringBuilder();
        try {
            url = new URL(urlAddress);
            con = (HttpURLConnection) url.openConnection();
            con.setUseCaches(false);
            con.setDoOutput(true);
            con.setRequestMethod("POST");
            con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:31.0) Gecko/20100101 Firefox/31.0");//放置因为没有user-agent导致访问不正常
            con.setConnectTimeout(0);//不超时 要不然 30s之后就会超时了。
            con.setReadTimeout(0);//不超时
            con.setRequestProperty("Connection", "Keep-Alive");// 设置长连接
            con.setRequestProperty("Cache-Control", "no-cache");//设置 不使用cache
            String paramsTemp = HttpPostUtils.getParamStringFromParamArray(params);
            byte[] b = paramsTemp.getBytes();
            con.getOutputStream().write(b, 0, b.length);
            con.getOutputStream().flush();
            con.getOutputStream().close();
            in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            while (true) {
                String line = in.readLine();
                if (line == null) {
                    break;
                } else {
                    result.append(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            String fileName = "E:/recoverUserFailure.txt";
            String content = HttpPostUtils.getParamStringFromParamArray(params);
            //按方法A追加文件
            FileUtils.appendMethodA(fileName, content + "\r\n");

        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (con != null) {
                    con.disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result.toString();
    }

    public static String getParamStringFromParamArray(String[] params) {
        String paramsTemp = "";
        for (String param : params) {
            if (param != null && !"".equals(param.trim())) {
                paramsTemp += "&" + param;
            }
        }
        return paramsTemp;
    }

    public static String httpGet(String urlAddress, String encoding, String successFileName, String errorFileName, boolean write) {
        URL url = null;
        HttpURLConnection con = null;
        BufferedReader in = null;
        StringBuilder result = new StringBuilder();
        try {
            url = new URL(urlAddress);
            con = (HttpURLConnection) url.openConnection();
            con.setUseCaches(false);
            con.setDoOutput(true);
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:31.0) Gecko/20100101 Firefox/31.0");//放置因为没有user-agent导致访问不正常
            con.setConnectTimeout(0);//不超时 要不然 30s之后就会超时了。
            con.setReadTimeout(0);//不超时
            con.setRequestProperty("Connection", "Keep-Alive");// 设置长连接
            con.setRequestProperty("Cache-Control", "no-cache");//设置 不使用cache
            con.setRequestProperty("Accept-Charset", encoding);
            con.getOutputStream().flush();
            con.getOutputStream().close();
            in = new BufferedReader(new InputStreamReader(con.getInputStream(), encoding));
            while (true) {
                String line = in.readLine();
                if (line == null) {
                    break;
                } else {
                    result.append(line).append("\r\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            //按方法A追加文件
            FileUtils.appendMethodA(errorFileName, urlAddress + ":" + e.getMessage() + "\r\n");

        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (con != null) {
                    con.disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();
                FileUtils.appendMethodA(errorFileName, urlAddress + ":" + e.getMessage() + "\r\n");
            }
        }
        if (write) {
            FileUtils.appendMethodA(successFileName, result.toString() + "\r\n");
        }

        return result.toString();
    }
}
