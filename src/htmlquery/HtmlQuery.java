/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package htmlquery;

import htmlquery.utils.FileUtils;
import htmlquery.utils.HttpUtil;
import htmlquery.utils.ReadFromFile;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.net.URLDecoder;

/**
 *
 * @author kira
 */
public class HtmlQuery {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String contentFileName = "E:/maps/entranceContent.txt";
        String errorFileName = "E:/maps/entranceErrContent.txt";
        String encoding = "gb2312";
        String rootUrl = "http://www.royal-canin.cn/mapsales/index.php";
        truncateFileByName(contentFileName);//防止因为多次运行该程序导致数据重复
        truncateFileByName(errorFileName);//防止因为多次运行该程序导致数据重复
        HttpUtil.httpGet(rootUrl, encoding, contentFileName, errorFileName, true);//获取入口内容(这个html中包含所有的二级文件入口链接)

        String urlListFileName = "E:/maps/urlList.txt";
        truncateFileByName(urlListFileName);//防止因为多次运行该程序导致数据重复
        getRealUrlList(contentFileName, encoding, rootUrl, urlListFileName);//根据入口文件内容获取所有的二级文件链接，为下一步做准备

        List<String> list = ReadFromFile.readFileByLines(urlListFileName, encoding);
        String areaListAllInOne = "E:/maps/areaList.txt";
        String nonShopList = "E:/maps/nonShopList.txt";
        truncateFileByName(areaListAllInOne);//防止因为多次运行该程序导致数据重复
        list.stream().forEach((url) -> {
            try {
                String content = HttpUtil.httpGet(url, encoding, contentFileName, errorFileName, false);//当前地区的完整html内容
                if (content.startsWith("<script") && url.endsWith("%CA%D0")) {
                    url = url.replace("%CA%D0", "");
                    content = HttpUtil.httpGet(url, encoding, contentFileName, errorFileName, false);//当前地区的完整html内容
                }
                
                String areaName = findAreaNameByUrl(url);//当前地区名字
                String finalAreaName = java.net.URLDecoder.decode(areaName, encoding);
                String currentAreaShopListFileName = "E:/maps/" + finalAreaName + ".txt";
//                truncateFileByName(urlListFileName);//防止因为多次运行该程序导致数据重复
                if (!content.startsWith("<script")) {//没有报错 包含需要找的店铺地址
                    List<String> shopList = findShopListByContent(content);//根据html获取shopList
                    FileUtils.appendMethodA(areaListAllInOne, areaName + "================\r\n");
                    shopList.stream().forEach((shop) -> {
                        FileUtils.appendMethodA(areaListAllInOne, shop + "\r\n");
                        FileUtils.appendMethodA(currentAreaShopListFileName, shop + "\r\n");
                    });
                } else {
                    FileUtils.appendMethodA(nonShopList, url + "\r\n");
                }
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(HtmlQuery.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }

    /**
     * 清空文件
     *
     * @author Jeff Liu<jeff.liu.guo@gmail.com>
     * @param fileName
     */
    public static void truncateFileByName(String fileName) {
        RandomAccessFile rf;
        try {
            rf = new RandomAccessFile(fileName, "rw");
            FileChannel fc = rf.getChannel();
            try {
                fc.truncate(0); //将文件大小截为0
            } catch (IOException ex) {
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(HtmlQuery.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * 获取地区名称(包含在url中了)
     *
     * @author Jeff Liu<jeff.liu.guo@gmail.com>
     * @param url
     * @return
     */
    public static String findAreaNameByUrl(String url) {
        String areaPattern = "action=q&s=(.+)";
        Pattern pattern = Pattern.compile(areaPattern);
        Matcher matcher = pattern.matcher(url);
        while (matcher.find()) {
            try {
                return URLDecoder.decode(matcher.group(1), "gb2312");
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(HtmlQuery.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return "";
    }

    /**
     * 获取url中包含的shopList
     *
     * @author Jeff Liu<jeff.liu.guo@gmail.com>
     * @param content
     * @return
     */
    public static List<String> findShopListByContent(String content) {
        String areaPattern = "area\\[\\d+\\]\\s*=\\s*\"([^\"]+)\";";
        Pattern pattern = Pattern.compile(areaPattern);
        Matcher matcher = pattern.matcher(content);
        List<String> areaList = new ArrayList();
        while (matcher.find()) {
            areaList.add(matcher.group(1));
        }

        return areaList;
    }

    /**
     * 获得二级页面url
     *
     * @author Jeff Liu<jeff.liu.guo@gmail.com>
     * @param contentFileName
     * @param encoding
     * @param root
     * @param urlListFileName
     */
    public static void getRealUrlList(String contentFileName, String encoding, String root, String urlListFileName) {
        List<String> list = ReadFromFile.readFileByLines(contentFileName, encoding);
        Pattern pattern = Pattern.compile("href=\"(\\./)(\\?action=q[^\"]+)");
        Matcher matcher;
        for (String line : list) {
            matcher = pattern.matcher(line);
            StringBuilder buffer = new StringBuilder();
            while (matcher.find()) {
                String f = matcher.group(2);
                if (!f.isEmpty()) {
                    buffer.append(root).append(matcher.group(2)).append("\r\n");
                    System.out.println(buffer.toString());
                    FileUtils.appendMethodA(urlListFileName, buffer.toString());
                }
            }
        }
    }

}
