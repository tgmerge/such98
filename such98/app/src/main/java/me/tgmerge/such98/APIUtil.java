package me.tgmerge.such98;

import android.app.Activity;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;


// 使用
//      new APIUtil.PostTopicPost(2803718, "title", "content", new APIUtil.APICallback() {
//          @Override
//          public void onSuccess(int statCode, Header[] headers, byte[] body) {
//              Toast.makeText(that, new String(body), Toast.LENGTH_LONG).show();
//          }
//
//          @Override
//          public void onFailure(int statCode, Header[] headers, byte[] body, Throwable error) {
//              Toast.makeText(that, statCode + ", " + error.toString(), Toast.LENGTH_LONG).show();
//          }
//      }).execute();

public class APIUtil {

    private static final AsyncHttpClient sClient = new AsyncHttpClient();
    private static final String URL_CC98API = "http://api.cc98.org/";
    private static final int METHOD_GET    = 0;
    private static final int METHOD_POST   = 1;
    private static final int METHOD_PUT    = 2;
    private static final int METHOD_DELETE = 3;

    protected abstract static class APIRequest {

        private APICallback mCallback = null;
        private String mURL = "";
        private RequestParams mParams = new RequestParams();
        private boolean mHasAuthHead = false;
        private Activity mAct = null;

        public APIRequest(Activity act, APICallback callback) {
            logDebug("APIRequest: a request is initializing, callback: " + callback.toString());
            mCallback = callback;
            mAct = act;
        }

        protected void setURL(String url) {
            mURL = url;
        }

        protected void setURL(String url, Integer pageFrom, Integer pageTo, Integer pageSize) {
            String pagedURL = url;
            pagedURL = addURIParamIfExists(pagedURL, "From", pageFrom);
            pagedURL = addURIParamIfExists(pagedURL, "To", pageTo);
            pagedURL = addURIParamIfExists(pagedURL, "Size", pageSize);
            this.setURL(pagedURL);
        }

        protected void addHeader(String key, String value) {
            sClient.addHeader(key, value);
        }

        protected void addParam(String key, String value) {
            mParams.put(key, value);
        }

        protected void removeHeaders() {
            sClient.removeAllHeaders();
        }

        protected void addAuthorization() {
            mHasAuthHead = true;
        }

        protected void fire(int type) {
            logDebug("APIRequest: firing a request, method=" + type + " URL=" + mURL);

            class MyAPIHandler extends AsyncHttpResponseHandler {
                @Override
                public void onStart() {
                    logDebug("MyAPIHandler: callback: onStart");
                }

                @Override
                public void onSuccess(int statCode, Header[] headers, byte[] body) {
                    logDebug("MyAPIHandler: callback: onSuccess");
                    mCallback.onSuccess(statCode, headers, body);
                    removeHeaders();
                }

                @Override
                public void onFailure(final int statCode, final Header[] headers, final byte[] body, final Throwable error) {
                    logError("MyAPIHandler: callback: onError");
                    if (statCode == 401) {
                        OAuthUtil oa = OAuthUtil.getInstance();
                        if (oa == null) {
                            logError("MyAPIHandler: callback: tried to refresh token, but OAuthUtil returned null");
                            mCallback.onFailure(statCode, headers, body, error);
                        } else {
                            logDebug("MyAPIHandler: callback: trying to refresh token");
                            oa.refreshToken(mAct, new OAuthUtil.OAuthCallback() {
                                @Override
                                public void onSuccess() {
                                    logDebug("MyAPIHandler: refreshToken: success");
                                    execute();
                                }

                                @Override
                                public void onFailure() {
                                    mAct.runOnUiThread(new Runnable() {
                                        public void run() {
                                            logError("MyAPIHandler: refreshToken: failed");
                                            logError("...code=" + statCode);
                                            if (body != null) logError("...body=" + new String(body));
                                            logError("...error=" + error.toString());
                                            mCallback.onFailure(statCode, headers, body, error);
                                        }
                                    });
                                }
                            });
                        }
                    } else {
                        mCallback.onFailure(statCode, headers, body, error);
                    }
                    removeHeaders();
                }
            }

            addHeader("Accept", "application/xml");
            if (mHasAuthHead) {
                OAuthUtil oa = OAuthUtil.getInstance();
                if (oa == null) {
                    logError("APIRequest: addAuthorization: OAuthUtil.getInstance() returns null");
                } else {
                    addHeader("Authorization", "Bearer " + oa.getAccessToken());
                }
            }

            if (type == METHOD_GET) {
                sClient.get(mURL, new MyAPIHandler());
            } else if (type == METHOD_POST) {
                sClient.post(mURL, mParams, new MyAPIHandler());
            } else if (type == METHOD_PUT) {
                sClient.put(mURL, mParams, new MyAPIHandler());
            } else if (type == METHOD_DELETE) {
                sClient.delete(mURL, new MyAPIHandler());
            }
        }

        @SuppressWarnings("unused")
        abstract protected void execute();
    }

    // API调用后的回调，使用时覆盖onSuccess和onFailure两个方法
    protected static class APICallback {
        public void onSuccess(int statCode, Header[] headers, byte[] body) {
            Log.d("onsuccess", "success");
        }

        public void onFailure(int statCode, Header[] headers, byte[] body, Throwable error) {
            Log.d("onfailure", "failure");
        }
    }

    // API: Topic
    // GET Topic/New	获取论坛的最新主题。
    @SuppressWarnings("unused")
    protected static final class GetNewTopic extends APIRequest {
        public GetNewTopic(Activity act, Integer pageFrom, Integer pageTo, Integer pageSize, APICallback callback) {
            super(act, callback);
            this.setURL(URL_CC98API + "Topic/New", pageFrom, pageTo, pageSize);
        }
        protected void execute() { this.fire(METHOD_GET); }
    }

    // GET Topic/Board/{boardId}	获取特定版面的主题。主题按照最后发言顺序排序。
    @SuppressWarnings("unused")
    protected static final class GetBoardTopic extends APIRequest {
        public GetBoardTopic(Activity act, int boardId, Integer pageFrom, Integer pageTo, Integer pageSize, APICallback callback) {
            super(act, callback);
            this.setURL(URL_CC98API + "Topic/Board/" + boardId, pageFrom, pageTo, pageSize);
            this.addAuthorization();
        }
        protected void execute() { this.fire(METHOD_GET); }
    }

    // GET Topic/Hot	获取论坛的热门主题。
    @SuppressWarnings("unused")
    protected final static class GetHotTopic extends APIRequest {
        public GetHotTopic(Activity act, Integer pageFrom, Integer pageTo, Integer pageSize, APICallback callback) {
            super(act, callback);
            this.setURL(URL_CC98API + "Topic/Hot", pageFrom, pageTo, pageSize);
        }
        protected void execute() { this.fire(METHOD_GET); }
    }

    //POST Topic/Board/{boardId}	向服务器创建一个新的主题。
    // todo do some test
    @SuppressWarnings("unused")
    protected static final class PostBoardTopic extends APIRequest {
        public PostBoardTopic(Activity act, int boardId, String title, String content, APICallback callback) {
            super(act, callback);
            this.setURL(URL_CC98API + "Topic/Board/" + boardId);
            this.addParam("Title", title);
            this.addParam("Content", content);
            this.addAuthorization();
        }
        protected void execute() { this.fire(METHOD_POST); }
    }

    //GET Topic/{id}	获取给定 ID 的主题的信息。
    @SuppressWarnings("unused")
    protected static final class GetTopic extends APIRequest {
        public GetTopic(Activity act, int topicId, APICallback callback) {
            super(act, callback);
            this.setURL(URL_CC98API + "Topic/" + topicId);
            this.addAuthorization();
        }
        protected void execute() { this.fire(METHOD_GET); }
    }

    // API: Post
    //GET Post/Topic/{topicId}	获取特定主题的用户发言。
    @SuppressWarnings("unused")
    protected static final class GetTopicPost extends APIRequest {
        public GetTopicPost(Activity act, int topicId, Integer pageFrom, Integer pageTo, Integer pageSize, APICallback callback) {
            super(act, callback);
            this.setURL(URL_CC98API + "Post/Topic/" + topicId, pageFrom, pageTo, pageSize);
            this.addAuthorization();
        }
        protected void execute() { this.fire(METHOD_GET); }
    }

    //GET Post/{id}	获取一个特定的发言。
    @SuppressWarnings("unused")
    protected static final class GetPost extends APIRequest {
        public GetPost(Activity act, int postId, APICallback callback) {
            super(act, callback);
            this.setURL(URL_CC98API + "Post/" + postId);
            this.addAuthorization();
        }
        protected void execute() { this.fire(METHOD_GET); }
    }

    //POST Post/Topic/{topicId}	为特定的主题追加新的发言。
    @SuppressWarnings("unused")
    protected static final class PostTopicPost extends APIRequest {
        public PostTopicPost(Activity act, int topicId, String title, String content, APICallback callback) {
            super(act, callback);
            this.setURL(URL_CC98API + "Post/Topic/" + topicId);
            this.addParam("Title", title);
            this.addParam("Content", content);
            this.addAuthorization();
        }
        protected void execute() { this.fire(METHOD_POST); }
    }

    // PUT Post/{id}	修改一个现有发言。
    // todo not support
    @SuppressWarnings("unused")
    protected static final class PutPost extends APIRequest {
        public PutPost(Activity act, int postId, String title, String content, APICallback callback) {
            super(act, callback);
            this.setURL(URL_CC98API + "Post/" + postId);
            this.addParam("Title", title);
            this.addParam("Content", content);
            this.addAuthorization();
        }
        protected void execute() { this.fire(METHOD_PUT); }
    }

    // API: User
    //GET User/Name/{name}	根据用户名获取用户的信息。
    // todo more test: 用户名含有特殊字符
    @SuppressWarnings("unused")
    protected static final class GetNameUser extends APIRequest {
        public GetNameUser(Activity act, String userName, APICallback callback) {
            super(act, callback);
            this.setURL(URL_CC98API + "User/Name/" + userName);
        }
        protected void execute() { this.fire(METHOD_GET); }
    }

    //GET User/{id}	获取具有指定标识的用户的信息。
    @SuppressWarnings("unused")
    protected static final class GetIdUser extends APIRequest {
        public GetIdUser(Activity act, int userId, APICallback callback) {
            super(act, callback);
            this.setURL(URL_CC98API + "User/" + userId);
        }
        protected void execute() { this.fire(METHOD_GET); }
    }

    // API: Board
    //GET Board/Root	获取所有根版面的信息。
    @SuppressWarnings("unused")
    protected static final class GetRootBoard extends APIRequest {
        public GetRootBoard(Activity act, Integer pageFrom, Integer pageTo, Integer pageSize, APICallback callback) {
            super(act, callback);
            this.setURL(URL_CC98API + "Board/Root", pageFrom, pageTo, pageSize);
            this.addAuthorization();
        }
        protected void execute() { this.fire(METHOD_GET); }
    }

    //GET Board/{boardId}/Subs	获取某个版面的直接子版面。
    // APIUtil.callCcAPI(new APIUtil.GetSubBoards(aq, 6, 0, null, 10, that, "callbackMethod"));
    @SuppressWarnings("unused")
    protected static final class GetSubBoards extends APIRequest {
        public GetSubBoards(Activity act, int boardId, Integer pageFrom, Integer pageTo, Integer pageSize, APICallback callback) {
            super(act, callback);
            this.setURL(URL_CC98API + "Board/" + boardId + "/Subs", pageFrom, pageTo, pageSize);
            this.addAuthorization();
        }
        protected void execute() { this.fire(METHOD_GET); }
    }

    //GET Board/{id}	获取给定版面的信息。
    @SuppressWarnings("unused")
    protected static final class GetBoard extends APIRequest {
        public GetBoard(Activity act, int boardId, APICallback callback) {
            super(act, callback);
            this.setURL(URL_CC98API + "Board/" + boardId);
            this.addAuthorization();
        }
        protected void execute() { this.fire(METHOD_GET); }
    }

    //GET Board?id[0]={id[0]}&id[1]={id[1]}	一次性获取多个版面的的信息。
    @SuppressWarnings("unused")
    protected static final class GetMultiBoards extends APIRequest {
        public GetMultiBoards(Activity act, int[] boardIds, Integer pageFrom, Integer pageTo, Integer pageSize, APICallback callback) {
            super(act, callback);
            String idPart = "";
            for (int i = 0; i < boardIds.length; i ++) {
                idPart += "id[" + i + "]=" + boardIds[i] + "&";
            }
            this.setURL(URL_CC98API + "Board?" + idPart, pageFrom, pageTo, pageSize);
            this.addAuthorization();
        }
        protected void execute() {
            this.fire(METHOD_GET);
        }
    }

    // API: Me
    // GET Me/Basic	获取当前登录用户的基本信息。
    @SuppressWarnings("unused")
    protected static final class GetBasicMe extends APIRequest {
        public GetBasicMe(Activity act, APICallback callback) {
            super(act, callback);
            this.setURL(URL_CC98API + "Me/Basic");
            this.addAuthorization();
        }
        protected void execute() { this.fire(METHOD_GET); }
    }

    //GET Me/CustomBoards	获取当前用户的自定义版面。
    @SuppressWarnings("unused")
    protected static final class GetCustomBoardsMe extends APIRequest {
        public GetCustomBoardsMe(Activity act, APICallback callback) {
            super(act, callback);
            this.setURL(URL_CC98API + "Me/CustomBoards");
            this.addAuthorization();
        }
        protected void execute() { this.fire(METHOD_GET); }
    }

    //GET Me	获取当前用户的信息。
    @SuppressWarnings("unused")
    protected static final class GetMe extends APIRequest {
        public GetMe(Activity act, APICallback callback) {
            super(act, callback);
            this.setURL(URL_CC98API + "Me");
            this.addAuthorization();
        }
        protected void execute() { this.fire(METHOD_GET); }
    }

    //PUT Me	设置当前用户的信息。
    // todo not support
    @SuppressWarnings("unused")
    protected static final class PutMe extends APIRequest {
        public PutMe(Activity act, String data, APICallback callback) {
            super(act, callback);
            this.setURL(URL_CC98API + "Me");
            this.addParam("todo what the key is this", data);
            this.addAuthorization();
        }
        protected void execute() { this.fire(METHOD_PUT); }
    }

    // API: SystemSetting
    //GET SystemSetting	获取 CC98 API 系统的的当前设置。
    @SuppressWarnings("unused")
    protected static final class GetSystemSetting extends APIRequest {
        public GetSystemSetting(Activity act, APICallback callback) {
            super(act, callback);
            this.setURL(URL_CC98API + "SystemSetting");
            this.addAuthorization();
        }
        protected void execute() { this.fire(METHOD_GET); }
    }

    // API: Message
    // GET Message/{id}	获取一条特定的短消息。
    @SuppressWarnings("unused")
    protected static final class GetMessage extends APIRequest {
        public GetMessage(Activity act, int messageId, APICallback callback) {
            super(act, callback);
            this.setURL(URL_CC98API + "Message/" + messageId);
            this.addAuthorization();
        }
        protected void execute() { this.fire(METHOD_GET); }
    }

    // GET Message?userName={userName}&filter={filter}	获取当前用户的短消息。
    // todo username没有经过转义可能造成问题
    @SuppressWarnings("unused")
    protected static final class GetUserMessage extends APIRequest {
        public static final int FILTER_NONE = 0, FILTER_SEND = 1, FILTER_RECEIVE = 2, FILTER_BOTH = 3;
        public GetUserMessage(Activity act, String userName, int filter, Integer pageFrom, Integer pageTo, Integer pageSize, APICallback callback) {
            super(act, callback);
            this.setURL(URL_CC98API + "Message?userName=" + userName + "&filter=" + filter, pageFrom, pageTo, pageSize);
            this.addAuthorization();
        }
        protected void execute() { this.fire(METHOD_GET); }
    }

    // DELETE Message/{id}	删除当前用户的特定短消息。
    @SuppressWarnings("unused")
    protected static final class DeleteMessage extends APIRequest {
        public DeleteMessage(Activity act, int messageId, APICallback callback) {
            super(act, callback);
            this.setURL(URL_CC98API + "Message/" + messageId);
            this.addAuthorization();
        }
        protected void execute() { this.fire(METHOD_DELETE); }
    }

    // PUT Message/{id}	修改现有短消息的内容。
    // todo not supported
    @SuppressWarnings("unused")
    protected static final class PutMessage extends APIRequest {
        public PutMessage(Activity act, int messageId, String receiverName, String title, String content, APICallback callback) {
            super(act, callback);
            this.setURL(URL_CC98API + "Message/" + messageId);
            this.addParam("ReceiverName", receiverName);
            this.addParam("Title", title);
            this.addParam("Content", content);
            this.addAuthorization();
        }
        protected void execute() { this.fire(METHOD_PUT); }
    }

    //POST Message	创建一条新的短消息。
    @SuppressWarnings("unused")
    protected static final class PostMessage extends APIRequest {
        public PostMessage(Activity act, String receiverName, String title, String content, APICallback callback) {
            super(act, callback);
            this.setURL(URL_CC98API + "Message");
            this.addParam("ReceiverName", receiverName);
            this.addParam("Title", title);
            this.addParam("Content", content);
            this.addAuthorization();
        }
        protected void execute() { this.fire(METHOD_POST); }
    }

    // - utility methods -

    // Similar to javascript encodeURIComponent
    public static final String encodeURIComponent(final String s) {
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
    // if the "value" arg is null, no modification is done to URL.
    private static final String addURIParamIfExists(String url, String key, String value) {
        if (url == null || url.equals("")) {
            return url;
        }
        if (!url.contains("?")) {
            url += "?";
        }
        if (!(url.endsWith("&") || url.endsWith("?"))) {
            url += "&";
        }
        url += key + "=" + encodeURIComponent(value);
        return url;
    }
    private static final String addURIParamIfExists(String url, String key, Integer value) {
        return (value == null) ? url : addURIParamIfExists(url, key, String.valueOf(value));
    }

    private static final void logDebug(String msg) {
        Log.d("APIUtil", msg);
    }

    private static final void logError(String msg) {
        Log.e("APIUtil", msg);
    }

}