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

package de.superlandnetwork.discord.bot.listeners;

import de.superlandnetwork.discord.bot.Main;
import org.javacord.api.event.server.member.ServerMemberJoinEvent;
import org.javacord.api.exception.MissingPermissionsException;
import org.javacord.api.listener.server.member.ServerMemberJoinListener;
import org.javacord.api.util.logging.ExceptionLogger;

import java.sql.SQLException;

public class MemberJoinListener implements ServerMemberJoinListener {

    @Override
    public void onServerMemberJoin(ServerMemberJoinEvent event) {
        try {
            String sql = "SELECT `id` FROM `sln_discord_users` WHERE `discord` = '" + event.getUser().getId() + "'";
            if (Main.mySQL.getResult(sql).next()) {
                String sql2 = "UPDATE `sln_discord_users` SET `last_name`='" + event.getUser().getDiscriminatedName() + "' WHERE `discord`='" + event.getUser().getId() + "'";
                Main.mySQL.update(sql2);
            } else {
                String sql2 = "INSERT INTO `sln_discord_users` (`discord`, `last_name`) VALUES ('" + event.getUser().getId() + "', '" + event.getUser().getDiscriminatedName() + "')";
                Main.mySQL.update(sql2);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (!event.getServer().getChannelById(646004622397538314L).isPresent()) {
            System.err.println("Channel not Found!");
            return;
        }

        event.getServer().getTextChannelById(646004622397538314L).ifPresent(channel -> {
            channel.sendMessage(event.getUser().getNicknameMentionTag() + " joined the server.").exceptionally(ExceptionLogger.get(MissingPermissionsException.class));
            System.out.println(event.getUser().getName() + " joined the server " + event.getServer().getName());
        });
    }
}
