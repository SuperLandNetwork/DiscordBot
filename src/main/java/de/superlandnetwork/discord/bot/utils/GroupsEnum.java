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

public enum GroupsEnum {

    VERIFY(649261218913320971L, 0, false),
    DIVIDE(649260975404613645L, 0, false),
    USER(649260975404613645L, 1, false),
    PREMIUM(649265212301639719L, 2, false),
    YOUTUBE(PREMIUM.getId(), 3, false),
    TWITCH(PREMIUM.getId(), 4, false),
    VIP(PREMIUM.getId(), 5, false),
    STAFF(646005216231292929L, 6, true),
    BUILDER(649266092996427806L, 7, true),
    SUPPORTER(649266037027635201L, 8, true),
    MODERATOR(649265965816872981L, 9, true),
    DEVELOPER(649264108906086410L, 10, true),
    ADMINISTRATOR(646005573434867733L, 11, true);

    private long id;
    private int mcId;
    private boolean staff;

    GroupsEnum(long id, int mcId, boolean staff) {
        this.id = id;
        this.mcId = mcId;
        this.staff = staff;
    }

    public static int getMcId(long id) {
        for (GroupsEnum e : GroupsEnum.values()) {
            if (e.getId() == id)
                return e.getMcId();
        }
        return 0;
    }

    public static long getId(int mcId) {
        for (GroupsEnum e : GroupsEnum.values()) {
            if (e.getMcId() == mcId)
                return e.getId();
        }
        return 0;
    }

    public static boolean isStaff(int mcId) {
        for (GroupsEnum e : GroupsEnum.values()) {
            if (e.getMcId() == mcId)
                return e.isStaff();
        }
        return false;
    }

    public long getId() {
        return id;
    }

    public int getMcId() {
        return mcId;
    }

    public boolean isStaff() {
        return staff;
    }

}
