package me.tgmerge.such98;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.util.XmlDom;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by tgmerge on 2/4.
 */
public class NetUtil {

    // NetUtil is a singleton class

    // context of the whole application,
    // will be set on NetUtil instance creates
    private static Context mCtx = null;

    // the only instance of the class,
    // will be set on first NetUtil instance creates
    private static NetUtil mInstance = null;


    // constructor
    public NetUtil(final Context context){
        mCtx = context;
    }

    // returns a NetUtil instance,
    // which is the only instance of the whole application
    public static final synchronized NetUtil getInstance(final Context context) {
        if (mInstance == null) {
            mInstance = new NetUtil(context.getApplicationContext());
        }
        return mInstance;
    }

    // Some utility methods

    // Similar to javascript encodeURIComponent
    public static String encodeURIComponent(final String s) {
        String result;

        try {
            result = URLEncoder.encode(s, "UTF-8")
                    .replaceAll("\\+", "%20")
                    .replaceAll("\\%21", "!")
                    .replaceAll("\\%27", "'")
                    .replaceAll("\\%28", "(")
                    .replaceAll("\\%29", ")")
                    .replaceAll("\\%7E", "~");
        } catch (UnsupportedEncodingException e) {
            result = s;
        }

        return result;
    }

    // Add a parameter to passed-in URL.
    // if the "value" arg is null, no modification is done to uri.
    private static String addURIParamIfExists(String uri, String key, String value) {
        if (uri == null || uri.equals("")) {
            return uri;
        }
        if (!uri.contains("?")) {
            uri += "?";
        }
        if (!(uri.endsWith("&") || uri.endsWith("?"))) {
            uri += "&";
        }
        uri += key + "=" + encodeURIComponent(value);
        return uri;
    }
    private static String addURIParamIfExists(String uri, String key, Integer value) {
        return (value == null) ? uri : addURIParamIfExists(uri, key, String.valueOf(value));
    }

    // returns accessToken from shared preferences file.
    // if not found, returns null string
    public final String getAccessToken() {
        SharedPreferences sharedPref = mCtx.getSharedPreferences(
                mCtx.getString(R.string.appConfig_preferenceFileKey_settings),
                Context.MODE_PRIVATE);
        return sharedPref.getString(
                mCtx.getString(R.string.appConfig_valueKey_settings_accessToken),
                "");
        // TODO: start Login intent if "" is returned
    }


    // clear accessToken from shared preferences file.
    public final void clearAccessToken() {
        SharedPreferences sharedPref = mCtx.getSharedPreferences(
                mCtx.getString(R.string.appConfig_preferenceFileKey_settings),
                mCtx.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(
                mCtx.getString(R.string.appConfig_valueKey_settings_accessToken),
                "");
        editor.commit();
    }

    //

    public static final void callCcAPI(final CcAPI api) { api.execute(); }

    abstract protected static class CcAPI {

        private AjaxCallback<XmlDom> mCb;
        private AQuery mAq;

        public static final String URL_CC98API = "http://api.cc98.org/";

        public CcAPI(AQuery aq) {
            mAq = aq;
        }

        public CcAPI(AQuery aq, Object handler, String callback) {
            mAq = aq;
            mCb = new AjaxCallback<>();
            mCb.type(XmlDom.class).weakHandler(handler, callback);
        }

        protected final CcAPI setURL(String url) {
            mCb.url(url);
            return this;
        }

        protected final CcAPI setURL(String url, Integer pageFrom, Integer pageTo, Integer pageSize) {
            String pagedURL = url;
            pagedURL = addURIParamIfExists(pagedURL, "From", pageFrom);
            pagedURL = addURIParamIfExists(pagedURL, "To", pageTo);
            pagedURL = addURIParamIfExists(pagedURL, "Size", pageSize);
            mCb.url(pagedURL);
            return this;
        }

        protected final CcAPI addAuthorization() {
            mCb.header("Authorization", "Bearer " + NetUtil.getInstance(mCtx).getAccessToken());
            return this;
        }

        protected final CcAPI addPayload(String key, String value) {
            mCb.param(key, value);
            return this;
        }

        protected final CcAPI addHeader(String key, String value) {
            mCb.header(key, value);
            return this;
        }

        protected final CcAPI setCallback(Object handler, String callback) {
            mCb.weakHandler(handler, callback);
            return this;
        }

        // TODO not sure is it GET or POST
        protected final CcAPI getRequest() {
            mCb.header("Accept", "application/xml");
            mCb.params(null);
            mAq.ajax(mCb);
            return this;
        }

        // TODO not sure is it GET or POST
        protected final CcAPI postRequest() {
            mCb.header("Accept", "application/xml")
               .header("Content-Type", "application/x-www-form-urlencoded");
            mAq.ajax(mCb);
            return this;
        }

        // TODO do something
        protected final CcAPI putRequest() {
            Log.e("CcAPI", "PUT is not supported yet");
            return this;
        }

        abstract protected CcAPI execute();
    }

    // API: Topic
    // GET Topic/New	获取论坛的最新主题。
    protected static final class GetNewTopic extends CcAPI {
        public GetNewTopic(AQuery aq,
                           Integer pageFrom, Integer pageTo, Integer pageSize,
                           Object handler, String callback) {
            super(aq, handler, callback);
            this.setURL(URL_CC98API + "Topic/New", pageFrom, pageTo, pageSize);
        }
        protected CcAPI execute() { return this.getRequest(); }
    }

    // GET Topic/Board/{boardId}	获取特定版面的主题。主题按照最后发言顺序排序。
    protected static final class GetBoardTopic extends CcAPI {
        public GetBoardTopic(AQuery aq, int boardId,
                             Integer pageFrom, Integer pageTo, Integer pageSize,
                             Object handler, String callback) {
            super(aq, handler, callback);
            this.setURL(URL_CC98API + "Topic/Board/" + boardId, pageFrom, pageTo, pageSize)
                .addAuthorization();
        }
        protected CcAPI execute() { return this.getRequest(); }
    }

    //GET Topic/Hot	获取论坛的热门主题。
    protected static final class GetHotTopic extends CcAPI {
        public GetHotTopic(AQuery aq,
                           Integer pageFrom, Integer pageTo, Integer pageSize,
                           Object handler, String callback) {
            super(aq, handler, callback);
            this.setURL(URL_CC98API + "Topic/Hot", pageFrom, pageTo, pageSize);
        }
        protected CcAPI execute() { return this.getRequest(); }
    }

    //POST Topic/Board/{boardId}	向服务器创建一个新的主题。
    protected static final class PostBoardTopic extends CcAPI {
        public PostBoardTopic(AQuery aq, int boardId,
                              String title, String content,
                              Object handler, String callback) {
            super(aq, handler, callback);
            this.setURL(URL_CC98API + "Topic/Board/" + boardId)
                .addPayload("Title", title)
                .addPayload("Content", content)
                .addAuthorization();
        }
        protected CcAPI execute() { return this.postRequest(); }
    }

    //GET Topic/{id}	获取给定 ID 的主题的信息。
    protected static final class GetTopic extends CcAPI {
        public GetTopic(AQuery aq, int topicId,
                        Object handler, String callback) {
            super(aq, handler, callback);
            this.setURL(URL_CC98API + "Topic/" + topicId)
                .addAuthorization();
        }
        protected CcAPI execute() { return this.getRequest(); }
    }

    // API: Post

    //POST Post/Topic/{topicId}	为特定的主题追加新的发言。
    protected static final class PostTopicPost extends CcAPI {
        public PostTopicPost(AQuery aq, int topicId,
                             String title, String content,
                             Object handler, String callback) {
            super(aq, handler, callback);
            this.setURL(URL_CC98API + "Post/Topic/" + topicId)
                .addPayload("Title", title)
                .addPayload("Content", content)
                .addAuthorization();
        }
        protected CcAPI execute() { return this.postRequest(); }
    }

    //GET Post/Topic/{topicId}	获取特定主题的用户发言。
    protected static final class GetTopicPost extends CcAPI {
        public GetTopicPost(AQuery aq, int topicId,
                            Integer pageFrom, Integer pageTo, Integer pageSize,
                            Object handler, String callback) {
            super(aq, handler, callback);
            this.setURL(URL_CC98API + "Post/Topic/" + topicId, pageFrom, pageTo, pageSize)
                .addAuthorization();
        }
        protected CcAPI execute() { return this.getRequest(); }
    }

    //GET Post/{id}	获取一个特定的发言。
    protected static final class GetPost extends CcAPI {
        public GetPost(AQuery aq, int postId,
                       Object handler, String callback) {
            super(aq, handler, callback);
            this.setURL(URL_CC98API + "Post/" + postId)
                .addAuthorization();
        }
        protected CcAPI execute() { return this.getRequest(); }
    }

    // PUT Post/{id}	修改一个现有发言。
    // todo not support
    protected static final class PutPost extends CcAPI {
        public PutPost(AQuery aq, int postId,
                       String title, String content,
                       Object handler, String callback) {
            super(aq, handler, callback);
            this.setURL(URL_CC98API + "Post/" + postId)
                .addPayload("Title", title)
                .addPayload("Content", content)
                .addAuthorization();
        }
        protected CcAPI execute() { return this.putRequest(); }
    }

    // API: User

    //GET User/Name/{name}	根据用户名获取用户的信息。
    protected static final class GetNameUser extends CcAPI {
        public GetNameUser(AQuery aq, String userName,
                           Object handler, String callback) {
            super(aq, handler, callback);
            this.setURL(URL_CC98API + "User/Name/" + userName);
        }
        protected CcAPI execute() { return this.getRequest(); }
    }

    //GET User/{id}	获取具有指定标识的用户的信息。
    protected static final class GetIdUser extends CcAPI {
        public GetIdUser(AQuery aq, String userId,
                         Object handler, String callback) {
            super(aq, handler, callback);
            this.setURL(URL_CC98API + "User/" + userId);
        }
        protected CcAPI execute() { return this.getRequest(); }
    }

    // API: Board

    //GET Board/Root	获取所有根版面的信息。
    protected static final class GetRootBoard extends CcAPI {
        public GetRootBoard(AQuery aq,
                            Integer pageFrom, Integer pageTo, Integer pageSize,
                            Object handler, String callback) {
            super(aq, handler, callback);
            this.setURL(URL_CC98API + "Board/Root", pageFrom, pageTo, pageSize)
                .addAuthorization();
        }
        protected CcAPI execute() { return this.getRequest(); }
    }

    //GET Board/{boardId}/Subs	获取某个版面的直接子版面。
    protected static final class GetSubBoards extends CcAPI {
        public GetSubBoards(AQuery aq, int boardId,
                            Integer pageFrom, Integer pageTo, Integer pageSize,
                            Object handler, String callback) {
            super(aq, handler, callback);
            this.setURL(URL_CC98API + "Board/" + boardId + "/Subs", pageFrom, pageTo, pageSize)
                .addAuthorization();
        }
        protected CcAPI execute() { return this.getRequest(); }
    }

    //GET Board/{id}	获取给定版面的信息。
    protected static final class GetBoard extends CcAPI {
        public GetBoard(AQuery aq, int boardId,
                        Object handler, String callback) {
            super(aq, handler, callback);
            this.setURL(URL_CC98API + "Board/" + boardId)
                .addAuthorization();
        }
        protected CcAPI execute() { return this.getRequest(); }
    }

    //GET Board?id[0]={id[0]}&id[1]={id[1]}	一次性获取多个版面的的信息。
    // todo test and support
    protected static final class GetMultiBoards extends CcAPI {
        public GetMultiBoards(AQuery aq, int[] boardIds,
                              Integer pageFrom, Integer pageTo, Integer pageSize,
                              Object handler, String callback) {
            super(aq, handler, callback);
            String idPart = "";
            for (int i = 0; i < boardIds.length; i ++) {
                idPart += "id[" + i + "]={" + boardIds[i] + "}&";
            }
            this.setURL(URL_CC98API + "Board?" + idPart, pageFrom, pageTo, pageSize)
                .addAuthorization();
            Log.e("CCAPI GETMULTIBOARDS", "unsupported method");
        }
        protected CcAPI execute() {
            Log.e("CCAPI GETMULTIBOARDS", "unsupported method");
            return this.getRequest();
        }
    }

    // API: Me

    // GET Me/Basic	获取当前登录用户的基本信息。
    protected static final class GetBasicMe extends CcAPI {
        public GetBasicMe(AQuery aq,
                          Object handler, String callback) {
            super(aq, handler, callback);
            this.setURL(URL_CC98API + "Me/Basic")
                .addAuthorization();
        }
        protected CcAPI execute() { return this.getRequest(); }
    }

    //GET Me/CustomBoards	获取当前用户的自定义版面。
    protected static final class GetCustomBoardsMe extends CcAPI {
        public GetCustomBoardsMe(AQuery aq,
                                 Object handler, String callback) {
            super(aq, handler, callback);
            this.setURL(URL_CC98API + "Me/CustomBoards")
                .addAuthorization();
        }
        protected CcAPI execute() { return this.getRequest(); }
    }

    //GET Me	获取当前用户的信息。
    protected static final class GetMe extends CcAPI {
        public GetMe(AQuery aq,
                     Object handler, String callback) {
            super(aq, handler, callback);
            this.setURL(URL_CC98API + "Me")
                .addAuthorization();
        }
        protected CcAPI execute() { return this.getRequest(); }
    }

    //PUT Me	设置当前用户的信息。
    // todo do something to implement
    protected static final class PutMe extends CcAPI {
        public PutMe(AQuery aq, String data,
                     Object handler, String callback) {
            super(aq, handler, callback);
            this.setURL(URL_CC98API + "Me")
                .addPayload("todo what the key is this", data)
                .addAuthorization();
            Log.e("CCAPI GETMULTIBOARDS", "unsupported method");
        }
        protected CcAPI execute() { return this.putRequest(); }
    }

    // API: test

    //GET TellMePassword	快告诉我密码！
    // todo will not implement

    // API: SystemSetting

    //GET SystemSetting	获取 CC98 API 系统的的当前设置。
    protected static final class GetSystemSetting extends CcAPI {
        public GetSystemSetting(AQuery aq,
                                Object handler, String callback) {
            super(aq, handler, callback);
            this.setURL(URL_CC98API + "SystemSetting")
                .addAuthorization();
        }
        protected CcAPI execute() { return this.getRequest(); }
    }

    // API: Message

    // GET Message/{id}	获取一条特定的短消息。
    protected static final class GetMessage extends CcAPI {
        public GetMessage(AQuery aq, int messageId,
                          Object handler, String callback) {
            super(aq, handler, callback);
            this.setURL(URL_CC98API + "Message/" + messageId)
                .addAuthorization();
        }
        protected CcAPI execute() { return this.getRequest(); }
    }

    // GET Message?userName={userName}&filter={filter}	获取当前用户的短消息。
    // todo username没有经过转义可能造成问题
    protected static final class GetUserMessage extends CcAPI {
        public GetUserMessage(AQuery aq, String userName,
                                    Integer pageFrom, Integer pageTo, Integer pageSize,
                                    Object handler, String callback) {
            super(aq, handler, callback);
            this.setURL(URL_CC98API + "Message?userName=" + userName + "&filter=Both", pageFrom, pageTo, pageSize)
                .addAuthorization();
        }
        protected CcAPI execute() { return this.getRequest(); }
    }

    // DELETE Message/{id}	删除当前用户的特定短消息。
    // todo do something to implement

    // PUT Message/{id}	修改现有短消息的内容。
    // todo not implemented
    protected static final class PutMessage extends CcAPI {
        public PutMessage(AQuery aq, int messageId,
                          String receiverName, String title, String content,
                          Object handler, String callback) {
            super(aq, handler, callback);
            this.setURL(URL_CC98API + "Message/" + messageId)
                .addPayload("ReceiverName", receiverName)
                .addPayload("Title", title)
                .addPayload("Content", content)
                .addAuthorization();
        }
        protected CcAPI execute() { return this.putRequest(); }
    }

    //POST Message	创建一条新的短消息。
    protected static final class PostMessage extends CcAPI {
        public PostMessage(AQuery aq,
                           String receiverName, String title, String content,
                           Object handler, String callback) {
            super(aq, handler, callback);
            this.setURL(URL_CC98API + "Message")
                .addPayload("ReceiverName", receiverName)
                .addPayload("Title", title)
                .addPayload("Content", content)
                .addAuthorization();
        }
        protected CcAPI execute() { return this.postRequest(); }
    }
}
