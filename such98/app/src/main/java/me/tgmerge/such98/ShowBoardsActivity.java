package me.tgmerge.such98;

import android.app.Activity;
import android.content.Context;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Xml;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import org.apache.http.Header;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;


public class ShowBoardsActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_boards);

        try {
            test();
        } catch (Exception e) {
            e.printStackTrace();
        }

        final Activity that = this;
        // todo SetUILoading();
        /*
        new APIUtil.GetRootBoard(this, 0, null, 10, new APIUtil.APICallback() {
            @Override
            public void onSuccess(int statCode, Header[] headers, byte[] body) {
                // todo boardObj = parseBoardXML();
                // todo updateUi(boardObj);
            }

            @Override
            public void onFailure(int statCode, Header[] headers, byte[] body, Throwable error) {
                Toast.makeText(that, "Error: get root board failure", Toast.LENGTH_LONG).show();
            }
        }).execute();*/
    }

    protected void test() throws Exception {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        String xml = "<ArrayOfBoardInfo xmlns:i=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://schemas.datacontract.org/2004/07/CC98.Api.Models\"><BoardInfo><ChildBoardCount>82</ChildBoardCount><Description>教师答疑、学生提问、师生交流、学习心得体会，导论、网络课件下载</Description><Id>2</Id><IsAnomynous>false</IsAnomynous><IsCategory>true</IsCategory><IsEncrypted>false</IsEncrypted><IsHidden>false</IsHidden><IsLocked>false</IsLocked><LastPostInfo><BoardId>714</BoardId><DateTime>2015-02-07T18:17:43</DateTime><PostId>789356752</PostId><TopicId>4485149</TopicId><TopicTitle>2014-2015冬学期课程...</TopicTitle><UserId>458430</UserId><UserName>Delostik</UserName></LastPostInfo><Masters xmlns:d3p1=\"http://schemas.microsoft.com/2003/10/Serialization/Arrays\"><d3p1:string>powercqcq</d3p1:string></Masters><Name>教师答疑</Name><ParentId>0</ParentId><PostTimeLimit>PT10S</PostTimeLimit><RootId>2</RootId><TodayPostCount>0</TodayPostCount><TotalPostCount>40883</TotalPostCount><TotalTopicCount>9797</TotalTopicCount></BoardInfo><BoardInfo><ChildBoardCount>12</ChildBoardCount><Description>学习与交流，专业与选课，梦想与未来，求知行思，我们一起畅游在学习的天地里</Description><Id>3</Id><IsAnomynous>false</IsAnomynous><IsCategory>true</IsCategory><IsEncrypted>false</IsEncrypted><IsHidden>false</IsHidden><IsLocked>false</IsLocked><LastPostInfo><BoardId>68</BoardId><DateTime>2015-02-08T00:33:43</DateTime><PostId>789359540</PostId><TopicId>4485121</TopicId><TopicTitle>98Deleter是谁？该回...</TopicTitle><UserId>358879</UserId><UserName>青杨</UserName></LastPostInfo><Masters xmlns:d3p1=\"http://schemas.microsoft.com/2003/10/Serialization/Arrays\"><d3p1:string>powercqcq</d3p1:string></Masters><Name>学习天地</Name><ParentId>0</ParentId><PostTimeLimit>PT10S</PostTimeLimit><RootId>3</RootId><TodayPostCount>1</TodayPostCount><TotalPostCount>684760</TotalPostCount><TotalTopicCount>46694</TotalTopicCount></BoardInfo><BoardInfo><ChildBoardCount>11</ChildBoardCount><Description>工作兼职、新闻时事……给你提供一个资讯的平台</Description><Id>4</Id><IsAnomynous>false</IsAnomynous><IsCategory>true</IsCategory><IsEncrypted>false</IsEncrypted><IsHidden>false</IsHidden><IsLocked>false</IsLocked><LastPostInfo><BoardId>339</BoardId><DateTime>2015-02-07T23:43:04</DateTime><PostId>789359056</PostId><TopicId>4485194</TopicId><TopicTitle>手套求主人认领YQ</TopicTitle><UserId>465534</UserId><UserName>frankkoo</UserName></LastPostInfo><Masters xmlns:d3p1=\"http://schemas.microsoft.com/2003/10/Serialization/Arrays\"><d3p1:string>dirklz</d3p1:string></Masters><Name>信息资讯</Name><ParentId>0</ParentId><PostTimeLimit>PT10S</PostTimeLimit><RootId>4</RootId><TodayPostCount>0</TodayPostCount><TotalPostCount>713240</TotalPostCount><TotalTopicCount>99101</TotalTopicCount></BoardInfo><BoardInfo><ChildBoardCount>16</ChildBoardCount><Description>校园生活，合作信息，轻松点击，尽收眼底。</Description><Id>5</Id><IsAnomynous>false</IsAnomynous><IsCategory>true</IsCategory><IsEncrypted>false</IsEncrypted><IsHidden>false</IsHidden><IsLocked>false</IsLocked><LastPostInfo><BoardId>537</BoardId><DateTime>2015-02-08T00:52:19</DateTime><PostId>789359597</PostId><TopicId>4484951</TopicId><TopicTitle>【寒假·2015官方水楼2...</TopicTitle><UserId>5298</UserId><UserName>Auser</UserName></LastPostInfo><Masters xmlns:d3p1=\"http://schemas.microsoft.com/2003/10/Serialization/Arrays\"><d3p1:string>磊磊1010</d3p1:string></Masters><Name>校园动态</Name><ParentId>0</ParentId><PostTimeLimit>PT10S</PostTimeLimit><RootId>5</RootId><TodayPostCount>306</TodayPostCount><TotalPostCount>3799202</TotalPostCount><TotalTopicCount>105659</TotalTopicCount></BoardInfo><BoardInfo><ChildBoardCount>12</ChildBoardCount><Description>时尚，旅行，美食，健康，数码，香车……生活个性尽在个性生活</Description><Id>6</Id><IsAnomynous>false</IsAnomynous><IsCategory>true</IsCategory><IsEncrypted>false</IsEncrypted><IsHidden>false</IsHidden><IsLocked>false</IsLocked><LastPostInfo><BoardId>180</BoardId><DateTime>2015-02-08T00:18:56</DateTime><PostId>789359422</PostId><TopicId>4483817</TopicId><TopicTitle>[伪开箱]PS4到啦</TopicTitle><UserId>310657</UserId><UserName>洗头</UserName></LastPostInfo><Masters xmlns:d3p1=\"http://schemas.microsoft.com/2003/10/Serialization/Arrays\"><d3p1:string>hezyyn</d3p1:string></Masters><Name>个性生活</Name><ParentId>0</ParentId><PostTimeLimit>PT10S</PostTimeLimit><RootId>6</RootId><TodayPostCount>1</TodayPostCount><TotalPostCount>543295</TotalPostCount><TotalTopicCount>29233</TotalTopicCount></BoardInfo><BoardInfo><ChildBoardCount>15</ChildBoardCount><Description>让我们大家一起来放松一下吧！</Description><Id>8</Id><IsAnomynous>false</IsAnomynous><IsCategory>true</IsCategory><IsEncrypted>false</IsEncrypted><IsHidden>false</IsHidden><IsLocked>false</IsLocked><LastPostInfo><BoardId>135</BoardId><DateTime>2015-02-08T00:46:20</DateTime><PostId>789359586</PostId><TopicId>4485196</TopicId><TopicTitle>烧烤节要到了，我的团魂在熊熊...</TopicTitle><UserId>397026</UserId><UserName>lv1148</UserName></LastPostInfo><Masters xmlns:d3p1=\"http://schemas.microsoft.com/2003/10/Serialization/Arrays\"><d3p1:string>洗头</d3p1:string></Masters><Name>休闲娱乐</Name><ParentId>0</ParentId><PostTimeLimit>PT10S</PostTimeLimit><RootId>8</RootId><TodayPostCount>5</TodayPostCount><TotalPostCount>1672839</TotalPostCount><TotalTopicCount>58112</TotalTopicCount></BoardInfo><BoardInfo><ChildBoardCount>17</ChildBoardCount><Description>体育 运动 激情释放一切</Description><Id>10</Id><IsAnomynous>false</IsAnomynous><IsCategory>true</IsCategory><IsEncrypted>false</IsEncrypted><IsHidden>false</IsHidden><IsLocked>false</IsLocked><LastPostInfo><BoardId>15</BoardId><DateTime>2015-02-08T00:27:50</DateTime><PostId>789359472</PostId><TopicId>2710604</TopicId><TopicTitle>★皇家马德里★ 四大皆空第...</TopicTitle><UserId>310657</UserId><UserName>洗头</UserName></LastPostInfo><Masters xmlns:d3p1=\"http://schemas.microsoft.com/2003/10/Serialization/Arrays\"><d3p1:string>TuTuJ</d3p1:string></Masters><Name>体育运动</Name><ParentId>0</ParentId><PostTimeLimit>PT10S</PostTimeLimit><RootId>10</RootId><TodayPostCount>1</TodayPostCount><TotalPostCount>751285</TotalPostCount><TotalTopicCount>20906</TotalTopicCount></BoardInfo><BoardInfo><ChildBoardCount>12</ChildBoardCount><Description>影音时尚无限，电影、tv、综艺、音乐、翻唱，空间锁定，尽情点击！</Description><Id>11</Id><IsAnomynous>false</IsAnomynous><IsCategory>true</IsCategory><IsEncrypted>false</IsEncrypted><IsHidden>false</IsHidden><IsLocked>false</IsLocked><LastPostInfo><BoardId>314</BoardId><DateTime>2015-02-07T23:58:34</DateTime><PostId>789359244</PostId><TopicId>4483866</TopicId><TopicTitle>【翻唱】新人求指教！浮夸+私...</TopicTitle><UserId>480502</UserId><UserName>狼啸九天</UserName></LastPostInfo><Masters xmlns:d3p1=\"http://schemas.microsoft.com/2003/10/Serialization/Arrays\"><d3p1:string>TuTuJ</d3p1:string></Masters><Name>影音无限</Name><ParentId>0</ParentId><PostTimeLimit>PT10S</PostTimeLimit><RootId>11</RootId><TodayPostCount>0</TodayPostCount><TotalPostCount>156757</TotalPostCount><TotalTopicCount>7249</TotalTopicCount></BoardInfo><BoardInfo><ChildBoardCount>22</ChildBoardCount><Description>电脑技术，编程，图形设计，软件开发，装机咨询，故障解决</Description><Id>12</Id><IsAnomynous>false</IsAnomynous><IsCategory>true</IsCategory><IsEncrypted>false</IsEncrypted><IsHidden>false</IsHidden><IsLocked>false</IsLocked><LastPostInfo><BoardId>329</BoardId><DateTime>2015-02-08T00:42:54</DateTime><PostId>789359582</PostId><TopicId>871871</TopicId><TopicTitle>水楼,恩恩</TopicTitle><UserId>418480</UserId><UserName>sliverwxj</UserName></LastPostInfo><Masters xmlns:d3p1=\"http://schemas.microsoft.com/2003/10/Serialization/Arrays\"><d3p1:string>Auser</d3p1:string></Masters><Name>电脑技术</Name><ParentId>0</ParentId><PostTimeLimit>PT10S</PostTimeLimit><RootId>12</RootId><TodayPostCount>2</TodayPostCount><TotalPostCount>199333</TotalPostCount><TotalTopicCount>21858</TotalTopicCount></BoardInfo><BoardInfo><ChildBoardCount>12</ChildBoardCount><Description>科技 社会 探索 幻想</Description><Id>13</Id><IsAnomynous>false</IsAnomynous><IsCategory>true</IsCategory><IsEncrypted>false</IsEncrypted><IsHidden>false</IsHidden><IsLocked>false</IsLocked><LastPostInfo><BoardId>402</BoardId><DateTime>2015-02-07T22:49:00</DateTime><PostId>789358572</PostId><TopicId>4371178</TopicId><TopicTitle>【水楼6.0 &amp; 米楼】...</TopicTitle><UserId>390984</UserId><UserName>赫耳墨斯</UserName></LastPostInfo><Masters xmlns:d3p1=\"http://schemas.microsoft.com/2003/10/Serialization/Arrays\"><d3p1:string>BandM</d3p1:string></Masters><Name>社科学术</Name><ParentId>0</ParentId><PostTimeLimit>PT10S</PostTimeLimit><RootId>13</RootId><TodayPostCount>0</TodayPostCount><TotalPostCount>121818</TotalPostCount><TotalTopicCount>5276</TotalTopicCount></BoardInfo></ArrayOfBoardInfo>";
        StringReader r = null;
        Log.d("old", "");
        r = new StringReader(xml);
        XMLUtil.ArrayOf<XMLUtil.BoardInfo> o = new XMLUtil.ArrayOf<>(r, "ArrayOfBoardInfo", XMLUtil.BoardInfo.class, "BoardInfo");
        Log.d("new", "" + o);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_show_boards, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
