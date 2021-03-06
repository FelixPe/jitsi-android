/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jitsi.android.gui.menu;

import android.os.*;
import android.view.*;

import org.jitsi.*;
import org.jitsi.android.*;
import org.jitsi.android.gui.*;
import org.jitsi.service.osgi.*;

/**
 * Extends this activity to handle exit options menu item.
 *
 * @author Pawel Domas
 */
public abstract class ExitMenuActivity
    extends OSGiActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // Context is null on restore from recent apps after Jitsi was closed
        // using exit option. This is caused by System.exit(0) called on exit
        // which clears all static fields(VM shutdown) including
        // AndroidGUIActivator.bundleContext.
        if(AndroidGUIActivator.bundleContext == null)
        {
            finish();
            startActivity(JitsiApplication.getHomeIntent());
        }

        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.exit_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if(item.getItemId() == R.id.menu_exit)
        {
            // Shutdown application
            // TODO: exit doesn't work while OSGi is starting
            JitsiApplication.shutdownApplication();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
