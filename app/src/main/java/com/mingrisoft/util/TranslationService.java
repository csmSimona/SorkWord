package com.mingrisoft.util;



import android.util.Log;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class TranslationService {
    private static Logger logger = LoggerFactory.getLogger(TranslationService.class);

    private static final String YOUDAO_URL = "http://openapi.youdao.com/api";

    private static final String APP_KEY = "489888f611fe71fd";

    private static final String APP_SECRET = "hEOuS3S0d2hv1lYpFFQGAbNcYY4nTWhw";

    //errorCode
    public static final int   SUCCEE_RESULT = 0;                      //翻译成功
    public static final int   ERROR_UNBINDING_INSTANCE = 110;         //应用没有绑定服务实例
    public static final int  ERROR_INVALID_APPKEY = 108;             //appkey无效
    public static final int   ERROR_PARAMAT_DISCARD = 101;            //参数不齐全，或书写不正确
    public static final int  ERROR_PROBLEM_CODE= 202;                //如果确认 appKey 和 appSecret 的正确性，仍返回202，一般是编码问题。请确保 q 为UTF-8编码.


    public static String start(String word) throws IOException {
        String result;                      //查询结果
        Map<String,String> params = new HashMap<String,String>();
        String  q = word;
        String salt = String.valueOf(System.currentTimeMillis());
        params.put("from", "EN");
        params.put("to", "zh-CHS");
        params.put("signType", "v3");
        String curtime = String.valueOf(System.currentTimeMillis() / 1000);
        params.put("curtime", curtime);
        String signStr = APP_KEY + truncate(q) + salt + curtime + APP_SECRET;
        String sign = getDigest(signStr);
        params.put("appKey", APP_KEY);
        params.put("q", q);
        params.put("salt", salt);
        params.put("sign", sign);
        /** 处理结果 */
        result=requestForHttp(YOUDAO_URL,params);
        return result;
    }

    public static String requestForHttp(String url,Map<String,String> params) throws IOException {
        String json;

        /** 创建HttpClient */
        CloseableHttpClient httpClient = HttpClients.createDefault();

        /** httpPost */
        HttpPost httpPost = new HttpPost(url);
        List<NameValuePair> paramsList = new ArrayList<NameValuePair>();
        Iterator<Map.Entry<String,String>> it = params.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry<String,String> en = it.next();
            String key = en.getKey();
            String value = en.getValue();
            paramsList.add(new BasicNameValuePair(key,value));
        }
        httpPost.setEntity(new UrlEncodedFormEntity(paramsList,"UTF-8"));
        CloseableHttpResponse httpResponse = httpClient.execute(httpPost);
        try{
            Header[] contentType = httpResponse.getHeaders("Content-Type");
            logger.info("Content-Type:" + contentType[0].getValue());
            HttpEntity httpEntity = httpResponse.getEntity();
            json = EntityUtils.toString(httpEntity,"UTF-8");
            consume(httpEntity);
            logger.info(json);
            System.out.println("翻译结果为："+json);
        }finally {
            try{
                if(httpResponse!=null){
                    httpResponse.close();
                }
            }catch(IOException e){
                logger.info("## release resouce error ##" + e);
            }
        }
        return "["+json+"]";
    }

    /**
     * 生成加密字段
     */
    public static String getDigest(String string) {
        if (string == null) {
            return null;
        }
        char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        byte[] btInput = string.getBytes();
        try {
            MessageDigest mdInst = MessageDigest.getInstance("SHA-256");
            mdInst.update(btInput);
            byte[] md = mdInst.digest();
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (byte byte0 : md) {
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    /**
     *
     * @param result 音频字节流
     * @param file 存储路径
     */
    private static void byte2File(byte[] result, String file) {
        File audioFile = new File(file);
        FileOutputStream fos = null;
        try{
            fos = new FileOutputStream(audioFile);
            fos.write(result);

        }catch (Exception e){
            logger.info(e.toString());
        }finally {
            if(fos != null){
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public static String truncate(String q) {
        if (q == null) {
            return null;
        }
        int len = q.length();
        String result;
        return len <= 20 ? q : (q.substring(0, 10) + len + q.substring(len - 10, len));
    }

    public static void consume(HttpEntity entity) throws IOException {
        if (entity != null) {
            if (entity.isStreaming()) {
                InputStream inStream = entity.getContent();
                if (inStream != null) {
                    inStream.close();
                }
            }

        }
    }

}
