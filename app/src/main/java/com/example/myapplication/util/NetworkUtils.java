package com.example.myapplication.util;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.myapplication.entity.Comment;
import com.example.myapplication.entity.CommentIsLiked;
import com.example.myapplication.entity.Item;
import com.example.myapplication.entity.Exhibition;
import com.example.myapplication.entity.Museum;
import com.example.myapplication.entity.MuseumCollectedPost;
import com.example.myapplication.entity.MuseumNew;
import com.example.myapplication.entity.Rating;

import java.io.IOException;
import java.util.Formatter;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.content.ContentValues.TAG;
import static com.example.myapplication.MainActivity.token;

//import org.json.JSONObject;

public class NetworkUtils {

    public static final MediaType TYPE_IMAGE_PNG = MediaType.parse("image/png");
    public static final MediaType TYPE_IMAGE_JPG = MediaType.parse("image/jpg");
    public static final MediaType TYPE_JSON = MediaType.parse("application/json");
    public static final MediaType TYPE_FILE = MediaType.parse("multipart/form-data");

    public enum ResultType {
        MUSEUM_ID, //博物馆ID查询结果
        MUSEUM,     //单个博物馆查询
        COMMENT,    //评论查询
        COMMENT_POST, //提交评论
        COMMENT_LIKE, //评论点赞数
        COMMENT_LIKE_POST, //评论点赞提交
        COMMENT_LIKE_CANCEL_POST, //取消评论
        COLLECT_POST, //收藏提交
        GRADE_POST, //博物馆评分
        MUSEUM_COLLECTION_POST, //博物馆收藏发送
        MUSEUM_COLLECTION_GET, //博物馆收藏获取
        USER_COMMENT,//用户评论查询
        ITEMS,      //藏品查询
        SHOWS,      //展览查询
        GRADES,      //展览查询
        MUSEUM_EXPLAIN,//博物馆讲解
        OBJECT_EXPLAIN,//藏品的讲解
        TEST,       //测试
        NEW,        //新闻
        ;
    }

    private static final HashMap<ResultType, String> m = new HashMap<ResultType, String>() {{
        put(ResultType.MUSEUM, "http://8.140.136.108/prod-api/system/museum/select/all/%s");
        put(ResultType.MUSEUM_ID, "http://8.140.136.108/prod-api/system/museum/%s");
        put(ResultType.COMMENT, "http://8.140.136.108/prod-api/system/comments/select/all/%s");
        put(ResultType.COMMENT_POST,"http://8.140.136.108/prod-api/system/comments");
        put(ResultType.COMMENT_LIKE,"http://8.140.136.108/prod-api/system/commentlike/select/all/%s");
        put(ResultType.COMMENT_LIKE_POST,"http://8.140.136.108/prod-api/system/commentlike");
        put(ResultType.COMMENT_LIKE_CANCEL_POST,"http://8.140.136.108/prod-api/system/%s");
        put(ResultType.COLLECT_POST,"http://8.140.136.108/prod-api/system/museumcollection");
        put(ResultType.MUSEUM_COLLECTION_POST,"http://8.140.136.108/dev-api/system/museumcollection/%s");
        put(ResultType.MUSEUM_COLLECTION_GET,"http://8.140.136.108/dev-api/system/museumcollection/select/all/%s");
        put(ResultType.GRADE_POST,"http://8.140.136.108/prod-api/system/museumrating");
        put(ResultType.GRADES,"http://8.140.136.108/prod-api/system/museumrating/sortid");
        put(ResultType.NEW,"http://8.140.136.108/prod-api/system/news/select/all/%s");
        put(ResultType.ITEMS,"http://8.140.136.108/prod-api/system/exhibitcollection/list?museumid=%s&exhibitname=%s");
        put(ResultType.TEST, "http://8.140.136.108:8081/sitemap.json");
        put(ResultType.SHOWS,"http://8.140.136.108/prod-api/system/exhibitcollection/select/all/%s");
    }};
    private static final OkHttpClient client = new OkHttpClient.Builder().build();

    public static void HttpRequestPost(Handler handler, Rating rating){
        String data=JSON.toJSONString(rating);
        HttpRequestPost(ResultType.GRADE_POST,handler,data.getBytes(),TYPE_JSON,null);
    }
    public static void HttpRequestPost(Handler handler, CommentIsLiked commentIsLiked){
        String data=JSON.toJSONString(commentIsLiked);
        HttpRequestPost(ResultType.COMMENT_LIKE_POST,handler,data.getBytes(),TYPE_JSON,null);
    }
    public static void HttpRequestPost(Handler handler,Comment comment) {
        String data = JSON.toJSONString(comment);
        HttpRequestPost(ResultType.COMMENT_POST,handler,data.getBytes(),TYPE_JSON,null);
    }
    public static void HttpRequestPost(Handler handler, MuseumCollectedPost museumCollectedPost) {
        String data = JSON.toJSONString(museumCollectedPost);
        HttpRequestPost(ResultType.COLLECT_POST,handler,data.getBytes(),TYPE_JSON,null);
    }

    /**
     * 提交文件 ,使用方式(jpg图片)：HttpRequestPost(resultType,handler,"name",null,data,NetworkUtil.TYPE_IMAGE_JPG)
     * @param resultType 链接种类，在ResultType枚举对象中选择合适的值，该值对应相应的提交接口链接
     * @param handler 处理函数
     * @param arg 表单名，比如"image"
     * @param name 文件名，默认为"uploaded_file"
     * @param data 数据，二进制数组
     * @param type 数据类型，可以是 TYPE_IMAGE_PNG、TYPE_IMAGE_JPG、TYPE_FILE，如果是null默认用TYPE_FILE.
     */
    public static void HttpRequestPost(ResultType resultType,Handler handler,String arg,String name, byte[] data,MediaType type) {
        if (type == null)
            type = TYPE_FILE;
        if (name == null||name.isEmpty())
            name = "uploaded_file";

        RequestBody fileBody = RequestBody.create( data,type);
        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(arg, name, fileBody)
                .build();

        HttpRequestPost(resultType,handler,null,null,body);
    }

    public static void HttpRequestPost(ResultType resultType,Handler handler,byte[] data,MediaType type,RequestBody requestBody) {
        String url = m.get(resultType);
        if (requestBody == null) {
            requestBody = RequestBody.create(data,type);
        }
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .header("Authorization", token)
                .build();

        final Call call = client.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "onFailure: ", e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                int code = response.code();
                Message message = new Message();
                message.what = 1;
                switch (code){
                    default:{
                        message.what = 0;
                        break;
                    }
                    case 200:{
                        String s = response.body().string();
                        JSONObject obj = JSONObject.parseObject(s);
                        int c = obj.getInteger("code");
                        if (c == 200) {
                            message.what = 1;
                        } else {
                            message.what = 0;
                        }
                        break;
                    }
                }
                handler.handleMessage(message);
            }
        });
    }

    public static void HttpRequestGet(ResultType resultType, Handler handler, Object... args) {
        String url = m.get(resultType);
        if (args != null) {
            Formatter formatter = new Formatter();
            formatter.format(url,args);
            url = formatter.toString();
            System.out.println(url);
        }

        assert url != null;
        url = url.replaceAll("%s", "");
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i("EEE", e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                int code = response.code();
                if (code == 200) {
                    String result = response.body().string();
                    JSONObject outcome;
                    outcome = JSON.parseObject(result);
                    Object send = null;
                    switch (resultType) {
                        case MUSEUM: {
                            JSONArray data = outcome.getJSONArray("rows");
                            send = JSON.parseArray(data.toJSONString(), Museum.class);
                            break;
                        }
                        case COMMENT: {
                            JSONArray data = outcome.getJSONArray("rows");
                            send = JSON.parseArray(data.toJSONString(), Comment.class);
                            break;
                        }
                        case COMMENT_LIKE:{
                            send=outcome.getInteger("total");
//                            JSONArray data = outcome.getJSONArray("rows");
//                            send=data.size();
                            break;
                        }
                        case NEW:{
                            JSONArray data = outcome.getJSONArray("rows");
                            send = JSON.parseArray(data.toJSONString(), MuseumNew.class);
                            break;
                        }
                        case ITEMS:{
                            JSONArray data = outcome.getJSONArray("rows");
                            send = JSON.parseArray(data.toJSONString(), Item.class);
                            break;
                        }
                        case GRADES:{
                            JSONArray data = outcome.getJSONArray("rows");
                            send = JSON.parseArray(data.toJSONString(), Rating.class);
                            break;
                        }
                        case MUSEUM_ID:{
                            JSONObject data = outcome.getJSONObject("data");
                            send = JSON.parseObject(data.toJSONString(), Museum.class);
                            break;
                        }

                        case SHOWS:{
                            JSONArray data = outcome.getJSONArray("rows");
                            send = JSON.parseArray(data.toJSONString(), Exhibition.class);
                            break;
                        }

                        case TEST: {

                            break;
                        }

                    }
                    Message message = new Message();
                    message.what = 1;
                    message.obj = send;
                    handler.sendMessage(message);
                } else {
                    Message message = new Message();
                    message.what = 0;
                    message.obj = null;
                    handler.sendMessage(message);
                }
            }
        });
    }

    public static void HttpRequestDelete(ResultType resultType,Handler handler,Object... args){
        String url = m.get(resultType);
        if (args != null) {
            Formatter formatter = new Formatter();
            formatter.format(url,args);
            url = formatter.toString();
            System.out.println(url);
        } else {
            url.replaceAll("%s", "");
        }
        assert url != null;
        Request request = new Request.Builder()
                .url(url)
                .delete()
                .build();
        final Call call = client.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "onFailure: ", e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                int code = response.code();
                Message message = new Message();
                message.what = 1;
                switch (code){
                    default:{
                        message.what = 0;
                        break;
                    }
                    case 200:{
                        String s = response.body().string();
                        JSONObject obj = JSONObject.parseObject(s);
                        int c = obj.getInteger("code");
                        if (c == 200) {
                            message.what = 1;
                        } else {
                            message.what = 0;
                        }
                        break;
                    }
                }
                handler.handleMessage(message);
            }
        });
    }

    public static void HttpRequestPut(ResultType resultType,Handler handler,byte[] data,MediaType type,RequestBody requestBody) {
        String url = m.get(resultType);
        if (requestBody == null) {
            requestBody = RequestBody.create(data,type);
        }
        Request request = new Request.Builder()
                .url(url)
                .put(requestBody)
                .header("Authorization", token)
                .build();

        final Call call = client.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "onFailure: ", e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                int code = response.code();
                Message message = new Message();
                message.what = 1;
                switch (code){
                    default:{
                        message.what = 0;
                        break;
                    }
                    case 200:{
                        String s = response.body().string();
                        JSONObject obj = JSONObject.parseObject(s);
                        int c = obj.getInteger("code");
                        if (c == 200) {
                            message.what = 1;
                        } else {
                            message.what = 0;
                        }
                        break;
                    }
                }
                handler.handleMessage(message);
            }
        });
    }
}
