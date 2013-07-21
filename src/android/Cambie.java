/**
 * Copyright 2013 Darryl Pogue
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cordova.plugins;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.FragmentTransaction;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.Window;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.apache.cordova.CordovaWebView;
import org.apache.cordova.DroidGap;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;


class Actionable
{

    private static AssetManager sAssetMgr;

    private String mTitle;

    private Boolean mDisabled;

    private int mFlags;

    private Drawable mIcon;

    private Boolean mSelected;

    private String mCallbackId;

    private int mOrder = 0;


    public static Actionable fromJSON(JSONObject jsobj)
    {
        try
        {
            Actionable action = new Actionable();
            action.mTitle = jsobj.getString("label");
            action.mDisabled = jsobj.optBoolean("disabled", false);
            action.mSelected = jsobj.optBoolean("selected", false);

            action.mCallbackId = jsobj.optString("callback");

            String iconname = jsobj.optString("icon", null);

            if (iconname != null) {
                try
                {
                    String tmp_uri = "www/" + iconname;
                    InputStream image = sAssetMgr.open(tmp_uri);
                    action.mIcon = Drawable.createFromStream(image, iconname);
                }
                catch (IOException e)
                {
                }
            }

            return action;
        }
        catch (JSONException e)
        {
            Log.v("Cambie", Log.getStackTraceString(e));
            return null;
        }
    }

    public static void setAssetManager(AssetManager mgr)
    {
        sAssetMgr = mgr;
    }

    public String getCallbackId()
    {
        return mCallbackId;
    }

    public int getFlags()
    {
        return this.mFlags;
    }

    public Drawable getIcon()
    {
        return this.mIcon;
    }

    public int getOrder()
    {
        return this.mOrder;
    }

    public String getTitle()
    {
        return this.mTitle;
    }

    public Boolean isDisabled()
    {
        return this.mDisabled;
    }

    public Boolean isSelected()
    {
        return this.mSelected;
    }

    public void setFlags(int flags)
    {
        this.mFlags = flags;
    }

    public void setOrder(int order)
    {
        this.mOrder = order;
    }
}


@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class Cambie extends CordovaPlugin implements TabListener
{
    /**
     * The DroidGap Activity instance.
     */
    private DroidGap mDroidGap;

    /**
     * The AssetManager for accessing images in the www folder.
     */
    private AssetManager mAssets;

    /**
     * The ActionBar we are dealing with.
     */
    private ActionBar mBar;

    /**
     * The Options Menu for the app.
     */
    private Menu mMenu;

    /**
     * The menu items and action items shown in the ActionBar.
     */
    private HashMap<String, Actionable> mMenuItems;



    private enum NavType {
        NONE("none"),
        BACK("back"),
        MENU("menu"),
        CLOSE("close");

        private String mKey;

        public static NavType parse(String key)
        {
            if (key != null)
            {
                for (NavType nt : NavType.values())
                {
                    if (nt.mKey.equalsIgnoreCase(key))
                    {
                        return nt;
                    }
                }
            }

            throw new IllegalArgumentException("Invalid Navigation Type");
        }


        private NavType(String key)
        {
            mKey = key;
        }

        public String ToString()
        {
            return mKey;
        }
    }



    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView)
    {
        super.initialize(cordova, webView);

        Log.v("Cambie", "Initializing");

        mDroidGap = (DroidGap)cordova.getActivity();
        mBar = mDroidGap.getActionBar();
        /* Hide the ActionBar until "init" is called from JS */
        mDroidGap.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mBar.hide();
            }
        });

        mAssets = mDroidGap.getAssets();
        mMenuItems = new HashMap<String, Actionable>();

        Actionable.setAssetManager(mAssets);

    }



// --- TabListener Interface Methods ------------------------------------------

    /**
     * Run the tab callback when the tab is selected.
     */
    public void onTabSelected(Tab tab, FragmentTransaction ft)
    {
        String callbackId = (String)tab.getTag();

        if (callbackId == null) {
            return;
        }

        PluginResult res = new PluginResult(PluginResult.Status.OK);
        res.setKeepCallback(true);
        this.webView.sendPluginResult(res, callbackId);
    }

    /**
     * Run the tab callback when the tab is re-selected.
     */
    public void onTabReselected(Tab tab, FragmentTransaction ft)
    {
        String callbackId = (String)tab.getTag();

        if (callbackId == null) {
            return;
        }

        PluginResult res = new PluginResult(PluginResult.Status.OK);
        res.setKeepCallback(true);
        this.webView.sendPluginResult(res, callbackId);
    }

    /** Do nothing when the tab is unselected. */
    public void onTabUnselected(Tab tab, FragmentTransaction ft) { }



// --- Cordova Plugin Interface Methods ---------------------------------------

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callback)
    {
        try
        {
            if (action.equals("init"))
            {
                // Nothing to do for init work on Android
                callback.success();
                return true;
            }
            else if (action.equals("update"))
            {
                JSONObject jsobj;
                Boolean force = false;

                if (args.length() > 0) {
                    jsobj = args.getJSONObject(0);

                    if (args.length() > 1) {
                        force = args.getBoolean(1);
                    }
                }
                else
                {
                    jsobj = new JSONObject();
                }

                /* Check for the title string. */
                String title = jsobj.optString("title");
                if (!title.isEmpty() || force) {
                    this.updateTitle(title);
                }

                String nav = jsobj.optString("nav", "none");
                NavType nt = NavType.parse(nav);
                this.updateNavType(nt);

                if (force)
                {
                    mMenuItems.clear();
                }

                JSONArray acts = jsobj.optJSONArray("actions");
                if (acts != null)
                {
                    this.updateButtons(acts);
                }

                JSONArray menu = jsobj.optJSONArray("menu");
                if (menu != null)
                {
                    this.updateMenu(menu);
                }

                JSONArray tabs = jsobj.optJSONArray("tabs");
                if (tabs != null)
                {
                    this.updateTabs(tabs);
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                {
                    mDroidGap.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mDroidGap.invalidateOptionsMenu();
                        }
                    });
                }

                callback.success();
                return true;
            }
            else if (action.equals("hide"))
            {
                mDroidGap.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mBar.hide();
                    }
                });

                callback.success();
                return true;
            }
            else if (action.equals("show"))
            {
                mDroidGap.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mBar.show();
                    }
                });

                callback.success();
                return true;
            }
            else
            {
                Log.v("Cambie", "Tried to call " + action);
                callback.sendPluginResult(new PluginResult(PluginResult.Status.INVALID_ACTION));
                return false;
            }
        }
        catch (JSONException e)
        {
            Log.v("Cambie", Log.getStackTraceString(e));

            e.printStackTrace();
            callback.sendPluginResult(new PluginResult(PluginResult.Status.JSON_EXCEPTION));
            return false;
        }
    }

    @Override
    public Object onMessage(String id, Object data)
    {
        if (id.equals("onCreateOptionsMenu") || id.equals("onPrepareOptionsMenu"))
        {
            mMenu = (Menu)data;

            mMenu.clear();
            this.buildMenu();
        }
        else if (id.equals("onOptionsItemSelected"))
        {
            MenuItem mi = (MenuItem)data;

            if (mMenuItems.containsKey(mi.getTitle()))
            {
                Actionable act = mMenuItems.get(mi.getTitle());

                PluginResult res = new PluginResult(PluginResult.Status.OK);
                res.setKeepCallback(true);
                this.webView.sendPluginResult(res, act.getCallbackId());
            }
            else
            {
                Log.v("Cambie", "Selected unknown menu item: " + mi.getTitle());
            }
        }

        return null;
    }



// --- Private Plugin Methods -------------------------------------------------

    private void updateTitle(final String title)
    {
        mDroidGap.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mBar.setTitle(title);
            }
        });
    }


    private void updateNavType(final NavType nav)
    {
        final Boolean enabled = (nav != NavType.NONE);

        mDroidGap.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mBar.setDisplayHomeAsUpEnabled(enabled);
            }
        });
    }


    private void updateButtons(final JSONArray buttons)
    {
        for (int i = 0; i < buttons.length(); i++)
        {
            try
            {
                JSONObject btn = buttons.getJSONObject(i);
                Actionable action = Actionable.fromJSON(btn);

                action.setFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

                mMenuItems.put(action.getTitle(), action);
            }
            catch (Exception e)
            {
                Log.v("Cambie", Log.getStackTraceString(e));
            }
        }
    }


    private void updateMenu(final JSONArray menu)
    {
        for (int i = 0; i < menu.length(); i++)
        {
            try
            {
                JSONObject mi = menu.getJSONObject(i);
                Actionable action = Actionable.fromJSON(mi);

                action.setOrder(i);

                mMenuItems.put(action.getTitle(), action);
            }
            catch (Exception e)
            {
                Log.v("Cambie", Log.getStackTraceString(e));
            }
        }
    }

    private void updateTabs(final JSONArray tabs)
    {
        for (int i = 0; i < tabs.length(); i++)
        {
            try
            {
                JSONObject mi = tabs.getJSONObject(i);
                final Actionable action = Actionable.fromJSON(mi);
                final TabListener self = this;

                mDroidGap.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Tab t = mBar.newTab();
                        t.setText(action.getTitle());
                        t.setTabListener(self);
                        t.setTag(action.getCallbackId());
                        if (action.getIcon() != null) {
                            t.setIcon(action.getIcon());
                        }
                        mBar.addTab(t);

                        if (action.isSelected())
                        {
                            mBar.selectTab(t);
                        }
                    }
                });

            }
            catch (Exception e)
            {
                Log.v("Cambie", Log.getStackTraceString(e));
            }
        }

        if (tabs.length() > 0)
        {
            mDroidGap.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
                }
            });
        }
    }

    public void buildMenu()
    {
        for (Actionable act : mMenuItems.values())
        {
            MenuItem mi = mMenu.add(Menu.NONE, Menu.NONE, act.getOrder(), act.getTitle());

            mi.setTitleCondensed(act.getTitle());
            mi.setShowAsAction(act.getFlags());

            if (act.isDisabled())
            {
                mi.setEnabled(false);
            }

            if (act.getIcon() != null)
            {
                mi.setIcon(act.getIcon());
            }
        }
    }
}
