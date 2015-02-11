package me.tgmerge.such98;

import android.os.Looper;
import android.util.Log;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

/**
 * Created by tgmerge on 2/8.
 * 扩展一个新的xml的方法：
 *
 * <ObjectA>
 *     <TagA1>some</TagA1>
 *     <ObjectB>
 *                              <- 解析子对象的时候，reader的起始位置会在这里
 *         <TagB1>some</TagB1>
 *         <TagB2>some</TagB2>
 *     </ObjectB>               <- 于是会在这里抛异常No open tag
 * </ObjectA>
 *
 *
 * e.g.

protected static final class BoardInfo {

    // 可以用setDefaultField()直接设置值的，设置为public
    public int Id;
    public String Name;

    // 需要特殊处理的，设置为protected
    protected Vector<String> Masters;
    protected BoardLastPostInfo LastPostInfo;

    // 可以直接设置值的变量名字
    private static HashSet<String> mDefaults = newHashSet("Id", "Name");

    public BoardInfo(Reader in, String openTag) throws Exception {

        Masters = new Vector<>();
        // 必须保存Reader的原始位置，以供parseSubXmlObj使用
        // 因为xpp的缓冲区会让reader一下读到后面的位置，而xpp的position只存储其自身缓冲区内的位置
        // parseSubXmlObj必须将xpp的position和reader在“输入xpp之前的原始位置”相加，
        // 以获取reader内XML子元素的位置
        int oldReaderPos = getReaderPos(in);
        XmlPullParser xpp = sFactory.newPullParser();
        xpp.setInput(in);

        try {
            int e = xpp.getEventType();
            while (e != XmlPullParser.END_DOCUMENT) {

                // 仅处理START_TAG事件
                if (e != XmlPullParser.START_TAG) {
                    e = xpp.next();
                    continue;
                }

                // 尝试用setDefaultField直接设置，如果tag名字不在mDefaults中，会返回false
                if (!setDefaultField(xpp, BoardInfo.class, this, mDefaults)) {

                    // 需要特殊处理
                    String tagName = xpp.getName();
                    if (tagName.equals("d3p1:string")) {
                        // 处理 <Masters>
                        Masters.add(xpp.getText());
                    } else if (tagName.equals("LastPostInfo")) {
                        // 处理 <LastPostInfo>: 是一个子对象
                        LastPostInfo = (BoardLastPostInfo) parseSubXmlObj(xpp, in, "LastPostInfo", BoardLastPostInfo.class);
                    }
                }

                // 下一个事件
                e = xpp.next();
            }
        } catch (XmlPullParserException e) {

            // 如果捕获了XmlPullParserException且当前tag和文档开始相同，说明this是在解析一个子对象，直接正常返回
            //     (xmlpullparser的解析无法回溯，遇到子对象解析时一定会少一个open tag)
            //     (也是因此，解析的时候需要传递Open tag的名字给构造方法)
            if (!xpp.getName().equals(openTag)) {
                throw e;
            }
        }
    }
}

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


    //- - -

    protected static abstract class XMLObj {

    }


    protected static final class ArrayOfint extends XMLObj {
        public Vector<Integer> Ints;

        public ArrayOfint(Reader in, String openTag) throws Exception {
            logDebug("ArrayOfint: begin");
            Ints = new Vector<>();
            XmlPullParser xpp = sFactory.newPullParser();
            xpp.setInput(in);

            try {
                int e = xpp.getEventType();
                while (e != XmlPullParser.END_DOCUMENT) {

                    if (e != XmlPullParser.START_TAG) {
                        e = xpp.next();
                        continue;
                    }

                    if (xpp.getName().equals("int")) {
                        Ints.add(Integer.parseInt(xpp.nextText()));
                    }

                    e = xpp.next();
                }
            } catch (XmlPullParserException e) {
                if (xpp.getName() == null || !xpp.getName().equals(openTag)) {
                    throw e;
                }
            }
            logDebug("ArrayOfint: finish");
        }


        public Integer get(int i) {
            return Ints.get(i);
        }


        public int size() {
            return Ints.size();
        }

    }


    protected static final class BoardLastPostInfo extends XMLObj {
        public int BoardId;
        public int TopicId;
        public int PostId;
        public String DateTime;  // todo type is date time
        public String UserName;
        public int UserId;
        public String TopicTitle;

        private static HashSet<String> mDefaults =
                newHashSet("BoardId", "TopicId", "PostId", "DateTime", "UserName", "UserId", "TopicTitle");


        public BoardLastPostInfo(Reader in, String openTag) throws Exception {
            logDebug("BoardLastPostInfo: begin");
            XmlPullParser xpp = sFactory.newPullParser();
            xpp.setInput(in);

            try {
                int e = xpp.getEventType();
                while (e != XmlPullParser.END_DOCUMENT) {

                    if (e != XmlPullParser.START_TAG) {
                        e = xpp.next();
                        continue;
                    }

                    if (!setDefaultField(xpp, getClass(), this, mDefaults)) {
                        logError("BoardLastPostInfo: unknown tag=" + xpp.getName());
                    }

                    e = xpp.next();
                }
            } catch (XmlPullParserException e) {
                if (xpp.getName() == null || !xpp.getName().equals(openTag)) {
                    throw e;
                }
            }
            logDebug("BoardLastPostInfo: finish");
        }
    }


    protected static final class BoardInfo extends XMLObj {
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
        protected Vector<String> Masters;
        protected BoardLastPostInfo LastPostInfo;
        public String PostTimeLimit; // todo type is timespan

        private static HashSet<String> mDefaults =
                newHashSet("Id", "Name", "Description", "ChildBoardCount", "ParentId", "RootId", "TotalPostCount",
                           "TotalTopicCount", "TodayPostCount", "IsHidden", "IsCategory", "IsEncrypted",
                           "IsAnomynous", "IsLocked", "PostTimeLimit");


        public BoardInfo(Reader in, String openTag) throws Exception {
            logDebug("BoardInfo: begin");
            Masters = new Vector<>();

            int oldReaderPos = getReaderPos(in);
            XmlPullParser xpp = sFactory.newPullParser();
            xpp.setInput(in);

            try {
                int e = xpp.getEventType();
                while (e != XmlPullParser.END_DOCUMENT) {

                    if (e != XmlPullParser.START_TAG) {
                        e = xpp.next();
                        continue;
                    }

                    if (!setDefaultField(xpp, BoardInfo.class, this, mDefaults)) {
                        // 需要特殊处理的tag
                        String tagName = xpp.getName();
                        if (tagName.equals("d3p1:string")) {
                            // "Masters": <Masters><d3p1:string>...</d3p1:string></Masters>
                            Masters.add(xpp.getText());
                        } else if (tagName.equals("LastPostInfo")) {
                            // "LastPostInfo"
                            LastPostInfo = (BoardLastPostInfo) parseSubXmlObj(xpp, in, oldReaderPos, "LastPostInfo", BoardLastPostInfo.class);
                        }
                    }

                    e = xpp.next();
                }
            } catch (XmlPullParserException e) {
                if (xpp.getName() == null || !xpp.getName().equals(openTag)) {
                    throw e;
                }
            }
            logDebug("BoardInfo: finish");
        }
    }


    // 有两种初始化ArrayOf<T>的方法：
    // 1. 解析<ArrayOfT><T></T><T></T></ArrayOfT>的对象
    //    使用(Reader in, String openTag, Class c, String itemOpenTag)构造方法
    // 2. 传入类型为T的数组或Vector，使用(T[] objs)或(Vector<T> objs)构造方法
    protected static final class ArrayOf<T extends XMLObj> extends XMLObj {
        private Vector<T> mObjs;


        public ArrayOf(T[] objs) {

            mObjs = new Vector<>(Arrays.asList(objs));
        }


        public ArrayOf(Vector<T> objs) {

            mObjs = objs;
        }


        public ArrayOf(Reader in, String openTag, Class c, String itemOpenTag) throws Exception {
            logDebug("ArrayOf: begin");
            mObjs = new Vector<>();

            int oldReaderPos = getReaderPos(in);
            XmlPullParser xpp = sFactory.newPullParser();
            xpp.setInput(in);

            try {
                int e = xpp.getEventType();
                while (e != XmlPullParser.END_DOCUMENT) {

                    if (e != XmlPullParser.START_TAG) {
                        e = xpp.next();
                        continue;
                    }

                    String tagName = xpp.getName();
                    if (tagName.equals(itemOpenTag)) {
                        T obj = (T) parseSubXmlObj(xpp, in, oldReaderPos, itemOpenTag, c);
                        mObjs.add(obj);
                    }

                    e = xpp.next();
                }
            } catch (XmlPullParserException e) {
                if (xpp.getName() == null || !xpp.getName().equals(openTag)) {
                    throw e;
                }
            }
            logDebug("ArrayOf: finish");
        }


        public T get(int i) {
            return mObjs.get(i);
        }


        public int size() {
            return mObjs.size();
        }

    }






    // - - - util method - - -
    private static HashSet<String> newHashSet(String... strings) {
        HashSet<String> set = new HashSet<String>();
        for (String s : strings) {
            set.add(s);
        }
        return set;
    }


    // 如果当前的tag name在defaults中，设置o的对应值
    // 如果成功设置，返回True
    // 否则返回False
    private static boolean setDefaultField(XmlPullParser parser, Class c, XMLObj o, Set<String> defaults) throws Exception {
        String tagName = parser.getName();
        if (defaults.contains(tagName)) {
            // tag name在defaults中
            String value = parser.nextText();
            Field field = c.getField(tagName);
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


    // 获取一个reader的pos位置，如果没有会抛出异常
    private static int getReaderPos(Reader reader) throws Exception{
        Field f = reader.getClass().getDeclaredField("pos");
        if (!f.isAccessible()) {
            f.setAccessible(true);
        }
        return (int) f.get(reader);
    }


    // 获取一个XmlPullParser的position值(private)，并将reader的位置设置为那个值
    // hell yeah
    // 保存reader的位置，新建一个类型是c的XML对象，再恢复reader的位置
    // 最后返回新建的对象
    private static XMLObj parseSubXmlObj(XmlPullParser xpp, Reader reader, int parentStartPos, String openTag, Class<? extends XMLObj> c) throws NoSuchFieldException, IllegalAccessException, IOException, NoSuchMethodException, InstantiationException {

        Field xppPosField = xpp.getClass().getDeclaredField("position");
        if (!xppPosField.isAccessible()) {
            xppPosField.setAccessible(true);
        }
        int xppPos = (int) xppPosField.get(xpp);

        // 可以用任意支持pos的reader替换
        Field f = reader.getClass().getDeclaredField("pos");
        if (!f.isAccessible()) {
            f.setAccessible(true);
        }
        int readerPos = (int) f.get(reader);


        // 将reader设置在xpp读到的位置上
        reader.reset();
        logDebug("Reader Position is set to " + parentStartPos + " + " + xppPos);
        reader.skip(parentStartPos + xppPos);

        // 创建对象
        XMLObj obj = null;
        try {
            obj = c.getConstructor(Reader.class, String.class).newInstance(reader, openTag);
        }
        catch (InvocationTargetException e) {
            logError("This is the cause:");
            e.getTargetException().printStackTrace();
        }

        // 恢复reader的位置
        reader.reset();
        reader.skip(readerPos);

        return obj;
    }


    // - - - util method - - -


    private static final void logDebug(String msg) {
        Log.d("XMLUtil", ((Looper.getMainLooper().equals(Looper.myLooper())) ? "[UI]" : "[notUI]") + msg);
    }

    private static final void logError(String msg) {
        Log.e("XMLUtil", ((Looper.getMainLooper().equals(Looper.myLooper())) ? "[UI]" : "[notUI]") + msg);
    }
}
