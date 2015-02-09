package me.tgmerge.such98;

import android.os.Looper;
import android.util.Log;
import android.util.Xml;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Vector;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

/**
 * Created by tgmerge on 2/8.
 * 其实可以用反射来写……但好像有点麻烦，先这样放着了
 */
public class XMLUtil {

    private static XPath mXP = XPathFactory.newInstance().newXPath();
    private static HashMap<String, XPathExpression> mKnownXPEs = new HashMap<>();

    // 根据XPath字符串返回编译后的XPathExpression
    // 曾经编译过的XPath会直接返回，无需再次编译
    // 解析XML时就不用费力static了……
    private static XPathExpression getXPE(String expStr) throws XPathExpressionException {
        XPathExpression exp = mKnownXPEs.get(expStr);
        if (exp == null) {
            exp = mXP.compile(expStr);
            mKnownXPEs.put(expStr, exp);
        }
        return exp;
    }

    // 直接返回XPath形如./name在e上执行后的结果，为一个Node
    private static Node xElemNode(String name, Node e) throws XPathExpressionException {
        return (Node) getXPE("./" + name).evaluate(e, XPathConstants.NODE);
    }

    // 直接返回XPath形如./name/text()在e上执行后的结果，类型为String
    private static String xElemString(String name, Node e) throws XPathExpressionException {
        return getXPE("./" + name + "/text()").evaluate(e);
    }

    // 直接返回XPath形如/root/name/text()在e上执行后的结果，类型为int
    private static int xElemInt(String name, Node e) throws XPathExpressionException {
        return Integer.parseInt(xElemString(name, e));
    }

    // 直接返回XPath形如/root/name/text()在e上执行后的结果，类型为Boolean
    private static boolean xElemBoolean(String name, Node e) throws XPathExpressionException {
        return Boolean.parseBoolean(xElemString(name, e));
    }

    // 提供获取XML内容的基本工具方法
    // 可用于获取XPath形如./{name}/text()的内容
    protected static class XMLObj {
        private Node mElem;

        public XMLObj(Node e) {
            this.mElem = e;
        }

        protected final Node xNode(String name) throws XPathExpressionException {
            return xElemNode(name, mElem);
        }

        protected final String xString(String name) throws XPathExpressionException {
            return xElemString(name, mElem);
        }

        protected final int xInt(String name) throws XPathExpressionException {
            return xElemInt(name, mElem);
        }

        protected final Boolean xBoolean(String name) throws XPathExpressionException {
            return xElemBoolean(name, mElem);
        }

        protected final void clearElem() {
            this.mElem = null;
        }
    }

    protected static class ArrayOf<T extends XMLObj> extends XMLObj {
        private Vector<T> mObjs;

        public ArrayOf(Class c, Node e) throws Exception {
            super(e);
            mObjs = new Vector<>();
            Constructor con = c.getDeclaredConstructor(Node.class);

            NodeList nodes = e.getChildNodes();
            for (int i = 0; i < nodes.getLength(); i ++) {
                Node node = nodes.item(i);
                if ( ! node.getNodeName().equals("")) {
                    mObjs.add((T) con.newInstance(node));
                }
            }
            clearElem();
        }

        protected T get(int i) {
            return mObjs.get(i);
        }

        protected int size(int i) {
            return mObjs.size();
        }
    }


    // - - - util

    protected static final class BoardLastPostInfo extends XMLObj {
        int BoardId;
        int TopicId;
        int PostId;
        String DateTime;  // todo type is date time
        String UserName;
        int UserId;
        String TopicTitle;

        // 由于没有推断xml类型的方法，
        // 默认根元素已经是BoardLastPostInfo类型了
        public BoardLastPostInfo(Node e) throws XPathExpressionException {
            super(e);
            BoardId = xInt("BoardId");
            TopicId = xInt("TopicId");
            PostId = xInt("PostId");
            DateTime = xString("DateTime");
            UserName = xString("UserName");
            UserId = xInt("UserId");
            TopicTitle = xString("TopicTitle");
            clearElem();
        }
    }

    protected static final class BoardInfo extends XMLObj {
        int Id;
        String Name;
        String Description;
        int ChildBoardCount;
        int ParentId;
        int RootId;
        int TotalPostCount;
        int TotalTopicCount;
        int TodayPostCount;
        boolean IsHidden;
        boolean IsCategory;
        boolean IsEncrypted;
        boolean IsAnomynous;  // todo typo in xml defination
        boolean IsLocked;
        Vector<String> Masters;
        BoardLastPostInfo LastPostInfo;
        String PostTimeLimit; // todo type is timespan

        public BoardInfo(Node e) throws XPathExpressionException {
            super(e);
            Id = xInt("Id");
            Name = xString("Name");
            Description = xString("Description");
            ChildBoardCount = xInt("ChildBoardCount");
            ParentId = xInt("ParentId");
            RootId = xInt("RootId");
            TotalPostCount = xInt("TotalPostCount");
            TotalTopicCount = xInt("TotalTopicCount");
            TodayPostCount = xInt("TodayPostCount");
            IsHidden = xBoolean("IsHidden");
            IsCategory = xBoolean("IsCategory");
            IsEncrypted = xBoolean("IsEncrypted");
            IsAnomynous = xBoolean("IsAnomynous"); // todo typo in xml
            IsLocked = xBoolean("IsLocked");

            Masters = new Vector<>();
            NodeList mNodes = xNode("Masters").getChildNodes();
            for (int i = 0; i < mNodes.getLength(); i ++) {
                Node mNode = mNodes.item(i);
                if (mNode.getNodeName().equals("d3p1:string")) {
                    Masters.add(mNode.getTextContent());
                }
            }

            LastPostInfo = new BoardLastPostInfo(xNode("LastPostInfo"));

            PostTimeLimit = xString("PostTimeLimit");

            clearElem();
        }
    }



    // - - - util method - - -

    private static final XmlPullParser getXmlPullParser() throws XmlPullParserException {
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(false);
        return factory.newPullParser();
    }

    private static final void logDebug(String msg) {
        Log.d("XMLUtil", msg);
        Log.d("XMLUtil", "Thread: on UI? " + (Looper.getMainLooper().equals(Looper.myLooper())));
    }

    private static final void logError(String msg) {
        Log.e("XMLUtil", msg);
        Log.d("XMLUtil", "Thread: on UI? " + (Looper.getMainLooper().equals(Looper.myLooper())));
    }
}
