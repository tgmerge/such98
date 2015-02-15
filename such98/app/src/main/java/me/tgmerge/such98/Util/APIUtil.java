package me.tgmerge.such98.Util;

import android.app.Activity;

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

public final class APIUtil {

    private static final AsyncHttpClient sClient = new AsyncHttpClient();
    private static final String URL_CC98API = "http://api.cc98.org/";
    private static final int METHOD_GET = 0;
    private static final int METHOD_POST = 1;
    private static final int METHOD_PUT = 2;
    private static final int METHOD_DELETE = 3;

    public abstract static class APIRequest {

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
            addHeader("Accept", "application/xml"); // todo monkey patch - ShowBoardsActivity中获取定制版面时会返回json
        }

        protected void addAuthorization() {
            mHasAuthHead = true;
        }

        protected void fire(int type) {
            logDebug("APIRequest: firing, method=" + type + " URL=" + mURL);

            class MyAPIHandler extends AsyncHttpResponseHandler {
                @Override
                public void onStart() {
                    logDebug("MyAPIHandler: callback: onStart");
                }

                @Override
                public void onSuccess(int statCode, Header[] headers, byte[] body) {
                    logDebug("MyAPIHandler: onSuccess, code=" + statCode);
                    mCallback.onSuccess(statCode, headers, body);
                    removeHeaders();
                }

                @Override
                public void onFailure(final int statCode, final Header[] headers, final byte[] body, final Throwable error) {
                    logError("MyAPIHandler: onFailure, code=" + statCode);
                    if (statCode == 401) { // todo Stronger test, maybe body == Unauthorized
                        OAuthUtil.OAuthManager oam = OAuthUtil.newOAuthManager(mAct);
                        if (oam == null) {
                            logError("MyAPIHandler: onFailure: tried to refresh token, but failed to get OAuthManager, calling callback.onFailure");
                            mCallback.onFailure(statCode, headers, body, error);
                        } else {
                            logDebug("MyAPIHandler: onFailure: trying to refresh token");
                            oam.refreshToken(new OAuthUtil.OAuthManager.OAuthCallback() {
                                @Override
                                public void onSuccess() {
                                    logDebug("MyAPIHandler: onFailure: oam.refreshToken: onSuccess, re-executing API request");
                                    execute(); // todo Could cause infinite loop call, if refreshToken succeed but API still returns 401
                                }

                                @Override
                                public void onFailure() {
                                    logError("MyAPIHandler: onFailure: oam.refreshToken: onFailure");
                                    logError("    code=" + statCode + ", error=" + error.toString() + ", body=" + (body != null ? new String(body) : null));
                                    logError("    so calling onFailure of API");
                                    mCallback.onFailure(statCode, headers, body, error);
                                }
                            });
                        }
                    } else {
                        logError("    calling onFailure of API");
                        mCallback.onFailure(statCode, headers, body, error);
                    }
                    removeHeaders();
                }
            }

            addHeader("Accept", "application/xml");
            if (mHasAuthHead) {
                String accessToken = OAuthUtil.getAccessToken(mAct);
                if (accessToken == null || accessToken.equals("")) {
                    logError("APIRequest: OAuthUtil.accessToken() returns empty string");
                } else {
                    addHeader("Authorization", "Bearer " + accessToken);
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
        public abstract void execute();
    }


    // API调用后的回调，使用时覆盖onSuccess和onFailure两个方法
    public static interface APICallback {
        public void onSuccess(int statCode, Header[] headers, byte[] body);

        public void onFailure(int statCode, Header[] headers, byte[] body, Throwable error);
    }


    // - - - API - - -


    // API: Topic
    // GET Topic/New	获取论坛的最新主题。
    @SuppressWarnings("unused")
    public static final class GetNewTopic extends APIRequest {
        public GetNewTopic(Activity act, Integer pageFrom, Integer pageTo, Integer pageSize, APICallback callback) {
            super(act, callback);
            this.setURL(URL_CC98API + "Topic/New", pageFrom, pageTo, pageSize);
        }

        public final void execute() {
            this.fire(METHOD_GET);
        }
    }

    // GET Topic/Board/{boardId}	获取特定版面的主题。主题按照最后发言顺序排序。
    @SuppressWarnings("unused")
    public static final class GetBoardTopic extends APIRequest {
        public GetBoardTopic(Activity act, int boardId, Integer pageFrom, Integer pageTo, Integer pageSize, APICallback callback) {
            super(act, callback);
            this.setURL(URL_CC98API + "Topic/Board/" + boardId, pageFrom, pageTo, pageSize);
            this.addAuthorization();
        }

        public final void execute() {
            this.fire(METHOD_GET);
        }
    }

    // GET Topic/Hot	获取论坛的热门主题。
    @SuppressWarnings("unused")
    public final static class GetHotTopic extends APIRequest {
        public GetHotTopic(Activity act, Integer pageFrom, Integer pageTo, Integer pageSize, APICallback callback) {
            super(act, callback);
            this.setURL(URL_CC98API + "Topic/Hot", pageFrom, pageTo, pageSize);
        }

        public final void execute() {
            this.fire(METHOD_GET);
        }
    }

    //POST Topic/Board/{boardId}	向服务器创建一个新的主题。
    // todo do some test
    @SuppressWarnings("unused")
    public static final class PostBoardTopic extends APIRequest {
        public PostBoardTopic(Activity act, int boardId, String title, String content, APICallback callback) {
            super(act, callback);
            this.setURL(URL_CC98API + "Topic/Board/" + boardId);
            this.addParam("Title", title);
            this.addParam("Content", content);
            this.addAuthorization();
        }

        public final void execute() {
            this.fire(METHOD_POST);
        }
    }

    //GET Topic/{id}	获取给定 ID 的主题的信息。
    @SuppressWarnings("unused")
    public static final class GetTopic extends APIRequest {
        public GetTopic(Activity act, int topicId, APICallback callback) {
            super(act, callback);
            this.setURL(URL_CC98API + "Topic/" + topicId);
            this.addAuthorization();
        }

        public final void execute() {
            this.fire(METHOD_GET);
        }
    }

    // API: Post
    //GET Post/Topic/{topicId}	获取特定主题的用户发言。
    @SuppressWarnings("unused")
    public static final class GetTopicPost extends APIRequest {
        public GetTopicPost(Activity act, int topicId, Integer pageFrom, Integer pageTo, Integer pageSize, APICallback callback) {
            super(act, callback);
            this.setURL(URL_CC98API + "Post/Topic/" + topicId, pageFrom, pageTo, pageSize);
            this.addAuthorization();
        }

        public final void execute() {
            this.fire(METHOD_GET);
        }
    }

    //GET Post/{id}	获取一个特定的发言。
    @SuppressWarnings("unused")
    public static final class GetPost extends APIRequest {
        public GetPost(Activity act, int postId, APICallback callback) {
            super(act, callback);
            this.setURL(URL_CC98API + "Post/" + postId);
            this.addAuthorization();
        }

        public final void execute() {
            this.fire(METHOD_GET);
        }
    }

    //POST Post/Topic/{topicId}	为特定的主题追加新的发言。
    @SuppressWarnings("unused")
    public static final class PostTopicPost extends APIRequest {
        public PostTopicPost(Activity act, int topicId, String title, String content, APICallback callback) {
            super(act, callback);
            this.setURL(URL_CC98API + "Post/Topic/" + topicId);
            this.addParam("Title", title);
            this.addParam("Content", content);
            this.addAuthorization();
        }

        public final void execute() {
            this.fire(METHOD_POST);
        }
    }

    // PUT Post/{id}	修改一个现有发言。
    // todo not support
    @SuppressWarnings("unused")
    public static final class PutPost extends APIRequest {
        public PutPost(Activity act, int postId, String title, String content, APICallback callback) {
            super(act, callback);
            this.setURL(URL_CC98API + "Post/" + postId);
            this.addParam("Title", title);
            this.addParam("Content", content);
            this.addAuthorization();
        }

        public final void execute() {
            this.fire(METHOD_PUT);
        }
    }

    // API: User
    //GET User/Name/{name}	根据用户名获取用户的信息。
    // todo more test: 用户名含有特殊字符
    @SuppressWarnings("unused")
    public static final class GetNameUser extends APIRequest {
        public GetNameUser(Activity act, String userName, APICallback callback) {
            super(act, callback);
            this.setURL(URL_CC98API + "User/Name/" + userName);
        }

        public final void execute() {
            this.fire(METHOD_GET);
        }
    }

    //GET User/{id}	获取具有指定标识的用户的信息。
    @SuppressWarnings("unused")
    public static final class GetIdUser extends APIRequest {
        public GetIdUser(Activity act, int userId, APICallback callback) {
            super(act, callback);
            this.setURL(URL_CC98API + "User/" + userId);
        }

        public final void execute() {
            this.fire(METHOD_GET);
        }
    }

    // API: Board
    //GET Board/Root	获取所有根版面的信息。
    @SuppressWarnings("unused")
    public static final class GetRootBoard extends APIRequest {
        public GetRootBoard(Activity act, Integer pageFrom, Integer pageTo, Integer pageSize, APICallback callback) {
            super(act, callback);
            this.setURL(URL_CC98API + "Board/Root", pageFrom, pageTo, pageSize);
            this.addAuthorization();
        }

        public final void execute() {
            this.fire(METHOD_GET);
        }
    }

    //GET Board/{boardId}/Subs	获取某个版面的直接子版面。
    // APIUtil.callCcAPI(new APIUtil.GetSubBoards(aq, 6, 0, null, 10, that, "callbackMethod"));
    @SuppressWarnings("unused")
    public static final class GetSubBoards extends APIRequest {
        public GetSubBoards(Activity act, int boardId, Integer pageFrom, Integer pageTo, Integer pageSize, APICallback callback) {
            super(act, callback);
            this.setURL(URL_CC98API + "Board/" + boardId + "/Subs", pageFrom, pageTo, pageSize);
            this.addAuthorization();
        }

        public final void execute() {
            this.fire(METHOD_GET);
        }
    }

    //GET Board/{id}	获取给定版面的信息。
    @SuppressWarnings("unused")
    public static final class GetBoard extends APIRequest {
        public GetBoard(Activity act, int boardId, APICallback callback) {
            super(act, callback);
            this.setURL(URL_CC98API + "Board/" + boardId);
            this.addAuthorization();
        }

        public final void execute() {
            this.fire(METHOD_GET);
        }
    }

    //GET Board?id[0]={id[0]}&id[1]={id[1]}	一次性获取多个版面的的信息。
    @SuppressWarnings("unused")
    public static final class GetMultiBoards extends APIRequest {
        public GetMultiBoards(Activity act, int[] boardIds, Integer pageFrom, Integer pageTo, Integer pageSize, APICallback callback) {
            super(act, callback);
            String idPart = "";
            for (int i = 0; i < boardIds.length; i++) {
                idPart += "id[" + i + "]=" + boardIds[i] + "&";
            }
            this.setURL(URL_CC98API + "Board?" + idPart, pageFrom, pageTo, pageSize);
            this.addAuthorization();
        }

        public final void execute() {
            this.fire(METHOD_GET);
        }
    }

    // API: Me
    // GET Me/Basic	获取当前登录用户的基本信息。
    @SuppressWarnings("unused")
    public static final class GetBasicMe extends APIRequest {
        public GetBasicMe(Activity act, APICallback callback) {
            super(act, callback);
            this.setURL(URL_CC98API + "Me/Basic");
            this.addAuthorization();
        }

        public final void execute() {
            this.fire(METHOD_GET);
        }
    }

    //GET Me/CustomBoards	获取当前用户的自定义版面。
    @SuppressWarnings("unused")
    public static final class GetCustomBoardsMe extends APIRequest {
        public GetCustomBoardsMe(Activity act, APICallback callback) {
            super(act, callback);
            this.setURL(URL_CC98API + "Me/CustomBoards");
            this.addAuthorization();
        }

        public final void execute() {
            this.fire(METHOD_GET);
        }
    }

    //GET Me	获取当前用户的信息。
    @SuppressWarnings("unused")
    public static final class GetMe extends APIRequest {
        public GetMe(Activity act, APICallback callback) {
            super(act, callback);
            this.setURL(URL_CC98API + "Me");
            this.addAuthorization();
        }

        public final void execute() {
            this.fire(METHOD_GET);
        }
    }

    //PUT Me	设置当前用户的信息。
    // todo not support
    @SuppressWarnings("unused")
    public static final class PutMe extends APIRequest {
        public PutMe(Activity act, String data, APICallback callback) {
            super(act, callback);
            this.setURL(URL_CC98API + "Me");
            this.addParam("todo what the key is this", data);
            this.addAuthorization();
        }

        public final void execute() {
            this.fire(METHOD_PUT);
        }
    }

    // API: SystemSetting
    //GET SystemSetting	获取 CC98 API 系统的的当前设置。
    @SuppressWarnings("unused")
    public static final class GetSystemSetting extends APIRequest {
        public GetSystemSetting(Activity act, APICallback callback) {
            super(act, callback);
            this.setURL(URL_CC98API + "SystemSetting");
            this.addAuthorization();
        }

        public final void execute() {
            this.fire(METHOD_GET);
        }
    }

    // API: Message
    // GET Message/{id}	获取一条特定的短消息。
    @SuppressWarnings("unused")
    public static final class GetMessage extends APIRequest {
        public GetMessage(Activity act, int messageId, APICallback callback) {
            super(act, callback);
            this.setURL(URL_CC98API + "Message/" + messageId);
            this.addAuthorization();
        }

        public final void execute() {
            this.fire(METHOD_GET);
        }
    }

    // GET Message?userName={userName}&filter={filter}	获取当前用户的短消息。
    // todo username没有经过转义可能造成问题
    @SuppressWarnings("unused")
    public static final class GetUserMessage extends APIRequest {
        public static final int FILTER_NONE = 0, FILTER_SEND = 1, FILTER_RECEIVE = 2, FILTER_BOTH = 3;

        public GetUserMessage(Activity act, String userName, int filter, Integer pageFrom, Integer pageTo, Integer pageSize, APICallback callback) {
            super(act, callback);
            this.setURL(URL_CC98API + "Message?userName=" + userName + "&filter=" + filter, pageFrom, pageTo, pageSize);
            this.addAuthorization();
        }

        public final void execute() {
            this.fire(METHOD_GET);
        }
    }

    // DELETE Message/{id}	删除当前用户的特定短消息。
    @SuppressWarnings("unused")
    public static final class DeleteMessage extends APIRequest {
        public DeleteMessage(Activity act, int messageId, APICallback callback) {
            super(act, callback);
            this.setURL(URL_CC98API + "Message/" + messageId);
            this.addAuthorization();
        }

        public final void execute() {
            this.fire(METHOD_DELETE);
        }
    }

    // PUT Message/{id}	修改现有短消息的内容。
    // todo not supported
    @SuppressWarnings("unused")
    public static final class PutMessage extends APIRequest {
        public PutMessage(Activity act, int messageId, String receiverName, String title, String content, APICallback callback) {
            super(act, callback);
            this.setURL(URL_CC98API + "Message/" + messageId);
            this.addParam("ReceiverName", receiverName);
            this.addParam("Title", title);
            this.addParam("Content", content);
            this.addAuthorization();
        }

        public final void execute() {
            this.fire(METHOD_PUT);
        }
    }

    //POST Message	创建一条新的短消息。
    @SuppressWarnings("unused")
    public static final class PostMessage extends APIRequest {
        public PostMessage(Activity act, String receiverName, String title, String content, APICallback callback) {
            super(act, callback);
            this.setURL(URL_CC98API + "Message");
            this.addParam("ReceiverName", receiverName);
            this.addParam("Title", title);
            this.addParam("Content", content);
            this.addAuthorization();
        }

        public final void execute() {
            this.fire(METHOD_POST);
        }
    }

    // - utility methods -


    // Similar to javascript encodeURIComponent
    private static final String encodeURIComponent(final String s) {
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
        HelperUtil.generalDebug("APIUtil", msg);
    }


    private static final void logError(String msg) {
        HelperUtil.generalError("APIUtil", msg);
    }
}