package com.bahri.simaling;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;


import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.bahri.simaling.NewsAdapter;
import com.bahri.simaling.AppController;
import com.bahri.simaling.NewsData;
import com.bahri.simaling.Server;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import java.lang.reflect.Array;
import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */

public class InformasiFragment extends Fragment implements OnRefreshListener {

    ListView list;
    SwipeRefreshLayout swipe;
    List<NewsData> newsList = new ArrayList<NewsData>();
    private static final String TAG = InformasiFragment.class.getSimpleName();

    private static String url_list   = Server.URL + "news.php?offset=";

    private int offSet = 0;

    int no;

    NewsAdapter adapter;

    public static final String TAG_NO       = "no";
    public static final String TAG_ID       = "id";
    public static final String TAG_JUDUL    = "judul";
    public static final String TAG_TGL      = "tgl";
    public static final String TAG_ISI      = "isi";
    public static final String TAG_GAMBAR   = "gambar";

    Handler handler;
    Runnable runnable;

    private InformasiFragment instace;

    public InformasiFragment() {
        // Required empty public constructor
        instace = this;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_informasi, container, false);
        swipe = (SwipeRefreshLayout)view.findViewById(R.id.swipe_refresh_layout);
        list = (ListView) view.findViewById(R.id.list_news);
        newsList.clear();

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), detail_news.class);
                intent.putExtra(TAG_ID, newsList.get(position).getId());
                startActivity(intent);
            }
        });

        adapter = new NewsAdapter(getActivity(), newsList);
        list.setAdapter(adapter);
        swipe.setOnRefreshListener(this);
        swipe.post(new Runnable() {
                       @Override
                       public void run() {
                           swipe.setRefreshing(true);
                           newsList.clear();
                           adapter.notifyDataSetChanged();
                           callNews(0);
                       }
                   }
        );

        list.setOnScrollListener(new AbsListView.OnScrollListener() {
            private int currentVisibleItemCount;
            private int currentScrollState;
            private int currentFirstVisibleItem;
            private int totalItem;
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                this.currentScrollState = scrollState;
                this.isScrollCompleted();
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                this.currentFirstVisibleItem = firstVisibleItem;
                this.currentVisibleItemCount = visibleItemCount;
                this.totalItem = totalItemCount;
            }

            private void isScrollCompleted(){
                if (totalItem - currentFirstVisibleItem == currentVisibleItemCount
                        && this.currentScrollState == SCROLL_STATE_IDLE) {

                    swipe.setRefreshing(true);
                    handler = new Handler();

                    runnable = new Runnable() {
                        public void run() {
                            callNews(offSet);
                        }
                    };

                    handler.postDelayed(runnable, 3000);
                }
            }
        });

        return view;
    }



    private void callNews(int page){

        swipe.setRefreshing(true);

        // Creating volley request obj
        JsonArrayRequest arrReq = new JsonArrayRequest(url_list + page,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d(TAG, response.toString());

                        if (response.length() > 0) {
                            // Parsing json
                            for (int i = 0; i < response.length(); i++) {
                                try {

                                    JSONObject obj = response.getJSONObject(i);
                                    NewsData news = new NewsData();

                                    no = obj.getInt(TAG_NO);

                                    news.setId(obj.getString(TAG_ID));
                                    news.setJudul(obj.getString(TAG_JUDUL));

                                    if (obj.getString(TAG_GAMBAR) != "") {
                                        news.setGambar(obj.getString(TAG_GAMBAR));
                                    }

                                    news.setDatetime(obj.getString(TAG_TGL));
                                    news.setIsi(obj.getString(TAG_ISI));

                                    // adding news to news array
                                    newsList.add(news);

                                    if (no > offSet)
                                        offSet = no;

                                    Log.d(TAG, "offSet " + offSet);

                                } catch (JSONException e) {
                                    Log.e(TAG, "JSON Parsing error: " + e.getMessage());
                                }

                                // notifying list adapter about data changes
                                // so that it renders the list view with updated data
                                adapter.notifyDataSetChanged();
                            }
                        }
                        swipe.setRefreshing(false);
                    }

                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                swipe.setRefreshing(false);
            }
        });

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(arrReq);
    }

    @Override
    public void onRefresh() {
        newsList.clear();
        adapter.notifyDataSetChanged();
        callNews(0);
    }
}
