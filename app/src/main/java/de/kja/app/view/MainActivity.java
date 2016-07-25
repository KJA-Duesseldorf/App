package de.kja.app.view;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.rest.spring.annotations.RestService;
import org.androidannotations.rest.spring.api.RestErrorHandler;
import org.springframework.core.NestedRuntimeException;

import java.util.List;

import de.kja.app.R;
import de.kja.app.client.ContentClient;
import de.kja.app.model.Content;

@EActivity
@OptionsMenu(R.menu.menu)
public class MainActivity extends AppCompatActivity implements RestErrorHandler, View.OnClickListener {

    @ViewById(R.id.swiperefresh)
    protected SwipeRefreshLayout swipeRefreshLayout;

    @ViewById(R.id.listview)
    protected RecyclerView listview;

    @RestService
    public static ContentClient contentClient;

    protected ContentAdapter contentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        contentClient.setRestErrorHandler(this);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                update();
            }
        });

        listview.setHasFixedSize(true);
        listview.setLayoutManager(new LinearLayoutManager(this));

        contentAdapter = new ContentAdapter(this);
        listview.setAdapter(contentAdapter);

        update();
    }

    @OptionsItem(R.id.menu_refresh)
    protected void menuRefreshSelected() {
        update();
    }

    @Background
    protected void update() {
        List<Content> contents = contentClient.getContents();
        showUpdate(contents);
    }

    @UiThread
    protected void showUpdate(List<Content> contents) {
        if(contents != null) {
            contentAdapter.setContents(contents);
        }
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    @UiThread
    public void onRestClientExceptionThrown(NestedRuntimeException e) {
        Log.e("MainActivity", "REST client error!", e);
        new AlertDialog.Builder(this)
                .setTitle(R.string.connectionerror)
                .setMessage(R.string.tryagain)
                .setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).show();

    }

    @Override
    public void onClick(View v) {
        int position = listview.getChildLayoutPosition(v);
        Content content = contentAdapter.getContent(position);

        Intent intent = new Intent(this, ContentActivity_.class);
        intent.putExtra(ContentActivity.EXTRA_TITLE, content.getTitle());
        intent.putExtra(ContentActivity.EXTRA_TEXT, content.getText());
        startActivity(intent);
    }
}
