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
package com.argosnotary.argos.service.adapter.out.mongodb.account;

import com.argosnotary.argos.domain.account.PersonalAccount;
import com.argosnotary.argos.domain.permission.Role;
import com.argosnotary.argos.service.domain.account.AccountSearchParams;
import com.argosnotary.argos.service.domain.account.PersonalAccountRepository;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.MongoRegexCreator;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.MongoRegexCreator.MatchMode.CONTAINING;

@Component
@RequiredArgsConstructor
public class PersonalAccountRepositoryImpl implements PersonalAccountRepository {

    public static final String COLLECTION = "personalaccounts";
    public static final String ACCOUNT_ID = "accountId";
    public static final String ACTIVE_KEY_ID_FIELD = "activeKeyPair.keyId";
    public static final String IN_ACTIVE_KEY_ID_FIELD = "inactiveKeyPairs.keyId";

    public static final String EMAIL = "email";
    public static final String NAME_FIELD = "name";
    public static final String ROLE_FIELD = "roles";
    public static final String PERMISSIONS_LABEL_ID_FIELD = "localPermissions.labelId";
    private static final String CASE_INSENSITIVE = "i";
    private final MongoTemplate template;

    @Override
    public Optional<PersonalAccount> findByEmail(String email) {
        return Optional.ofNullable(template.findOne(new Query(where(EMAIL).is(email)), PersonalAccount.class, COLLECTION));
    }

    @Override
    public Optional<PersonalAccount> findByAccountId(String accountId) {
        return Optional.ofNullable(template.findOne(getPrimaryQuery(accountId), PersonalAccount.class, COLLECTION));
    }

    @Override
    public void save(PersonalAccount personalAccount) {
        template.save(personalAccount, COLLECTION);
    }

    @Override
    public void update(PersonalAccount existingPersonalAccount) {
        Query query = getPrimaryQuery(existingPersonalAccount.getAccountId());
        Document document = new Document();
        template.getConverter().write(existingPersonalAccount, document);
        template.updateFirst(query, Update.fromDocument(document), PersonalAccount.class, COLLECTION);
    }

    @Override
    public Optional<PersonalAccount> findByActiveKeyId(String activeKeyId) {
        return Optional.ofNullable(template.findOne(getActiveKeyQuery(activeKeyId), PersonalAccount.class, COLLECTION));
    }

    @Override
    public long getTotalNumberOfAccounts() {
        return template.count(new Query(), PersonalAccount.class, COLLECTION);
    }

    @Override
    public List<PersonalAccount> search(AccountSearchParams params) {
        Query query = params.getLocalPermissionsLabelId().map(this::labelIdQuery)
                                .orElseGet(() -> params.getName().map(this::nameQuery)
                                        .orElseGet(() -> params.getActiveKeyIds().map(this::activeKeyIdsQuery)
                                                .orElseGet(() -> params.getInActiveKeyIds().map(this::inActiveKeyIdsQuery).orElseGet(Query::new))));
        query.fields().include(ACCOUNT_ID).include(EMAIL).include(NAME_FIELD);
        return template.find(query.with(Sort.by(NAME_FIELD)), PersonalAccount.class, COLLECTION);
    }

    @Override
    public List<PersonalAccount> searchWithRoles(AccountSearchParams params) {
        Query query = params.getRole().map(this::roleQuery)
                .orElseGet(() -> params.getName().map(this::nameQuery)
                                        .orElseGet(Query::new));
        query.fields().include(ACCOUNT_ID).include(EMAIL).include(NAME_FIELD).include(ROLE_FIELD);
        return template.find(query.with(Sort.by(NAME_FIELD)), PersonalAccount.class, COLLECTION);
    }

    private Query activeKeyIdsQuery(List<String> keyIds) {
        return new Query(Criteria.where(ACTIVE_KEY_ID_FIELD).in(keyIds));
    }

    private Query inActiveKeyIdsQuery(List<String> keyIds) {
        return new Query(Criteria.where(IN_ACTIVE_KEY_ID_FIELD).in(keyIds));
    }

    private Query nameQuery(String name) {
        return new Query(where(NAME_FIELD).regex(requireNonNull(MongoRegexCreator.INSTANCE.toRegularExpression(name, CONTAINING)), CASE_INSENSITIVE));
    }

    private Query labelIdQuery(String labelId) {
        return new Query(where(PERMISSIONS_LABEL_ID_FIELD).is(labelId));
    }

    private Query roleQuery(Role role) {
        return new Query(where(ROLE_FIELD).in(role.name()));
    }

    @Override
    public boolean activeKeyExists(String activeKeyId) {
        return template.exists(getActiveKeyQuery(activeKeyId), PersonalAccount.class, COLLECTION);
    }

    private Query getActiveKeyQuery(String activeKeyId) {
        return new Query(Criteria.where(ACTIVE_KEY_ID_FIELD).is(activeKeyId));
    }

    private Query getPrimaryQuery(String userId) {
        return new Query(where(ACCOUNT_ID).is(userId));
    }
}
