package me.tgmerge.such98;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import org.apache.http.Header;
import java.io.StringReader;



/* Intent params:
 * id - Board ID to be shown
 *      value:
 *          ID_ROOT(0)    - show root board
 *          ID_CUSTOM(-1) - show custom board
 *          others(id)    - show specified board
 */
public class ShowBoardsActivity extends ActionBarActivity {

    public final static String INTENT_ID = "id";
    public final static int ID_ROOT = 0;
    public final static int ID_CUSTOM = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_boards);

        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        Intent intent = getIntent();
        int intentId = intent.getIntExtra(INTENT_ID, 0);
        final Activity that = this;

        intentId = 2;

        if (intentId == ID_ROOT) {

            // Show root board
            setTitle("ShowBoardsActivity: Root Board");
            new APIUtil.GetRootBoard(this, 0, null, 20, new APIUtil.APICallback() { // todo: hardcoded pagesize
                @Override
                public void onSuccess(int statCode, Header[] headers, byte[] body) {
                    String s = new String(body);
                    XMLUtil.ArrayOf<XMLUtil.BoardInfo> boardInfoArr = new XMLUtil.ArrayOf<>(XMLUtil.BoardInfo.class);
                    try {
                        boardInfoArr.parse(s);
                    } catch (Exception e) {
                        HelperUtil.errorToast(that, "GetRootBoard parse exception:" + e.toString());
                        e.printStackTrace();
                    }
                    recyclerView.setAdapter(new ShowBoardsAdapter(boardInfoArr));
                }

                @Override
                public void onFailure(int statCode, Header[] headers, byte[] body, Throwable error) {
                    HelperUtil.errorToast(that, "GetRootBoard failed, code=" + statCode + ", info=" + new String(body));
                }
            }).execute();

        } else if (intentId == ID_CUSTOM) {

            // Show custom boards
            setTitle("ShowBoardsActivity: Custom Boards");
            new APIUtil.GetCustomBoardsMe(this, new APIUtil.APICallback() {
                @Override
                public void onSuccess(int statCode, Header[] headers, byte[] body) {
                    String s = new String(body);
                    XMLUtil.ArrayOfint boardIdVec = new XMLUtil.ArrayOfint();
                    try {
                        boardIdVec.parse(s);
                    } catch (Exception e) {
                        HelperUtil.errorToast(that, "GetCustomBoardsMe parse exception:" + e.toString());
                        e.printStackTrace();
                    }

                    int[] ids = new int[boardIdVec.values.size()];
                    for (int i = 0; i < boardIdVec.values.size(); i ++) {
                        ids[i] = boardIdVec.values.get(i);
                    }

                    new APIUtil.GetMultiBoards(that, ids, 0, null, 20, new APIUtil.APICallback() { // todo: hardcoded pagesize
                        @Override
                        public void onSuccess(int statCode, Header[] headers, byte[] body) {
                            String ss = new String(body);
                            XMLUtil.ArrayOf<XMLUtil.BoardInfo> boardInfoArr = new XMLUtil.ArrayOf<>(XMLUtil.BoardInfo.class);
                            try {
                                boardInfoArr.parse(ss);
                            } catch (Exception e) {
                                HelperUtil.errorToast(that, "GetMultiBoards parse exception:" + e.toString());
                                e.printStackTrace();
                            }
                            recyclerView.setAdapter(new ShowBoardsAdapter(boardInfoArr));
                        }

                        @Override
                        public void onFailure(int statCode, Header[] headers, byte[] body, Throwable error) {
                            HelperUtil.errorToast(that, "GetMultiBoards failed, code=" + statCode + ", info=" + new String(body));
                        }
                    }).execute();
                }

                @Override
                public void onFailure(int statCode, Header[] headers, byte[] body, Throwable error) {
                    HelperUtil.errorToast(that, "GetCustomBoardsMe failed, code=" + statCode + ", info=" + new String(body));
                }
            }).execute();

        } else {

            // Show board by id
            setTitle("ShowBoardsActivity: Board ID=" + intentId);
            new APIUtil.GetSubBoards(this, intentId, 0, null, 20, new APIUtil.APICallback() {
                @Override
                public void onSuccess(int statCode, Header[] headers, byte[] body) {
                    String s = new String(body);
                    XMLUtil.ArrayOf<XMLUtil.BoardInfo> boardInfoArr = new XMLUtil.ArrayOf<>(XMLUtil.BoardInfo.class);
                    try {
                        boardInfoArr.parse(s);
                    } catch (Exception e) {
                        HelperUtil.errorToast(that, "GetSubBoards parse exception:" + e.toString());
                        e.printStackTrace();
                    }
                    recyclerView.setAdapter(new ShowBoardsAdapter(boardInfoArr));
                }

                @Override
                public void onFailure(int statCode, Header[] headers, byte[] body, Throwable error) {
                    HelperUtil.errorToast(that, "GetSubBoards failed, code=" + statCode + ", info=" + new String(body));
                }
            }).execute();
        }

    }



    private static class ShowBoardsAdapter extends RecyclerView.Adapter<ShowBoardsAdapter.ViewHolder> {

        private XMLUtil.ArrayOf<XMLUtil.BoardInfo> mData;


        public ShowBoardsAdapter(XMLUtil.ArrayOf<XMLUtil.BoardInfo> data) {
            mData = data;
        }


        // Create new views, invoked by the layout manager
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            // create a new view
            View itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_board_card, parent, false);
            return new ViewHolder(itemLayoutView);
        }


        // Replace the contents of a view, invoked by the layout manager
        @Override
        public void onBindViewHolder(ViewHolder viewHolder, int position) {
            XMLUtil.BoardInfo dataItem = mData.get(position);
            viewHolder.name.setText(dataItem.Name);
            viewHolder.isCategory.setText(dataItem.IsCategory ? "分类" : "");
            viewHolder.description.setText(dataItem.Description);
        }


        // Return the size of items data, invoked by the layout manager
        @Override
        public int getItemCount() {
            return mData.size();
        }


        // inner class to hold a reference to each item of RecyclerView
        public static class ViewHolder extends RecyclerView.ViewHolder {

            public TextView name;
            public TextView isCategory;
            public TextView description;

            public ViewHolder(View itemLayoutView) {
                super(itemLayoutView);
                name = (TextView) itemLayoutView.findViewById(R.id.text_name);
                isCategory = (TextView) itemLayoutView.findViewById(R.id.text_isCategory);
                description = (TextView) itemLayoutView.findViewById(R.id.text_description);
            }
        }
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
