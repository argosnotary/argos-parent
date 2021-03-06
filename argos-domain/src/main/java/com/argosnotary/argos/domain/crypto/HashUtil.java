/*
 * Argos Notary - A new way to secure the Software Supply Chain
 *
 * Copyright (C) 2019 - 2020 Rabobank Nederland
 * Copyright (C) 2019 - 2021 Gerard Borst <gerard.borst@argosnotary.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.argosnotary.argos.domain.crypto;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.input.UnixLineEndingInputStream;

import com.argosnotary.argos.domain.ArgosError;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HashUtil {

    public static String createHash(InputStream inputStream, String filename, boolean normalizeLineEndings) {
        MessageDigest digest = DigestUtils.getSha256Digest();
        try {
            InputStream file = normalizeLineEndings ? new UnixLineEndingInputStream(inputStream, false) : inputStream;
            byte[] buffer = new byte[2048];
            int len;
            while ((len = file.read(buffer)) > -1) {
                digest.update(buffer, 0, len);
            }
        } catch (IOException e) {
            throw new ArgosError("The file " + filename + " couldn't be recorded: " + e.getMessage());
        }
        return Hex.encodeHexString(digest.digest());
    }
}
