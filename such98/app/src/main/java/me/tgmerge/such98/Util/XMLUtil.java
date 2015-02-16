package me.tgmerge.such98.Util;


import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import me.tgmerge.such98.Util.HelperUtil;

/**
 * Created by tgmerge on 2/8.
 * 扩展一个新的xml的方法：参考BoardLastPostInfo和BoardInfo
 */
public class XMLUtil {


    static XmlPullParserFactory sFactory;


    static {
        try {
            sFactory = XmlPullParserFactory.newInstance();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
    }


    abstract public static class XMLObj {

        abstract public Set<String> getDefaultFields();

        abstract public void processSpecialTags(XmlPullParser xpp) throws Exception;

        public void parse(XmlPullParser xpp) throws Exception {
            generalParse(xpp, this);
        }

        public void parse(Reader reader) throws Exception {
            XmlPullParser xpp = sFactory.newPullParser();
            xpp.setInput(reader);
            parse(xpp);
        }

        public void parse(String str) throws Exception {
            Reader reader = new StringReader(str);
            parse(reader);
        }

    }


    // - - - XMLObj - - -


    @SuppressWarnings("unused")
    public static final class BoardLastPostInfo extends XMLObj {
        public int BoardId;
        public int TopicId;
        public int PostId;
        public String DateTime;
        public String UserName;
        public int UserId;
        public String TopicTitle;

        private static final HashSet<String> mDefaults =
                newHashSet("BoardId", "TopicId", "PostId", "DateTime", "UserName", "UserId", "TopicTitle");

        @Override
        public Set<String> getDefaultFields() {
            return mDefaults;
        }

        @Override
        public void processSpecialTags(XmlPullParser xpp) throws Exception {
        }
    }


    @SuppressWarnings("unused")
    public static final class BoardInfo extends XMLObj {
        public int Id;
        public String Name;
        public String Description;
        public int ChildBoardCount;
        public int ParentId;
        public int RootId;
        public int TotalPostCount;
        public int TotalTopicCount;
        public int TodayPostCount;
        public boolean IsHidden;
        public boolean IsCategory;
        public boolean IsEncrypted;
        public boolean IsAnomynous;  // todo typo in xml definition
        public boolean IsLocked;
        public Vector<String> Masters = new Vector<>();
        public BoardLastPostInfo LastPostInfo;
        public String PostTimeLimit;

        private static final HashSet<String> mDefaults =
                newHashSet("Id", "Name", "Description", "ChildBoardCount", "ParentId", "RootId", "TotalPostCount",
                        "TotalTopicCount", "TodayPostCount", "IsHidden", "IsCategory", "IsEncrypted",
                        "IsAnomynous", "IsLocked", "PostTimeLimit");

        @Override
        public Set<String> getDefaultFields() {
            return mDefaults;
        }

        @Override
        public void processSpecialTags(XmlPullParser xpp) throws Exception {
            String tag = xpp.getName();
            if (tag.equals("d3p1:string")) {
                Masters.add(xpp.nextText());
            } else if (tag.equals("LastPostInfo")) {
                LastPostInfo = new BoardLastPostInfo();
                LastPostInfo.parse(xpp);
            }
        }
    }


    @SuppressWarnings("unused")
    public static final class TopicInfo extends XMLObj {
        public String Title;
        public int HitCount;
        public int Id;
        public int BoardId;
        public String BestState;
        public static final String BESTSTATE_NORMAL = "Normal";
        public static final String BESTSTATE_RESERVED = "Reserved";
        public static final String BESTSTATE_BEST = "Best";
        public String TopState;
        public static final String TOPSTATE_NONE = "None";
        public static final String TOPSTATE_TEMPORARYTOP = "TemporaryTop";
        public static final String TOPSTATE_BOARDTOP = "BoardTop";
        public static final String TOPSTATE_AREATOP = "AreaTop";
        public static final String TOPSTATE_SITETOP = "SiteTop";
        public int ReplyCount;
        public boolean IsVote;
        public boolean IsAnonymous;
        public String AuthorName;
        public int AuthorId;
        public boolean IsLocked;
        public String CreateTime;
        public TopicLastPostInfo LastPostInfo;

        private static final HashSet<String> mDefaults =
                newHashSet("Title", "HitCount", "Id", "BoardId", "BestState", "TopState", "ReplyCount",
                        "IsVote", "IsAnonymous", "AuthorName", "AuthorId", "IsLocked", "CreateTime");


        @Override
        public Set<String> getDefaultFields() {
            return mDefaults;
        }

        @Override
        public void processSpecialTags(XmlPullParser xpp) throws Exception {
            String tag = xpp.getName();
            if (tag.equals("LastPostInfo")) {
                LastPostInfo = new TopicLastPostInfo();
                LastPostInfo.parse(xpp);
            }
        }
    }


    @SuppressWarnings("unused")
    public static final class TopicLastPostInfo extends XMLObj {
        public String UserName;
        public String ContentSummary;
        public String Time;

        private static final HashSet<String> mDefaults =
                newHashSet("UserName", "ContentSummary", "Time");

        @Override
        public Set<String> getDefaultFields() {
            return mDefaults;
        }

        @Override
        public void processSpecialTags(XmlPullParser xpp) throws Exception {
        }
    }


    @SuppressWarnings("unused")
    public static final class HotTopicInfo extends XMLObj {
        public String Title;
        public int HitCount;
        public int Id;
        public int BoardId;
        public String BoardName;
        public int ReplyCount;
        public int ParticipantCount;
        public String AuthorName;
        public String CreateTime;

        private static final HashSet<String> mDefaults =
                newHashSet("Title", "HitCount", "Id", "BoardId", "BoardName", "ReplyCount", "ParticipantCount",
                        "AuthorName", "CreateTime");

        @Override
        public Set<String> getDefaultFields() {
            return mDefaults;
        }

        @Override
        public void processSpecialTags(XmlPullParser xpp) throws Exception {
        }
    }


    @SuppressWarnings("unused")
    public static final class PostInfo extends XMLObj {
        public int Id;
        public String Title;
        public String Content;
        public String Time;
        public boolean IsDeleted;
        public int Floor;
        public String UserName;
        public int UserId;
        public boolean IsAnomynous; // todo typo

        private static final HashSet<String> mDefaults =
                newHashSet("Id", "Title", "Content", "Time", "IsDeleted", "Floor", "UserName", "UserId",
                           "IsAnomynous");

        @Override
        public Set<String> getDefaultFields() {
            return mDefaults;
        }

        @Override
        public void processSpecialTags(XmlPullParser xpp) throws Exception {
        }
    }


    @SuppressWarnings("unused")
    public static final class UserInfo extends XMLObj {
        public int Id;
        public String Name;
        public String Title;
        public int PostCount;
        public int Prestige;
        public String Fraction;
        public String GroupName;
        public String RegisterTime;
        public String LastLogOnTime;
        public Boolean IsOnline;
        public String PortraitUrl;
        public int PortraitHeight;
        public int PortraitWidth;
        public String PhotoUrl;
        public String SignatureCode;
        public String Gender;
        public String Level;
        public String EmailAddress;
        public String HomePageUrl;
        public String Birthday;
        public String QQ;
        public String Msn;

        private static final HashSet<String> mDefaults =
                newHashSet("Id", "Name", "Title", "PostCount", "Prestige", "Faction", "GroupName", "RegisterTime",
                           "LastLogOnTime", "IsOnline", "PortraitUrl", "PortraitHeight", "PortraitWidth",
                           "PhotoUrl", "SignatureCode", "Gender", "Level", "EmailAddress", "HomePageUrl",
                           "Birthday", "QQ", "Msn");

        @Override
        public Set<String> getDefaultFields() {
            return mDefaults;
        }

        @Override
        public void processSpecialTags(XmlPullParser xpp) throws Exception {
        }
    }


    @SuppressWarnings("unused")
    public static final class UserBasicInfo extends XMLObj {
        public int V1Id;
        public int V2Id;
        public String Roles;
        public String Name;

        private static final HashSet<String> mDefaults = newHashSet("V1Id", "V2Id", "Roles", "Name");

        @Override
        public Set<String> getDefaultFields() {
            return mDefaults;
        }

        @Override
        public void processSpecialTags(XmlPullParser xpp) throws Exception {
        }
    }


    // 有两种初始化ArrayOf<T>的方法：
    // 1. 解析<ArrayOfT><T></T><T></T></ArrayOfT>的对象
    // 2. 传入类型为T的数组或Vector，使用(T[] objs)或(Vector<T> objs)构造方法
    // 使用： XMLUtil.ArrayOf<XMLUtil.BoardInfo> o = new XMLUtil.ArrayOf<>(XMLUtil.BoardInfo.class);
    //       o.parse(string);
    @SuppressWarnings("unused")
    public static final class ArrayOf<T extends XMLObj> extends XMLObj {
        private Vector<T> mObjs = new Vector<>();
        private Class<T> mObjClass;

        public ArrayOf(Class<T> c) {
            mObjs = new Vector<>();
            mObjClass = c;
        }

        public ArrayOf(T[] objs) {
            mObjs = new Vector<>(Arrays.asList(objs));
        }

        public ArrayOf(Vector<T> objs) {
            mObjs = objs;
        }

        @Override
        public Set<String> getDefaultFields() {
            return null;
        }

        @Override
        public void processSpecialTags(XmlPullParser xpp) throws Exception {
            T obj = mObjClass.newInstance();
            obj.parse(xpp);
            mObjs.add(obj);
        }

        public Class<? extends XMLObj> getItemClass() {
            return mObjClass;
        }

        public T get(int i) {
            return mObjs.get(i);
        }

        public int size() {
            return mObjs.size();
        }

        public void append(ArrayOf<T> obj) {
            mObjs.addAll(obj.mObjs);
        }

        public void append(Collection<T> obj) {
            mObjs.addAll(obj);
        }
    }


    @SuppressWarnings("unused")
    public static final class ArrayOfint extends XMLObj {
        public Vector<Integer> values = new Vector<>();

        @Override
        public Set<String> getDefaultFields() {
            return null;
        }

        @Override
        public void processSpecialTags(XmlPullParser xpp) throws Exception {
            String tag = xpp.getName();
            if (tag.equals("int")) {
                values.add(Integer.parseInt(xpp.nextText()));
            }
        }
    }


    // - - - util method - - -


    // 通用的XML解析过程
    // 继承XMLObj的对象应该覆盖processSpecialTags(xpp)和getDefaultFields方法来定制
    private static final void generalParse(XmlPullParser xpp, XMLObj obj) throws Exception {

        logDebug(obj.getClass().getName() + ": start parsing " + xpp.getPositionDescription());

        int e = xpp.getEventType();
        while (e != XmlPullParser.START_TAG) {
            e = xpp.next();
        }

        // 现在xpp在根元素的open tag后面
        int thisDepth = xpp.getDepth();
        logDebug(obj.getClass().getName() + ": thisDepth=" + thisDepth);

        // 进入内部元素的层级(thisDepth+1)
        e = xpp.next();

        // 如果跑到根元素这一级，XML对象结束
        while (xpp.getDepth() > thisDepth) {
            if (e == XmlPullParser.START_TAG) {
                if (!setDefaultField(xpp, obj, obj.getDefaultFields())) {
                    logDebug(obj.getClass().getName() + ": special tag " + xpp.getPositionDescription());
                    obj.processSpecialTags(xpp);
                }
            }
            e = xpp.next();
        }

        logDebug(obj.getClass().getName() + ": finish " + xpp.getPositionDescription());
    }


    private static final HashSet<String> newHashSet(String... strings) {
        HashSet<String> set = new HashSet<String>();
        Collections.addAll(set, strings);
        return set;
    }


    // 如果当前的tag name在defaults中，设置o的对应值
    // 如果成功设置，返回True
    // 否则返回False
    private static final boolean setDefaultField(XmlPullParser xpp, XMLObj o, Set<String> defaults) throws Exception {
        if (defaults == null || o == null || xpp == null) {
            return false;
        }

        String tagName = xpp.getName();
        if (defaults.contains(tagName)) {
            // tag name在defaults中
            String value = xpp.nextText();
            Field field = o.getClass().getField(tagName);
            Class<?> type = field.getType();
            if (type == int.class) {
                field.set(o, Integer.parseInt(value));
            } else if (type == boolean.class) {
                field.set(o, Boolean.parseBoolean(value));
            } else {
                field.set(o, value);
            }
            return true;
        } else {
            return false;
        }
    }


    private static final void logDebug(String msg) {
        HelperUtil.generalDebug("XMLUtil", msg);
    }


    private static final void logError(String msg) {
        HelperUtil.generalError("XMLUtil", msg);
    }
}
