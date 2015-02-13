package me.tgmerge.such98;

import android.os.Looper;
import android.test.suitebuilder.annotation.Suppress;
import android.util.Log;
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
        public String DateTime;  // todo type is date time
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
        public boolean IsAnomynous;  // todo typo in xml defination
        public boolean IsLocked;
        protected Vector<String> Masters = new Vector<>();
        protected BoardLastPostInfo LastPostInfo;
        public String PostTimeLimit; // todo type is timespan

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
            T obj = mObjClass.newInstance(); // todo Default constructor only!
            obj.parse(xpp);
            mObjs.add(obj);
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

        Vector<Integer> values = new Vector<>();

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
