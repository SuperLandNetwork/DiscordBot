/*
 * MIT License
 *
 * Copyright (c) 2019 Filli Group (Einzelunternehmen)
 * Copyright (c) 2019 Filli IT (Einzelunternehmen)
 * Copyright (c) 2019 Filli Games (Einzelunternehmen)
 * Copyright (c) 2019 Ursin Filli
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package de.superlandnetwork.discord.bot;

import de.superlandnetwork.discord.bot.listeners.MemberBanListener;
import de.superlandnetwork.discord.bot.listeners.MemberJoinListener;
import de.superlandnetwork.discord.bot.listeners.MemberLeaveListener;
import de.superlandnetwork.discord.bot.listeners.MemberUnbanListener;
import de.superlandnetwork.discord.bot.utils.Config;
import de.superlandnetwork.discord.bot.utils.GroupsEnum;
import de.superlandnetwork.discord.bot.utils.MySQL;
import org.javacord.api.AccountType;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.activity.ActivityType;
import org.javacord.api.entity.permission.Permissions;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class Main {

    public static MySQL mySQL;

    public static void main(String[] args) {
        Properties settings = new Config().getSettingsProps();
        mySQL = new MySQL(settings.getProperty("host"), settings.getProperty("port"), settings.getProperty("database"), settings.getProperty("username"), settings.getProperty("password"));

        DiscordApi api = new DiscordApiBuilder().setToken(settings.getProperty("key")).setAccountType(AccountType.BOT).login().join();

        try {
            mySQL.connect();
            System.out.println("MySQL Connected");
        } catch (SQLException e) {
            api.disconnect();
            System.err.println("MySQL Failed");
            System.exit(1);
        }

        api.updateActivity(ActivityType.PLAYING, "SuperLandNetwork.de");

        api.addMessageCreateListener(event -> {
            if (event.isPrivateMessage()) {
                String msg = event.getMessageContent();
                if (msg.startsWith("!")) {
                    if (msg.equalsIgnoreCase("!ping")) {
                        event.getChannel().sendMessage("Pong!");
                    }
                    return;
                }
                event.getMessage().getUserAuthor().ifPresent(user -> {
                    if (user.isBot()) return;
                    if (msg.equalsIgnoreCase("abort")) {
                        cancleVerify(user);
                        user.sendMessage("Verifizierung abgebrochen!");
                        return;
                    }
                    checkVerify(api, user, msg);
                });
            }
        });

        System.out.println("You can invite the bot by using the following url: " + api.createBotInvite(Permissions.fromBitmask(402861058)));

        api.addServerMemberJoinListener(new MemberJoinListener());
        api.addServerMemberLeaveListener(new MemberLeaveListener());
        api.addServerMemberBanListener(new MemberBanListener());
        api.addServerMemberUnbanListener(new MemberUnbanListener());

        api.addServerJoinListener(event -> System.out.println("Joined server " + event.getServer().getName()));
        api.addServerLeaveListener(event -> System.out.println("Left server " + event.getServer().getName()));

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                checkVerifyUsers(api);
            }
        }, 60000L, 1800000L);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                sendVerify(api);
            }
        }, 60000L, 60000L);
    }

    private static void checkVerify(DiscordApi api, User user, String name) {
        String id = user.getDiscriminatedName();
        try {
            String sql = "SELECT name,uuid FROM `sln_verify` WHERE `send` = 1 AND `type` = 2 AND `content` = '" + id + "'";
            ResultSet rs = mySQL.getResult(sql);
            if (rs.next()) {
                if (rs.getString("name").equalsIgnoreCase(name)) {
                    cancleVerify(user);
                    String sql2 = "UPDATE `sln_users` SET `discord`='" + user.getId() + "' WHERE `uuid`='" + rs.getString("uuid") + "'";
                    mySQL.update(sql2);
                    api.getServerById(316274214598475776L).ifPresent(server -> {
                        server.getMemberByDiscriminatedName(id).ifPresent(user1 -> {
                            server.getRoleById(GroupsEnum.VERIFY.getId()).ifPresent(user1::addRole);
                            checkUserRoles(server, user1);
                        });
                    });
                    user.sendMessage("Deine Identität wurde bestätigt");
                } else
                    user.sendMessage("Falscher Minecraft-Name!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void cancleVerify(User user) {
        String id = user.getDiscriminatedName();
        try {
            String sql = "DELETE FROM `sln_verify` WHERE `send` = 1 AND `type` = 2 AND `content` = '" + id + "'";
            mySQL.update(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void sendVerify(DiscordApi api) {
        try {
            String sql = "SELECT id,content FROM `sln_verify` WHERE `send` = 0 AND `type` = 2";
            ResultSet rs = mySQL.getResult(sql);
            while (rs.next()) {
                String sql2 = "UPDATE `sln_verify` SET `send` = 1 WHERE `id` = '" + rs.getInt("id") + "'";
                mySQL.update(sql2);
                api.getCachedUserByDiscriminatedNameIgnoreCase(rs.getString("content"))
                        .ifPresent(user -> {
                            user.sendMessage("Bitte sende deinen Minecraft-Namen in den Chat, um die Verifizierung deiner Identität abzuschliessen!");
                            user.sendMessage("Falls diese Verifizierung nicht von dir stammt sende bitte ''abort'' im Chat!");
                        });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void checkVerifyUsers(DiscordApi api) {
        api.getServerById(316274214598475776L).ifPresent(server -> {
            for (User user : server.getMembers()) {
                if (user.isBot()) continue;
                Optional<Role> r = api.getRoleById(GroupsEnum.VERIFY.getId());
                if (!r.isPresent()) return;
                if (!user.getRoles(server).contains(r.get())) continue;
                checkUserRoles(server, user);
            }
        });
    }

    private static void checkUserRoles(Server server, User user) {
        List<Integer> l = new ArrayList<>();
        try {
            l = getUser(user.getId());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        for (Role r : user.getRoles(server)) {
            int mcId = GroupsEnum.getMcId(r.getId());
            if (mcId != 0) {
                if (!l.contains(mcId))
                    user.removeRole(r);
            }
        }
        for (int list : l) {
            server.getRoleById(GroupsEnum.getId(list)).ifPresent(role -> {
                if (!user.getRoles(server).contains(role))
                    user.addRole(role);
            });
            if (GroupsEnum.isStaff(list))
                server.getRoleById(GroupsEnum.DIVIDE.getId()).ifPresent(role -> {
                    if (!user.getRoles(server).contains(role))
                        user.addRole(role);
                });
        }
    }

    private static List<Integer> getUser(long id) throws SQLException {
        List<Integer> groupIds = new ArrayList<>();
        UUID uuid = getUUID(id);
        if (uuid != null) {
            String sql = "SELECT groupId FROM `sln_mc_perm_users` WHERE `deleted_at` IS NULL AND `uuid` = '" + uuid.toString() + "'";
            ResultSet rs = mySQL.getResult(sql);
            while (rs.next()) {
                groupIds.add(rs.getInt("groupId"));
            }
        }
        return groupIds;
    }

    private static UUID getUUID(long id) throws SQLException {
        String sql = "SELECT uuid FROM `sln_users` WHERE `deleted_at` IS NULL AND `discord` = '" + id + "'";
        ResultSet rs = mySQL.getResult(sql);
        if (rs.next())
            return UUID.fromString(rs.getString("uuid"));
        return null;
    }

}
