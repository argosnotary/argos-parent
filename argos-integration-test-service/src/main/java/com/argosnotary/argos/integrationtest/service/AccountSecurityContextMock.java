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
package com.argosnotary.argos.integrationtest.service;

import com.argosnotary.argos.domain.account.Account;
import com.argosnotary.argos.domain.permission.Permission;
import com.argosnotary.argos.service.domain.security.AccountSecurityContext;
import com.argosnotary.argos.service.domain.security.TokenInfo;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
public class AccountSecurityContextMock implements AccountSecurityContext {
    @Override
    public Optional<Account> getAuthenticatedAccount() {
        return Optional.empty();
    }

    @Override
    public Optional<TokenInfo> getTokenInfo() {
        return Optional.empty();
    }

    @Override
    public Set<Permission> getGlobalPermission() {
        return Collections.emptySet();
    }

    @Override
    public Set<Permission> allLocalPermissions(List<String> labelIds) {
        return Collections.emptySet();
    }
}
