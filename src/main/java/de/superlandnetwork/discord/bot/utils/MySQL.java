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

package de.superlandnetwork.discord.bot.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MySQL {

    private static Connection connection;
    private String host, port, database, username, password;

    /**
     * @param host     Host
     * @param port     Port
     * @param database Database
     * @param username Username
     * @param password Password
     */
    public MySQL(String host, String port, String database, String username, String password) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
    }

    /**
     * Connect to Database Server
     *
     * @throws SQLException In Case of Error
     */
    public void connect() throws SQLException {
        if (isConnected())
            close();
        connection = DriverManager.getConnection(
                "jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database + "?autoReconnect=true",
                this.username, this.password);
    }

    /**
     * Close Database Connection
     *
     * @throws SQLException In Case of Error
     */
    public void close() throws SQLException {
        if (isConnected())
            connection.close();
    }

    /**
     * Check if a Connection exists to Database Server
     *
     * @return status
     * @throws SQLException In Case of Error
     */
    public boolean isConnected() throws SQLException {
        if (connection != null)
            return !connection.isClosed();
        return false;
    }

    /**
     * Update Data
     *
     * @param sql Sql String
     * @return Amount of Updated Data
     * @throws SQLException In Case of Error
     */
    public int update(String sql) throws SQLException {
        if (!isConnected())
            connect();
        return connection.prepareStatement(sql).executeUpdate();
    }

    /**
     * Get Data
     *
     * @param sql Sql String
     * @return Data
     * @throws SQLException In Case of Error
     */
    public ResultSet getResult(String sql) throws SQLException {
        if (!isConnected())
            connect();
        return connection.prepareStatement(sql).executeQuery();
    }

}
