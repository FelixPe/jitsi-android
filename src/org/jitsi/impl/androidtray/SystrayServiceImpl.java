/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jitsi.impl.androidtray;

import android.content.*;
import net.java.sip.communicator.service.contactlist.*;
import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.service.systray.*;
import net.java.sip.communicator.service.systray.event.*;

import net.java.sip.communicator.util.*;
import org.jitsi.android.*;
import org.jitsi.android.gui.*;
import org.jitsi.android.gui.chat.*;
import org.jitsi.android.plugin.notificationwiring.*;
import org.jitsi.service.osgi.*;

/**
 * Android system tray implementation. Makes use of status bar notifications to
 * provide tray functionality.
 *
 * @author Pawel Domas
 */
public class SystrayServiceImpl
    extends AbstractSystrayService
{
    /**
     *  The logger
     */
    private static final Logger logger
            = Logger.getLogger(SystrayServiceImpl.class);

    /**
     * Id of Jitsi icon notification
     */
    private static int generalNotificationId = -1;

    /**
     * Popup message handler.
     */
    private final NotificationPopupHandler trayPopupHandler
            = new NotificationPopupHandler();
    /**
     * Popup message click listener impl.
     */
    private final PopupListenerImpl popupMessageListener
            = new PopupListenerImpl();

    /**
     * <tt>BroadcastReceiver</tt> that catches "on click" and "clear" events for
     * displayed notifications.
     */
    private final PopupClickReceiver clickReceiver;

    /**
     * Creates new instance of <tt>SystrayServiceImpl</tt>.
     */
    public SystrayServiceImpl()
    {
        super(AndroidTrayActivator.bundleContext);

        AndroidTrayActivator.bundleContext.registerService(
                PopupMessageHandler.class,
                trayPopupHandler, null );

        initHandlers();

        this.clickReceiver = new PopupClickReceiver(trayPopupHandler);
    }

    /**
     * Set the handler which will be used for popup message
     * @param newHandler the handler to set. providing a null handler is like
     * disabling popup.
     * @return the previously used popup handler.
     */
    public PopupMessageHandler setActivePopupMessageHandler(
            PopupMessageHandler newHandler)
    {
        PopupMessageHandler oldHandler = getActivePopupHandler();

        if (oldHandler != null)
            oldHandler.removePopupMessageListener(popupMessageListener);
        if (newHandler != null)
            newHandler.addPopupMessageListener(popupMessageListener);

        return super.setActivePopupMessageHandler(newHandler);
    }

    /**
     * Starts the service.
     */
    public void start()
    {
        clickReceiver.registerReceiver();
    }

    /**
     * Stops the service.
     */
    public void stop()
    {
        clickReceiver.unregisterReceiver();

        trayPopupHandler.dispose();
    }

    /**
     * {@inheritDoc}
     */
    public void setSystrayIcon(int i)
    {
        System.err.println("Requested icon: "+i);
        switch (i)
        {
            case SC_IMG_AWAY_TYPE:
                //TODO: set away icon here
                break;
            case SC_IMG_DND_TYPE:
                //TODO: dnd icon here
                break;
            case SC_IMG_FFC_TYPE:
                //TODO: set FFC icon here
                break;
            case SC_IMG_OFFLINE_TYPE:
                //TODO: set offline icon here
                break;
            case SC_IMG_TYPE:
                //TODO: set image icon here
                break;
            case ENVELOPE_IMG_TYPE:
                //TODO: set envelope icon here
                break;
        }
    }

    /**
     * Returns id of general notification that is bound to Jitsi icon.
     * @return id of general notification that is bound to Jitsi icon.
     */
    public static int getGeneralNotificationId()
    {
        int serviceIcondId = OSGiService.getGeneralNotificationId();

        // Use service icon if available
        if(serviceIcondId != -1 && generalNotificationId != serviceIcondId)
        {
            generalNotificationId = serviceIcondId;
        }

        // There is not service icon available
        if(generalNotificationId == -1)
        {
            generalNotificationId
                = (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
        }
        return generalNotificationId;
    }

    /**
     * Class implements <tt>SystrayPopupMessageListener</tt> in order to display
     * chat <tt>Activity</tt> when popup is clicked.
     */
    private class PopupListenerImpl implements SystrayPopupMessageListener
    {

        /**
         * Indicates that user has clicked on the systray popup message.
         *
         * @param evt the event triggered when user clicks on the systray popup
         *            message
         */
        public void popupMessageClicked(SystrayPopupMessageEvent evt)
        {
            Object src = evt.getSource();
            PopupMessage message = null;
            if(src instanceof PopupMessage)
            {
                message = (PopupMessage) evt.getSource();
            }

            Object tag = evt.getTag();
            if(tag instanceof Contact)
            {
                Contact contact = (Contact) tag;
                MetaContact metaContact
                    = AndroidGUIActivator.getContactListService()
                        .findMetaContactByContact(contact);
                if(metaContact == null)
                {
                    logger.error("Meta contact not found for "+contact);
                    return;
                }

                Intent targetIntent;
                String group = message != null ? message.getGroup() : null;

                if(AndroidNotifications.MESSAGE_GROUP.equals(group))
                {
                    targetIntent
                        = ChatSessionManager.getChatIntent(metaContact);

                    if(targetIntent == null)
                    {
                        logger.error(
                            "Failed to create chat with " + metaContact);
                    }
                }
                else
                {
                    // TODO: add call history intent here
                    targetIntent = null;
                }

                if(targetIntent != null)
                {
                    JitsiApplication
                        .getGlobalContext().startActivity(targetIntent);
                }

                return;
            }

            // Displays popup message details when the notification is clicked
            if(message != null)
            {
                DialogActivity.showDialog(
                        JitsiApplication.getGlobalContext(),
                        message.getMessageTitle(),
                        message.getMessage());
            }
        }
    }
}
