package com.tubes.kantincepat.client;

import com.tubes.kantincepat.client.view.KitchenHistoryPanel;
import com.tubes.kantincepat.client.view.KitchenPanel;
import com.tubes.kantincepat.client.view.KitchenLoginPanel;
import com.tubes.kantincepat.client.view.LiveChat;

import javax.swing.*;
import java.awt.*;

public class KitchenApp extends JFrame {

    public static final int MOBILE_WIDTH = 900;
    public static final int MOBILE_HEIGHT = 600;

    public static final int TABLET_WIDTH = 900;
    public static final int TABLET_HEIGHT = 600;

    public static final Color COLOR_BG = new Color(248, 248, 252);
    public static final Color COLOR_ACCENT = new Color(246, 237, 243);
    public static final Color COLOR_PRIMARY = new Color(212, 163, 196);

    private CardLayout cardLayout;
    private JPanel mainPanel;

    private LiveChat liveChatPanel;
    private KitchenPanel kitchenPanel;
    private KitchenHistoryPanel kitchenHistoryPanel;

    private int currentUserId;
    private String currentUserRole;
    private String currentUserName;

    public void setCurrentUser(int userId, String role, String fullName) {
        this.currentUserId = userId;
        this.currentUserRole = role;
        this.currentUserName = fullName;
    }

    public int getCurrentUserId() {
        return currentUserId;
    }

    public String getCurrentUserRole() {
        return currentUserRole;
    }

    public String getCurrentUserName() {
        return currentUserName;
    }

    public KitchenApp() {
        setTitle("Kantin Pintar");
        setSize(TABLET_WIDTH, TABLET_HEIGHT);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // auth
        mainPanel.add(new KitchenLoginPanel(this), "LOGIN");
        
        // Satu-satunya instance
        kitchenPanel = new KitchenPanel(this);
        kitchenHistoryPanel = new KitchenHistoryPanel(this);

        System.out.println("[KitchenApp] kitchenHistoryPanel instance = " +
                System.identityHashCode(kitchenHistoryPanel));

        mainPanel.add(kitchenPanel, "KITCHEN");
        mainPanel.add(kitchenHistoryPanel, "KITCHEN_HISTORY");

        // live chat
        // liveChatPanel = new LiveChat(this);
        // mainPanel.add(liveChatPanel, "LIVE_CHAT");

        add(mainPanel);
    }

    public LiveChat getLiveChatPanel() {
        return liveChatPanel;
    }

    public void showView(String viewName) {
        cardLayout.show(mainPanel, viewName);
    }

    public void showKitchen() {
        if (kitchenPanel != null) {
            kitchenPanel.refreshOrders();
        }
        showView("KITCHEN");
    }

    public void showKitchenHistory() {
        if (kitchenHistoryPanel != null) {
            System.out.println("[KitchenApp] showKitchenHistory() instance = " +
                    System.identityHashCode(kitchenHistoryPanel));
            kitchenHistoryPanel.refreshHistory();
        }
        showView("KITCHEN_HISTORY");
    }

    public void switchToKitchenView() {
        setSize(TABLET_WIDTH, TABLET_HEIGHT);
        setLocationRelativeTo(null);
        setTitle("Kantin Pintar - Dapur");
        showKitchen();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new KitchenApp().setVisible(true);
        });
    }
}
