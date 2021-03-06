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
package com.argosnotary.argos.service.adapter.out.mongodb.hierarchy;

import com.mongodb.client.result.UpdateResult;
import com.argosnotary.argos.domain.ArgosError;
import com.argosnotary.argos.domain.hierarchy.Label;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.Optional;

import static com.argosnotary.argos.service.adapter.out.mongodb.hierarchy.LabelRepositoryImpl.COLLECTION;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LabelRepositoryImplTest {

    private static final String LABEL_ID = "labelId";
    private static final String LABEL_NAME = "labelName";
    private static final String PARENT_LABEL_ID = "parentLabelId";

    @Mock
    private MongoTemplate template;
    private LabelRepositoryImpl repository;

    @Mock
    private Label label;

    @Captor
    private ArgumentCaptor<Query> queryArgumentCaptor;

    @Captor
    private ArgumentCaptor<Update> updateArgumentCaptor;

    @Mock
    private MongoConverter converter;

    @Mock
    private UpdateResult updateResult;

    @Mock
    private DuplicateKeyException duplicateKeyException;

    @BeforeEach
    void setUp() {
        repository = new LabelRepositoryImpl(template);
    }

    @Test
    void save() {
        repository.save(label);
        verify(template).save(label, COLLECTION);
    }

    @Test
    void saveDuplicateKeyException() {
        when(label.getName()).thenReturn(LABEL_NAME);
        when(label.getParentLabelId()).thenReturn(PARENT_LABEL_ID);
        doThrow(duplicateKeyException).when(template).save(label, COLLECTION);
        ArgosError argosError = assertThrows(ArgosError.class, () -> repository.save(label));
        assertThat(argosError.getMessage(), is("label with name: labelName and parentLabelId: parentLabelId already exists"));
        assertThat(argosError.getCause(), sameInstance(duplicateKeyException));
        assertThat(argosError.getLevel(), is(ArgosError.Level.WARNING));
    }

    @Test
    void findById() {
        when(template.findOne(any(), eq(Label.class), eq(COLLECTION))).thenReturn(label);
        assertThat(repository.findById(LABEL_ID), is(Optional.of(label)));
        verify(template).findOne(queryArgumentCaptor.capture(), eq(Label.class), eq(COLLECTION));
        assertThat(queryArgumentCaptor.getValue().toString(), is("Query: { \"labelId\" : \"labelId\"}, Fields: {}, Sort: {}"));
    }

    @Test
    void exists() {
        when(template.exists(any(), eq(Label.class), eq(COLLECTION))).thenReturn(true);
        assertThat(repository.exists(LABEL_ID), is(true));
        verify(template).exists(queryArgumentCaptor.capture(), eq(Label.class), eq(COLLECTION));
        assertThat(queryArgumentCaptor.getValue().toString(), is("Query: { \"labelId\" : \"labelId\"}, Fields: {}, Sort: {}"));
    }

    @Test
    void deleteById() {
        repository.deleteById(LABEL_ID);
        verify(template).remove(queryArgumentCaptor.capture(), eq(COLLECTION));
        assertThat(queryArgumentCaptor.getValue().toString(), is("Query: { \"labelId\" : \"labelId\"}, Fields: {}, Sort: {}"));
    }

    @Test
    void updateFound() {
        when(template.getConverter()).thenReturn(converter);
        when(template.updateFirst(any(), any(), eq(Label.class), eq(COLLECTION))).thenReturn(updateResult);
        when(updateResult.getMatchedCount()).thenReturn(1L);
        Optional<Label> update = repository.update(LABEL_ID, label);
        assertThat(update, is(Optional.of(label)));
        verify(label).setLabelId(LABEL_ID);
        verify(template).updateFirst(queryArgumentCaptor.capture(), updateArgumentCaptor.capture(), eq(Label.class), eq(COLLECTION));
        assertThat(queryArgumentCaptor.getValue().toString(), is("Query: { \"labelId\" : \"labelId\"}, Fields: {}, Sort: {}"));
        verify(converter).write(eq(label), any());
        assertThat(updateArgumentCaptor.getValue().toString(), is("{}"));
    }

    @Test
    void updateNotFound() {
        when(template.getConverter()).thenReturn(converter);
        when(template.updateFirst(any(), any(), eq(Label.class), eq(COLLECTION))).thenReturn(updateResult);
        when(updateResult.getMatchedCount()).thenReturn(0L);
        Optional<Label> update = repository.update(LABEL_ID, label);
        assertThat(update, is(Optional.empty()));
    }

    @Test
    void updateDuplicateKeyException() {
        when(template.getConverter()).thenReturn(converter);
        when(template.updateFirst(any(), any(), eq(Label.class), eq(COLLECTION))).thenThrow(duplicateKeyException);
        ArgosError argosError = assertThrows(ArgosError.class, () -> repository.update(LABEL_ID, label));
        assertThat(argosError.getCause(), sameInstance(duplicateKeyException));
    }
}