package com.uservoice.uvdemo;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;

import com.uservoice.uservoicesdk.Config;
import com.uservoice.uservoicesdk.UserVoice;

public class MainActivity extends Activity implements UserVoice.IHelpLinkClickedCallback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//		Config config = new Config("yoursite.uservoice.com");
        Config config = new Config("demo.uservoice.com");
//        config.setShowKnowledgeBase(false);
        List<String> helpLinks = new ArrayList<String>();
        helpLinks.add("This is the first custom help link");
        helpLinks.add("This is the second");
        config.setHelpLinks(helpLinks);
        UserVoice.init(config, this);

        // hack to always show the overflow menu in the action bar
        try {
            ViewConfiguration viewConfig = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(viewConfig, false);
            }
        } catch (Exception ex) {
            // Ignore
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void launchFeedback(MenuItem menuItem) {
        UserVoice.launchUserVoice(this, this);
    }

    public void launchFeedback(View view) {
        UserVoice.launchUserVoice(this, this);
    }

    public void launchForum(View view) {
        UserVoice.launchForum(this);
    }

    public void launchContactUs(View view) {
        UserVoice.launchContactUs(this);
    }

    public void launchPostIdea(View view) {
        UserVoice.launchPostIdea(this);
    }

    @Override
    public void onHelpLinkClicked(Context context, int index) {
        new AlertDialog.Builder(context)
                .setTitle("Help link clicked")
                .setMessage("You've clicked on custom help link index #" + index)
                .show();
    }
}
