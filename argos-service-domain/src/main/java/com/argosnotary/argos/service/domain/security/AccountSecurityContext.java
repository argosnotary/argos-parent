/*
 * Copyright (C) 2020 Argos Notary Coöperatie UA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.argosnotary.argos.service.domain.security;

import com.argosnotary.argos.domain.account.Account;
import com.argosnotary.argos.domain.permission.Permission;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface AccountSecurityContext {
    Optional<Account> getAuthenticatedAccount();

    Optional<TokenInfo> getTokenInfo();

    Set<Permission> getGlobalPermission();

    Set<Permission> allLocalPermissions(List<String> labelIds);
}
