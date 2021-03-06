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
package com.argosnotary.argos.service.domain.release;

import com.argosnotary.argos.domain.account.AccountKeyInfo;
import com.argosnotary.argos.domain.account.AccountType;
import com.argosnotary.argos.domain.account.KeyInfo;
import com.argosnotary.argos.domain.crypto.PublicKey;
import com.argosnotary.argos.domain.hierarchy.HierarchyMode;
import com.argosnotary.argos.domain.hierarchy.TreeNode;
import com.argosnotary.argos.domain.layout.Layout;
import com.argosnotary.argos.domain.layout.LayoutMetaBlock;
import com.argosnotary.argos.domain.link.Artifact;
import com.argosnotary.argos.domain.link.LinkMetaBlock;
import com.argosnotary.argos.domain.release.ReleaseDossierMetaData;
import com.argosnotary.argos.domain.release.ReleaseResult;
import com.argosnotary.argos.service.domain.account.AccountInfoRepository;
import com.argosnotary.argos.service.domain.hierarchy.HierarchyRepository;
import com.argosnotary.argos.service.domain.layout.LayoutMetaBlockRepository;
import com.argosnotary.argos.service.domain.link.LinkMetaBlockRepository;
import com.argosnotary.argos.service.domain.verification.VerificationProvider;
import com.argosnotary.argos.service.domain.verification.VerificationRunResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class ReleaseServiceImplTest {
    protected static final String SUPPLY_CHAIN_ID = "supplyChainId";
    @Mock
    private VerificationProvider verificationProvider;
    @Mock
    private LayoutMetaBlockRepository layoutMetaBlockRepository;
    @Mock
    private ReleaseRepository releaseRepository;
    @Mock
    private AccountInfoRepository accountInfoRepository;
    @Mock
    private HierarchyRepository hierarchyRepository;
    @Mock
    private LinkMetaBlockRepository linkMetaBlockRepository;

    @Mock
    private TreeNode treeNode;

    @Mock
    private ReleaseDossierMetaData releaseDossierMetaData;

    @Mock
    private VerificationRunResult verificationRunResult;

    @Mock
    private LayoutMetaBlock layoutMetaBlock;

    @Mock
    LinkMetaBlock linkMetaBlock;

    @Mock
    private Layout layout;

    @Mock
    PublicKey publicKey;

    private ReleaseService releaseService;

    @BeforeEach
    void setup() {
        releaseService = new ReleaseServiceImpl(
                verificationProvider,
                layoutMetaBlockRepository,
                releaseRepository,
                accountInfoRepository,
                hierarchyRepository,
                linkMetaBlockRepository);

    }

    @Test
    void createReleaseForExistingDossierShouldReturnStoredDossier() {
        Artifact releaseArtifact = Artifact.builder().hash("hash").uri("/target/").build();
        List<Set<Artifact>> releaseArtifacts = Collections.singletonList(Set.of(releaseArtifact));
        when(treeNode.getName()).thenReturn("name");
        when(treeNode.getPathToRoot()).thenReturn(Collections.singletonList("path"));
        when(hierarchyRepository.getSubTree(SUPPLY_CHAIN_ID, HierarchyMode.NONE, 0)).thenReturn(Optional.of(treeNode));
        when(releaseRepository.findReleaseByReleasedArtifactsAndPath(any(), any())).thenReturn(Optional.of(releaseDossierMetaData));
        ReleaseResult releaseResult = releaseService.createRelease(SUPPLY_CHAIN_ID, releaseArtifacts);
        assertThat(releaseResult.isReleaseIsValid(), is(true));
        assertThat(releaseResult.getReleaseDossierMetaData(), sameInstance(releaseDossierMetaData));
        verifyNoInteractions(layoutMetaBlockRepository, accountInfoRepository, verificationProvider, linkMetaBlockRepository);
    }

    @Test
    void createReleaseForNonExistingLayoutShouldReturnFalse() {
        Artifact releaseArtifact = Artifact.builder().hash("hash").uri("/target/").build();
        List<Set<Artifact>> releaseArtifacts = Collections.singletonList(Set.of(releaseArtifact));
        when(treeNode.getName()).thenReturn("name");
        when(treeNode.getPathToRoot()).thenReturn(Collections.singletonList("path"));
        when(hierarchyRepository.getSubTree(SUPPLY_CHAIN_ID, HierarchyMode.NONE, 0)).thenReturn(Optional.of(treeNode));
        when(releaseRepository.findReleaseByReleasedArtifactsAndPath(any(), any())).thenReturn(Optional.empty());
        when(layoutMetaBlockRepository.findBySupplyChainId(SUPPLY_CHAIN_ID)).thenReturn(Optional.empty());
        ReleaseResult releaseResult = releaseService.createRelease(SUPPLY_CHAIN_ID, releaseArtifacts);
        assertThat(releaseResult.isReleaseIsValid(), is(false));
    }

    @Test
    void createReleaseForNonExistingValidReleaseShouldReturnAndStoreNewDossier() {
        Artifact releaseArtifact = Artifact.builder().hash("hash").uri("/target/").build();
        List<Set<Artifact>> releaseArtifacts = Collections.singletonList(Set.of(releaseArtifact));
        when(treeNode.getName()).thenReturn("name");
        when(treeNode.getPathToRoot()).thenReturn(Collections.singletonList("path"));
        when(hierarchyRepository.getSubTree(SUPPLY_CHAIN_ID, HierarchyMode.NONE, 0)).thenReturn(Optional.of(treeNode));
        when(releaseRepository.findReleaseByReleasedArtifactsAndPath(any(), any())).thenReturn(Optional.empty());
        when(layoutMetaBlockRepository.findBySupplyChainId(SUPPLY_CHAIN_ID)).thenReturn(Optional.of(layoutMetaBlock));
        when(verificationProvider.verifyRun(any(), any())).thenReturn(verificationRunResult);
        when(verificationRunResult.isRunIsValid()).thenReturn(true);
        when(layoutMetaBlock.getLayout()).thenReturn(layout);
        when(layout.getKeys()).thenReturn(Collections.singletonList(publicKey));
        when(publicKey.getKeyId()).thenReturn("keyId");
        AccountKeyInfo accountKeyInfo = AccountKeyInfo
                .builder()
                .accountId("id")
                .accountType(AccountType.PERSONAL_ACCOUNT)
                .name("name")
                .key(KeyInfo.builder().status(KeyInfo.KeyStatus.ACTIVE).keyId("keyId").build())
                .pathToRoot(Collections.singletonList("path"))
                .build();
        when(accountInfoRepository.findByKeyIds(any())).thenReturn(Collections.singletonList(accountKeyInfo));
        when(verificationRunResult.getValidLinkMetaBlocks()).thenReturn(Collections.singletonList(linkMetaBlock));

        ReleaseResult releaseResult = releaseService.createRelease(SUPPLY_CHAIN_ID, releaseArtifacts);
        assertThat(releaseResult.isReleaseIsValid(), is(true));
        assertThat(releaseResult.getReleaseDossierMetaData(), is(notNullValue()));
        verify(releaseRepository).storeRelease(any(), any());
        verify(linkMetaBlockRepository).deleteBySupplyChainId(SUPPLY_CHAIN_ID);
    }


    @Test
    void createReleaseForNonExistingInvalidValidReleaseShouldReturnInvalidResult() {
        Artifact releaseArtifact = Artifact.builder().hash("hash").uri("/target/").build();
        List<Set<Artifact>> releaseArtifacts = Collections.singletonList(Set.of(releaseArtifact));
        when(treeNode.getName()).thenReturn("name");
        when(treeNode.getPathToRoot()).thenReturn(Collections.singletonList("path"));
        when(hierarchyRepository.getSubTree(SUPPLY_CHAIN_ID, HierarchyMode.NONE, 0)).thenReturn(Optional.of(treeNode));
        when(releaseRepository.findReleaseByReleasedArtifactsAndPath(any(), any())).thenReturn(Optional.empty());
        when(layoutMetaBlockRepository.findBySupplyChainId(SUPPLY_CHAIN_ID)).thenReturn(Optional.of(layoutMetaBlock));
        when(verificationProvider.verifyRun(any(), any())).thenReturn(verificationRunResult);
        when(verificationRunResult.isRunIsValid()).thenReturn(false);
        ReleaseResult releaseResult = releaseService.createRelease(SUPPLY_CHAIN_ID, releaseArtifacts);
        assertThat(releaseResult.isReleaseIsValid(), is(false));
        assertThat(releaseResult.getReleaseDossierMetaData(), is(nullValue()));
        verifyNoInteractions(accountInfoRepository, linkMetaBlockRepository);
    }
}