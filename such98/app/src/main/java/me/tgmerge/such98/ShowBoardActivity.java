package me.tgmerge.such98;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.apache.http.Header;

import java.util.ArrayList;
import java.util.List;


public class ShowBoardActivity extends ActionBarActivity {

    public static final List<String> data;

    static {
        data = new ArrayList<String>();
        data.add("item1");
        data.add("item1");
        data.add("item1");
        data.add("item1");
        data.add("item1");
        data.add("item1");
        data.add("item1");
        data.add("item1");
        data.add("item1");
        data.add("item1");
        data.add("item1");
        data.add("item1");
        data.add("item1");
        data.add("item1");
    }

    LinearLayoutManager layoutManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_board);

        // Get RecyclerView
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.RecyclerView);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(new MyAdapter(data));

        new APIUtil.GetRootBoard(this, 0, null, 10, new APIUtil.APICallback() {
            public void onSuccess(int statCode, Header[] headers, byte[] body) {
                //... ...
            }
            @Override
            public void onFailure(int statCode, Header[] headers, byte[] body, Throwable error) {
                //... ...
            }
        }).execute();

    }

    private static class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
        private List<String> data;

        public MyAdapter(List<String> data) {
            this.data = data;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            // 加载Item的布局.布局中用到的真正的CardView.
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_item_activity_show_board, viewGroup, false);
            // ViewHolder参数一定要是Item的Root节点.
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, int i) {
            viewHolder.text.setText(data.get(i));
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView text;

            public ViewHolder(View itemView) {
                // super这个参数一定要注意,必须为Item的根节点.否则会出现莫名的FC.
                super(itemView);
                text = (TextView) itemView.findViewById(R.id.text);
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_show_board, menu);
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
